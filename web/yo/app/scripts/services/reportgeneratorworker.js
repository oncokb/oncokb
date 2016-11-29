'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.reportGeneratorWorker
 * @description
 * # reportGeneratorWorker
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .service('reportGeneratorWorker', function($q, DatabaseConnector, GenerateReportDataService, reportGeneratorParseAnnotation, reportGenerator) {
        var worker = function(data, patientId) {
            var self = this;
            self.parseAnnotation = function(data) {
                self.annotation = reportGeneratorParseAnnotation.parse(data);
            };

            self.prepareReportParams = function() {
                self.reportParams.reportContent = GenerateReportDataService.init([{
                    geneName: self.gene,
                    alteration: self.alteration,
                    tumorType: self.tumorType,
                    annotation: self.annotation.annotation,
                    relevantCancerType: self.annotation.relevantCancerType
                }]);
                self.reportParams.reportContent.patientName = self.patientId || '';
                self.reportParams.reportContent.diagnosis = self.reportParams.reportContent.tumorTissueType = self.tumorType;
                self.reportParams.requestInfo.email = self.email;
                self.reportParams.requestInfo.folderName = self.folderName;
                self.reportParams.requestInfo.folderId = self.folderId;
                self.reportParams.requestInfo.fileName = self.fileName;
                self.reportParams.requestInfo.userName = self.userName;
            };

            self.generateGoogleDoc = function() {
                var _deferred = $q.defer();
                reportGenerator.generateGoogleDoc(self.reportParams).then(function() {
                    _deferred.resolve();
                }, function() {
                    _deferred.reject();
                });
                return _deferred.promise;
            };

            self.getData = function() {
                var params = {
                    alterationType: 'MUTATION',
                    hugoSymbol: self.gene,
                    alteration: self.alteration,
                    tumorType: self.tumorType
                };
                var deferred = $q.defer();
                DatabaseConnector.searchAnnotation(params, function(data) {
                    self.parseAnnotation(data);
                    deferred.resolve();
                }, function() {
                    deferred.reject('Error": no annotation data returned', self);
                });

                return deferred.promise;
            };

            self.init = function() {
                self.id = '';
                self.status = {};
                self.status.generate = 0; // 0: ungenerated, 1: successfully generated, -1: unsuccessfully generated, 2: initializing, 3: initialized, 4: generating
                self.email = '';
                self.folderName = '';
                self.folderId = '';
                self.fileName = '';
                self.userName = '';
                self.parent = {};
                self.annotation = {};
                self.parent.name = ''; // could be the entry name
                self.reportParams = {
                    requestInfo: {},
                    reportContent: {}
                };

                if (angular.isString(patientId)) {
                    self.patientId = patientId;
                } else {
                    self.patientId = -new Date().getTime();
                }

                self.gene = data.gene;
                self.alteration = data.alteration;
                self.tumorType = data.tumorType;

                if (data.hasOwnProperty('patientId')) {
                    self.patientId = data.patientId;
                }
            };
            self.init();
        };

        return (worker);
    });

angular.module('oncokbApp')
    .factory('reportGeneratorWorkers', ['reportGeneratorWorker', '_', function(ReportGeneratorWorker, _) {
        var workers = [];

        function get() {
            return workers;
        }

        // Workers could be categorised by XLSX entries or by patientId, each entry could also category variants based on
        // patientId
        function set(data) {
            workers.length = 0;
            createWorkers(data);
        }

        function createWorkers(data) {
            // self is categorised by XLSX entries

            if (angular.isObject(data) && !angular.isArray(data)) {
                _.each(data, function(item, key) {
                    if (angular.isArray(item)) {
                        item.forEach(function(e) {
                            var _worker = new ReportGeneratorWorker(e);
                            _worker.parent.name = key;
                            workers.push(_worker);
                        });
                    } else {
                        _.each(item, function(value, patientId) {
                            var _worker = new ReportGeneratorWorker(value, patientId);
                            _worker.parent.name = key;
                            workers.push(_worker);
                        });
                    }
                });
                _.each(data, function(item, key) {
                    if (angular.isArray(item)) {
                        item.forEach(function(e) {
                            var _worker = new ReportGeneratorWorker(e);
                            _worker.parent.name = key;
                            workers.push(_worker);
                        });
                    } else {
                        _.each(item, function(value, patientId) {
                            var _worker = new ReportGeneratorWorker(value, patientId);
                            _worker.parent.name = key;
                            workers.push(_worker);
                        });
                    }
                });
            } else if (angular.isArray(data)) {  // self is categorised by patientId
                data.forEach(function(e) {
                    var _worker = new ReportGeneratorWorker(e);
                    workers.push(_worker);
                });
            } else {
                console.log('Does not support data format.');
            }
        }

        return {
            set: set,
            get: get
        };
    }]);

