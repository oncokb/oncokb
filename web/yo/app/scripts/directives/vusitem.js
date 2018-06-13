'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:vusItem
 * @description
 * # vusItem
 */
angular.module('oncokbApp')
    .directive('vusItem', function(dialogs, OncoKB, FirebaseModel, $rootScope) {
        return {
            templateUrl: 'views/vusItem.html',
            restrict: 'E',
            scope: {
                vus: '=',
                index: '=',
                gene: '=',
                vusUpdateInGene: '&vusUpdate'
            },
            link: function postLink(scope) {
                scope.variant = scope.vus.vus[scope.index];
                scope.dt = new Date(Number(scope.variant.time[scope.variant.time.length - 1].value));
                scope.dtBy = scope.variant.time[scope.variant.time.length - 1].by.name;
                scope.dateOptions = {
                    formatYear: 'yy',
                    startingDay: 1,
                    popupPosition: 'top'
                };
                scope.$watch('dt', function(n, o) {
                    if (n !== o) {
                        var user = $rootScope.me;
                        var timeStamp = new FirebaseModel.TimeStamp(user.name, user.email);
                        scope.vus.vus[scope.index].time.push(timeStamp);
                        scope.dtBy = user.name;
                        scope.vusUpdate();
                    }
                });
            },
            controller: function($scope) {
                $scope.remove = function() {
                    var dlg = dialogs.confirm('Confirmation', 'Are you sure you want to delete this entry?');
                    dlg.result.then(function() {
                        $scope.vus.vus.splice($scope.index, 1);
                        $scope.vusUpdate();
                    }, function() {
                    });
                };
                $scope.update = function() {
                    $scope.dt = new Date();
                };
                $scope.getClass = function(dt) {
                    if (dt instanceof Date) {
                        var _month = new Date().getMonth();
                        var _year = new Date().getYear();
                        var _monthDiff = (_year - dt.getYear()) * 12 + _month - dt.getMonth();
                        if (_monthDiff > 3) {
                            return 'danger';
                        } else if (_monthDiff > 1) {
                            return 'warning';
                        }
                    }

                    return '';
                };
                $scope.vusUpdate = function() {
                    // pass parameters in value pair mapping format
                    $scope.vusUpdateInGene();
                };
                $scope.fileEditable = $rootScope.fileEditable;
            }
        };
    });
