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
            }
            loadDrugTable();

            function hasSameName(newDrugName, uuid) {
                return _.some(mainUtils.getKeysWithoutFirebasePrefix($scope.drugList), (key) => ($scope.drugList[key].uuid !== uuid && (newDrugName === $scope.drugList[key].drugName || $scope.drugList[key].synonyms !== undefined && $scope.drugList[key].synonyms.indexOf(newDrugName) > -1)) === true);
            }

            function modalError(errorTitle, errorMessage, deleteDrug, drug) {
                var dlgfortherapy = dialogs.create('views/modalError.html', 'ModalErrorCtrl', {
                        errorTitle: errorTitle,
                        errorMessage: errorMessage,
                        deleteDrug: deleteDrug,
                        drug: drug
                    },
                    {
                        size: 'sm'
                    });
            }

            $scope.saveDrugName = function (newDrugName, drug) {
                if (hasSameName(newDrugName, drug.uuid)) {
                    modalError("Sorry", "Same name exists.", false, drug);
                } else {
                    if (!newDrugName)
                        newDrugName = drug.drugName;
                    firebaseConnector.set('Drugs/' + drug.uuid + '/drugName', newDrugName);
                }
            };
        }]
    )
    .controller('ModalErrorCtrl', function ($scope, $modalInstance, data) {
        $scope.errorTitle = data.errorTitle;
        $scope.errorMessage = data.errorMessage;
        $scope.deleteDrug = data.deleteDrug;
        $scope.cancel = function () {
            $modalInstance.dismiss('canceled');
        };
        $scope.confirm = function () {
            firebaseConnector.set('Drugs/' + data.drug.uuid, null);
            $modalInstance.dismiss('canceled');
        };
    });


