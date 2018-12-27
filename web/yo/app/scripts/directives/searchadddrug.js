'use strict';
angular.module('oncokbApp')
    .directive('searchAddDrug', function (DatabaseConnector, dialogs, _, mainUtils, $q, FirebaseModel, firebaseConnector) {
        return {
            templateUrl: 'views/searchAddDrug.html',
            restrict: 'E',
            controller: function ($scope) {
                function checkSameDrug(drugName, code) {
                    return _.some(mainUtils.getKeysWithoutFirebasePrefix($scope.drugList), (key) => ((code === '' && drugName === $scope.drugList[key].drugName) || (code !== '' && code === $scope.drugList[key].ncitCode)) === true);
                }

                function createDrug(drugName, ncitCode, synonyms, ncitName) {
                    ncitCode = undefinedToEmptyString(ncitCode);
                    synonyms = undefinedToEmptyString(synonyms);
                    ncitName = undefinedToEmptyString(ncitName);
                    var deferred = $q.defer();
                    if (($scope.drugList === undefined) || (checkSameDrug(drugName, ncitCode) === false)) {
                        var drug = new FirebaseModel.Drug(drugName, ncitCode, synonyms, ncitName);
                        firebaseConnector.addDrug(drug.uuid, drug).then(function (result) {
                            deferred.resolve();
                            $scope.addDrugMessage = drugName + " has been added successfully.";
                        }, function (error) {
                            $scope.addDrugMessage = 'Failed to create the drug ' + drugName + '! Please contact developers.';
                            deferred.reject(error);
                        });
                    }
                    else {
                        $scope.addDrugMessage = "Sorry, same drug exists.";
                        $scope.suggestedDrug = '';
                        $scope.preferName = '';
                    }
                    return deferred.promise;
                }

                function undefinedToEmptyString(item) {
                    if(!item){
                        item = '';
                    }
                    return item;
                }

                $scope.addDrug = function (drug, preferName) {
                    if(drug !== ''){
                        if (!drug.ncitCode) {
                            preferName = drug;
                            createDrug(preferName).then(function (result) {
                                    $scope.suggestedDrug = '';
                                    $scope.preferName = '';});
                        }
                        else {
                            if (!preferName) {
                                preferName = drug.drugName;
                            }
                            createDrug(preferName, drug.ncitCode, drug.synonyms, drug.drugName).then(function (result) {
                                    $scope.suggestedDrug = '';
                                    $scope.preferName = '';});
                        }
                    }
                };

                $scope.processSearchDrugs = function (keyword) {
                    return DatabaseConnector.searchDrugs(keyword)
                        .then(
                            function (result) {
                                $scope.searchDrugsError = false;
                                return result;
                            })
                        .catch(
                            function (error) {
                                $scope.searchDrugsError = true;
                                return [];
                            }
                        )
                };

            }
        };
    });
