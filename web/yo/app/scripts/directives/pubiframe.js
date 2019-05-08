'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:pubIframe
 * @description
 * # pubIframe
 */
angular.module('oncokbApp')
    .directive('pubIframe', function(FindRegex, S, $timeout, pubCache) {
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
                        var cachedPubs = pubCache.get(pubs);
                        if (cachedPubs.notValidatedPubs.length > 0) {
                            pubCache.validatePub(cachedPubs.notValidatedPubs).then(function(result) {
                                scope.pubs = _.concat(cachedPubs.validatedPubs, result);
                            }, function (error) {
                                console.log('Error happened', error);
                            });
                        } else {
                            scope.pubs = cachedPubs.validatedPubs;
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
