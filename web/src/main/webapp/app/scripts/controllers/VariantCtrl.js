angular.module('webappApp')
    .controller('VariantCtrl', [
        '$scope',
        '$filter',
        '$location',
        '$timeout',
        '$rootScope',
        'dialogs',
        'DatabaseConnector',
        'GenerateReportDataService',
        'DeepMerge',
        'x2js',
        function ($scope, $filter, $location, $timeout, $rootScope, dialogs, DatabaseConnector, ReportDataService, DeepMerge, x2js) {

        'use strict';

        var changedAttr = ['cancer_type', 'nccn_guidelines', 'clinical_trial', 'sensitive_to', 'resistant_to', 'treatment', 'drug'];
        
        $scope.init = function () {

            $scope.rendering = false;
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

            $scope.specialAttr = ['investigational_therapeutic_implications', 'standard_therapeutic_implications'];

            if(OncoKB.global.genes && OncoKB.global.genes && OncoKB.global.tumorTypes) {
                $scope.genes = getUnique(angular.copy(OncoKB.global.genes), 'hugoSymbol');
                // $scope.alterations = getUnique(angular.copy(OncoKB.global.alterations), 'name');
                $scope.tumorTypes = getUnique(angular.copy(OncoKB.global.tumorTypes), 'name');
            }else {
                DatabaseConnector.getGeneAlterationTumortype(function(data){
                    OncoKB.global.genes = angular.copy(data.genes);
                    OncoKB.global.alterations = angular.copy(data.alterations);
                    OncoKB.global.tumorTypes = angular.copy(data.tumorTypes);

                    $scope.genes = getUnique(data.genes, 'hugoSymbol');
                    // $scope.alterations = getUnique(data.alterations, 'name');
                    $scope.tumorTypes = getUnique(data.tumorTypes, 'name');
                    checkUrl();
                });
            }
        };
        
        function getUnique(data, attr) {
            var unique = [];

            if(angular.isArray(data)){
                data.forEach(function(e, i) {
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
                        $scope.alteration = urlVars.alteration;
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
            if(!same) {
                $location.search(params).replace();
                return true;
            }else {
                return false;
            }
        }
        
        $scope.isSearchable = function() {
            if($scope.gene && $scope.alteration) {
                return true;
            }else {
                return false;
            }
        }

        $scope.fdaApproved = function(drug) {
            if (typeof drug.fda_approved === 'string' && drug.fda_approved.toLowerCase() === 'yes'){
                return true;
            }else{
                return false;
            }
        };

        $scope.hasSelectedTumorType = function() {
            if($scope.hasOwnProperty('selectedTumorType') && $scope.selectedTumorType && $scope.selectedTumorType !== '') {
                return true;
            }else {
                return false;
            }
        }
        
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

        $scope.findRegex = function(str) {

            if(typeof str === 'string' && str !== '') {
                var regex = [/PMID:\s*([0-9]+,*\s*)+/ig, /NCT[0-9]+/ig],
                    links = ['http://www.ncbi.nlm.nih.gov/pubmed/',
                             'http://clinicaltrials.gov/show/'];
                for (var j = 0, regexL = regex.length; j < regexL; j++) {
                    var result = str.match(regex[j]);

                    if(result) {
                        var uniqueResult = result.filter(function(elem, pos) {
                            return result.indexOf(elem) === pos;
                        });
                        for(var i = 0, resultL = uniqueResult.length; i < resultL; i++) {
                            var _datum = uniqueResult[i];
                            
                            switch(j) {
                                case 0:
                                    var _number = _datum.split(':')[1].trim();
                                    _number = _number.replace(/\s+/g, '');
                                    str = str.replace(new RegExp(_datum, 'g'), '<a class="withUnderScore" target="_blank" href="'+ links[j] + _number+'">' + _datum + '</a>');
                                    break;
                                default:
                                    str = str.replace(_datum, '<a class="withUnderScore" target="_blank" href="'+ links[j] + _datum+'">' + _datum + '</a>');
                                    break;
                            }
                            
                        }
                    }
                }
            }
            return str;
        };
        
        $scope.search = function() {
            var hasSelectedTumorType = $scope.hasSelectedTumorType();
            $scope.rendering = true;
            $scope.reportViewActive = hasSelectedTumorType;
            $scope.regularViewActive = !hasSelectedTumorType;
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
                params['tumorType'] = $scope.selectedTumorType;
            }
            
            changeUrl(params);
            
            DatabaseConnector.searchAnnotation(params, function(data) {
                searchAnnotationCallback('success', data);
            }, function(){
                searchAnnotationCallback('fail');
            });
        };
        
        function searchAnnotationCallback(status, data) {
            var annotation = {};
            if(status === 'success') {
                annotation = processData(x2js.xml_str2json(data).xml);
                for(var key in annotation) {
                    annotation[key] = formatDatum(annotation[key], key);
                }
                
                $scope.annotation = annotation;
                if($scope.annotation.cancer_type) {
                    var relevantCancerType = [];
                    for(var i=0, cancerTypeL = $scope.annotation.cancer_type.length; i < cancerTypeL; i++) {
                        var _cancerType = $scope.annotation.cancer_type[i];
                        if(_cancerType.$relevant_to_patient_disease.toLowerCase() === 'yes') {
                            relevantCancerType.push(_cancerType);
                        }
                    }
                    if(relevantCancerType.length > 1) {
                        var obj1 = relevantCancerType[0];

                        for(var i=1, relevantL=relevantCancerType.length; i < relevantL; i++) {
                            obj1 = DeepMerge.init(obj1, relevantCancerType[i], obj1.$type, relevantCancerType[i].$type);
                        }
                        $scope.relevantCancerType = obj1;
                    }else if(relevantCancerType.length === 1){
                        $scope.relevantCancerType = relevantCancerType[0];
                    }else {
                        $scope.relevantCancerType = null;
                    }
                }else {
                    $scope.relevantCancerType = null;
                }

                $scope.reportParams = ReportDataService.init($scope.gene, $scope.alteration, $scope.selectedTumorType, $scope.relevantCancerType, $scope.annotation);
    //                $scope.regularViewData = regularViewData($scope.annotation);
                $scope.reportViewData = reportViewData($scope.reportParams);
            }
            $scope.rendering = false;
        }
        
        function processData(object){
            if(isArray(object)) {
                object.forEach(function(e, i){
                    e = processData(e);
                });
            }else if(isObject(object)) {
                for(var key in object) {
                    object[key] = processData(object[key]);
                }
            }else if(isString(object)) {
                object = S(object).decodeHTMLEntities().s;
            }else {

            }
            return object;
        }

        function upperFirstLetter(str){
            str = str.replace('_', ' ');
            return str.charAt(0).toUpperCase() + str.substr(1).toLowerCase();
        }
        
        function reportViewData(params) {
            var _parmas = angular.copy(params);
            _parmas.overallInterpretation = processOverallInterpretation(_parmas.overallInterpretation);
            _parmas = constructData(_parmas);
            return _parmas;
        }
        
//        function regularViewData(annotation) {
//            
//            if(annotation.hasOwnProperty('cancer_type')) {
//                annotation.cancer_type.forEach(function(e, i){
//                    var obj = {};
//                    
//                    obj['Treatment Implications'] = {
//                        'nccn_guidelines' : e.nccn_guidelines || '',
//                        'prognostic_implications': e.prognostic_implications || ''
//                    };
//                    
//                    obj['FDA Approved Drugs in Tumor Type'] = {};
//                    if(e.hasOwnProperty('standard_therapeutic_implications')){
//                        if(e.standard_therapeutic_implications.hasOwnProperty('general_statement')) {
//                            obj['Treatment Implications'].standard_therapeutic_implications = e.standard_therapeutic_implications.general_statement;
//                            delete e.standard_therapeutic_implications.general_statement;
//                        }
//                        obj['FDA Approved Drugs in Tumor Type']['drugs'] = {};
//                        for(var key in e.standard_therapeutic_implications) {
//                            if(e.standard_therapeutic_implications.hasOwnProperty(key)){
//                                obj['FDA Approved Drugs in Tumor Type']['drugs'][key]= e.standard_therapeutic_implications[key];
//                            }
//                        }
//                        if(Object.keys(obj['FDA Approved Drugs in Tumor Type']['drugs']).length > 0) {
//                            obj['FDA Approved Drugs in Tumor Type']['type'] = e.type || '';
//                        }
//                        delete e.standard_therapeutic_implications;
//                    }
//                    delete e.nccn_guidelines;
//                    delete e.prognostic_implications;
//                    
//                    obj['Clinical Trials'] = {
//                        'clinical_trial' : e.clinical_trial || '',
//                        'investigational_therapeutic_implications': e.investigational_therapeutic_implications || ''
//                    };
//                    
//                    delete e.clinical_trial;
//                    delete e.investigational_therapeutic_implications;
//                    
//                    obj['type'] = e.type || '';
//                    obj['relevant_to_patient_disease'] = e.relevant_to_patient_disease || '';
//                    
//                    delete e.type;
//                    delete e.relevant_to_patient_disease;
//                    
//                    //Add rest info to  Additional Information section
//                    obj['Additional Information'] = {};
//                    for(var key in e) {
//                        if(e.hasOwnProperty(key)) {
//                            obj['Additional Information'][key] = e[key];
//                            delete e[key];
//                        }
//                    }
//                    annotation.cancer_type[i] = obj;
//                });
//                
//                annotation.cancer_type.forEach(function(e, i){
//                    e['FDA Approved Drugs in Other Tumor Type'] = [];
//                    annotation.cancer_type.forEach(function(e1, i1){
//                        if(i !== i1) {
//                            if(e1['FDA Approved Drugs in Tumor Type'].hasOwnProperty('type')) {
//                                e['FDA Approved Drugs in Other Tumor Type'].push(e1['FDA Approved Drugs in Tumor Type']);
//                            }
//                        }
//                    });
//                });
//            }
//            console.log(annotation);
//            return annotation;
//        }

        function isObject(obj) {
            return angular.isObject(obj) && !angular.isArray(obj);
        }
        
        function isArray(obj) {
            return angular.isArray(obj);
        }
        
        function isString(obj) {
            return angular.isString(obj);
        }
        
        function bottomObject(obj) {
           var flag = true;
           if(obj && typeof obj === 'object') {
             for(var key in obj) {
               if(typeof obj[key] !== 'string' && typeof obj[key] !== 'number') {
                 flag = false;
                 break;
               }
             }
           }else {
             flag = false;
           }
           return flag;
        }

        function objToArray(obj) {
            var delayAttrs = ['description'];
            var priorAttrs = ['trial','nccn_special','recommendation category 1 / 2A / 2 / 2A / 2A'];
            
            if (!angular.isObject(obj)) {
              return obj;
            }

            var keys = Object.keys(obj).filter(function(item) {
                return item !== '$$hashKey';
            }).sort(function(a,b) {
                var delayIndexOfA = delayAttrs.indexOf(a),
                    delayIndexOfB = delayAttrs.indexOf(b),
                    priorIndexOfA = priorAttrs.indexOf(a),
                    priorIndexOfB = priorAttrs.indexOf(b);

                if(priorIndexOfA !== -1 && priorIndexOfB !== -1) {
                    if(priorIndexOfA <= priorIndexOfB) {
                        return -1;
                    }else {
                        return 1;
                    }
                }else if(priorIndexOfA !== -1) {
                    return -1;
                }else if(priorIndexOfB !== -1) {
                    return 1;
                }else {
                    if(delayIndexOfA !== -1 && delayIndexOfB !== -1) {
                        if(delayIndexOfA <= delayIndexOfB) {
                            return 1;
                        }else {
                            return -1;
                        }
                    }else if(delayIndexOfA !== -1) {
                        return 1;
                    }else if(delayIndexOfB !== -1) {
                        return -1;
                    }else {
                        if(a < b) {
                            return -1;
                        }else {
                            return 1;
                        }
                    }
                }
            });
            
            var returnArray = keys.map(function (key) {
                    var _obj = {};
                    
                    _obj.key = key;
                    _obj.value = $scope.findRegex(obj[key]).toString();
                    return _obj;
                });

            return returnArray;
        }

        function constructData(data) {
            for(var key in data) {
                var datum = data[key];
                if(isArray(datum)) {
                    datum = constructData(datum);
                }else if(isObject(datum) && !bottomObject(datum)) {
                    datum = constructData(datum);
                }else if(isObject(datum) && bottomObject(datum)) {
                    datum = objToArray(datum);
                }else {
                    datum = $scope.findRegex(datum);
                }
                data[key] = datum;
            }
            
            return data;
        }

        function googleDocData(params) {
            return params;
        }
        
        $scope.generateReport = function() {
            var dlg = dialogs.create('views/emailDialog.html','emailDialogCtrl', 'Test string');
            dlg.result.then(function(data){
                if(typeof data !== 'undefined' && data && data !== '') {
                    $scope.generaingReport =true;
                    dlg = dialogs.wait('Sending request','Please wait...');
                    generating();
                    var params = googleDocData($scope.reportParams);
                    params.email = data;
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

        $scope.useExample = function() {
            $scope.gene= $scope.genes[$filter('getIndexByObjectNameInArray')($scope.genes, 'BRAF')];
            // $scope.alteration = $scope.alterations[$filter('getIndexByObjectNameInArray')($scope.alterations, 'V600E')];
            $scope.alteration = 'V600E';
            $scope.selectedTumorType = $scope.tumorTypes[$filter('getIndexByObjectNameInArray')($scope.tumorTypes, 'melanoma')];
            $scope.search();
        };

        function processOverallInterpretation(str) {
            var content = str.split(/[\n\r]/g);
            for(var i=0; i< content.length; i++) {
                if(i%2 === 0) {
                    content[i]='<b>' + content[i] + '</b>';
                }
            }
            str = content.join('<br/>');
            return str;
        }
        
        function lineBreakToHtml(str) {
            str = str.replace(/(\r\n|\n|\r)/gm, '<br/>');
            return str;
        }

        function formatDatum(value, key) {
            if($scope.isArray(value) || (!$scope.isArray(value) && $scope.isObject(value) && changedAttr.indexOf(key) !== -1)) {
                if(!$scope.isArray(value) && $scope.isObject(value) && changedAttr.indexOf(key) !== -1) {
                    value = [value];
                }

                for (var i = 0; i < value.length; i++) {
                    value[i] = formatDatum(value[i], i);
                }
            }else if($scope.isObject(value)) {
                for(var _key in value) {
                    value[_key] = formatDatum(value[_key], _key);
                }
            }
            
            return value;
        }
    }]);