'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:toolXML
 * @description
 * # toolXML
 */
angular.module('oncokbApp')
  .directive('toolXML', function () {
    return {
      template: '<div></div>',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
        element.text('this is the toolXML directive');
      },
      controller: function($scope){


        function readXMLfile(file) {
          var reader = new FileReader();

          reader.onload = function(e) {
            var full = x2js.xml_str2json(e.target.result);
            var reportViewDatas = [];
            var variants = [];
            if(angular.isDefined(full.document.sample.test.variant)) {
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
                      obj1 = DeepMerge.init(obj1, relevantCancerTypeA[i], obj1.$type, relevantCancerTypeA[i].$type);
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
                var params = {
                  'geneName': annotation.hgnc_symbol,
                  'alteration': annotation.hgvs_p_short,
                  'tumorType': full.document.sample.diagnosis,
                  'annotation': annotation,
                  'relevantCancerType': relevantCancerType};

                var reportParams = ReportDataService.init([params])[0];
                reportViewDatas.push(reportViewData(reportParams));
              }
              $scope.reportViewDatas = reportViewDatas;
              $scope.isXML = true;
              $scope.$apply();
            }else{
              $scope.isXML = false;
              $scope.$apply();
            }
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
            if(angular.isArray(datum)) {
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
      }
    };
  });
