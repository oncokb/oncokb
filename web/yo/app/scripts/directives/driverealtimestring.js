'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:driveRealtimeString
 * @description
 * # driveRealtimeString
 */
angular.module('oncokbApp')
    .directive('driveRealtimeString', function(gapi, $timeout, _, $rootScope, user, stringUtils, mainUtils, ReviewResource) {
        return {
            templateUrl: 'views/driveRealtimeString.html',
            restrict: 'AE',
            scope: {
                es: '=', // Evidence Status
                reviewObj: '=', // Review status and last reviewed content
                mutationMessages: '=',
                treatmentMessages: '=',
                uuid: '=', // evidence uuid
                object: '=', // target object
                objecttype: '=', // drive document attribute type; Default: string
                objectkey: '=', // if the attribute type is object, it has to have a key
                addon: '=', // Special design for mutation effect
                fe: '=', // file editable
                io: '=', // isOpen
                t: '=', // type
                o: '=', // options
                chosenid: '=', // Chosen selection ID
                checkboxes: '=', // checkbox list for input is checkbox
                checkboxid: '=',
                ph: '=', // Place holder
                mutation: '=',
                tumor: '=',
                therapyCategory: '=',
                treatment: '=',
                getMutationMessagesInGene: '&getMutationMessages',
                getTreatmentMessagesInGene: '&getTreatmentMessages'
            },
            replace: true,
            link: function postLink(scope) {
                scope.reviewMode = ReviewResource.reviewMode;
                scope.addOnTimeoutPromise = '';
                scope.stringTimeoutPromise = '';
                scope.changedBy = 'self';
                if (scope.objecttype === 'object' && scope.objectkey) {
                    if (scope.object.has(scope.objectkey)) {
                        scope.content.stringO = scope.object.get(scope.objectkey);
                    } else {
                        scope.content.stringO = '';
                    }
                } else {
                    scope.content.stringO = scope.object.text;
                }

                if (typeof scope.addon !== 'undefined') {
                    scope.content.addon = scope.addon.text;
                    scope.$watch('content.addon', function(n, o) {
                        $timeout.cancel(scope.addOnTimeoutPromise);  // does nothing, if timeout already done
                        scope.addOnTimeoutPromise = $timeout(function() {   // Set timeout
                            if (n !== o) {
                                scope.addon.text = n;
                                scope.valueChanged();
                            }
                        }, 1000);
                    });
                }
                scope.content.preStringO = scope.content.stringO;
                scope.$watch('object.text', function(n, o) {
                    if (n !== o) {
                        if (scope.reviewObj && _.isNull(scope.reviewObj.get('lastReviewed')) && (!ReviewResource.reviewMode || ReviewResource.rejected.indexOf(scope.uuid.getText()) === -1)) {
                            scope.reviewObj.set('lastReviewed', o);
                        }
                        if (scope.content.stringO !== scope.object.getText()) {
                            scope.content.stringO = scope.object.text;
                            scope.changedBy = 'others';
                        }
                    }
                });
                scope.$watch('content.stringO', function(n, o) {
                    $timeout.cancel(scope.stringTimeoutPromise);  // does nothing, if timeout already done
                    scope.stringTimeoutPromise = $timeout(function() {   // Set timeout
                        if (n !== o) {
                            if (scope.es && scope.es.get('obsolete') === 'true' || scope.changedBy === 'others') {
                                // If item is obsoleted, or the change is made by others, we only update the object.text value without tracking data for review mode
                                if (scope.objecttype === 'object' && scope.objectkey) {
                                    scope.object.set(scope.objectkey, n);
                                } else {
                                    scope.object.text = n;
                                }
                            } else if(!scope.uuid || !scope.uuid.getText() || !scope.reviewObj) {
                                // for the additional info items, since we don't need to track them in the review mode
                                scope.object.text = n;
                            } else if(!ReviewResource.reviewMode || ReviewResource.rejected.indexOf(scope.uuid.getText()) === -1) {
                                // exclude the case of reject action changing real time doc
                                scope.reviewObj.set('updatedBy', user.name);
                                scope.reviewObj.set('updateTime', new Date().getTime());
                                var uuid = scope.uuid.getText();
                                var tempMapping = $rootScope.geneMetaData.get(uuid);
                                if (!tempMapping) {
                                    tempMapping = $rootScope.metaModel.createMap();
                                }
                                if (ReviewResource.rollback.indexOf(scope.uuid.getText() !== -1)) {
                                    ReviewResource.rollback = _.without(ReviewResource.rollback, scope.uuid.getText());
                                }
                                if (scope.objecttype === 'object' && scope.objectkey) {
                                    // currently this condition is only designed for gene type
                                    if (!scope.reviewObj.get('lastReviewed')) {
                                        scope.reviewObj.set('lastReviewed', _.clone({
                                            TSG: scope.object.get('TSG'),
                                            OCG: scope.object.get('OCG')
                                        }));
                                    }
                                    scope.object.set(scope.objectkey, n);
                                    if (scope.reviewObj.get('lastReviewed').TSG === scope.object.get('TSG')
                                        && scope.reviewObj.get('lastReviewed').OCG === scope.object.get('OCG')) {
                                        tempMapping.set('review', false);
                                        ReviewResource.rollback.push(scope.uuid.getText());
                                        scope.reviewObj.delete('updatedBy');
                                    } else {
                                        tempMapping.set('review', true);
                                    }
                                } else {
                                    scope.object.text = n;
                                    if (scope.reviewObj.get('lastReviewed') !== n) {
                                        tempMapping.set('review', true);
                                    } else {
                                        tempMapping.set('review', false);
                                        ReviewResource.rollback.push(scope.uuid.getText());
                                        scope.reviewObj.delete('updatedBy');
                                    }
                                }
                                $rootScope.geneMetaData.set(uuid, tempMapping);
                            }
                            scope.valueChanged();
                            scope.changedBy = 'self';
                        }
                    }, 1000);
                });

                if (scope.t === 'treatment-select') {
                    if (!_.isUndefined(scope.es) && scope.es.get('propagation')) {
                        scope.content.propagation = scope.es.get('propagation');
                    }
                    scope.changePropagation(true);
                    scope.$watch('content.propagation', function(n, o) {
                        if (!_.isUndefined(scope.es)) {
                            if (_.isUndefined(scope.es.get('propagation')) ||
                                scope.es.get('propagation') !== n) {
                                scope.es.set('propagation', n);
                            }
                        }
                        if (o && n !== o && scope.es && scope.es.get('obsolete') !== 'true') {
                            scope.reviewObj.set('lastReviewedPropagation', o);
                            var uuid = scope.uuid.getText();
                            var tempMapping =  $rootScope.geneMetaData.get(uuid);
                            if (!tempMapping) {
                                tempMapping = $rootScope.metaModel.createMap();
                            }
                            tempMapping.set('review', true);
                             $rootScope.geneMetaData.set(uuid, tempMapping);
                            scope.reviewObj.set('updatedBy', user.name);
                            scope.reviewObj.set('updateTime', new Date().getTime());
                        }
                    });
                }
            },
            controller: function($scope) {
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
                    if ($scope.content.stringO === '1' || $scope.content.stringO === '2A') {
                        _propagationOpts = [
                            $scope.propagationOpts.no,
                            $scope.propagationOpts['2B'],
                            $scope.propagationOpts['4']
                        ];
                        if (!($scope.content.propagation && initialize)) {
                            $scope.content.propagation = '2B';
                        }
                    } else if ($scope.content.stringO === '3A') {
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
                function calculateDiff() {
                    if(($scope.t === 'p' || $scope.t === 'short') && $scope.reviewObj && $scope.reviewObj.has('lastReviewed')) {
                        var dmp = new diff_match_patch();
                        var newContent = stringUtils.getTextString($scope.content.stringO);
                        var oldContent = stringUtils.getTextString($scope.lastReviewed);
                        var diff = dmp.diff_main(oldContent, newContent);
                        dmp.diff_cleanupSemantic(diff);
                        $scope.diffHTML = dmp.diff_prettyHtml(diff);
                    }
                }
                $scope.valueChanged = function() {
                    if ($scope.t === 'treatment-select' && (!ReviewResource.reviewMode || !mainUtils.processedInReview('reject', $scope.treatment.level_uuid))) {
                        $scope.changePropagation();
                    }
                    if (!_.isUndefined($scope.es)) {
                        $scope.es.set('vetted', 'uv');
                    }
                    if(ReviewResource.reviewMode === true) {
                        calculateDiff();
                    }
                    if ($scope.t === 'MUTATION_NAME') {
                        $scope.getMutationMessages();
                    }
                    if ($scope.t === 'TREATMENT_NAME') {
                        $scope.getTreatmentMessages($scope.mutation, $scope.tumor, $scope.therapyCategory);
                    }
                };

                $scope.sCheckboxChange = function() {
                    $scope.content.addon = '';
                };

                $scope.uncheck = function() {
                    if ($scope.content.preStringO === $scope.content.stringO && $scope.content.preStringO !== '') {
                        $scope.content.stringO = '';
                    }
                    $scope.content.preStringO = $scope.content.stringO;
                };
                $scope.getInputClass = function() {
                    if (ReviewResource.reviewMode) {
                        $scope.lastReviewed = $scope.reviewObj.get('lastReviewed');
                        calculateDiff();
                    }
                    var contentEditable = ReviewResource.reviewMode ? ($scope.uuid && ReviewResource.rejected.indexOf($scope.uuid.getText()) === -1 ? true : false) : $scope.fe;
                    var classResult = contentEditable ? 'editableBox' : 'unEditableBox';
                    if ($scope.t === 'p') {
                        classResult += ' doubleH';
                    }
                    return classResult;
                };
                $scope.getMutationMessages = function() {
                    return $scope.getMutationMessagesInGene();
                };
                $scope.getTreatmentMessages = function(mutation, tumor, ti) {
                    return $scope.getTreatmentMessagesInGene({
                        mutation: mutation,
                        tumor: tumor,
                        ti: ti
                    });
                };
                $scope.getDuplicationMessage = function() {
                    var mutationName = $scope.mutation.name.getText().trim().toLowerCase();
                    if ($scope.t === 'MUTATION_NAME') {
                        if ($scope.mutationMessages[mutationName]) {
                            return $scope.mutationMessages[mutationName];
                        } else return '';
                    } else if ($scope.t === 'TREATMENT_NAME') {
                        var tumorName = mainUtils.getCancerTypesName($scope.tumor.cancerTypes).toLowerCase();
                        var tiName = $scope.therapyCategory.name.text.toLowerCase();
                        var treatmentName = $scope.object.text.toLowerCase();
                        if ($scope.treatmentMessages[mutationName] && $scope.treatmentMessages[mutationName][tumorName] && $scope.treatmentMessages[mutationName][tumorName][tiName] && $scope.treatmentMessages[mutationName][tumorName][tiName][treatmentName]) {
                            return $scope.treatmentMessages[mutationName][tumorName][tiName][treatmentName];
                        } else return '';
                    }

                };
                $scope.reviewLayout = function(type) {
                    if (type === 'regular') {
                        // display the new header, and difference header and content only when the item is not inside an added/deleted sections, and haven't accepted or rejected yet
                        return !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid) && !mainUtils.processedInReview('inside', $scope.uuid);
                    } else if (type === 'name') {
                        return mainUtils.processedInReview('name', $scope.uuid) && !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid) && !mainUtils.processedInReview('add', $scope.uuid) && !mainUtils.processedInReview('inside', $scope.uuid);
                    }
                };
                $scope.rejectedAction = function() {
                    return mainUtils.processedInReview('reject', $scope.uuid);
                };
                $scope.reviewContentEditable = function(type) {
                    if (type === 'regular') {
                        return !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid);
                    } else if (type === 'name') {
                        return !mainUtils.processedInReview('inside', $scope.uuid) && !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid) && !mainUtils.processedInReview('add', $scope.uuid);
                    }
                };
                $scope.inReviewMode = function () {
                    return ReviewResource.reviewMode;
                }
            }
        };
    })
;
