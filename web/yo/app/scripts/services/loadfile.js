'use strict';

/**
 * @ngdoc service
 * @name oncokb.loadFile
 * @description
 * # loadFile
 * Service in the oncokb.
 */
angular.module('oncokbApp')
    .service('loadFile', function loadFile($route, $location, $q, storage, documents) {
        return function() {
            var title = $route.current.params.geneName;
            var userId = $route.current.params.user;
            var recheckDocPromise;

            function check() {
                var deferred = $q.defer();
                storage.requireAuth(true, userId).then(function() {
                    var _documents = documents.get({title: title});

                    if (angular.isArray(_documents) && _documents.length > 0) {
                        deferred.resolve(storage.getRealtimeDocument(_documents[0].id));
                    } else {
                        storage.retrieveAllFiles().then(function(result) {
                            documents.set(result);
                            var __documents = documents.get({title: title});
                            if (angular.isArray(__documents) && __documents.length > 0) {
                                deferred.resolve(storage.getRealtimeDocument(__documents[0].id));
                            } else {
                                deferred.resolve(null);
                            }
                        });
                    }
                });
                return deferred.promise;
            }

            recheckDocPromise = check();

            return $q.all([recheckDocPromise]).then(function(realdocument) {
                if (angular.isArray(realdocument) && realdocument.length > 0) {
                    if (realdocument[0]) {
                        return realdocument[0];
                    }
                    $location.path('/');
                } else {
                    $location.path('/');
                }
            });
        };
    });
