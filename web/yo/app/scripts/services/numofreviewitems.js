'use strict';

/**
 * @ngdoc factory
 * @name oncokbApp.numOfReviewItems
 * @description
 * # numOfReviewItems
 * This factory is used for record number of changes in Review mode.
 * If all changes are replied(accept/reject), the number of changes should be set to 0 under the specific username.
 * The "Accept all" button will be disabled once the number is 0.
 */
angular.module('oncokbApp')
    .factory('numOfReviewItems', function() {
        var reviewItemsCount = {};

        function get() {
            return reviewItemsCount;
        }
        function set(userName, count) {
            if (_.isEmpty(userName)) {
                reviewItemsCount = count;
            } else {
                reviewItemsCount[userName] = count;
            }
        }
        function add(updatedBy) {
            if (_.isUndefined(reviewItemsCount[updatedBy])) {
                reviewItemsCount[updatedBy] = 0;
            }
            reviewItemsCount[updatedBy]++;
        }
        function minus(updatedBy) {
            reviewItemsCount[updatedBy]--;
        }

        return {
            set: set,
            get: get,
            add: add,
            minus: minus
        };
    });
