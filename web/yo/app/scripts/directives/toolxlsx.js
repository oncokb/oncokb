'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:toolXLSX
 * @description
 * # toolXLSX
 */
angular.module('oncokbApp')
    .directive('toolXlsx', function (
        $timeout,
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
              initParams(function(){
                $scope.status.fileSelected = true;
                if(fileItem.file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
                  $scope.status.isXLSX = true;
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

            $scope.workers = [];

            $scope.progress = {
              value: 0,
              dynamic: 0,
              max: $scope.workers.length
            };

            $scope.table = [
              {
                name: 'parent',
                title: 'Entry'
              },
              {
                name: 'patientId',
                title: 'Patient ID'
              },
              {
                name: 'gene',
                title: 'Gene'
              },
              {
                name: 'alteration',
                title: 'Alteration'
              },
              {
                name: 'tumorType',
                title: 'Tumor Type'
              }
            ];

            $scope.status = {
              isXLSX: false,
              generating: false,
              generateIndex: -1,
              initializingIndex: -1,
              failed: false,
              fileSelected: false,
              mergePatient: true
            };

            $scope.$watch('$scope.workers.length', function(n, o){
              $scope.progress.max = n;
            });

            $scope.$watch('status.generateIndex', function(n, o){
              if(n == 0){
                $scope.progress.dynamic = 0;
                $scope.progress.value = 0;
                $scope.progress.max = $scope.workers.length;
              }
              if(n >= 0) {
                if(n >= $scope.workers.length) {
                  $scope.status.generating = false;
                  $scope.status.generateIndex = -1;
                }

                if(n < $scope.workers.length){
                  generate();
                }
              }
            });

            $scope.$watch('status.generateIndex', function(n, o){
              if(n == 0){
                $scope.progress.dynamic = 0;
                $scope.progress.value = 0;
                $scope.progress.max = $scope.workers.length;
              }
              if(n >= 0) {
                if(n >= $scope.workers.length) {
                  $scope.status.generating = false;
                  $scope.status.generateIndex = -1;
                }

                if(n < $scope.workers.length){
                  generate();
                }
              }
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

              for (var i=0, workbookSheetsNum = workbook.SheetNames.length; i < workbookSheetsNum; i++) {
                var sheetName = workbook.SheetNames[i];
                var json = XLSX.utils.sheet_to_json(workbook.Sheets[workbook.SheetNames[i]]);

                fileValue[sheetName] = [];
                fileAttrs[sheetName] = ['gene', 'alteration', 'tumorType'];

                $scope.sheets.folder[sheetName] = {};

                json.forEach(function(e,i){
                  var datum = {
                    'id': sheetName + '-' + i,
                    'gene': '',
                    'alteration': '',
                    'tumorType': ''
                  };

                  for(var key in e) {
                    if (e.hasOwnProperty(key)) {
                      if(/patient/i.test(key)) {
                        datum.patientId = e[key];
                      }else if(/gene/i.test(key)) {
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

              reportGeneratorWorkers.set(fileValue);
              $scope.workers = reportGeneratorWorkers.get();
              console.log($scope.workers);
              $scope.sheets.length = workbookSheetsNum;
              $scope.sheets.attr = fileAttrs;
              $scope.sheets.arr = fileValue;
              $scope.progress.dynamic = 0;
              $scope.progress.value = 0;
              $scope.isXLSX = true;
              $scope.$apply();
            };

            reader.readAsBinaryString(file._file);
          }

          function initialzeWorkersData(){
            $scope.workers[$scope.status.initializingIndex].email = $scope.sheets.email || 'jackson.zhang.828@gmail.com';
            $scope.workers[$scope.status.initializingIndex].folderName = $scope.sheets.folder[$scope.workers[$scope.status.generateIndex].parent.name].name || '';
            $scope.workers[$scope.status.initializingIndex].status.generate = 2;
            $scope.workers[$scope.status.initializingIndex].getData().then(function(){
              $scope.workers[$scope.status.initializingIndex].status.generate = 3;
            });
          }
          function generate(){
            console.log('Generating - ', $scope.status.generateIndex);
            $scope.workers[$scope.status.generateIndex].status.generate = 4;
            $scope.workers[$scope.status.generateIndex].getData().then(function(){
              $scope.workers[$scope.status.initializingIndex].status.generate = 1;
            });
          }

          $scope.generate = generate;
          $scope.init = initUploader;
        }
      };
    });
