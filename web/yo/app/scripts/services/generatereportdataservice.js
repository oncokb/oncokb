'use strict';

/**
 * @ngdoc service
 * @name oncokb.GenerateReportDataService
 * @description
 * # GenerateReportDataService
 * Factory in the oncokb.
 */
angular.module('oncokbApp')
    .factory('GenerateReportDataService', function(reportItem) {
        function generateItems(data) {
            var items = [];
            if (angular.isArray(data)) {
                data.forEach(function(e) {
                    items.push(reportItem.init(e.geneName, e.alteration, e.tumorType, e.annotation, e.relevantCancerType));
                });
            }
            return items;
        }

        function init(data) {
            var params = {
                patientId: '',
                specimen: '',
                clientNum: '',
                diagnosis: '',
                tumorTissueType: '',
                specimenSource: 'None.',
                blockId: 'None.',
                stage: 'None.',
                grade: 'None.',
                items: []
            };
            params.items = generateItems(data);
            params.diagnosis = params.tumorTissueType = data[0].tumorType;
            return params;
        }

        // Public API here
        return {
            init: init
        };
    });
