'use strict';

/**
 * @ngdoc function
 * @name oncokb.controller:ReportgeneratorCtrl
 * @description
 * # ReportgeneratorCtrl
 * Controller of the oncokb
 */
angular.module('oncokbApp')
    .controller('ReportgeneratorCtrl', [
        '$scope',
        '$timeout' ,
        'dialogs',
        'DatabaseConnector',
        'GenerateReportDataService',
        'DeepMerge',
        'x2js',
        'FindRegex',
        'OncoKB',
        'S',
        'Levenshtein',
        'reportGeneratorWorkers',
        'XLSX',
        function(
            $scope,
            $timeout,
            dialogs,
            DatabaseConnector,
            ReportDataService,
            DeepMerge,
            x2js,
            FindRegex,
            OncoKB,
            S,
            Levenshtein,
            rgWorkers,
            XLSX) {
            var uploader; //$scope.uploader -- selected file handler

            function generate(workers) {
                console.log(workers);

                $scope.workers = workers;
                $scope.working = true;
                $scope.progress.value = 0.5;
                $scope.progress.dynamic = 0;
                $scope.progress.max = workers.length;
                $scope.generateIndex = -1;
                generateReports();
            }

            function getWorkers(data, type, flag) {
                var workers = [];
                /* jshint -W083 */
                if(type === 'sheet') {
                    rgWorkers.set($scope.sheets.arr);
                    workers = rgWorkers.get();
                }else if(type === 'multiV') {
                    rgWorkers.set(data);
                    workers = rgWorkers.get();
                }
                /* jshint +W083 */
                return workers;
            }

            function checkEmail() {
                if($scope.sheets.email === '') {
                    dialogs.notify('','Please check your email address.');
                    return false;
                }else {
                    return true;
                }
            }

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

                if(OncoKB.global.genes && OncoKB.global.genes && OncoKB.global.tumorTypes) {
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

            function generateReports() {
                $scope.workers.forEach(function(worker){
                    worker.getData().then(function(){
                       worker.generateGoogleDoc().then(function(result){
                           console.log(worker);
                           $scope.progress.dynamic += 1;
                           $scope.progress.value = $scope.progress.dynamic / $scope.progress.max * 100;
                           worker.status.generated = true;
                           worker.status.generating = false;
                       });
                    });
                });
                //$timeout(function () {
                //    if(!$scope.generating) {
                //        $scope.generateIndex++;
                //        if ($scope.generateIndex < $scope.workers.length) {
                //            var worker = $scope.workers[$scope.generateIndex];
                //            $scope.sheets.arr[worker.sheet][worker.id].generating = true;
                //            $scope.generating = true;
                //            getAnnotation(worker);
                //        }else {
                //            $scope.working = false;
                //        }
                //    }else {
                //        generateReports();
                //    }
                //}, 500);
            }

            function trimAlteration(alteration) {
                if(S(alteration).startsWith('p.')) {
                    alteration = alteration.slice(2);
                }

                if(alteration.indexOf('Ter')) {
                    alteration = alteration.replace('Ter', '*');
                }
                return alteration;
            }

            function bottomObject(obj) {
                var flag = true;
                if(obj && typeof obj === 'object') {
                    for(var key in obj) {
                        if(typeof obj[key] !== 'string' && typeof obj[key] !== 'number') {
                            flag = false;
                            break;
                        }
                    }
                }else {
                    flag = false;
                }
                return flag;
            }

            function googleDocCallback(worker, status) {
                $scope.generating = false;
                $scope.progress.dynamic += 1;
                $scope.progress.value = $scope.progress.dynamic / $scope.progress.max * 100;
                $scope.sheets.arr[worker.sheet][worker.id].generating = false;
                $scope.sheets.arr[worker.sheet][worker.id].generated = status==='success'?1:-1;
                if(!$scope.hasFailed) {
                    $scope.hasFailed = status==='success'?false:true;
                }
                generateReports();
            }

            function deleteItem(key, datum, type) {
                if(type === "sheet"){
                    $scope.sheets.arr[key].forEach(function(e, i) {
                        if(e.id === datum.id) {
                            $scope.sheets.arr[key].splice(i,1);
                        }
                    });
                }else if(type === "multiV") {
                    datum.splice(key);
                }
            }

            function check(datum, checkV, attr) {
                var similarity = [],
                    same = false;

                for (var i = 0, length = $scope[checkV].length; i < length; i++) {
                    var _datum;

                    if(typeof attr !== 'undefined' && attr) {
                        _datum = $scope[checkV][i][attr];
                    }else {
                        _datum = $scope[checkV][i];
                    }


                    if(datum.toString().toUpperCase() === _datum.toString().toUpperCase()) {
                        datum = _datum;
                        same = true;
                        break;
                    }else {
                        var lavenshtein = new Levenshtein(datum, _datum);
                        similarity.push(lavenshtein.distance);
                    }
                }

                // if(same) {
                return datum;
                // }else {
                //     return datum + ' ' + noMatchSeperator;
                // }
            }



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

            function addItem(patientId, gene, mutation, tumorType) {
                $scope.multiVariants.push({
                    'patientId': patientId,
                    'gene': gene,
                    'mutation': mutation,
                    'tumorType': tumorType
                });
            }

            function objToArray(obj) {
                var delayAttrs = ['description'];
                var priorAttrs = ['trial','nccn_special','recommendation category 1 / 2A / 2 / 2A / 2A'];

                if (!angular.isObject(obj)) {
                    return obj;
                }

                var keys = Object.keys(obj).filter(function(item) {
                    return item !== '$$hashKey';
                }).sort(function(a,b) {
                    var delayIndexOfA = delayAttrs.indexOf(a),
                        delayIndexOfB = delayAttrs.indexOf(b),
                        priorIndexOfA = priorAttrs.indexOf(a),
                        priorIndexOfB = priorAttrs.indexOf(b);

                    if(priorIndexOfA !== -1 && priorIndexOfB !== -1) {
                        if(priorIndexOfA <= priorIndexOfB) {
                            return -1;
                        }else {
                            return 1;
                        }
                    }else if(priorIndexOfA !== -1) {
                        return -1;
                    }else if(priorIndexOfB !== -1) {
                        return 1;
                    }else {
                        if(delayIndexOfA !== -1 && delayIndexOfB !== -1) {
                            if(delayIndexOfA <= delayIndexOfB) {
                                return 1;
                            }else {
                                return -1;
                            }
                        }else if(delayIndexOfA !== -1) {
                            return 1;
                        }else if(delayIndexOfB !== -1) {
                            return -1;
                        }else {
                            if(a < b) {
                                return -1;
                            }else {
                                return 1;
                            }
                        }
                    }
                });

                var returnArray = keys.map(function (key) {
                    var _obj = {};

                    _obj.key = key;
                    _obj.value = FindRegex.get(obj[key]).toString();
                    return _obj;
                });

                return returnArray;
            }

            function processOverallInterpretation(str) {
                var content = str.split(/[\n\r]/g);
                for(var i=0; i< content.length; i++) {
                    if(i%2 === 0) {
                        content[i]='<b>' + content[i] + '</b>';
                    }
                }
                str = content.join('<br/>');
                return str;
            }



            $scope.init = initParams;

            $scope.generate = function() {
                if(checkEmail()) {
                    if($scope.sheets && $scope.sheets.arr) {
                        generate(getWorkers($scope.sheets.arr, 'sheet'));
                    }else if($scope.multiV && $scope.multiV.length > 0){
                        generate(getWorkers($scope.multiVariants, 'multiV'));
                    }
                }
            };

            $scope.addItem = addItem;
            $scope.deleteItem = deleteItem;

            $scope.regenerate = function() {
                if(checkEmail()) {
                    generate(getWorkers(null, null,'regenerate'));
                }
            };

            $scope.variants = {};
            $scope.multiVariants = [];
        }]);
