'use strict';

angular.module('oncokbApp')
    .controller('ReportgeneratorCtrl', ['$scope', 'FileUploader', 'dialogs', 'storage', 'documents', 'OncoKB', 'DatabaseConnector', 'stringUtils', '$timeout', '_', '$http', '$q', 'FindRegex', 'mainUtils',
        function($scope, FileUploader, dialogs, storage, Documents, OncoKB, DatabaseConnector, stringUtils, $timeout, _, $http, $q, FindRegex, mainUtils) {
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
            var historyResults;
            $scope.disableHistoryButton = true;
            $scope.checkHistoryInputStatus = function() {
                if (_.isArray($scope.genesForHistory) && $scope.genesForHistory.length > 0) {
                    $scope.disableHistoryButton = false;
                } else {
                    $scope.disableHistoryButton = true;
                }
            };
            $scope.searchHistory = function(genesForHistory, index) {
                $scope.loading = true;
                if (index === 0) {
                    historyResults = [];
                }
                var documents = Documents.get({title: genesForHistory[index]});
                var document = _.isArray(documents) && documents.length === 1 ? documents[0] : null;
                if (document) {
                    storage.getRealtimeDocument(document.id).then(function(realtime) {
                        if (realtime && realtime.error) {
                            dialogs.error('Error', 'Fail to load ' + genesForHistory[index] + ' document. Please contact the developer.');
                        } else {
                            var model = realtime.getModel();
                            var historyModel = model.getRoot().get('history');
                            if (historyModel) {
                                var historyData = stringUtils.getHistoryData(historyModel).api;
                                _.each(historyData, function(item) {
                                    historyResults.push({gene: genesForHistory[index], admin: item.admin, timeStamp: item.timeStamp, records: item.records});
                                });
                            }
                            if (index === genesForHistory.length - 1) {
                                $scope.historySearchResults = historyResults;
                                $scope.showHistoryResultTable = true;
                                $scope.loading = false;
                            } else {
                                $timeout(function() {
                                    index++;
                                    $scope.searchHistory(genesForHistory, index);
                                }, 200);
                            }
                        }
                    });
                }
            };

            $scope.reviewedDT = {};
            $scope.reviewedDT.dtOptions = {
                paging: true,
                hasBootstrap: true,
                scrollY: 500,
                scrollCollapse: true
            };
            $scope.evidenceType = '';
            $scope.evidenceTypes = [{
                label: 'Gene Type',
                value: 'gene_type'
            }, {
                label: 'Mutation Effect',
                value: 'mutation_effect'
            }, {
                label: 'Tumor Summary',
                value: 'tumor_summary'
            }, {
                label: 'Drugs',
                value: 'drugs'
            }];
            $scope.reviewedDataHeaders = {
                gene_type: ['Gene', 'Oncogene', 'Tumor Suppressor'],
                mutation_effect: ['Gene', 'Mutation', 'Oncogenic', 'Mutation Effect', 'Description', 'Citations'],
                tumor_summary: ['Gene', 'Mutation', 'Tumor Type', 'Tumor Summary'],
                drugs: ['Gene', 'Mutation', 'Tumor Type', 'Drugs', 'Level', 'Description', 'Citations']
            };
            var reviewedData = {
                gene_type: [],
                mutation_effect: [],
                tumor_summary: [],
                drugs: []
            };
            $scope.reviewedData = {
                gene_type: [],
                mutation_effect: [],
                tumor_summary: [],
                drugs: []
            };
            $scope.loadingReviewed = false;
            $scope.displayReviewedData = false;
            var subtypeMapping = {};
            function loopArray(arr, index) {
                if (index < arr.length) {
                    var drugs = [];
                    var item = arr[index];
                    if (item.treatments.length > 0) {
                        _.each(item.treatments, function (treatment) {
                            _.each(treatment.drugs, function (drug) {
                                drugs.push(drug.drugName);
                            });
                        });
                        if (item.subtype) {
                            reviewedData.drugs.push({
                                gene: item.gene.hugoSymbol, 
                                mutation: getAlterations(item.alterations), 
                                tumor_type: subtypeMapping[item.subtype], 
                                drugs: drugs.join(), 
                                level: item.levelOfEvidence,
                                description: item.description,
                                citations: getCitations(item.description)
                            });
                        } else {
                            reviewedData.drugs.push({
                                gene: item.gene.hugoSymbol, 
                                mutation: getAlterations(item.alterations), 
                                tumor_type: item.cancerType, 
                                drugs: drugs.join(), 
                                level: item.levelOfEvidence,
                                description: item.description,
                                citations: getCitations(item.description)
                            });
                        }
                    } 
                    loopArray(arr, ++index);
                } else {
                    $scope.reviewedData.drugs = reviewedData.drugs;
                    finishLoadingReviewedData();
                }
            }
            function finishLoadingReviewedData() {
                $scope.loadingReviewed = false;
                $scope.displayReviewedData = true;
            }
            $scope.updateReviewData = function() {
                $scope.displayReviewedData = false;
            }
            $scope.generateEvidences = function () {
                $scope.loadingReviewed = true;
                reviewedData = {
                    gene_type: [],
                    mutation_effect: [],
                    tumor_summary: [],
                    drugs: []
                };
                $scope.reviewedData = {
                    gene_type: [],
                    mutation_effect: [],
                    tumor_summary: [],
                    drugs: []
                };
                if ($scope.evidenceType === 'gene_type') {
                    $http.get(OncoKB.config.publicApiLink + 'genes').then(function(response) {
                        _.each(response.data, function(item) {
                            reviewedData.gene_type.push({
                                gene: item.hugoSymbol,
                                oncogene: item.oncogene,
                                tsg: item.tsg
                            });
                        });
                        $scope.reviewedData.gene_type = reviewedData.gene_type;
                        finishLoadingReviewedData();
                    });
                } else if ($scope.evidenceType === 'mutation_effect') {
                    $http.get(OncoKB.config.publicApiLink + 'evidences/lookup?source=oncotree&evidenceTypes=MUTATION_EFFECT%2C%20ONCOGENIC').then(function (response) {
                        _.each(response.data, function (item) {
                            var flag = false;
                            for (var i = 0; i < reviewedData.mutation_effect.length; i++) {
                                var evidence = reviewedData.mutation_effect[i];
                                if (item.gene.hugoSymbol === evidence.gene && getAlterations(item.alterations) === evidence.mutation) {
                                    flag = true;
                                    insertEvidence(item, evidence);
                                    break;
                                }
                            }
                            if (flag === false) {
                                var newEvidence = {
                                    gene: item.gene.hugoSymbol,
                                    mutation: getAlterations(item.alterations)
                                };
                                insertEvidence(item, newEvidence);
                                reviewedData.mutation_effect.push(newEvidence);
                            }
                        });
                        $scope.reviewedData.mutation_effect = reviewedData.mutation_effect;
                        finishLoadingReviewedData();
                    });
                } else {
                    mainUtils.getOncoTreeMainTypes().then(function(result) {
                        _.each(result.tumorTypes, function(items) {
                            _.each(items, function(item) {
                                subtypeMapping[item.code] = item.name;
                            });
                        });
                        if ($scope.evidenceType === 'tumor_summary') {
                            $http.get(OncoKB.config.publicApiLink + 'evidences/lookup?source=oncotree&evidenceTypes=TUMOR_TYPE_SUMMARY').then(function (response) {
                                _.each(response.data, function (item) {
                                    if (item.subtype) {
                                        reviewedData.tumor_summary.push({
                                            gene: item.gene.hugoSymbol,
                                            mutation: getAlterations(item.alterations),
                                            tumor_type: subtypeMapping[item.subtype],
                                            tumor_summary: item.description
                                        });
                                    } else {
                                        reviewedData.tumor_summary.push({
                                            gene: item.gene.hugoSymbol,
                                            mutation: getAlterations(item.alterations),
                                            tumor_type: item.cancerType,
                                            tumor_summary: item.description
                                        });
                                    }
                                });
                                $scope.reviewedData.tumor_summary = reviewedData.tumor_summary;
                                finishLoadingReviewedData();
                            });
                        } else if ($scope.evidenceType === 'drugs') {
                            $http.get(OncoKB.config.publicApiLink + 'evidences/lookup?source=oncotree&evidenceTypes=STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY%2C%20STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE%2C%20INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY%2C%20INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE').then(function (drugResponse) {
                                loopArray(drugResponse.data, 0);
                            });
                        }
                    });
                } 
            }
            function getAlterations(alterations) {
                var result = [];
                _.each(alterations, function (item) {
                    result.push(item.alteration);
                });
                return result.join();
            }
            function getCitations(data) {
                var processedData = FindRegex.result(data);
                var PMIDs = [];
                var abstracts = [];
                _.each(processedData, function (item) {
                    if (item.type === 'pmid') {
                        PMIDs.push(item.id.toString());
                    } else if (item.type === 'abstract') {
                        abstracts.push(item.id);
                    }
                });
                var result = '';
                if (PMIDs.length > 0) {
                    result = PMIDs.join(', ');
                }
                if (abstracts.length > 0) {
                    result += abstracts.join(', ');
                }
                return result;
            }
            function insertEvidence(item, evidence) {
                if (item.evidenceType === 'MUTATION_EFFECT') {
                    evidence['mutation_effect'] = item.knownEffect;
                    evidence['description'] = item.description;
                    evidence['citations'] = getCitations(item.description);
                } else if (item.evidenceType === 'ONCOGENIC') {
                    evidence['oncogenic'] = item.knownEffect;
                }
            }
            $scope.downloadReviewedData = function() {
                var header = [];
                var content = [];
                var tempArr = [];
                var fileName = 'Reviewed.xls';
                if ($scope.evidenceType === 'gene_type') {
                    fileName = 'GeneType.xls';
                    header = ['Gene', 'Oncogene', 'Tumor Suppressor'];
                    content.push(header.join('\t'));
                    _.each(reviewedData.gene_type, function(item) {
                        tempArr = [item.gene, item.oncogene, item.tsg];
                        content.push(tempArr.join('\t'));
                    });  
                } else if ($scope.evidenceType === 'mutation_effect') {
                    fileName = 'MutationEffect.xls';
                    header = ['Gene', 'Mutation', 'Oncogenic', 'Mutation Effect', 'Description', 'Citations'];
                    content.push(header.join('\t'));
                    _.each(reviewedData.mutation_effect, function(item) {
                        tempArr = [item.gene, item.mutation, item.oncogenic, item.mutation_effect, item.description, item.citations];
                        content.push(tempArr.join('\t'));
                    });  
                } else if ($scope.evidenceType === 'tumor_summary') {
                    fileName = 'TumorSummary.xls';
                    header = ['Gene', 'Mutation', 'Tumor Type', 'Summary'];
                    content.push(header.join('\t'));
                    _.each(reviewedData.tumor_summary, function(item) {
                        tempArr = [item.gene, item.mutation, item.tumor_type, item.tumor_summary];
                        content.push(tempArr.join('\t'));
                    });  
                } else if ($scope.evidenceType === 'drugs') {
                    fileName = 'Drugs.xls';
                    header = ['Gene', 'Mutation', 'Tumor Type', 'Drugs', 'Level', 'Description', 'Citations'];
                    content.push(header.join('\t'));
                    _.each(reviewedData.drugs, function(item) {
                        tempArr = [item.gene, item.mutation, item.tumor_type, item.drugs, item.level, item.description, item.citations];
                        content.push(tempArr.join('\t'));
                    }); 
                }
                var blob = new Blob([content.join('\n')], {
                    type: 'text/plain;charset=utf-8;',
                });
                saveAs(blob, fileName);
            };
            $scope.getReviewButtonContent = function() {
                if ($scope.loadingReviewed) {
                    return 'Loading <i class="fa fa-spinner fa-spin"></i>';
                } else {
                    return 'Submit';
                }
            }
        }]);
