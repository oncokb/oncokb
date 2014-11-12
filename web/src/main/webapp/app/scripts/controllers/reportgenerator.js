'use strict';

/**
 * @ngdoc function
 * @name webappApp.controller:ReportgeneratorCtrl
 * @description
 * # ReportgeneratorCtrl
 * Controller of the webappApp
 */
angular.module('webappApp')
  .controller('ReportgeneratorCtrl', ['$scope', '$timeout' ,'FileUploader', 'SearchVariant', 'GenerateDoc', 'GenerateReportDataService', function($scope, $timeout, FileUploader, SearchVariant, GenerateDoc, ReportDataService) {
    var changedAttr = ['cancer_type', 'nccn_guidelines', 'clinical_trial', 'sensitive_to', 'resistant_to', 'treatment', 'drug'];
    
    $scope.init = function() {
        initParams();
    };
    
    function initParams() {
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
        //if file selected
        $scope.fileSelected = false;
        
        //one worker running
        $scope.generating = false;
        
        //all workering running
        $scope.working = false;
        $scope.generateIndex = -1;
    }
    
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
            $scope.working = true;
            $scope.progress.value = 0.5;
            $scope.progress.dynamic = 0;
            $scope.progress.max = worker.length;
            $scope.generateIndex = -1;
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
                if(!$scope.generating) {
                    $scope.generateIndex++; 
                    if ($scope.generateIndex < $scope.workers.length) { 
                        var worker = $scope.workers[$scope.generateIndex];
                        $scope.generating = true;
                        getAnnotation(alterationProcee(worker.gene), alterationProcee(worker.alteration), alterationProcee(worker.tumorType));
                    }else {
                        $scope.working = false;
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

            var reportParams = ReportDataService.init(params.hugoSymbol, params.alteration, params.tumorType, relevantCancerType,annotation);
            reportParams.email = 'jackson.zhang.828@gmail.com';
            
            GenerateDoc.getDoc(reportParams).success(function(data) {
                $scope.generating = false;
                $scope.progress.dynamic += 1;
                $scope.progress.value = $scope.progress.dynamic / $scope.progress.max * 100;
                generateReports();
            });
        });
    }
    
    $scope.deleteItem = deleteItem;
    
    function deleteItem(sheetName, sheetArrDatum) {
        $scope.sheets.arr[sheetName].forEach(function(e, i) {
            if(e.id === sheetArrDatum.id) {
                $scope.sheets.arr[sheetName].splice(i,1);
            }
        });
    }
    
    var uploader = $scope.uploader = new FileUploader();

    uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    uploader.onAfterAddingFile  = function(fileItem) {
        console.info('onAfterAddingFile', fileItem);
        initParams();
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
                        'id': sheetName + '-' + i,
                        'gene': '',
                        'alteration': '',
                        'tumorType': ''
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
            $scope.fileSelected = true;
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
