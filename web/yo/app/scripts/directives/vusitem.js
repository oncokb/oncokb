'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:vusItem
 * @description
 * # vusItem
 */
angular.module('oncokbApp')
  .directive('vusItem', function (dialogs, OncoKB, user) {
    return {
      templateUrl: 'views/vusItem.html',
      restrict: 'E',
      scope: {
        vus: '=',
        index: '=',
        fileEditable: '=',
        dModel: '=' //drive realtime document model
      },
      link: function postLink(scope, element, attrs) {
        scope.variant = scope.vus.get(scope.index);
        scope.dt = new Date(Number(scope.variant.time.get(scope.variant.time.length - 1).value.getText()));
        scope.status = {
          opened: false
        };
        scope.dateOptions = {
          formatYear: 'yy',
          startingDay: 1
        };

        scope.$watch("dt", function (n, o) {
          if(n !== o) {
            var timeStamp = scope.dModel.create(OncoKB.TimeStamp);
            timeStamp.value.setText(n.getTime().toString());
            timeStamp.by.setText(user.email);
            scope.variant.time.push(timeStamp);
          }
        });
      },
      controller: function ($scope) {
        $scope.remove = function () {
          var dlg = dialogs.confirm('Confirmation', 'Are you sure you want to delete this entry?');
          dlg.result.then(function () {
              $scope.vus.remove($scope.index);
          }, function () {
          });
        };
      }
    };
  });
