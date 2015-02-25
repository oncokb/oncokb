'use strict';

/**
 * @ngdoc directive
 * @name oncokb.directive:comments
 * @description
 * # comments
 */
angular.module('oncokb')
  .directive('commentsDict', function () {
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
        scope.key = attrs.key;
        scope.params = {};

        scope.$watch('comments.length', function(){
          if(scope.fileEditable || scope.comments.length > 0) {
            element.find('i').off('mouseenter');
            element.find('i').bind('mouseenter', function(event) {
              element.find('commentsBody').show();
            });
            element.find('i').off('mouseleave');
            element.find('i').bind('mouseleave', function(event) {
              element.find('commentsBody').hide();
            });
          }else {
            element.find('i').off('mouseenter');
            element.find('i').off('mouseleave');
          }
        });

        element.bind('keydown', function (event) {
          if(event.which === 13) {
            if(scope.params.newCommentContent) {
              scope.$apply(function(){
                scope.add();
              });
            }
            event.preventDefault();
          }
        });
      },
      controller: function($scope){
        $scope.add = function() {
          $scope.addComment({arg1: $scope.object, arg2: $scope.key, arg3: $scope.params.newCommentContent});
          $scope.params.newCommentContent = '';
        };
        $scope.resolve = function(comment) {
          comment.resolved.setText('true');
        };
        $scope.delete = function(index) {
          $scope.comments.remove(index);
        };
      }
    };
  });
