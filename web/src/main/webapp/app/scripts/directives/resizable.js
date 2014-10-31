'use strict';

/**
 * @ngdoc directive
 * @name webappApp.directive:resizable
 * @description
 * # resizable
 */
angular.module('webappApp')
  .directive('resizable', function () {
    return {
      template: '<div></div>',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
        element.text('this is the resizable directive');
      }
    };
  });
