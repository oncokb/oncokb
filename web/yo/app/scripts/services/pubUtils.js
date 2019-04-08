'use strict';

/**
 * @ngdoc factory
 * @name oncokbApp.pubUtils
 * @author Jing Su
 * @description
 * This factory is a cache service used for storing validated PMIDs.
 */
angular.module('oncokbApp')
    .factory('pubUtils', function() {
        var validatedPubs = {};

        function clear() {
            validatedPubs = {};
        }
        function get(pubs) {
            var validatedPubsArray = [];
            var notValidatedPubs = [];
            _.each(pubs, function(pub){
                if (validatedPubs[pub.id]) {
                    validatedPubsArray.push(pub);
                } else {
                    notValidatedPubs.push(pub);
                }
            });
            return {
                validatedPuds: validatedPubsArray,
                notValidatedPuds: notValidatedPubs
            };
        }
        function set(pubs) {
            _.each(pubs, function(pub){
                validatedPubs[pub.id] = pub;
            });
        }

        return {
            clear: clear,
            get: get,
            set: set
        };
    });
