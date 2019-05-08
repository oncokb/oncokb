'use strict';

/**
 * @ngdoc factory
 * @name oncokbApp.pubCache
 * @author Jing Su
 * @description
 * This factory is a cache service used for storing validated PMIDs.
 */
angular.module('oncokbApp')
    .factory('pubCache', function($q, FindRegex) {
        var validatedPubs = {};

        function clear() {
            validatedPubs = {};
        }
        function get(pubs) {
            var validatedPubsArray = [];
            var notValidatedPubs = [];
            _.each(pubs, function(pub){
                if (validatedPubs[pub.id]) {
                    validatedPubsArray.push(validatedPubs[pub.id]);
                } else {
                    notValidatedPubs.push(pub);
                }
            });
            return {
                validatedPubs: validatedPubsArray,
                notValidatedPubs: notValidatedPubs
            };
        }
        function set(pubs) {
            _.each(pubs, function(pub){
                validatedPubs[pub.id] = pub;
            });
        }
        function validatePub(pubs) {
            var deferred = $q.defer();
            FindRegex.validation(pubs).then(function(result) {
                set(result);
                deferred.resolve(result);
            }, function(error) {
                deferred.reject(error);
            });
            return deferred.promise;
        }

        return {
            clear: clear,
            get: get,
            set: set,
            validatePub: validatePub
        };
    });
