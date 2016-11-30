'use strict';

/**
 * @ngdoc directive
 * @name oncokb.directive:reportInfo
 * @description
 * # reportInfo
 */
angular.module('oncokbApp')
    .directive('reportInfo', function() {
        return {
            restrict: 'E',
            compile: function(element, attrs) {
                var title = attrs.title || '';
                var oneCell = attrs.oneCell !== 'false';
                var border = attrs.border !== 'false';
                var content = attrs.content || '';
                var margin = attrs.margin !== 'false';

                var htmlText = '<div style="display: inline-block; width: 100%;' + ((margin && border) ? 'margin: 5px 0;' : '') + '">' +
                    '<div style="width:80%; float:left"><b>' + title + '</b></div>' +
                    '<div style="width:20%; float:left; text-align:right"><b>Lab EZ</b></div>' +
                    '<div ng-if="' + oneCell + '" style="width:100%; float:left; ' + (border ? 'border: 1px solid black;padding: 5px 8px;' : '') + 'min-height: 30px" ng-bind-html=' + content + '></div>' +
                    '</div>';
                element.replaceWith(htmlText);
            }
        };
    });
