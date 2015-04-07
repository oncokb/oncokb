'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:toolXML
 * @description
 * # toolXML
 */
angular.module('oncokbApp')
    .directive('toolXml', function (
        FileUploader,
        reportGeneratorWorkers,
        reportGeneratorParseAnnotation,
        reportViewFactory,
        dialogs,
        FindRegex,
        x2js) {
      return {
        templateUrl: 'views/toolxml.html',
        restrict: 'E',
        scope: {},
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
                if(fileItem.file.type === 'text/xml') {
                  $scope.status.isXML = true;
                  readXMLfile(fileItem);
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
            $scope.status = {
              isXML: false
            };

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

            callback();
          }

          function readXMLfile(file) {
            var reader = new FileReader();
            var workers = [];
            var i;

            reader.onload = function(e) {
              var full = x2js.xml_str2json(e.target.result);
              console.log(full);
              var reportViewData = [];
              var variants = [];
              var reportViewDatas = [];
              if(angular.isDefined(full.document.sample.test.variant)) {
                if(angular.isArray(full.document.sample.test.variant)) {
                  variants = full.document.sample.test.variant;
                }else {
                  variants.push(full.document.sample.test.variant);
                }

                for(var k = 0 ;k < variants.length;k++) {
                  reportViewData.push({
                    gene: variants[k].allele.transcript.hgnc_symbol,
                    alteration: variants[k].allele.transcript.hgvs_p_short,
                    tumorType: full.document.sample.diagnosis
                  });
                }
                console.log(reportViewData);

                reportGeneratorWorkers.set(reportViewData);
                workers = reportGeneratorWorkers.get();
                for(i=0; i<workers.length; i++) {
                  workers[i].annotation.annotation = variants[i].allele.transcript;
                  workers[i].annotation.relevantCancerType = reportGeneratorParseAnnotation.getRelevantCancerType(variants[i].allele.transcript);
                  console.log(workers[i]);
                  workers[i].prepareReportParams();

                  var reportViewParams = {};
                  for(var key in workers[i].reportParams.reportContent) {
                    if(key !== 'items') {
                      reportViewParams[key] = workers[i].reportParams.reportContent[key];
                    }else{
                      for(var key1 in workers[i].reportParams.reportContent.items[0]){
                        reportViewParams[key1] = workers[i].reportParams.reportContent.items[0][key1];
                      }
                    }
                  }
                  reportViewDatas.push(reportViewFactory.getData(reportViewParams));
                }
                console.log(workers);
                console.log(reportViewDatas);

                $scope.reportViewDatas = reportViewDatas;
                $scope.status.isXML = true;
                $scope.$apply();
              }else{
                $scope.status.isXML = false;
                $scope.$apply();
              }
            };

            reader.readAsBinaryString(file._file);
          }



          $scope.init = initUploader;
        }
      };
    });
