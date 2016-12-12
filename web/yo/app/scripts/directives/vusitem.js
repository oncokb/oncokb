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
                addCommentInGene: '&addComment' // reference to the external function "addComment" in the gene controller
            },
            link: function postLink(scope) {
                scope.variant = scope.vus.get(scope.index);
                scope.dt = new Date(Number(scope.variant.time.get(scope.variant.time.length - 1).value.getText()));
                scope.dtBy = scope.variant.time.get(scope.variant.time.length - 1).by.name.getText();
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
                        var timeStamp = scope.dModel.create(OncoKB.TimeStampWithCurator);
                        timeStamp.value.setText(n.getTime().toString());
                        timeStamp.by.name.setText(user.name);
                        timeStamp.by.email.setText(user.email);
                        scope.variant.time.push(timeStamp);
                        scope.dtBy = user.name;
                    }
                });
            },
            controller: function($scope) {
                $scope.remove = function() {
                    var dlg = dialogs.confirm('Confirmation', 'Are you sure you want to delete this entry?');
                    dlg.result.then(function() {
                        $scope.vus.remove($scope.index);
                    }, function() {
                    });
                };
                $scope.open = function() {
                    $scope.status.opened = true;
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
                $scope.addComment = function(arg1, arg2, arg3) {
                    // pass parameters in value pair mapping format
                    $scope.addCommentInGene({
                        arg1: arg1,
                        arg2: arg2,
                        arg3: arg3
                    });
                };
            }
        };
    });
