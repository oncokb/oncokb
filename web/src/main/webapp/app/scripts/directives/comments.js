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
        comments: '='
      },
      replace: true,
      link: function postLink(scope, element, attrs) {
        // console.debug(scope);
        scope.key = attrs.key;
        // console.log(scope.object);
        // console.log(scope.object[scope.key]);
        // scope.object[scope.key + '_comments'].asArray().forEach(function(e){ console.log(e);});
        element.find('i').bind('mouseenter', function() {
          element.find('commentsBody').show();
        });
        element.parent().bind('mouseleave', function() {
          element.find('commentsBody').hide();
        });
      },
      controller: function($scope){
        $scope.add = function() {
          $scope.addComment({arg1: $scope.object, arg2: $scope.key, arg3: $scope.newCommentContent});
          $scope.newCommentContent = '';
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
