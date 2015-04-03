'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.reportGeneratorWorker
 * @description
 * # reportGeneratorWorker
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .service('reportGeneratorWorker', function ($q, DatabaseConnector, GenerateReportDataService, reportGeneratorParseAnnotation,reportGenerator) {
        var worker = function(data, patientId){
            var self = this;
            self.parseAnnotation = function(data){
                var params = {};
                var annotation = reportGeneratorParseAnnotation.parse(data);

                params.geneName = self.gene;
                params.alteration = self.alteration;
                params.tumorType = self.tumorType;
                params.annotation = annotation.annotation;
                params.relevantCancerType = annotation.relevantCancerType;

                self.reportParams = GenerateReportDataService.init([{
                    geneName: self.gene,
                    alteration: self.alteration,
                    tumorType: self.tumorType,
                    annotation: annotation.annotation,
                    relevantCancerType: annotation.relevantCancerType
                }])[0];
                self.reportParams.email = self.email;
                self.reportParams.folderName = self.folderName;
                self.reportParams.fileName = self.fileName;
            };

            self.generateGoogleDoc = function(){
                var _deferred = $q.defer();
                reportGenerator.generateGoogleDoc(self.reportParams).then(function(){
                    _deferred.resolve();
                },function(){
                    _deferred.reject();
                });
                return _deferred.promise;
            };

            self.getData = function(){
                var params = {
                    'alterationType': 'MUTATION',
                    'hugoSymbol': self.gene,
                    'alteration': self.alteration,
                    'tumorType': self.tumorType
                };
                var deferred = $q.defer();
                DatabaseConnector.searchAnnotation(params, function(data){
                    self.parseAnnotation(data);
                    deferred.resolve();
                }, function(){
                    deferred.reject('Error": no annotation data returned', self);
                });

                return deferred.promise;
            };

            self.init = function() {
                self.id = '';
                self.status = {};
                self.status.generate = 0; //0: ungenerated, 1: successfully generated, -1: unsuccessfully generated, 2: generating
                self.email = '';
                self.folderName = '';
                self.fileName = '';
                self.parent = {};
                self.annotation = '';
                self.parent.name = ''; // could be the entry name
                self.reportParams = {};

                if(angular.isString(patientId)) {
                    self.patientId = patientId;
                }

                self.gene = data.gene;
                self.alteration = data.alteration;
                self.tumorType = data.tumorType;

                if(data.hasOwnProperty('patientId')) {
                    self.patientId = data.patientId;
                }
            };
            self.init();

        };

        return (worker);
    });

angular.module('oncokbApp')
    .factory('reportGeneratorWorkers', ['reportGeneratorWorker' , function (ReportGeneratorWorker) {
        var workers = [];

        function get() {
            return workers;
        }

        //Workers could be categorised by XLSX entries or by patientId, each entry could also category variants based on
        //patientId
        function set(data) {
            workers.length = 0;
            createWorkers(data);
            initWorkers();
        }

        function initWorkers(){
            if(workers.length > 0) {
                workers.forEach(function(e){
                    e.init();
                });
            }
        }

        function createWorkers(data){
            //self is categorised by XLSX entries
            if(angular.isObject(data)){
                for(var key in data){
                    var datum = data[key];
                    if(angular.isArray(data[key])){
                        datum.forEach(function(e){
                            var _worker = new ReportGeneratorWorker(e);
                            _worker.parent.name = key;
                            workers.push(_worker);
                        });
                    }else{
                        for(var patientId in datum){
                            var _worker = new ReportGeneratorWorker(datum[patientId], patientId);
                            _worker.parent.name = key;
                            workers.push(_worker);
                        }
                    }

                }
            }else if(angular.isArray(data)){  //self is categorised by patientId
                data.forEach(function(e){
                    var _worker = new ReportGeneratorWorker(e);
                    workers.push(_worker);
                });
            }else{
                console.log('Does not support data format.');
            }
        }

        return {
            set: set,
            get: get
        };
    }]);

