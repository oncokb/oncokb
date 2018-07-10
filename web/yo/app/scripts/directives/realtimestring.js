'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:driveRealtimeString
 * @description
 * # driveRealtimeString
 */
angular.module('oncokbApp')
    .directive('realtimeString', function ($timeout, _, $rootScope, mainUtils, ReviewResource, $firebaseObject) {
        return {
            templateUrl: 'views/realtimeString.html',
            restrict: 'AE',
            scope: {
                t: '=',
                key: '=',
                path: '=',
                checkboxes: '=',
                o: '=',
                uuid: '=',
                mutation: '=',
                tumor: '=',
                indicateMutationContentInGene: '&indicateMutationContent',
                indicateTumorContentInGene: '&indicateTumorContent'
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
                        $rootScope.$watch('reviewMode', function(n, o) {
                            scope.calculateDiff();
                        });
                    }, function (error) {
                        console.log('error');
                    });
                },
                post: function postLink(scope) {
                    scope.timeoutRef = '';
                    scope.pContent = '';
                    scope.$watch('data[key]', function (n, o) {
                        if (!(scope.data && scope.data[scope.key+'_editing'])) {
                            if (scope.t === 'treatment-select' && scope.key === 'level') {
                                scope.$watch('data.propagation', function(newPro, oldPro) {
                                    if (newPro !== oldPro && (!$rootScope.reviewMode || ReviewResource.rejected.indexOf(scope.data.propagation_uuid) === -1)) {
                                        scope.setReviewRelatedContent(newPro, oldPro, true);
                                    }
                                });
                            }
                            if (n !== o && (!$rootScope.reviewMode || ReviewResource.rejected.indexOf(scope.uuid) === -1)) {     
                                $rootScope.geneMeta.lastModifiedAt = new Date().getTime();
                                $rootScope.geneMeta.lastModifiedBy = $rootScope.me.name;
                                scope.data[scope.key] = OncoKB.utils.getString(scope.data[scope.key]);                  
                                scope.pContent = scope.data[scope.key];
                                if (scope.t === 'treatment-select' && scope.key === 'level') {
                                    scope.changePropagation();
                                }
                                if (scope.key !== 'short' && (scope.key !== 'name' || !$rootScope.moving)) {
                                    scope.setReviewRelatedContent(n, o, false);
                                }
                                if (scope.t === 'p' || scope.t === 'short') {
                                    $timeout.cancel(scope.timeoutRef);
                                    if (scope.fe === true && !scope.data[scope.key+'_editing']) {
                                        scope.data[scope.key+'_editing'] = $rootScope.me.name;
                                    }
                                    if (scope.data[scope.key+'_editing'] !== $rootScope.me.name) {
                                        scope.initializeFE();
                                    }
                                    scope.timeoutRef = $timeout(function() {
                                        delete scope.data[scope.key+'_editing'];
                                        scope.initializeFE();
                                    }, 10*1000);
                                }   
                            }  
                            if (n !== o && (scope.key === 'level' || scope.key === 'summary' && scope.mutation && scope.tumor)) {
                                $timeout(function() {
                                    scope.indicateMutationContent(scope.mutation);
                                    scope.indicateTumorContent(scope.tumor);
                                }, 500);                            
                            }
                        }                       
                    });
                    $rootScope.$watch('fileEditable', function(n, o) {
                        if (n !== o) {
                            scope.fe = n;
                            scope.editingMessage = '';
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
                        if (_.isUndefined(scope.data[key + '_review'])) {
                            scope.data[key + '_review'] = {};
                        }
                        scope.data[key + '_review'].updatedBy = $rootScope.me.name;
                        scope.data[key + '_review'].updateTime = new Date().getTime();
                        if ((!$rootScope.geneMeta.review[uuid] || _.isUndefined(scope.data[key + '_review'].lastReviewed)) && !_.isUndefined(o)) {
                            scope.data[key + '_review'].lastReviewed = o;
                            $rootScope.geneMeta.review[uuid] = true;                                       
                            ReviewResource.rollback = _.without(ReviewResource.rollback, uuid);
                        } else if (n === scope.data[key + '_review'].lastReviewed) {
                            delete scope.data[key + '_review'].lastReviewed;
                            delete $rootScope.geneMeta.review[uuid];
                            // if this kind of change happens inside review mode, we track current section in rollback status to remove the review panel since there is nothing to be approved
                            if ($rootScope.reviewMode) {
                                ReviewResource.rollback.push(uuid);
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
                $scope.setTrackSignal = function() {
                    $rootScope.moving = false;
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
                    if ($scope.data[$scope.key+'_editing']) {
                        $scope.data[$scope.key+'_editing'] = '';
                    }
                }
                $scope.changePropagation = function (initial) {
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
                        if (!initial && !$scope.data.propagation) {
                            $scope.data.propagation = '2B';
                        }
                    } else if ($scope.data[$scope.key] === '3A') {
                        _propagationOpts = [
                            $scope.propagationOpts.no,
                            $scope.propagationOpts['3B'],
                            $scope.propagationOpts['4']
                        ];
                        if (!initial && !$scope.data.propagation) {
                            $scope.data.propagation = '3B';
                        }
                    } else if ($scope.data[$scope.key] === '4') {
                        _propagationOpts = [
                            $scope.propagationOpts.no,
                            $scope.propagationOpts['4']
                        ];
                        if (!initial && !$scope.data.propagation) {
                            $scope.data.propagation = '4';
                        }
                    } else {
                        $scope.data.propagation = null;
                    }
                    $scope.content.propagationOpts = _propagationOpts;
                };
                $scope.inReviewMode = function () {
                    return $rootScope.reviewMode;
                };
                $scope.calculateDiff = function() {
                    if ($rootScope.reviewMode && $scope.t === 'p') {
                        var dmp = new diff_match_patch();
                        var newContent = mainUtils.getTextString($scope.data[$scope.key]);
                        var oldContent = '';
                        if ($scope.data[$scope.key+'_review'] && $scope.data[$scope.key+'_review'].lastReviewed) {
                            oldContent = mainUtils.getTextString($scope.data[$scope.key+'_review'].lastReviewed);
                        }
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
                    var contentEditable = $rootScope.reviewMode ? (!mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid)) : $scope.fe;
                    var classResult = '' ;
                    if (['MUTATION_NAME', 'TREATMENT_NAME'].indexOf($scope.t) === -1) {
                        classResult = contentEditable ? 'editableBox' : 'unEditableBox';
                    }
                    if ($scope.t === 'p') {
                        classResult += ' doubleH';
                    }
                    return classResult;
                };
                $scope.getInputStyle = function(type) {
                    if ($scope.key === 'ocg' && $scope.reviewLayout('regular')) {
                        if (type === 'new') {
                            return {'margin-top': "-85px"};
                        } else if (type === 'old') {
                            return {'margin-top': "35px"};
                        }
                    }
                };
                $scope.getOldcontentChecked = function(checkbox) {
                    if ($scope.key === 'tsg' || $scope.key === 'ocg') {
                        if (_.isUndefined($scope.data[$scope.key+'_review']) || _.isUndefined($scope.data[$scope.key+'_review'].lastReviewed)) {
                            return $scope.data[$scope.key] === checkbox;
                        }
                    } 
                    return $scope.data && $scope.data[$scope.key+'_review'] && $scope.data[$scope.key+'_review'].lastReviewed === checkbox;
                }
                $scope.reviewLayout = function (type) {
                    if (type === 'regular') {
                        // display the new header, and difference header and content only when the item is not inside an added/deleted sections, and haven't accepted or rejected yet
                        return !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid) && !mainUtils.processedInReview('inside', $scope.uuid);
                    } else if (type === 'name') {
                        return mainUtils.processedInReview('name', $scope.uuid) && !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid) && !mainUtils.processedInReview('add', $scope.uuid) && !mainUtils.processedInReview('inside', $scope.uuid);
                    } else if (type === 'inside') {
                        return mainUtils.processedInReview('inside', $scope.uuid);
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
                $scope.getOldContentClass = function(content) {
                    var className = 'unEditableBox';
                    if (content && content.length > 80) {
                        className += ' longContent';
                    }
                    return className;
                }
                $scope.getOldContentDivClass = function(content) {
                    if (content.length > 80) {
                        return 'longContentDivMargin';
                    }
                };
                $scope.indicateMutationContent = function(mutation) {
                    $scope.indicateMutationContentInGene({
                        mutation: mutation
                    });
                };
                $scope.indicateTumorContent = function(tumor) {
                    $scope.indicateTumorContentInGene({
                        tumor: tumor
                    });
                };
            }
        };
    })
    ;
