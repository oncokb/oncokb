'use strict';

angular.module('oncokbApp')
    .controller('DrugsCtrl', ['$window', '$scope', '$location', '$timeout', '$routeParams', '_', 'DTColumnDefBuilder', 'DTOptionsBuilder', '$firebaseArray', 'FirebaseModel', 'firebaseConnector', '$q', 'dialogs', 'drugMapUtils', '$rootScope', 'DatabaseConnector', '$firebaseObject', 'mainUtils',
        function ($window, $scope, $location, $timeout, $routeParams, _, DTColumnDefBuilder, DTOptionsBuilder, $firebaseArray, FirebaseModel, firebaseConnector, $q, dialogs, drugMapUtils, $rootScope, DatabaseConnector, $firebaseObject, mainUtils) {
            $scope.status = {
                updatingDrugName : false
            };
            if(!$rootScope.drugList){
                // Loading all drugs info
                $firebaseObject(firebaseConnector.ref("Drugs/")).$bindTo($rootScope, "drugList").then(function () {
                }, function (error) {
                    dialogs.error('Error', 'Failed to load drugs information. Please Contact developer and stop curation.');
                });
            }
            function loadDrugTable() {
                var deferred1 = $q.defer();
                var deferred2 = $q.defer();
                var mutationsReviewed = [];
                var mutationsLatest = [];
                var geneReviewed = false;
                $scope.drugMapReviewed = {};
                $scope.drugMapLatest = {};
                firebase.database().ref('Map').on('value', function (doc) {
                    var mapList = doc.val();
                    _.each(_.keys(mapList), function (drug) {
                        $scope.drugMapReviewed[drug] = [];
                        $scope.drugMapLatest[drug] = [];
                        _.each(_.keys(mapList[drug]), function (gene) {
                            geneReviewed = false;
                            mutationsReviewed = [];
                            mutationsLatest = [];
                            _.each(_.keys(mapList[drug][gene]), function (mutationUuid) {
                                _.each(mapList[drug][gene][mutationUuid].cancerTypes, function(cancerType){
                                    _.each(cancerType, function(therapy){
                                        if(therapy.status == 'reviewed'){
                                            geneReviewed = true;
                                            mutationsReviewed.push(mapList[drug][gene][mutationUuid].mutationName);
                                        }
                                        else {
                                            mutationsLatest.push(mapList[drug][gene][mutationUuid].mutationName);
                                        }
                                    })
                                } )
                            });
                            mutationsReviewed = _.uniq(mutationsReviewed);
                            mutationsLatest = _.uniq(mutationsLatest);
                            var mapInformation = {
                                geneName: gene,
                                geneLink: "#!/gene/" + gene,
                                mutationNumber: mutationsReviewed.length,
                                mutationInfo: mutationsReviewed.join('; ')
                            };
                            if(geneReviewed){
                                $scope.drugMapReviewed[drug].push(mapInformation);
                            }
                            else{
                                $scope.drugMapLatest[drug].push(mapInformation);
                            }
                        });
                    });
                    deferred2.resolve();
                }, function (error) {
                    deferred2.reject(error);
                });
                var bindingAPI = [deferred1.promise, deferred2.promise];
                $q.all(bindingAPI)
                    .then(function (result) {
                    }, function (error) {
                        dialogs.notify('Warning','Sorry, the drug page doesn\'t work well. Please contact developers.');
                    });
            }
            loadDrugTable();


            function hasSameName(newDrugName, uuid) {
                return _.some(drugMapUtils.getKeysWithoutFirebasePrefix($rootScope.drugList), function(key){
                    if(($rootScope.drugList[key].uuid !== uuid && (newDrugName === $rootScope.drugList[key].drugName || $rootScope.drugList[key].synonyms !== undefined && $rootScope.drugList[key].synonyms.indexOf(newDrugName) > -1)) === true){
                        return key;
                    }
                });
            }

            function modalError(errorTitle, errorMessage, sameName, deleteDrug, drugUuid, genes) {
                var dlgfortherapy = dialogs.create('views/modalError.html', 'ModalErrorCtrl', {
                        errorTitle: errorTitle,
                        errorMessage: errorMessage,
                        sameName: sameName,
                        deleteDrug: deleteDrug,
                        drugUuid: drugUuid,
                        genes: genes
                    },
                    {
                        size: 'sm'
                    });
            }

            $scope.saveDrugName = function (newDrugName, drug) {
                if(newDrugName && (newDrugName !== drug.drugName)){
                    if (hasSameName(newDrugName, drug.uuid)) {
                        modalError("Sorry", "Same name exists.", true, false, drug.uuid);
                    } else {
                        $scope.status.updatingDrugName = true;
                        var content = drug.drugName + " has been changed to " + newDrugName + ". Its NCI treasure code is " + drug.ncitCode + ".";
                        firebaseConnector.setDrugName(drug.uuid, newDrugName).then(function() {
                            DatabaseConnector.updateDrugPreferredName(drug.ncitCode, newDrugName)
                                .then(function(value) {
                                }, function() {
                                    dialogs.error('Error', 'System cannot update the drug preferred name. Please Contact developer and stop curation.');
                                })
                                .finally(function() {
                                    $scope.status.updatingDrugName = false;
                                });
                        }, function(reason) {
                            // something goes wrong then the data in database should not be updated.
                        });
                        mainUtils.sendEmailtoMultipulUsers(['kundrar@mskcc.org', 'chakravd@mskcc.org', 'nissanm@mskcc.org'], 'Reminder: A therapy preferred name changed.', content);
                    }
                }
            };

            $scope.checkDrugInUse = function (uuid) {
                return !($scope.drugMapLatest[uuid] || $scope.drugMapReviewed[uuid]);
            };

            $scope.removeDrug = function (drug) {
                if ($scope.drugMapLatest[drug.uuid]) {
                    var genes = _.map($scope.drugMapLatest[drug.uuid], function(gene){
                        return gene.geneName
                    });
                    modalError("Sorry", "Can't delete this therapy, because it is found in the following gene pages, though therapies haven't been reviewed yet.", false, false, drug.uuid, genes);
                }
                else if ($scope.drugMapReviewed[drug.uuid]) {
                    var genes = _.map($scope.drugMapReviewed[drug.uuid], function(gene){
                        return gene.geneName
                    });
                    modalError("Sorry", "Can't delete this therapy, because it is found in the following gene pages.", false, false, drug.uuid, genes);
                }
                else {
                    modalError("Attention", drug.drugName + " will be deleted.", false, true, drug.uuid);
                }
            };

            $scope.generateSynonyms = function (drug) {
                if (_.indexOf(drug.synonyms, drug.ncitName) > -1){
                    return drug.synonyms;
                }
                else return _.concat(drug.ncitName, drug.synonyms);
            };
        }]
    )
    .controller('ModalErrorCtrl', function ($scope, $modalInstance, data, firebaseConnector) {
        $scope.errorTitle = data.errorTitle;
        $scope.errorMessage = data.errorMessage;
        $scope.sameName = data.sameName;
        $scope.deleteDrug = data.deleteDrug;
        if(data.genes){
            $scope.drugGenes = data.genes.join(', ');
        }
        $scope.cancel = function () {
            $modalInstance.dismiss('canceled');
        };
        $scope.confirm = function () {
            firebaseConnector.removeDrug(data.drugUuid);
            $modalInstance.dismiss('canceled');
        };
    });


