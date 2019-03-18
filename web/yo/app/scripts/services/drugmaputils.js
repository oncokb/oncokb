'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.drugMapUtils
 * @description
 * # drugMapUtils
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .factory('drugMapUtils', function(OncoKB, _, $q, DatabaseConnector, $rootScope, ReviewResource, S, UUIDjs, $routeParams, firebaseConnector, firebasePathUtils) {

        function therapyStrToArr(key){
            return key.split(",").map(function(element){
                return element.trim().split(" + ")
            });
        }
        function drugUuidtoName(key, drugList){
            if (key != undefined){
                var keys = therapyStrToArr(key);
                return getDrugNameByUuids(keys, drugList);
            }
        }
        function getKeysWithoutFirebasePrefix(array){
            return _.keys(array).filter(function (item) {
                return item.indexOf("$") !== 0;
            });
        }
        function checkDifferenceBetweenTherapies(oldContent, newContent){
            var oldArray = _.flatten(therapyStrToArr(oldContent));
            var newArray = _.flatten(therapyStrToArr(newContent));
            return checkDifferenceBetweenTwoArrays(oldArray, newArray);
        }
        function changeMapByCurator(actionType, dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, content, oldContent){
            switch (actionType) {
                case 'add':
                    changeMapWhenAdd(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, null, content);
                    break;
                case 'name':
                    var difference = checkDifferenceBetweenTherapies(oldContent, content);
                    changeMapWhenRemove(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, difference.extraDrugsInOld);
                    changeTherapyNameInMapWithValidatingStatus(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, difference.sameDrugs, content);
                    changeMapWhenAdd(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, difference.extraDrugsInNew, content);
                    break;
                case 'remove':
                    var drugArray = _.flatten(therapyStrToArr(content));
                    changeMapWhenRemove(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, drugArray);
                    break;
                default:
                    break;
            }
        }

        function getDrugNameByUuids(keys, drugList) {
            return keys.map(function (element) {
                return element.map(function (key) {
                    return drugList[key].drugName;
                }).join(" + ");
            }).join(", ");
        }
        function checkDifferenceBetweenTwoArrays(oldArray, newArray){
            var difference = {
                'sameDrugs': _.intersection(oldArray, newArray),
                'extraDrugsInOld': _.difference(oldArray, newArray),
                'extraDrugsInNew': _.difference(newArray, oldArray)
            };
            return difference;
        }
        function changeMapWhenAdd(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, drugs, content){
            switch (dataType){
                case 'mutation':
                    break;
                case 'tumor':
                    break;
                case 'treatment':
                    var drugArray = [];
                    if (!drugs){
                        drugArray = _.flatten(therapyStrToArr(content));
                    }
                    else {
                        drugArray = drugs;
                    }
                    var therapyObject = {
                        'name': content,
                        'status': 'latest'
                    };
                    var mapPath = '';
                    var chainDrug = $q.when();
                    _.forEach(drugArray, function (drug) {
                        chainDrug = chainDrug.then(function(){
                            mapPath = firebasePathUtils.getTherapyMapPath(drug, geneName, mutationUuid, cancerTypeUuid, therapyUuid);
                            firebaseConnector.setMap(mapPath, therapyObject);
                            mapPath = firebasePathUtils.getMutationNameMapPath(drug, geneName, mutationUuid);
                            firebaseConnector.setMap(mapPath, mutationName);
                        });
                    });
                    break;
                default:
                    break;
            }
        }
        function changeMapWhenRemove(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, name_uuid, drugArray){
            switch (dataType){
                case 'mutation':
                    break;
                case 'tumor':
                    break;
                case 'treatment':
                    //Functions here should be optimized.
                    _.forEach(drugArray, function(drug){
                        var chainDrug = $q.when();
                        var mapPath = '';
                        chainDrug = chainDrug.then(function(){
                            mapPath = firebasePathUtils.getTherapyMapPath(drug, geneName, mutationUuid, cancerTypeUuid, name_uuid);
                            firebaseConnector.mapOnce(mapPath + '/status').then(function (result){
                                if(result === 'latest'){
                                    firebaseConnector.removeMap(mapPath).then(function () {
                                        firebaseConnector.ref(firebasePathUtils.getCancerTypesMapPath(drug, geneName, mutationUuid)).once('value', function (doc) {
                                            if (!doc.val()) {
                                                mapPath = firebasePathUtils.getMutationNameMapPath(drug, geneName, mutationUuid);
                                                firebaseConnector.removeMap(mapPath);
                                            }
                                        })
                                    })
                                }
                            })
                        })
                    });
                    break;
                default:
                    break;
            }
        }
        function changeTherapyNameInMapWithValidatingStatus(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, drugArray, content){
            switch (dataType){
                case 'mutation':
                    break;
                case 'tumor':
                    break;
                case 'treatment':
                    _.forEach(drugArray, function(drug){
                        var mapPath = '';
                        var chainDrug = $q.when();
                        chainDrug = chainDrug.then(function(){
                            mapPath = firebasePathUtils.getTherapyMapPath(drug, geneName, mutationUuid, cancerTypeUuid, therapyUuid);
                            firebaseConnector.mapOnce(mapPath + '/status').then(function (result){
                                if(result === 'latest'){
                                    mapPath = firebasePathUtils.getTherapyNameMapPath(drug, geneName, mutationUuid, cancerTypeUuid, therapyUuid);
                                    firebaseConnector.setMap(mapPath, content);
                                }
                            })
                        })
                    });
                    break;
                default:
                    break;
            }
        }
        return {
            therapyStrToArr: therapyStrToArr,
            drugUuidtoName: drugUuidtoName,
            getKeysWithoutFirebasePrefix: getKeysWithoutFirebasePrefix,
            checkDifferenceBetweenTherapies: checkDifferenceBetweenTherapies,
            changeMapByCurator: changeMapByCurator
        };

    });
