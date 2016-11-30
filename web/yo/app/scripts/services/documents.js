'use strict';

/**
 * @ngdoc service
 * @name oncokb.documents
 * @description
 * # documents
 * Service in the oncokb
 */
angular.module('oncokbApp')
    .service('documents', function documents(storage, $rootScope, $q, users) {
        // AngularJS will instantiate a singleton by calling "new" on this function
        var self = {};
        self.documents = [];
        self.documentsL = 0;

        function searchById(id) {
            for (var i = 0; i < self.documentsL; i++) {
                if (self.documents[i].id === id) {
                    return self.documents[i];
                }
            }
            return false;
        }

        // Ideally should only return one file, but may have duplicates
        function searchByTitle(title) {
            var seletecd = [];
            for (var i = 0; i < self.documentsL; i++) {
                if (self.documents[i].title === title) {
                    seletecd.push(self.documents[i]);
                }
            }
            return seletecd;
        }

        function getPermission(index, callback) {
            if (index < self.documentsL) {
                storage.getPermission(self.documents[index].id).then(function(file) {
                    self.documents[index].permissions = file.items;
                    getPermission(++index, callback);
                });
            } else {
                callback(true);
            }
        }

        function getAllPermission(onComplete) {
            if (angular.isArray(self.documents) && self.documents.length > 0) {
                getPermission(0, onComplete);
            }
        }

        // gene.json format
        function setStatus(genes) {
            if (angular.isArray(genes)) {
                var status = {};

                genes.forEach(function(e) {
                    status[e.hugoSymbol] = e.status;
                });

                for (var i = 0; i < self.documentsL; i++) {
                    var gene = self.documents[i].title;
                    if (status.hasOwnProperty(gene)) {
                        self.documents[i].oncokbStatus = status[gene];
                    } else {
                        self.documents[i].oncokbStatus = 'Not available';
                    }
                }
            }
        }

        function updateStatus(gene) {
            if (angular.isObject(gene) && !angular.isArray(gene) && gene.geneId && gene.status) {
                for (var i = 0; i < self.documentsL; i++) {
                    if (self.documents[i].title === gene.geneId) {
                        self.documents[i].oncokbStatus = gene.status;
                    }
                }
            }
        }

        function setCurators() {
            var usersData = users.getUsers();
            if (angular.isArray(usersData)) {
                var usersL = usersData.length;
                for (var i = 0; i < self.documentsL; i++) {
                    var gene = self.documents[i].title;
                    var curators = [];
                    for (var j = 0; j < usersL; j++) {
                        if (usersData[j].genes && usersData[j].genes.indexOf(gene) !== -1) {
                            var _user = {
                                name: usersData[j].name,
                                email: usersData[j].email,
                                mskccEmail: usersData[j].mskccEmail
                            };
                            curators.push(_user);
                            self.documents[i].phase = usersData[j].phases[usersData[j].genes.indexOf(gene)];
                        }
                    }
                    self.documents[i].curators = curators;
                }
            }
        }

        return {
            setWithPermission: function(documents) {
                var deferred = $q.defer();
                var onComplete = function(result) {
                    if (result && !result.error) {
                        deferred.resolve(result);
                    } else {
                        deferred.reject(result);
                    }
                };

                if (angular.isArray(documents)) {
                    self.documents = documents;
                    self.documentsL = documents.length;
                    getAllPermission(onComplete);
                }
                return deferred.promise;
            },
            set: function(documents) {
                if (angular.isArray(documents)) {
                    self.documents = documents;
                    self.documentsL = documents.length;
                    setCurators();
                }
            },
            setStatus: setStatus,
            updateStatus: updateStatus,
            setCurators: setCurators,
            get: function(params) {
                // Only ID and Title accepted
                if (angular.isObject(params)) {
                    if (params.hasOwnProperty('id')) {
                        return searchById(params.id);
                    } else if (params.hasOwnProperty('title')) {
                        return searchByTitle(params.title);
                    }
                    return false;
                }
                return self.documents;
            }
        };
    });
