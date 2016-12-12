'use strict';

/**
 * @ngdoc directive
 * @name oncokb.directive:reportViewRecursionCell
 * @description
 * # reportViewRecursionCell
 */
angular.module('oncokbApp')
    .directive('reportViewRecursionCell', function(RecursionHelper) {
        return {
            templateUrl: 'views/reportViewRecursionCellTemp.html',
            restrict: 'E',
            scope: {
                data: '=',
                depth: '='
            },
            controller: function($scope) {
                $scope.depth++;
            },
            compile: function(element) {
                return RecursionHelper.compile(element, function(scope) {
                    // Define your normal link function here.
                    // Alternative: instead of passing a function,
                    // you can also pass an object with
                    // a 'pre'- and 'post'-link function.
                    scope.isObject = function(obj) {
                        return angular.isObject(obj) && !angular.isArray(obj);
                    };
                    scope.isArray = function(obj) {
                        return angular.isArray(obj);
                    };
                    scope.isString = function(obj) {
                        return angular.isString(obj);
                    };
                    scope.bottomObject = function(obj) {
                        var flag = true;
                        if (obj && typeof obj === 'object') {
                            for (var key in obj) {
                                if (typeof obj[key] !== 'string') {
                                    flag = false;
                                    break;
                                }
                            }
                        } else {
                            flag = false;
                        }
                        return flag;
                    };
                    scope.keyProcess = function(key) {
                        if (key && key.indexOf('(Resistance)') !== -1) {
                            key = key.replace('(Resistance)', '<span class="resistance-title">(Resistance)</span>');
                        }
                        key = '<b>' + key + '</b>';
                        return key;
                    };
                    scope.multiObject = function(obj) {
                        var flag = false;
                        if (obj && typeof obj === 'object' && Object.keys(obj).length === 2 && Object.keys(obj).indexOf('value') !== -1 && Object.keys(obj).indexOf('Cancer type') !== -1) {
                            flag = true;
                        } else {
                            flag = false;
                        }
                        return flag;
                    };
                    scope.show = function(key, value) {
                        var disabledKey = ['description', 'trial', 'title', 'nccn_special', 'effect', 'recommendation category', 'value', 'no_evidence'];
                        var str = '';

                        if (disabledKey.indexOf(key.toLowerCase()) === -1) {
                            str += key + ': ' + value;
                        } else {
                            str += value;
                        }
                        str += '<br/>';
                        return str;
                    };
                });
            }
        };
    });
