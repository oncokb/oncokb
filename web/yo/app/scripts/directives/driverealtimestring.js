'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:driveRealtimeString
 * @description
 * # driveRealtimeString
 */
angular.module('oncokbApp')
    .directive('driveRealtimeString', function(gapi, $timeout, _, $rootScope, user, stringUtils) {
        return {
            templateUrl: 'views/driveRealtimeString.html',
            restrict: 'AE',
            scope: {
                es: '=', // Evidence Status
                reviewObj: '=', // Review status and last reviewed content
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
                therapyCategory: '=',
                treatment: '=',
                validateMutationInGene: '&validateMutation',
                validateTreatmentInGene: '&validateTreatment'
            },
            replace: true,
            link: function postLink(scope) {
                scope.reviewMode = $rootScope.reviewMode;
                scope.addOnTimeoutPromise = '';
                scope.stringTimeoutPromise = '';

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
                $rootScope.$watch('reviewMode', function(n, o) {
                    if(n !== o) {
                        scope.reviewMode = n;
                    }
                });
                scope.$watch('object.text', function(n, o) {
                    if (n !== o) {
                        if (scope.reviewObj && _.isNull(scope.reviewObj.get('lastReviewed')) && (!scope.reviewMode || scope.reviewObj.get('review') !== false)) {
                            scope.reviewObj.set('lastReviewed', o);
                        }
                        if (scope.content.stringO !== scope.object.getText()) {
                            scope.content.stringO = scope.object.text;
                        }
                    }
                });
                scope.$watch('content.stringO', function(n, o) {
                    $timeout.cancel(scope.stringTimeoutPromise);  // does nothing, if timeout already done
                    scope.stringTimeoutPromise = $timeout(function() {   // Set timeout
                        if (n !== o) {
                            if (scope.t === 'MUTATION_NAME') {
                                scope.error = scope.validateMutation(n);
                            }
                            if (scope.t === 'TREATMENT_NAME') {
                                scope.error = scope.validateTreatment(n, false, false, scope.therapyCategory);
                            }
                            if (scope.es && scope.es.get('obsolete') === 'true') {
                                if (scope.objecttype === 'object' && scope.objectkey) {
                                    scope.object.set(scope.objectkey, n);
                                } else {
                                    scope.object.text = n;
                                }
                            } else if(!scope.uuid || !scope.uuid.getText() || !scope.reviewObj) {
                                // for the additional info items, since we don't need to track them in the review mode
                                scope.object.text = n;
                            } else if(!scope.reviewMode || scope.reviewObj.get('review') !== false) {
                                // exclude the case of reject action changing real time doc
                                scope.reviewObj.set('updatedBy', user.name);
                                scope.reviewObj.set('updateTime', new Date().getTime());
                                var uuid = scope.uuid.getText();
                                var tempMapping = $rootScope.geneMetaData.get(uuid);
                                if (!tempMapping) {
                                    tempMapping = $rootScope.metaModel.createMap();
                                }
                                if (scope.reviewObj && scope.reviewObj.get('rollback')) {
                                    scope.reviewObj.set('rollback', null);
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
                                        if (scope.reviewMode) {
                                            scope.reviewObj.set('rollback', true);
                                        }
                                        scope.reviewObj.delete('lastReviewed');
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
                                        if (scope.reviewMode) {
                                            scope.reviewObj.set('rollback', true);
                                        }
                                        scope.reviewObj.delete('lastReviewed');
                                        scope.reviewObj.delete('updatedBy');
                                    }
                                }
                                $rootScope.geneMetaData.set(uuid, tempMapping);
                            }
                            scope.valueChanged();
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
                $scope.error = '';
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
                    if ($scope.t === 'treatment-select' && (!$scope.reviewMode || $scope.reviewObj.get('review') !== false)) {
                        $scope.changePropagation();
                    }
                    if (!_.isUndefined($scope.es)) {
                        $scope.es.set('vetted', 'uv');
                    }
                    if($scope.reviewMode === true) {
                        calculateDiff();
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
                    if ($scope.reviewMode) {
                        $scope.lastReviewed = $scope.reviewObj.get('lastReviewed');
                        calculateDiff();
                    }
                    var contentEditable = $scope.reviewMode ? ($scope.reviewObj.get('review') !== false ? true : false) : $scope.fe;
                    var classResult = contentEditable ? 'editableBox' : 'unEditableBox';
                    if ($scope.t === 'p') {
                        classResult += ' doubleH';
                    }
                    return classResult;
                };
                $scope.validateMutation = function(newMutationName, alert, firstEnter, mutation) {
                    return $scope.validateMutationInGene({
                        newMutationName: newMutationName,
                        alert: alert,
                        firstEnter: firstEnter,
                        mutation: mutation
                    });
                };
                $scope.validateTreatment = function(newTreatmentName, alert, firstEnter, therapyCategory, treatment) {
                    return $scope.validateTreatmentInGene({
                        newTreatmentName: newTreatmentName,
                        alert: alert,
                        firstEnter: firstEnter,
                        therapyCategory: therapyCategory,
                        treatment: treatment
                    });
                };
                function duplicatedNameCheck() {
                    if ($scope.t === 'MUTATION_NAME') {
                        $scope.error = $scope.validateMutation($scope.object.text, false, true, $scope.mutation);
                    }
                    if ($scope.t === 'TREATMENT_NAME') {
                        $scope.error = $scope.validateTreatment($scope.object.text, false, true, $scope.therapyCategory, $scope.treatment);
                    }
                }
                duplicatedNameCheck();
            }
        };
    })
;