angular.module('oncokbApp')
    .factory('reportGeneratorParseAnnotation', function(reportGenerator, S, x2js, DeepMerge, _) {
        function isObject(obj) {
            return angular.isObject(obj) && !angular.isArray(obj);
        }

        function formatDatum(value, key) {
            var changedAttr = ['cancer_type', 'nccn_guidelines', 'clinical_trial', 'sensitive_to', 'resistant_to', 'treatment', 'drug'];
            if (angular.isArray(value) || (!angular.isArray(value) && isObject(value) && changedAttr.indexOf(key) !== -1)) {
                if (!angular.isArray(value) && isObject(value) && changedAttr.indexOf(key) !== -1) {
                    value = [value];
                }

                for (var i = 0; i < value.length; i++) {
                    value[i] = formatDatum(value[i], i);
                }
            } else if (isObject(value)) {
                _.each(value, function(item, _key) {
                    value[_key] = formatDatum(item, _key);
                });
            }

            return value;
        }

        function processData(object) {
            if (angular.isArray(object)) {
                object.forEach(function(e) {
                    processData(e);
                });
            } else if (isObject(object)) {
                _.each(object, function(item, _key) {
                    object[_key] = processData(item);
                });
            } else if (angular.isString(object)) {
                /* eslint new-cap: 0*/
                object = S(object).decodeHTMLEntities().s;
            } else {

            }
            return object;
        }

        function parse(data) {
            return parseJSON(processData(data.xml));
        }

        function parseJSON(data) {
            _.each(data, function(item, key) {
                data[key] = formatDatum(item, key);
            });

            return {
                annotation: data,
                relevantCancerType: getRelevantCancerType(data)
            };
        }

        function getRelevantCancerType(annotation) {
            var relevantCancerType = {};
            var relevantCancerTypeArray = [];
            if (annotation.cancer_type) {
                var i = 0;

                for (var cancerTypeL = annotation.cancer_type.length; i < cancerTypeL; i++) {
                    var _cancerType = annotation.cancer_type[i];
                    if (_cancerType.$relevant_to_patient_disease.toLowerCase() === 'yes') {
                        relevantCancerTypeArray.push(_cancerType);
                    }
                }
                if (relevantCancerTypeArray.length > 1) {
                    relevantCancerType = relevantCancerTypeArray[0];
                    i = 1;
                    for (var relevantL = relevantCancerTypeArray.length; i < relevantL; i++) {
                        relevantCancerType = DeepMerge.init(relevantCancerType, relevantCancerTypeArray[i], relevantCancerType.$type, relevantCancerTypeArray[i].$type);
                    }
                } else if (relevantCancerTypeArray.length === 1) {
                    relevantCancerType = relevantCancerTypeArray[0];
                } else {
                    relevantCancerType = null;
                }
            }

            // Sort clinical trials
            if (relevantCancerType && angular.isArray(relevantCancerType.clinical_trial)) {
                relevantCancerType.clinical_trial.sort(function(a, b) {
                    var _a = a.phase ? a.phase : '';
                    var _b = b.phase ? b.phase : '';
                    var regex = /\d|\d\/\d/igm;

                    var matchA = _a.match(regex) || ['-1']; // If no match found, give lowest priority
                    var matchB = _b.match(regex) || ['-1'];

                    var largestA = Math.max.apply(Math, matchA);
                    var largestB = Math.max.apply(Math, matchB);

                    if (largestA - largestB > 0) {
                        return -1;
                    } else if (largestA === largestB) {
                        if (matchA.length > matchB.length) {
                            return 1;
                        }
                        return -1;
                    }
                    return 1;
                });
            }
            return relevantCancerType;
        }

        return {
            parse: parse,
            parseJSON: parseJSON,
            getRelevantCancerType: getRelevantCancerType
        };
    });

angular.module('oncokbApp')
    .factory('reportGenerator', function($q, DatabaseConnector) {
        function generateGoogleDoc(reportParams) {
            var deferred = $q.defer();
            DatabaseConnector.googleDoc(reportParams, function() {
                deferred.resolve();
            }, function() {
                deferred.reject();
            });

            return deferred.promise;
        }

        return {
            generateGoogleDoc: generateGoogleDoc
        };
    });

angular.module('oncokbApp')
    .factory('reportGeneratorData', function($q, DatabaseConnector, OncoKB) {
        var genes;
        var alterations;
        var tumorTypes;

        function init() {
            var defer = $q.defer();
            DatabaseConnector.getGeneTumorType(function(data) {
                OncoKB.global.genes = angular.copy(data.genes);
                OncoKB.global.tumorTypes = angular.copy(data.tumorTypes);

                genes = getUnique(data.genes, 'hugoSymbol');
                tumorTypes = getUnique(data.tumorTypes, 'name');
                defer.resolve();
            });

            return defer.promise;
        }

        function getGene() {
            var defer = $q.defer();
            if (genes) {
                defer.resolve(genes);
            } else {
                init().then(function() {
                    defer.resolve(genes);
                });
            }
            return defer.promise;
        }

        function getMutation() {
            var defer = $q.defer();
            if (alterations) {
                defer.resolve(alterations);
            } else {
                init().then(function() {
                    defer.resolve(alterations);
                });
            }
            return defer.promise;
        }

        function getTumorType() {
            var defer = $q.defer();
            if (tumorTypes) {
                defer.resolve(tumorTypes);
            } else {
                init().then(function() {
                    defer.resolve(tumorTypes);
                });
            }
            return defer.promise;
        }

        function get() {
            var defer = $q.defer();
            getGene().then(function() {
                getMutation().then(function() {
                    getTumorType().then(function() {
                        defer.resolve({
                            genes: genes,
                            alterations: alterations,
                            tumorTypes: tumorTypes
                        });
                    });
                });
            });
            return defer.promise;
        }

        function getUnique(data, attr) {
            var unique = [];

            if (angular.isArray(data)) {
                data.forEach(function(e) {
                    if (unique.indexOf(e[attr]) === -1) {
                        unique.push(e[attr]);
                    }
                });
                return unique.sort();
            }
            return null;
        }

        return {
            getGene: getGene,
            getMutation: getMutation,
            getTumorType: getTumorType,
            get: get
        };
    });

