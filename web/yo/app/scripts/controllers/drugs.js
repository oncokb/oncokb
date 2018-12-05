'use strict';

angular.module('oncokbApp')
    .controller('DrugsCtrl', ['$window', '$scope', '$rootScope', '$location', '$timeout', '$routeParams', '_', 'DTColumnDefBuilder', 'DTOptionsBuilder', 'DatabaseConnector', 'loadFiles', '$firebaseObject', '$firebaseArray', 'FirebaseModel', '$q',
        function($window, $scope, $rootScope, $location, $timeout, $routeParams, _, DTColumnDefBuilder, DTOptionsBuilder, DatabaseConnector, loadFiles, $firebaseObject, $firebaseArray, FirebaseModel, $q) {

            $scope.drugList = {};
            function getDrugList() {
                loadFiles.load(['drugs']).then(function(result) {
                    $scope.hugoSymbols = _.without(_.keys($rootScope.drugsData));
                     _.each($scope.hugoSymbols, function(hugoSymbol) {
                         $scope.drugList[hugoSymbol] = {
                                drugName: $rootScope.drugsData[hugoSymbol].drugName,
                                ncitCode: $rootScope.drugsData[hugoSymbol].ncitCode,
                                description: $rootScope.drugsData[hugoSymbol].description,
                                uuid: $rootScope.drugsData[hugoSymbol].uuid,
                                ncitName: $rootScope.drugsData[hugoSymbol].ncitName,
                                synonyms: $rootScope.drugsData[hugoSymbol].synonyms
                         };
                     });
                //$scope.status.rendering = false;
                });
            };
            getDrugList();




            // function checkSame(Code){
            //     var ref = firebase.database().ref();
            //     ref.once("value", function (element) {
            //         if(element.child(Code).exists()){
            //             console.log("exists");
            //             return true;
            //         }
            //         else{
            //             return false;
            //         }
            //     })
            // }

            function createDrug(drugName, ncitCode, synonyms, ncitName) {
                var deferred = $q.defer(); //check same，free text
                var drug = new FirebaseModel.Drug(drugName, ncitCode, synonyms, ncitName);
                var key;
                // console.log(checkSame(ncitCode, drugName));
                // if(checkSame(ncitCode, drugName) == true){
                //     console.log("meijia");
                //     dialogs.notify('Warning', 'Failed to create the drug ' + drugName + '!');
                //     deferred.reject('same element');
                // }else{
                    //console.log("jiale");
                firebase.database().ref('Drugs/'+drug.uuid).set(drug).then(function(result) {
                    deferred.resolve();
                    }, function(error) {
                    console.log(error);
                    dialogs.notify('Warning', 'Failed to create the drug ' + drugName + '!');
                    deferred.reject(error);});
                getDrugList();
                //}
                return deferred.promise;
            }

            $scope.addDrug = function (drug, preferName) {


                // firebase.database().ref.child("Drugs").orderByChild("ncitCode").equalTo("drug.ncitCode").once("value",snapshot => {
                //     const existingData = snapshot.val();
                //     if (existingData){
                //         console.log("exists!");
                //     }
                // });

                if ((drug.ncitCode == null) || (drug.ncitCode =='')){
                    preferName = drug;
                    createDrug(preferName,'','','').then(function (result) {
                        $scope.suggestedDrug = '';
                        $scope.preferName = '';
                    },
                        function (error) {
                            console.log("add unsuccessfully")
                        });
                }
                else{
                    if ((preferName == '')||(preferName == null)){
                        preferName = drug.drugName;
                    }
                    createDrug(preferName, drug.ncitCode, drug.synonyms, drug.drugName).then(function(result) {
                            $scope.suggestedDrug = '';
                            $scope.preferName = '';
                        },
                        function (error) {
                            console.log("add unsuccessfully.")
                        });
                }
            }

            // function getDrugList() {
            //     DatabaseConnector.getAllDrugs()
            //         .then(function(result){
            //             $scope.drugList = result;
            //         })
            // };


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

            $scope.saveDrugName = function (newDrugName, drug) {
                //To do; API
                //check blank
                firebase.database().ref('Drugs/' + drug.uuid +'/drugName').set(newDrugName);
                getDrugList();
                //return firebase.database().ref('Drugs/').update(updates);


                // if ((!(newDrugName === "" || newDrugName === null)) && (newDrugName !== drug.drugName)){
                //     if (_.contains(drug.synonyms, drug.drugName)){
                //         drug.drugName = newDrugName;
                //     }else{
                //         drug.synonyms.push(drug.drugName);
                //         drug.drugName = newDrugName;
                //     }
                // }
            };

            // $scope.addDrug = function (preferName, drugCode) {
            //     return DatabaseConnector.addtheDrug(preferName, drugCode, function (result) {
            //             getDrugList();
            //             $scope.suggestedDrug = "";
            //             $scope.preferName = "";
            //             $scope.addDrugMessage = "It has been added successfully.";
            //         },
            //         function (error) {
            //             $scope.addDrugMessage = "Sorry, adding the drug failed.";
            //         });
            // }

        }]
    );
