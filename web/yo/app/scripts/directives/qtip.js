'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:qtip
 * @description
 * # qtip
 */
angular.module('oncokbApp')
    .directive('qtip', function() {
        return {
            restrict: 'A',
            scope: {
                time: '=',
                by: '='
            },
            link: function(scope, element, attrs) {
                var src = '';
                var content = '';
                var hideEvent = 'mouseleave';
                var my = attrs.hasOwnProperty('my') ? attrs.my : 'bottom center';
                var at = attrs.hasOwnProperty('at') ? attrs.at : 'top center';

                if (attrs.type && ['pmid', 'nct', 'abstract'].indexOf(attrs.type) !== -1) {
                    src = '<iframe width="600px" height="400px" src=\'';
                    if (attrs.type && attrs.number) {
                        switch (attrs.type) {
                        case 'pmid':
                            src += 'https://www.ncbi.nlm.nih.gov/pubmed/' + attrs.number;
                            break;
                        case 'nct':
                            src += 'https://clinicaltrials.gov/show/' + attrs.number;
                            break;
                        case 'abstract':
                            src += attrs.number;
                            break;
                        default:
                            break;
                        }
                    }
                    src += '\'></iframe>';
                    content = $(src);
                    hideEvent = 'unfocus';
                    my = 'top left';
                    at = 'bottom right';
                } else if (attrs.type === 'vusItem') {
                    content = '<span>Last edit: ' + new Date(scope.time).toLocaleDateString() + '</span><br/><span>By: ' + scope.by + '</span>';
                }

                if (!attrs.type) {
                    hideEvent = 'mouseleave';
                    content = attrs.content;
                }

                var options = {
                    content: content,
                    position: {
                        my: my,
                        at: at,
                        viewport: $(window)
                    },
                    style: {
                        classes: 'qtip-light qtip-rounded'
                    },
                    show: 'mouseover',
                    hide: {
                        event: hideEvent,
                        fixed: true,
                        delay: 500
                    }
                };
                if (attrs.number !== undefined && attrs.number.length > 0) {
                    $(element).qtip(options);
                }

                scope.$watch('time', function(n) {
                    if (n) {
                        if ($(element).data('qtip')) {
                            $(element).qtip('api').set('content.text', '<span>Last edit: ' + new Date(scope.time).toLocaleDateString() + '</span><br/><span>By: ' + scope.by + '</span>');
                        }
                    }
                });
            }
        };
    });
