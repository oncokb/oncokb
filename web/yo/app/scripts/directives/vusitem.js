'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:vusItem
 * @description
 * # vusItem
 */
angular.module('oncokbApp')
    .directive('vusItem', function(dialogs, OncoKB, user) {
        return {
            templateUrl: 'views/vusItem.html',
            restrict: 'E',
            scope: {
                vus: '=',
                index: '=',
                fileEditable: '=',
                dModel: '=', // drive realtime document model
                addCommentInGene: '&addComment', // reference to the external function "addComment" in the gene controller
                vusUpdateInGene: '&vusUpdate'
            },
            link: function postLink(scope) {
                scope.variant = scope.vus[scope.index];
                scope.dt = new Date(Number(scope.variant.time[scope.variant.time.length - 1].value));
                scope.dtBy = scope.variant.time[scope.variant.time.length - 1].by.name;
                scope.status = {
                    opened: false
                };
                scope.dateOptions = {
                    formatYear: 'yy',
                    startingDay: 1,
                    popupPosition: 'top'
                };

                scope.$watch('dt', function(n, o) {
                    if (n !== o) {
                        var timeStamp = {
                            by: {
                                email: user.email,
                                name: user.name
                            },
                            value: n.getTime().toString()
                        };
                        scope.variant.time.push(timeStamp);
                        scope.dtBy = user.name;
                        var tempMessage = user.name + ' tried to refresh ' + scope.variant.name + ' at ' + new Date().toLocaleString();
                        scope.vusUpdate(tempMessage);
                    }
                });
            },
            controller: function($scope) {
                $scope.remove = function() {
                    var dlg = dialogs.confirm('Confirmation', 'Are you sure you want to delete this entry?');
                    dlg.result.then(function() {
                        var tempMessage = user.name + ' tried to delete ' + $scope.vus[$scope.index].name + ' at ' + new Date().toLocaleString();
                        $scope.vus.splice($scope.index, 1);
                        $scope.vusUpdate(tempMessage);
                    }, function() {
                    });
                };
                $scope.open = function() {
                    $scope.status.opened = true;
                };
                $scope.update = function() {
                    $scope.dt = new Date();
                    $scope.dtBy = user.name;
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
                $scope.addComment = function(arg1, arg2, arg3) {
                    // pass parameters in value pair mapping format
                    $scope.addCommentInGene({
                        arg1: arg1,
                        arg2: arg2,
                        arg3: arg3
                    });
                };
                $scope.vusUpdate = function(message) {
                    // pass parameters in value pair mapping format
                    $scope.vusUpdateInGene({
                        message: message
                    });
                };
            }
        };
    });
