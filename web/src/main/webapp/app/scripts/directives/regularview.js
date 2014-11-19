'use strict';

/**
 * @ngdoc directive
 * @name webappApp.directive:regularView
 * @description
 * # regularView
 */
angular.module('webappApp')
    .directive('regularView', function () {
        function link($scope) {
            $scope.getDrugHeader = function(key, data) {
                var header = '';
                
                if(key !== 'sensitive_to') {
                    header += $scope.displayProcess(key) + ": ";
                }
                
                if(typeof data === 'object') {
                    if(data.hasOwnProperty('treatment')) {
                        var hasTreatment = false;
                        data.treatment.forEach(function(e, i){
                            if(e.hasOwnProperty('drug')) {
                                var hasDrug = false;
                                e.drug.forEach(function(e1, i1){
                                    if(e1.hasOwnProperty('name')) {
                                        hasDrug = true;
                                        header += e1.name + ' + ';
                                    }
                                });
                                if(hasDrug){
                                    hasTreatment = true;
                                    header = header.substr(0, header.length-3);
                                }
                            }
                            header += ' & ';
                        });
                        if(hasTreatment){
                            header = header.substr(0, header.length-3);
                        }
                    }
                }
                return header;
            };
            $scope.getDrugBody = function(data) {
                var body = '';
                if(hasEvidenceLevel(data)) {
                    body += 'Highest Level of Evidence: ' + data.level_of_evidence_for_patient_indication.level + '<br/>';
                }
                body += data.hasOwnProperty('description')?$scope.findRegex(data.description):'';
                return body;
            };
            $scope.hasGeneralStatement = function(data) {
                if(typeof data === 'object' && data.hasOwnProperty('general_statement')) {
                    return true;
                }else {
                    return false;
                }
            }
            function hasEvidenceLevel(data) {
                if(typeof data === 'object' 
                        && data.hasOwnProperty('level_of_evidence_for_patient_indication')
                        && data['level_of_evidence_for_patient_indication'].hasOwnProperty('level')) {
                    return true;
                }else {
                    return false;
                }
            }
        }

        return {
            templateUrl: 'views/regularView.html',
            restrict: 'E',
            link: link,
            scope: {
                annotation: '=',
                summaryTableTitles: '=',
                summaryTableTitlesContent: '=',
                specialAttr: '=',
                displayProcess: '=',
                findRegex: '=',
                setCollapsed: '=',
                getCollapseIcon: '=',
                isCollapsed: '=',
                generateTrial: '=',
                fdaApproved: '=',
                generateNccn: '=',
                displayParts: '='
            }
        };
    });
