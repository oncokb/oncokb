'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:toolMultiVariants
 * @description
 * # toolMultiVariants
 */
angular.module('oncokbApp')
  .directive('toolMultiVariants', function () {
    return {
      template: '<div></div>',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
        element.text('this is the toolMultiVariants directive');
      }
    };
  });
