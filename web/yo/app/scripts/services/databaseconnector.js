'use strict';

/**
 * @ngdoc service
 * @name oncokb.DatabaseConnector
 * @description
 * # DatabaseConnector
 * Factory in the oncokb.
 */
angular.module('oncokbApp')
    .factory('DatabaseConnector', [
        '$timeout',
        '$q',
        'Gene',
        'Alteration',
        'TumorType',
        'Evidence',
        'SearchVariant',
        'GenerateDoc',
        'DriveOncokbInfo',
        'OncoTreeTumorTypes',
        'DriveAnnotation',
        'SendEmail',
        'DataSummary',
        'GeneStatus',
        'ServerUtils',
        'InternalAccess',
        function ($timeout,
                  $q,
                  Gene,
                  Alteration,
                  TumorType,
                  Evidence,
                  SearchVariant,
                  GenerateDoc,
                  DriveOncokbInfo,
                  OncoTreeTumorTypes,
                  DriveAnnotation,
                  SendEmail,
                  DataSummary,
                  GeneStatus,
                  ServerUtils,
                  InternalAccess) {

            var numOfLocks = {},
                data = {};

            //When running locally, set this to true, all servlet will read data from relative files.
            var dataFromFile = false;

            function getAllGene(callback, timestamp) {
                if (dataFromFile) {
                    Gene.getFromFile()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                } else {
                    Gene.getFromServer()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                }
            }

            function getDataSummary() {
                var deferred = $q.defer();
                if (dataFromFile) {
                    DataSummary.getFromFile()
                        .success(function (data) {
                            deferred.resolve(data);
                        })
                        .error(function (result) {
                            deferred.reject(result);
                        });
                } else {
                    DataSummary.getFromFile()
                        .success(function (data) {
                            deferred.resolve(data);
                        })
                        .error(function (result) {
                            deferred.reject(result);
                        });
                }
                return deferred.promise;
            }

            function getGeneStatus(params) {
                var deferred = $q.defer();
                if (dataFromFile) {
                    GeneStatus.getFromFile(params)
                        .success(function (data) {
                            deferred.resolve(data);
                        })
                        .error(function (result) {
                            deferred.reject(result);
                        });
                } else {
                    GeneStatus.getFromServer(params)
                        .success(function (data) {
                            deferred.resolve(data);
                        })
                        .error(function (result) {
                            deferred.reject(result);
                        });
                }
                return deferred.promise;
            }

            function setGeneStatus(params) {
                var deferred = $q.defer();
                if (dataFromFile) {
                    GeneStatus.setToFile(params)
                        .success(function (data) {
                            deferred.resolve(data);
                        })
                        .error(function (result) {
                            deferred.reject(result);
                        });
                } else {
                    GeneStatus.setToServer(params)
                        .success(function (data) {
                            deferred.resolve(data);
                        })
                        .error(function (result) {
                            deferred.reject(result);
                        });
                }
                return deferred.promise;
            }

            function getAllAlteration(callback, timestamp) {
                if (dataFromFile) {
                    Alteration.getFromFile()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                } else {
                    Alteration.getFromServer()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                }
            }

            function getAllOncoTreeTumorTypes(callback, timestamp) {
                if (dataFromFile) {
                    OncoTreeTumorTypes.getFromFile()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                } else {
                    OncoTreeTumorTypes.getFromServer()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                }
            }

            function getOncokbInfo(callback, timestamp) {
                if (dataFromFile) {
                    DriveOncokbInfo.getFromFile()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                } else {
                    DriveOncokbInfo.getFromServer()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                }
            }

            function getAllTumorType(callback, timestamp) {
                if (dataFromFile) {
                    TumorType.getFromFile()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                } else {
                    TumorType.getFromServer()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                }
            }

            function getAllEvidence(callback, timestamp) {
                if (dataFromFile) {
                    Evidence.getFromFile()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                } else {
                    Evidence.getFromServer()
                        .success(function (data) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback(data);
                        })
                        .error(function (result) {
                            if (timestamp) {
                                numOfLocks[timestamp]--;
                            }
                            callback();
                        });
                }
            }

            function searchVariant(params, success, fail) {
                if (dataFromFile) {
                    SearchVariant.annotationFromFile(params)
                        .success(function (data) {
                            success(data);
                        })
                        .error(function (result) {
                            fail();
                        });
                } else {
                    SearchVariant
                        .getAnnotation(params)
                        .success(function (data) {
                            success(data);
                        })
                        .error(function () {
                            fail();
                        });
                }
            }

            function generateGoogleDoc(params, success, fail) {
                console.log(params);
                if (dataFromFile) {
                    success('');
                } else {
                    GenerateDoc
                        .getDoc(params)
                        .success(function (data) {
                            success(data);
                        })
                        .error(function () {
                            fail();
                        });
                }
            }

            function updateGene(data, success, fail) {
                if (dataFromFile) {
                    success('');
                } else {
                    DriveAnnotation
                        .updateGene(data)
                        .success(function (data) {
                            success(data);
                        })
                        .error(function () {
                            fail();
                        });
                }
            }

            function createGoogleFolder(params) {
                var deferred = $q.defer();

                if (dataFromFile) {
                    deferred.resolve('test name');
                } else {
                    GenerateDoc
                        .createFolder(params)
                        .success(function (data) {
                            deferred.resolve(data);
                        })
                        .error(function () {
                            deferred.reject();
                        });
                }
                return deferred.promise;
            }

            function sendEmail(params, success, fail) {
                if (dataFromFile) {
                    success(true);
                } else {
                    console.log(params);
                    SendEmail
                        .init(params)
                        .success(function (data) {
                            success(data);
                        })
                        .error(function () {
                            fail();
                        });
                }
            }

            function timeout(callback, timestamp) {
                $timeout(function () {
                    if (numOfLocks[timestamp] === 0) {
                        callback(data[timestamp]);
                    } else {
                        timeout(callback, timestamp);
                    }
                }, 100);
            }

            function getHotspotList(callback) {
                if (dataFromFile) {
                    ServerUtils.hotspot.getFromFile()
                        .success(function (data) {
                            callback(data);
                        })
                        .error(function () {
                            callback();
                        });
                } else {
                    ServerUtils.hotspot.getFromServer()
                        .success(function (data) {
                            callback(data);
                        })
                        .error(function () {
                            callback();
                        });
                }
            }

            function getAutoMutationList(callback) {
                if (dataFromFile) {
                    ServerUtils.autoMutation.getFromFile()
                        .success(function (data) {
                            callback(data);
                        })
                        .error(function () {
                            callback();
                        });
                } else {
                    ServerUtils.autoMutation.hotspot.getFromServer()
                        .success(function (data) {
                            callback(data);
                        })
                        .error(function () {
                            callback();
                        });
                }
            }

            function testAccess(successCallback, failCallback) {
                InternalAccess
                    .success(function (data, status, headers, config) {
                        if (angular.isFunction(successCallback)) {
                            successCallback(data, status, headers, config);
                        }
                    })
                    .error(function (data, status, headers, config) {
                        if (angular.isFunction(failCallback)) {
                            failCallback(data, status, headers, config);
                        }
                    });
            }

            // Public API here
            return {
                'getGeneAlterationTumorType': function (callback) {
                    var timestamp = new Date().getTime().toString();

                    numOfLocks[timestamp] = 3;
                    data[timestamp] = {};

                    getAllGene(function (d) {
                        data[timestamp].genes = d;
                    }, timestamp);
                    getAllAlteration(function (d) {
                        data[timestamp].alterations = d;
                    }, timestamp);
                    getAllTumorType(function (d) {
                        data[timestamp].tumorTypes = d;
                    }, timestamp);

                    timeout(callback, timestamp);
                },
                'getGeneTumorType': function (callback) {
                    var timestamp = new Date().getTime().toString();

                    numOfLocks[timestamp] = 2;
                    data[timestamp] = {};

                    getAllGene(function (d) {
                        data[timestamp].genes = d;
                    }, timestamp);
                    getAllTumorType(function (d) {
                        data[timestamp].tumorTypes = d;
                    }, timestamp);

                    timeout(callback, timestamp);
                },
                'getAllEvidence': getAllEvidence,
                'getAllGene': getAllGene,
                'getDataSummary': getDataSummary,
                'searchAnnotation': searchVariant,
                'googleDoc': generateGoogleDoc,
                'createGoogleFolder': createGoogleFolder,
                'getOncokbInfo': getOncokbInfo,
                'getAllTumorType': getAllTumorType,
                'getAllOncoTreeTumorTypes': getAllOncoTreeTumorTypes,
                'updateGene': updateGene,
                'sendEmail': sendEmail,
                'setGeneStatus': setGeneStatus,
                'getGeneStatus': getGeneStatus,
                'getHotspotList': getHotspotList,
                'getAutoMutationList': getAutoMutationList,
                'testAccess': testAccess
            };
        }]);
