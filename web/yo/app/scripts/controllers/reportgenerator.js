'use strict';

/**
 * @ngdoc function
 * @name oncokb.controller:ReportgeneratorCtrl
 * @description
 * # ReportgeneratorCtrl
 * Controller of the oncokb
 */
angular.module('oncokbApp')
  .controller('ReportgeneratorCtrl', [
    '$scope',
    '$timeout' ,
    'FileUploader',
    'dialogs',
    'DatabaseConnector',
    'GenerateReportDataService',
    'DeepMerge',
    'x2js',
    'FindRegex',
    'OncoKB',
    'S',
    'Levenshtein',
    'XLSX',
    function(
        $scope,
        $timeout,
        FileUploader,
        dialogs,
        DatabaseConnector,
        ReportDataService,
        DeepMerge,
        x2js,
        FindRegex,
        OncoKB,
        S,
        Levenshtein,
        XLSX) {
    var changedAttr = ['cancer_type', 'nccn_guidelines', 'clinical_trial', 'sensitive_to', 'resistant_to', 'treatment', 'drug'];
    var uploader; //$scope.uploader -- selected file handler
    var noMatchSeperator = '(Not exists)';
    
    
    
    function generate(workers) {
        $scope.workers = workers;
        $scope.working = true;
        $scope.progress.value = 0.5;
        $scope.progress.dynamic = 0;
        $scope.progress.max = workers.length;
        $scope.generateIndex = -1;
        generateReports();
    }
    
    function getWorkers(flag) {
        var workers = [],
            workerKeys = ['gene', 'alteration', 'tumorType'];
        /* jshint -W083 */
        for(var sheet in $scope.sheets.arr) {
            $scope.sheets.arr[sheet].forEach(function(e, i) {
                var datum = {};
                    workerKeys.forEach(function(e1) {
                        if(e.hasOwnProperty(e1)) {
                            datum[e1] = e[e1].replace(noMatchSeperator, '').trim();
                        }
                    });
                    datum.sheet = sheet;
                    datum.id = i;

                if(typeof flag === 'string' && flag==='regenerate') {
                    if(Number(e.generated) === -1) {
                        workers.push(datum);
                    }
                }else {
                    workers.push(datum);
                }
            });
        }
        /* jshint +W083 */
        return workers;
    }
    
    function checkEmail() {
        if($scope.sheets.email === '') {
            dialogs.notify('','Please check your email address.');
            return false;
        }else {
            return true;
        }
    }
    
    function initParams() {
        $scope.sheets = {
                length: 0,
                attr: ['sheet1'],
                arr: {},
                folder: {},
                email: 'jackson.zhang.828@gmail.com'
        };
        $scope.progress = {
            value: 0,
            dynamic: 0,
            max: 0
        };
        //if file selected
        $scope.fileSelected = false;
        
        //default file type is xlsx
        $scope.isXML = false;
        $scope.isXLSX = false;
        
        //one worker running
        $scope.generating = false;
        
        //all workering running
        $scope.working = false;
        $scope.generateIndex = -1;
        
        $scope.hasFailed = false;

        if(OncoKB.global.genes && OncoKB.global.genes && OncoKB.global.tumorTypes) {
            $scope.genes = getUnique(angular.copy(OncoKB.global.genes), 'hugoSymbol');
            $scope.alterations = getUnique(angular.copy(OncoKB.global.alterations), 'name');
            $scope.tumorTypes = getUnique(angular.copy(OncoKB.global.tumorTypes), 'name');
        }else {
            DatabaseConnector.getGeneAlterationTumortype(function(data){
                OncoKB.global.genes = angular.copy(data.genes);
                OncoKB.global.alterations = angular.copy(data.alterations);
                OncoKB.global.tumorTypes = angular.copy(data.tumorTypes);

                $scope.genes = getUnique(data.genes, 'hugoSymbol');
                $scope.alterations = getUnique(data.alterations, 'name');
                $scope.tumorTypes = getUnique(data.tumorTypes, 'name');
            });
        }
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
    }

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

    function generateReports() {
        $timeout(function () {
            if(!$scope.generating) {
                $scope.generateIndex++; 
                if ($scope.generateIndex < $scope.workers.length) { 
                    var worker = $scope.workers[$scope.generateIndex];
                    $scope.sheets.arr[worker.sheet][worker.id].generating = true;
                    $scope.generating = true;
                    getAnnotation(worker);
                }else {
                    $scope.working = false;
                }
            }else {
                generateReports();
            }
        }, 500);
    }
        
    function trimAlteration(alteration) {
        if(S(alteration).startsWith('p.')) {
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
        
    function getAnnotation(worker) {
        var params = {
            'alterationType': 'MUTATION',
            'hugoSymbol': worker.gene,
            'alteration': worker.alteration,
            'tumorType': worker.tumorType
        };

        DatabaseConnector.searchAnnotation(params, function(data){
            searchAnnotation('success', data, worker);
        }, function(){
            searchAnnotation('fail');
        });
    }
    
    function searchAnnotation(status, data, worker) {
        var annotation = {};
        var reportParams = '';
        var relevantCancerTypeArray = [];
        var relevantCancerType = {};

        if(status === 'success') {
            annotation = processData(x2js.xml_str2json(data).xml);
            for(var key in annotation) {
                annotation[key] = formatDatum(annotation[key], key);
            }

            if(annotation.cancer_type) {
                var i = 0;

                for(var cancerTypeL = annotation.cancer_type.length; i < cancerTypeL; i++) {
                    var _cancerType = annotation.cancer_type[i];
                    if(_cancerType.$relevant_to_patient_disease.toLowerCase() === 'yes') {
                        relevantCancerTypeArray.push(_cancerType);
                    }
                }
                if(relevantCancerTypeArray.length > 1) {
                    relevantCancerType = relevantCancerTypeArray[0];
                    i = 0;
                    for(var relevantL=relevantCancerTypeArray.length; i < relevantL; i++) {
                        relevantCancerType = DeepMerge.init(relevantCancerType, relevantCancerTypeArray[i], relevantCancerType.$type, relevantCancerTypeArray[i].$type);
                    }
                }else if(relevantCancerTypeArray.length === 1){
                    relevantCancerType = relevantCancerTypeArray[0];
                }else {
                    relevantCancerType = null;
                }
            }

            reportParams = ReportDataService.init(worker.gene, worker.alteration, worker.tumorType, relevantCancerType,annotation);

            //Check email
            if($scope.sheets.email && $scope.sheets.email !== '') {
                reportParams.email = $scope.sheets.email;
            }else {
                reportParams.email = 'jackson.zhang.828@gmail.com';
            }

            //Check folder name
            if($scope.sheets.folder.hasOwnProperty(worker.sheet) && 
                    $scope.sheets.folder[worker.sheet].hasOwnProperty('name') && 
                    $scope.sheets.folder[worker.sheet].name && 
                    $scope.sheets.folder[worker.sheet].name !== '') {
                reportParams.folderName = $scope.sheets.folder[worker.sheet].name;
            }

            //Check folder ID
            if($scope.sheets.folder.hasOwnProperty(worker.sheet) && 
                    $scope.sheets.folder[worker.sheet].hasOwnProperty('id') && 
                    $scope.sheets.folder[worker.sheet].id && 
                    $scope.sheets.folder[worker.sheet].id !== '') {
                reportParams.folderId = $scope.sheets.folder[worker.sheet].id;
            }
            
            if(reportParams.hasOwnProperty('folderName') && 
                    reportParams.folderName && 
                    reportParams.folderName !== '' && 
                    !reportParams.hasOwnProperty('folderId')) {
                DatabaseConnector.createGoogleFolder({'folderName': reportParams.folderName}, function(data){
                    if(data !== '') {
                        $scope.sheets.folder[worker.sheet].id = data;
                        reportParams.folderId = $scope.sheets.folder[worker.sheet].id;
                        generateGoogleDoc(reportParams, worker);
                    }
                });
            }else {
                generateGoogleDoc(reportParams, worker);
            }
        }else {
            $scope.generating = false;
            $scope.progress.dynamic += 1;
            $scope.progress.value = $scope.progress.dynamic / $scope.progress.max * 100;
            $scope.sheets.arr[worker.sheet][worker.id].generating = false;
            $scope.sheets.arr[worker.sheet][worker.id].generated = -1;
            $scope.hasFailed = true;
            generateReports();
        }
    }
    
    function generateGoogleDoc(reportParams, worker) {
        DatabaseConnector.googleDoc(reportParams, function(){
            googleDocCallback(worker, 'success');
        }, function(){
            googleDocCallback(worker, 'fail');
        });
    }
        
    function googleDocCallback(worker, status) {
        $scope.generating = false;
        $scope.progress.dynamic += 1;
        $scope.progress.value = $scope.progress.dynamic / $scope.progress.max * 100;
        $scope.sheets.arr[worker.sheet][worker.id].generating = false;
        $scope.sheets.arr[worker.sheet][worker.id].generated = status==='success'?1:-1;
        if(!$scope.hasFailed) {
            $scope.hasFailed = status==='success'?false:true;
        }
        generateReports();
    }
    
    function processData(object) {
        if(isArray(object)) {
            object.forEach(function(e){
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
    
    function deleteItem(sheetName, sheetArrDatum) {
        $scope.sheets.arr[sheetName].forEach(function(e, i) {
            if(e.id === sheetArrDatum.id) {
                $scope.sheets.arr[sheetName].splice(i,1);
            }
        });
    }

    function check(datum, checkV, attr) {
        var similarity = [],
            same = false;

        for (var i = 0, length = $scope[checkV].length; i < length; i++) {
            var _datum;

            if(typeof attr !== 'undefined' && attr) {
                _datum = $scope[checkV][i][attr];
            }else {
                _datum = $scope[checkV][i];
            }
           

            if(datum.toString().toUpperCase() === _datum.toString().toUpperCase()) {
                datum = _datum;
                same = true;
                break;
            }else {
                var lavenshtein = new Levenshtein(datum, _datum);
                similarity.push(lavenshtein.distance);
            }
        }

        if(same) {
            return datum;
        }else {
            return datum + ' ' + noMatchSeperator;
        }
    }

    function readXLSXfile(file) {
        var reader = new FileReader();

        reader.onload = function(e) {
            var data = e.target.result;

            /* if binary string, read with type 'binary' */
            var workbook = XLSX.read(data, {type: 'binary'});
            var fileValue = {};
            var fileAttrs = {};
            var totalRecord = 0;

            for (var i=0, workbookSheetsNum = workbook.SheetNames.length; i < workbookSheetsNum; i++) {
                var sheetName = workbook.SheetNames[i];

                var json = XLSX.utils.sheet_to_json(workbook.Sheets[workbook.SheetNames[i]]);

                fileValue[sheetName] = [];
                fileAttrs[sheetName] = ['gene', 'alteration', 'tumorType'];
                /* jshint -W083 */
                json.forEach(function(e,i){
                    var datum = {
                        'id': sheetName + '-' + i,
                        'gene': '',
                        'alteration': '',
                        'tumorType': '',
                        'generated': 0, //0: hasn't been generated 1: successfully generated -1: unsuccessfully generated
                        'generating': false
                    };

                    for(var key in e) {
                        if (e.hasOwnProperty(key)) {
                            if(/gene/i.test(key)) {
                                var _gene = check(e[key], 'genes');
                                datum.gene = _gene;
                            }else if(/alteration/i.test(key)) {
                                var _alteration = check(trimAlteration(e[key]), 'alterations');
                                datum.alteration = _alteration;
                            }else if(/tumor/i.test(key)) {
                                var _tumorType = check(e[key], 'tumorTypes');
                                datum.tumorType = _tumorType;
                            }
                        }
                    }
                    fileValue[sheetName].push(datum);
                });
                /* jshint +W083 */
                totalRecord += fileValue[sheetName].length;
            }
            $scope.sheets.length = workbookSheetsNum;
            $scope.sheets.attr = fileAttrs;
            $scope.sheets.arr = fileValue;
            $scope.progress.dynamic = 0;
            $scope.progress.value = 0;
            $scope.progress.max = totalRecord;
            $scope.isXLSX = true;
            $scope.$apply();
        };

        reader.readAsBinaryString(file._file);
    }
    
    function readXMLfile(file) {
        var reader = new FileReader();

        reader.onload = function(e) {
            var full = x2js.xml_str2json(e.target.result);
            var reportViewDatas = [];
            var variants = [];
            if(angular.isArray(full.document.sample.test.variant)) {
                variants = full.document.sample.test.variant;
            }else {
                variants.push(full.document.sample.test.variant);
            }
            for(var k = 0 ;k < variants.length;k++){
                var annotation = processData(variants[k].allele.transcript);
                var relevantCancerType = {};
                for(var key in annotation) {
                    annotation[key] = formatDatum(annotation[key], key);
                }
                if(annotation.cancer_type) {
                    var relevantCancerTypeA = [];
                    var i = 0;
                    var cancerTypeL = annotation.cancer_type.length;

                    for(; i < cancerTypeL; i++) {
                        var _cancerType = annotation.cancer_type[i];
                        if(_cancerType.$relevant_to_patient_disease.toLowerCase() === 'yes') {
                            relevantCancerTypeA.push(_cancerType);
                        }
                    }
                    if(relevantCancerTypeA.length > 1) {
                        /* jshint -W083 */
                        relevantCancerTypeA.sort(function(e){
                            if(e.$type.toString().toLowerCase() === 'all tumors'){
                                return -1;
                            }else{
                                return 1;
                            }
                        });
                        /* jshint +W083 */
                        var obj1 = relevantCancerTypeA[0];
                        var relevantL=relevantCancerTypeA.length;
                        
                        i = 1;

                        for(; i < relevantL; i++) {
                            obj1 = DeepMerge.init(obj1, relevantCancerTypeA[i], obj1.$type, relevantCancerType[i].$type);
                        }
                        relevantCancerType = obj1;
                    }else if(relevantCancerTypeA.length === 1){
                        relevantCancerType = relevantCancerTypeA[0];
                    }else {
                        relevantCancerType = null;
                    }
                }else {
                    relevantCancerType = null;
                }
                var reportParams = ReportDataService.init(annotation.hgnc_symbol, annotation.hgvs_p_short, full.document.sample.diagnosis, relevantCancerType, annotation);
                reportViewDatas.push(reportViewData(reportParams));
            }
            $scope.reportViewDatas = reportViewDatas;
            $scope.isXML = true;
            $scope.$apply();
        };

        reader.readAsBinaryString(file._file);
    }
    
    function reportViewData(params) {
        var _parmas = angular.copy(params);
        _parmas.overallInterpretation = processOverallInterpretation(_parmas.overallInterpretation);
        _parmas = constructData(_parmas);
        return _parmas;
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
                datum = FindRegex.get(datum);
            }
            data[key] = datum;
        }

        return data;
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
                _obj.value = FindRegex.get(obj[key]).toString();
                return _obj;
            });

        return returnArray;
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
        
    uploader = $scope.uploader = new FileUploader();

    uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    uploader.onAfterAddingFile  = function(fileItem) {
        console.info('onAfterAddingFile', fileItem);
        initParams();
        $scope.fileSelected = true;
        if(fileItem.file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
            readXLSXfile(fileItem);
        }else if(fileItem.file.type === 'text/xml'){
            readXMLfile(fileItem);
        }else {
            dialogs.error('Error', 'Do not support the type of selected file, only XLSX or XML file is supported.');
            uploader.removeFromQueue(fileItem);
        }
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

    $scope.init = initParams;

    $scope.generate = function() {
        if(checkEmail()) {
            generate(getWorkers());
        }
    };

    $scope.deleteItem = deleteItem;

    $scope.regenerate = function() {
        if(checkEmail()) {
            generate(getWorkers('regenerate'));
        }
    };
  }]);
