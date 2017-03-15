'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:driveRealtimeString
 * @description
 * # driveRealtimeString
 */
angular.module('oncokbApp')
    .directive('driveRealtimeString', function(gapi, $timeout, _, $rootScope, user) {
        return {
            templateUrl: 'views/driveRealtimeString.html',
            restrict: 'AE',
            scope: {
                es: '=', // Evidence Status
                rs: '=', // Review status and last reviewed content
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
                ph: '=' // Place holder
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
                    scope.reviewMode = n;
                });
                scope.$watch('object.text', function(n, o) {
                    if (n !== o) {
                        if(scope.rs && _.isNull(scope.rs.get('lastReviewed')) && (!scope.reviewMode || scope.rs.get('action') !== 'rejected')) {
                            scope.rs.set('lastReviewed', o);
                        }
                        scope.content.stringO = scope.object.text;
                    }
                });
                scope.$watch('content.stringO', function(n, o) {
                    $timeout.cancel(scope.stringTimeoutPromise);  // does nothing, if timeout already done
                    scope.stringTimeoutPromise = $timeout(function() {   // Set timeout
                        if (n !== o) {
                            if(scope.es && scope.es.get('obsolete') === 'true') {
                                if (scope.objecttype === 'object' && scope.objectkey) {
                                    scope.object.set(scope.objectkey, n);
                                } else {
                                    scope.object.text = n;
                                }
                            } else if(scope.rs && (!scope.reviewMode || scope.rs.get('action') !== 'rejected')) {
                                // for the case of reject action changing real time doc
                                scope.rs.set('updatedBy', user.name);
                                scope.rs.set('updateTime', new Date().toLocaleString());
                                var uuid = scope.uuid.getText();
                                var tempMapping = $rootScope.reviewMeta.get(uuid);
                                if(!tempMapping) {
                                    tempMapping = $rootScope.metaModel.createMap();
                                }
                                if(scope.rs && scope.rs.get('rollback')) {
                                    scope.rs.set('rollback', null);
                                }
                                if (scope.objecttype === 'object' && scope.objectkey) {
                                    if(!scope.rs.get('lastReviewed')) {
                                        scope.rs.set('lastReviewed', _.clone({TSG: scope.object.get('TSG'), OCG: scope.object.get('OCG')}));
                                    }
                                    if(scope.rs.get('lastReviewed')[scope.objectkey] !== n) {
                                        tempMapping.set('review', true);
                                    }else{
                                        tempMapping.set('review', false);
                                        if (scope.reviewMode) {
                                            scope.rs.set('rollback', true);
                                        }
                                    }
                                    scope.object.set(scope.objectkey, n);
                                } else {
                                    scope.object.text = n;
                                    scope.content.stringO = n;
                                    if(scope.rs.get('lastReviewed') !== n) {
                                        tempMapping.set('review', true);
                                    }else{
                                        tempMapping.set('review', false);
                                        if (scope.reviewMode) {
                                            scope.rs.set('rollback', true);
                                        }
                                    }
                                }
                                $rootScope.reviewMeta.set(uuid, tempMapping);
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
                        if(o && n !== o && scope.es && scope.es.get('obsolete') !== 'true') {
                            scope.rs.set('lastReviewedPropagation', o);
                            var uuid = scope.uuid.getText();
                            var tempMapping = $rootScope.reviewMeta.get(uuid);
                            if(!tempMapping) {
                                tempMapping = $rootScope.metaModel.createMap();
                            }
                            tempMapping.set('review', true);
                            $rootScope.reviewMeta.set(uuid, tempMapping);
                            scope.rs.set('updatedBy', user.name);
                            scope.rs.set('updateTime', new Date().toLocaleString());
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

                $scope.valueChanged = function() {
                    if ($scope.t === 'treatment-select') {
                        $scope.changePropagation();
                    }
                    if (!_.isUndefined($scope.es)) {
                        $scope.es.set('vetted', 'uv');
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
                    if($scope.reviewMode) {
                        $scope.lastReviewed = $scope.rs.get('lastReviewed');
                    }
                    var contentEditable = $scope.reviewMode ? (!$scope.rs.get('action') ? true : false) : $scope.fe;
                    var classResult = contentEditable ? 'editableBox' : 'unEditableBox';
                    if($scope.t === 'p') classResult += ' doubleH';
                    return classResult;
                };
            }
        };
    })
;
