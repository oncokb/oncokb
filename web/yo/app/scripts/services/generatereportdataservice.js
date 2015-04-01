'use strict';

/**
 * @ngdoc service
 * @name oncokb.GenerateReportDataService
 * @description
 * # GenerateReportDataService
 * Factory in the oncokb.
 */
angular.module('oncokbApp')
  .factory('GenerateReportDataService', function (reportItem) {
    /*jshint -W106 */
    function generateReportData(data) {
        var items = [];
        if(angular.isArray(data)){
            data.forEach(function(e){
                items.push(reportItem.init(e.geneName, e.alteration, e.tumorType, e.annotation, e.relevantCancerType));
            });
        }
        return items;
    }

    // Public API here
    return {
      init: generateReportData
    };
  });
