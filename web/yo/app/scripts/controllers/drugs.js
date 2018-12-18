'use strict';

angular.module('oncokbApp')
    .controller('DrugsCtrl', ['$window', '$scope', '$rootScope', '$location', '$timeout', '$routeParams', '_', 'DTColumnDefBuilder', 'DTOptionsBuilder', 'DatabaseConnector', 'loadFiles', '$firebaseObject', '$firebaseArray', 'FirebaseModel', '$q',
        function($window, $scope, $rootScope, $location, $timeout, $routeParams, _, DTColumnDefBuilder, DTOptionsBuilder, DatabaseConnector, loadFiles, $firebaseObject, $firebaseArray, FirebaseModel, $q) {

            $rootScope.drugList = {};
            $scope.mapList = {};
            function getDrugList() {

                loadFiles.load(['map']).then(function (result) {
                    var inforArray = new Array();
                    $scope.keys = _.without(_.keys($rootScope.mapData));
                    _.each($scope.keys, function (key) {
                        inforArray = [];
                        var genes = _.without(_.keys($rootScope.mapData[key]));
                        _.each(genes, function (gindex) {
                            var mutations = _.without(_.keys($rootScope.mapData[key][gindex]));
                            var informationtext = gindex + ": " + mutations.length + " mutation(s)";
                            inforArray.push(informationtext);
                        });
                        var inforString = inforArray.join('\n');
                        $scope.mapList[key] = {
                            key: key,
                            geneNumber: genes.length,
                            inforString: inforString
                        };
                    });
                });

                loadFiles.load(['drugs']).then(function (result) {
                    $scope.hugoSymbols = _.without(_.keys($rootScope.drugsData));
                    _.each($scope.hugoSymbols, function (hugoSymbol) {
                        $rootScope.drugList[hugoSymbol] = {
                            drugName: $rootScope.drugsData[hugoSymbol].drugName,
                            ncitCode: $rootScope.drugsData[hugoSymbol].ncitCode,
                            description: $rootScope.drugsData[hugoSymbol].description,
                            uuid: $rootScope.drugsData[hugoSymbol].uuid,
                            ncitName: $rootScope.drugsData[hugoSymbol].ncitName,
                            synonyms: $rootScope.drugsData[hugoSymbol].synonyms,
                            map: $scope.mapList[$rootScope.drugsData[hugoSymbol].uuid]
                        };
                    });
                });

            };
            getDrugList();


            function checkSame(drugName, code) {
                var isSame = false;
                // loadFiles.load(['drugs']).then(function (result) {
                //     var keys = _.without(_.keys($rootScope.drugsData));
                //     console.log(keys);
                //     _.each(keys, function (key) {
                //         console.log($rootScope.drugList[key].ncitCode);
                //         if ((code == '') || (code == null)) {
                //             console.log("1");
                //             if (drugName == $rootScope.drugList[key].drugName)
                //                 isSame = true
                //         }
                //         else if (code == $rootScope.drugList[key].ncitCode)
                //         {console.log("2");
                //             isSame = true;}
                //
                //     });
                //     console.log(isSame);
                // });
                _.each($rootScope.drugList, function (drug) {
                    if ((code == '') || (code == null)) {
                        if (drugName == drug.drugName)
                            isSame = true;
                    }
                    else if (code == drug.ncitCode){
                        isSame = true;
                    }
                })
                if(isSame){
                    return true;
                }
                return false;
            };

            function createDrug(drugName, ncitCode, synonyms, ncitName) {
                var deferred = $q.defer(); //check same，free text
                var drug = new FirebaseModel.Drug(drugName, ncitCode, synonyms, ncitName);
                //var key;
                // console.log(checkSame(ncitCode, drugName));
                // if(checkSame(ncitCode, drugName) == true){
                //     console.log("meijia");
                //     dialogs.notify('Warning', 'Failed to create the drug ' + drugName + '!');
                //     deferred.reject('same element');
                // }else{
                //console.log("jiale");
                if (checkSame(drugName, ncitCode) == false) {
                    firebase.database().ref('Drugs/' + drug.uuid).set(drug).then(function (result) {
                        deferred.resolve();
                    }, function (error) {
                        console.log(error);
                        dialogs.notify('Warning', 'Failed to create the drug ' + drugName + '!');
                        deferred.reject(error);
                    });
                    getDrugList();
                    $scope.addDrugMessage = drugName + " has been added successfully.";
                }
                //}
                else {
                    $scope.addDrugMessage = "Sorry, same drug exists.";
                    $scope.suggestedDrug = '';
                    $scope.preferName = '';
                }
                return deferred.promise;
            }

            $scope.addDrug = function (drug, preferName) {


                // firebase.database().ref.child("Drugs").orderByChild("ncitCode").equalTo("drug.ncitCode").once("value",snapshot => {
                //     const existingData = snapshot.val();
                //     if (existingData){
                //         console.log("exists!");
                //     }
                // });

                if ((drug.ncitCode == null) || (drug.ncitCode == '')) {
                    preferName = drug;
                    createDrug(preferName, '', '', '').then(function (result) {
                            $scope.suggestedDrug = '';
                            $scope.preferName = '';
                            $scope.addDrugMessage = '';
                        },
                        function (error) {
                            console.log("add unsuccessfully")
                        });
                }
                else {
                    if ((preferName == '') || (preferName == null)) {
                        preferName = drug.drugName;
                    }
                    createDrug(preferName, drug.ncitCode, drug.synonyms, drug.drugName).then(function (result) {
                            $scope.suggestedDrug = '';
                            $scope.preferName = '';
                        },
                        function (error) {
                            console.log("add unsuccessfully.")
                        });
                }
            }

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
                        }
                    )
            };

            $scope.saveDrugName = function (newDrugName, drug) {
                //To do; API
                if ((newDrugName == '') || (newDrugName == null))
                    newDrugName = drug.drugName;
                firebase.database().ref('Drugs/' + drug.uuid + '/drugName').set(newDrugName);
                getDrugList();

            };


            $scope.removeDrug = function (drug){
                firebase.database().ref('Map').once('value', function(snapshot){
                    if (snapshot.hasChild(drug.uuid)) {
                        alert("Can't delete this drug, because it is used in therapies.")
                    }
                    else{
                        firebase.database().ref('Drugs/' + drug.uuid).set(null).then();
                        alert(drug.drugName + "has been deleted." );

                    }
                });
                getDrugList();
            }

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
