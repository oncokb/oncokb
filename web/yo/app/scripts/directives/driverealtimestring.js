'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:driveRealtimeString
 * @description
 * # driveRealtimeString
 */
angular.module('oncokbApp')
  .directive('driveRealtimeString', function (gapi, $timeout) {
    return {
      templateUrl: 'views/driveRealtimeString.html',
      restrict: 'AE',
      scope: {
        es: '=', //Evidence Status
        object: '=', //target object
        addon: '=', //Special design for mutation effect
        fe: '=', //file editable
        io: '=', //isOpen
        t: '=', //type
        o: '=', //options
        chosenid: '=', //Chosen selection ID
        checkboxes: '=', //checkbox list for input is checkbox
        checkboxid: '=',
        ph: '=' //Place holder
      },
      replace: true,
      link: function postLink(scope, element, attrs) {
        scope.addOnTimeoutPromise = '';
        scope.stringTimeoutPromise = '';
        scope.content = {};
        scope.content.stringO = scope.object.text;

        if(typeof scope.addon !== 'undefined') {
          scope.content.addon = scope.addon.text;
          scope.$watch("content.addon", function (n, o) {
            $timeout.cancel(scope.addOnTimeoutPromise);  //does nothing, if timeout already done
            scope.addOnTimeoutPromise = $timeout(function(){   //Set timeout
              if( n !== o) {
                scope.addon.text = n;
                scope.valueChanged();
              }
            }, 1000);
          });
        }

        scope.$watch("content.stringO", function (n, o) {
          $timeout.cancel(scope.stringTimeoutPromise);  //does nothing, if timeout already done
          scope.stringTimeoutPromise = $timeout(function(){   //Set timeout
            if( n !== o) {
              scope.object.text = n;
              scope.valueChanged();
            }
          }, 1000);
        });
      },
      controller: function($scope, $rootScope){
        $scope.valueChanged = function(newVal) {
          $scope.es.set('vetted', 'uv');
        };

        $scope.sCheckboxChange = function() {
          $scope.content.addon = '';
        };
      }
    };
  });
