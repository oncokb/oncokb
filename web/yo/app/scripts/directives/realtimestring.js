'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:driveRealtimeString
 * @description
 * # driveRealtimeString
 */
angular.module('oncokbApp')
    .directive('realtimeString', function (gapi, $timeout, _, $rootScope, user, stringUtils, mainUtils, ReviewResource, $firebaseObject, users) {
        return {
            templateUrl: 'views/realtimeString.html',
            restrict: 'AE',
            scope: {
                t: '=',
                key: '=',
                path: '=',
                checkboxes: '=',
                o: '='
            },
            replace: true,
            link: {
                pre: function preLink(scope) {
                    users.isFileEditable().then(function(result) {
                        scope.fe = result;
                    }, function(error) {
                    });
                    $firebaseObject(firebase.database().ref(scope.path)).$bindTo(scope, "data").then(function (success) {
                        scope.uuid = scope.data[scope.key+'_uuid'];
                        if (scope.t === 'treatment-select') {
                            scope.changePropagation(true);
                        }
                    }, function (error) {
                        console.log('error');
                    });
                },
                post: function postLink(scope) {
                    scope.reviewMode = ReviewResource.reviewMode;

                    // scope.preStringO = scope.data[scope.key];
                    scope.$watch('data[key]', function (n, o) {
                        if (n !== o) {
                            scope.data[scope.key] = OncoKB.utils.getString(scope.data[scope.key]);
                            if (scope.key !== 'short') {
                                // we track the change in two conditions:
                                // 1) When editing happens not in review mode
                                // 2) When editing happends in review mode but not from admin's "Reject" action
                                // if (!ReviewResource.reviewMode || ReviewResource.rejected.indexOf(scope.uuid) === -1) {
                                //     if (_.isUndefined(scope.data[scope.key + '_review'])) {
                                //         scope.data[scope.key + '_review'] = {
                                //             updatedBy: user.name,
                                //             updateTime: new Date().getTime()
                                //         };
                                //     }
                                //     if (_.isUndefined(scope.data[scope.key + '_review'].lastReviewed)) {
                                //         scope.data[scope.key + '_review'].lastReviewed = o;
                                //         scope.data[scope.key + '_review'].updatedBy = user.name;
                                //         scope.data[scope.key + '_review'].updateTime = new Date().getTime();
                                //         $rootScope.metaFire[scope.uuid] = { review: true };
                                //         ReviewResource.rollback = _.without(ReviewResource.rollback, scope.uuid);
                                //     } else if (n === scope.data[scope.key]) {
                                //         delete scope.data[scope.key + '_review'].lastReviewed;
                                //         delete $rootScope.metaFire[scope.uuid];
                                //         // if this kind of change happens inside review mode, we track current section in rollback status to remove the review panel since there is nothing to be approved
                                //         if (ReviewResource.reviewMode) {
                                //             ReviewResource.rollback.push(scope.uuid);
                                //         }
                                //     }
                                // }
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
            }
        };
    })
    ;
