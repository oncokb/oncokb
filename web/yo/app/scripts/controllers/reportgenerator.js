'use strict';

angular.module('oncokbApp')
    .controller('ReportgeneratorCtrl', ['$scope', 'FileUploader', 'dialogs', 'storage', 'documents', 'OncoKB', 'DatabaseConnector', 'stringUtils', '$timeout', '_', 'FindRegex', 'mainUtils', 
        function($scope, FileUploader, dialogs, storage, Documents, OncoKB, DatabaseConnector, stringUtils, $timeout, _, FindRegex, mainUtils) {
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
                value: 'geneType'
            }, {
                label: 'Mutation Effect',
                value: 'mutationEffect'
            }, {
                label: 'Tumor Summary',
                value: 'tumorSummary'
            }, {
                label: 'Drugs',
                value: 'drugs'
            }];
            $scope.reviewedData = {
                geneType: {
                    header: ['Gene', 'Oncogene', 'Tumor Suppressor'],
                    body: [],
                    keys: ['gene', 'oncogene', 'tsg'],
                    fileName: 'GeneType.xls',
                    evidenceTypes: 'geneType'
                },
                mutationEffect: {
                    header:['Gene', 'Mutation', 'Oncogenic', 'Mutation Effect', 'Description', 'Citations'],
                    body: [],
                    keys: ['gene', 'mutation', 'oncogenic', 'mutationEffect', 'description', 'citations'],
                    fileName: 'MutationEffect.xls',
                    evidenceTypes: 'MUTATION_EFFECT,ONCOGENIC'
                },
                tumorSummary: {
                    header: ['Gene', 'Mutation', 'Tumor Type', 'Tumor Summary'],
                    body: [],
                    keys: ['gene', 'mutation', 'tumorType', 'tumorSummary'],
                    fileName: 'TumorSummary.xls',
                    evidenceTypes: 'TUMOR_TYPE_SUMMARY'
                },
                drugs: {
                    header: ['Gene', 'Mutation', 'Tumor Type', 'Drugs', 'Level', 'Description', 'Citations'],
                    body: [],
                    keys: ['gene', 'mutation', 'tumorType', 'drugs', 'level', 'description', 'citations'],
                    fileName: 'Drugs.xls',
                    evidenceTypes: 'STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY,STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE,INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY,INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE'
                }
            };
            $scope.loadingReviewed = false;
            $scope.displayReviewedData = false;
            var subtypeMapping = {};
            function finishLoadingReviewedData() {
                $scope.loadingReviewed = false;
                $scope.displayReviewedData = true;
            }
            $scope.updateReviewData = function() {
                $scope.displayReviewedData = false;
            }
            $scope.generateEvidences = function () {
                $scope.loadingReviewed = true;
                $scope.reviewedData.geneType.body = [];
                $scope.reviewedData.mutationEffect.body = [];
                $scope.reviewedData.tumorSummary.body = [];
                $scope.reviewedData.drugs.body = [];

                DatabaseConnector.getReviewedData($scope.reviewedData[$scope.evidenceType].evidenceTypes).then(function(response) {
                    if ($scope.evidenceType === 'geneType') {
                        _.each(response, function(item) {
                            $scope.reviewedData.geneType.body.push({
                                gene: item.hugoSymbol,
                                oncogene: item.oncogene,
                                tsg: item.tsg
                            });
                        });
                        finishLoadingReviewedData();
                    } else if ($scope.evidenceType === 'mutationEffect') {
                        _.each(response, function (item) {
                            var flag = false;
                            for (var i = 0; i < $scope.reviewedData.mutationEffect.body.length; i++) {
                                var evidence = $scope.reviewedData.mutationEffect.body[i];
                                if (item.gene.hugoSymbol === evidence.gene && getAlterations(item.alterations) === evidence.mutation) {
                                    flag = true;
                                    constructMEObj(item, evidence);
                                    break;
                                }
                            }
                            if (flag === false) {
                                var newEvidence = {
                                    gene: item.gene.hugoSymbol,
                                    mutation: getAlterations(item.alterations)
                                };
                                constructMEObj(item, newEvidence);
                                $scope.reviewedData.mutationEffect.body.push(newEvidence);
                            }
                        });
                        finishLoadingReviewedData();
                    } else {
                        mainUtils.getOncoTreeMainTypes().then(function(result) {
                            _.each(result.tumorTypes, function(items) {
                                _.each(items, function(item) {
                                    subtypeMapping[item.code] = item.name;
                                });
                            });
                            if ($scope.evidenceType === 'tumorSummary') {
                                _.each(response, function (item) {
                                    if (item.subtype) {
                                        $scope.reviewedData.tumorSummary.body.push({
                                            gene: item.gene.hugoSymbol,
                                            mutation: getAlterations(item.alterations),
                                            tumorType: subtypeMapping[item.subtype],
                                            tumorSummary: item.description
                                        });
                                    } else {
                                        $scope.reviewedData.tumorSummary.body.push({
                                            gene: item.gene.hugoSymbol,
                                            mutation: getAlterations(item.alterations),
                                            tumorType: item.cancerType,
                                            tumorSummary: item.description
                                        });
                                    }
                                });
                            } else if ($scope.evidenceType === 'drugs') {
                                _.each(response, function(item) {
                                    var drugs = [];
                                    if (item.treatments.length > 0) {
                                        _.each(item.treatments, function (treatment) {
                                            _.each(treatment.drugs, function (drug) {
                                                drugs.push(drug.drugName);
                                            });
                                        });
                                        var tempObj = {
                                            gene: item.gene.hugoSymbol, 
                                            mutation: getAlterations(item.alterations), 
                                            drugs: drugs.join(), 
                                            level: item.levelOfEvidence,
                                            description: item.description,
                                            citations: getCitations(item.description)
                                        };
                                        if (item.subtype) {
                                            tempObj.tumorType = subtypeMapping[item.subtype];
                                        } else {
                                            tempObj.tumorType = item.cancerType;
                                        }
                                        $scope.reviewedData.drugs.body.push(tempObj);
                                    } 
                                });
                            }
                            finishLoadingReviewedData();
                        });
                    }
                });
                 
            }
            function getAlterations(alterations) {
                var result = [];
                _.each(alterations, function (item) {
                    result.push(item.alteration);
                });
                return result.join(', ');
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
            function constructMEObj(item, evidence) {
                if (item.evidenceType === 'MUTATION_EFFECT') {
                    evidence['mutationEffect'] = item.knownEffect;
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
                if ($scope.evidenceType && $scope.reviewedData[$scope.evidenceType]) {
                    content.push($scope.reviewedData[$scope.evidenceType].header.join('\t'));
                    fileName = $scope.reviewedData[$scope.evidenceType].fileName;
                }
                _.each($scope.reviewedData[$scope.evidenceType].body, function(item) {
                    tempArr = [];
                    _.each($scope.reviewedData[$scope.evidenceType].keys, function(key) {
                        tempArr.push(item[key]);
                    });
                    content.push(tempArr.join('\t'));
                });  
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
