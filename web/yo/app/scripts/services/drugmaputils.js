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

        function therapyStrToArr(key) {
            if (key) {
                return key.split(",").map(function (element) {
                    return element.trim().split(" + ")
                });
            } else {
                return [];
            }
        }

        function drugUuidtoName(key, drugList) {
            if (key != undefined) {
                var keys = therapyStrToArr(key);
                return getDrugNameByUuids(keys, drugList);
            } else {
                return key;
            }
        }

        function drugUuidtoDrug(key, drugList) {
            if (key != undefined) {
                var keys = therapyStrToArr(key);
                return getDrugsByUuids(keys, drugList);
            } else {
                return {};
            }
        }

        function getKeysWithoutFirebasePrefix(array) {
            if (array) {
                return _.keys(array).filter(function (item) {
                    return item.indexOf("$") !== 0;
                });
            } else {
                return array;
            }
        }

        function checkDifferenceBetweenTherapies(oldContent, newContent) {
            if (oldContent || newContent) {
                var oldArray = _.flatten(therapyStrToArr(oldContent));
                var newArray = _.flatten(therapyStrToArr(newContent));
                return checkDifferenceBetweenTwoArrays(oldArray, newArray);
            } else {
                return {};
            }
        }
        function changeMapByCurator(actionType, dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, content, oldContent){
            switch (actionType) {
                case 'add':
                    changeMapWhenAdd(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, null, content);
                    break;
                case 'name':
                    var difference = checkDifferenceBetweenTherapies(oldContent, content);
                    changeMapWhenRemove(dataType, geneName, mutationUuid, cancerTypeUuid, therapyUuid, difference.extraDrugsInOld);
                    changeTherapyNameInMapWithValidatingStatus(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, difference.sameDrugs, content);
                    changeMapWhenAdd(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, difference.extraDrugsInNew, content);
                    break;
                case 'remove':
                    //Map will be handled by changeMapWhenDeleteSection Function.
                    break;
                default:
                    break;
            }
        }

        function changeMapWhenDeleteSection(dataType, geneName, mutation, tumor, treatment, mapInfo){
            switch (dataType){
                case 'mutation':
                    _.each(mutation.tumors, function (tumor){
                        var mapInfo = {
                            'mutationUuid': mutation.name_uuid,
                            'cancerTypeUuid': null
                        }
                        changeMapWhenDeleteSection('tumor', geneName, mutation, tumor, null, mapInfo);
                    });
                    break;
                case 'tumor':
                    if(!mapInfo){
                        var mapInfo = {
                            'mutationUuid': mutation.name_uuid,
                            'cancerTypeUuid': tumor.cancerTypes_uuid
                        }
                    }
                    else{
                        var mapInfo = mapInfo;
                        mapInfo.cancerTypeUuid = tumor.cancerTypes_uuid;
                    }
                    _.each(tumor.TIs, function(ti){
                        _.each(ti.treatments, function(treatment){
                            changeMapWhenDeleteSection('treatment', geneName, mutation, tumor, treatment, mapInfo);
                        })
                    });
                    break;
                case 'treatment':
                    if(!mapInfo){
                        var mapInfo = {
                            'mutationUuid': mutation.name_uuid,
                            'cancerTypeUuid': tumor.cancerTypes_uuid
                        }
                    }
                    var therapyUuids = _.flatten(therapyStrToArr(treatment.name));
                    changeMapWhenRemove('treatment', geneName, mapInfo.mutationUuid, mapInfo.cancerTypeUuid, treatment.name_uuid, therapyUuids);
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

        function getDrugsByUuids(keys, drugList) {
            return keys.map(function (element) {
                return element.map(function (key) {
                    return drugList[key];
                });
            });
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
        function changeMapWhenRemove(dataType, geneName, mutationUuid, cancerTypeUuid, name_uuid, drugArray){
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
            drugUuidtoDrug: drugUuidtoDrug,
            getKeysWithoutFirebasePrefix: getKeysWithoutFirebasePrefix,
            checkDifferenceBetweenTherapies: checkDifferenceBetweenTherapies,
            changeMapByCurator: changeMapByCurator,
            changeMapWhenDeleteSection: changeMapWhenDeleteSection
        };

    });
