'use strict';

/**
 * @ngdoc directive
 * @name oncokb.directive:deleteItem
 * @description
 * # deleteItem
 */
angular.module('oncokb')
  .directive('deleteItem', function () {
    return {
      restrict: 'A',
      link: function postLink(scope, element, attrs) {
        console.log(scope);
        console.log(attrs);
      }
    };
  });
