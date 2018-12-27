'use strict';
angular.module('oncokbApp')
    .directive('addModifyTherapy', function ($rootScope, DatabaseConnector, _, $q, FirebaseModel, firebaseConnector, mainUtils, $window) {
        return {
            templateUrl: 'views/addModifyTherapy.html',
            restrict: 'E',
            controller: function ($scope) {
                var drugs = [];
                var therapyUuid = [];
                function getDrugList() {
                    var defer = $q.defer();
                    firebaseConnector.ref('Drugs').on('value', function (snapshot) {
                        $scope.drugList = snapshot.val();
                        drugs = _.map(mainUtils.getKeysWithoutFirebasePrefix($scope.drugList), function (key) {return $scope.drugList[key];});
                        defer.resolve("Success");
                    }, function (error) {
                        defer.reject("Failed to bind drugs.");
                        $scope.therapyErrorMessage = "Sorry, loading drugs failed."
                    });
                    return defer.promise;
                }

                getDrugList().then(function () {
                    if ($scope.modifyMode === true) {
                        clearData();
                        initTherapy();
                    } else {
                        clearData();
                    }
                });

                function clearData() {
                    $scope.therapy = [[]];
                    therapyUuid = [[]];
                    $scope.addTherapyError = false;
                    $scope.noData = true;
                    $scope.therapyResult = "";
                }

                function reformatTherapyResult() {
                    $scope.therapyResult = _.map($scope.therapy, function (element) {
                        return (_.map(element, function (name) {
                            return name.drugName;
                        }).join(" + "));
                    }).join(", ");
                }

                function initTherapy() {
                    var newTherapy = [];
                    newTherapy = mainUtils.therapyStrToArr($scope.treatmentRef.name);
                    for (var i = 0; i < newTherapy.length; i++) {
                        $scope.therapy.push([]);
                        therapyUuid.push([]);
                        var tem = [];
                        var temuuid = [];
                        for (var j = 0; j < newTherapy[i].length; j++) {
                            newTherapy[i][j] = newTherapy[i][j].toString();
                            tem.push($scope.drugList[newTherapy[i][j]]);
                            temuuid.push($scope.drugList[newTherapy[i][j]].uuid);
                        }
                        $scope.therapy[i] = tem;
                        therapyUuid[i] = temuuid;
                    }
                    reformatTherapyResult();
                }

                $scope.loadDrugs = function($query) {
                    $scope.addTherapyError = false;
                    return drugs.filter(function (drug) {
                        var lowerCaseQuery = $query.toLowerCase();
                        return drug.drugName.toLowerCase().indexOf(lowerCaseQuery) != -1 || (!drug.synonyms ? false : drug.synonyms.join(',').toLowerCase().indexOf(lowerCaseQuery) != -1);
                    })
                };

                $scope.addDruginTherapy = function (uuid, index) {
                    $scope.noData = false;
                    therapyUuid[index].push(uuid);
                    validateTherapies();
                    addTherapy(index);
                };

                $scope.removeDruginTherapy = function (uuid, index) {
                    $scope.noData = false;
                    therapyUuid[index].splice(therapyUuid[index].indexOf(uuid), 1);
                    validateTherapies();
                    addTherapy(index);
                };

                function addTherapy(index) {
                    if ($scope.therapy.length === index + 1) {
                        $scope.therapy.push([]);
                        therapyUuid.push([]);
                    }
                }

                function validateTherapies() {
                    $scope.addTherapyError = false;
                    var therapyVali = [];
                    therapyUuid.map(function (element) {
                        var tem = element.sort().join(' ');
                        if (tem !== '')
                            therapyVali.push(tem);
                    });
                    if ((_.uniq(therapyVali).length) !== therapyVali.length) {
                        $scope.therapyErrorMessage = "Same Elements. Please check and save again.";
                        $scope.addTherapyError = true;
                    }
                    reformatTherapyResult();
                }

                $scope.deleteTherapy = function (index) {
                    if (index > 0) {
                        $scope.therapy.splice(index, 1);
                        therapyUuid.splice(index, 1);
                    }
                    validateTherapies();
                };

                function isValidTreatment(indices, newTreatmentName) {
                    var isValid = true;
                    if(_.find($scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]].treatments, function (treatment) {return treatment.name === newTreatmentName}) !== undefined)
                        isValid = false;
                    if (!isValid) {
                        $scope.therapyErrorMessage = "Same therapy exists.";
                        $scope.addTherapyError = true;
                    }
                    return isValid;
                }

                $scope.save = function () {
                    therapyUuid = _.filter(therapyUuid, function (item) {
                        return item != ''
                    });
                    var therapyString = [];
                    var indices = $scope.indices;
                    therapyString = _.map(therapyUuid, function (element) {
                        return element.join(' + ').trim();
                    });
                    var newTreatmentName = therapyString.join(', ');
                    if (isValidTreatment(indices, newTreatmentName)) {
                        therapyUuid = _.flatten(therapyUuid);
                        if ($scope.modifyMode === true) {
                            $scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]].treatments[indices[3]].name = newTreatmentName;
                            var name_uuid = $scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]].treatments[indices[3]].name_uuid;
                            mainUtils.setUUIDInReview(name_uuid);
                            $scope.$$prevSibling.indicateTumorContent($scope.tumorRef);
                            $scope.closeWindow();
                        }
                        else {
                            var treatment = new FirebaseModel.Treatment(newTreatmentName);
                            treatment.name_review = {
                                updatedBy: $rootScope.me.name,
                                updateTime: new Date().getTime(),
                                added: true
                            };
                            if (!$scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]].treatments) {
                                firebaseConnector.addTreatment($scope.path, treatment);
                            }
                            else {
                                $scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]].treatments.push(treatment);
                            }
                            $scope.indicateTumorContent($scope.tumorRef);
                            mainUtils.setUUIDInReview(treatment.name_uuid);
                        }
                        clearData();
                    }
                };

                $scope.cancel = function () {
                    $scope.closeWindow();
                };

                $scope.goToDrugs = function () {
                    $window.open('#!/drugs', '_blank');
                };
            }
        }
    });
