'use strict';

/**
 * @ngdoc directive
 * @name oncokb.directive:comments
 * @description
 * # comments
 */
angular.module('oncokbApp')
    .directive('commentsDict', function(DatabaseConnector, users, $timeout) {
        return {
            templateUrl: 'views/comments.html',
            restrict: 'AE',
            scope: {
                object: '=',
                addComment: '&addComment',
                comments: '=',
                fileEditable: '=',
                parentEvent: '='
            },
            replace: true,
            link: function postLink(scope, element, attrs) {
                scope.mouseLeaveTimeout = '';
                scope.key = attrs.key;
                scope.params = {};
                scope.status = {
                    hasComment: false,
                    allResolved: false
                };
                scope.userRole = users.getMe().role;

                // create a copy of comments. The original comments is a big
                // object, has too many methods, cannot use scope watch on the
                // whole object.
                scope.commentsCopy = [];

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

                scope.$watch('comments.length', function() {
                    if(scope.comments !== undefined) {
                        var commentsCopy = {
                            resolved: [],
                            content: []
                        };
                        for (var i = 0; i < scope.comments.length; i++) {
                            if (_.isArray(scope.comments)) {
                                commentsCopy.resolved.push(scope.comments[i].resolved);
                                commentsCopy.content.push(scope.comments[i].content);
                            } else {
                                commentsCopy.resolved.push(scope.comments.get(i).resolved.getText());
                                commentsCopy.content.push(scope.comments.get(i).content.getText());
                            }
                        }
                        scope.commentsCopy = commentsCopy;
                    }
                });

                scope.$watch('commentsCopy.resolved', function() {
                    if(!_.isEmpty(scope.commentsCopy)) {
                        var allResolved = true;
                        if (scope.commentsCopy.resolved.length > 0) {
                            scope.status.hasComment = true;
                            for (var i = 0; i < scope.commentsCopy.resolved.length; i++) {
                                if (scope.commentsCopy.resolved[i] !== 'true') {
                                    allResolved = false;
                                    break;
                                }
                            }
                            scope.status.allResolved = allResolved;
                        } else {
                            scope.status = {
                                hasComment: false,
                                allResolved: false
                            };
                        }
                    }
                }, true);

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
                $scope.blur = function(index) {
                    /* eslint new-cap: 0*/
                    if(_.isArray($scope.comments)) {
                        if ($scope.comments[index].content !== $scope.commentsCopy.content[index]) {
                            $scope.commentsCopy.content[index] = $scope.comments[index].content;
                        }
                    } else {
                        if ($scope.comments.get(index).content.getText() !== $scope.commentsCopy.content[index]) {
                            $scope.commentsCopy.content[index] = $scope.comments.get(index).content.getText();
                        }
                    }
                };

                $scope.add = function() {
                    console.log($scope);
                    $scope.addComment({
                        arg1: $scope.object,
                        arg2: $scope.key,
                        arg3: $scope.params.newCommentContent
                    });
                    $scope.params.newCommentContent = '';
                };
                $scope.resolve = function(index) {
                    if(_.isArray($scope.comments)) {
                        $scope.comments[index].resolved = 'true';
                    } else {
                        $scope.comments.get(index).resolved.setText('true');
                    }
                    $scope.commentsCopy.resolved[index] = 'true';
                };
                $scope.delete = function(index) {
                    $scope.comments.remove(index);
                };
                $scope.isArray = function(object) {
                    return _.isArray(object);
                }
            }
        };
    });
