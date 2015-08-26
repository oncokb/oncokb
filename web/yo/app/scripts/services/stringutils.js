'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.stringUtils
 * @description
 * # stringUtils
 * Factory in the oncokbApp.
 */
angular.module('oncokbApp')
  .factory('stringUtils', function () {
    // Service logic
    // ...

    var meaningOfLife = 42;

    // Public API here
    return {
      trimMutationName: function (mutation) {
        if(typeof mutation === 'string') {
          if(mutation.indexOf('p.') === 0) {
            mutation = mutation.substring(2);
          }
        }
        return mutation;
      }
    };
  });
