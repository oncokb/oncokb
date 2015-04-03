'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:reportTable
 * @description
 * # reportTable
 */
angular.module('oncokbApp')
  .directive('reportTable', function () {
    return {
      templateUrl: 'view/reporttable.html',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
        element.text('this is the reportTable directive');
      }
    };
  });
