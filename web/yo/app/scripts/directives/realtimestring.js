'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:driveRealtimeString
 * @description
 * # driveRealtimeString
 */
angular.module('oncokbApp')
    .directive('realtimeString', function ($timeout, _, $rootScope, stringUtils, mainUtils, ReviewResource, $firebaseObject) {
        return {
            templateUrl: 'views/realtimeString.html',
            restrict: 'AE',
            scope: {
                t: '=',
                key: '=',
                path: '=',
                checkboxes: '=',
                o: '=',
                uuid: '='
            },
            replace: true,
            link: {
                pre: function preLink(scope) {
                    $firebaseObject(firebase.database().ref(scope.path)).$bindTo(scope, "data").then(function (success) {
                        if (scope.t === 'treatment-select') {
                            scope.changePropagation(true);
                        }  
                    }, function (error) {
                        console.log('error');
                    }); 
                },
                post: function postLink(scope) {
                    scope.reviewMode = ReviewResource.reviewMode;
                    scope.pContent = '';
                    scope.contentModified = false;
                    scope.$watch('pContent', function(n, o) {
                        if (scope.contentModified && scope.key !== 'short' && n !== o) {
                            // we track the change in two conditions:
                            // 1) When editing happens not in review mode
                            // 2) When editing happends in review mode but not from admin's "Reject" action
                            if (!ReviewResource.reviewMode || ReviewResource.rejected.indexOf(scope.uuid) === -1) {
                                // The first time this piece of data is recorded in review mode
                                if (_.isUndefined(scope.data[scope.key + '_review']) || _.isUndefined(scope.data[scope.key + '_review'].lastReviewed)) {
                                    scope.data[scope.key + '_review'] = {
                                        updatedBy: $rootScope.me.name,
                                        updateTime: new Date().getTime(),
                                        lastReviewed: o
                                    };
                                    $rootScope.metaFire[scope.uuid] = { review: true };
                                    ReviewResource.rollback = _.without(ReviewResource.rollback, scope.uuid);
                                }
                                // If the data was reviewed before
                                if (n === scope.data[scope.key + '_review'].lastReviewed) {
                                    // If the data is changed back to the original value
                                    delete scope.data[scope.key + '_review'].lastReviewed;
                                    delete $rootScope.metaFire[scope.uuid];
                                    // if this kind of change happens inside review mode, we track current section in rollback status to remove the review panel since there is nothing to be approved
                                    if (ReviewResource.reviewMode) {
                                        ReviewResource.rollback.push(scope.uuid);
                                    }
                                }
                            }
                        }

                    });
                }
            },
            controller: function ($scope) {
                $scope.content = {};
                $scope.content.propagationOpts = [];
                $scope.propagationOpts = {
                    'no': {
                        name: 'No level',
                        value: 'no'
                    },
                    '2B': {
                        name: 'Level 2B',
                        value: '2B'
                    },
                    '3B': {
                        name: 'Level 3B',
                        value: '3B'
                    },
                    '4': {
                        name: 'Level 4',
                        value: '4'
                    }
                };
                $scope.changePropagation = function(initialize) {
                    var _propagationOpts = [];
                    if ($scope.data[$scope.key] === '1' || $scope.data[$scope.key] === '2A') {
                        _propagationOpts = [
                            $scope.propagationOpts.no,
                            $scope.propagationOpts['2B'],
                            $scope.propagationOpts['4']
                        ];
                        if (!($scope.content.propagation && initialize)) {
                            $scope.content.propagation = '2B';
                        }
                    } else if ($scope.data[$scope.key] === '3A') {
                        _propagationOpts = [
                            $scope.propagationOpts.no,
                            $scope.propagationOpts['3B'],
                            $scope.propagationOpts['4']
                        ];
                        if (!($scope.content.propagation && initialize)) {
                            $scope.content.propagation = '3B';
                        }
                    } else {
                        $scope.content.propagation = null;
                    }
                    $scope.content.propagationOpts = _propagationOpts;
                };
                $scope.inReviewMode = function () {
                    return ReviewResource.reviewMode;
                };
                $scope.fe = $rootScope.fileEditable;
                function calculateDiff() {
                    if (($scope.t === 'p')) {
                        var dmp = new diff_match_patch();
                        var newContent = stringUtils.getTextString($scope.data[$scope.key]);
                        var oldContent = stringUtils.getTextString($scope.lastReviewed);
                        var diff = dmp.diff_main(oldContent, newContent);
                        dmp.diff_cleanupSemantic(diff);
                        $scope.diffHTML = dmp.diff_prettyHtml(diff);
                    }
                }
                $scope.uncheck = function () {
                    if ($scope.preStringO === $scope.data[$scope.key] && $scope.preStringO !== '') {
                        $scope.data[$scope.key] = '';
                    }
                    $scope.preStringO = $scope.data[$scope.key];
                };
                $scope.getInputClass = function () {
                    if (ReviewResource.reviewMode) {
                        $scope.lastReviewed = $scope.data[$scope.key + '_review'].lastReviewed;
                        calculateDiff();
                    }
                    var contentEditable = ReviewResource.reviewMode ? ($scope.uuid && ReviewResource.rejected.indexOf($scope.uuid) === -1 ? true : false) : $rootScope.fileEditable;
                    var classResult = contentEditable ? 'editableBox' : 'unEditableBox';
                    if ($scope.t === 'p') {
                        classResult += ' doubleH';
                    }
                    return classResult;
                };
                $scope.reviewLayout = function (type) {
                    if (type === 'regular') {
                        // display the new header, and difference header and content only when the item is not inside an added/deleted sections, and haven't accepted or rejected yet
                        return !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid) && !mainUtils.processedInReview('inside', $scope.uuid);
                    } else if (type === 'name') {
                        return mainUtils.processedInReview('name', $scope.uuid) && !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid) && !mainUtils.processedInReview('add', $scope.uuid) && !mainUtils.processedInReview('inside', $scope.uuid);
                    }
                };
                $scope.rejectedAction = function () {
                    return mainUtils.processedInReview('reject', $scope.uuid);
                };
                $scope.reviewContentEditable = function (type) {
                    if (type === 'regular') {
                        return !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid);
                    } else if (type === 'name') {
                        return !mainUtils.processedInReview('inside', $scope.uuid) && !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid) && !mainUtils.processedInReview('add', $scope.uuid);
                    }
                };
                $scope.initFirepad = function() {
                    $timeout(function() {
                        var firepadRef = firebase.database().ref($scope.path+'/'+$scope.key+'_firepad');
                        var codeMirror = CodeMirror(document.getElementById($scope.uuid), {lineWrapping: true, readOnly: !$scope.fe});
                        var firepad = Firepad.fromCodeMirror(firepadRef, codeMirror, 
                            {
                                richTextShortcuts: false, 
                                richTextToolbar: false, 
                                defaultText: '',
                                userId: $rootScope.me.name
                        });
                        firepad.on('ready', function() {
                            $scope.pContent = firepad.getText();
                            // firepad.setText('');
                        });
                        firepad.on('synced', function(isSynced) {
                            $scope.pContent = firepad.getText();
                            $scope.contentModified = true;
                            // $timeout(function() {                                                                
                            // }, 1000);                       
                        });
                    }, 200);
                }
            }
        };
    })
    ;
