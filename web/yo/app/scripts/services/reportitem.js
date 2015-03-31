'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.reportItem
 * @description
 * # reportItem
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
  .service('reportItem', function (geneName, mutation, tumorType, annotation, relevantCancerType) {
    this.geneName = geneName || 'N/A';
    this.mutation = mutation || 'N/A';
    this.tumorType = tumorType || 'N/A';
    this.annotation = annotation || 'N/A';
    this.relevantCancerType = relevantCancerType || {};
    this.overallInterpretation = this.geneName + ' ' + this.mutation + ' SUMMARY\n' + this.annptation.annotation_summary || 'None.';
    this.alterType = 'MUTATION';
    this.mutationFreq = 'N/A';
    this.tumorTypeDrugs = 'N/A';
    this.nonTumorTypeDrugs = 'N/A';
    this.hasClinicalTrial = 'N/A';
    this.treatment = 'None.';
    this.fdaApprovedInTumor = 'None.';
    this.fdaApprovedInOtherTumor = 'None.';
    this.clinicalTrials = 'None.';
    this.additionalInfo = 'None.';

    this.init = function(){
      getClinicalTrial();
      getTreatment();
      getfdaInfo();
      getAdditionalInfo(annotation, geneName, alteration, tumorType, relevantCancerType);
    };

    this.getData =function(){
      var keys = ['overallInterpretation','geneName','mutation','alterType',
      'mutationFreq','tumorTypeDrugs','nonTumorTypeDrugs','hasClinicalTrial',
      'treatment','fdaApprovedInTumor','fdaApprovedInOtherTumor',
      'clinicalTrials','additionalInfo'];
      var value = {};

      for(var i = 0 ; i < keys.length; i++) {
        value[keys[i]] = this[keys[i]];
      }
      return value;
    };

    function findApprovedDrug(datum, object, tumorType, key, valueExtend) {
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
                                _key+=_drug.name + ' + ';
                            }
                        }
                    }
                    _key = _key.substr(0, _key.length-3);
                    _key += ', ';
                }
            }

            _key = _key.substr(0, _key.length-2);
            
            if(valueExtend !== undefined) {
                _key += valueExtend;
            }
            
            while(object.hasOwnProperty(_key)) {
                _key+=specialKeyChars;
            }
                    
            if(_subDatum.level_of_evidence_for_patient_indication && _subDatum.level_of_evidence_for_patient_indication.level) {
                _level = _subDatum.level_of_evidence_for_patient_indication.level;
                _obj['Level of evidence'] = isNaN(_level)?_level.toUpperCase():_level;
            }
            if(checkDescription(_subDatum)) {
                _obj.description = _subDatum.description;
            }
            if(typeof tumorType !== 'undefined' && tumorType !== '') {
                _obj['Cancer Type']= tumorType;
            }
            object[_key] = [_obj];
        }

        return object;
    }

    function findByLevelEvidence(datum, object, tumorType, key, valueExtend) {
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
                                _key+=_drug.name + ' + ';
                        }
                    }

                    _key = _key.substr(0, _key.length-3);
                    _key += ', ';
                }
            }

            _key = _key.substr(0, _key.length-2);
            
            if(valueExtend !== undefined) {
                _key += valueExtend;
            }
            
            while(object.hasOwnProperty(_key)) {
                _key+=specialKeyChars;
            }

            if(_subDatum.level_of_evidence_for_patient_indication && _subDatum.level_of_evidence_for_patient_indication.level) {
                _level = _subDatum.level_of_evidence_for_patient_indication.level;
                _obj['Level of evidence'] = isNaN(_level)?_level.toUpperCase():_level;
            }
            if(checkDescription(_subDatum)) {
                _obj.description = _subDatum.description;
            }
            if(typeof tumorType !== 'undefined' && tumorType !== '') {
                _obj['Cancer Type']= tumorType;
            }
            object[_key] = [_obj];
        }
        return object;
    }

    function displayProcess(str) {
        var specialUpperCasesWords = ['NCCN'];
        var specialLowerCasesWords = ['of', 'for', 'to'];

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
    
    //Is not relevant cancer type
    function isNRCT(relevent, type) {
        if(typeof relevent === 'object') {
            if(relevent instanceof Array) {
                for(var i=0; i<relevent.length; i++) {
                    if(relevent[i]['Cancer type'] === type) {
                        return false;
                    }
                }
                return true;
            }else {
                if(relevent.type === type) {
                    return false;
                }else {
                    return true;
                }
            }
        }else {
            return null;
        }
    }

    function addRecord(keys, value, array) {
        if(Array.isArray(value)) {
            value.forEach(function(e) {
                var _obj = {};
                keys.array.forEach(function(e1) {
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
    
    function checkDescription(datum) {
        if(datum && datum.hasOwnProperty('description') && (angular.isString(datum.description) || datum.description instanceof Array)) {
            return true;
        }else {
            return false;
        }
    }

    function removeCharsInDescription(str) {
        if(typeof str !== 'undefined') {
            str = str.trim();
            str = str.replace(/(\r\n|\n|\r)/gm,'');
            str = str.replace(/(\s\s*)/g,' ');
            return str;
        }else {
            return '';
        }
    }
    
    function getAdditionalInfo() {
        var additionalInfo = [],
            key = '',
            value = [],
            object = {},
            this.relevantCancerType = relevantCancerType || {};

        if(this.annptation.gene_annotation && checkDescription(this.annptation.gene_annotation)) {
            value = [];
            key = 'BACKGROUND';
            object = {};
            value.push({'description': removeCharsInDescription(this.annptation.gene_this.annptation.description)});
            object[key] = value;
            additionalInfo.push(object);
        }

        if(this.relevantCancerType.prevalence) {
            value = [];
            key = 'MUTATION PREVALENCE';
            object = {};
            object[key] = addRecord({'array': ['Cancer type', 'value'], 'object':'description'}, this.relevantCancerType.prevalence.description, value);
            additionalInfo.push(object);
        }

        if(this.annptation.variant_effect) {
            value = [];
            key = 'MUTATION EFFECT';
            object = {};
            value.push({
                'effect': this.annptation.variant_effect.effect || '',
                'description': this.annptation.variant_effect.description? removeCharsInDescription(this.annptation.variant_effect.description) : ''
            });
            object[key] = value;
            additionalInfo.push(object);
        }
        return additionalInfo;
    }
    
    function getClinicalTrial() {
        var clincialTrials = [],
            key = '',
            value = [],
            object = {},
            attrsToDisplay = ['resistant_to', 'sensitive_to'];

        if(this.relevantCancerType.clinical_trial) {
            var _datum = this.relevantCancerType.clinical_trial;

            value = [];
            object = {};
            key = 'CLINICAL TRIALS MATCHED FOR GENE AND DISEASE';

            for(var i=0, _datumL = _datum.length; i < _datumL; i++) {
                    var _subDatum = {},
                        _phase = _datum[i].phase || '';

                    if(_phase.indexOf('/') !== -1 && _phase !== 'N/A') {
                        var _phases = _phase.split('/');
                        /* jshint -W083 */
                        _phases.forEach(function(e, i, array){
                            array[i] = e.replace(/phase/gi, '').trim();
                        });
                        /* jshint +W083 */
                        _phase = 'Phase ' + _phases.sort().join('/');
                    }
                    _subDatum.trial = _datum[i].trial_id + (_phase!==''?(', ' + _phase):'');
                    _subDatum.title = _datum[i].title;
                    if(checkDescription(_subDatum)) {
                        _subDatum.description = removeCharsInDescription(_datum[i].description);
                    }
                    value.push(_subDatum);
            }
            
            object[key] = value;
            clincialTrials.push(object);
        }

        if(this.relevantCancerType.investigational_therapeutic_implications) {
            var hasdrugs = false;
            if(this.relevantCancerType.investigational_therapeutic_implications.general_statement) {
                value = [];
                object = {};
                key = 'INVESTIGATIONAL THERAPEUTIC IMPLICATIONS';
                object[key] = addRecord({'array': ['Cancer type', 'value'], 'object':'description'}, this.relevantCancerType.investigational_therapeutic_implications.general_statement.sensitivity.description, value);
            
                clincialTrials.push(object);
            }else if(Object.keys(this.relevantCancerType.investigational_therapeutic_implications).length > 0){
                clincialTrials.push({'INVESTIGATIONAL THERAPEUTIC IMPLICATIONS': []});
            }
            
            for (var j = 0; j < attrsToDisplay.length; j++) {
                if(this.relevantCancerType.investigational_therapeutic_implications[attrsToDisplay[j]]) {
                    object = {};
                    if(attrsToDisplay[j] === 'sensitive_to') {
                        object = findByLevelEvidence(this.relevantCancerType.investigational_therapeutic_implications[attrsToDisplay[j]], object);
                    }else if(attrsToDisplay[j] === 'resistant_to'){
                        object = findByLevelEvidence(this.relevantCancerType.investigational_therapeutic_implications[attrsToDisplay[j]], object, '', '', ' (Resistance)');
                    }else {
                        object = findByLevelEvidence(this.relevantCancerType.investigational_therapeutic_implications[attrsToDisplay[j]], object, '', displayProcess(attrsToDisplay[j]) + ': ');
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
                if(!this.relevantCancerType.investigational_therapeutic_implications.general_statement) {
                    value = [];
                    object = {};
                    key = 'INVESTIGATIONAL THERAPEUTIC IMPLICATIONS';
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
                {'level 1': 'FDA-approved biomarker and drug associatio = '';n in this indication.'},
                {'level 2A': 'FDA-approved biomarker and drug association in another indication, and NCCN-compendium listed = ''; for this indication.'},
                {'level 2B': 'FDA-approved biomarker in another indication, but not FDA or NCCN-compendium-listed = ''; for this indication.'},
                {'level 3': 'Clinical evidence l = '';inks this biomarker to drug response but no FDA-approved or NCCN compendium-listed biomarker and drug association.'},
                {'level 4': 'Preclinical evidence potentially l = '';inks this biomarker to response but no FDA-approved or NCCN compendium-listed biomarker and drug association.'}
            ];
            object[key] = value;
            clincialTrials.push(object);
        }
        this.hasClinicalTrial = 'NO';
        if(_clinicalTrail.length > 0) {
            for(var i =0; i < _clinicalTrail.length; i++) {
                if(_clinicalTrail[i].hasOwnProperty('CLINICAL TRIALS MATCHED FOR GENE AND DISEASE') && 
                    _clinicalTrail[i]['CLINICAL TRIALS MATCHED FOR GENE AND DISEASE'].length > 0) {
                    this.hasClinicalTrial = 'YES';
                    break;
                }
            }
            this.clinicalTrials = _clinicalTrail;
        }else {
            this.clinicalTrials = 'None.';
        }
        
        if(this.hasClinicalTrial === 'NO') {
            this.annptation.annotation_summary = this.annptation.annotation_summary.toString().replace('Please refer to the clinical trials section.', '');
        }

        return clincialTrials;
    }

    function getTreatment() {
        var treatment = [],
            key = '',
            value = [],
            object = {},
            description = '';

        if(this.annptation.annotation_summary) {
            key = geneName + ' ' + mutation + ' SUMMARY';
            value.push({'description': this.annptation.annotation_summary});
            object[key] = value;
            treatment.push(object);
        }

        if(this.relevantCancerType.nccn_guidelines) {
            var _datum = this.relevantCancerType.nccn_guidelines;
            var _datumL = _datum.length;
            var i = 0;
            var versions = {};

            value = [];
            object = {};
            key = 'NCCN GUIDELINES';
            for(i=0; i < _datumL; i++) {
                if(!versions.hasOwnProperty(_datum[i].version)) {
                    versions[_datum[i].version] = {};
                }
                if(checkDescription(_datum[i])) {
                    versions[_datum[i].version]['recommendation category'] = _datum[i].description;
                }
            }

            for(var versionKey in versions) {
                var version = versions[versionKey];
                version.nccn_special = 'Version: ' + versionKey + ', Cancer type: ' + tumorType;
                value.push(version);
            }
            
            object[key] = value;
            treatment.push(object);
        }
        
        if(this.relevantCancerType.standard_therapeutic_implications && this.relevantCancerType.standard_therapeutic_implications.general_statement && checkDescription(this.relevantCancerType.standard_therapeutic_implications.general_statement.sensitivity)) {
            description = this.relevantCancerType.standard_therapeutic_implications.general_statement.sensitivity.description;
            value = [];
            object = {};
            key = 'STANDARD THERAPEUTIC IMPLICATIONS';
            if(typeof description === 'string') {
                description = description.trim();
            }
            value.push({'description': this.relevantCancerType.standard_therapeutic_implications.general_statement.sensitivity.description});
            object[key] = value;
            treatment.push(object);
        }
        
        if(this.relevantCancerType.prognostic_implications && checkDescription(this.relevantCancerType.prognostic_implications)) {
            description = this.relevantCancerType.prognostic_implications.description;
            value = [];
            key = 'PROGNOSTIC IMPLICATIONS';
            object = {};
            if(angular.isString(description)) {
                description = description.trim();
            }else {
                if(angular.isArray(description)){
                    var str = [];
                    description.forEach(function(e){
                        if(e['Cancer type'].toString().toLowerCase() === 'all tumors' && str.length > 0) {
                            str.unshift(e.value.toString().trim());
                        }else {
                            str.push(e.value.toString().trim());
                        }
                    });
                    description = str.join(' ');
                }else{
                    description = '';
                    console.log('PROGNOSTIC IMPLICATIONS --- not string --- not array');
                }
            }
            value.push({'description': description});
            object[key] = value;
            treatment.push(object);
        }

        this.treatment = _treatment.length > 0 ? _treatment : 'None.';
        return treatment;
    }

    function getfdaInfo() {
        var fdaApproved = [],
            fdaApprovedInOther = [],
            object = {},
            attrsToDisplay = ['sensitive_to', 'resistant_to'],
            i =0;

        if(this.relevantCancerType.standard_therapeutic_implications) {
            for (i = 0; i < attrsToDisplay.length; i++) {
                if(this.relevantCancerType.standard_therapeutic_implications[attrsToDisplay[i]]) {
                    var _datum = this.relevantCancerType.standard_therapeutic_implications[attrsToDisplay[i]];

                    object = {};
                    if(attrsToDisplay[i] === 'sensitive_to') {
                        object = findApprovedDrug(_datum, object);
                    }else if(attrsToDisplay[i] === 'resistant_to'){
                        object = findByLevelEvidence(_datum, object, '', '', ' (Resistance)');
                    }else {
                        object = findApprovedDrug(_datum, object, '', displayProcess(attrsToDisplay[i]) + ': ');
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

        if(this.annptation.cancer_type && relevantCancerType && relevantCancerType.$type) {
            object = {};

            for (i = 0; i < this.annptation.cancer_type.length; i++) {
                if(isNRCT(relevantCancerType.$type, this.annptation.cancer_type[i].$type)) {
                    if(this.annptation.cancer_type[i].standard_therapeutic_implications) {
                        for (var j = 0; j < attrsToDisplay.length; j++) {
                            if(this.annptation.cancer_type[i].standard_therapeutic_implications[attrsToDisplay[j]]) {
                                /* jshint -W004 */
                                var _datum = this.annptation.cancer_type[i].standard_therapeutic_implications[attrsToDisplay[j]];
                                if(attrsToDisplay[j] === 'sensitive_to') {
                                    object = findApprovedDrug(_datum, object, this.annptation.cancer_type[i].$type);
                                }else if(attrsToDisplay[j] === 'resistant_to'){
                                    object = findByLevelEvidence(_datum, object, '', '', ' (Resistance)');
                                }else {
                                    object = findApprovedDrug(_datum, object, this.annptation.cancer_type[i].$type, attrsToDisplay[j] + ': ');
                                }
                                /* jshint +W004 */
                            }
                        }
                    }
                }
            }

            /* jshint -W004 */
            for(var _key in object) {
                var _object = {};
                _object[_key] = object[_key];

                for(i = 0; i < _object[_key].length; i++ ) {
                    delete _object[_key][i]['Level of evidence'];
                    delete _object[_key][i].description;
                }

                fdaApprovedInOther.push(_object);
                _object = null;
            }
            /* jshint +W004 */
        }
        if(_fdaInfo.approved.length > 0) {
          this.tumorTypeDrugs = 'YES';
          this.fdaApprovedInTumor = _fdaInfo.approved;
      }else {
          this.tumorTypeDrugs = 'NO';
          this.fdaApprovedInTumor = 'None.';
      }
      if(_fdaInfo.approvedInOther.length > 0) {
          this.nonTumorTypeDrugs = 'YES';
          this.fdaApprovedInOtherTumor = _fdaInfo.approvedInOther;
      }else {
          this.nonTumorTypeDrugs = 'NO';
          this.fdaApprovedInOtherTumor = 'None.';
      }

        return {'approved': fdaApproved, 'approvedInOther': fdaApprovedInOther};
    }

  });
