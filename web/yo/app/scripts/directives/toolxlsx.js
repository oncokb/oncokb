'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:toolXLSX
 * @description
 * # toolXLSX
 */
angular.module('oncokbApp')
    .directive('toolXLSX', function (FileUploader, DatabaseConnector) {
      return {
        template: '<div></div>',
        restrict: 'E',
        scope: {
          uploader: ''
        },
        link: function postLink(scope, element, attrs) {
          element.text('this is the toolXLSX directive');
        },
        controller: function($scope){
          var uploader = $scope.uploader;
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

              console.log(workbookSheetsNum, fileAttrs, fileValue, totalRecord);
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
        }
      };
    });
