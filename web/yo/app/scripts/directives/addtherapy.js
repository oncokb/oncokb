'use strict';
angular.module('oncokbApp')
    .directive('addTherapy', function ($rootScope, DatabaseConnector, _, $q, FirebaseModel, firebaseConnector, drugMapUtils, $window) {
        return {
            templateUrl: 'views/addTherapy.html',
            restrict: 'E',
            scope:{
                modifyName: '=',
                tiRef: '=',
                treatmentRef: '=',
                saveCallbackToController: '&saveTherapiesCallback',
                cancelCallback: '&closeWindow'
            },
            controller: function ($scope) {
                var drugs = [];
                var drugUuids = [];
                var historicalTherapy = [];
                var oldContent='';
                function getDrugList() {
                    var defer = $q.defer();
                    firebaseConnector.ref('Drugs').on('value', function (snapshot) {
                        $scope.drugList = snapshot.val();
                        drugs = _.map(drugMapUtils.getKeysWithoutFirebasePrefix($scope.drugList), function (key) {return $scope.drugList[key];});
                        defer.resolve("Success");
                    }, function (error) {
                        defer.reject("Failed to bind drugs.");
                        $scope.therapyErrorMessage = "Sorry, loading drugs failed."
                    });
                    return defer.promise;
                }

                getDrugList().then(function () {
                    if ($scope.modifyName === true) {
                        clearData();
                        initTherapy();
                    } else {
                        clearData();
                    }
                });

                function clearData() {
                    $scope.therapy = [[]];
                    drugUuids = [[]];
                    $scope.addTherapyError = false;
                    $scope.noData = true;
                    $scope.therapyResult = "";
                }

                function reformatTherapyResult() {
                    var therapy = _.filter($scope.therapy, function (item) {
                        return item != ''
                    });
                    $scope.therapyResult = _.map(therapy, function (element) {
                        return (_.map(element, function (name) {
                            return name.drugName;
                        }).join(" + "));
                    }).join(", ");
                }

                function initTherapy() {
                    oldContent = $scope.treatmentRef.name;
                    historicalTherapy = drugMapUtils.therapyStrToArr(oldContent);
                    for (var i = 0; i < historicalTherapy.length; i++) {
                        $scope.therapy.push([]);
                        drugUuids.push([]);
                        var tem = [];
                        var temuuid = [];
                        for (var j = 0; j < historicalTherapy[i].length; j++) {
                            tem.push($scope.drugList[historicalTherapy[i][j]]);
                            temuuid.push($scope.drugList[historicalTherapy[i][j]].uuid);
                        }
                        $scope.therapy[i] = tem;
                        drugUuids[i] = temuuid;
                    }
                    reformatTherapyResult();
                }

                $scope.loadDrugs = function($query) {
                    $scope.addTherapyError = false;
                    var lowerCaseQuery = $query.toLowerCase();
                    var filteredDrugs = drugs.filter(function (drug){
                        return drug.drugName.toLowerCase().indexOf(lowerCaseQuery) !== -1 || (!drug.synonyms ? false : drug.synonyms.join(',').toLowerCase().indexOf(lowerCaseQuery) !== -1);
                    })
                    return getDrugsInOrder(lowerCaseQuery, filteredDrugs);
                };

                $scope.addDruginTherapy = function (uuid, index) {
                    $scope.noData = false;
                    drugUuids[index].push(uuid);
                    validateTherapies();
                    addTherapy(index);
                };

                $scope.removeDruginTherapy = function (uuid, index) {
                    $scope.noData = false;
                    drugUuids[index].splice(drugUuids[index].indexOf(uuid), 1);
                    validateTherapies();
                    addTherapy(index);
                };

                function getDrugsInOrder(lowerCaseQuery, filteredDrugs) {
                    _.map(filteredDrugs, function(drug){
                        var lowerCaseDrugName = drug.drugName.toLowerCase();
                        if(lowerCaseDrugName===lowerCaseQuery){
                            return drug.weight = 1.0;
                        }
                        else if(lowerCaseDrugName.startsWith(lowerCaseQuery)){
                            return drug.weight = 1.5;
                        }
                        else if(lowerCaseDrugName.indexOf(lowerCaseQuery) !== -1){
                            return drug.weight = 2.0;
                        }
                        else if(_.find(drug.synonyms, function (synonyms) {return synonyms.toLowerCase() === lowerCaseQuery}) !== -1)
                        {
                            return drug.weight = 2.5;
                        }
                        else if(_.find(drug.synonyms, function (synonyms) {return synonyms.toLowerCase().indexOf(lowerCaseQuery) !== -1 }) !== -1){
                            return drug.weight = 3.0;
                        }
                    });
                    return _.sortBy(filteredDrugs, 'weight');
                }

                function addTherapy(index) {
                    if ($scope.therapy.length === index + 1) {
                        $scope.therapy.push([]);
                        drugUuids.push([]);
                    }
                }

                function validateTherapies() {
                    $scope.addTherapyError = false;
                    var therapyVali = [];
                    drugUuids.map(function (element) {
                        var tem = element.sort().join(' ');
                        if (tem !== '')
                            therapyVali.push(tem);
                    });
                    if (_.isEmpty(therapyVali)) {
                        $scope.addTherapyError = true;
                    }
                    else if ((_.uniq(therapyVali).length) !== therapyVali.length) {
                        $scope.therapyErrorMessage = "Same Elements. Please check and save again.";
                        $scope.addTherapyError = true;
                    }
                    reformatTherapyResult();
                }

                $scope.deleteTherapy = function (index) {
                    if(!_.isEmpty($scope.therapy[index])){
                        $scope.noData = false;
                    }
                    if (index > 0) {
                        $scope.therapy.splice(index, 1);
                        drugUuids.splice(index, 1);
                    }
                    validateTherapies();
                };

                function isValidTreatment(newTreatmentName) {
                    var isValid = true;
                    if(_.find($scope.tiRef.treatments, function (treatment) {return treatment.name === newTreatmentName}) !== undefined)
                        isValid = false;
                    if (!isValid) {
                        $scope.therapyErrorMessage = "Same therapy exists.";
                        $scope.addTherapyError = true;
                    }
                    return isValid;
                }

                $scope.save = function (){
                    drugUuids = _.filter(drugUuids, function (item) {
                        return item != ''
                    });
                    var therapyString = [];
                    var indices = $scope.indices;
                    therapyString = _.map(drugUuids, function (element) {
                        return element.join(' + ').trim();
                    });
                    var newTreatmentName = therapyString.join(', ');
                    if (isValidTreatment(newTreatmentName)) {
                        drugUuids = _.flatten(drugUuids);
                        var therapyObject = {};
                        if ($scope.modifyName === true) {
                            $scope.saveTherapiesCallback(newTreatmentName, oldContent);
                            $scope.closeWindow();
                        }
                        else {
                            $scope.saveTherapiesCallback(newTreatmentName);
                        }
                        clearData();
                    }
                };

                $scope.cancel = function (){
                    $scope.closeWindow();
                };

                $scope.saveTherapiesCallback = function (newTreatmentName, oldContent) {
                    $scope.saveCallbackToController({
                        newTreatmentName: newTreatmentName,
                        oldContent: oldContent
                    })
                };

                $scope.closeWindow = function () {
                    $scope.cancelCallback();
                };

                $scope.goToDrugs = function () {
                    $window.open('#!/therapies', '_blank');
                };
            }
        }
    });
