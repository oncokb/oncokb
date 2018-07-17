'use strict';

/**
 * @ngdoc directive
 * @name oncokb.directive:comments
 * @description
 * # comments
 */
angular.module('oncokbApp')
    .directive('commentsDict', function(DatabaseConnector, $timeout, $firebaseObject, FirebaseModel, ReviewResource, $rootScope) {
        return {
            templateUrl: 'views/comments.html',
            restrict: 'AE',
            scope: {
                key: '=',
                path: '='
            },
            replace: true,
            link: function postLink(scope, element, attrs) {
                scope.me = $rootScope.me;
                scope.mouseLeaveTimeout = '';
                scope.params = {};
                scope.status = {
                    rendering: true,
                    hasComment: false,
                    allResolved: false
                };
                $firebaseObject(firebase.database().ref(scope.path)).$bindTo(scope, "obj").then(function (success) {
                    scope.checkResolvedStatus();
                    scope.status.rendering = false;
                }, function (error) {
                    console.log('error');
                });
                element.find('i').off('mouseenter');
                element.find('i').bind('mouseenter', function() {
                    if (scope.mouseLeaveTimeout) {
                        $timeout.cancel(scope.mouseLeaveTimeout);
                        scope.mouseLeaveTimeout = '';
                    }
                    element.find('commentsBody').show();
                });

                element.find('i').off('mouseleave');
                element.find('i').bind('mouseleave', function() {
                    scope.mouseLeaveTimeout = $timeout(function() {
                        element.find('commentsBody').hide();
                    }, 500);
                });
                element.bind('keydown', function(event) {
                    if (event.which === 13) {
                        if (scope.params.newCommentContent) {
                            scope.$apply(function() {
                                scope.add();
                            });
                        }
                        event.preventDefault();
                    }
                });
            },
            controller: function($scope) {
                $scope.add = function() {
                    var user = $rootScope.me;
                    var comment = new FirebaseModel.Comment(user.name, user.email, $scope.params.newCommentContent);
                    if(!$scope.obj[$scope.key+'_comments']) {
                        $scope.obj[$scope.key+'_comments'] = [];
                    }
                    $scope.obj[$scope.key+'_comments'].push(comment);
                    $scope.params.newCommentContent = '';
                    $scope.checkResolvedStatus();
                };
                $scope.resolve = function(index) {
                    $scope.obj[$scope.key+'_comments'][index].resolved = 'true';
                    $scope.checkResolvedStatus();
                };
                $scope.delete = function(index) {
                    $scope.obj[$scope.key+'_comments'].splice(index, 1);
                    $scope.checkResolvedStatus();
                };
                $scope.checkResolvedStatus = function() {
                    $scope.status.hasComment = false;
                    $scope.status.allResolved = true;
                    if ($scope.obj[$scope.key+'_comments'] && $scope.obj[$scope.key+'_comments'].length > 0) {
                        $scope.status.hasComment = true;
                        _.each($scope.obj[$scope.key+'_comments'], function(comment) {
                            if (comment.resolved === 'false') {
                                $scope.status.allResolved = false;
                            }
                        });
                    }
                };
                $scope.fileEditable = $rootScope.fileEditable;
            }
        };
    });
