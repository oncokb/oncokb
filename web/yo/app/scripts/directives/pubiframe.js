'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:pubIframe
 * @description
 * # pubIframe
 */
angular.module('oncokbApp')
    .directive('pubIframe', function(FindRegex, S, $timeout, pubUtils) {
        return {
            templateUrl: 'views/pubIframe.html',
            restrict: 'E',
            require: 'ngModel',
            link: function(scope, element, attr, ngModel) {
                /* eslint new-cap: 0*/
                function updatePubs(modelValue) {
                    if (!modelValue) {
                        modelValue = '';
                    }
                    modelValue = S(modelValue).decodeHTMLEntities().s;
                    modelValue = S(modelValue).stripTags().s;
                    modelValue = S(modelValue).collapseWhitespace().s;
                    var pubs = FindRegex.result(modelValue);
                    if (pubs.length > 0) {
                        var pubsCache = pubUtils.get(pubs);
                        if (pubsCache.notValidatedPuds.length > 0) {
                            FindRegex.validation(pubsCache.notValidatedPuds).then(function(result) {
                                pubUtils.set(result);
                                scope.pubs = _.union(pubsCache.validatedPuds, result);
                            }, function (error) {
                                console.log('Error happened', error);
                            });
                        } else {
                            scope.pubs = pubsCache.validatedPuds;
                        }
                    } else {
                        scope.pubs = [];
                    }
                }

                scope.pubs = [];

                // update the color picker whenever the value on the scope changes
                ngModel.$render = function() {
                    $timeout(function() {
                        updatePubs(ngModel.$modelValue);
                    }, 500);
                };
            },
            controller: function($scope) {
                $scope.pubs = [];
            }
        };
    });
