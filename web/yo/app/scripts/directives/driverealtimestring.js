'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:driveRealtimeString
 * @description
 * # driveRealtimeString
 */
angular.module('oncokbApp')
    .directive('driveRealtimeString', function(gapi, $timeout, _) {
        return {
            templateUrl: 'views/driveRealtimeString.html',
            restrict: 'AE',
            scope: {
                es: '=', // Evidence Status
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
                scope.addOnTimeoutPromise = '';
                scope.stringTimeoutPromise = '';
                scope.content = {};
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
                        scope.content.stringO = scope.object.text;
                    }
                });
                scope.$watch('content.stringO', function(n, o) {
                    $timeout.cancel(scope.stringTimeoutPromise);  // does nothing, if timeout already done
                    scope.stringTimeoutPromise = $timeout(function() {   // Set timeout
                        if (n !== o) {
                            if (scope.objecttype === 'object' && scope.objectkey) {
                                scope.object.set(scope.objectkey, n);
                            } else {
                                scope.object.text = n;
                            }
                            scope.valueChanged();
                        }
                    }, 1000);
                });
            },
            controller: function($scope) {
                $scope.valueChanged = function() {
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
                    // console.log(event, $scope.content.stringO);
                };
            }
        };
    });
