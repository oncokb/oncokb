'use strict';

/**
 * @ngdoc directive
 * @name oncokb.directive:comments
 * @description
 * # comments
 */
angular.module('oncokbApp')
  .directive('commentsDict', function (DatabaseConnector, S) {
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

        if(attrs.geneName) {
          scope.geneName = attrs.geneName;
        }

        if(attrs.mutation) {
          scope.mutation = attrs.mutation;
        }

        if(attrs.tumorType) {
          scope.tumorType = attrs.tumorType;
        }

        if(attrs.therapy) {
          scope.therapy = attrs.therapy;
        }

        if(attrs.displayName) {
          scope.displayName = attrs.displayName;
        }

        //create a copy of comments. The original comments is a big
        //object, has too many methods, cannot use scope watch on the
        //whole object.
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

          var commentsCopy = {
            resolved: [],
            content: []
          };
          for(var i = 0; i < scope.comments.length; i++) {
            commentsCopy.resolved.push(scope.comments.get(i).resolved.getText());
            commentsCopy.content.push(scope.comments.get(i).content.getText());
          }
          scope.commentsCopy = commentsCopy;
        });

        scope.$watch('commentsCopy.resolved', function(){
          var allResolved = true;
          if(scope.commentsCopy.resolved.length > 0) {
            scope.status.hasComment = true;
            for(var i = 0; i< scope.commentsCopy.resolved.length; i++) {
              if(scope.commentsCopy.resolved[i] !== 'true') {
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
      controller: function($scope, $rootScope){
        function sendEmail(subject, content) {
          var param = {subject: subject, content: content};

          DatabaseConnector.sendEmail(
            param,
            function(result){ console.log('success', result);}, 
            function(result){ console.log('failed', result);}
          );
        }

        function createEmail(type, comment, previousComment) {
          if($rootScope.user.role < 8) {
            var subject = '',
                content = '';

            if($scope.geneName) {
              subject += 'Gene: ' + $scope.geneName;
            }

            if($scope.mutation) {
              subject += ', Mutation: ' + $scope.mutation;
            }

            if($scope.tumorType) {
              subject += ', TumorType: ' + $scope.tumorType;
            }

            if($scope.therapy) {
              subject += ', Therapy: ' + $scope.therapy;
            }

            if($scope.object[$scope.key]) {
              var _display = '';
              if($scope.object[$scope.key].display) {
                _display = ', ' + $scope.object[$scope.key].display;
              }else if($scope.displayName){
                _display = ', ' + $scope.displayName;
              }else{
                _display = '';
              }
              subject += _display;
            }


            subject += ' comment has been ';
            content = 'Comment content: \n\n' + comment;
            switch (type) {
              case 'add':
                subject += 'ADDED';
                break;
              case 'resolved':
                subject += 'RESOLVED';
                break;
              case 'delete':
                subject += 'REMOVED';
                break;
              case 'change':
                subject += 'CHANGED';
                content = 'Previous comment content: \n\n' + previousComment + 
                '\n\nCurrent comment content: \n\n' + comment;
                break;
              default:
                break;
            }

            subject +=  ' by ' + $rootScope.user.name;

            sendEmail(subject, content);
          }
        }

        $scope.blur = function(index) {
          if($scope.comments.get(index).content.getText() !== $scope.commentsCopy.content[index]){
            createEmail('change', S($scope.comments.get(index).content.getText()).stripTags().s, S($scope.commentsCopy.content[index]).stripTags().s);
            $scope.commentsCopy.content[index] = $scope.comments.get(index).content.getText();
          }
        };

        $scope.add = function() {
          console.log($scope);
          $scope.addComment({arg1: $scope.object, arg2: $scope.key, arg3: $scope.params.newCommentContent});
          createEmail('add', $scope.params.newCommentContent);
          $scope.params.newCommentContent = '';
        };
        $scope.resolve = function(index) {
          $scope.comments.get(index).resolved.setText('true');
          $scope.commentsCopy.resolved[index] = 'true';
          createEmail('resolved', S($scope.comments.get(index).content.getText()).stripTags().s);
        };
        $scope.delete = function(index) {
          createEmail('delete', S($scope.comments.get(index).content.getText()).stripTags().s);
          $scope.comments.remove(index);
        };
      }
    };
  });
