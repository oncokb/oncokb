'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:driveRealtimeString
 * @description
 * # driveRealtimeString
 */
angular.module('oncokbApp')
    .directive('realtimeString', function ($timeout, _, $rootScope, mainUtils, ReviewResource, $firebaseObject, checkNameChange) {
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
                            if (n) {
                                scope.calculateDiff();
                            }
                        });
                    }, function (error) {
                        console.log('error');
                    });
                },
                post: function postLink(scope) {
                    scope.timeoutRef = '';
                    scope.pContent = '';
                    if (scope.t === 'treatment-select' && scope.key === 'level') {
                        scope.$watch('data.propagation', function(newPro, oldPro) {
                            if (newPro !== oldPro && !_.isUndefined(newPro) && (!$rootScope.reviewMode || ReviewResource.rejected.indexOf(scope.data.propagation_uuid) === -1)) {
                                scope.setReviewRelatedContent(newPro, oldPro, true);
                            }
                        });
                    }
                    scope.$watch('data[key]', function (n, o) {
                        if (n !== o && !_.isUndefined(n) && scope.fe) {
                            if (!scope.data || !scope.data[scope.key+'_editing'] || scope.data[scope.key+'_editing'] === $rootScope.me.name) {
                                if (_.keys($rootScope.collaborators).length > 1) { // Multiple users on the same gene
                                    if (scope.isChangedByOthers()) {
                                        return;
                                    }
                                }
                                var isRejected = mainUtils.processedInReview('reject', scope.uuid);
                                if (!$rootScope.reviewMode || !isRejected) {
                                    mainUtils.updateLastModified();
                                    if (scope.pasting === true) {
                                        scope.data[scope.key] = OncoKB.utils.getString(scope.data[scope.key]);
                                        scope.pasting = false;
                                    }
                                    scope.pContent = scope.data[scope.key];
                                    if (scope.t === 'treatment-select' && scope.key === 'level') {
                                        scope.changePropagation();
                                    }
                                    // 1) Do not trigger setReviewRelatedContent() when edit Additional Information (Optional).
                                    // 2) Do not trigger setReviewRelatedContent() when move mutations.
                                    if (scope.key !== 'short' && !(scope.key === 'name' && ($rootScope.movingSection || checkNameChange.get()))) {
                                        scope.setReviewRelatedContent(n, o, false);
                                    }
                                }
                                if (n !== o && (scope.key === 'level' || scope.key === 'summary' && scope.mutation && scope.tumor)) {
                                    $timeout(function() {
                                        scope.indicateMutationContent(scope.mutation);
                                        scope.indicateTumorContent(scope.tumor);
                                    }, 500);
                                }
                            }
                            if (scope.t === 'p' || scope.t === 'short') {
                                $timeout.cancel(scope.timeoutRef);
                                if (scope.fe === true && !scope.data[scope.key+'_editing']) {
                                    scope.data[scope.key+'_editing'] = $rootScope.me.name;
                                }
                                if (scope.data && (scope.data[scope.key+'_editing'] !== $rootScope.me.name)) {
                                    scope.initializeFE();
                                }
                                scope.timeoutRef = $timeout(function() {
                                    delete scope.data[scope.key+'_editing'];
                                    scope.initializeFE();
                                }, 10*1000);
                            }
                            // Check difference when user edits content in review mode.
                            if ($rootScope.reviewMode) {
                                scope.calculateDiff();
                            }
                        }
                    });
                    $rootScope.$watch('fileEditable', function(n, o) {
                        if (n !== o) {
                            scope.fe = n;
                            scope.editingMessage = '';
                        }
                    });
                    scope.setReviewRelatedContent = function(n, o, isPropagation) {
                        var key = scope.key;
                        var uuid = scope.uuid;
                        if (isPropagation === true) {
                            key = 'propagation';
                            uuid = scope.data.propagation_uuid;
                            if (_.isUndefined(o)) {
                                // Even if propagation old content is undefined, i.e. Level 0 -> Level 2A Propagation 2B.
                                // We still need to set its UUID in Meta/GeneName/review since we need its UUID for recording history old content.
                                mainUtils.setUUIDInReview(uuid);
                            }
                        }
                        // 1) we track the change in two conditions:
                        // 2) When editing happens not in review mode
                        // 3) When editing happends in review mode but not from admin's "Reject" action
                        if (_.isUndefined(scope.data[key + '_review'])) {
                            scope.data[key + '_review'] = {};
                        }
                        if (!_.isUndefined(scope.data[key + '_review'].added) && scope.data[key + '_review'].added) {
                            scope.data[key + '_review'].updatedBy = scope.data[key + '_review'].updatedBy;
                            scope.data[key + '_review'].updateTime = scope.data[key + '_review'].updateTime;
                        } else if (!_.isUndefined(scope.data[key + '_review'].removed) && scope.data[key + '_review'].removed) {
                            scope.data[key + '_review'].updatedBy = scope.data[key + '_review'].updatedBy;
                            scope.data[key + '_review'].updateTime = scope.data[key + '_review'].updateTime;
                        } else {
                            scope.data[key + '_review'].updatedBy = $rootScope.me.name;
                            scope.data[key + '_review'].updateTime = new Date().getTime();
                        }
                        if ((!$rootScope.reviewMeta[uuid] || _.isUndefined(scope.data[key + '_review'].lastReviewed)) && !_.isUndefined(o)) {
                            scope.data[key + '_review'].lastReviewed = o;
                            mainUtils.setUUIDInReview(uuid);
                            ReviewResource.rollback = _.without(ReviewResource.rollback, uuid);
                        } else if (n === scope.data[key + '_review'].lastReviewed) {
                            delete scope.data[key + '_review'].lastReviewed;
                            mainUtils.deleteUUID(uuid);
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
                    mainUtils.updateMovingFlag(false);
                };
                $scope.uuidtoName = function(key){
                    return mainUtils.drugUuidtoName(key, $scope.$parent.drugList);
                }
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
                };
                $scope.changePropagation = function (initial) {
                    if (!initial && $scope.data.propagation_review) {
                        delete $scope.data.propagation_review.lastReviewed;
                        mainUtils.deleteUUID($scope.data.propagation_uuid);
                    }
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
                            $scope.data.propagation = 'no';
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
                    if ($scope.t === 'p' && $scope.data[$scope.key+'_review'] && $scope.data[$scope.key+'_review'].lastReviewed) {
                        $scope.diffHTML = mainUtils.calculateDiff($scope.data[$scope.key + '_review'].lastReviewed, $scope.data[$scope.key]);
                    }
                };
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
                };
                $scope.reviewLayout = function (type) {
                    if (type === 'regular') {
                        // display the new header, and difference header and content only when the item is not inside an added/deleted sections, and haven't accepted or rejected yet
                        return !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid) && !mainUtils.processedInReview('inside', $scope.uuid) && !mainUtils.processedInReview('rollback', $scope.uuid);
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
                };
                $scope.getOldContentClass = function(content) {
                    var className = 'unEditableBox';
                    if (content && content.length > 80) {
                        className += ' longContent';
                    }
                    return className;
                };
                $scope.getOldContentDivClass = function(content) {
                    if (content && content.length > 80) {
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
                $scope.trimCSS = function() {
                    $scope.pasting = true;
                };
                $scope.isChangedByOthers = function() {
                    var changedByOthers = false;
                    firebase.database().ref($scope.path).on('value', function(doc) {
                        if (doc.val()[$scope.key] === $scope.data[$scope.key]) {
                            changedByOthers = true;
                            return;
                        }
                    }, function () {
                        console.log('Failed to load firebase object');
                    });
                    return changedByOthers;
                };
            }
        };
    });
