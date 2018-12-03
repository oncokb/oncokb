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

            $scope.saveDrugName = function (drug,newDrugName) {
                //To do; API
                if ((!(newDrugName === "" || newDrugName === null)) && (newDrugName !== drug.drugName)){
                    if (_.contains(drug.synonyms, drug.drugName)){
                        drug.drugName = newDrugName;
                    }else{
                        drug.synonyms.push(drug.drugName);
                        drug.drugName = newDrugName;
                    }
                }
            };
            $scope.addDrug = function (preferName, drugCode) {
                return DatabaseConnector.addtheDrug(preferName, drugCode)
            }
        }]
    );
