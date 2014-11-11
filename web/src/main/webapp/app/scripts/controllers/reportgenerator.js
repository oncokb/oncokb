'use strict';

/**
 * @ngdoc function
 * @name webappApp.controller:ReportgeneratorCtrl
 * @description
 * # ReportgeneratorCtrl
 * Controller of the webappApp
 */
angular.module('webappApp')
  .controller('ReportgeneratorCtrl', ['$scope', '$timeout' ,'FileUploader', 'SearchVariant', 'GenerateDoc', function($scope, $timeout, FileUploader, SearchVariant, GenerateDoc) {
    var changedAttr = ['cancer_type', 'nccn_guidelines', 'clinical_trial', 'sensitive_to', 'resistant_to', 'treatment', 'drug'];
        
        $scope.init = function() {
            $scope.sheets = {
                    length: 0,
                    attr: ['sheet1'],
                    arr: {}
            };
            $scope.progress = {
                value: 0,
                dynamic: 0,
                max: 0
            };
            $scope.regerating = false;
            $scope.generateIndex = -1;
    };
        
    $scope.generate = function() {
        var worker = [],
                    i = -1;
        for(var sheet in $scope.sheets.arr) {
                    $scope.sheets.arr[sheet].forEach(function(e, i) {
                        var datum = {};
                        for(var key in e) {
                            if(e.hasOwnProperty(key) && key !== '$$hashkey') {
                                datum[key] = e[key];
                            }
                        }
                        worker.push(datum);
                    });
        }
            $scope.workers = worker;
            generateReports();
    };

    $scope.validate = function(key, content) {

        if(/{tumor|cancer}\stype/i.test(key)){

        }else if(key.toLowerCase().indexOf('alteration') !== -1){
            content = alterationProcee(content);
        }

        return content;
    };
        
        function generateReports() {
            console.log("run ---");
            $timeout(function () {
                if(!$scope.regerating) {
                    $scope.generateIndex++; 
                    if ($scope.generateIndex < $scope.workers.length) { 
                        var worker = $scope.workers[$scope.generateIndex];
                        $scope.regerating = true;
                        getAnnotation(alterationProcee(worker.gene), alterationProcee(worker.alteration), alterationProcee(worker.tumorType));
                    }
                }else {
                    generateReports();
                }
            }, 500);
        }
        
    function alterationProcee(alteration) {
        if(alteration.indexOf('p.') === 0) {
            alteration = alteration.slice(2);
        }

        if(alteration.indexOf('Ter')) {
            alteration = alteration.replace('Ter', '*');
        }
        return alteration;
    }

    function formatDatum(value, key) {
        if(isArray(value) || (!isArray(value) && isObject(value) && changedAttr.indexOf(key) !== -1)) {
            if(!isArray(value) && isObject(value) && changedAttr.indexOf(key) !== -1) {
                value = [value];
            }

            for (var i = 0; i < value.length; i++) {
                value[i] = formatDatum(value[i], i);
            }
        }else if(isObject(value)) {
            for(var _key in value) {
                value[_key] = formatDatum(value[_key], _key);
            }
        }
        
        return value;
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

    function generateReportData(geneName, alteration, tumorType, relevantCancerType,annotation) {
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
        params.fdaApprovedInOtherTumor = _fdaInfo.nonApproved.length > 0 ? _fdaInfo.approved : "";
        var _clinicalTrail = constructClinicalTrial(annotation, geneName, alteration, tumorType, relevantCancerType);
        params.clinicalTrials = _clinicalTrail.length > 0 ? _clinicalTrail : "";
        var _additionalInfo = constructAdditionalInfo(annotation, geneName, alteration, tumorType, relevantCancerType);
        params.additionalInfo = _additionalInfo.length > 0 ? _additionalInfo : "";
        
        return params;
    }
    function getAnnotation(gene, alteration, tumorType) {
        var params = {
            'alterationType': 'MUTATION',
            'hugoSymbol': gene,
            'alteration': alteration,
            'tumorType': tumorType
        };

//      SearchVariant.annotationFromFile(params).success(function(data) {
         SearchVariant.getAnnotation(params).success(function(data) {
            var annotation = {};

            annotation = xml2json.parser(data).xml;

            for(var key in annotation) {
                annotation[key] = formatDatum(annotation[key], key);
            }

            if(annotation.cancer_type) {
                var relevantCancerTypeArray = [];
                var relevantCancerType;

                for(var i=0, cancerTypeL = annotation.cancer_type.length; i < cancerTypeL; i++) {
                    var _cancerType = annotation.cancer_type[i];
                    if(_cancerType.relevant_to_patient_disease.toLowerCase() === 'yes') {
                        relevantCancerTypeArray.push(_cancerType);
                    }
                }
                if(relevantCancerTypeArray.length > 1) {
                    relevantCancerType = relevantCancerTypeArray[0];

                    for(var i=1, relevantL=relevantCancerTypeArray.length; i < relevantL; i++) {
                        relevantCancerType = deepmerge(relevantCancerType, relevantCancerTypeArray[i], relevantCancerType.type, relevantCancerTypeArray[i].type);
                    }
                }else if(relevantCancerTypeArray.length === 1){
                    relevantCancerType = relevantCancerTypeArray[0];
                }else {
                    relevantCancerType = null;
                }
            }

            var reportParams = generateReportData(params.hugoSymbol, params.alteration, params.tumorType, relevantCancerType,annotation);
            reportParams.email = 'jackson.zhang.828@gmail.com';
            
            GenerateDoc.getDoc(reportParams).success(function(data) {
                $scope.regerating = false;
                $scope.progress.dynamic += 1;
                $scope.progress.value = $scope.progress.dynamic / $scope.progress.max * 100;
                generateReports();
            });
        });
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
                versions[_datum[i].version]['recommendation category ' + _datum[i].recommendation_category] = _datum[i].description;
            }
            
            for(var versionKey in versions) {
                var version = versions[versionKey];
                version.version.nccn_special = 'Version: ' + versionKey + ', Cancer type: ' + tumorType;
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
                        
                        _key = _key.substr(0, _key.length-3);
                        _key += " & ";
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
                        object = findApprovedDrug(_datum, object, '', displayProcess(attrsToDisplay[i]) + ': ');
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
                                    object = findApprovedDrug(_datum, object, annotation.cancer_type[i].type, displayProcess(attrsToDisplay[j]) + ': ');
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
                    var _subDatum = {};
                    _subDatum.trial = _datum[i].trial_id + ", " + _datum[i].phase;
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
                object[key] = addRecord({'array': ['cancer_type', 'value'], 'object':'description'}, cancerTypeInfo.investigational_therapeutic_implications.general_statement.sensitivity.description, value);
            
                clincialTrials.push(object);
            }
            
            for (var j = 0; j < attrsToDisplay.length; j++) {
                if(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]]) {
                    object = {};
                    if(attrsToDisplay[j] === 'sensitive_to') {
                        object = findByLevelEvidence(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]], object);
                    }else {
                        object = findByLevelEvidence(cancerTypeInfo.investigational_therapeutic_implications[attrsToDisplay[j]], object, '', displayProcess(attrsToDisplay[j]) + ': ');
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
            object[key] = addRecord({'array': ['cancer_type', 'value'], 'object':'description'}, cancerTypeInfo.prevalence.description, value);
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
    var uploader = $scope.uploader = new FileUploader();

    uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    uploader.onAfterAddingFile  = function(fileItem) {
        console.info('onAfterAddingFile', fileItem);
        var reader = new FileReader();
        reader.onload = function(e) {
            var data = e.target.result;

            /* if binary string, read with type 'binary' */
            var workbook = XLSX.read(data, {type: 'binary'});
            var fileValue = {};
            var fileAttrs = {};
                var totalRecord = 0;
            for (var i=0, workbookSheetsNum = workbook.SheetNames.length; i < workbookSheetsNum; i++) {
                var attrL = 0,
                    sheetName = workbook.SheetNames[i];
                
                var json = XLSX.utils.sheet_to_json(workbook.Sheets[workbook.SheetNames[i]]);
                
                fileValue[sheetName] = [];
                fileAttrs[sheetName] = ['gene', 'alteration', 'tumorType'];
                json.forEach(function(e,i){
                    var datum = {
                        gene: '',
                        alteration: '',
                        tumorType: ''
                    };

                    for(var key in e) {
                        if (e.hasOwnProperty(key)) {
                            if(/gene/i.test(key)) {
                                datum.gene = e[key];
                            }else if(/alteration/i.test(key)) {
                                datum.alteration = e[key];
                            }else if(/tumor/i.test(key)) {
                                datum.tumorType = e[key];
                            }
                        }
                    }
                    fileValue[sheetName].push(datum);
                });
                        totalRecord += fileValue[sheetName].length;
            }
            $scope.sheets.length = workbookSheetsNum;
            $scope.sheets.attr = fileAttrs;
            $scope.sheets.arr = fileValue;
                $scope.progress.dynamic = 0;
                $scope.progress.value = 0;
                $scope.progress.max = totalRecord;
            $scope.$apply();
            /* DO SOMETHING WITH workbook HERE */
        };
        reader.readAsBinaryString(fileItem._file);
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

  }]);
