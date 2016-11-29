'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:loading
 * @description
 * # loading
 */
angular.module('oncokbApp')
    .directive('loading', function() {
        return {
            templateUrl: 'views/loading.html',
            restrict: 'E',
            require: 'ngModel',
            link: function postLink(scope, element, attrs, ngModel) {
                if (attrs.content) {
                    scope.content = attrs.content;
                } else {
                    scope.content = 'Please wait, we are loading information.';
                }

                if (attrs.hasOwnProperty('isAClass')) {
                    scope.isAClass = (attrs.isAClass === 'true');
                } else {
                    scope.isAClass = true;
                }

                ngModel.$render = function() {
                    scope.rendering = ngModel.$modelValue;
                };
            }
        };
    });
