/**
 * @ngdoc service
 * @name oncokbApp.firebaseConnector
 * @author Jing Su on 2019/01/23
 * @description
 * # FirebaseConnector
 * This service is used for encapsulating all manipulations related to firebase.
 */

'use strict';

angular.module('oncokbApp')
    .service('firebaseConnector', function firebaseConnector($q) {
        function once(path) {
            var defer = $q.defer();
            firebase.database().ref(path).once('value', function(doc) {
                defer.resolve(doc.val());
            }, function (error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function ref(path) {
            return firebase.database().ref(path);
        }
        function set(path, data) {
            var defer = $q.defer();
            firebase.database().ref(path).set(data).then(function (result) {
                defer.resolve(result);
            }, function (error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function off(path) {
            firebase.database().ref(path).off();
        }
        function remove(path) {
            firebase.database().ref(path).remove();
        }
        function update(path, data) {
            var defer = $q.defer();
            firebase.database().ref(path).update(data).then(function() {
                defer.resolve();
            }, function(error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function addDrug(uuid, drug) {
            return set('Drugs/' + uuid, drug);
        }
        function setDrugName(uuid, drugName) {
            return set('Drugs/' + uuid + '/drugName', drugName);
        }
        function removeDrug(uuid){
            return remove('Drugs/' + uuid);
        }
        function addTreatment(path, treatment){
            return set(path + "/treatments/0", treatment);
        }
        function mapOnce(path){
            var defer = $q.defer();
            firebase.database().ref('Map/' + path).once('value', function(doc) {
                defer.resolve(doc.val());
            }, function (error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function setMap(path, name){
            return set('Map/' + path, name);
        }
        function removeMap(path){
            return set('Map/' + path, null);
        }
        function createSetting(setting) {
            return set('Setting', setting);
        }
        function addAttributeInSetting(path, attribute) {
            return set('Setting/' + path, attribute);
        }
        function removeAttributeFromSetting(path) {
            return remove('Setting/' + path);
        }

        return {
            ref: ref,
            set: set,
            off: off,
            once: once,
            remove: remove,
            update: update,
            addDrug: addDrug,
            setDrugName: setDrugName,
            removeDrug: removeDrug,
            addTreatment: addTreatment,
            mapOnce:mapOnce,
            setMap: setMap,
            removeMap: removeMap,
            createSetting: createSetting,
            addAttributeInSetting: addAttributeInSetting,
            removeAttributeFromSetting: removeAttributeFromSetting
        };
    });
