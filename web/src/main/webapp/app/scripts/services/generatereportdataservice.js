'use strict';

/**
 * @ngdoc service
 * @name webappApp.GenerateReportDataService
 * @description
 * # GenerateReportDataService
 * Factory in the webappApp.
 */
angular.module('webappApp')
  .factory('GenerateReportDataService', function () {
    var specialKeyChars = '#$%';
    
    function generateReportData(geneName, alteration, tumorType, relevantCancerType, annotation) {
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

        params.overallInterpretation = (geneName + ' ' + alteration + ' SUMMARY\n' + 
            annotation.annotation_summary + 
            '\nOTHER GENES\nNo additional somatic mutations were detected in this patient sample in the other sequenced gene regions.') || '';
        params.geneName = geneName;
        params.mutation = alteration;
        params.diagnosis = tumorType;
        params.tumorTissueType = params.diagnosis;
        var _treatment = constructTreatment(annotation, geneName, alteration, tumorType, relevantCancerType);
        params.treatment = _treatment.length > 0 ? _treatment : "";
        var _fdaInfo = constructfdaInfo(annotation, geneName, alteration, tumorType, relevantCancerType);
        params.fdaApprovedInTumor = _fdaInfo.approved.length > 0 ? _fdaInfo.approved : "";
        params.fdaApprovedInOtherTumor = _fdaInfo.nonApproved.length > 0 ? _fdaInfo.nonApproved : "";
        var _clinicalTrail = constructClinicalTrial(annotation, geneName, alteration, tumorType, relevantCancerType);
        params.clinicalTrials = _clinicalTrail.length > 0 ? _clinicalTrail : "";
        var _additionalInfo = constructAdditionalInfo(annotation, geneName, alteration, tumorType, relevantCancerType);
        params.additionalInfo = _additionalInfo.length > 0 ? _additionalInfo : "";
        
        return params;
    }


    function constructTreatment(annotation, geneName, mutation, tumorType, relevantCancerType) {
        var treatment = [],
            key = '',
            value = [],
            object = {},
            cancerTypeInfo = relevantCancerType || {};

        if(annotation.annotation_summary) {
            key = geneName + ' ' + mutation + " SUMMARY";
            value.push({'description': annotation.annotation_summary});
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
                versions[_datum[i].version]['recommendation category'] = _datum[i].description;
            }

            for(var versionKey in versions) {
                var version = versions[versionKey];
                version.nccn_special = 'Version: ' + versionKey + ', Cancer type: ' + tumorType;
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
                _key = '',
                _obj = {},
                _level;

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

            if(_subDatum.level_of_evidence_for_patient_indication && _subDatum.level_of_evidence_for_patient_indication.level) {
                _level = _subDatum.level_of_evidence_for_patient_indication.level;
                _obj['Level of evidence'] = isNaN(_level)?_level.toUpperCase():_level;
            }
            if(_subDatum.description) {
                _obj.description = _subDatum.description;
            }
            if(typeof tumorType !== "undefined" && tumorType !== "") {
                _obj['Cancer Type']= tumorType;
            }
            object[_key] = [_obj];
        }

        return object;
    }

    function findByLevelEvidence(datum, object, tumorType, key) {
        for(var m=0, datumL = datum.length; m < datumL; m++) {
            var _subDatum = datum[m],
                _key = '',
                _level;

            if(typeof key !== 'undefined') {
                _key = key;
            }

            if(_subDatum.level_of_evidence_for_patient_indication 
                && _subDatum.level_of_evidence_for_patient_indication.level) {

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
                        _key+=specialKeyChars;
                    }
                    _level = _subDatum.level_of_evidence_for_patient_indication.level;
                    object[_key] = [{
                        'Level of evidence': isNaN(_level)?_level.toUpperCase():_level,
                        'Description': _subDatum.description
                    }];
                    
                }
            }
        return object;
    }

    function displayProcess(str) {
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
    }

    function constructfdaInfo(annotation, geneName, mutation, tumorType, relevantCancerType) {
        var fdaApproved = [],
            fdaNonApproved = [],
            object = {},
            cancerTypeInfo = relevantCancerType || {},
            attrsToDisplay = ['sensitive_to', 'resistant_to'];

        if(cancerTypeInfo.standard_therapeutic_implications) {
            for (var i = 0; i < attrsToDisplay.length; i++) {
                if(cancerTypeInfo.standard_therapeutic_implications[attrsToDisplay[i]]) {
                    var _datum = cancerTypeInfo.standard_therapeutic_implications[attrsToDisplay[i]];
                    
                    object = {};
                    if(attrsToDisplay[i] === 'sensitive_to') {
                        object = findApprovedDrug(_datum, object);
                    }else {
                        object = findApprovedDrug(_datum, object, '', attrsToDisplay[i] + ': ');
                    }

                    for(var _key in object) {
                        var _object = {};
                        _object[_key] = object[_key];
                        fdaApproved.push(_object);
                        _object = null;
                    }
                }
            }
        }
        
        if(annotation.cancer_type && relevantCancerType && relevantCancerType.type) {
            object = {};

            for (var i = 0; i < annotation.cancer_type.length; i++) {
                if(annotation.cancer_type[i].type !== relevantCancerType.type) {
                    if(annotation.cancer_type[i].standard_therapeutic_implications) {
                        for (var j = 0; j < attrsToDisplay.length; j++) {
                            if(annotation.cancer_type[i].standard_therapeutic_implications[attrsToDisplay[j]]) {
                                var _datum = annotation.cancer_type[i].standard_therapeutic_implications[attrsToDisplay[j]];
                                if(attrsToDisplay[j] === 'sensitive_to') {
                                    object = findApprovedDrug(_datum, object, annotation.cancer_type[i].type);
                                }else {
                                    object = findApprovedDrug(_datum, object, annotation.cancer_type[i].type, attrsToDisplay[j] + ': ');
                                }
                            }
                        }
                    }
                }
            }

            for(var _key in object) {
                var _object = {};
                _object[_key] = object[_key];
                fdaNonApproved.push(_object);
                _object = null;
            }
        }
        return {'approved': fdaApproved, 'nonApproved': fdaNonApproved};
    }

    function constructClinicalTrial(annotation, geneName, mutation, tumorType, relevantCancerType) {
        var clincialTrials = [],
            key = '',
            value = [],
            object = {},
            cancerTypeInfo = relevantCancerType || {},
            attrsToDisplay = ['sensitive_to', 'resistant_to'];

        if(cancerTypeInfo.clinical_trial) {
            var _datum = cancerTypeInfo.clinical_trial;

            value = [];
            object = {};
            key = "CLINICAL TRIALS MATCHED FOR GENE AND DISEASE";

            for(var i=0, _datumL = _datum.length; i < _datumL; i++) {
                    var _subDatum = {},
                        _phase = _datum[i].phase.toString().trim() || '';

                    if(_phase.indexOf('/') !== -1 && _phase !== 'N/A') {
                        var _phases = _phase.split('/');
                        _phases.forEach(function(e, i, array){
                            array[i] = e.replace(/phase/gi, '').trim();
                        });
                         
                        _phase = 'Phase ' + _phases.sort().join('/');
                    }
                    _subDatum.trial = _datum[i].trial_id + (_phase!==''?(', ' + _phase):'');
                    _subDatum.title = _datum[i].title;
                    _subDatum.description = removeCharsInDescription(_datum[i].description);
                    value.push(_subDatum);
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
                object[key] = addRecord({'array': ['Cancer type', 'value'], 'object':'description'}, cancerTypeInfo.investigational_therapeutic_implications.general_statement.sensitivity.description, value);
            
                clincialTrials.push(object);
            }else if(Object.keys(cancerTypeInfo.investigational_therapeutic_implications).length > 0){
                clincialTrials.push({"INVESTIGATIONAL THERAPEUTIC IMPLICATIONS": []});
            }
            
            for (var j = 0; j < attrsToDisplay.length; j++) {
                if(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]]) {
                    object = {};
                    if(attrsToDisplay[j] === 'sensitive_to') {
                        object = findByLevelEvidence(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]], object);
                    }else {
                        object = findByLevelEvidence(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]], object, '', attrsToDisplay[j] + ': ');
                    }
                    if(Object.keys(object).length > 0) {
                        hasdrugs = true;
                    }
                    for(var _key in object) {
                        var _object = {},
                            _newKey = _key.replace(specialKeyChars, '');
                        _object[_newKey] = object[_key];
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
                {'level 2A': 'FDA-approved biomarker in unapproved indication with NCCN-guideline listing.'},
                {'level 2B': 'FDA-approved biomarker in unapproved indication.'},
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
    
    function constructAdditionalInfo(annotation, geneName, mutation, tumorType, relevantCancerType) {
        var additionalInfo = [],
            key = '',
            value = [],
            object = {},
            cancerTypeInfo = relevantCancerType || {};

        if(annotation.gene_annotation) {
            value = [];
            key = 'BACKGROUND';
            object = {};
            value.push({'description': removeCharsInDescription(annotation.gene_annotation.description)});
            object[key] = value;
            additionalInfo.push(object);
        }

        if(cancerTypeInfo.prevalence) {
            value = [];
            key = 'MUTATION PREVALENCE';
            object = {};
            object[key] = addRecord({'array': ['Cancer type', 'value'], 'object':'description'}, cancerTypeInfo.prevalence.description, value);
            additionalInfo.push(object);
        }

        if(annotation.variant_effect) {
            value = [];
            key = 'MUTATION EFFECT';
            object = {};
            value.push({
                'effect': annotation.variant_effect.effect || '',
                'description': annotation.variant_effect.description? removeCharsInDescription(annotation.variant_effect.description) : ''
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
    // Public API here
    return {
      init: generateReportData
    };
  });
