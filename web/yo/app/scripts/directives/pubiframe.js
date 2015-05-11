'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:pubIframe
 * @description
 * # pubIframe
 */
angular.module('oncokbApp')
  .directive('pubIframe', function (FindRegex, S, sortPubsFilter) {
    return {
      templateUrl: 'views/pubIframe.html',
      restrict: 'E',
      require: 'ngModel',
      link: function(scope, element, attr, ngModel) {

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
          updatePubs(ngModel.$modelValue);
        };
      },
      controller: function($scope){
        $scope.pubs = [];
      }
    };
  });
