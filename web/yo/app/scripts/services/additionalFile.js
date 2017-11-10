/**
 * Created by jiaojiao on 10/24/17.
 */
'use strict';

/**
 * @ngdoc service
 * @name oncokb.additionalFile
 * @description
 * # Additional Files, currently including meta and queues.
 * Service in the oncokb.
 */
angular.module('oncokbApp')
    .service('additionalFile', function additionalFile($rootScope, $q, storage, mainUtils, documents, dialogs, $timeout, DatabaseConnector) {
        function load(types) {
            function loadMeta() {
                var metaDefer = $q.defer();
                if ($rootScope.metaData) {
                    metaDefer.resolve('success');
                } else {
                    var meta = documents.getAdditionalDoc('meta');
                    storage.getAdditionalRealtimeDocument(meta.id).then(function(metaRealtime) {
                        if (metaRealtime && metaRealtime.error) {
                            dialogs.error('Error', 'Fail to get meta document! Please stop editing and contact the developer!');
                            metaDefer.reject('Fail to load meta file');
                        } else {
                            $rootScope.metaRealtime = metaRealtime;
                            $rootScope.metaModel = metaRealtime.getModel();
                            $rootScope.metaData = metaRealtime.getModel().getRoot().get('review');
                            $rootScope.timeStamp = metaRealtime.getModel().getRoot().get('timeStamp');
                            $rootScope.apiData = metaRealtime.getModel().getRoot().get('api');
                            metaDefer.resolve('success');
                            if ($rootScope.internal) {
                                synchronizeData();
                            }
                        }
                    });
                }
                return metaDefer.promise;
            }
            function loadQueues() {
                var queuesDefer = $q.defer();
                if ($rootScope.queuesData) {
                    queuesDefer.resolve('success');
                } else {
                    var queuesDoc = documents.getAdditionalDoc('queues');
                    storage.getAdditionalRealtimeDocument(queuesDoc.id).then(function(queuesRealtime) {
                        if (queuesRealtime && queuesRealtime.error) {
                            dialogs.error('Error', 'Fail to get queues document! Please stop editing and contact the developer!');
                            queuesDefer.reject('Fail to load queues file');
                        } else {
                            $rootScope.queuesRealtime = queuesRealtime;
                            $rootScope.queuesModel = queuesRealtime.getModel();
                            $rootScope.queuesData = queuesRealtime.getModel().getRoot().get('queues');
                            queuesDefer.resolve('success');
                        }
                    });
                }
                return queuesDefer.promise;
            }
            /**
             * Loop through api calls recorded in the meta file and update it to database every 5 mins
             * **/
            function synchronizeData() {
                var hugoSymbols = $rootScope.apiData.keys();
                _.each(hugoSymbols, function(hugoSymbol) {
                    if ($rootScope.apiData.get(hugoSymbol).has('vus')) {
                        updateByType('vus', hugoSymbol, $rootScope.apiData.get(hugoSymbol).get('vus').get('data'));
                    }
                    // TODO
                    // updateByType('priority', hugoSymbol, $rootScope.apiData.get(hugoSymbol).get('priority'));
                    // updateByType('drug', hugoSymbol, $rootScope.apiData.get(hugoSymbol).get('drug'));
                });
                $timeout(function() {
                    synchronizeData();
                }, 300000);
            }
            function updateByType(type, hugoSymbol, data) {
                if (type === 'vus') {
                    DatabaseConnector.updateVUS(hugoSymbol, data, function() {
                        $rootScope.apiData.get(hugoSymbol).delete('vus');
                    });
                } else if (type === 'priority') {
                    // TODO
                } else if (type === 'drug') {
                    // TODO
                }
            }
            var deferred = $q.defer();
            storage.retrieveAdditional().then(function(result) {
                if (!result || result.error || !_.isArray(result) || result.length !== 2) {
                    dialogs.error('Error', 'Fail to retrieve additional files! Please stop editing and contact the developer!');
                    var subject = 'Fail to retrieve meta file';
                    var content = 'The additional files are not correctly located. Please double check. ';
                    if (result && result.error) {
                        content += 'System error is ' + JSON.stringify(result.error);
                    }
                    mainUtils.notifyDeveloper(subject, content);
                    deferred.reject(result);
                } else {
                    documents.setAdditionalDocs(result);
                    var apiCalls = [];
                    if (types.indexOf('all') !== -1  || types.indexOf('meta') !== -1) {
                        apiCalls.push(loadMeta());
                    }
                    if (types.indexOf('all') !== -1 || types.indexOf('queues') !== -1) {
                        apiCalls.push(loadQueues());
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

                }
            });
            return deferred.promise;
        }
        return {
            load: load
        }
    });
