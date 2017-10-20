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
    .service('additionalFile', function additionalFile($rootScope, $q, storage, mainUtils, documents, dialogs) {
        function load(types) {
            function loadMeta() {
                var metaDefer = $q.defer();
                var meta = documents.getAdditionalDoc('meta');
                storage.getMetaRealtimeDocument(meta.id).then(function(metaRealtime) {
                    if (metaRealtime && metaRealtime.error) {
                        dialogs.error('Error', 'Fail to get meta document! Please stop editing and contact the developer!');
                        metaDefer.reject('Fail to load meta file');
                    } else {
                        $rootScope.metaRealtime = metaRealtime;
                        $rootScope.metaModel = metaRealtime.getModel();
                        $rootScope.metaData = metaRealtime.getModel().getRoot().get('review');
                        metaDefer.resolve('success');
                    }
                });
                return metaDefer.promise;
            }

            function loadQueues() {
                var queuesDefer = $q.defer();
                var queuesDoc = documents.getAdditionalDoc('queues');
                storage.getMetaRealtimeDocument(queuesDoc.id).then(function(queuesRealtime) {
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
                return queuesDefer.promise;
            }
            var deferred = $q.defer();
            storage.retrieveAdditional().then(function(result) {
                if (!result || result.error || !_.isArray(result) || result.length !== 2) {
                    dialogs.error('Error', 'Fail to retrieve additional files! Please stop editing and contact the developer!');
                    var sendTo = 'dev.oncokb@gmail.com';
                    var subject = 'Fail to retrieve meta file';
                    var content = 'The additional files are not correctly located. Please double check. ';
                    if (result && result.error) {
                        content += 'System error is ' + JSON.stringify(result.error);
                    }
                    mainUtils.sendEmail(sendTo, subject, content);
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
