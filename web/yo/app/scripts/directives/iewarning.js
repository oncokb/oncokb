'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:ieWarning
 * @description
 * # ieWarning
 */
angular.module('oncokbApp')
    .directive('ieWarning', function() {
        return {
            templateUrl: 'views/ieWarning.html',
            restrict: 'E',
            controller: function($scope, browser) {
                var browserType = browser.detectBrowser();
                if (browserType === 'ie') {
                    $scope.isIE = true;
                    $scope.isUnknown = false;
                } else {
                    $scope.isIE = false;
                    if (browserType === 'unknown') {
                        $scope.isUnknown = true;
                    } else {
                        $scope.isUnknown = false;
                    }
                }
            }
        };
    });
