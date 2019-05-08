'use strict';

/**
 * @ngdoc factory
 * @name oncokbApp.checkNameChange
 * @author Jing Su
 * @description
 * # checkNameChange
 * This factory is used for checking if mutations are deleted in Review mode.
 * If mutations are deleted, we should avoid setReviewRelatedContent() triggered in realtimestring.js.
 * Otherwise, each mutation will be moved forward 1 index in firebase which mislead plenty of "Name Updated" changes in review mode.
 */
angular.module('oncokbApp')
    .factory('checkNameChange', function() {
        var hasDeletedMutation = false;

        function clear() {
            hasDeletedMutation = false;
        }
        function get() {
            return hasDeletedMutation;
        }
        function set(bool) {
            hasDeletedMutation = bool;
        }

        return {
            clear: clear,
            get: get,
            set: set
        };
    });
