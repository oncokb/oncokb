'use strict';

angular.module('oncokbApp')
    .controller('ReportgeneratorCtrl', ['$scope', 'FileUploader', 'dialogs', 'storage', 'documents', 'OncoKB', 'DatabaseConnector', 'stringUtils', '$timeout', '_',
        function($scope, FileUploader, dialogs, storage, Documents, OncoKB, DatabaseConnector, stringUtils, $timeout, _) {
            function initUploader() {
                var uploader = $scope.uploader = new FileUploader();

                uploader.onWhenAddingFileFailed = function(item /* {File|FileLikeObject}*/, filter, options) {
                    console.info('onWhenAddingFileFailed', item, filter, options);
                };

                uploader.onAfterAddingFile = function(fileItem) {
                    console.info('onAfterAddingFile', fileItem);
                    $scope.status = {
                        fileSelected: false,
                        isXLSX: false,
                        isXML: false,
                        rendering: true
                    };
                    $scope.status.fileSelected = true;
                    $scope.fileItem = fileItem;

                    if (fileItem.file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
                        $scope.status.isXLSX = true;
                    } else if (fileItem.file.type === 'text/xml') {
                        $scope.status.isXML = true;
                    } else {
                        dialogs.error('Error', 'Do not support the type of selected file, only XLSX or XML file is supported.');
                        uploader.removeFromQueue(fileItem);
                    }

                    console.log($scope.status);
                };

                uploader.onAfterAddingAll = function(addedFileItems) {
                    console.info('onAfterAddingAll', addedFileItems);
                };
                uploader.onBeforeUploadItem = function(item) {
                    console.info('onBeforeUploadItem', item);
                };
                uploader.onProgressItem = function(fileItem, progress) {
                    console.info('onProgressItem', fileItem, progress);
                };
                uploader.onProgressAll = function(progress) {
                    console.info('onProgressAll', progress);
                };
                uploader.onSuccessItem = function(fileItem, response, status, headers) {
                    console.info('onSuccessItem', fileItem, response, status, headers);
                };
                uploader.onErrorItem = function(fileItem, response, status, headers) {
                    console.info('onErrorItem', fileItem, response, status, headers);
                };
                uploader.onCancelItem = function(fileItem, response, status, headers) {
                    console.info('onCancelItem', fileItem, response, status, headers);
                };
                uploader.onCompleteItem = function(fileItem, response, status, headers) {
                    console.info('onCompleteItem', fileItem, response, status, headers);
                };
                uploader.onCompleteAll = function() {
                    console.info('onCompleteAll');
                };
            }

            $scope.init = function() {
                $scope.status = {
                    fileSelected: false,
                    isXLSX: false,
                    isXML: false,
                    rendering: false
                };
                initUploader();
                $scope.resultTable = false;
                $scope.loading = false;
                $scope.disableButton = true;
                var geneNames = [];
                if (OncoKB.global.genes) {
                    storage.requireAuth(true).then(function() {
                        storage.retrieveAllFiles().then(function(result) {
                            Documents.set(result);
                            Documents.setStatus(OncoKB.global.genes);
                            $scope.documents = Documents.get();
                            _.each($scope.documents, function(doc) {
                                geneNames.push(doc.title);
                            });
                            $scope.geneNames = geneNames;
                        });
                    });
                } else {
                    DatabaseConnector.getAllGene(function(data) {
                        OncoKB.global.genes = data;
                        storage.requireAuth(true).then(function() {
                            storage.retrieveAllFiles().then(function(result) {
                                Documents.set(result);
                                Documents.setStatus(OncoKB.global.genes);
                                $scope.documents = Documents.get();
                                _.each($scope.documents, function(doc) {
                                    geneNames.push(doc.title);
                                });
                                $scope.geneNames = geneNames;
                            });
                        });
                    });
                }
            };
            $scope.dt = {};
            $scope.dt.dtOptions = {
                paging: false,
                hasBootstrap: true,

                scrollY: 500,
                scrollCollapse: true

            };
            $scope.searchResults = [];
            $scope.geneNames = [];
            var results = [];
            $scope.checkInputStatus = function() {
                $scope.disableButton = true;
                if (!_.isUndefined($scope.inputGenes) && $scope.inputGenes.length > 0 && ($scope.redHand || $scope.obsolete || $scope.inconclusive)) {
                    $scope.disableButton = false;
                }
            };
            $scope.searchVariants = function(inputGenes, index) {
                $scope.loading = true;
                if (index === 0) {
                    results = [];
                }
                var documents = Documents.get({title: inputGenes[index]});
                var document = _.isArray(documents) && documents.length === 1 ? documents[0] : null;
                if (document) {
                    storage.getRealtimeDocument(document.id).then(function(realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            var model = realtime.getModel();
                            var geneModel = model.getRoot().get('gene');
                            if (geneModel) {
                                var gene = stringUtils.getGeneData(geneModel, false, false, false);
                                if ($scope.redHand) {
                                    _.each(gene.mutations, function(mutation) {
                                        if (mutation.oncogenic_eStatus.curated === false) {
                                            results.push({gene: gene.name, annotation: mutation.name, status: 'Red Hand'});
                                        }
                                    });
                                }
                                if ($scope.obsolete) {
                                    if (gene.summary_eStatus.obsolete === 'true') {
                                        results.push({gene: gene.name, annotation: 'summary', status: 'obsolete'});
                                    }
                                    if (gene.background_eStatus.obsolete === 'true') {
                                        results.push({gene: gene.name, annotation: 'background', status: 'obsolete'});
                                    }
                                    _.each(gene.mutations, function(mutation) {
                                        if (mutation.name_eStatus.obsolete === 'true') {
                                            results.push({gene: gene.name, annotation: mutation.name, status: 'obsolete'});
                                        }
                                    });
                                }
                                if ($scope.inconclusive) {
                                    _.each(gene.mutations, function(mutation) {
                                        if (mutation.effect.value === 'Inconclusive' && mutation.oncogenic === 'Inconclusive') {
                                            results.push({gene: gene.name, annotation: mutation.name, status: 'Inconclusive/Inconclusive'});
                                        }
                                    });
                                }
                                if (index === inputGenes.length - 1) {
                                    $scope.searchResults = results;
                                    $scope.resultTable = true;
                                    $scope.loading = false;
                                } else {
                                    $timeout(function() {
                                        index++;
                                        $scope.searchVariants(inputGenes, index);
                                    }, 200);
                                }
                            } else {
                                console.log('\t\tNo gene model.');
                            }
                        }
                    });
                }
            };
        }]);
