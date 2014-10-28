angular.module('webappApp')
    .controller('VariantCtrl', [
        '$scope', 
        '$filter',
        '$location',
        '$timeout',
        'TumorType', 
        'SearchVariant',
        'GenerateDoc',
        function ($scope, $filter, $location, $timeout, TumorType, SearchVariant, GenerateDoc) {

        'use strict';

        var changedAttr = ['nccn_guidelines', 'clinical_trial', 'sensitive_to', 'resistant_to', 'treatment', 'drug'];
        
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
                'Additional Information', 
                'FDA Approved Drugs in Tumor Type', 
                'FDA Approved Drugs in Other Tumor Type'];
                
            $scope.summaryTableTitlesContent = {
                'Treatment Implications': [
                    'nccn_guidelines',
                    'standard_therapeutic_implications',
                    'investigational_therapeutic_implications'],
                'Clinical Trials': ['clinical_trial'], 
                'Additional Information': ['prevalence', 'prognostic_implications'], 
                'FDA Approved Drugs in Tumor Type': [], 
                'FDA Approved Drugs in Other Tumor Type': []
            };

            $scope.specialAttr = ['investigational_therapeutic_implications', 'standard_therapeutic_implications'];

            // TumorType.getFromFile().success(function(data) {
            TumorType.getFromServer().success(function(data) {
                $scope.tumorTypes = data;
                if($location.url() !== $location.path()) {
                    var urlVars = $location.search();
                    $scope.rendering = true;
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
            $scope.rendering = true;
            var params = {'alterationType': 'MUTATION'};
            var paramsContent = {
                'hugoSymbol': 'geneName',
                'alteration': 'mutation'
            }

            for (var key in paramsContent) {
                if($scope.hasOwnProperty(paramsContent[key])) {
                    params[key] = $scope[paramsContent[key]];
                }
            }
            if($scope.hasOwnProperty('selectedTumorType')) {
                params['tumorType'] = $scope.selectedTumorType.name;
            }                

            // SearchVariant.annotationFromFile(params).success(function(data) {
            SearchVariant.postAnnotation(params).success(function(data) {
                var annotation = {};

                annotation = xml2json.parser(data).xml;

                for(var key in annotation) {
                    annotation[key] = formatDatum(annotation[key], key);
                }

                $scope.annotation = annotation;
                $scope.rendering = false;
                if($scope.annotation.cancer_type) {
                    for(var i=0, cancerTypeL = $scope.annotation.cancer_type.length; i < cancerTypeL; i++) {
                        var _cancerType = $scope.annotation.cancer_type[i];
                        if(_cancerType.relevant_to_patient_disease.toLowerCase() === 'yes') {
                            $scope.relevantCancerType = _cancerType;
                            break;
                        }
                    }
                }
            });
        };
        
        $scope.generateReport = function() {
            $scope.generaingReport =true;
            var params = generateReportData();

            // $timeout(function() {
            //     $scope.generaingReport =false;
            // }, 1000)
            GenerateDoc.getDoc(params).success(function(data) {
                $scope.generaingReport =false;
            });
        };
        
        $scope.useExample = function() {
            $scope.geneName = 'BRAF';
            $scope.mutation = 'V600E';
            $scope.selectedTumorType = $scope.tumorTypes[$filter('getIndexByObjectNameInArray')($scope.tumorTypes, 'name', 'melanoma')];
            $scope.search();
        };
        
        function generateReportData() {
            var params = {
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

            params.overallInterpretation = ($scope.geneName + ' SUMMARY\n' + 
                $scope.annotation.annotation_summary + 
                '\nOTHER GENES\nNo additional somatic mutations were detected in this patient sample in the other sequenced gene regions.') || '';
            params.geneName = $scope.geneName;
            params.mutation = $scope.mutation;
            params.alterType = $scope.selectedTumorType.name;
            
            if($scope.relevantCancerType) {
                var _treatment = constructTreatment();
                params.treatment = _treatment.length > 0 ? _treatment : "";
                var _fdaInfo = constructfdaInfo();
                params.fdaApprovedInTumor = _fdaInfo.approved.length > 0 ? _fdaInfo.approved : "";
                params.fdaApprovedInOtherTumor = _fdaInfo.nonApproved.length > 0 ? _fdaInfo.approved : "";
                var _clinicalTrail = constructClinicalTrial();
                params.clinicalTrials = _clinicalTrail.length > 0 ? _clinicalTrail : "";
                var _additionalInfo = constructAdditionalInfo();
                params.additionalInfo = _additionalInfo.length > 0 ? _additionalInfo : "";;
            }
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
                cancerTypeInfo = $scope.relevantCancerType;

            if($scope.annotation.annotation_summary) {
                key = $scope.geneName + " SUMMARY";
                value.push({'description': $scope.annotation.annotation_summary});
                object[key] = value;
                treatment.push(object);
            }

            if(cancerTypeInfo.nccn_guidelines) {
                var _datum = cancerTypeInfo.nccn_guidelines;

                value = [];
                object = {};
                key = "NCCN GUIDELINES";

                for(var i=0, _datumL = _datum.length; i < _datumL; i++) {
                    var _subDatum = {};
                    _subDatum.cancer_type = $scope.tumorType;
                    _subDatum.version = _datum[i].version;
                    _subDatum.description = _datum[i].description;
                    value.push(_subDatum);
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
                    }
                }

                _key = _key.substr(0, _key.length-3);

                if(typeof tumorType !== "undefined" && tumorType !== "") {
                    _key += " in " + tumorType;
                }
                object[_key] = {'description': _subDatum.description};
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
                    && _subDatum.level_of_evidence_for_patient_indication.level
                    && Number(_subDatum.level_of_evidence_for_patient_indication.level) < 4) {
                    
                    if(_subDatum.treatment) {
                        for (var i = 0; i < _subDatum.treatment.length; i++) {
                            var _treatment = _subDatum.treatment[i];
                            if(_treatment.drug) {
                                for (var j = 0; j < _treatment.drug.length; j++) {
                                    var _drug = _treatment.drug[j];
                                        _key+=_drug.name + " + ";
                                };
                            }
                        }
                    }

                    _key = _key.substr(0, _key.length-3);

                    if(object.hasOwnProperty(_key)) {
                        console.log('key duplicated: ' + _key);
                    }else {
                        object[_key] = {
                            'level of evidence': _subDatum.level_of_evidence_for_patient_indication.level,
                            'description': _subDatum.description
                        };
                    }
                }
            }
            return object;
        }

        function constructfdaInfo() {
            var fdaApproved = [],
                fdaNonApproved = [],
                object = {},
                cancerTypeInfo = $scope.relevantCancerType,
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
            return {'approved': fdaApproved, 'nonApproved': fdaNonApproved};
        }

        function constructClinicalTrial() {
            var clincialTrials = [],
                key = '',
                value = [],
                object = {},
                cancerTypeInfo = $scope.relevantCancerType,
                attrsToDisplay = ['sensitive_to', 'resistant_to'];

            if(cancerTypeInfo.clinical_trial) {
                var _datum = cancerTypeInfo.clinical_trial;

                value = [];
                object = {};
                key = "CLINICAL TRIALS MATCED FOR GENE AND DISEASE";

                for(var i=0, _datumL = _datum.length; i < _datumL; i++) {
                    if(/phase 3|phase 4/i.test(_datum[i].phase)) {
                        var _subDatum = {};
                        _subDatum.trial = _datum[i].trial_id + ", " + _datum[i].phase;
                        _subDatum.title = _datum[i].title;
                        _subDatum.purpose = removeCharsInDescription(_datum[i].purpose);
                        _subDatum.description = removeCharsInDescription(_datum[i].description);
                        value.push(_subDatum);
                    }
                }
                
                object[key] = value;
                clincialTrials.push(object);
            }

            if(cancerTypeInfo.investigational_therapeutic_implications && cancerTypeInfo.investigational_therapeutic_implications.general_statement) {
                var hasdrugs = false;
                value = [];
                object = {};
                key = "INVESTIGATIONAL THERAPEUTIC IMPLICATIONS";
                value.push({'description': removeCharsInDescription(cancerTypeInfo.investigational_therapeutic_implications.general_statement.sensitivity.description)});
                object[key] = value;
                clincialTrials.push(object);

                for (var j = 0; j < attrsToDisplay.length; j++) {
                    if(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]]) {
                        object = {};
                        if(attrsToDisplay[j] === 'sensitive_to') {
                            object = findByLevelEvidence(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]], object);
                        }else {
                            object = findByLevelEvidence(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]], object, '', $scope.displayProcess(attrsToDisplay[j]) + ': ');
                        }
                        if(Object.keys(object).length) {
                            hasdrugs = true;
                        }
                        for(var _key in object) {
                            var _object = {};
                            _object[_key] = object[_key]
                            clincialTrials.push(_object);
                            _object = null;
                        }
                    }
                }

                if(!hasdrugs) {
                    clincialTrials.push({'no_evidence': 'There are no investigational therapies that meet level 1, 2 or 3 evidence.'});
                }

                object = {};
                key = 'LEVEL OF EVIDENCE';
                value =  [
                    {'level 1': 'This alteration has been used as an eligibility criterion for clinical trials of this agent or class of agents.'},
                    {'level 2': 'There is clinical evidence for an association between this biomarker and response/resistance to this agent of class of agents in another tumor type only.'},
                    {'level 3': 'There is limited clinical evidence (early or conflicting data) for an association between this alteration and response/resistance to this agent or class of agents.'},
                    {'level 4': 'There is preclinical evidence linking unapproved biomarker to response.'},
                ];
                object[key] = value;
                clincialTrials.push(object);
            }
            return clincialTrials;
        }

        function constructAdditionalInfo() {
            var additionalInfo = [],
                key = '',
                value = [],
                object = {},
                cancerTypeInfo = $scope.relevantCancerType;

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
                value.push({'description': removeCharsInDescription(cancerTypeInfo.prevalence.description)});
                object[key] = value;
                additionalInfo.push(object);
            }

            if($scope.annotation.variant_effect) {
                value = [];
                key = 'MUTATION EFFECT';
                object = {};
                value.push({
                    'effect': $scope.annotation.variant_effect.effect,
                    'description': removeCharsInDescription($scope.annotation.variant_effect.description)
                });
                object[key] = value;
                additionalInfo.push(object);
            }
            return additionalInfo;
        }

        function removeCharsInDescription(str) {
            if(typeof str !== 'undefined') {
                return str.replace(/(\r\n|\n|\r)/gm,'');
            }else {
                return '';
            }
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