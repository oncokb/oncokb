'use strict';

angular.module('oncokbApp')
    .controller('GenesCtrl', ['$scope', '$rootScope', '$location', '$timeout',
        '$routeParams', '_', 'config', 'storage', 'documents',
        'users', 'DTColumnDefBuilder', 'DTOptionsBuilder', 'DatabaseConnector',
        'OncoKB', 'stringUtils', 'S', 'mainUtils', 'gapi', 'UUIDjs', 'dialogs', 'additionalFile', '$firebaseObject', '$firebaseArray',
        function($scope, $rootScope, $location, $timeout, $routeParams, _,
                 config, storage, Documents, users,
                 DTColumnDefBuilder, DTOptionsBuilder, DatabaseConnector,
                 OncoKB, stringUtils, S, MainUtils, gapi, UUIDjs, dialogs, additionalFile, $firebaseObject, $firebaseArray) {
            function saveGene(docs, docIndex, callback) {
                if (docIndex < docs.length) {
                    var fileId = docs[docIndex].id;
                    storage.getRealtimeDocument(fileId).then(function(realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            console.log(docs[docIndex].title, '\t\t', docIndex);
                            console.log('\t copying');
                            var gene = realtime.getModel().getRoot().get('gene');
                            var vus = realtime.getModel().getRoot().get('vus');
                            if (gene) {
                                var geneData = stringUtils.getGeneData(gene, true, true);
                                var vusData = stringUtils.getVUSFullData(vus, true);
                                var params = {};

                                if (geneData) {
                                    params.gene = JSON.stringify(geneData);
                                }
                                if (vusData) {
                                    params.vus = JSON.stringify(vusData);
                                }
                                DatabaseConnector.updateGene(params,
                                    function(result) {
                                        console.log('\t success', result);
                                        $timeout(function() {
                                            saveGene(docs, ++docIndex, callback);
                                        }, 200, false);
                                    },
                                    function(result) {
                                        console.log('\t failed', result);
                                        $timeout(function() {
                                            saveGene(docs, ++docIndex, callback);
                                        }, 200, false);
                                    }
                                );
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function() {
                                    saveGene(docs, ++docIndex, callback);
                                }, 200, false);
                            }
                        }
                    });
                } else {
                    if (callback) {
                        callback();
                    }
                    console.log('finished.');
                }
            }
            $scope.showDocs = function() {
                $scope.documents.forEach(function(item) {
                    console.log(item.title);
                });
                // console.log($scope.documents);
            };
            $scope.metaFlags = {};
            
            function processMeta() {
                additionalFile.load(['all']).then(function(result) {
                    _.each(_.keys($rootScope.metaData), function(hugoSymbol) {
                        $scope.metaFlags[hugoSymbol] = {
                            hugoSymbol: hugoSymbol,
                            lastModifiedBy: $rootScope.metaData[hugoSymbol].lastModifiedBy,
                            lastModifiedAt: $rootScope.metaData[hugoSymbol].lastModifiedAt,
                            queues: 0,
                            review: 'No'
                        };
                        if (_.keys($rootScope.metaData[hugoSymbol]).length > 2) {
                            $scope.metaFlags[hugoSymbol].review = 'Yes';
                        }
                        if ($rootScope.firebaseQueues[hugoSymbol]) {
                            _.each($rootScope.firebaseQueues[hugoSymbol].queue, function(item) {
                                if (!item.curated) {
                                    if ($scope.metaFlags[hugoSymbol] && $scope.metaFlags[hugoSymbol].queues) {
                                        $scope.metaFlags[hugoSymbol].queues++;
                                    } else {
                                        $scope.metaFlags[hugoSymbol] = {
                                            queues: 1
                                        };
                                    }
                                }
                            });
                        }
                    });
                    $scope.status.rendering = false;
                });
            }
            processMeta();
            var dueDay = angular.element(document.querySelector('#genesdatepicker'));
            dueDay.datepicker();
            $scope.redirect = function(path) {
                $location.path(path);
            };

            $scope.checkError = function() {
                console.log($rootScope.errors);
            };

            $scope.saveAllGenes = function() {
                $scope.status.saveAllGenes = false;
                saveGene($scope.documents, 0, function() {
                    $scope.status.saveAllGenes = true;
                });
            };

            $scope.userRole = users.getMe().role;

            var sorting = [[2, 'asc'], [1, 'desc'], [0, 'asc']];
            if (users.getMe().role === 8) {
                sorting = [[4, 'desc'], [5, 'desc'], [1, 'desc'], [0, 'asc']];
            }

            $scope.dtOptions = DTOptionsBuilder
                .newOptions()
                .withDOM('ifrtlp')
                .withOption('order', sorting)
                .withBootstrap();

            $scope.dtColumns = [
                DTColumnDefBuilder.newColumnDef(0),
                DTColumnDefBuilder.newColumnDef(1).withOption('sType', 'date'),
                DTColumnDefBuilder.newColumnDef(2),
                DTColumnDefBuilder.newColumnDef(3)
            ];
            if (users.getMe().role === 8) {
                $scope.dtColumns.push(DTColumnDefBuilder.newColumnDef(4));
                $scope.dtColumns.push(DTColumnDefBuilder.newColumnDef(5));
            }

            $scope.status = {
                saveAllGenes: true,
                migrate: true,
                rendering: true,
                queueRendering: true
            };
            $scope.adminEmails = [];
            // $scope.getDocs();
            $scope.oncoTree = {
                mainTypes: {}
            };
            $scope.mappedTumorTypes = {};
            getCacheStatus();

            var newGenes = [];

            $scope.migrate = function() {
                // console.log($scope.documents);
                $scope.status.migrate = false;
                importer
                    .migrate()
                    .then(function(result) {
                        if (result && result.error) {
                            $scope.status.migrate = true;
                        } else {
                            $scope.status.migrate = true;
                        }
                    });
            };

            $scope.create = function() {
            };

            $scope.convertData = function() {
                console.info('Converting tumor types to OncoTree tumor types...');

                convertData(0, function() {
                    console.info('Finished.');
                });
            };

            $scope.findRelevantVariants = function() {
                console.info('Finding relevant variants...');
                var list = [];

                findRelevantVariants(list, 0, function() {
                    console.info('Finished.');
                });
            };

            $scope.changeCacheStatus = function() {
                if ($scope.status.cache === 'enabled') {
                    DatabaseConnector.disableCache()
                        .then(function() {
                            $scope.status.cache = 'disabled';
                        }, function() {
                            $scope.status.cache = 'unknown';
                        });
                } else if ($scope.status.cache === 'disabled') {
                    DatabaseConnector.enableCache()
                        .then(function() {
                            $scope.status.cache = 'enabled';
                        }, function() {
                            $scope.status.cache = 'unknown';
                        });
                }
            };

            $scope.resetCache = function() {
                DatabaseConnector.resetCache()
                    .then(function() {
                        console.log('succeed.');
                    }, function() {
                        console.log('failed.');
                    });
            };

            $scope.showValidationResult = function() {
                console.info('Gene\tVariant\tCategory');

                showValidationResult(0, function() {
                    console.info('Finished.');
                });
            };

            $scope.developerCheck = function() {
                return MainUtils.developerCheck(users.getMe().name);
            };

            function getCacheStatus() {
                DatabaseConnector.getCacheStatus().then(function(result) {
                    $scope.status.cache = result.hasOwnProperty('status') ? result.status : 'unknown';
                }, function(result) {
                    $scope.status.cache = 'unknown';
                });
            }

            function isExist(array, string) {
                var mark = false;
                _.each(array, function(item) {
                    if (item.toString().toLowerCase() === string.toString().toLowerCase()) {
                        mark = true;
                    }
                });
                return mark;
            }

            function findIndexIgnorecase(array, string) {
                var index = -1;
                _.each(array, function(item, ind) {
                    if (item.toString().toLowerCase() === string.toString().toLowerCase()) {
                        index = ind;
                    }
                });
                return index;
            }

            function isUndefinedOrEmpty(str) {
                if (_.isUndefined(str)) {
                    return true;
                }
                return str.toString().trim() === '';
            }
        }]
    );
