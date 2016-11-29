'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:eStatus
 * @description
 * # eStatus
 */
angular.module('oncokbApp')
    .directive('eStatus', function(gapi, $timeout) {
        return {
            templateUrl: 'views/eStatus.html',
            restrict: 'AE',
            scope: {
                object: '=',
                id: '='
            },
            replace: true,
            link: function postLink(scope, element) {
                scope.mouseLeaveTimeout = '';
                scope.checkboxes = [{
                    display: 'Unvetted',
                    value: 'uv',
                    icon: 'fa-battery-0',
                    color: '#808080'
                }, {
                    display: 'Knowledgebase Vetted',
                    value: 'kv',
                    icon: 'fa-battery-2',
                    color: '#82E452'
                }, {
                    display: 'Expert Vetted',
                    value: 'ev',
                    icon: 'fa-battery-4',
                    color: '#309119'
                }
                ];

                if (scope.object.has('vetted')) {
                    scope.vetted = scope.getStatusByValue(scope.object.get('vetted'));
                } else {
                    scope.object.set('vetted', 'uv');
                    scope.vetted = scope.getStatusByValue('uv');
                }

                if (scope.object.has('obsolete')) {
                    scope.obsolete = scope.object.get('obsolete');
                } else {
                    scope.object.set('obsolete', 'false');
                    scope.obsolete = 'false';
                }

                scope.object.addEventListener(
                    gapi.drive.realtime.EventType.VALUE_CHANGED, function() {
                        var vetted = scope.getStatusByValue(scope.object.get('vetted'));
                        if (vetted !== scope.vetted) {
                            scope.vetted = vetted;
                        }
                    });

                element.find('status').bind('mouseenter', function() {
                    if (scope.mouseLeaveTimeout) {
                        $timeout.cancel(scope.mouseLeaveTimeout);
                        scope.mouseLeaveTimeout = '';
                    }
                    element.find('statusBody').show();
                });
                element.find('status').bind('mouseleave', function() {
                    scope.mouseLeaveTimeout = $timeout(function() {
                        element.find('statusBody').hide();
                    }, 500);
                });
            },
            controller: function($scope) {
                $scope.getStatusByValue = function(status) {
                    for (var i = 0; i < $scope.checkboxes.length; i++) {
                        if ($scope.checkboxes[i].value === status) {
                            return $scope.checkboxes[i];
                        }
                    }
                    return {
                        display: 'Unknown status',
                        value: 'na'
                    };
                };

                $scope.click = function(newStatus) {
                    if (newStatus === 'o') {
                        if ($scope.obsolete === 'false') {
                            $scope.obsolete = 'true';
                        } else {
                            $scope.obsolete = 'false';
                        }
                        $scope.object.set('obsolete', $scope.obsolete);
                    } else {
                        $scope.vetted = $scope.getStatusByValue(newStatus);
                        $scope.object.set('vetted', $scope.vetted.value);
                    }
                };
            }
        };
    });
