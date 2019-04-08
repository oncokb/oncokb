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
                        pubCache.get(pubs).then(function(result){
                            scope.pubs = result;
                        });
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
