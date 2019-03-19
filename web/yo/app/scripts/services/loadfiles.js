/**
 * Created by jiaojiao on 10/24/17.
 */
'use strict';

/**
 * @ngdoc service
 * @name oncokb.loadFiles
 * @description
 * # Additional Files, currently including meta and queues.
 * Service in the oncokb.
 */
angular.module('oncokbApp')
    .service('loadFiles', function loadFiles($rootScope, $q, mainUtils, dialogs, $timeout, DatabaseConnector,
                                             $firebaseObject, FirebaseModel, firebaseConnector) {
        function load(types, data) {
            function loadMeta() {
                var metaDefer = $q.defer();
                var ref = firebase.database().ref('Meta');
                ref.on('value', function(doc) {
                    if ($rootScope.me.admin) {
                        $rootScope.metaData = doc.val();
                    } else if (!_.isUndefined($rootScope.me.genes)) {
                        if ($rootScope.me.genes.read === 'all') {
                            $rootScope.metaData = doc.val();
                        } else {
                            var hugoSymbols = $rootScope.me.genes.read.split(',');
                            var metas = {};
                            _.each(hugoSymbols,function(hugoSymbol) {
                                metas[hugoSymbol] = doc.val()[hugoSymbol];
                            });
                            $rootScope.metaData = metas;
                        }
                    }
                    metaDefer.resolve('success');
                    if ($rootScope.internal && !_.isUndefined($rootScope.apiData)) {
                        synchronizeData();
                    }
                }, function(error) {
                    metaDefer.reject('Fail to load queues file');
                });
                return metaDefer.promise;
            }
            function loadCollaborators() {
                var collaboratorsDefer = $q.defer();
                if ($rootScope.collaboratorsMeta) {
                    collaboratorsDefer.resolve('success');
                } else {
                    $firebaseObject(firebase.database().ref('Meta/collaborators')).$bindTo($rootScope, "collaboratorsMeta").then(function () {
                        collaboratorsDefer.resolve('success');
                    }, function (error) {
                        collaboratorsDefer.reject('Failed to bind meta firebase object');
                    });
                }
                return collaboratorsDefer.promise;
            }
            function loadReviewMeta(hugoSymbol) {
                var reviewMetaDefer = $q.defer();
                firebase.database().ref('Meta/'+hugoSymbol+'/review').on('value', function(doc) {
                    $rootScope.reviewMeta = doc.val();
                    reviewMetaDefer.resolve('success');
                }, function () {
                    reviewMetaDefer.reject('Failed to bind meta firebase object');
                });
                return reviewMetaDefer.promise;
            }
            function loadMovingMeta(hugoSymbol) {
                var movingSectionDefer = $q.defer();
                firebase.database().ref('Meta/'+hugoSymbol).on('value', function(doc) {
                    $rootScope.movingSection = doc.val().movingSection;
                    movingSectionDefer.resolve('success');
                }, function () {
                    movingSectionDefer.reject('Failed to bind meta firebase object');
                });
                return movingSectionDefer.promise;
            }
            function loadQueues() {
                var queuesDefer = $q.defer();
                var ref = firebase.database().ref('Queues');
                ref.on('value', function(doc) {
                    $rootScope.firebaseQueues = doc.val();
                    queuesDefer.resolve('success');
                }, function(error) {
                    queuesDefer.reject('Fail to load queues file');
                });
                return queuesDefer.promise;
            }
            function loadHistory() {
                var historyDefer = $q.defer();
                var ref = firebase.database().ref('History');
                ref.on('value', function(doc) {
                    $rootScope.historyData = doc.val();
                    historyDefer.resolve('success');
                }, function(error) {
                    historyDefer.reject('Fail to load history file');
                });
                return historyDefer.promise;
            }
            function loadSetting() {
                var settingDefer = $q.defer();
                // We need to update rootscope.setting when that is changed in firebase, so we call firebase.on
                // directly instead of using firebaseconnector.on.
                $rootScope.setting = new FirebaseModel.Setting();
                firebase.database().ref('Setting').on('value', function(doc) {
                    if (doc.exists()) {
                        var fbSetting = doc.val();
                        _.forEach(_.union(_.keys($rootScope.setting), _.keys(fbSetting)), function(key){
                            if (fbSetting[key] ) {
                                if ($rootScope.setting[key]) {
                                    $rootScope.setting[key] = fbSetting[key];
                                } else {
                                    firebaseConnector.removeAttributeFromSetting(key);
                                }
                            } else if ($rootScope.setting[key]) {
                                firebaseConnector.addAttributeInSetting(key, $rootScope.setting[key]);
                            }
                        });
                        settingDefer.resolve('success');
                    } else {
                        firebaseConnector.createSetting($rootScope.setting);
                    }
                }, function(error) {
                    settingDefer.reject('Fail to load setting file');
                });
                return settingDefer.promise;
            }
            /**
             * Loop through api calls recorded in the meta file and update it to database every 5 mins
             * **/
            function synchronizeData() {
                var hugoSymbols = _.keys($rootScope.apiData);
                _.each(hugoSymbols, function(hugoSymbol) {
                    if (!_.isUndefined($rootScope.apiData[hugoSymbol]['vus'])) {
                        updateByType('vus', hugoSymbol, $rootScope.apiData[hugoSymbol]['vus']);
                    }
                    // TODO
                    // updateByType('priority', hugoSymbol, $rootScope.apiData.get(hugoSymbol).get('priority'));
                    // updateByType('drug', hugoSymbol, $rootScope.apiData.get(hugoSymbol).get('drug'));
                });
            }
            function updateByType(type, hugoSymbol, data) {
                if (type === 'vus') {
                    DatabaseConnector.updateVUS(hugoSymbol, data, function() {
                        delete $rootScope.apiData[hugoSymbol]['vus'];
                    });
                } else if (type === 'priority') {
                    // TODO
                } else if (type === 'drug') {
                    // TODO
                }
            }
            var deferred = $q.defer();
            var apiCalls = [];
            if (types.indexOf('meta') !== -1) {
                apiCalls.push(loadMeta());
            }
            if (types.indexOf('collaborators') !== -1) {
                apiCalls.push(loadCollaborators());
            }
            if (types.indexOf('queues') !== -1) {
                apiCalls.push(loadQueues());
            }
            if (types.indexOf('history') !== -1) {
                apiCalls.push(loadHistory());
            }
            if (types.indexOf('reviewMeta') !== -1) {
                apiCalls.push(loadReviewMeta(data));
            }
            if (types.indexOf('movingSection') !== -1) {
                apiCalls.push(loadMovingMeta(data));
            }
            if (types.indexOf('setting') !== -1) {
                apiCalls.push(loadSetting());
            }
            if (apiCalls.length > 0) {
                $q.all(apiCalls)
                    .then(function(result) {
                        deferred.resolve('success');
                    }, function(error) {
                        deferred.reject('fail to load specified files');
                    });
            } else {
                deferred.resolve('success');
            }
            return deferred.promise;
        }
        return {
            load: load
        }
    });
