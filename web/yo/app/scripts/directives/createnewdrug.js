'use strict';
/*@Description
The component is for creating a new drug in Firebase. The curator can search and choose one drug from drop down list (Drugs in the dropdown list are from NCI website) or they can do free text if there is no result.
* */
angular.module('oncokbApp')
    .directive('createNewDrug', function (DatabaseConnector, dialogs, _, drugMapUtils, $q, FirebaseModel, firebaseConnector, mainUtils) {
        return {
            templateUrl: 'views/createNewDrug.html',
            restrict: 'E',
            controller: function ($scope) {
                function checkSameDrug(drugName, code) {
                    return _.some(drugMapUtils.getKeysWithoutFirebasePrefix($scope.drugList), function(key){
                        if(((code === '' && drugName === $scope.drugList[key].drugName) || (code !== '' && code === $scope.drugList[key].ncitCode)) === true){
                            return key;
                        }
                    });
                }

                function createDrug(drugName, ncitCode, synonyms, ncitName) {
                    ncitCode = undefinedToEmptyString(ncitCode);
                    synonyms = undefinedToEmptyString(synonyms);
                    ncitName = undefinedToEmptyString(ncitName);
                    var deferred = $q.defer();
                    if (($scope.drugList === undefined) || (checkSameDrug(drugName, ncitCode) === false)) {
                        var drug = new FirebaseModel.Drug(drugName, ncitCode, synonyms, ncitName);
                        if(!ncitCode){
                            var content = drugName + " has been added. It doesn't have NCI treasure code.";
                        }else{
                            var content = drugName + " has been added. Its NCI treasure code is " + ncitCode + ".";
                        }
                        firebaseConnector.addDrug(drug.uuid, drug).then(function (result) {
                            deferred.resolve();
                            $scope.addDrugMessage = drugName + " has been added successfully.";
                        }, function (error) {
                            $scope.addDrugErrorMessage = 'Failed to create the drug ' + drugName + '! Please contact developers.';
                            deferred.reject(error);
                        });
                        mainUtils.sendEmailtoMultipulUsers(['kundrar@mskcc.org', 'chakravd@mskcc.org', 'nissanm@mskcc.org'], 'Reminder: A therapy has been added.', content);
                    }
                    else {
                        $scope.addDrugErrorMessage = "Sorry, same drug exists.";
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
                    $scope.addDrugMessage = '';
                    $scope.addDrugErrorMessage = '';
                    return DatabaseConnector.searchNCITDrugs(keyword)
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
