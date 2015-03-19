'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:qtip
 * @description
 * # qtip
 */
angular.module('oncokbApp')
  .directive('qtip', function () {
    return {
      restrict: 'A',
      link: function (scope, element, attrs) {
        var src = '<iframe width="600px" height="400px" src=\'';
        if(attrs.type && attrs.number) {
          switch (attrs.type) {
            case 'pmid':
              src += 'http://www.ncbi.nlm.nih.gov/pubmed/' + attrs.number;
              break;
            case 'nct':
              src += 'https://clinicaltrials.gov/show/' + attrs.number;
              break;
            default:
              break;
          }
        }

        src += '\'></iframe>';

        var options = {
          content: $(src),
          position: {
            my: 'top left',
            at: 'bottom right',
            viewport: $(window)
          },
          style: {
            classes: 'qtip-light qtip-rounded'
          },
          hide: {
            event: 'unfocus'
          }
        };
        $(element).qtip(options);
      }
    };
  });
