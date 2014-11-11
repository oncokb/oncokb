angular.module('webappApp')
    .controller('VariantCtrl', [
        '$scope', 
        '$filter',
        '$location',
        '$timeout',
        '$rootScope',
        'dialogs',
        'TumorType', 
        'SearchVariant',
        'GenerateDoc',
        function ($scope, $filter, $location, $timeout, $rootScope, dialogs, TumorType, SearchVariant, GenerateDoc) {

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
                'Clinical Trials',
                'FDA Approved Drugs in Tumor Type', 
                'FDA Approved Drugs in Other Tumor Type',
                'Additional Information'
            ];

            $scope.reportMatchedParams = [
                'treatment', 
                'clinicalTrials',
                'fdaApprovedInTumor', 
                'fdaApprovedInOtherTumor',
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

            $scope.reportViewActive = $scope.hasSelectedTumorType();
            $scope.regularViewActive = !$scope.hasSelectedTumorType();
            // TumorType.getFromFile().success(function(data) {
            TumorType.getFromServer().success(function(data) {
                $scope.tumorTypes = data;
                if($location.url() !== $location.path()) {
                    var urlVars = $location.search();
                    if(urlVars.hasOwnProperty('hugoSymbol')){
                        $scope.geneName = urlVars.hugoSymbol;
                    }
                    if(urlVars.hasOwnProperty('alteration')){
                        $scope.mutation = urlVars.alteration;
                    }
                    if(urlVars.hasOwnProperty('tumorType')){
                        $scope.selectedTumorType = $scope.tumorTypes[$filter('getIndexByObjectNameInArray')($scope.tumorTypes, 'name', urlVars['tumorType'].toLowerCase() || '')];
                    }
                    $scope.search();
                }
            });
        };
        
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

        $scope.getCollapseIcon = function(trial, attr) {
            if(typeof $scope.isCollapsed[trial.trial_id] === 'undefined' || $scope.isCollapsed[trial.trial_id][attr] ) {
                return "images/add.svg";
            }else{
                return "images/subtract.svg";
            }
        };

        $scope.generateTrial = function(trial) {
            var str = '';
            var purposeStr = '';

            if(typeof $scope.isCollapsed[trial.trial_id] === 'undefined') {
                $scope.isCollapsed[trial.trial_id] = {
                    purpose: true,
                    eligibility_criteria: true
                };
            }

            str += trial.hasOwnProperty('trial_id')?('TRIAL ID: ' + $scope.findRegex(trial.trial_id) + (trial.hasOwnProperty('phase')?(' / ' + trial.phase): '') + '<br/>'):'';
            str += trial.hasOwnProperty('title')?('TITLE: ' + trial.title + '<br/>'):'';
            
            // str += trial.hasOwnProperty('description')?('<br>' + $scope.findRegex(trial.description) + '<br/>'):'';
            return str;
        };

        $scope.generateNCCN = function(nccn) {
            var str = '<i>';

            str += nccn.hasOwnProperty('disease')?('Disease: ' + nccn.disease):'';
            str += nccn.hasOwnProperty('version')?(' Version: ' + nccn.version):'';
            str += nccn.hasOwnProperty('pages')?(' Pages: ' + nccn.pages):'';

            str += '</i>';
            str += nccn.hasOwnProperty('description')?('<br>' + $scope.findRegex(nccn.description) + '<br/>'):'';

            return str;
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
                'hugoSymbol': 'geneName',
                'alteration': 'mutation'
            };

            for (var key in paramsContent) {
                if($scope.hasOwnProperty(paramsContent[key]) && $scope[paramsContent[key]] && $scope[paramsContent[key]] !== '') {
                    params[key] = $scope[paramsContent[key]];
                }
            }
            if(hasSelectedTumorType) {
                params['tumorType'] = $scope.selectedTumorType.name;
            }                

            // SearchVariant.annotationFromFile(params).success(function(data) {
            SearchVariant.getAnnotation(params).success(function(data) {
                var annotation = {};
                annotation = processData(xml2json.parser(data).xml);

                for(var key in annotation) {
                    annotation[key] = formatDatum(annotation[key], key);
                }

                $scope.annotation = annotation;
                $scope.rendering = false;
                if($scope.annotation.cancer_type) {
                    var relevantCancerType = [];
                    for(var i=0, cancerTypeL = $scope.annotation.cancer_type.length; i < cancerTypeL; i++) {
                        var _cancerType = $scope.annotation.cancer_type[i];
                        if(_cancerType.relevant_to_patient_disease.toLowerCase() === 'yes') {
                            relevantCancerType.push(_cancerType);
                        }
                    }
                    if(relevantCancerType.length > 1) {
                        var obj1 = relevantCancerType[0];

                        for(var i=1, relevantL=relevantCancerType.length; i < relevantL; i++) {
                            obj1 = deepmerge(obj1, relevantCancerType[i], obj1.type, relevantCancerType[i].type);
                        }
                        $scope.relevantCancerType = obj1;
                    }else if(relevantCancerType.length === 1){
                        $scope.relevantCancerType = relevantCancerType[0];
                    }else {
                        $scope.relevantCancerType = null;
                    }
                }

                $scope.reportParams = generateReportData();
                $scope.reportViewData = reportViewData($scope.reportParams);
            });
        };

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
               if(typeof obj[key] !== 'string') {
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
            var prioAttrs = ['trial','cancer_type','version'];

            if (!(obj instanceof Object)) {
              return obj;
            }

            
            var keys = Object.keys(obj).filter(function(item) {
                return item !== '$$hashKey';
            }).sort(function(a,b) {
                        if( delayAttrs.indexOf(a) !== -1){
                            return 1;
                        }else if (delayAttrs.indexOf(b) !== -1) {
                            return -1;
                        }else if (prioAttrs.indexOf(a) !== -1) {
                            return -1;
                        }else if(prioAttrs.indexOf(b) !== -1) {
                            return 1;
                        }else {
                            if(a < b) {
                                return -1;
                            }else {
                                return 1;
                            }
                        }
                    });
            
                var returnArray = keys.map(function (key) {
                        var _obj = {};
                        
                        _obj.key = upperFirstLetter(key);
                        _obj.value = $scope.findRegex(obj[key]);
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
        //This original function comes fromhttps://github.com/nrf110/deepmerge
        //Made changed for using in current project
        //ct1: cancer type of target; ct2: cancer type of source
        function deepmerge(target, src, ct1, ct2) {
            var array = Array.isArray(src);
            var dst = array && [] || {};

            if (array) {
                target = target || [];
                dst = dst.concat(target);
                src.forEach(function(e, i) {
                    dst.push(e);
                });
            } else {
                if (target && typeof target === 'object') {
                    Object.keys(target).forEach(function (key) {
                        dst[key] = target[key];
                    });
                }
                Object.keys(src).forEach(function (key) {
                    if (typeof src[key] !== 'object' || !src[key]) {
                        if(!Array.isArray(dst[key])) {
                            var _tmp = dst[key];
                            dst[key] = [{'value':_tmp, 'cancer_type': ct1}];
                        }
                        dst[key].push({'value':src[key], 'cancer_type': ct2} );
                    }
                    else {
                        if (!target[key]) {
                            dst[key] = src[key];
                        } else {
                            dst[key] = deepmerge(target[key], src[key], ct1, ct2);
                        }
                    }
                });
            }
            return dst;
        }
        
        $scope.generateReport = function() {
//            if(typeof $scope.relevantCancerType !== 'undefined' && $scope.relevantCancerType && $scope.relevantCancerType !== '') {
                var dlg = dialogs.create('views/emailDialog.html','emailDialogCtrl', 'Test string');
                dlg.result.then(function(data){
                    if(typeof data !== 'undefined' && data && data !== '') {
                        $scope.generaingReport =true;
                        dlg = dialogs.wait('Sending request','Please wait...');
                        generating();
                        var params = googleDocData($scope.reportParams);
                        params.email = data;
//                         $timeout(function() {
//                             console.log(params);
//                             $scope.generaingReport =false;
//                         }, 2000)
                        GenerateDoc.getDoc(params).success(function(data) {
                            $scope.generaingReport =false;
                        });
                    }
                },function(){
                  console.log('Did not do anything.');
                });
//            }else {
//                alert('No relevant cancer type can be found. Please recheck your gene name, mutation and selected tumor type.');
//            }
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
            $scope.geneName = 'BRAF';
            $scope.mutation = 'V600E';
            $scope.selectedTumorType = $scope.tumorTypes[$filter('getIndexByObjectNameInArray')($scope.tumorTypes, 'name', 'melanoma')];
            $scope.search();
        };
        
        function generateReportData() {
            var params = {
                "email": "",
                "patientName": "",
                "specimen": "",
                "clientNum": "",
                "overallInterpretation": "",
                "diagnosis": "",
                "tumorTissueType": "",
                "specimenSource": "",
                "blockId": "",
                "stage": "",
                "grade": "",
                "geneName": "",
                "mutation": "",
                "alterType": "",
                "mutationFreq": "",
                "tumorTypeDrugs": "",
                "nonTumorTypeDrugs": "",
                "hasClinicalTrial": "",
                "treatment": "",
                "fdaApprovedInTumor": "",
                "fdaApprovedInOtherTumor": "",
                "clinicalTrials": "",
                "additionalInfo": "",
                "companionDiagnostics": ""
            };

            // console.log($scope.annotation);

            params.overallInterpretation = ($scope.geneName + ' ' + $scope.mutation + ' SUMMARY\n' + 
                $scope.annotation.annotation_summary + 
                '\nOTHER GENES\nNo additional somatic mutations were detected in this patient sample in the other sequenced gene regions.') || '';
            params.geneName = $scope.geneName;
            params.mutation = $scope.mutation;
            params.diagnosis = ($scope.selectedTumorType && $scope.selectedTumorType.name)?$scope.selectedTumorType.name:'';
            params.tumorTissueType = params.diagnosis;
//            if($scope.relevantCancerType) {
                var _treatment = constructTreatment();
                params.treatment = _treatment.length > 0 ? _treatment : "";
                var _fdaInfo = constructfdaInfo();
                params.fdaApprovedInTumor = _fdaInfo.approved.length > 0 ? _fdaInfo.approved : "";
                params.fdaApprovedInOtherTumor = _fdaInfo.nonApproved.length > 0 ? _fdaInfo.approved : "";
                var _clinicalTrail = constructClinicalTrial();
                params.clinicalTrials = _clinicalTrail.length > 0 ? _clinicalTrail : "";
                var _additionalInfo = constructAdditionalInfo();
                params.additionalInfo = _additionalInfo.length > 0 ? _additionalInfo : "";;
//            }
            // console.log(params);
            return params;
        }

        function constructObjectDatum(key, value) {
            return {key: value};
        }

        // constructContent(cancerTypeInfo.nccn_guidelines, "NCCN GUIDELINES", [[cancer_type, version, description]]);

        // function constructContent(object, keyToSave, attrs) {

        //     if(attrs instanceof Array) {
        //         for (var i = 0; i < attrs.length; i++) {
                    
        //         };
        //     }
        //     if(object) {
        //         var _datum = angular.copy(object)，
        //             _value = [],
        //             _object = {},
        //             _key = keyToSave;

        //         for(var i=0, _datumL = _datum.length; i < _datumL; i++) {
        //             var _subDatum = {};
        //             _subDatum.cancer_type = $scope.tumorType;
        //             _subDatum.version = _datum[i].version;
        //             _subDatum.description = _datum[i].description;
        //             value.push(_subDatum);
        //         }
                
        //         object[key] = value;
        //         treatment.push(object);
        //     }
        // }

        function constructTreatment() {
            var treatment = [],
                key = '',
                value = [],
                object = {},
                cancerTypeInfo = $scope.relevantCancerType || {};

            if($scope.annotation.annotation_summary) {
                key = $scope.geneName + ' ' + $scope.mutation + " SUMMARY";
                value.push({'description': $scope.annotation.annotation_summary});
                object[key] = value;
                treatment.push(object);
            }

            if(cancerTypeInfo.nccn_guidelines) {
                var _datum = cancerTypeInfo.nccn_guidelines;
                var versions = {};

                value = [];
                object = {};
                key = "NCCN GUIDELINES";
                for(var i=0, _datumL = _datum.length; i < _datumL; i++) {
                    if(!versions.hasOwnProperty(_datum[i].version)) {
                        versions[_datum[i].version] = {};
                    }
                }

                for(var i=0, _datumL = _datum.length; i < _datumL; i++) {
                    versions[_datum[i].version]['recommendation category ' + _datum[i].recommendation_category] = _datum[i].description;
                }
                
                for(var versionKey in versions) {
                    var version = versions[versionKey];
                    version.nccn_special = 'Version: ' + versionKey + (($scope.selectedTumorType && $scope.selectedTumorType.name)? (', Cancer type: ' + $scope.selectedTumorType.name) : '');
                    value.push(version);
                }
                
                object[key] = value;
                treatment.push(object);
            }
            
            if(cancerTypeInfo.standard_therapeutic_implications && cancerTypeInfo.standard_therapeutic_implications.general_statement) {
                value = [];
                object = {};
                key = "STANDARD THERAPEUTIC IMPLICATIONS";
                value.push({'description': cancerTypeInfo.standard_therapeutic_implications.general_statement.sensitivity.description});
                object[key] = value;
                treatment.push(object);
            }

            if(cancerTypeInfo.prognostic_implications) {
                value = [];
                key = "PROGNOSTIC IMPLICATIONS";
                object = {};
                value.push({'description': cancerTypeInfo.prognostic_implications.description});
                object[key] = value;
                treatment.push(object);
            }

            return treatment;
        }

        function findApprovedDrug(datum, object, tumorType, key) {
            for(var m=0, datumL = datum.length; m < datumL; m++) {
                var _subDatum = datum[m],
                    _key = '';

                if(typeof key !== 'undefined') {
                    _key = key;
                }

                if(_subDatum.treatment) {
                    for (var i = 0; i < _subDatum.treatment.length; i++) {
                        var _treatment = _subDatum.treatment[i];
                        if(_treatment.drug) {
                            for (var j = 0; j < _treatment.drug.length; j++) {
                                var _drug = _treatment.drug[j];
                                if(_drug.fda_approved === 'Yes') {
                                    _key+=_drug.name + " + ";
                                }
                            };
                        }
                        _key = _key.substr(0, _key.length-3);
                        _key += " & ";
                    }
                }

                _key = _key.substr(0, _key.length-3);

                if(typeof tumorType !== "undefined" && tumorType !== "") {
                    _key += " in " + tumorType;
                }
                object[_key] = [{'description': _subDatum.description}];
            }

            return object;
        }

        function findByLevelEvidence(datum, object, tumorType, key) {
            for(var m=0, datumL = datum.length; m < datumL; m++) {
                var _subDatum = datum[m],
                    _key = '';

                if(typeof key !== 'undefined') {
                    _key = key;
                }

                if(_subDatum.level_of_evidence_for_patient_indication 
                    && _subDatum.level_of_evidence_for_patient_indication.level) {
                    
//                    if(     (isNaN(_subDatum.level_of_evidence_for_patient_indication.level) 
//                            && /2a|2b/g.test(_subDatum.level_of_evidence_for_patient_indication.level))
//                        ||  (!isNaN(_subDatum.level_of_evidence_for_patient_indication.level)
//                            && (Number(_subDatum.level_of_evidence_for_patient_indication.level) < 4))
//                        ) {

                        if(_subDatum.treatment) {
                            for (var i = 0; i < _subDatum.treatment.length; i++) {
                                var _treatment = _subDatum.treatment[i];
                                if(_treatment.drug) {
                                    for (var j = 0; j < _treatment.drug.length; j++) {
                                        var _drug = _treatment.drug[j];
                                            _key+=_drug.name + " + ";
                                    };
                                }
                                _key = _key.substr(0, _key.length-3);
                                _key += " & ";
                            }
                        }

                        _key = _key.substr(0, _key.length-3);

                        if(object.hasOwnProperty(_key)) {
                            console.log('key duplicated: ' + _key);
                        }else {
                            object[_key] = [{
                                'level of evidence': _subDatum.level_of_evidence_for_patient_indication.level,
                                'description': _subDatum.description
                            }];
                        }
                    }
//                }
                }
            return object;
        }

        function constructfdaInfo() {
            var fdaApproved = [],
                fdaNonApproved = [],
                object = {},
                cancerTypeInfo = $scope.relevantCancerType || {},
                attrsToDisplay = ['sensitive_to', 'resistant_to'];

            if(cancerTypeInfo.standard_therapeutic_implications) {
                for (var i = 0; i < attrsToDisplay.length; i++) {
                    if(cancerTypeInfo.standard_therapeutic_implications[attrsToDisplay[i]]) {
                        var _datum = cancerTypeInfo.standard_therapeutic_implications[attrsToDisplay[i]];
                        
                        object = {};
                        if(attrsToDisplay[i] === 'sensitive_to') {
                            object = findApprovedDrug(_datum, object);
                        }else {
                            object = findApprovedDrug(_datum, object, '', $scope.displayProcess(attrsToDisplay[i]) + ': ');
                        }

                        for(var _key in object) {
                            var _object = {};
                            _object[_key] = object[_key]
                            fdaApproved.push(_object);
                            _object = null;
                        }
                    }
                }
            }

            if($scope.annotation.cancer_type && $scope.relevantCancerType && $scope.relevantCancerType.type) {
                object = {};

                for (var i = 0; i < $scope.annotation.cancer_type.length; i++) {
                    if($scope.annotation.cancer_type[i].type !== $scope.relevantCancerType.type) {
                        if($scope.annotation.cancer_type[i].standard_therapeutic_implications) {
                            for (var j = 0; j < attrsToDisplay.length; j++) {
                                if($scope.annotation.cancer_type[i].standard_therapeutic_implications[attrsToDisplay[j]]) {
                                    var _datum = $scope.annotation.cancer_type[i].standard_therapeutic_implications[attrsToDisplay[j]];
                                    if(attrsToDisplay[j] === 'sensitive_to') {
                                        object = findApprovedDrug(_datum, object, $scope.annotation.cancer_type[i].type);
                                    }else {
                                        object = findApprovedDrug(_datum, object, $scope.annotation.cancer_type[i].type, $scope.displayProcess(attrsToDisplay[j]) + ': ');
                                    }
                                }
                            }
                        }
                    }
                }

                for(var _key in object) {
                    var _object = {};
                    _object[_key] = object[_key]
                    fdaNonApproved.push(_object);
                    _object = null;
                }
            }
            return {'approved': fdaApproved, 'nonApproved': fdaNonApproved};
        }

        function constructClinicalTrial() {
            var clincialTrials = [],
                key = '',
                value = [],
                object = {},
                cancerTypeInfo = $scope.relevantCancerType || {},
                attrsToDisplay = ['sensitive_to', 'resistant_to'];

            if(cancerTypeInfo.clinical_trial) {
                var _datum = cancerTypeInfo.clinical_trial;

                value = [];
                object = {};
                key = "CLINICAL TRIALS MATCHED FOR GENE AND DISEASE";

                for(var i=0, _datumL = _datum.length; i < _datumL; i++) {
//                    if(/phase 3|phase 4/i.test(_datum[i].phase)) {
                        var _subDatum = {};
                        _subDatum.trial = _datum[i].trial_id + ", " + _datum[i].phase;
                        _subDatum.title = _datum[i].title;
                        _subDatum.description = removeCharsInDescription(_datum[i].description);
                        value.push(_subDatum);
//                    }
                }
                
                object[key] = value;
                clincialTrials.push(object);
            }

            if(cancerTypeInfo.investigational_therapeutic_implications) {
                var hasdrugs = false;
                if(cancerTypeInfo.investigational_therapeutic_implications.general_statement) {
                    value = [];
                    object = {};
                    key = "INVESTIGATIONAL THERAPEUTIC IMPLICATIONS";
                    object[key] = addRecord({'array': ['cancer_type', 'value'], 'object':'description'}, cancerTypeInfo.investigational_therapeutic_implications.general_statement.sensitivity.description, value);
                
                    clincialTrials.push(object);
                }
                
                for (var j = 0; j < attrsToDisplay.length; j++) {
                    if(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]]) {
                        object = {};
                        if(attrsToDisplay[j] === 'sensitive_to') {
                            object = findByLevelEvidence(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]], object);
                        }else {
                            object = findByLevelEvidence(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]], object, '', $scope.displayProcess(attrsToDisplay[j]) + ': ');
                        }
                        if(Object.keys(object).length > 0) {
                            hasdrugs = true;
                        }
                        for(var _key in object) {
                            var _object = {};
                            _object[_key] = object[_key];
                            clincialTrials.push(_object);
                            _object = null;
                        }
                    }
                }

                if(!hasdrugs) {
                    if(!cancerTypeInfo.investigational_therapeutic_implications.general_statement) {
                        value = [];
                        object = {};
                        key = "INVESTIGATIONAL THERAPEUTIC IMPLICATIONS";
                        value.push({'description': 'There are no investigational therapies that meet level 1, 2 or 3 evidence.'});
                        object[key] = value;
                        clincialTrials.push(object);
                    }else {
                        clincialTrials.push({'no_evidence': 'There are no investigational therapies that meet level 1, 2 or 3 evidence.'});
                    }
                }

                object = {};
                key = 'LEVELS OF EVIDENCE';
                value =  [
                    {'level 1': 'FDA-approved biomarker in approved indication.'},
                    {'level 2a': 'FDA-approved biomarker in unapproved indication with NCCN-guideline listing.'},
                    {'level 2b': 'FDA-approved biomarker in unapproved indication.'},
                    {'level 3': 'Clinical evidence linking unapproved biomarker to response'},
                    {'level 4': 'Preclinical evidence linking unapproved biomarker to response.'}
                ];
                object[key] = value;
                clincialTrials.push(object);
            }
            return clincialTrials;
        }

        function addRecord(keys, value, array) {
            if(Array.isArray(value)) {
                value.forEach(function(e, i) {
                    var _obj = {};
                    keys.array.forEach(function(e1, i1) {
                        _obj[e1] = removeCharsInDescription(e[e1]);
                    });
                    array.push(_obj);
                });
            }else {
                var _obj = {};
                _obj[keys.object] = removeCharsInDescription(value);
                array.push(_obj);
            }
            return array;
        }
        
        function constructAdditionalInfo() {
            var additionalInfo = [],
                key = '',
                value = [],
                object = {},
                cancerTypeInfo = $scope.relevantCancerType || {};

            if($scope.annotation.gene_annotation) {
                value = [];
                key = 'BACKGROUND';
                object = {};
                value.push({'description': removeCharsInDescription($scope.annotation.gene_annotation.description)});
                object[key] = value;
                additionalInfo.push(object);
            }

            if(cancerTypeInfo.prevalence) {
                value = [];
                key = 'MUTATION PREVALENCE';
                object = {};
                object[key] = addRecord({'array': ['cancer_type', 'value'], 'object':'description'}, cancerTypeInfo.prevalence.description, value);
                additionalInfo.push(object);
            }

            if($scope.annotation.variant_effect) {
                value = [];
                key = 'MUTATION EFFECT';
                object = {};
                value.push({
                    'effect': $scope.annotation.variant_effect.effect || '',
                    'description': $scope.annotation.variant_effect.description? removeCharsInDescription($scope.annotation.variant_effect.description) : ''
                });
                object[key] = value;
                additionalInfo.push(object);
            }
            return additionalInfo;
        }

        function removeCharsInDescription(str) {
            if(typeof str !== 'undefined') {
                str = str.replace(/(\r\n|\n|\r)/gm,'');
                str = str.replace(/(\s\s*)/g,' ');
                return str;
            }else {
                return '';
            }
        }

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