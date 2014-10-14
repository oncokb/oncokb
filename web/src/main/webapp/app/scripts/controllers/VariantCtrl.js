'use strict';

/**
 * @ngdoc function
 * @name webappApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the webappApp
 */
angular.module('webappApp')
  .controller('VariantCtrl', function ($scope, TumorType) {
    TumorType.getFromFile().success(function(data) {
    	$scope.tumorTypes = data;
    })
  });