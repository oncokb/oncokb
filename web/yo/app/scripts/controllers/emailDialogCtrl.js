'use strict';
angular.module('oncokbApp')
  .controller('emailDialogCtrl', function($scope, $uibModalInstance) {
      $scope.user = {email: ''};

      $scope.cancel = function() {
          $uibModalInstance.dismiss('canceled');
      }; // end cancel

      $scope.done = function() {
          $uibModalInstance.close($scope.user.email);
      }; // end save
  })
  .run(function($templateCache) {
      $templateCache.put('views/emailDialog.html', '<div class="modal-header"><h4 class="modal-title">Please input your email address</h4></div><div class="modal-body"><ng-form name="emailDialog" novalidate role="form"><div class="form-group input-group-lg" ng-class="{true: \'has-error\'}[emailDialog.email.$dirty && emailDialog.email.$invalid]"><label class="control-label" for="email">Email:</label><input type="email" class="form-control" name="email" id="email" ng-model="user.email" ng-keyup="keyup()" required></div></ng-form></div><div class="modal-footer"><button type="button" class="btn btn-default" ng-click="cancel()">Cancel</button><button ng-disabled="emailDialog.email.$pristine || emailDialog.email.$invalid" class="btn btn-default" ng-click="done()">Done</button></div>');
  }); // end run / module
