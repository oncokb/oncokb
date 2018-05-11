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
                        // console.log(scope.pContent);
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
                        if (n !== o) {     
                            scope.data[scope.key] = OncoKB.utils.getString(scope.data[scope.key]);                  
                            scope.pContent = scope.data[scope.key];
                            $timeout.cancel(scope.timeoutRef);
                            if (scope.fe === true && !scope.data[scope.key+'_editing']) {
                                scope.data[scope.key+'_editing'] = $rootScope.me.name;
                            }
                            if (scope.data[scope.key+'_editing'] !== $rootScope.me.name) {
                                scope.initializeFE();
                            }
                            scope.timeoutRef = $timeout(function() {
                                if (scope.fe === true && scope.data[scope.key+'_editing'] === $rootScope.me.name) {
                                    scope.data[scope.key+'_editing'] = '';
                                }
                                scope.initializeFE();
                            }, 30*1000);
                            // we should use 30*60*1000
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
                    if (ReviewResource.reviewMode) {
                        $scope.lastReviewed = $scope.data[$scope.key + '_review'].lastReviewed;
                        calculateDiff();
                    }
                    var contentEditable = ReviewResource.reviewMode ? ($scope.uuid && ReviewResource.rejected.indexOf($scope.uuid) === -1 ? true : false) : $scope.fe;
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
