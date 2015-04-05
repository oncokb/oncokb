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
            $timeout) {

            //function generate(workers) {
            //    console.log(workers);
            //
            //    $scope.workers = workers;
            //    $scope.working = true;
            //    $scope.progress.value = 0.5;
            //    $scope.progress.dynamic = 0;
            //    $scope.progress.max = workers.length;
            //    $scope.generateIndex = -1;
            //    generateReports();
            //}
            //
            //function getWorkers(data, type, flag) {
            //    var workers = [];
            //    /* jshint -W083 */
            //    if(type === 'sheet') {
            //        rgWorkers.set($scope.sheets.arr);
            //        workers = rgWorkers.get();
            //    }else if(type === 'multiV') {
            //        rgWorkers.set(data);
            //        workers = rgWorkers.get();
            //    }
            //    /* jshint +W083 */
            //    return workers;
            //}
            //
            //function checkEmail() {
            //    if($scope.sheets.email === '') {
            //        dialogs.notify('','Please check your email address.');
            //        return false;
            //    }else {
            //        return true;
            //    }
            //}
            //
            //function generateReports() {
            //    $scope.workers.forEach(function(worker){
            //        worker.getData().then(function(){
            //            worker.generateGoogleDoc().then(function(result){
            //                console.log(worker);
            //                $scope.progress.dynamic += 1;
            //                $scope.progress.value = $scope.progress.dynamic / $scope.progress.max * 100;
            //                worker.status.generated = true;
            //                worker.status.generating = false;
            //            });
            //        });
            //    });
            //    //$timeout(function () {
            //    //    if(!$scope.generating) {
            //    //        $scope.generateIndex++;
            //    //        if ($scope.generateIndex < $scope.workers.length) {
            //    //            var worker = $scope.workers[$scope.generateIndex];
            //    //            $scope.sheets.arr[worker.sheet][worker.id].generating = true;
            //    //            $scope.generating = true;
            //    //            getAnnotation(worker);
            //    //        }else {
            //    //            $scope.working = false;
            //    //        }
            //    //    }else {
            //    //        generateReports();
            //    //    }
            //    //}, 500);
            //}
            //
            //function bottomObject(obj) {
            //    var flag = true;
            //    if(obj && typeof obj === 'object') {
            //        for(var key in obj) {
            //            if(typeof obj[key] !== 'string' && typeof obj[key] !== 'number') {
            //                flag = false;
            //                break;
            //            }
            //        }
            //    }else {
            //        flag = false;
            //    }
            //    return flag;
            //}
            //
            //function deleteItem(key, datum, type) {
            //    if(type === "sheet"){
            //        $scope.sheets.arr[key].forEach(function(e, i) {
            //            if(e.id === datum.id) {
            //                $scope.sheets.arr[key].splice(i,1);
            //            }
            //        });
            //    }else if(type === "multiV") {
            //        datum.splice(key);
            //    }
            //}
            //
            //function addItem(patientId, gene, mutation, tumorType) {
            //    $scope.multiVariants.push({
            //        'patientId': patientId,
            //        'gene': gene,
            //        'mutation': mutation,
            //        'tumorType': tumorType
            //    });
            //}
            //
            //function objToArray(obj) {
            //    var delayAttrs = ['description'];
            //    var priorAttrs = ['trial','nccn_special','recommendation category 1 / 2A / 2 / 2A / 2A'];
            //
            //    if (!angular.isObject(obj)) {
            //        return obj;
            //    }
            //
            //    var keys = Object.keys(obj).filter(function(item) {
            //        return item !== '$$hashKey';
            //    }).sort(function(a,b) {
            //        var delayIndexOfA = delayAttrs.indexOf(a),
            //            delayIndexOfB = delayAttrs.indexOf(b),
            //            priorIndexOfA = priorAttrs.indexOf(a),
            //            priorIndexOfB = priorAttrs.indexOf(b);
            //
            //        if(priorIndexOfA !== -1 && priorIndexOfB !== -1) {
            //            if(priorIndexOfA <= priorIndexOfB) {
            //                return -1;
            //            }else {
            //                return 1;
            //            }
            //        }else if(priorIndexOfA !== -1) {
            //            return -1;
            //        }else if(priorIndexOfB !== -1) {
            //            return 1;
            //        }else {
            //            if(delayIndexOfA !== -1 && delayIndexOfB !== -1) {
            //                if(delayIndexOfA <= delayIndexOfB) {
            //                    return 1;
            //                }else {
            //                    return -1;
            //                }
            //            }else if(delayIndexOfA !== -1) {
            //                return 1;
            //            }else if(delayIndexOfB !== -1) {
            //                return -1;
            //            }else {
            //                if(a < b) {
            //                    return -1;
            //                }else {
            //                    return 1;
            //                }
            //            }
            //        }
            //    });
            //
            //    var returnArray = keys.map(function (key) {
            //        var _obj = {};
            //
            //        _obj.key = key;
            //        _obj.value = FindRegex.get(obj[key]).toString();
            //        return _obj;
            //    });
            //
            //    return returnArray;
            //}
            //
            //function processOverallInterpretation(str) {
            //    var content = str.split(/[\n\r]/g);
            //    for(var i=0; i< content.length; i++) {
            //        if(i%2 === 0) {
            //            content[i]='<b>' + content[i] + '</b>';
            //        }
            //    }
            //    str = content.join('<br/>');
            //    return str;
            //}
            //
            //$scope.generate = function() {
            //    if(checkEmail()) {
            //        if($scope.sheets && $scope.sheets.arr) {
            //            generate(getWorkers($scope.sheets.arr, 'sheet'));
            //        }else if($scope.multiV && $scope.multiV.length > 0){
            //            generate(getWorkers($scope.multiVariants, 'multiV'));
            //        }
            //    }
            //};
            //
            //$scope.addItem = addItem;
            //$scope.deleteItem = deleteItem;
            //
            //$scope.regenerate = function() {
            //    if(checkEmail()) {
            //        generate(getWorkers(null, null,'regenerate'));
            //    }
            //};
            //
            //$scope.variants = {};
            //$scope.multiVariants = [];
        }]);
