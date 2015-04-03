'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:toolXLSX
 * @description
 * # toolXLSX
 */
angular.module('oncokbApp')
    .directive('toolXlsx', function (
        FileUploader,
        DatabaseConnector,
        reportGeneratorData,
        reportGeneratorWorkers,
        XLSX,
        dialogs,
        S,
        Levenshtein) {
      return {
        templateUrl: 'views/toolxlsx.html',
        restrict: 'E',
        scope: {},
        link: function postLink(scope, element, attrs) {

        },
        controller: function($scope){
          function initUploader() {
            var uploader = $scope.uploader;
            uploader = $scope.uploader = new FileUploader();

            uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
              console.info('onWhenAddingFileFailed', item, filter, options);
            };

            uploader.onAfterAddingFile  = function(fileItem) {
              console.info('onAfterAddingFile', fileItem);
              console.log($scope);
              initParams(function(){
                $scope.fileSelected = true;
                if(fileItem.file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
                  readXLSXfile(fileItem);
                }else {
                  dialogs.error('Error', 'Do not support the type of selected file, only XLSX or XML file is supported.');
                  uploader.removeFromQueue(fileItem);
                }
              });
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
          }

          function initParams(callback) {
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
            $scope.$watch('sheets.folder', function(o,n){
              console.log(o, n);
            });

            getGMT(callback);
          }

          //get genes, mutations and tumor types
          function getGMT(callback){
            var data = reportGeneratorData.get().then(function(data){
              console.log(data);
              $scope.genes = data.genes;
              $scope.alterations = data.alterations;
              $scope.tumorTypes = data.tumorTypes;
              callback();
            });
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
                console.log(json);
                fileValue[sheetName] = [];
                fileAttrs[sheetName] = ['gene', 'alteration', 'tumorType'];
                $scope.sheets.folder[sheetName] = {};
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
                        datum.gene = check(e[key], 'genes');
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

              function trimAlteration(alteration) {
                if(S(alteration).startsWith('p.')) {
                  alteration = alteration.slice(2);
                }

                if(alteration.indexOf('Ter')) {
                  alteration = alteration.replace('Ter', '*');
                }
                return alteration;
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

                // if(same) {
                return datum;
                // }else {
                //     return datum + ' ' + noMatchSeperator;
                // }
              }

              console.log(workbookSheetsNum, fileAttrs, fileValue, totalRecord);
              reportGeneratorWorkers.set(fileValue);
              $scope.workers = reportGeneratorWorkers.get();
              $scope.sheets.length = workbookSheetsNum;
              $scope.sheets.attr = fileAttrs;
              $scope.sheets.arr = fileValue;
              $scope.progress.dynamic = 0;
              $scope.progress.value = 0;
              $scope.progress.max = totalRecord;
              $scope.isXLSX = true;
              $scope.$apply();
              console.log($scope.workers);
            };

            reader.readAsBinaryString(file._file);
          }

          function generate(){
            console.log($scope);
            $scope.workers.forEach(function(worker){
              worker.email = $scope.sheets.email || 'jackson.zhang.828@gmail.com';
              worker.folderName = $scope.sheets.folder[worker.parent.name].name || '';
              worker.status.generate = 2;
              worker.getData().then(function(){
                worker.generateGoogleDoc().then(function(result){
                  console.log(worker);
                  $scope.progress.dynamic += 1;
                  $scope.progress.value = $scope.progress.dynamic / $scope.progress.max * 100;
                  if(result && result.error){
                    worker.status.generate = -1;
                  }else{
                    worker.status.generate = 1;
                  }
                });
                console.log(worker);
              });
            });
          }

          $scope.generate = generate;
          $scope.init = initUploader;
        }
      };
    });
