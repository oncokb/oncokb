'use strict';

angular.module('oncokbApp')
    .controller('DrugsCtrl', ['$window', '$scope', '$location', '$timeout', '$routeParams', '_', 'DTColumnDefBuilder', 'DTOptionsBuilder', '$firebaseObject', '$firebaseArray', 'FirebaseModel', 'firebaseConnector', '$q', 'dialogs', 'mainUtils',
        function ($window, $scope, $location, $timeout, $routeParams, _, DTColumnDefBuilder, DTOptionsBuilder, $firebaseObject, $firebaseArray, FirebaseModel, firebaseConnector, $q, dialogs, mainUtils) {
            function loadDrugTable() {
                var deferred1 = $q.defer();
                $firebaseObject(firebaseConnector.ref("Drugs/")).$bindTo($scope, "drugList").then(function () {
                    deferred1.resolve();
                }, function (error) {
                    deferred1.reject(error);
                });
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
                            var mapInformation = {
                                geneName: gene,
                                geneLink: "#!/gene/" + gene,
                                mutationNumber: mutationsReviewed.length,
                                mutationInfo: mutationsReviewed,
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
                return _.some(mainUtils.getKeysWithoutFirebasePrefix($scope.drugList), (key) => ($scope.drugList[key].uuid !== uuid && (newDrugName === $scope.drugList[key].drugName || $scope.drugList[key].synonyms !== undefined && $scope.drugList[key].synonyms.indexOf(newDrugName) > -1)) === true);
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
                if (hasSameName(newDrugName, drug.uuid)) {
                    modalError("Sorry", "Same name exists.", true, false, drug.uuid);
                } else {
                    if (!newDrugName)
                        newDrugName = drug.drugName;
                    firebaseConnector.setDrugName(drug.uuid, newDrugName);
                }
            };

            $scope.removeDrug = function (drug) {
                if ($scope.drugMapLatest[drug.uuid]) {
                    var genes = _.map($scope.drugMapLatest[drug.uuid], (gene) => {
                        return gene.geneName
                    });
                    modalError("Sorry", "Can't delete this therapy, because it is found in the following gene pages, though therapies haven't been reviewed yet.", false, false, drug.uuid, genes);
                }
                else if ($scope.drugMapReviewed[drug.uuid]) {
                    var genes = _.map($scope.drugMapReviewed[drug.uuid], (gene) => {
                        return gene.geneName
                    });
                    modalError("Sorry", "Can't delete this therapy, because it is found in the following gene pages.", false, false, drug.uuid, genes);
                }
                else {
                    modalError("Attention", drug.drugName + " will be deleted.", false, true, drug.uuid);
                }
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
