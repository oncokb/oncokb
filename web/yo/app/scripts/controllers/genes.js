'use strict';

angular.module('oncokbApp')
    .controller('GenesCtrl', ['$scope', '$rootScope', '$location', '$timeout',
        '$routeParams', '_', 'config', 'importer', 'storage', 'documents',
        'users', 'DTColumnDefBuilder', 'DTOptionsBuilder', 'DatabaseConnector',
        'OncoKB', 'stringUtils', 'S', 'mainUtils', 'gapi', 'UUIDjs', 'dialogs', 'additionalFile',
        function($scope, $rootScope, $location, $timeout, $routeParams, _,
                 config, importer, storage, Documents, users,
                 DTColumnDefBuilder, DTOptionsBuilder, DatabaseConnector,
                 OncoKB, stringUtils, S, MainUtils, gapi, UUIDjs, dialogs, additionalFile) {
            function saveGene(docs, docIndex, excludeObsolete, callback) {
                if (docIndex < docs.length) {
                    var fileId = docs[docIndex].id;
                    storage.getRealtimeDocument(fileId).then(function(realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            console.log(docs[docIndex].title, '\t\t', docIndex);
                            console.log('\t copying');
                            var gene = realtime.getModel().getRoot().get('gene');
                            var vus = realtime.getModel().getRoot().get('vus');
                            if (gene) {
                                var geneData = stringUtils.getGeneData(gene, excludeObsolete, true, true, true);
                                var vusData = stringUtils.getVUSFullData(vus, true);
                                var params = {};

                                if (geneData) {
                                    params.gene = JSON.stringify(geneData);
                                }
                                if (vusData) {
                                    params.vus = JSON.stringify(vusData);
                                }
                                DatabaseConnector.updateGene(params,
                                    function(result) {
                                        console.log('\t success', result);
                                        $timeout(function() {
                                            saveGene(docs, ++docIndex, excludeObsolete, callback);
                                        }, 200, false);
                                    },
                                    function(result) {
                                        console.log('\t failed', result);
                                        $timeout(function() {
                                            saveGene(docs, ++docIndex, excludeObsolete, callback);
                                        }, 200, false);
                                    }
                                );
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function() {
                                    saveGene(docs, ++docIndex, excludeObsolete, callback);
                                }, 200, false);
                            }
                        }
                    });
                } else {
                    if (callback) {
                        callback();
                    }
                    console.log('finished.');
                }
            }
            $scope.showDocs = function() {
                $scope.documents.forEach(function(item) {
                    console.log(item.title);
                });
                // console.log($scope.documents);
            };
            $scope.metaFlags = {};
            $scope.getDocs = function() {
                var docs = Documents.get();
                if (docs.length > 0) {
                    // $scope.$apply(function() {
                    processMeta();
                    // });
                } else if (OncoKB.global.genes) {
                    storage.requireAuth(true).then(function() {
                        storage.retrieveAllFiles().then(function(result) {
                            Documents.set(result);
                            Documents.setStatus(OncoKB.global.genes);
                            processMeta();
                            // loading_screen.finish();
                        });
                    });
                } else {
                    DatabaseConnector.getAllGene(function(data) {
                        OncoKB.global.genes = data;
                        storage.requireAuth(true).then(function() {
                            storage.retrieveAllFiles().then(function(result) {
                                Documents.set(result);
                                Documents.setStatus(OncoKB.global.genes);
                                if (users.getMe().role === 8) {
                                    additionalFile.load(['all']).then(function(result) {
                                        processMeta();
                                    });
                                } else {
                                    $scope.documents = Documents.get();
                                    $scope.status.rendering = false;
                                }
                            });
                        });
                    });
                }
            };
            function processMeta() {
                var genesToReview = $rootScope.metaData.keys();
                for (var i = 0; i < genesToReview.length; i++) {
                    $scope.metaFlags[genesToReview[i]] = {};
                    var geneMetaData = $rootScope.metaData.get(genesToReview[i]);
                    var uuids = geneMetaData.keys();
                    var flag = true;
                    for (var j = 0; j < uuids.length; j++) {
                        if (geneMetaData.get(uuids[j]).type === 'Map' && geneMetaData.get(uuids[j]).get('review')) {
                            $scope.metaFlags[genesToReview[i]].review = true;
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        $scope.metaFlags[genesToReview[i]].review = false;
                    }
                }
                var genesInQueues = $rootScope.queuesData.keys();
                for (var i = 0; i < genesInQueues.length; i++) {
                    var tempCount = 0;
                    for (var j = 0; j < $rootScope.queuesData.get(genesInQueues[i]).length; j++) {
                        if ($rootScope.queuesData.get(genesInQueues[i]).get(j).get('curated') !== true) {
                            tempCount++;
                        }
                    }
                    if (tempCount >0) {
                        if ($scope.metaFlags[genesInQueues[i]]) {
                            $scope.metaFlags[genesInQueues[i]].queues = tempCount;
                        } else {
                            $scope.metaFlags[genesInQueues[i]] = {
                                queues: tempCount
                            };
                        }
                    }
                }
                $scope.documents = Documents.get();
                $scope.status.rendering = false;
            }
            var dueDay = angular.element(document.querySelector('#genesdatepicker'));
            dueDay.datepicker();
            $scope.backup = function(backupFolderName) {
                $scope.status.backup = false;
                OncoKB.backingUp = true;
                importer.backup(backupFolderName, function() {
                    $scope.status.backup = true;
                    OncoKB.backingUp = false;
                });
            };

            $scope.redirect = function(path) {
                $location.path(path);
            };

            $scope.checkError = function() {
                console.log($rootScope.errors);
            };

            $scope.saveAllGenes = function() {
                $scope.status.saveAllGenes = false;
                saveGene($scope.documents, 0, true, function() {
                    $scope.status.saveAllGenes = true;
                });
            };

            $scope.userRole = users.getMe().role;

            var sorting = [[2, 'asc'], [1, 'desc'], [0, 'asc']];
            if (users.getMe().role === 8) {
                sorting = [[4, 'desc'], [5, 'desc'], [1, 'desc'], [0, 'asc']];
            }

            $scope.dtOptions = DTOptionsBuilder
                .newOptions()
                .withDOM('ifrtlp')
                .withOption('order', sorting)
                .withBootstrap();

            $scope.dtColumns = [
                DTColumnDefBuilder.newColumnDef(0),
                DTColumnDefBuilder.newColumnDef(1).withOption('sType', 'date'),
                DTColumnDefBuilder.newColumnDef(2),
                DTColumnDefBuilder.newColumnDef(3)
            ];
            if (users.getMe().role === 8) {
                $scope.dtColumns.push(DTColumnDefBuilder.newColumnDef(4));
                $scope.dtColumns.push(DTColumnDefBuilder.newColumnDef(5));
            }

            $scope.status = {
                backup: true,
                saveAllGenes: true,
                migrate: true,
                rendering: true,
                queueRendering: true
            };
            $scope.adminEmails = [];
            $scope.getDocs();
            $scope.oncoTree = {
                mainTypes: {}
            };
            $scope.mappedTumorTypes = {};
            getCacheStatus();

            var newGenes = [];

            $scope.migrate = function() {
                // console.log($scope.documents);
                $scope.status.migrate = false;
                importer
                    .migrate()
                    .then(function(result) {
                        if (result && result.error) {
                            $scope.status.migrate = true;
                        } else {
                            $scope.status.migrate = true;
                        }
                    });
            };

            $scope.create = function() {
                createDoc(0);
            };

            $scope.givePermission = function() {
                var testGene = {'test@gmail.com': 'AKT2'};
                var genes = [];

                for (var key in testGene) {
                    if (testGene.hasOwnProperty(key)) {
                        var _genes =
                            testGene[key].trim().split(',').map(function(e) {
                                return e.trim();
                            });
                        _genes.forEach(function(_gene) {
                            if (_gene) {
                                genes.push({email: key, gene: _gene});
                            }
                        });
                    }
                }

                $scope.genesPermissions = genes;
                givePermissionSub(0);
            };

            function givePermissionSub(index) {
                if (index < $scope.genesPermissions.length) {
                    var genePermission = $scope.genesPermissions[index];
                    console.log(genePermission.gene, '\t', genePermission.email);
                    var _docs = Documents.get({title: genePermission.gene});
                    var _doc = _docs[0];
                    if (_doc && _doc.id) {
                        storage.requireAuth().then(function() {
                            storage.getPermission(_doc.id).then(function(result) {
                                if (result.items && angular.isArray(result.items)) {
                                    var permissionIndex = -1;
                                    result.items.forEach(function(permission, _index) {
                                        if (permission.emailAddress && permission.emailAddress === genePermission.email) {
                                            permissionIndex = _index;
                                        }
                                    });

                                    if (permissionIndex === -1) {
                                        storage.insertPermission(_doc.id, genePermission.email, 'user', 'writer').then(function(result) {
                                            if (result && result.error) {
                                                console.log('Error when insert permission.');
                                            } else {
                                                console.log('\tinsert writer to', genePermission.gene);
                                                $timeout(function() {
                                                    givePermissionSub(++index);
                                                }, 100);
                                            }
                                        });
                                    } else if (result.items[permissionIndex].role === 'writer') {
                                        console.log('\tDont need to do anything on ', genePermission.email);
                                        $timeout(function() {
                                            givePermissionSub(++index);
                                        }, 100);
                                    } else {
                                        storage.updatePermission(_doc.id, result.items[permissionIndex].id, 'writer').then(function(result) {
                                            if (result && result.error) {
                                                console.log('Error when update permission.');
                                            } else {
                                                console.log('\tupdate  writer to', genePermission.gene);
                                                $timeout(function() {
                                                    givePermissionSub(++index);
                                                }, 100);
                                            }
                                        });
                                    }
                                }
                            });
                        });
                    } else {
                        console.log('\tThis gene document is not available');
                        $timeout(function() {
                            givePermissionSub(++index);
                        }, 100);
                    }
                } else {
                    console.info('Done.....');
                }
            }

            $scope.resetPermission = function() {
                resetPermissionSub(0);
            };

            function resetPermissionSub(index) {
                if (index < $scope.documents.length) {
                    var _doc = $scope.documents[index];
                    if (_doc && _doc.id) {
                        storage.requireAuth().then(function() {
                            storage.getPermission(_doc.id).then(function(result) {
                                if (result.items && angular.isArray(result.items)) {
                                    result.items.forEach(function(permission, _index) {
                                        if (permission.emailAddress && $scope.adminEmails.indexOf(permission.emailAddress) === -1 && permission.role === 'writer') {
                                            console.log('\tUpdating permission to reader: ', _doc.title, permission.emailAddress);
                                            storage.updatePermission(_doc.id, permission.id, 'reader').then(function(result) {
                                                if (result && result.error) {
                                                    console.log('Error when update permission.');
                                                } else {
                                                    console.log('\tFinish update permission to reader: ', _doc.title, permission.emailAddress);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        });

                        $timeout(function() {
                            resetPermissionSub(++index);
                        }, 100);
                    } else {
                        console.log('\tThis gene document is not available');
                        $timeout(function() {
                            resetPermissionSub(++index);
                        }, 100);
                    }
                } else {
                    console.info('Done.....');
                }
            }

            $scope.giveFolderPermission = function() {
                var emails = ['cbioportal@gmail.com'];
                var folderId = config.folderId;

                emails.forEach(function(email) {
                    storage.requireAuth(true).then(function() {
                        storage.getDocument(folderId).then(function(e1) {
                            if (e1.id) {
                                storage.getPermission(e1.id).then(function(result) {
                                    if (result.items && angular.isArray(result.items)) {
                                        var permissionIndex = -1;
                                        result.items.forEach(function(permission, _index) {
                                            if (permission.emailAddress && permission.emailAddress === email) {
                                                permissionIndex = _index;
                                            }
                                        });

                                        console.log(permissionIndex);
                                        // if(permissionIndex === -1) {
                                        //   storage.insertPermission(e1.id, key, 'user', 'writer').then(function(result){
                                        //     console.log('insert result', result);
                                        //   });
                                        // }else if(result.items[permissionIndex].role !== 'writer'){
                                        //   storage.updatePermission(e1.id, result.items[permissionIndex].id, 'writer').then(function(result){
                                        //     console.log('update result', result);
                                        //   });
                                        // }
                                    }
                                });
                            }
                        });
                    });
                });
            };

            $scope.initialStatus = function() {
                initial($scope.documents, 0, function() {
                    $scope.status.saveAllGenes = true;
                });
            };

            $scope.changeData = function() {
                console.info('Gene\tVariant\tTumorType\tTreatment' +
                    '\tFDA approved indication\tLevel\tShortDescription\t' +
                    'Description\tObsolete');

                console.info('Gene\tVariant\tTumorType\tTI\tTreatment' +
                    '\tCategory\tShort\tFull');
                changeData(0, function() {
                    console.info('Finished.');
                });
            };

            $scope.convertData = function() {
                console.info('Converting tumor types to OncoTree tumor types...');

                convertData(0, function() {
                    console.info('Finished.');
                });
            };

            $scope.findRelevantVariants = function() {
                console.info('Finding relevant variants...');
                var list = [];

                findRelevantVariants(list, 0, function() {
                    console.info('Finished.');
                });
            };

            $scope.changeCacheStatus = function() {
                if ($scope.status.cache === 'enabled') {
                    DatabaseConnector.disableCache()
                        .then(function() {
                            $scope.status.cache = 'disabled';
                        }, function() {
                            $scope.status.cache = 'unknown';
                        });
                } else if ($scope.status.cache === 'disabled') {
                    DatabaseConnector.enableCache()
                        .then(function() {
                            $scope.status.cache = 'enabled';
                        }, function() {
                            $scope.status.cache = 'unknown';
                        });
                }
            };

            $scope.resetCache = function() {
                DatabaseConnector.resetCache()
                    .then(function() {
                        console.log('succeed.');
                    }, function() {
                        console.log('failed.');
                    });
            };

            $scope.convertMutationEffect = function() {
                convertMutationEffect(0, function() {
                    console.log('Done converting mutation effect.');
                });
            };

            $scope.showValidationResult = function() {
                console.info('Gene\tVariant\tCategory');

                showValidationResult(0, function() {
                    console.info('Finished.');
                });
            };

            $scope.developerCheck = function() {
                return MainUtils.developerCheck(users.getMe().name);
            };

            function convertOncogenic(oncogenic) {
                var result = 'Unknown';
                if (oncogenic) {
                    switch (oncogenic) {
                    case 'YES':
                        result = 'Yes';
                        break;
                    case 'LIKELY':
                        result = 'Likely';
                        break;
                    case 'NO':
                        result = 'Likely Neutral';
                        break;
                    case 'UNKNOWN':
                        result = 'Unknown';
                        break;
                    case 'false':
                        result = 'Likely Neutral';
                        break;
                    case 'Yes':
                        result = 'Yes';
                        break;
                    case 'Likely':
                        result = 'Likely';
                        break;
                    case 'Likely Neutral':
                        result = 'Likely Neutral';
                        break;
                    case 'Inconclusive':
                        result = 'Inconclusive';
                        break;
                    default:
                        console.log('Couldn\'t find mapping for', oncogenic);
                        break;
                    }
                }
                return result;
            }

            function convertMutationEffect(index, callback) {
                if (index < $scope.documents.length) {
                    var document = $scope.documents[index];
                    storage.getRealtimeDocument(document.id).then(function(realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            console.log(document.title, '\t\t', index + 1);
                            var model = realtime.getModel();
                            var gene = model.getRoot().get('gene');
                            if (gene) {
                                var geneIndex = index + 1;
                                model.beginCompoundOperation();
                                gene.mutations.asArray().forEach(function(mutation, index) {
                                    var message;
                                    if (mutation.effect && mutation.effect.value.getText()) {
                                        var effect = mutation.effect.value.getText();
                                        var newEffects = ['Gain-of-function', 'Likely Gain-of-function', 'Loss-of-function', 'Likely Loss-of-function', 'Switch-of-function', 'Likely Switch-of-function', 'Neutral', 'Likely Neutral', 'Inconclusive'];

                                        if (newEffects.indexOf(effect) === -1) {
                                            if (mutation.effect.value.getText().toLowerCase() === 'other') {
                                                if (mutation.effect.addOn.getText()) {
                                                    effect = mutation.effect.addOn.getText();
                                                } else {
                                                    effect = 'Other';
                                                }
                                            } else if (mutation.effect.addOn.getText()) {
                                                if (mutation.effect.addOn.getText().toLowerCase().indexOf(mutation.effect.value.getText().toLowerCase()) === -1) {
                                                    effect += ' ' + mutation.effect.addOn.getText();
                                                } else {
                                                    effect = mutation.effect.addOn.getText();
                                                }
                                            }

                                            message = geneIndex + '\t' + document.title + '\t' + mutation.name + '\tmutation effect\t' + effect;
                                            mutation.effect.value.setText(stringUtils.findMutationEffect(effect));
                                            message += '\t' + stringUtils.findMutationEffect(effect);
                                            console.log(message);
                                        } else {
                                            message = geneIndex + '\t' + document.title + '\t' + mutation.name + '\tmutation effect\t' + effect + ' ' + mutation.effect.addOn.getText() + '\t' + effect + '\t1';
                                            console.log(message);
                                        }
                                        mutation.effect.addOn.setText('');
                                    }

                                    if (mutation.oncogenic) {
                                        message = geneIndex + '\t' + document.title + '\t' + mutation.name + '\toncogenic\t' + mutation.oncogenic.getText();
                                        mutation.oncogenic.setText(convertOncogenic(mutation.oncogenic.getText()));
                                        message += '\t' + convertOncogenic(mutation.oncogenic.getText());
                                        console.log(message);
                                    }
                                });
                                model.endCompoundOperation();
                                $timeout(function() {
                                    convertMutationEffect(++index, callback);
                                }, 1000, false);
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function() {
                                    convertMutationEffect(++index, callback);
                                }, 1000, false);
                            }
                        }
                    });
                } else if (_.isFunction(callback)) {
                    callback();
                }
            }

            function getCacheStatus() {
                DatabaseConnector.getCacheStatus().then(function(result) {
                    $scope.status.cache = result.hasOwnProperty('status') ? result.status : 'unknown';
                }, function(result) {
                    $scope.status.cache = 'unknown';
                });
            }

            function isExist(array, string) {
                var mark = false;
                _.each(array, function(item) {
                    if (item.toString().toLowerCase() === string.toString().toLowerCase()) {
                        mark = true;
                    }
                });
                return mark;
            }

            function findIndexIgnorecase(array, string) {
                var index = -1;
                _.each(array, function(item, ind) {
                    if (item.toString().toLowerCase() === string.toString().toLowerCase()) {
                        index = ind;
                    }
                });
                return index;
            }

            function changeDataBasedOnGenes(genes, index, callback) {
                if (index < genes.length) {
                    var documents = Documents.get({title: genes[index]});
                    var document =
                        _.isArray(documents) && documents.length === 1 ?
                            documents[0] : null;
                    if (document) {
                        storage.getRealtimeDocument(document.id).then(function(realtime) {
                            if (realtime && realtime.error) {
                                console.log('did not get realtime document.');
                            } else {
                                // console.log(document.title, '\t\t', index + 1);
                                var model = realtime.getModel();
                                var gene = model.getRoot().get('gene');
                                if (gene) {
                                    var geneName = gene.name.getText();
                                    console.log('----' + (index + 1) + ' ' + geneName + '----');
                                    // model.beginCompoundOperation();
                                    gene.mutations.asArray().forEach(function(mutation, index) {
                                        var oncogenic = mutation.oncogenic.getText();
                                        var mutationEffect = mutation.effect.value.getText();
                                        var mutationName = mutation.name.getText();

                                        if (mutationName.toLowerCase() === 'fusion' || mutationName.toLowerCase() === 'fusions') {
                                            console.log(geneName + '\t' + mutationName);
                                            console.log('\t' + oncogenic + '\t' + mutationEffect);
                                            if (oncogenic !== 'Likely') {
                                                console.log('\t\tOncogenic is changing to Likely.');
                                                mutation.oncogenic.setText('Likely');
                                            }
                                            if (mutationEffect !== 'Likely Gain-of-function') {
                                                console.log('\t\tMutation effect is changing to Likely Gain-of-function.');
                                                mutation.effect.value.setText('Likely Gain-of-function');
                                            }
                                        }
                                    });
                                    // model.endCompoundOperation();
                                    $timeout(function() {
                                        changeDataBasedOnGenes(genes, ++index, callback);
                                    }, 500, false);
                                } else {
                                    console.log('\t\tNo gene model.');
                                    $timeout(function() {
                                        changeDataBasedOnGenes(genes, ++index, callback);
                                    }, 500, false);
                                }
                            }
                        });
                    } else {
                        console.log('\t\tDocuments are wrong:' + documents);
                        $timeout(function() {
                            changeDataBasedOnGenes(genes, ++index, callback);
                        }, 500, false);
                    }
                } else if (_.isFunction(callback)) {
                    callback();
                }
            }

            function insertTreatment(levels, levelIndex, callback) {
                if (levelIndex < levels.length) {
                    var record = levels[levelIndex];
                    var documents = Documents.get({title: record.gene});
                    var document =
                        _.isArray(documents) && documents.length === 1 ?
                            documents[0] : null;
                    if (document) {
                        storage.getRealtimeDocument(document.id).then(function(realtime) {
                            if (realtime && realtime.error) {
                                console.log('did not get realtime document.');
                            } else {
                                var model = realtime.getModel();
                                var gene = model.getRoot().get('gene');
                                if (gene) {
                                    var geneName = gene.name.getText();
                                    var foundMutation = false;
                                    console.info(stringUtils.stringObject(record));

                                    gene.mutations.asArray().forEach(function(mutation) {
                                        var mutationName = mutation.name.getText();
                                        if (isExist([record.mutation], mutationName)) {
                                            var foundTT = false;
                                            mutation.tumors.asArray().forEach(function(tumor) {
                                                if (isExist([record.tumorType], MainUtils.getCancerTypesName(tumor.cancerTypes))) {
                                                    tumor.TI.asArray().forEach(function(ti) {
                                                        if (ti.name.getText() === 'Investigational implications for sensitivity to therapy') {
                                                            var foundTreatment = false;
                                                            ti.treatments.asArray().forEach(function(treatment) {
                                                                var treatmentName = treatment.name.getText();
                                                                if (isExist([record.treatment], treatmentName)) {
                                                                    // Treatment exists
                                                                    console.info('\tTreatment exists');
                                                                    foundTreatment = true;
                                                                    if (treatment.level.getText() === '4') {
                                                                        if (treatment.description.getText().indexOf(record.pmids) === -1) {
                                                                            console.info(record.pmids, treatment.description.getText());
                                                                            treatment.description.setText(treatment.description.getText() + ' ' + record.pmids);
                                                                        } else {
                                                                            console.error('\tPMIDs exist');
                                                                        }
                                                                    } else {
                                                                        console.error('\tThe exist treatment is not level 4');
                                                                    }
                                                                }
                                                            });
                                                            if (!foundTreatment) {
                                                                // Create new treatment
                                                                var newTreatment = MainUtils.createTreatment(model, record.treatment);
                                                                console.log('\tNeed to create new treatments');
                                                                newTreatment.description.setText(record.pmids);
                                                                newTreatment.level.setText('4');
                                                                ti.treatments.push(newTreatment);
                                                            }
                                                        }
                                                    });
                                                    $timeout(function() {
                                                        insertTreatment(levels, ++levelIndex, callback);
                                                    }, 500, false);
                                                    foundTT = true;
                                                }
                                            });
                                            if (!foundTT) {
                                                // Need to create tumor type
                                                console.log('Need to create tumor type');
                                                DatabaseConnector.getOncoTreeTumorTypeByName(record.tumorType, true)
                                                    .then(function(data) {
                                                        if (_.isObject(data) && _.isArray(data.data) && data.data.length > 0) {
                                                            var newTumorType = MainUtils.createTumorType(model);
                                                            _.each(data.data, function(ct) {
                                                                if (ct.mainType && ct.mainType.name) {
                                                                    var cancerType = MainUtils.createCancerType(model, ct.mainType.name, ct.name, ct.code);
                                                                    newTumorType.cancerTypes.push(cancerType);
                                                                }
                                                            });

                                                            var newTreatment = MainUtils.createTreatment(model, record.treatment);
                                                            newTreatment.description.setText(record.pmids);
                                                            newTreatment.level.setText('4');
                                                            newTumorType.TI.get(2).treatments.push(newTreatment);

                                                            mutation.tumors.push(newTumorType);
                                                            console.info('\tNewly generated tumor type', newTumorType.toString());
                                                            $timeout(function() {
                                                                insertTreatment(levels, ++levelIndex, callback);
                                                            }, 500, false);
                                                        } else {
                                                            DatabaseConnector.getOncoTreeTumorTypesByMainType(record.tumorType)
                                                                .then(function(data) {
                                                                    var newTumorType;
                                                                    var newTreatment;
                                                                    if (_.isObject(data) && _.isArray(data.data) && data.data.length > 0) {
                                                                        newTumorType = MainUtils.createTumorType(model);
                                                                        _.each(data.data, function(ct) {
                                                                            if (ct.mainType && ct.mainType.name) {
                                                                                var cancerType = MainUtils.createCancerType(model, ct.mainType.name, ct.name, ct.code);
                                                                                newTumorType.cancerTypes.push(cancerType);
                                                                            }
                                                                        });

                                                                        newTreatment = MainUtils.createTreatment(model, record.treatment);
                                                                        newTreatment.description.setText(record.pmids);
                                                                        newTreatment.level.setText('4');
                                                                        newTumorType.TI.get(2).treatments.push(newTreatment);

                                                                        mutation.tumors.push(newTumorType);
                                                                    } else {
                                                                        console.error('\tNo OncoTree match');
                                                                        if (isExist(['all tumors'], record.tumorType)) {
                                                                            newTumorType = MainUtils.createTumorType(model);
                                                                            var cancerType = MainUtils.createCancerType(model, record.tumorType);
                                                                            newTumorType.cancerTypes.push(cancerType);

                                                                            newTreatment = MainUtils.createTreatment(model, record.treatment);
                                                                            newTreatment.description.setText(record.pmids);
                                                                            newTreatment.level.setText('4');
                                                                            newTumorType.TI.get(2).treatments.push(newTreatment);

                                                                            mutation.tumors.push(newTumorType);
                                                                        } else {
                                                                            console.log('\tNot special tumor type neither');
                                                                        }
                                                                    }
                                                                    $timeout(function() {
                                                                        insertTreatment(levels, ++levelIndex, callback);
                                                                    }, 500, false);
                                                                });
                                                        }
                                                    });
                                            }
                                            foundMutation = true;
                                        }
                                    });

                                    if (!foundMutation) {
                                        console.log('Need to create new mutation');
                                        var newMutation = MainUtils.createMutation(model, record.mutation);
                                        DatabaseConnector.getOncoTreeTumorTypeByName(record.tumorType, true)
                                            .then(function(data) {
                                                if (_.isObject(data) && _.isArray(data.data) && data.data.length > 0) {
                                                    var newTumorType = MainUtils.createTumorType(model);
                                                    _.each(data.data, function(ct) {
                                                        if (ct.mainType && ct.mainType.name) {
                                                            var cancerType = MainUtils.createCancerType(model, ct.mainType.name, ct.name, ct.code);
                                                            newTumorType.cancerTypes.push(cancerType);
                                                        }
                                                    });

                                                    // mutation.tumors.push(_tumorType);

                                                    var newTreatment = MainUtils.createTreatment(model, record.treatment);
                                                    newTreatment.description.setText(record.pmids);
                                                    newTreatment.level.setText('4');
                                                    newTumorType.TI.get(2).treatments.push(newTreatment);

                                                    newMutation.tumors.push(newTumorType);
                                                    gene.mutations.push(newMutation);
                                                    $timeout(function() {
                                                        insertTreatment(levels, ++levelIndex, callback);
                                                    }, 500, false);
                                                } else {
                                                    DatabaseConnector.getOncoTreeTumorTypesByMainType(record.tumorType)
                                                        .then(function(data) {
                                                            var newTumorType;
                                                            var newTreatment;
                                                            if (_.isObject(data) && _.isArray(data.data) && data.data.length > 0) {
                                                                newTumorType = MainUtils.createTumorType(model);
                                                                _.each(data.data, function(ct) {
                                                                    if (ct.mainType && ct.mainType.name) {
                                                                        var cancerType = MainUtils.createCancerType(model, ct.mainType.name, ct.name, ct.code);
                                                                        newTumorType.cancerTypes.push(cancerType);
                                                                    }
                                                                });

                                                                newTreatment = MainUtils.createTreatment(model, record.treatment);
                                                                newTreatment.description.setText(record.pmids);
                                                                newTreatment.level.setText('4');
                                                                newTumorType.TI.get(2).treatments.push(newTreatment);

                                                                newMutation.tumors.push(newTumorType);
                                                            } else {
                                                                console.error('\tNo OncoTree match on');
                                                                if (isExist(['all tumors'], record.tumorType)) {
                                                                    newTumorType = MainUtils.createTumorType(model);
                                                                    var cancerType = MainUtils.createCancerType(model, record.tumorType);
                                                                    newTumorType.cancerTypes.push(cancerType);

                                                                    newTreatment = MainUtils.createTreatment(model, record.treatment);
                                                                    newTreatment.description.setText(record.pmids);
                                                                    newTreatment.level.setText('4');
                                                                    newTumorType.TI.get(2).treatments.push(newTreatment);

                                                                    newMutation.tumors.push(newTumorType);
                                                                } else {
                                                                    console.log('\tNot special tumor type neither');
                                                                }
                                                            }
                                                            gene.mutations.push(newMutation);
                                                            $timeout(function() {
                                                                insertTreatment(levels, ++levelIndex, callback);
                                                            }, 500, false);
                                                        });
                                                }
                                            });
                                    }
                                } else {
                                    console.log('\t\tNo gene model.');
                                    $timeout(function() {
                                        insertTreatment(levels, ++levelIndex, callback);
                                    }, 500, false);
                                }
                            }
                        });
                    } else {
                        console.log('\t\tDocuments are wrong:' + documents);
                        $timeout(function() {
                            insertTreatment(levels, ++levelIndex, callback);
                        }, 500, false);
                    }
                } else if (_.isFunction(callback)) {
                    callback();
                }
            }

            function changeData(index, callback) {
                if (index < $scope.documents.length) {
                    var document = $scope.documents[index];
                    storage.getRealtimeDocument(document.id).then(function(realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            // console.log(document.title, '\t\t', index + 1);
                            var model = realtime.getModel();
                            var gene = model.getRoot().get('gene');
                            if (gene) {
                                var geneName = gene.name.getText();
                                var result = [];
                                console.log((index + 1) + ' Gene: ' + geneName);
                                // model.beginCompoundOperation();
                                gene.mutations.asArray().forEach(function(mutation, index) {
                                    var oncogenic = mutation.oncogenic.getText().trim().toLowerCase();
                                    var mutationEffect = mutation.effect.value.getText().trim().toLowerCase();
                                    var summary = mutation.summary.getText();
                                    var shortSummary = mutation.shortSummary.getText();
                                    var desp = mutation.description.getText();
                                    var shortDesp = mutation.short.getText();
                                    var tumorTypes = mutation.tumors;
                                    var mutationName = mutation.name.getText();

                                    // if(oncogenic === 'unknown') {
                                    //     result = [geneName, mutationName, '', '', '', 'Change oncogenic to inconclusive', mutation.oncogenic.getText()];
                                    //     console.log(result.join('\t'));
                                    //     mutation.oncogenic.setText('Inconclusive');
                                    // }
                                    //
                                    // if(mutationEffect === 'unknown') {
                                    //     result = [geneName, mutationName, '', '', '', 'Change ME to inconclusive', mutation.effect.value.getText()];
                                    //     console.log(result.join('\t'));
                                    //     mutation.effect.value.setText('Inconclusive');
                                    // }
                                    //
                                    // if(!stringUtils.isUndefinedOrEmpty(shortDesp)) {
                                    //     result = [geneName, mutationName, '', '', '', 'Biological effect', shortDesp, desp];
                                    //     console.log(result.join('\t'));
                                    //     mutation.description.setText(shortDesp);
                                    //     mutation.short.setText(desp);
                                    // }
                                    //
                                    // if(!stringUtils.isUndefinedOrEmpty(shortSummary)) {
                                    //     result = [geneName, mutationName, '', '', '', 'Clinical Effect', shortSummary, summary];
                                    //     console.log(result.join('\t'));
                                    //     if(stringUtils.isUndefinedOrEmpty(summary)) {
                                    //         mutation.summary.setText(shortSummary);
                                    //         mutation.shortSummary.setText('');
                                    //     }
                                    // }

                                    // if(isUndefinedOrEmpty(mutationEffect) &&
                                    //     (_.isString(oncogenic) && oncogenic.toLowerCase() === 'inconclusive') &&
                                    //     isUndefinedOrEmpty(oncogenicSummary) &&
                                    //     isUndefinedOrEmpty(mutationDesp) &&
                                    //     isUndefinedOrEmpty(mutationShortDesp)) {
                                    //     console.log(gene.name.getText() + '\t' + mutation.name.getText() + (tumorTypes.length === 0 ? "\tNo cancer type" : "\tHas cancer type"));
                                    //     // mutation.oncogenic.setText('');
                                    // }
                                    // if (mutation.oncogenic_eStatus.get('curated') === false && mutation.name_eStatus.get('obsolete') !== 'true') {
                                    //     console.log("Red hand\t" + gene.name.getText() + '\t' + mutation.name.getText());
                                    // }
                                    // if (mutation.name_eStatus.get('obsolete') === 'true') {
                                    //     console.log("Obsoleted\t" + gene.name.getText() + '\t' + mutation.name.getText());
                                    // }

                                    // if (!isUndefinedOrEmpty(oncogenic) && !isUndefinedOrEmpty(mutationEffect)) {
                                    //     var lOncogenic = oncogenic.toLowerCase();
                                    //     var lME = mutationEffect.toLowerCase();
                                    //     if (lOncogenic === 'unknown') {
                                    //         if (lME === 'unknown') {
                                    //             console.log("Both Unknown\t" + gene.name.getText() + '\t' + mutation.name.getText() + '\t' + oncogenic + '\t' + mutationEffect);
                                    //         } else {
                                    //             console.log("Oncogenic Unknown\t" + gene.name.getText() + '\t' + mutation.name.getText() + '\t' + oncogenic + '\t' + mutationEffect);
                                    //         }
                                    //     } else if (lME === 'unknown') {
                                    //         console.log("ME Unknown\t" + gene.name.getText() + '\t' + mutation.name.getText() + '\t' + oncogenic + '\t' + mutationEffect);
                                    //     }
                                    // }else {
                                    //     console.log("Both Empty\t" + gene.name.getText() + '\t' + mutation.name.getText() + '\t' + oncogenic + '\t' + mutationEffect);
                                    // }
                                    // if(mutationName.indexOf(',') !== -1 || mutationName.indexOf('/') !== -1 ) {
                                    //     console.log("String mutation\t" + gene.name.getText() + '\t' + mutation.name.getText());
                                    // }
                                    //
                                    // if (isUndefinedOrEmpty(mutationName)) {
                                    //     console.error('Mutation Name is empty');
                                    // } else {
                                    //     if (mutationName.toLowerCase().indexOf('fusion') !== -1) {
                                    //         console.log("Fusions\t" + gene.name.getText() + '\t' + mutation.name.getText() + '\t' + oncogenic + '\t' + mutationEffect);
                                    //     }
                                    // }
                                    //
                                    // if (mutationName.indexOf('Truncat') !== -1) {
                                    //     console.log("Truncating mutations\t" + gene.name.getText() + '\t' + mutation.name.getText() + '\t' + oncogenic + '\t' + mutationEffect);
                                    // }
                                    //
                                    // if(mutationName.indexOf('Delet') !== -1 ) {
                                    //     console.log("Deletions\t" + gene.name.getText() + '\t' + mutation.name.getText());
                                    // }
                                    //
                                    // if(mutationName.indexOf('Amplif') !== -1 ) {
                                    //     console.log("Amplification\t" + gene.name.getText() + '\t' + mutation.name.getText());
                                    // }
                                    // if(tumorTypes.length > 0 &&
                                    // isUndefinedOrEmpty(mutationEffect) &&
                                    // isUndefinedOrEmpty(mutationDesp) &&
                                    // isUndefinedOrEmpty(mutationShortDesp)) {
                                    //     console.log(gene.name.getText() + '\t' + mutation.name.getText() + "\tNo mutation effect but has treatments.");
                                    // }

                                    // if(isUndefinedOrEmpty(mutationEffect)) {
                                    //     category[0] = '0';
                                    // }else {
                                    //     category[0] = '1';
                                    // }
                                    //
                                    // if(isUndefinedOrEmpty(oncogenic)) {
                                    //     category[1] = '0';
                                    // }else {
                                    //     category[1] = '1';
                                    // }
                                    //
                                    // if(containPMID) {
                                    //     category[2] = '1';
                                    // }else {
                                    //     category[2] = '0';
                                    // }
                                    // if( category.join('') !== '111' &&
                                    //     mutation.oncogenic_eStatus.get('curated')===true &&
                                    //     mutation.name_eStatus.get('obsolete') === 'false')
                                    //     console.log(gene.name.getText() + '\t' + mutation.name.getText() + "\t" + category.join(''));

                                    // mutation.tumors.asArray().forEach(function(tumor) {
                                    // var tumorName = MainUtils.getCancerTypesName(tumor.cancerTypes);
                                    // var tumorShortPrev = tumor.shortPrevalence.getText();
                                    // var tumorPrev = tumor.prevalence.getText();
                                    // var tumorShortNCCN = tumor.nccn.short.getText();
                                    // var tumorNCCN = tumor.nccn.description.getText();
                                    // var tumorShortProgImp = tumor.shortProgImp.getText();
                                    // var tumorProgImp = tumor.progImp.getText();
                                    //
                                    // if(!stringUtils.isUndefinedOrEmpty(tumorShortPrev)) {
                                    //     result = [geneName, mutationName, tumorName, '', '', 'tumorPrev', tumorShortPrev, tumorPrev];
                                    //     console.log(result.join('\t'));
                                    //     tumor.prevalence.setText(tumorShortPrev);
                                    //     tumor.shortPrevalence.setText(tumorPrev);
                                    // }
                                    // if(!stringUtils.isUndefinedOrEmpty(tumorShortNCCN)) {
                                    //     result = [geneName, mutationName, tumorName, '', '', 'tumorNCCN', tumorShortNCCN, tumorNCCN];
                                    //     console.log(result.join('\t'));
                                    //     tumor.nccn.description.setText(tumorShortNCCN);
                                    //     tumor.nccn.short.setText(tumorNCCN);
                                    // }
                                    // if(!stringUtils.isUndefinedOrEmpty(tumorShortProgImp)) {
                                    //     result = [geneName, mutationName, tumorName, '', '', 'ProgImp', tumorShortProgImp, tumorProgImp];
                                    //     console.log(result.join('\t'));
                                    //     tumor.progImp.setText(tumorShortProgImp);
                                    //     tumor.shortProgImp.setText(tumorProgImp);
                                    // }

                                    // tumor.TI.asArray().forEach(function(ti) {
                                    //     var tiName = ti.name.getText();
                                    // ti.treatments.asArray().forEach(function(treatment) {
                                    //     var treatmentName = treatment.name.getText();
                                    //     var short = treatment.short.getText();
                                    //     var full = treatment.description.getText();
                                    //
                                    //     if(!stringUtils.isUndefinedOrEmpty(short)) {
                                    //         result = [geneName, mutationName, tumorName, tiName, treatmentName, 'treatment', short, full];
                                    //         console.log(result.join('\t'));
                                    //         treatment.description.setText(short);
                                    //         treatment.short.setText(full);
                                    //     }
                                    // if (treatment.level.getText() === 'R1') {
                                    // var result = [gene.name.getText(),
                                    //     mutation.name.getText(),
                                    //     MainUtils.getCancerTypesName(tumor.cancerTypes),
                                    //     treatment.name.getText(),
                                    //     treatment.indication.getText(),
                                    //     treatment.level.getText(),
                                    //     getString(treatment.short.getText()),
                                    //     getString(treatment.description.getText()),
                                    //     treatment.name_eStatus.get('obsolete')
                                    // ];
                                    // console.log(result.join('\t'));
                                    // }
                                    // });
                                    // });
                                    // });
                                });
                                // model.endCompoundOperation();
                                $timeout(function() {
                                    changeData(++index, callback);
                                }, 100, false);
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function() {
                                    changeData(++index, callback);
                                }, 100, false);
                            }
                        }
                    });
                } else if (_.isFunction(callback)) {
                    callback();
                }
            }

            function showValidationResult(index, callback) {
                if (index < $scope.documents.length) {
                    var document = $scope.documents[index];
                    storage.getRealtimeDocument(document.id).then(function(realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            var model = realtime.getModel();
                            var gene = model.getRoot().get('gene');
                            if (gene) {
                                var geneName = gene.name.getText();
                                gene.mutations.asArray().forEach(function(mutation, index) {
                                    var oncogenic = mutation.oncogenic.getText();
                                    var mutationEffect = mutation.effect.value.getText();
                                    var mutationName = mutation.name.getText();

                                    // Not obsoleted variants but are not curated
                                    if (mutation.oncogenic_eStatus.get('curated') === false && mutation.name_eStatus.get('obsolete') !== 'true') {
                                        console.log(geneName + '\t' + mutationName + '\tRed hand');
                                    }

                                    // Obsoleted variants
                                    if (mutation.name_eStatus.get('obsolete') === 'true') {
                                        console.log(geneName + '\t' + mutationName + '\tObsoleted');
                                    }

                                    // Not obsolete variant, but oncogenic is obsoleted.
                                    if (mutation.shortSummary_eStatus.get('obsolete') === 'true' && mutation.name_eStatus.get('obsolete') !== 'true') {
                                        console.log(geneName + '\t' + mutationName + '\tNot obsolete variant, but oncogenic is obsoleted.');
                                    }

                                    // Not obsolete variant, but mutation effect is obsoleted.
                                    if (mutation.oncogenic_eStatus.get('obsolete') === 'true' && mutation.name_eStatus.get('obsolete') !== 'true') {
                                        console.log(geneName + '\t' + mutationName + '\tNot obsolete variant, but mutation effect is obsoleted.');
                                    }

                                    // Both inconclusive/inconclusive for oncogenicity and mutation effect
                                    if (_.isString(oncogenic) &&
                                        _.isString(mutationEffect) &&
                                        oncogenic.toLowerCase() === 'inconclusive' &&
                                        mutationEffect.toLowerCase() === 'inconclusive') {
                                        console.log(geneName + '\t' + mutationName + '\tBoth inconclusive');
                                    }
                                });
                                $timeout(function() {
                                    showValidationResult(++index, callback);
                                }, 500, false);
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function() {
                                    showValidationResult(++index, callback);
                                }, 500, false);
                            }
                        }
                    });
                } else if (_.isFunction(callback)) {
                    callback();
                }
            }

            function isUndefinedOrEmpty(str) {
                if (_.isUndefined(str)) {
                    return true;
                }
                return str.toString().trim() === '';
            }

            function findRelevantVariants(list, index, callback) {
                if (index < list.length) {
                    var result = Documents.get({title: list[index].gene});
                    var document = _.isArray(result) ? result[0] : '';
                    var message = (index + 1) + '\t' + list[index].gene + ' ' + list[index].alt;

                    if (document) {
                        storage.getRealtimeDocument(document.id).then(function(realtime) {
                            if (realtime && realtime.error) {
                                console.log('Did not get realtime document.');
                            } else {
                                var model = realtime.getModel();
                                var gene = model.getRoot().get('gene');
                                if (gene) {
                                    // model.beginCompoundOperation();
                                    var found = [];
                                    gene.mutations.asArray().forEach(function(mutation, mutationIndex) {
                                        if (mutation.name.getText().trim().toLowerCase() === list[index].alt.trim().toLowerCase()) {
                                            found.push(mutationIndex);
                                        }
                                    });

                                    if (found.length > 0) {
                                        message += '\t\tFound mapping';
                                        if (found.length > 1) {
                                            message += '\t\tFound duplicates.\t' + JSON.stringify(found);
                                        }
                                    } else {
                                        message += '\t\tNo mapping';
                                    }
                                    console.log(message);
                                    // model.endCompoundOperation();

                                    // Google has limitation for numbere of requests within one second
                                    $timeout(function() {
                                        findRelevantVariants(list, ++index, callback);
                                    }, 300, false);
                                } else {
                                    console.log('\t\tNo gene model.');
                                    $timeout(function() {
                                        findRelevantVariants(list, ++index, callback);
                                    }, 300, false);
                                }
                            }
                        });
                    } else {
                        console.log('\t\tNo gene found.');
                        $timeout(function() {
                            findRelevantVariants(list, ++index, callback);
                        }, 300, false);
                    }
                } else if (_.isFunction(callback)) {
                    callback();
                }
            }

            function convertData(index, callback) {
                if (index < $scope.documents.length) {
                    var document = $scope.documents[index];
                    storage.getRealtimeDocument(document.id).then(function(realtime) {
                        if (realtime && realtime.error) {
                            console.log('Did not get realtime document.');
                        } else {
                            console.log(document.title, '\t\t', index + 1);
                            var model = realtime.getModel();
                            var gene = model.getRoot().get('gene');
                            if (gene) {
                                // model.beginCompoundOperation();
                                gene.mutations.asArray().forEach(function(mutation) {
                                    if (mutation.shor) {
                                        mutation.tumors.asArray().forEach(function(tumor) {
                                            var tumorName = tumor.name.getText();
                                            var message = '\tGene: ' + gene.name.getText() +
                                                '\tMutation: ' + mutation.name.getText() +
                                                '\tTumor type: ' + tumorName;

                                            var hasNLung = false;
                                            var hasSLung = false;
                                            var sLungIndices = [];

                                            _.each(tumor.cancerTypes.asArray(), function(ct, ctIndex) {
                                                if (ct.cancerType.getText().trim() === 'Small Cell Lung Cancer') {
                                                    sLungIndices.push(ctIndex);
                                                    hasSLung = true;
                                                }
                                                if (ct.cancerType.getText().trim() === 'Non-Small Cell Lung Cancer') {
                                                    hasNLung = true;
                                                }
                                            });

                                            if (hasNLung && hasSLung) {
                                                message += '\t' + JSON.stringify(sLungIndices);
                                                console.log(message);

                                                if (sLungIndices.length > 1) {
                                                    console.error('\t\t\t\tHas multiple small cell');
                                                } else if (sLungIndices.length === 1) {
                                                    console.log('\t\t\t\tRemoving...');
                                                    // tumor.cancerTypes.remove(sLungIndices[0]);
                                                }
                                            }
                                        });
                                    }
                                });
                                // model.endCompoundOperation();

                                // Google has limitation for numbere of requests within one second
                                $timeout(function() {
                                    convertData(++index, callback);
                                }, 300, false);
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function() {
                                    convertData(++index, callback);
                                }, 300, false);
                            }
                        }
                    });
                } else if (_.isFunction(callback)) {
                    callback();
                }
            }

            function convertTumorTypeToOncoTree(index, callback) {
                if (index < $scope.documents.length) {
                    var document = $scope.documents[index];
                    storage.getRealtimeDocument(document.id).then(function(realtime) {
                        if (realtime && realtime.error) {
                            console.log('Did not get realtime document.');
                        } else {
                            console.log(document.title, '\t\t', index + 1);
                            var model = realtime.getModel();
                            var gene = model.getRoot().get('gene');
                            if (gene) {
                                model.beginCompoundOperation();
                                gene.mutations.asArray().forEach(function(mutation, index) {
                                    // var tumors = {};
                                    mutation.tumors.asArray().forEach(function(tumor) {
                                        // Convert to desired OncoTree tumor types
                                        // if(tumors.hasOwnProperty(tumor.name.getText())) {
                                        //     var message = '\tGene: ' + gene.name.getText() +
                                        //         '\tMutation: ' + mutation.name.getText() +
                                        //         '\tTumor type: ' + tumor.name.getText();
                                        //     console.log(message);
                                        // }else {
                                        //     tumors[tumor.name.getText()] = 1;
                                        // }
                                        var tt = tumor.name.getText().toString().trim().toLowerCase().replace('’', '\'');
                                        var message = '\tGene: ' + gene.name.getText() +
                                            '\tMutation: ' + mutation.name.getText() +
                                            '\tTumor type: ' + tumor.name.getText();
                                        var mapped = $scope.mappedTumorTypes[tt];
                                        if (_.isArray(mapped) && mapped.length > 0) {
                                            var mappedName = [];
                                            _.each(mapped, function(map, index) {
                                                var exist = false;
                                                _.each(tumor.cancerTypes.asArray(), function(ct) {
                                                    if (ct.cancerType.getText() === map.name) {
                                                        exist = true;
                                                    }
                                                });
                                                if (!exist) {
                                                    var cancerType = model.create(OncoKB.CancerType);
                                                    cancerType.cancerType.setText(map.name);
                                                    cancerType.cancerType_eStatus.set('obsolete', 'false');
                                                    cancerType.subtype_eStatus.set('obsolete', 'false');
                                                    cancerType.oncoTreeCode_eStatus.set('obsolete', 'false');
                                                    tumor.cancerTypes.push(cancerType);
                                                }
                                                mappedName.push(map.name);
                                                message += '\t' + (index + 1) + ': ' + map.name;
                                            });
                                            message += '\tMapped name:' + mappedName.join(', ');
                                        } else {
                                            message += '\tNo map.';
                                        }
                                        console.log(message);
                                    });
                                });
                                model.endCompoundOperation();

                                // Google has limitation for numbere of requests within one second
                                $timeout(function() {
                                    convertTumorTypeToOncoTree(++index, callback);
                                }, 500, false);
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function() {
                                    convertTumorTypeToOncoTree(++index, callback);
                                }, 500, false);
                            }
                        }
                    });
                } else if (_.isFunction(callback)) {
                    callback();
                }
            }

            function findMainType(mainType) {
                var mainTypes = [];
                if (mainType === 'Skin Cancer, Non-Melanoma') {
                    mainTypes = ['Skin Cancer, Non-Melanoma'];
                } else {
                    mainTypes = mainType.split(',').map(function(item) {
                        return item.toString().trim();
                    });
                }
                var map = [];
                _.each(mainTypes, function(mt) {
                    for (var i = 0; i < $scope.oncoTree.mainTypes.length; i++) {
                        if ($scope.oncoTree.mainTypes[i].name.toString().trim().toLowerCase() === mt.toLowerCase()) {
                            map.push($scope.oncoTree.mainTypes[i]);
                            break;
                        }
                    }
                });

                return map;
            }

            function getAlteration(codon, aa) {
                var alteration = [];
                if (codon) {
                    if (aa) {
                        var variants = aa.split(/\|/);
                        var filters = [];
                        variants.forEach(function(e, i) {
                            var components = e.split(':').map(function(str) {
                                return str.trim();
                            });
                            if (components.length === 2 && Number(components[1]) >= 5) {
                                filters.push(components[0]);
                            }
                        });

                        if (filters.length > 0) {
                            alteration = filters.map(function(e) {
                                return codon + e;
                            });
                        }
                    } else {
                        alteration.push(codon);
                    }
                }
                return alteration;
            }

            function initial(docs, docIndex, callback) {
                if (docIndex < docs.length) {
                    var fileId = docs[docIndex].id;
                    storage.getRealtimeDocument(fileId).then(function(realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            realtime.addEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED, function(evt) {
                                if (!evt.isSaving) {
                                    realtime.removeEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED);
                                    storage.closeDocument();
                                    $timeout(function() {
                                        initial(docs, ++docIndex, callback);
                                    }, 200, false);
                                }
                            });
                            console.log(docs[docIndex].title, '\t\t', docIndex + 1);
                            console.log('\t Initializing status...');
                            var model = realtime.getModel();
                            var gene = model.getRoot().get('gene');
                            if (gene) {
                                model.beginCompoundOperation();
                                gene.mutations.asArray().forEach(function(mutation) {
                                    if (!mutation.shortSummary_eStatus.has('obsolete')) {
                                        mutation.shortSummary_eStatus.set('obsolete', 'false');
                                    }
                                    if (!mutation.shortSummary_eStatus.has('vetted')) {
                                        mutation.shortSummary_eStatus.set('vetted', 'uv');
                                    }
                                    // //console.log('Add mutation estatus');
                                    // mutation.tumors.asArray().forEach(function (tumor) {
                                    //    if (!tumor.prevalence_eStatus.has('obsolete')) {
                                    //        tumor.prevalence_eStatus.set('obsolete', 'false');
                                    //    }
                                    //    if (!tumor.progImp_eStatus.has('obsolete')) {
                                    //        tumor.progImp_eStatus.set('obsolete', 'false');
                                    //    }
                                    //
                                    //    //console.log('Add tumor estatus');
                                    //    tumor.TI.asArray().forEach(function (ti) {
                                    //        ti.treatments.asArray().forEach(function (treatment) {
                                    //            if (!treatment.name_eStatus.has('obsolete')) {
                                    //                treatment.name_eStatus.set('obsolete', 'false');
                                    //            }
                                    //        })
                                    //    });
                                    // });
                                });
                                model.endCompoundOperation();
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function() {
                                    initial(docs, ++docIndex, callback);
                                }, 200, false);
                            }
                        }
                    });
                } else {
                    if (callback) {
                        callback();
                    }
                    console.log('finished.');
                }
            }
            function createDoc(index) {
                if (index < newGenes.length) {
                    var gene = newGenes[index];
                    var _documents = Documents.get({title: gene});
                    if (!_.isArray(_documents) || _documents.length === 0) {
                        MainUtils.createGene(gene)
                            .then(function(result) {
                                if (result && result.error) {
                                    console.log('Error creating document.');
                                } else {
                                    $timeout(function() {
                                        createDoc(++index);
                                    }, 2000);
                                }
                            });
                    } else {
                        createDoc(++index);
                    }
                } else {
                    console.log('finished');
                }
            }

            /**
             * Update gene type
             * @param {Array} genes List of genes which need to be updated
             * @param {number} index The current index of genes
             * @param {Function} callback The function to be called after all genes have been updated
             */
            function setGeneType(genes, index, callback) {
                if (index < $scope.documents.length) {
                    var document = $scope.documents[index];
                    storage.getRealtimeDocument(document.id).then(function(realtime) {
                        if (realtime && realtime.error) {
                            console.log('Did not get realtime document.');
                        } else {
                            // console.log(document.title, '\t\t', index + 1);
                            var model = realtime.getModel();
                            var gene = model.getRoot().get('gene');
                            if (gene) {
                                var hugoSymbol = gene.name.getText();
                                // model.beginCompoundOperation();
                                // console.log('TSG: ' + gene.type.get('TSG') +
                                //     ' OCG: ' + gene.type.get('OCG'));
                                var result = [index + 1, hugoSymbol];

                                if (gene.type.get('TSG') && gene.type.get('OCG')) {
                                    result.push('Both');
                                } else {
                                    result.push(gene.type.get('TSG') || gene.type.get('OCG'));
                                }

                                var hasNewData = false;
                                for (var i = 0; i < genes.length; i++) {
                                    var _gene = genes[i];
                                    if (_gene.gene === hugoSymbol &&
                                        _gene.type) {
                                        result.push(_gene.type);
                                        hasNewData = true;
                                        break;
                                    }
                                }
                                if (!hasNewData) {
                                    result.push('');
                                }

                                var hasTruncating = false;
                                gene.mutations.asArray().forEach(function(mutation, index) {
                                    var mutationName = mutation.name.getText();
                                    if (mutationName && mutationName === 'Truncating Mutations') {
                                        hasTruncating = true;
                                    }
                                });

                                if (result[3] !== result[2]) {
                                    if (result[3] === 'Both') {
                                        gene.type.set('TSG', 'Tumor Suppressor');
                                        gene.type.set('OCG', 'Oncogene');
                                    } else if (result[3] === 'Oncogene') {
                                        gene.type.set('TSG', '');
                                        gene.type.set('OCG', 'Oncogene');
                                    } else if (result[3] === 'Tumor Suppressor') {
                                        gene.type.set('TSG', 'Tumor Suppressor');
                                        gene.type.set('OCG', '');
                                    } else {
                                        gene.type.set('TSG', '');
                                        gene.type.set('OCG', '');
                                    }
                                    result.push('Changed');
                                }

                                if (hasTruncating) {
                                    result.push('YES');
                                } else {
                                    result.push('');
                                }
                                console.log(result.join('&'));
                                // model.endCompoundOperation();

                                // Google has limitation for number of requests within one second
                                $timeout(function() {
                                    setGeneType(genes, ++index, callback);
                                }, 300, false);
                            } else {
                                // console.log('\t\tNo gene model.');
                                $timeout(function() {
                                    setGeneType(genes, ++index, callback);
                                }, 300, false);
                            }
                        }
                    });
                } else if (_.isFunction(callback)) {
                    callback();
                }
            }
        }]
    );
