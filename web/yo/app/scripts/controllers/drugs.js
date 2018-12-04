'use strict';

angular.module('oncokbApp')
    .controller('DrugsCtrl', ['$window', '$scope', '$rootScope', '$location', '$timeout', '$routeParams', '_', 'DTColumnDefBuilder', 'DTOptionsBuilder', 'DatabaseConnector',
        function($window, $scope, $rootScope, $location, $timeout, $routeParams, _, DTColumnDefBuilder, DTOptionsBuilder, DatabaseConnector) {

            function getDrugList() {
                DatabaseConnector.getAllDrugs()
                    .then(function(result){
                        $scope.drugList = result;
                    })
            };
            getDrugList();

            $scope.processSearchDrugs = function(keyword) {
                return DatabaseConnector.searchDrugs(keyword)
                    .then(
                        function(result) {
                            $scope.searchDrugsError = false;
                            return result;
                        })
                    .catch(
                        function (error) {
                            $scope.searchDrugsError = true;
                        }
                    )
            };

            $scope.saveDrugName = function (newDrugName, id) {
                //To do; API
                return DatabaseConnector.updateDrugName(newDrugName, id, function (result) {
                        console.log("update successfully");
                    },
                    function (error) {
                        console.log("failed");
                    });


                // if ((!(newDrugName === "" || newDrugName === null)) && (newDrugName !== drug.drugName)){
                //     if (_.contains(drug.synonyms, drug.drugName)){
                //         drug.drugName = newDrugName;
                //     }else{
                //         drug.synonyms.push(drug.drugName);
                //         drug.drugName = newDrugName;
                //     }
                // }


            };

            $scope.addDrug = function (preferName, drugCode) {
                return DatabaseConnector.addtheDrug(preferName, drugCode, function (result) {
                        getDrugList();
                        $scope.suggestedDrug = "";
                        $scope.preferName = "";
                        $scope.addDrugMessage = "It has been added successfully.";
                    },
                    function (error) {
                        $scope.addDrugMessage = "Sorry, adding the drug failed.";
                    });
            }

        }]
    );
