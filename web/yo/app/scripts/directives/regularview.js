'use strict';

angular.module('oncokbApp')
    .directive('regularView', ['FindRegex', function(FindRegex) {
        function link($scope) {
            $scope.getDrugHeader = function(key, data) {
                var header = '';

                if (key !== 'sensitive_to') {
                    header += $scope.displayProcess(key) + ': ';
                }

                if (typeof data === 'object') {
                    if (data.hasOwnProperty('treatment')) {
                        var hasTreatment = false;
                        data.treatment.forEach(function(e) {
                            if (e.hasOwnProperty('drug')) {
                                var hasDrug = false;
                                e.drug.forEach(function(e1) {
                                    if (e1.hasOwnProperty('name')) {
                                        hasDrug = true;
                                        header += e1.name + ' + ';
                                    }
                                });
                                if (hasDrug) {
                                    hasTreatment = true;
                                    header = header.substr(0, header.length - 3);
                                }
                            }
                            header += ' & ';
                        });
                        if (hasTreatment) {
                            header = header.substr(0, header.length - 3);
                        }
                    }
                }
                return header;
            };
            $scope.getDrugBody = function(data) {
                var body = '';
                if (hasEvidenceLevel(data)) {
                    body += 'Highest Level of Evidence: ' + data.level_of_evidence_for_patient_indication.level + '<br/>';
                }
                body += (data.hasOwnProperty('description') && angular.isString(data.description)) ? FindRegex.get(data.description) : '';
                return body;
            };
            $scope.hasGeneralStatement = function(data) {
                if (typeof data === 'object' && data.hasOwnProperty('general_statement')) {
                    return true;
                }
                return false;
            };

            $scope.findRegex = FindRegex.get;

            $scope.isNonArray = function(object) {
                if (!angular.isArray(object)) {
                    return true;
                }
                return false;
            };

            $scope.isArray = function(object) {
                if (angular.isArray(object)) {
                    return true;
                }
                return false;
            };

            function hasEvidenceLevel(data) {
                if (typeof data === 'object' &&
                    data.hasOwnProperty('level_of_evidence_for_patient_indication') &&
                    data.level_of_evidence_for_patient_indication.hasOwnProperty('level')) {
                    return true;
                }
                return false;
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
                isCollapsed: '=',
                fdaApproved: '=',
                displayParts: '='
            }
        };
    }]);
