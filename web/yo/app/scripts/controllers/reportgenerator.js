'use strict';

angular.module('oncokbApp')
    .controller('ReportgeneratorCtrl', ['$scope', 'dialogs', 'storage', 'documents', 'OncoKB', 'DatabaseConnector', 'stringUtils', '$timeout', '_', 'FindRegex', 'mainUtils',
        function($scope, dialogs, storage, Documents, OncoKB, DatabaseConnector, stringUtils, $timeout, _, FindRegex, mainUtils) {
            $scope.init = function() {
                $scope.loading = false;
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
            $scope.getHistoryButtonContent = function() {
                if ($scope.loading) {
                    return 'Loading <i class="fa fa-spinner fa-spin"></i>';
                } else {
                    return 'Submit';
                }
            }

            $scope.reviewedDT = {};
            $scope.reviewedDT.dtOptions = {
                paging: true,
                hasBootstrap: true,
                scrollY: 500,
                scrollCollapse: true
            };
            $scope.evidenceType = '';
            $scope.evidenceTypes = [{
                label: 'Oncogene/Tumor Suppressor',
                value: 'geneType'
            }, {
                label: 'Mutation Effect',
                value: 'mutationEffect'
            }, {
                label: 'Tumor Type Summary',
                value: 'tumorSummary'
            }, {
                label: 'Therapeutics (All Levels)',
                value: 'drugs'
            }];
            $scope.reviewedData = {
                geneType: {
                    header: ['Gene', 'Oncogene', 'Tumor Suppressor', 'Truncating Mutations', 'Deletion', 'Amplification'],
                    body: [],
                    keys: ['gene', 'oncogene', 'tsg', 'truncatingMutations', 'deletion', 'amplification'],
                    fileName: 'Onc/TS.xls',
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
                    fileName: 'TumorTypeSummary.xls',
                    evidenceTypes: 'TUMOR_TYPE_SUMMARY'
                },
                drugs: {
                    header: ['Gene', 'Mutation', 'Tumor Type', 'Drugs', 'Level', 'Description', 'Citations'],
                    body: [],
                    keys: ['gene', 'mutation', 'tumorType', 'drugs', 'level', 'description', 'citations'],
                    fileName: 'Therapeutics.xls',
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
                        var variantLookupBody = _.map(response, function(item) {
                            return {
                                hugoSymbol: item.hugoSymbol
                            };
                        });
                        var geneWithVariants = {};
                        DatabaseConnector.lookupVariants(variantLookupBody).then(function(result) {
                            _.each(result, function(items) {
                                var tempObj = {};
                                _.each(items, function(item) {
                                    if (_.isEmpty(tempObj)) {
                                        tempObj = {
                                            gene: item.gene.hugoSymbol,
                                            oncogene: item.gene.oncogene,
                                            tsg: item.gene.tsg,
                                            truncatingMutations: false,
                                            deletion: false,
                                            amplification: false
                                        };
                                        geneWithVariants[item.gene.hugoSymbol] = true;
                                    }
                                    if (item.alteration === 'Truncating Mutations') {
                                        tempObj.truncatingMutations = true;
                                    }
                                    if (item.alteration === 'Deletion') {
                                        tempObj.deletion = true;
                                    }
                                    if (item.alteration === 'Amplification') {
                                        tempObj.amplification = true;
                                    }
                                });
                                if (!_.isEmpty(tempObj)) {
                                    $scope.reviewedData.geneType.body.push(tempObj);
                                }
                            });
                            _.each(response, function(item) {
                                if (geneWithVariants[item.hugoSymbol] !== true) {
                                    $scope.reviewedData.geneType.body.push({
                                        gene: item.hugoSymbol,
                                        oncogene: item.oncogene,
                                        tsg: item.tsg,
                                        truncatingMutations: false,
                                        deletion: false,
                                        amplification: false
                                    });
                                }
                            });
                            finishLoadingReviewedData();
                        });
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
                                    var tempObj =  {
                                        gene: item.gene.hugoSymbol,
                                        mutation: getAlterations(item.alterations),
                                        tumorSummary: item.description
                                    };
                                    if (item.subtype) {
                                        tempObj.tumorType = subtypeMapping[item.subtype];
                                    } else {
                                        tempObj.tumorType = item.cancerType;
                                    }
                                    $scope.reviewedData.tumorSummary.body.push(tempObj);
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

            $scope.validation = {
                flag: false,
                result: '',
                validating: false
            };
            $scope.reverseValidation = {
                flag: false,
                result: '',
                validating: false
            };
            $scope.validateTruncating = function(type) {
                if (type === 'validate') {
                    $scope.validation = {
                        flag: false,
                        result: '',
                        validating: true
                    };
                } else if (type === 'reverseValidate') {
                    $scope.reverseValidation = {
                        flag: false,
                        result: '',
                        validating: true
                    };
                }
                DatabaseConnector.getReviewedData('geneType').then(function(response) {
                    var geneTypes = {};
                    var tempHugo = '';
                    var variantCallBody = [];
                    _.each(response, function(item) {
                        geneTypes[item.hugoSymbol] = {
                            oncogene: item.oncogene,
                            tsg: item.tsg
                        };
                        variantCallBody.push({
                            hugoSymbol: item.hugoSymbol
                        });
                    });
                    DatabaseConnector.lookupVariants(variantCallBody).then(function(result) {
                        if (type === 'validate') {
                            var validationResult = [];
                            _.each(result, function(alterations) {
                                _.each(alterations, function(alteration) {
                                    if (alteration.alteration === 'Truncating Mutations') {
                                        tempHugo = alteration.gene.hugoSymbol;
                                        if (geneTypes[tempHugo] && geneTypes[tempHugo].tsg === false && geneTypes[tempHugo].oncogene === true) {
                                            validationResult.push(tempHugo);
                                        }
                                    }
                                });
                            });
                            if (validationResult.length === 0) {
                                $scope.validation.result = 'Yes! All genes passed the validation.';
                                $scope.validation.flag = true;
                            } else {
                                $scope.validation.result = 'Genes that having Truncating Mutation curated but only marked as Oncogenes: ' + validationResult.join(', ');
                                $scope.validation.flag = false;
                            }
                            $scope.validation.validating = false;
                        } else if (type === 'reverseValidate') {
                            var reverseValidationResult = [];
                            // Add a validation to find tumor suppressor genes that have no truncating mutations curated
                            _.each(result, function(alterations) {
                                if (alterations.length > 0) {
                                    tempHugo = alterations[0].gene.hugoSymbol;
                                    if (geneTypes[tempHugo] && geneTypes[tempHugo].tsg === true) {
                                        var isTruncating = false;
                                        _.each(alterations, function(alteration) {
                                            if (alteration.alteration === 'Truncating Mutations') {
                                                isTruncating = true;
                                            }
                                        });
                                        if(!isTruncating){
                                            reverseValidationResult.push(tempHugo);
                                        }
                                    }
                                }
                            });
                            if (reverseValidationResult.length === 0) {
                                $scope.reverseValidation.result = 'Yes! All genes passed the validation.';
                                $scope.reverseValidation.flag = true;
                            } else {
                                $scope.reverseValidation.result = 'Tumor suppressor genes that have no ' +
                                    'Truncating Mutations curated are: ' + reverseValidationResult.join(', ');
                                $scope.reverseValidation.flag = false;
                            }
                            $scope.reverseValidation.validating = false;
                        }

                    });
                });
            }
            $scope.getValidationButtonContent = function(type) {
                if ($scope.validation.validating && type === 'validate') {
                    return 'Validating <i class="fa fa-spinner fa-spin"></i>';
                } else if ($scope.reverseValidation.validating && type === 'reverseValidate') {
                    return 'Validating <i class="fa fa-spinner fa-spin"></i>';
                } else {
                    return 'Validate';
                }
            }
        }]);
