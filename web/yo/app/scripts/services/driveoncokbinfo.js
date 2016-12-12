'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.driveOncokbInfo
 * @description
 * # driveOncokbInfo
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .service('driveOncokbInfo', function driveOncokbInfo(_) {
        var self = {};
        self.genes = {};
        self.genesA = [];
        self.genesL = 0;

        function Gene() {
            this.gene = '';
            this.pubMedLinks = [];
            this.mutations = [];
        }

        function get(params, key) {
            if (angular.isUndefined(key)) {
                return self.genesA;
            }
            if (angular.isObject(params)) {
                if (params.hasOwnProperty('gene')) {
                    var _gene = params.gene.toString().trim();
                    if (self.genes.hasOwnProperty(_gene)) {
                        var __obj = {
                            gene: _gene
                        };
                        __obj[key] = self.genes[_gene][key];
                        return __obj;
                    }
                    return {};
                }
                return false;
            }

            var desiredA = [];
            _.each(self.genes, function(item, geneKey) {
                var _obj = {
                    gene: geneKey.gene
                };
                _obj[key] = geneKey[key];
                desiredA.push(_obj);
            });
            return desiredA;
        }

        function set(data, dataKey, geneKey, type) {
            if (!angular.isUndefined(dataKey) && !angular.isUndefined(geneKey) && angular.isArray(data)) {
                var jsonObject = {};
                data.forEach(function(e) {
                    if (e.gene) {
                        var _gene = e.gene.toString().trim();
                        if (!self.genes.hasOwnProperty(_gene)) {
                            self.genes[_gene] = new Gene();
                        }

                        if (angular.isDefined(type) && type === 'object') {
                            if (e[dataKey]) {
                                jsonObject = JSON.parse(e[dataKey]);
                                self.genes[_gene][geneKey] = _.forIn(jsonObject, function(value, key) {
                                    jsonObject[key] = splitLinks(value);
                                });
                            } else {
                                self.genes[_gene][geneKey] = {};
                            }
                        } else if (angular.isString(e[dataKey])) {
                            self.genes[_gene][geneKey] = splitLinks(e[dataKey]);
                        } else {
                            self.genes[_gene][geneKey] = [];
                        }
                    }
                });
                updateGenesArray();
            }
        }

        function splitLinks(str) {
            return str.split(';').map(function(e) {
                return e.toString().trim();
            });
        }

        function updateGenesArray() {
            self.genesA.length = 0;

            _.each(self.genes, function(gene) {
                self.genesA.push(gene);
            });
            self.genesL = self.genesA.length;
        }

        return {
            setSuggestions: function(suggestions) {
                set(suggestions, 'mutations', 'mutations');
            },
            setPubMed: function(pubMed) {
                set(pubMed, 'links', 'pubMedLinks');
                set(pubMed, 'mutationLinks', 'pubMedMutationLinks', 'object');
            },
            get: get,
            getSuggestions: function(params) {
                return get(params, 'mutations');
            },
            getPubMed: function(params) {
                var pubMed = {
                    gene: [],
                    mutations: {}
                };
                var gene = get(params, 'pubMedLinks');
                var mutations = get(params, 'pubMedMutationLinks');
                if (gene) {
                    pubMed.gene = gene;
                }

                if (mutations) {
                    pubMed.mutations = mutations;
                }
                return pubMed;
            },
            getMutation: function(gene) {
                if (angular.isString(gene)) {
                    var _gene = get({gene: gene}, 'mutations');
                    if (_gene) {
                        return _gene.mutations;
                    }
                    return undefined;
                }
                return false;
            }
        };
    });
