'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:pubIframe
 * @description
 * # pubIframe
 */
angular.module('oncokbApp')
    .directive('pubIframe', function(FindRegex, S, $timeout) {
        return {
            templateUrl: 'views/pubIframe.html',
            restrict: 'E',
            require: 'ngModel',
            link: function(scope, element, attr, ngModel) {
                /* eslint new-cap: 0*/
                function updatePubs(modelValue) {
                    modelValue = S(modelValue).decodeHTMLEntities().s;
                    modelValue = S(modelValue).stripTags().s;
                    modelValue = S(modelValue).collapseWhitespace().s;
                    var pubs = FindRegex.result(modelValue);
                    scope.pubs = pubs;
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
