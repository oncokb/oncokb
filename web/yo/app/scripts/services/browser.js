'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.browser
 * @description
 * # browser
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .service('browser', function browser($window) {
        return {
            detectBrowser: function() {
                var userAgent = $window.navigator.userAgent;
                var browsers = {
                    chrome: /chrome/i,
                    safari: /safari/i,
                    firefox: /firefox/i,
                    ie: /MSIE|Trident/i
                };

                for (var key in browsers) {
                    if (browsers[key].test(userAgent)) {
                        return key;
                    }
                }

                return 'unknown';
            }
        };
    });
