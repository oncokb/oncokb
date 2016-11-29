'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:toolXML
 * @description
 * # toolXML
 */
angular.module('oncokbApp')
    .directive('toolXml', function(reportGeneratorWorkers,
                                   reportGeneratorParseAnnotation,
                                   reportViewFactory,
                                   dialogs,
                                   FindRegex,
                                   x2js,
                                   _) {
        return {
            templateUrl: 'views/toolxml.html',
            restrict: 'E',
            scope: {
                file: '=',
                open: '=',
                rendering: '='
            },
            link: function(scope) {
                scope.$watch('file', function(n) {
                    if (angular.isDefined(n) && scope.open) {
                        scope.init();
                    }
                });
            },
            controller: function($scope) {
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
                        var reportViewData = [];
                        var variants = [];
                        var reportViewDatas = [];
                        if (full && full.document && full.document.sample && full.document.sample.test && full.document.sample.test.variant) {
                            if (angular.isArray(full.document.sample.test.variant)) {
                                variants = full.document.sample.test.variant;
                            } else {
                                variants.push(full.document.sample.test.variant);
                            }

                            for (var k = 0; k < variants.length; k++) {
                                reportViewData.push({
                                    gene: variants[k].allele.transcript.hgnc_symbol,
                                    alteration: variants[k].allele.transcript.hgvs_p_short,
                                    tumorType: full.document.sample.diagnosis
                                });
                            }

                            reportGeneratorWorkers.set(reportViewData);
                            workers = reportGeneratorWorkers.get();
                            for (i = 0; i < workers.length; i++) {
                                var _annotation = reportGeneratorParseAnnotation.parseJSON(variants[i].allele.transcript);
                                workers[i].annotation = _annotation;
                                workers[i].prepareReportParams();
                                var reportViewParams = {};
                                for (var key in workers[i].reportParams.reportContent) {
                                    if (key === 'items') {
                                        _.each(workers[i].reportParams.reportContent.items[0], function(item, key1) {
                                            reportViewParams[key1] = item;
                                        });
                                    } else {
                                        reportViewParams[key] = workers[i].reportParams.reportContent[key];
                                    }
                                }
                                reportViewDatas.push(reportViewFactory.getData(reportViewParams));
                            }

                            console.log(workers);
                            $scope.reportViewDatas = reportViewDatas;
                            $scope.status.isXML = true;
                            $scope.status.validXML = true;
                            $scope.rendering = false;
                            $scope.$apply();
                        } else {
                            if (full) {
                                $scope.status.isXML = true;
                                $scope.status.validXML = false;
                            } else {
                                $scope.status.isXML = false;
                                $scope.status.validXML = null;
                            }
                            $scope.rendering = false;
                            $scope.$apply();
                        }
                    };

                    reader.readAsText(file._file);
                }

                $scope.init = function() {
                    initParams(function() {
                        $scope.status.fileSelected = true;
                        readXMLfile($scope.file);
                    });
                };
            }
        };
    });
