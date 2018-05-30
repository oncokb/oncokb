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
                        scope.pContent = scope.data[scope.key];
                        if (scope.t === 'treatment-select') {
                            scope.changePropagation(true);
                        }
                        scope.cleanUpEditing();
                        scope.initializeFE();
                    }, function (error) {
                        console.log('error');
                    });
                },
                post: function postLink(scope) {
                    scope.reviewMode = ReviewResource.reviewMode;
                    scope.timeoutRef = '';
                    scope.pContent = '';
                    // scope.contentModified = false;
                    scope.$watch('data[key]', function (n, o) {
                        if (scope.t === 'treatment-select' && scope.key === 'level') {
                            scope.$watch('data.propagation', function(newPro, oldPro) {
                                if (newPro !== oldPro) {
                                    scope.setReviewRelatedContent(n, o, true);
                                }
                            });
                        }
                        if (n !== o) {     
                            scope.data[scope.key] = OncoKB.utils.getString(scope.data[scope.key]);                  
                            scope.pContent = scope.data[scope.key];
                            if (scope.t === 'treatment-select' && scope.key === 'level') {
                                scope.changePropagation();
                            }
                            $timeout.cancel(scope.timeoutRef);
                            if (scope.fe === true && !scope.data[scope.key+'_editing']) {
                                scope.data[scope.key+'_editing'] = $rootScope.me.name;
                            }
                            if (scope.data[scope.key+'_editing'] !== $rootScope.me.name) {
                                scope.initializeFE();
                            }
                            if (scope.key !== 'short') {
                                scope.setReviewRelatedContent(n, o, false);
                            }
                            scope.timeoutRef = $timeout(function() {
                                if (scope.fe === true && scope.data[scope.key+'_editing'] === $rootScope.me.name) {
                                    delete scope.data[scope.key+'_editing'];
                                }
                                scope.initializeFE();
                            }, 30*1000);
                        }  
                    });
                    $rootScope.$watch('rejectedUUIDs["'+scope.uuid+'"]', function(n, o) {
                        if (n !== o && n === true) {
                            scope.data[scope.key] = scope.data[scope.key+'_review'].lastReviewed;
                            delete scope.data[scope.key+'_review'].lastReviewed;
                            delete $rootScope.geneMeta.review[scope.uuid];
                            delete $rootScope.rejectedUUIDs[scope.uuid];
                            ReviewResource.rejected.push(scope.uuid);
                        }
                    });
                    scope.setReviewRelatedContent = function(n, o, isPropogation) {
                        var key = scope.key;
                        var uuid = scope.uuid;
                        if (isPropogation === true) {
                            key = 'propagation';
                            uuid = scope.data.propagation_uuid;
                        }
                        // 1) we track the change in two conditions:
                        // 2) When editing happens not in review mode
                        // 3) When editing happends in review mode but not from admin's "Reject" action
                        if (!ReviewResource.reviewMode || ReviewResource.rejected.indexOf(uuid) === -1) {
                            if (_.isUndefined(scope.data[key + '_review'])) {
                                scope.data[key + '_review'] = {
                                    updatedBy: $rootScope.me.name,
                                    updateTime: new Date().getTime()
                                };
                            }
                            if (_.isUndefined(scope.data[key + '_review'].lastReviewed) && !_.isUndefined(o)) {
                                scope.data[key + '_review'].lastReviewed = o;
                                if (_.isUndefined($rootScope.geneMeta.review)) {
                                    $rootScope.geneMeta.review = {};
                                }
                                $rootScope.geneMeta.review[uuid] = true;                                       
                                ReviewResource.rollback = _.without(ReviewResource.rollback, uuid);
                            } else if (n === scope.data[key + '_review'].lastReviewed) {
                                delete scope.data[key + '_review'].lastReviewed;
                                delete $rootScope.geneMeta.review[uuid];
                                // if this kind of change happens inside review mode, we track current section in rollback status to remove the review panel since there is nothing to be approved
                                if (ReviewResource.reviewMode) {
                                    ReviewResource.rollback.push(uuid);
                                }
                            }
                        }
                    }
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
                $scope.initializeFE = function() {
                    if ($scope.data[$scope.key+'_editing']) {
                        if ($scope.data[$scope.key+'_editing'] === $rootScope.me.name) {
                            $scope.fe = true;
                            $scope.editingMessage = '';
                        } else {
                            $scope.fe = false;
                            $scope.editingMessage = 'Please wait. ' + $scope.data[$scope.key+'_editing'] + ' is editing this section...';
                        }                        
                    } else {
                        $scope.fe = $rootScope.fileEditable;
                    }
                };
                $scope.cleanUpEditing = function() {
                    if ($scope.data[$scope.key+'_editing'] && !$rootScope.collaborators[$scope.data[$scope.key+'_editing']]) {
                        $scope.data[$scope.key+'_editing'] = '';
                    }
                }
                $scope.changePropagation = function (initialize) {
                    if ($scope.data.propagation_review) {
                        delete $scope.data.propagation_review.lastReviewed;
                    }
                    delete $rootScope.geneMeta.review[$scope.data.propagation_uuid];
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
                    if (ReviewResource.reviewMode && $scope.data[$scope.key + '_review'] && $scope.data[$scope.key + '_review'].lastReviewed) {
                        $scope.lastReviewed = $scope.data[$scope.key + '_review'].lastReviewed;
                        calculateDiff();
                    }
                    var contentEditable = ReviewResource.reviewMode ? (!mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid)) : $scope.fe;
                    var classResult = '' ;
                    if (['MUTATION_NAME', 'TREATMENT_NAME'].indexOf($scope.t) === -1) {
                        classResult = contentEditable ? 'editableBox' : 'unEditableBox';
                    }
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
                $scope.updateThePath = function() {
                    console.log($scope.uuid, $scope.path);
                    var tempArr = $scope.path.split('/');
                    var lastEle = Number(tempArr[tempArr.length-1]);
                    if (_.isNumber(lastEle) && !_.isNaN(lastEle)) {
                        tempArr[tempArr.length-1] = $rootScope.indiciesByUUID[$scope.uuid];
                        $scope.path = tempArr.join('/');
                        console.log($scope.path);
                    }
                }                
            }
        };
    })
    ;
