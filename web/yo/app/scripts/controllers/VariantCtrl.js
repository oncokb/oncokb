'use strict';

angular.module('oncokbApp')
    .controller('VariantCtrl', [
        '$scope',
        '$filter',
        '$location',
        '$timeout',
        '$rootScope',
        'dialogs',
        'DatabaseConnector',
        'reportGeneratorParseAnnotation',
        'GenerateReportDataService',
        'reportViewFactory',
        'DeepMerge',
        'x2js',
        'FindRegex',
        'OncoKB',
        'stringUtils',
        '_',
        function($scope, $filter, $location, $timeout, $rootScope, dialogs, DatabaseConnector, reportGeneratorParseAnnotation, ReportDataService, reportViewFactory, DeepMerge, x2js, FindRegex, OncoKB, stringUtils, _) {
            'use strict';

            function getUnique(data, attr) {
                var unique = [];

                if (angular.isArray(data)) {
                    data.forEach(function(e) {
                        if (unique.indexOf(e[attr]) === -1) {
                            unique.push(e[attr]);
                        }
                    });
                    return unique.sort();
                }
                return null;
            }

            function checkUrl() {
                $timeout(function() {
                    if ($location.url() !== $location.path()) {
                        var urlVars = $location.search();
                        if (urlVars.hasOwnProperty('hugoSymbol')) {
                            $scope.gene = $scope.genes[$filter('getIndexByObjectNameInArray')($scope.genes, urlVars.hugoSymbol || '')];
                        }
                        if (urlVars.hasOwnProperty('alteration')) {
                            // $scope.alteration = $scope.alterations[$filter('getIndexByObjectNameInArray')($scope.alterations, urlVars.alteration || '')];
                            $scope.alteration = stringUtils.trimMutationName(urlVars.alteration);
                        }
                        if (urlVars.hasOwnProperty('cancerType')) {
                            $scope.view.selectedCancerType = $scope.view.filteredCancerTypes[$filter('getIndexByObjectNameInArray')($scope.view.filteredCancerTypes, urlVars.cancerType, 'cancerType')];
                        }
                        if (urlVars.hasOwnProperty('subtype')) {
                            $scope.view.selectedSubtype = $scope.view.filteredCancerTypes[$filter('getIndexByObjectNameInArray')($scope.view.filteredSubtypes, urlVars.subtype, 'subtype')];
                        }
                        $scope.search();
                    }
                }, 200);
            }

            // Retru whether URL changed
            function changeUrl(params) {
                var location = $location.search();
                var same = true;

                if (Object.keys(location).length === Object.keys(params).length) {
                    for (var key in params) {
                        if (!location.hasOwnProperty(key)) {
                            same = false;
                            break;
                        } else if (location[key] !== params[key]) {
                            same = false;
                            break;
                        }
                    }
                } else {
                    same = false;
                }
                if (same) {
                    return false;
                }
                $location.search(params).replace();
                return true;
            }

            function searchAnnotationCallback(status, data) {
                var annotation = {};
                var params = {
                    geneName: '',
                    alteration: '',
                    cancerType: '',
                    subtype: '',
                    annotation: '',
                    relevantCancerType: ''
                };
                if (status === 'success') {
                    annotation = reportGeneratorParseAnnotation.parse(data);
                    $scope.annotation = annotation.annotation;

                    params.geneName = $scope.gene;
                    params.alteration = $scope.alteration;
                    params.tumorType = $scope.view.selectedCancerType ? $scope.view.selectedCancerType.cancerType : '';// ReportDataService accepts tumorType for now
                    params.subtype = $scope.view.selectedSubtype ? $scope.view.selectedSubtype.subtype : '';
                    params.annotation = annotation.annotation;
                    params.relevantCancerType = annotation.relevantCancerType;

                    $scope.reportParams.reportContent = ReportDataService.init([params]);
                    $scope.reportParams.requestInfo = {
                        email: $rootScope.user.email,
                        folderName: '',
                        fileName: params.geneName + '_' + params.alteration + '_' + params.cancerType + '_' + params.subtype,
                        userName: $rootScope.user.name
                    };

                    var reportViewParams = {};
                    for (var key in $scope.reportParams.reportContent) {
                        if (key === 'items') {
                            _.each($scope.reportParams.reportContent.items[0], function(item, key1) {
                                reportViewParams[key1] = item;
                            });
                        } else {
                            reportViewParams[key] = $scope.reportParams.reportContent[key];
                        }
                    }
                    $scope.reportViewData = reportViewFactory.getData(reportViewParams);
                }
                $scope.rendering = false;
            }

            function generating() {
                $timeout(function() {
                    if ($scope.generaingReport) {
                        generating();
                    } else {
                        $rootScope.$broadcast('dialogs.wait.complete');
                        dialogs.notify('Request finished', 'The request has been added. Your will received an email when your report(s) is ready. Thank you.');
                    }
                }, 500);
            }

            function separateTumorTypes(tumorTypes) {
                var subtypes = {};
                var cancerTypes = {};
                _.each(tumorTypes, function(tumorType) {
                    if (tumorType) {
                        if (tumorType.subtype && !subtypes.subtype) {
                            subtypes[tumorType.subtype] = tumorType;
                        }
                        if (tumorType.cancerType && !cancerTypes.cancerType) {
                            cancerTypes[tumorType.cancerType] = tumorType;
                        }
                    }
                });
                return {
                    subtypes: _.values(subtypes),
                    cancerTypes: _.values(cancerTypes)
                };
            }

            function findCancerType(cancerType) {
                for (var i = 0; i < $scope.view.filteredCancerTypes.length; i++) {
                    if ($scope.view.filteredCancerTypes[i].cancerType === cancerType) {
                        return $scope.view.filteredCancerTypes[i];
                    }
                }
            }

            function findSubtypesByCancerType(cancerType) {
                var subtypes = [];
                for (var i = 0; i < $scope.subtypes.length; i++) {
                    if ($scope.subtypes[i].cancerType === cancerType) {
                        subtypes.push($scope.subtypes[i]);
                    }
                }
                return subtypes;
            }

            function containSubtype(subtype, subtypes) {
                for (var i = 0; i < subtypes.length; i++) {
                    if (subtypes[i].subtype === subtype.subtype) {
                        return true;
                    }
                }
                return false;
            }

            $scope.init = function() {
                $scope.view = {};
                $scope.reportParams = {
                    reportContent: {},
                    requestInfo: {}
                };

                $scope.rendering = false;

                $scope.loadingPage = true;

                $scope.generaingReport = false;

                // Control UI-Bootstrap angularjs, open one at a time
                $scope.oneAtATime = true;

                $scope.isCollapsed = {};

                $scope.displayParts = {
                    annotationSummary: {
                        displayName: 'Summary',
                        objectName: 'annotation_summary'
                    },
                    geneAnnotation: {
                        displayName: 'Gene Annotation',
                        objectName: 'gene_annotation'
                    },
                    variantEffect: {
                        displayName: 'Variant Effect',
                        objectName: 'variant_effect'
                    }
                };

                $scope.summaryTableTitles = [
                    'Treatment Implications',
                    'FDA Approved Drugs in Tumor Type',
                    'FDA Approved Drugs in Other Tumor Type',
                    'Clinical Trials',
                    'Additional Information'
                ];

                $scope.reportMatchedParams = [
                    'treatment',
                    'fdaApprovedInTumor',
                    'fdaApprovedInOtherTumor',
                    'clinicalTrials',
                    'additionalInfo'
                ];

                $scope.summaryTableTitlesContent = {
                    'Treatment Implications': [
                        'nccn_guidelines',
                        'standard_therapeutic_implications'],
                    'Clinical Trials': ['clinical_trial', 'investigational_therapeutic_implications'],
                    'Additional Information': ['prevalence', 'prognostic_implications'],
                    'FDA Approved Drugs in Tumor Type': [],
                    'FDA Approved Drugs in Other Tumor Type': []
                };

                /* eslint camelcase: ["error", {properties: "never"}]*/
                $scope.consequences = {
                    feature_truncation: 'Truncation',
                    frameshift_variant: 'Frame shift',
                    inframe_deletion: 'In frame deletion',
                    inframe_insertion: 'In frame insertion',
                    initiator_codon_variant: 'Initiator codon',
                    missense_variant: 'Missense',
                    stop_gained: 'Stop gained',
                    synonymous_variant: 'Synonymous'
                };

                $scope.specialAttr = ['investigational_therapeutic_implications', 'standard_therapeutic_implications'];

                if (OncoKB.global.genes && OncoKB.global.genes && OncoKB.global.tumorTypes) {
                    var separatedTumorTypes = separateTumorTypes(OncoKB.global.tumorTypes);

                    $scope.genes = getUnique(angular.copy(OncoKB.global.genes), 'hugoSymbol');
                    $scope.cancerTypes = separatedTumorTypes.cancerTypes;
                    $scope.subtypes = separatedTumorTypes.subtypes;
                    $scope.view.filteredCancerTypes = angular.copy($scope.cancerTypes);
                    $scope.view.filteredSubtypes = angular.copy($scope.subtypes);
                    $scope.loadingPage = false;
                } else {
                    DatabaseConnector.getGeneTumorType(function(data) {
                        OncoKB.global.genes = angular.copy(data.genes);
                        OncoKB.global.tumorTypes = angular.copy(data.tumorTypes);

                        var separatedTumorTypes = separateTumorTypes(OncoKB.global.tumorTypes);

                        $scope.genes = getUnique(data.genes, 'hugoSymbol');
                        $scope.cancerTypes = separatedTumorTypes.cancerTypes;
                        $scope.subtypes = separatedTumorTypes.subtypes;
                        $scope.view.filteredCancerTypes = angular.copy($scope.cancerTypes);
                        $scope.view.filteredSubtypes = angular.copy($scope.subtypes);
                        $scope.loadingPage = false;
                        checkUrl();
                    });
                }
            };

            $scope.isSearchable = function() {
                if ($scope.gene && $scope.alteration) {
                    return true;
                }
                return false;
            };

            $scope.fdaApproved = function(drug) {
                if (typeof drug.fda_approved === 'string' && drug.fda_approved.toLowerCase() === 'yes') {
                    return true;
                }
                return false;
            };

            $scope.hasSelectedCancerType = function() {
                if ($scope.view.hasOwnProperty('selectedCancerType') && $scope.view.selectedCancerType) {
                    return true;
                }
                return false;
            };

            $scope.setCollapsed = function(trial, attr) {
                $scope.isCollapsed[trial.trial_id][attr] = !$scope.isCollapsed[trial.trial_id][attr];
            };

            $scope.isArray = function(_var) {
                if (_var instanceof Array) {
                    return true;
                }
                return false;
            };

            $scope.isObject = function(_var) {
                if (typeof _var === 'object') {
                    return true;
                }
                return false;
            };

            $scope.displayProcess = function(str) {
                var specialUpperCasesWords = ['NCCN'];
                var specialLowerCasesWords = ['of', 'for'];

                str = str.replace(/_/g, ' ');
                str = str.replace(
                    /\w\S*/g,
                    function(txt) {
                        var _upperCase = txt.toUpperCase();
                        var _lowerCase = txt.toLowerCase();

                        if (specialUpperCasesWords.indexOf(_upperCase) !== -1) {
                            return _upperCase;
                        }

                        if (specialLowerCasesWords.indexOf(_lowerCase) !== -1) {
                            return _lowerCase;
                        }

                        return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
                    }
                );
                return str;
            };

            $scope.search = function() {
                var hasSelectedCancerType = $scope.hasSelectedCancerType();
                $scope.rendering = true;
                $scope.reportViewActive = hasSelectedCancerType;
                $scope.regularViewActive = !hasSelectedCancerType;
                $scope.alteration = stringUtils.trimMutationName($scope.alteration);
                var params = {alterationType: 'MUTATION'};
                var paramsContent = {
                    hugoSymbol: $scope.gene || '',
                    alteration: $scope.alteration || ''
                };

                for (var key in paramsContent) {
                    if (paramsContent[key] !== '') {
                        params[key] = paramsContent[key];
                    }
                }
                if (hasSelectedCancerType) {
                    params.cancerType = $scope.view.selectedCancerType.cancerType;
                    if ($scope.view.selectedSubtype && $scope.view.selectedSubtype.subtype) {
                        params.tumorType = $scope.view.selectedSubtype.subtype;
                    } else {
                        params.tumorType = params.cancerType;
                    }
                } else {
                    params.cancerType = '';
                    params.subtype = '';
                    params.tumorType = '';
                }
                if ($scope.hasOwnProperty('selectedConsequence') && $scope.selectedConsequence) {
                    params.consequence = $scope.selectedConsequence;
                }

                changeUrl(params);

                DatabaseConnector.searchAnnotation(params, function(data) {
                    searchAnnotationCallback('success', data);
                }, function() {
                    searchAnnotationCallback('fail');
                });
            };

            $scope.generateReport = function() {
                var dlg = dialogs.create('views/emailDialog.html', 'emailDialogCtrl', 'Test string');
                dlg.result.then(function(data) {
                    if (typeof data !== 'undefined' && data && data !== '') {
                        $scope.generaingReport = true;
                        dlg = dialogs.wait('Sending request', 'Please wait...');
                        generating();
                        var params = $scope.reportParams;
                        params.requestInfo.email = data;
                        DatabaseConnector.googleDoc(params, function() {
                            $scope.generaingReport = false;
                        }, function() {
                            $scope.generaingReport = false;
                        });
                    }
                }, function() {
                    console.log('Did not do anything.');
                });
            };

            $scope.useExample = function() {
                $scope.gene = $scope.genes[$filter('getIndexByObjectNameInArray')($scope.genes, 'BRAF')];
                // $scope.alteration = $scope.alterations[$filter('getIndexByObjectNameInArray')($scope.alterations, 'V600E')];
                $scope.alteration = 'V600E';
                $scope.view.selectedCancerType = $scope.view.filteredCancerTypes[$filter('getIndexByObjectNameInArray')($scope.view.filteredCancerTypes, 'Melanoma', 'cancerType')];
                $scope.view.selectedSubtype = '';
                $scope.selectedConsequence = '';
                $scope.search();
            };

            $scope.$watch('view.selectedSubtype', function(n) {
                if (n) {
                    $scope.view.selectedCancerType = findCancerType(n.cancerType);
                } else {
                    // console.log($scope.view.selectedSubtype);
                }
            });

            $scope.$watch('view.selectedCancerType', function(n) {
                if (n) {
                    if ($scope.view.selectedSubtype) {
                        var mappedSubtypes = findSubtypesByCancerType(n.cancerType);
                        if (!containSubtype($scope.view.selectedSubtype, mappedSubtypes)) {
                            $scope.view.filteredSubtypes = mappedSubtypes;
                            $scope.view.selectedSubtype = '';
                        }
                    } else {
                        $scope.view.filteredSubtypes = findSubtypesByCancerType(n.cancerType);
                    }
                } else {
                    $scope.view.filteredSubtypes = angular.copy($scope.subtypes);
                    $scope.view.selectedSubtype = '';
                }
            });
        }]);
