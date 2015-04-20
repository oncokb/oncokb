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
                var src = '';
                var content = '';
                var hideEvent = '';
                var my= attrs.hasOwnProperty('my')?attrs.my: "bottom center";
                var at= attrs.hasOwnProperty('at')?attrs.at: "top center";

                if(attrs.type && ['pmid', 'nct'].indexOf(attrs.type) !== -1){
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
                    content = $(src);
                    hideEvent = 'unfocus';
                    my = 'top left';
                    at = 'bottom right';
                }

                if(!attrs.type){
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
                    hide: {
                        event: hideEvent,
                        fixed: true,
                        delay: 100
                    }
                };
                $(element).qtip(options);
            }
        };
    });
