'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:eStatus
 * @description
 * # eStatus
 */
angular.module('oncokbApp')
  .directive('eStatus', function () {
    return {
      templateUrl: 'views/eStatus.html',
      restrict: 'AE',
      scope: {
        object: '=',
        id: '='
      },
      replace: true,
      link: function postLink(scope, element, attrs) {
        scope.checkboxes = [
          {
            display: 'Obsolete',
            value: 'o',
            icon: 'fa-battery-1',
            color: '#CC0000'
          },
          {
            display: 'Unvetted',
            value: 'uv',
            icon: 'fa-battery-0',
            color: '#808080'
          },
          {
            display: 'Knowledgebase Vetted',
            value: 'kv',
            icon: 'fa-battery-3',
            color: '#82E452'
          },
          {
            display: 'Expert Vetted',
            value: 'ev',
            icon: 'fa-battery-4',
            color: '#309119'
          }
        ];

        scope.$watch('status', function(n,o){
          if(n !== o) {
            console.log(n, o);
          }
        });

        if(!scope.object.has('status')){
          scope.object.set('status', 'uv');
          scope.status = scope.getStatusByValue('uv');
        }else{
          scope.status = scope.getStatusByValue(scope.object.get('status'));
        }

        element.find('status').bind('mouseenter', function() {
          element.find('statusBody').show();
        });
        element.find('status').bind('mouseleave', function() {
          element.find('statusBody').hide();
        });
      },
      controller: function($scope, $rootScope){
        $scope.getStatusByValue = function(status){
          for(var i = 0; i < $scope.checkboxes.length; i++) {
            if($scope.checkboxes[i].value === status) {
              return $scope.checkboxes[i];
            }
          }
          return {
            display: 'Unknown status',
            value: 'na'
          };
        };

        $scope.click = function(newStatus) {
          $scope.status = $scope.getStatusByValue(newStatus);
          $scope.object.set('status', $scope.status.value);
        };
      }
    };
  });
