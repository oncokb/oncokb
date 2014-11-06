'use strict';

/**
 * @ngdoc directive
 * @name webappApp.directive:regularView
 * @description
 * # regularView
 */
angular.module('webappApp')
  .directive('regularView', function () {
    return {
      templateUrl: 'views/regularView.html',
      restrict: 'E',
      scope: {
      	annotation: '=',
      	summaryTableTitles: '=',
      	summaryTableTitlesContent: '=',
      	specialAttr: '=',
    		displayProcess: '=',
    		findRegex: '=',
    		setCollapsed: '=',
    		getCollapseIcon: '=',
    		isCollapsed: '=',
    		generateTrial: '=',
    		fdaApproved: '=',
    		generateNccn: '=',
    		displayParts: '='
      }
    };
  });
