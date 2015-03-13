'use strict';

/**
 * @ngdoc directive
 * @name oncokb.directive:comments
 * @description
 * # comments
 */
angular.module('oncokbApp')
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
        scope.status = {
          hasComment: false,
          allResolved: false
        };
        scope.commentsCopy = [];

        scope.$watch('comments.length', function(){
          if(scope.fileEditable || scope.comments.length > 0) {
            element.find('i').off('mouseenter');
            element.find('i').bind('mouseenter', function() {
              element.find('commentsBody').show();
            });
            element.find('i').off('mouseleave');
            element.find('i').bind('mouseleave', function() {
              element.find('commentsBody').hide();
            });
          }else {
            element.find('i').off('mouseenter');
            element.find('i').off('mouseleave');
          }

          var commentsCopy = [];
          for(var i = 0; i < scope.comments.length; i++) {
            commentsCopy.push({'resolved': scope.comments.get(i).resolved.getText()});
          }
          scope.commentsCopy = commentsCopy;
        });

        scope.$watch('commentsCopy', function(){
          var allResolved = true;
          if(scope.commentsCopy.length > 0) {
            scope.status.hasComment = true;
            for(var i = 0; i< scope.commentsCopy.length; i++) {
              if(scope.commentsCopy[i].resolved !== 'true') {
                allResolved = false;
                break;
              }
            }
            scope.status.allResolved = allResolved;
          }else{
            scope.status = {
              hasComment: false,
              allResolved: false
            };
          }
        }, true);

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
        $scope.resolve = function($index) {
          $scope.comments.get($index).resolved.setText('true');
          $scope.commentsCopy[$index].resolved = 'true';
        };
        $scope.delete = function(index) {
          $scope.comments.remove(index);
        };
      }
    };
  });
