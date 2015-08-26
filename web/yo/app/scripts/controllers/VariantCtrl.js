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
        function ($scope, $filter, $location, $timeout, $rootScope, dialogs, DatabaseConnector, reportGeneratorParseAnnotation, ReportDataService, reportViewFactory, DeepMerge, x2js, FindRegex, OncoKB, stringUtils) {

            'use strict';

            function getUnique(data, attr) {
                var unique = [];

                if(angular.isArray(data)){
                    data.forEach(function(e) {
                        if(unique.indexOf(e[attr]) === -1) {
                            unique.push(e[attr]);
                        }
                    });
                    return unique.sort();
                }else {
                    return null;
                }
            }

            function checkUrl() {
                $timeout(function(){
                    if($location.url() !== $location.path()) {
                        var urlVars = $location.search();
                        if(urlVars.hasOwnProperty('hugoSymbol')){
                            $scope.gene = $scope.genes[$filter('getIndexByObjectNameInArray')($scope.genes, urlVars.hugoSymbol || '')];
                        }
                        if(urlVars.hasOwnProperty('alteration')){
                            // $scope.alteration = $scope.alterations[$filter('getIndexByObjectNameInArray')($scope.alterations, urlVars.alteration || '')];
                            $scope.alteration = stringUtils.trimMutationName(urlVars.alteration);
                        }
                        if(urlVars.hasOwnProperty('tumorType')){
                            $scope.selectedTumorType = $scope.tumorTypes[$filter('getIndexByObjectNameInArray')($scope.tumorTypes, urlVars.tumorType || '')];
                        }
                        $scope.search();
                    }
                }, 200);
            }

            //Retru whether URL changed
            function changeUrl(params) {
                var location = $location.search(),
                    same = true;


                if(Object.keys(location).length !== Object.keys(params).length){
                    same = false;
                }else{
                    for(var key in params) {
                        if(!location.hasOwnProperty(key)) {
                            same = false;
                            break;
                        }else{
                            if(location[key] !== params[key]) {
                                same = false;
                                break;
                            }
                        }
                    }
                }
                if(!same) {
                    $location.search(params).replace();
                    return true;
                }else {
                    return false;
                }
            }

            function searchAnnotationCallback(status, data) {
                var annotation = {};
                var params = {
                    'geneName': '',
                    'alteration': '',
                    'tumorType': '',
                    'annotation': '',
                    'relevantCancerType': ''};
                if(status === 'success') {
                    annotation = reportGeneratorParseAnnotation.parse(data);
                    $scope.annotation = annotation.annotation;

                    params.geneName = $scope.gene;
                    params.alteration = $scope.alteration;
                    params.tumorType = $scope.selectedTumorType;
                    params.annotation = annotation.annotation;
                    params.relevantCancerType = annotation.relevantCancerType;

                    $scope.reportParams.reportContent = ReportDataService.init([params]);
                    $scope.reportParams.requestInfo = {
                        email: $rootScope.user.email,
                        folderName: '',
                        fileName: params.geneName + '_' + params.alteration + '_' + params.tumorType,
                        userName: $rootScope.user.name
                    };

                    var reportViewParams = {};
                    for(var key in $scope.reportParams.reportContent) {
                        if(key !== 'items') {
                            reportViewParams[key] = $scope.reportParams.reportContent[key];
                        }else{
                            for(var key1 in $scope.reportParams.reportContent.items[0]){
                                reportViewParams[key1] = $scope.reportParams.reportContent.items[0][key1];
                            }
                        }
                    }
                    $scope.reportViewData = reportViewFactory.getData(reportViewParams);
                }
                $scope.rendering = false;
            }

            function generating() {
                $timeout(function(){
                    if($scope.generaingReport){
                        generating();
                    }else{
                        $rootScope.$broadcast('dialogs.wait.complete');
                        dialogs.notify('Request finished', 'The request has been added. Your will received an email when your report(s) is ready. Thank you.');
                    }
                },500);
            }

            $scope.init = function () {

                $scope.rendering = false;

                $scope.loadingPage = true;

                $scope.generaingReport =false;

                //Control UI-Bootstrap angularjs, open one at a time
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

                $scope.consequences = {
                    'feature_truncation': 'Truncation',
                    'frameshift_variant': 'Frame shift',
                    'inframe_deletion': 'In frame deletion',
                    'inframe_insertion': 'In frame insertion',
                    'initiator_codon_variant': 'Initiator codon',
                    'missense_variant': 'Missense',
                    'stop_gained': 'Stop gained',
                    'synonymous_variant': 'Synonymous'
                };

                $scope.specialAttr = ['investigational_therapeutic_implications', 'standard_therapeutic_implications'];

                if(OncoKB.global.genes && OncoKB.global.genes && OncoKB.global.tumorTypes) {
                    $scope.genes = getUnique(angular.copy(OncoKB.global.genes), 'hugoSymbol');
                    // $scope.alterations = getUnique(angular.copy(OncoKB.global.alterations), 'name');
                    $scope.tumorTypes = getUnique(angular.copy(OncoKB.global.tumorTypes), 'name');
                    $scope.loadingPage = false;
                }else {
                    DatabaseConnector.getGeneAlterationTumortype(function(data){
                        OncoKB.global.genes = angular.copy(data.genes);
                        OncoKB.global.alterations = angular.copy(data.alterations);
                        OncoKB.global.tumorTypes = angular.copy(data.tumorTypes);

                        $scope.genes = getUnique(data.genes, 'hugoSymbol');
                        // $scope.alterations = getUnique(data.alterations, 'name');
                        $scope.tumorTypes = getUnique(data.tumorTypes, 'name');
                        $scope.loadingPage = false;
                        checkUrl();
                    });
                }

                $scope.reportParams = {
                    reportContent: {},
                    requestInfo: {}
                };
            };

            $scope.isSearchable = function() {
                if($scope.gene && $scope.alteration) {
                    return true;
                }else {
                    return false;
                }
            };

            $scope.fdaApproved = function(drug) {
                if (typeof drug.fda_approved === 'string' && drug.fda_approved.toLowerCase() === 'yes'){
                    return true;
                }else{
                    return false;
                }
            };

            $scope.hasSelectedTumorType = function() {
                if($scope.hasOwnProperty('selectedTumorType') && $scope.selectedTumorType) {
                    return true;
                }else {
                    return false;
                }
            };

            $scope.setCollapsed = function(trial, attr) {
                $scope.isCollapsed[trial.trial_id][attr] = !$scope.isCollapsed[trial.trial_id][attr];
            };

            $scope.isArray = function(_var) {
                if(_var instanceof Array) {
                    return true;
                }else {
                    return false;
                }
            };

            $scope.isObject = function(_var) {
                if(typeof _var === 'object') {
                    return true;
                }else {
                    return false;
                }
            };

            $scope.displayProcess = function(str) {
                var specialUpperCasesWords = ['NCCN'];
                var specialLowerCasesWords = ['of', 'for'];

                str = str.replace(/_/g, ' ');
                str = str.replace(
                    /\w\S*/g,
                    function(txt) {
                        var _upperCase = txt.toUpperCase(),
                            _lowerCase = txt.toLowerCase();

                        if( specialUpperCasesWords.indexOf(_upperCase) !== -1 ) {
                            return _upperCase;
                        }

                        if( specialLowerCasesWords.indexOf(_lowerCase) !== -1 ) {
                            return _lowerCase;
                        }

                        return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
                    }
                );
                return str;
            };

            $scope.search = function() {
                var hasSelectedTumorType = $scope.hasSelectedTumorType();
                $scope.rendering = true;
                $scope.reportViewActive = hasSelectedTumorType;
                $scope.regularViewActive = !hasSelectedTumorType;
                $scope.alteration = stringUtils.trimMutationName($scope.alteration);
                var params = {'alterationType': 'MUTATION'};
                var paramsContent = {
                    'hugoSymbol': $scope.gene || '',
                    'alteration': $scope.alteration || ''
                };

                for (var key in paramsContent) {
                    if(paramsContent[key] !== '') {
                        params[key] = paramsContent[key];
                    }
                }
                if(hasSelectedTumorType) {
                    params.tumorType = $scope.selectedTumorType;
                }else{
                    params.tumorType = '';
                }
                if($scope.hasOwnProperty('selectedConsequence') && $scope.selectedConsequence) {
                    params.consequence = $scope.selectedConsequence;
                }

                changeUrl(params);

                DatabaseConnector.searchAnnotation(params, function(data) {
                    searchAnnotationCallback('success', data);
                }, function(){
                    searchAnnotationCallback('fail');
                });
            };

            $scope.generateReport = function() {
                var dlg = dialogs.create('views/emailDialog.html','emailDialogCtrl', 'Test string');
                dlg.result.then(function(data){
                    if(typeof data !== 'undefined' && data && data !== '') {
                        $scope.generaingReport =true;
                        dlg = dialogs.wait('Sending request','Please wait...');
                        generating();
                        var params = $scope.reportParams;
                        params.requestInfo.email = data;
                        DatabaseConnector.googleDoc(params, function() {
                            $scope.generaingReport =false;
                        }, function() {
                            $scope.generaingReport =false;
                        });
                    }
                },function(){
                    console.log('Did not do anything.');
                });
            };

            $scope.useExample = function() {
                $scope.gene= $scope.genes[$filter('getIndexByObjectNameInArray')($scope.genes, 'BRAF')];
                // $scope.alteration = $scope.alterations[$filter('getIndexByObjectNameInArray')($scope.alterations, 'V600E')];
                $scope.alteration = 'V600E';
                $scope.selectedTumorType = $scope.tumorTypes[$filter('getIndexByObjectNameInArray')($scope.tumorTypes, 'melanoma')];
                $scope.selectedConsequence = '';
                $scope.search();
            };
        }]);