angular.module('oncokbApp')
    .factory('reportGeneratorParseAnnotation', function(reportGenerator, S, x2js, DeepMerge){
        function isObject(obj) {
            return angular.isObject(obj) && !angular.isArray(obj);
        }

        function formatDatum(value, key) {
            var changedAttr = ['cancer_type', 'nccn_guidelines', 'clinical_trial', 'sensitive_to', 'resistant_to', 'treatment', 'drug'];
            if(angular.isArray(value) || (!angular.isArray(value) && isObject(value) && changedAttr.indexOf(key) !== -1)) {
                if(!angular.isArray(value) && isObject(value) && changedAttr.indexOf(key) !== -1) {
                    value = [value];
                }

                for (var i = 0; i < value.length; i++) {
                    value[i] = formatDatum(value[i], i);
                }
            }else if(isObject(value)) {
                for(var _key in value) {
                    value[_key] = formatDatum(value[_key], _key);
                }
            }

            return value;
        }

        function processData(object) {
            if(angular.isArray(object)) {
                object.forEach(function(e){
                    e = processData(e);
                });
            }else if(isObject(object)) {
                for(var key in object) {
                    object[key] = processData(object[key]);
                }
            }else if(angular.isString(object)) {
                object = S(object).decodeHTMLEntities().s;
            }else {

            }
            return object;
        }

        function parse(data){
            var relevantCancerTypeArray = [];
            var relevantCancerType = {};

            var annotation = processData(x2js.xml_str2json(data).xml);

            for(var key in annotation) {
                annotation[key] = formatDatum(annotation[key], key);
            }

            if(annotation.cancer_type) {
                var i = 0;

                for(var cancerTypeL = annotation.cancer_type.length; i < cancerTypeL; i++) {
                    var _cancerType = annotation.cancer_type[i];
                    if(_cancerType.$relevant_to_patient_disease.toLowerCase() === 'yes') {
                        relevantCancerTypeArray.push(_cancerType);
                    }
                }
                if(relevantCancerTypeArray.length > 1) {
                    relevantCancerType = relevantCancerTypeArray[0];
                    i = 0;
                    for(var relevantL=relevantCancerTypeArray.length; i < relevantL; i++) {
                        relevantCancerType = DeepMerge.init(relevantCancerType, relevantCancerTypeArray[i], relevantCancerType.$type, relevantCancerTypeArray[i].$type);
                    }
                }else if(relevantCancerTypeArray.length === 1){
                    relevantCancerType = relevantCancerTypeArray[0];
                }else {
                    relevantCancerType = null;
                }
            }

            return {
                annotation: annotation,
                relevantCancerType: relevantCancerType
            };
        }

        return {
            'parse': parse
        };
    });

angular.module('oncokbApp')
    .factory('reportGenerator', function($q, DatabaseConnector){
        function generateGoogleDoc(reportParams) {
            var deferred = $q.defer();
            DatabaseConnector.googleDoc(reportParams, function(){
                deferred.resolve();
            }, function(){
                deferred.reject();
            });

            return deferred.promise;
        }

        return {
            'generateGoogleDoc': generateGoogleDoc
        };
    });

angular.module('oncokbApp')
    .factory('reportGeneratorData', function($q, DatabaseConnector){
        var genes, alterations, tumorTypes;
        function init(){
            var defer = $q.defer();
            DatabaseConnector.getGeneAlterationTumortype(function(data){
                OncoKB.global.genes = angular.copy(data.genes);
                OncoKB.global.alterations = angular.copy(data.alterations);
                OncoKB.global.tumorTypes = angular.copy(data.tumorTypes);

                genes = getUnique(data.genes, 'hugoSymbol');
                alterations = getUnique(data.alterations, 'name');
                tumorTypes = getUnique(data.tumorTypes, 'name');
                defer.resolve();
            });

            return defer.promise;
        }

        function getGene(){
            var defer = $q.defer();
            if(!genes) {
                init().then(function(){
                    defer.resolve(genes);
                })
            }else{
                defer.resolve(genes);
            }
            return defer.promise;;
        }

        function getMutation(){
            var defer = $q.defer();
            if(!alterations) {
                init().then(function(){
                    defer.resolve(alterations);
                });
            }else{
                defer.resolve(alterations);
            }
            return defer.promise;
        }

        function getTumorType(){
            var defer = $q.defer();
            if(!tumorTypes) {
                init().then(function(){
                    defer.resolve(tumorTypes);
                });
            }else{
                defer.resolve(tumorTypes);
            }
            return defer.promise;
        }

        function get(){
            var defer = $q.defer();
            getGene().then(function(){
                getMutation().then(function(){
                    getTumorType().then(function(){
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

            if(angular.isArray(data)){
                data.forEach(function(e) {
                    if(unique.indexOf(e[attr]) === -1) {
                        unique.push(e[attr]);
                    }
                });
                return unique.sort();
            }else {
                return null;
            }
        }

        return {
            'getGene': getGene,
            'getMutation': getMutation,
            'getTumorType': getTumorType,
            'get': get
        };
    });

