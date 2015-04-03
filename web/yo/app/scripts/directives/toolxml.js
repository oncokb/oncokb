'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:toolXML
 * @description
 * # toolXML
 */
angular.module('oncokbApp')
  .directive('toolXML', function () {
    return {
      template: '<div></div>',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
        element.text('this is the toolXML directive');
      }
    };
  });
