'use strict';

angular.module('oncokbApp')
    .factory('DatabaseConnector', [
        '$timeout',
        '$q',
        '$rootScope',
        'config',
        'Gene',
        'Alteration',
        'TumorType',
        'Evidence',
        'SearchVariant',
        'DriveOncokbInfo',
        'DriveAnnotation',
        'SendEmail',
        'DataSummary',
        'Cache',
        'OncoTree',
        'InternalAccess',
        'ApiUtils',
        'PrivateApiUtils',
        function($timeout,
                 $q,
                 $rootScope,
                 config,
                 Gene,
                 Alteration,
                 TumorType,
                 Evidence,
                 SearchVariant,
                 DriveOncokbInfo,
                 DriveAnnotation,
                 SendEmail,
                 DataSummary,
                 Cache,
                 OncoTree,
                 InternalAccess,
                 ApiUtils,
                 PrivateApiUtils) {
            var numOfLocks = {};
            var data = {};
            var testing = config.testing || false;

            function getAllGene(callback, timestamp) {
                Gene.getFromServer()
                    .then(function(data) {
                        if (timestamp) {
                            numOfLocks[timestamp]--;
                        }
                        callback(data);
                    }, function() {
                        if (timestamp) {
                            numOfLocks[timestamp]--;
                        }
                        callback();
                    });
            }
            function getAllTumorType(callback, timestamp) {
                TumorType.getFromServer()
                    .then(function(data) {
                        if (timestamp) {
                            numOfLocks[timestamp]--;
                        }
                        callback(data);
                    }, function() {
                        if (timestamp) {
                            numOfLocks[timestamp]--;
                        }
                        callback();
                    });
            }

            function getReviewedData(evidenceType) {
                var deferred = $q.defer();
                if (evidenceType === 'geneType') {
                    DataSummary.getGeneType()
                        .then(function(data) {
                            deferred.resolve(data);
                        }, function(result) {
                            deferred.reject(result);
                        });
                } else {
                    DataSummary.getEvidenceByType(evidenceType)
                        .then(function(data) {
                            deferred.resolve(data);
                        }, function(result) {
                            deferred.reject(result);
                        });
                }
                return deferred.promise;
            }

            function searchVariant(params, success, fail) {
                SearchVariant
                    .getAnnotation(params)
                    .then(function(data) {
                        success(data);
                    }, function() {
                        fail();
                    });
            }

            function updateGene(data, success, fail) {
                if (testing) {
                    success('');
                } else {
                    DriveAnnotation
                        .updateGene(data)
                        .then(function(data) {
                            success(data);
                        }, function() {
                            fail();
                        });
                }
            }

            function updateGeneType(hugoSymbol, data, historyData, success, fail) {
                if (testing) {
                    success('');
                    updateHistory(historyData);
                } else {
                    DriveAnnotation
                        .updateGeneType(hugoSymbol, data)
                        .then(function(data) {
                            success(data);
                            updateHistory(historyData);
                        }, function() {
                            fail();
                        });
                }
            }

            function updateEvidence(uuid, data, success, fail) {
                DriveAnnotation
                    .updateEvidence(uuid, data)
                    .then(function(data) {
                        success(data);
                    }, function() {
                        fail();
                    });
            }

            function getEvidencesByUUID(uuid, success, fail) {
                if (testing) {
                    success('');
                } else {
                    DriveAnnotation
                        .getEvidencesByUUID(uuid)
                        .then(function(data) {
                            success(data);
                        }, function() {
                            fail();
                        });
                }
            }

            function getEvidencesByUUIDs(uuids, success, fail) {
                if (testing) {
                    success('');
                } else {
                    DriveAnnotation
                        .getEvidencesByUUIDs(uuids)
                        .then(function(data) {
                            success(data);
                        }, function() {
                            fail();
                        });
                }
            }
            function getPubMedArticle(pubMedIDs, success, fail) {
                success(data);
                // DriveAnnotation
                //     .getPubMedArticle(pubMedIDs)
                //     .then(function(data) {
                //         success(data);
                //     }, function() {
                //         fail();
                //     });
            }
            function deleteEvidences(data, historyData, success, fail) {
                if (testing) {
                    success('');
                    updateHistory(historyData);
                } else {
                    DriveAnnotation
                        .deleteEvidences(data)
                        .then(function(data) {
                            success(data);
                            updateHistory(historyData);
                        }, function() {
                            fail();
                        });
                }
            }

            function updateVUS(hugoSymbol, data, success, fail) {
                if ($rootScope.internal) {
                    if (testing) {
                        success('');
                    } else {
                        DriveAnnotation
                            .updateVUS(hugoSymbol, data)
                            .then(function(data) {
                                success(data);
                            }, function(error) {
                                var subject = 'VUS update Error for ' + hugoSymbol;
                                var content = 'The system error returned is ' + JSON.stringify(error);
                                sendEmail({sendTo: 'dev.oncokb@gmail.com', subject: subject, content: content},
                                    function(result) {
                                        console.log('sent old history to oncokb dev account');
                                    },
                                    function(error) {
                                        console.log('fail to send old history to oncokb dev account', error);
                                    }
                                );
                                fail(error);
                                setAPIData('vus', hugoSymbol, data);
                            });
                    }
                } else {
                    setAPIData('vus', hugoSymbol, data);
                }
            }
            function setAPIData(type, hugoSymbol, data) {
                if (!$rootScope.apiData.has(hugoSymbol)) {
                    $rootScope.apiData.set(hugoSymbol, $rootScope.metaModel.createMap());
                }
                if (type === 'vus') {
                    $rootScope.apiData.get(hugoSymbol).set('vus', $rootScope.metaModel.createMap({data: data}));
                } else if (type === 'priority' || type === 'drug') {
                    // TODO
                    // $rootScope.apiData.get(hugoSymbol).set(type, $rootScope.metaModel.createList(''));
                }
            }
            function updateEvidenceBatch(data, historyData, success, fail) {
                if (testing) {
                    success('');
                    updateHistory(historyData);
                } else {
                    DriveAnnotation
                        .updateEvidenceBatch(data)
                        .then(function(data) {
                            success(data);
                            updateHistory(historyData);
                        }, function() {
                            fail();
                        });
                }
            }

            function updateEvidenceTreatmentPriorityBatch(data, success, fail) {
                if (testing) {
                    success('');
                } else {
                    DriveAnnotation
                        .updateEvidenceTreatmentPriorityBatch(data)
                        .then(function(data) {
                            success(data);
                        }, function() {
                            fail();
                        });
                }
            }

            function sendEmail(params, success, fail) {
                if (testing) {
                    success(true);
                } else {
                    SendEmail
                        .init(params)
                        .then(function(data) {
                            success(data);
                        }, function() {
                            fail();
                        });
                }
            }

            function timeout(callback, timestamp) {
                $timeout(function() {
                    if (numOfLocks[timestamp] === 0) {
                        callback(data[timestamp]);
                    } else {
                        timeout(callback, timestamp);
                    }
                }, 100);
            }

            function testAccess(successCallback, failCallback) {
                if (testing) {
                    if (angular.isFunction(successCallback)) {
                        successCallback();
                    }
                } else {
                    InternalAccess
                        .hasAccess()
                        .then(function(data, status, headers, config) {
                            if (angular.isFunction(successCallback)) {
                                successCallback(data, status, headers, config);
                            }
                        }, function(data, status, headers, config) {
                            if (angular.isFunction(failCallback)) {
                                failCallback(data, status, headers, config);
                            }
                        });
                }
            }

            function getCacheStatus() {
                var deferred = $q.defer();
                if (testing) {
                    deferred.resolve('enabled');
                } else {
                    Cache.getStatus()
                        .then(function(data) {
                            deferred.resolve(data);
                        }, function(result) {
                            deferred.reject(result);
                        });
                }
                return deferred.promise;
            }

            function setCache(operation) {
                var deferred = $q.defer();
                if (testing) {
                    if (operation === 'enable') {
                        deferred.resolve('enabled');
                    }
                    if (operation === 'disable') {
                        deferred.resolve('disabled');
                    }
                } else {
                    switch (operation) {
                    case 'disable':
                        Cache.disable()
                            .success(function(data) {
                                deferred.resolve(data);
                            })
                            .error(function(result) {
                                deferred.reject(result);
                            });
                        break;
                    case 'enable':
                        Cache.enable()
                            .success(function(data) {
                                deferred.resolve(data);
                            })
                            .error(function(result) {
                                deferred.reject(result);
                            });
                        break;
                    case 'reset':
                        Cache.reset()
                            .success(function(data) {
                                deferred.resolve(data);
                            })
                            .error(function(result) {
                                deferred.reject(result);
                            });
                        break;
                    default:
                        break;
                    }
                }
                return deferred.promise;
            }

            function updateGeneCache(hugoSymbol) {
                var deferred = $q.defer();
                if (testing) {
                    deferred.resolve();
                } else if (hugoSymbol) {
                    Cache.updateGene(hugoSymbol)
                        .success(function(data) {
                            deferred.resolve(data);
                        })
                        .error(function(result) {
                            deferred.reject(result);
                        });
                } else {
                    deferred.reject();
                }
                return deferred.promise;
            }

            function getOncoTreeMainTypes() {
                var deferred = $q.defer();
                OncoTree.getMainType()
                    .then(function(data) {
                        deferred.resolve(data);
                    }, function(result) {
                        deferred.reject(result);
                    });
                return deferred.promise;
            }

            function getOncoTreeTumorTypesByMainType(mainType) {
                var deferred = $q.defer();
                OncoTree.getTumorTypeByMainType(mainType)
                    .success(function(data) {
                        deferred.resolve(data);
                    })
                    .error(function(result) {
                        deferred.reject(result);
                    });
                return deferred.promise;
            }

            function getOncoTreeTumorTypesByMainTypes(mainTypes) {
                var deferred = $q.defer();
                OncoTree.getTumorTypesByMainTypes(mainTypes)
                    .then(function(data) {
                        deferred.resolve(data);
                    }, function(result) {
                        deferred.reject(result);
                    });
                return deferred.promise;
            }

            function getOncoTreeTumorTypeByName(name, exactMatch) {
                var deferred = $q.defer();
                OncoTree.getTumorType('name', name, exactMatch)
                    .success(function(data) {
                        deferred.resolve(data);
                    })
                    .error(function(result) {
                        deferred.reject(result);
                    });
                return deferred.promise;
            }
            function getTumorSubtypes() {
                var deferred = $q.defer();
                OncoTree.getTumorSubtypes().then(function(data) {
                    data.data.push({
                        name: 'All Liquid Tumors'
                    });
                    data.data.push({
                        name: 'All Solid Tumors'
                    });
                    data.data.push({
                        name: 'All Tumors'
                    });
                    data.data.push({
                        name: 'Germline Disposition'
                    });
                    data.data.push({
                        name: 'All Pediatric Tumors'
                    });
                    data.data.push({
                        name: 'Other Tumor Types'
                    });
                    deferred.resolve(data.data);
                }, function(result) {
                    deferred.reject(result);
                });
                return deferred.promise;
            }

            function getIsoforms(type) {
                var deferred = $q.defer();
                ApiUtils.getIsoforms(type)
                    .then(function(data) {
                        deferred.resolve(data.data);
                    }, function(result) {
                        deferred.reject(result);
                    });
                return deferred.promise;
            }

            function getOncogeneTSG() {
                var deferred = $q.defer();
                ApiUtils.getOncogeneTSG()
                    .then(function(data) {
                        deferred.resolve(data.data);
                    }, function(result) {
                        deferred.reject(result);
                    });
                return deferred.promise;
            }

            function getSuggestedVariants() {
                var deferred = $q.defer();
                if (testing) {
                    deferred.resolve({
                        meta: '',
                        data: ['Fusion']
                    });
                } else {
                    PrivateApiUtils.getSuggestedVariants()
                        .then(function(data) {
                            deferred.resolve(data);
                        }, function(result) {
                            deferred.reject(result);
                        });
                }
                return deferred.promise;
            }

            function isHotspot(hugoSymbol, variant) {
                var deferred = $q.defer();
                if (testing) {
                    deferred.resolve({
                        meta: '',
                        data: false
                    });
                } else {
                    PrivateApiUtils.isHotspot(hugoSymbol, variant)
                        .then(function(data) {
                            deferred.resolve(data);
                        }, function(result) {
                            deferred.reject(result);
                        });
                }
                return deferred.promise;
            }

            function updateHistory(historyData) {
                if (!$rootScope.historyRef.api) {
                    $rootScope.historyRef.api = [];
                }
                $rootScope.historyRef.api.push({
                    admin: $rootScope.me.name,
                    timeStamp: new Date().getTime(),
                    records: historyData
                });
            }

            function lookupVariants(body) {
                var deferred = $q.defer();
                SearchVariant.lookupVariants(body)
                    .then(function(data) {
                        deferred.resolve(data);
                    }, function(result) {
                        deferred.reject(result);
                    });
                return deferred.promise;
            }

            // Public API here
            return {
                getGeneTumorType: function(callback) {
                    var timestamp = new Date().getTime().toString();

                    numOfLocks[timestamp] = 2;
                    data[timestamp] = {};

                    getAllGene(function(d) {
                        data[timestamp].genes = d.data;
                    }, timestamp);
                    getAllTumorType(function(d) {
                        data[timestamp].tumorTypes = d.data;
                    }, timestamp);

                    timeout(callback, timestamp);
                },
                searchAnnotation: searchVariant,
                updateGene: updateGene,
                updateGeneType: updateGeneType,
                updateEvidence: updateEvidence,
                deleteEvidences: deleteEvidences,
                updateVUS: updateVUS,
                updateEvidenceBatch: updateEvidenceBatch,
                updateEvidenceTreatmentPriorityBatch: updateEvidenceTreatmentPriorityBatch,
                sendEmail: sendEmail,
                getCacheStatus: getCacheStatus,
                disableCache: function() {
                    return setCache('disable');
                },
                enableCache: function() {
                    return setCache('enable');
                },
                resetCache: function() {
                    return setCache('reset');
                },
                updateGeneCache: function(hugoSymbol) {
                    return updateGeneCache(hugoSymbol);
                },
                getOncoTreeMainTypes: getOncoTreeMainTypes,
                getOncoTreeTumorTypesByMainType: getOncoTreeTumorTypesByMainType,
                getOncoTreeTumorTypesByMainTypes: getOncoTreeTumorTypesByMainTypes,
                getOncoTreeTumorTypeByName: getOncoTreeTumorTypeByName,
                testAccess: testAccess,
                getIsoforms: getIsoforms,
                getOncogeneTSG: getOncogeneTSG,
                getSuggestedVariants: getSuggestedVariants,
                isHotspot: isHotspot,
                getEvidencesByUUID: getEvidencesByUUID,
                getEvidencesByUUIDs: getEvidencesByUUIDs,
                getPubMedArticle: getPubMedArticle,
                getReviewedData: getReviewedData,
                lookupVariants: lookupVariants,
                getTumorSubtypes: getTumorSubtypes
            };
        }]);
