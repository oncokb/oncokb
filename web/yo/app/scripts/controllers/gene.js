'use strict';

angular.module('oncokbApp')
    .controller('GenesCtrl', ['$scope', '$rootScope', '$location', '$timeout',
        '$routeParams', '_', 'config', 'importer', 'storage', 'documents',
        'users', 'DTColumnDefBuilder', 'DTOptionsBuilder', 'DatabaseConnector',
        'OncoKB', 'stringUtils', 'S', 'mainUtils', 'gapi', 'UUIDjs', 'dialogs',
        function($scope, $rootScope, $location, $timeout, $routeParams, _,
                 config, importer, storage, Documents, users,
                 DTColumnDefBuilder, DTOptionsBuilder, DatabaseConnector,
                 OncoKB, stringUtils, S, MainUtils, gapi, UUIDjs, dialogs) {
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
                                var geneData = stringUtils.getGeneData(gene, excludeObsolete, true, true, false);
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

            $scope.getDocs = function() {
                var docs = Documents.get();
                if (docs.length > 0) {
                    // $scope.$apply(function() {
                    $scope.documents = Documents.get();
                    $scope.status.rendering = false;
                    // });
                } else if (OncoKB.global.genes) {
                    storage.requireAuth(true).then(function() {
                        storage.retrieveAllFiles().then(function(result) {
                            Documents.set(result);
                            Documents.setStatus(OncoKB.global.genes);
                            $scope.documents = Documents.get();
                            $scope.status.rendering = false;
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
                                    $scope.metaFlags = {};
                                    storage.retrieveMeta().then(function(result) {
                                        if (result && result.error) {
                                            dialogs.error('Error', 'Fail to retrieve meta file! Please stop editing and contact the developer!');
                                        } else {
                                            storage.getMetaRealtimeDocument(result[0].id).then(function(metaRealtime) {
                                                if (metaRealtime && metaRealtime.error) {
                                                    dialogs.error('Error', 'Fail to get meta document! Please stop editing and contact the developer!');
                                                } else {
                                                    var metaData = metaRealtime.getModel().getRoot().get('review');
                                                    var genes = metaData.keys();
                                                    for (var i = 0; i < genes.length; i++) {
                                                        var geneMetaData = metaData.get(genes[i]);
                                                        var uuids = geneMetaData.keys();
                                                        var flag = true;
                                                        for (var j = 0; j < uuids.length; j++) {
                                                            if (geneMetaData.get(uuids[j]).type === 'Map' && geneMetaData.get(uuids[j]).get('review')) {
                                                                $scope.metaFlags[genes[i]] = true;
                                                                flag = false;
                                                                break;
                                                            }
                                                        }
                                                        if (flag) {
                                                            $scope.metaFlags[genes[i]] = false;
                                                        }
                                                    }
                                                }
                                                $scope.documents = Documents.get();
                                                $scope.status.rendering = false;
                                            });
                                        }
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

            $scope.backup = function() {
                $scope.status.backup = false;
                OncoKB.backingUp = true;
                importer.backup(function() {
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

            var sorting = [[5, 'desc'], [1, 'asc'], [0, 'asc']];
            if (users.getMe().role === 8) {
                sorting = [[7, 'desc'], [4, 'desc'], [1, 'asc'], [0, 'asc']];
            }

            $scope.dtOptions = DTOptionsBuilder
                .newOptions()
                .withDOM('ifrtlp')
                .withOption('order', sorting)
                .withBootstrap();

            $scope.dtColumns = [
                DTColumnDefBuilder.newColumnDef(0),
                DTColumnDefBuilder.newColumnDef(1),
                DTColumnDefBuilder.newColumnDef(2).notSortable(),
                DTColumnDefBuilder.newColumnDef(3).notSortable(),
                DTColumnDefBuilder.newColumnDef(4),
                DTColumnDefBuilder.newColumnDef(5),
                DTColumnDefBuilder.newColumnDef(6)
            ];
            if (users.getMe().role === 8) {
                $scope.dtColumns.push(DTColumnDefBuilder.newColumnDef(7));
            }

            $scope.status = {
                backup: true,
                saveAllGenes: true,
                migrate: true,
                rendering: true
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
        }]
    )
    .controller('GeneCtrl', ['_', 'S', '$resource', '$interval', '$timeout', '$scope', '$rootScope', '$location', '$route', '$routeParams', '$window', 'dialogs', 'importer', 'storage', 'loadFile', 'user', 'users', 'documents', 'OncoKB', 'gapi', 'DatabaseConnector', 'SecretEmptyKey', '$sce', 'jspdf', 'FindRegex', 'stringUtils',
        function(_, S, $resource, $interval, $timeout, $scope, $rootScope, $location, $route, $routeParams, $window, dialogs, importer, storage, loadFile, User, Users, Documents, OncoKB, gapi, DatabaseConnector, SecretEmptyKey, $sce, jspdf, FindRegex, stringUtils) {
            $scope.test = function(event, a, b, c, d, e, f, g) {
                $scope.stopCollopse(event);
                console.log(a, b, c, d, e, f, g);
            };
            $scope.authorize = function() {
                storage.requireAuth(false).then(function() {
                    var target = $location.search().target;
                    if (target) {
                        $location.url(target);
                    } else {
                        storage.getDocument('1rFgBCL0ftynBxRl5E6mgNWn0WoBPfLGm8dgvNBaHw38').then(function(file) {
                            storage.downloadFile(file).then(function(text) {
                                $scope.curateFile = text;
                            });
                        });
                    }
                });
            };

            $scope.addMutation = function(newMutationName) {
                if (this.gene && newMutationName) {
                    newMutationName = newMutationName.toString().trim();
                    var exists = false;
                    var isVUS = false;
                    var mutationNameBlackList = [
                        'activating mutations',
                        'activating mutation',
                        'inactivating mutations',
                        'inactivating mutation'
                    ];
                    this.gene.mutations.asArray().forEach(function(e) {
                        if (e.name.getText().toLowerCase() === newMutationName.toLowerCase()) {
                            exists = true;
                        }
                    });
                    this.vus.asArray().forEach(function(e) {
                        if (e.name.getText().toLowerCase() === newMutationName.toLowerCase()) {
                            isVUS = true;
                        }
                    });

                    if (mutationNameBlackList
                            .indexOf(newMutationName.toLowerCase()) !== -1) {
                        dialogs.notify('Warning',
                            'This mutation name is not allowed.');
                    } else if (exists) {
                        dialogs.notify('Warning', 'Mutation exists.');
                    } else if (isVUS) {
                        dialogs.notify('Warning', 'Mutation is in VUS list.');
                    } else {
                        var _mutation = '';
                        $scope.realtimeDocument.getModel().beginCompoundOperation();
                        _mutation = $scope.realtimeDocument.getModel().create(OncoKB.Mutation);
                        var filteredContent = [];
                        _.each(newMutationName.split(','), function(item) {
                            item = item.trim();
                            if (item.length > 0) {
                                filteredContent.push(item);
                            }
                        });
                        _mutation.name.setText(filteredContent.join(','));
                        _mutation.oncogenic_eStatus.set('obsolete', 'false');
                        _mutation.shortSummary_eStatus.set('obsolete', 'false');

                        this.gene.mutations.push(_mutation);
                        $scope.realtimeDocument.getModel().endCompoundOperation();
                        $scope.geneStatus[this.gene.mutations.length - 1] = new GeneStatusSingleton();
                    }
                }
            };

            $scope.stateComparator = function(state, viewValue) {
                return viewValue === SecretEmptyKey || (String(state)).toLowerCase().indexOf((String(viewValue)).toLowerCase()) > -1;
            };

            $scope.getComments = function() {
                console.log($scope.comments);
            };

            $scope.addComment = function(object, key, string) {
                var _user = Users.getMe();
                if (object && object[key + '_comments'] && _user.email) {
                    var _comment = '';
                    var _date = new Date();

                    $scope.realtimeDocument.getModel().beginCompoundOperation();
                    _comment = $scope.realtimeDocument.getModel().create('Comment');
                    _comment.date.setText(_date.getTime().toString());
                    if (_user.name) {
                        _comment.userName.setText(_user.name);
                    } else {
                        _comment.userName.setText('Unknown');
                    }
                    _comment.email.setText(_user.email);
                    _comment.content.setText(string);
                    _comment.resolved.setText('false');
                    object[key + '_comments'].push(_comment);
                    $scope.realtimeDocument.getModel().endCompoundOperation();
                } else {
                    console.log('Unable to add comment.');
                }
            };
            $scope.getData = function() {
                var test1 = this.gene;
                var test = $scope.geneStatus;
                console.log(test);

            };
            function parseMutationString(mutationStr) {
                mutationStr = mutationStr.replace(/\([^\)]+\)/g, '');
                var parts = mutationStr.split(',');
                var altResults = [];
                var proteinChange = '';
                var displayName = '';
                for (var i = 0; i < parts.length; i++) {
                    if (parts[i].indexOf('[') === -1) {
                        proteinChange = parts[i];
                        displayName = parts[i];
                    } else {
                        var l = parts[i].indexOf('[');
                        var r = parts[i].indexOf(']');
                        proteinChange = parts[i].substring(0, l);
                        displayName = parts[i].substring(l + 1, r);
                    }

                    if (proteinChange.indexOf('/') === -1) {
                        altResults.push({
                            alteration: proteinChange,
                            name: displayName,
                            gene: {
                                hugoSymbol: $scope.gene.name.getText()
                            }
                        });
                    } else {
                        var tempRes = proteinChange.match(/([A-Z][0-9]+)(.*)/i);
                        var refs = tempRes[2].split('/');
                        for (var j = 0; j < refs.length; j++) {
                            altResults.push({
                                alteration: tempRes[1] + refs[j],
                                name: displayName,
                                gene: {
                                    hugoSymbol: $scope.gene.name.getText()
                                }
                            });
                        }
                    }
                }
                return altResults;
            }

            $rootScope.reviewMode = false;
            $scope.displayCheck = function(uuid, reviewObj, mutationReview, tumorReview, treatmentReview) {
                // regular mode check
                if (!$rootScope.reviewMode) {
                    if (mutationReview && mutationReview.get('removed') || tumorReview && tumorReview.get('removed') || treatmentReview && treatmentReview.get('removed')) {
                        return false;
                    }
                    return true;
                }
                // review mode check
                if (mutationReview && mutationReview.get('removed') || tumorReview && tumorReview.get('removed') || treatmentReview && treatmentReview.get('removed')) {
                    return true;
                } else if (uuid !== null && checkReview(uuid)) {
                    return true;
                }
                return reviewObj.get('review') || reviewObj.get('action') || reviewObj.get('rollback');
            };
            $scope.signatureCheck = function(reviewObj, mutationReview, tumorReview, treatmentReview) {
                if (!$rootScope.reviewMode) {
                    return false;
                } else if (mutationReview && mutationReview.get('removed') || tumorReview && tumorReview.get('removed') || treatmentReview && treatmentReview.get('removed')) {
                    return false;
                } else if (reviewObj.get('action')) {
                    return false;
                }
                return true;
            };
            $scope.iconClass = function(type, reviewObj) {
                if (!reviewObj.get('action')) {
                    if (type === 'accept') {
                        return 'fa-comments-red';
                    }
                    if (type === 'reject') {
                        return 'fa-comments-grey';
                    }
                } else if (type === 'accept' && reviewObj.get('action') === 'accepted' || type === 'reject' && reviewObj.get('action') === 'rejected') {
                    return 'reviewed';
                }
            };
            $scope.iconExist = function(type, reviewObj, nameReview) {
                if (nameReview && nameReview.get('removed')) {
                    return false;
                }
                if (type === 'accept') {
                    return $rootScope.reviewMode && reviewObj.get('action') !== 'rejected' && !reviewObj.get('rollback');
                } else if (type === 'reject') {
                    return $rootScope.reviewMode && reviewObj.get('action') !== 'accepted' && !reviewObj.get('rollback');
                }
            };
            function resetReview(reviewObj) {
                reviewObj.delete('review');
                reviewObj.delete('action');
                reviewObj.delete('rollback');
            }

            $scope.review = function() {
                if ($rootScope.reviewMode) {
                    $scope.exitReview();
                } else {
                    var collaborators = $scope.realtimeDocument.getCollaborators();
                    var otherCollaborators = {};
                    _.each(collaborators, function(collaborator) {
                        if (collaborator.displayName !== User.name) {
                            otherCollaborators[collaborator.displayName] = '';
                        }
                    });
                    if (Object.keys(otherCollaborators).length > 0) {
                        var dlg = dialogs.confirm('Reminder', Object.keys(otherCollaborators).join(', ') + ' are currently working on this gene document. Enter review mode will disable them from editing.');
                        dlg.result.then(function() {
                            prepareReviewItems();
                        });
                    } else {
                        prepareReviewItems();
                    }
                }
            };
            $scope.exitReview = function() {
                var currentReviewer = $scope.realtimeDocument.getModel().createString('');
                $scope.gene.name_review.set('currentReviewer', currentReviewer);
                $rootScope.reviewMode = false;
                $scope.fileEditable = true;
                myUpdatedEvidenceModels = [];
                $interval.cancel($scope.reviewMoeInterval);
            };
            $scope.developerCheck = function() {
                var developers = ['Hongxin Zhang', 'Jianjiong Gao', 'Jiaojiao Wang'];
                if (developers.indexOf(Users.getMe().name) !== -1) {
                    return true;
                }
                return false;
            };
            $scope.geneMainDivStyle = {
                opacity: '1'
            };
            function checkReview(uuid) {
                uuid = uuid.getText();
                if ($rootScope.reviewMeta.get(uuid) && $rootScope.reviewMeta.get(uuid).get('review')) {
                    return true;
                }
                return false;
            }

            function setReview(uuid, flag) {
                uuid = uuid.getText();
                if (flag) {
                    if ($rootScope.reviewMeta.get(uuid)) {
                        $rootScope.reviewMeta.get(uuid).set('review', true);
                    } else {
                        var temp = $rootScope.metaModel.createMap();
                        temp.set('review', true);
                        $rootScope.reviewMeta.set(uuid, temp);
                    }
                } else if (!flag) {
                    if ($rootScope.reviewMeta.get(uuid)) {
                        $rootScope.reviewMeta.get(uuid).set('review', false);
                    }
                }
            }

            function setOriginalStatus(reviewObjs) {
                for (var i = 0; i < reviewObjs.length; i++) {
                    var reviewObj = reviewObjs[i];
                    resetReview(reviewObj, true);
                }
            }

            function mostRecentItem(reviewObjs) {
                var mostRecent = -1;
                for (var i = 0; i < reviewObjs.length; i++) {
                    if (mostRecent < 0) {
                        if (reviewObjs[i].get('updateTime')) {
                            mostRecent = i;
                        }
                    } else if (Date.parse(reviewObjs[mostRecent].get('updateTime') > Date.parse(reviewObjs[i].get('updateTime')))) {
                        mostRecent = i;
                    }
                }
                if (mostRecent < 0) {
                    return 0;
                }
                return mostRecent;
            }

            function setUpdatedSignature(tempArr, reviewObj) {
                var mostRecent = mostRecentItem(tempArr);
                reviewObj.set('updatedBy', tempArr[mostRecent].get('updatedBy'));
                reviewObj.set('updateTime', tempArr[mostRecent].get('updateTime'));
                if(reviewObj.get('updatedBy') === User.name) {
                    return true;
                }else{
                    return false;
                }
            }
            var myUpdatedEvidences = {};
            var myDeletedEvidences = [];
            var myGeneTypeEvidence = {};
            var myUpdatedEvidenceModels = [];
            var myDeletedEvidenceIndice = [];
            function formMyEvidences(type, mutation, tumor, TI, treatment) {
                var evidenceResult = formEvidenceItem(type, mutation, tumor, TI, treatment);
                var dataUUID = evidenceResult[0];
                var data = evidenceResult[1];
                var extraDataUUID = evidenceResult[2];
                var extraData = evidenceResult[3];
                if(type === 'Clinical effect') {
                    if(dataUUID.length > 0) {
                        myUpdatedEvidences[dataUUID] = data;
                        myUpdatedEvidenceModels.push(['ONCOGENIC', mutation, tumor, TI, treatment]);
                    }
                    if(extraDataUUID.length > 0) {
                        myUpdatedEvidences[extraDataUUID] = extraData;
                        myUpdatedEvidenceModels.push(['MUTATION_SUMMARY', mutation, tumor, TI, treatment]);
                    }
                } else {
                    if(dataUUID.length > 0) {
                        myUpdatedEvidences[dataUUID] = data;
                        myUpdatedEvidenceModels.push([type, mutation, tumor, TI, treatment]);
                    }
                }
            }
            function prepareReviewItems() {
                $rootScope.reviewMode = true;
                var currentReviewer = $scope.realtimeDocument.getModel().createString(User.name);
                $scope.gene.name_review.set('currentReviewer', currentReviewer);
                $scope.allMyChanges = false;
                myUpdatedEvidences = {};
                myDeletedEvidences = [];
                myGeneTypeEvidence = {};
                myUpdatedEvidenceModels = [];
                setOriginalStatus([$scope.gene.summary_review, $scope.gene.type_review, $scope.gene.background_review]);
                if($scope.gene.summary_review.get('updatedBy') === User.name) {
                    formMyEvidences('GENE_SUMMARY', null, null, null, null);
                }
                if($scope.gene.background_review.get('updatedBy') === User.name) {
                    formMyEvidences('GENE_BACKGROUND', null, null, null, null);
                }
                if($scope.gene.type_review.get('updatedBy') === User.name) {
                    myGeneTypeEvidence = {
                        hugoSymbol: $scope.gene.name.getText(),
                        oncogene: $scope.gene.type.get('OCG').trim().length > 0,
                        tsg: $scope.gene.type.get('TSG').trim().length > 0
                    };
                }
                var mutationChanged = false;
                var tumorChanged = false;
                var treatmentChanged = false;
                var tempArr = [];
                for (var i = 0; i < $scope.gene.mutations.length; i++) {
                    var mutation = $scope.gene.mutations.get(i);
                    if (mutation.name_review.get('removed')) {
                        myDeletedEvidences = collectUUIDs('mutation', mutation, myDeletedEvidences);
                        continue;
                    }
                    tempArr = [mutation.oncogenic_review, mutation.shortSummary_review, mutation.summary_review];
                    setOriginalStatus(tempArr);
                    if (checkReview(mutation.shortSummary_uuid) || checkReview(mutation.summary_uuid) || checkReview(mutation.oncogenic_uuid)) {
                        mutation.oncogenic_review.set('review', true);
                        if(setUpdatedSignature(tempArr, mutation.oncogenic_review)) {
                            formMyEvidences('Clinical effect', mutation, null, null, null);
                        }
                        mutationChanged = true;
                    }
                    tempArr = [mutation.effect_review, mutation.short_review, mutation.description_review];
                    setOriginalStatus(tempArr);
                    if (checkReview(mutation.short_uuid) || checkReview(mutation.description_uuid) || checkReview(mutation.effect_uuid)) {
                        mutation.effect_review.set('review', true);
                        mutation.effect_review.set('mutation_effect', true);
                        if(setUpdatedSignature(tempArr, mutation.effect_review)) {
                            formMyEvidences('MUTATION_EFFECT', mutation, null, null, null);
                        }
                        mutationChanged = true;
                    }
                    for (var j = 0; j < mutation.tumors.length; j++) {
                        var tumor = mutation.tumors.get(j);
                        if (tumor.name_review.get('removed')) {
                            mutationChanged = true;
                            myDeletedEvidences = collectUUIDs('tumor', tumor, myDeletedEvidences);
                            continue;
                        }
                        tempArr = [tumor.shortPrevalence_review, tumor.prevalence_review];
                        setOriginalStatus(tempArr);
                        if (checkReview(tumor.shortPrevalence_uuid) || checkReview(tumor.prevalence_uuid)) {
                            tumor.prevalence_review.set('review', true);
                            if(setUpdatedSignature(tempArr, tumor.prevalence_review)) {
                                formMyEvidences('PREVALENCE', mutation, tumor, null, null);
                            }
                            tumorChanged = true;
                        }
                        tempArr = [tumor.shortProgImp_review, tumor.progImp_review];
                        setOriginalStatus(tempArr);
                        if (checkReview(tumor.shortProgImp_uuid) || checkReview(tumor.progImp_uuid)) {
                            tumor.progImp_review.set('review', true);
                            if(setUpdatedSignature(tempArr, tumor.progImp_review)) {
                                formMyEvidences('PROGNOSTIC_IMPLICATION', mutation, tumor, null, null);
                            }
                            tumorChanged = true;
                        }
                        tempArr = [tumor.nccn_review, tumor.nccn.therapy_review, tumor.nccn.disease_review, tumor.nccn.version_review, tumor.nccn.description_review, tumor.nccn.short_review];
                        setOriginalStatus(tempArr);
                        if (checkReview(tumor.nccn.therapy_uuid) || checkReview(tumor.nccn.disease_uuid) || checkReview(tumor.nccn.version_uuid) || checkReview(tumor.nccn.description_uuid) || checkReview(tumor.nccn.short_uuid)) {
                            tumor.nccn_review.set('review', true);
                            if(setUpdatedSignature(tempArr, tumor.nccn_review)) {
                                formMyEvidences('NCCN_GUIDELINES', mutation, tumor, null, null);
                            }
                            tumorChanged = true;
                        }
                        for (var k = 0; k < tumor.TI.length; k++) {
                            var ti = tumor.TI.get(k);
                            for (var m = 0; m < ti.treatments.length; m++) {
                                var treatment = ti.treatments.get(m);
                                if (treatment.name_review.get('removed')) {
                                    treatmentChanged = true;
                                    myDeletedEvidences = collectUUIDs('treatment', treatment, myDeletedEvidences);
                                    continue;
                                }
                                tempArr = [treatment.name_review, treatment.level_review, treatment.indication_review, treatment.description_review, treatment.short_review];
                                setOriginalStatus(tempArr);
                                if (checkReview(treatment.name_uuid) || checkReview(treatment.level_uuid) || checkReview(treatment.indication_uuid) || checkReview(treatment.description_uuid) || checkReview(treatment.short_uuid)) {
                                    treatment.name_review.set('review', true);
                                    if(setUpdatedSignature(tempArr, treatment.name_review)) {
                                        formMyEvidences(ti.name.getText(), mutation, tumor, ti, treatment);
                                    }
                                    treatmentChanged = true;
                                }
                            }
                            setOriginalStatus([ti.name_review, ti.description_review]);
                            if (checkReview(ti.description_uuid) || treatmentChanged) {
                                ti.name_review.set('review', true);
                                if(ti.description_review.get('updatedBy') === User.name) {
                                    formMyEvidences(ti.name.getText(), mutation, tumor, ti, null);
                                }
                                tumorChanged = true;
                            }
                            treatmentChanged = false;
                        }
                        setOriginalStatus([tumor.name_review, tumor.summary_review, tumor.trials_review]);
                        if (tumorChanged || checkReview(tumor.summary_uuid) || checkReview(tumor.trials_uuid)) {
                            tumor.name_review.set('review', true);
                            if(tumor.trials_review.get('updatedBy') === User.name) {
                                formMyEvidences('CLINICAL_TRIAL', mutation, tumor, null, null);
                            }
                            if(tumor.summary_review.get('updatedBy') === User.name) {
                                formMyEvidences('TUMOR_TYPE_SUMMARY', mutation, tumor, null, null);
                            }
                            mutationChanged = true;
                        }
                        tumorChanged = false;
                    }
                    setOriginalStatus([mutation.name_review]);
                    if (mutationChanged) {
                        mutation.name_review.set('review', true);
                    }
                    mutationChanged = false;
                }
                if(_.isEmpty(myGeneTypeEvidence) && _.isEmpty(myUpdatedEvidences) && _.isEmpty(myDeletedEvidences)) {
                    $scope.allMyChanges = true;
                }
            }
            var apiCallCount = 0;
            function doneSaving() {
                if(apiCallCount === 1) {
                    $scope.$emit('doneSaveDataToDatabase');
                    $scope.allMyChanges = true;
                    myUpdatedEvidences = {};
                    myDeletedEvidences = [];
                    myGeneTypeEvidence = {};
                    myUpdatedEvidenceModels = [];
                    myDeletedEvidenceIndice = [];
                }else {
                    apiCallCount--;
                }
            }
            $scope.acceptAllMyChanges = function() {
                if ($scope.status.isDesiredGene) {
                    $scope.$emit('startSaveDataToDatabase');
                }
                if(_.isEmpty(myGeneTypeEvidence) && _.isEmpty(myUpdatedEvidences) && !_.isEmpty(myDeletedEvidences)) {
                    apiCallCount = 1;
                    evidenceDeleteUpdate(doneSaving);
                } else if(_.isEmpty(myGeneTypeEvidence) && !_.isEmpty(myUpdatedEvidences) && _.isEmpty(myDeletedEvidences)) {
                    apiCallCount = 1;
                    evidenceBatchUpdate(doneSaving);
                } else if(_.isEmpty(myGeneTypeEvidence) && !_.isEmpty(myUpdatedEvidences) && !_.isEmpty(myDeletedEvidences)) {
                    apiCallCount = 2;
                    evidenceBatchUpdate(doneSaving);
                    evidenceDeleteUpdate(doneSaving);
                } else if(!_.isEmpty(myGeneTypeEvidence) && _.isEmpty(myUpdatedEvidences) && _.isEmpty(myDeletedEvidences)) {
                    apiCallCount = 1;
                    geneTypeUpdate(doneSaving);
                } else if(!_.isEmpty(myGeneTypeEvidence) && _.isEmpty(myUpdatedEvidences) && !_.isEmpty(myDeletedEvidences)) {
                    apiCallCount = 2;
                    geneTypeUpdate(doneSaving);
                    evidenceDeleteUpdate(doneSaving);
                } else if(!_.isEmpty(myGeneTypeEvidence) && !_.isEmpty(myUpdatedEvidences) && _.isEmpty(myDeletedEvidences)) {
                    apiCallCount = 2;
                    geneTypeUpdate(doneSaving);
                    evidenceBatchUpdate(doneSaving);
                } else if(!_.isEmpty(myGeneTypeEvidence) && !_.isEmpty(myUpdatedEvidences) && !_.isEmpty(myDeletedEvidences)) {
                    apiCallCount = 3;
                    geneTypeUpdate(doneSaving);
                    evidenceBatchUpdate(doneSaving);
                    evidenceDeleteUpdate(doneSaving);
                }
            }
            function geneTypeUpdate(callback) {
                if($scope.status.isDesiredGene) {
                    DatabaseConnector.updateGeneType($scope.gene.name.getText(), myGeneTypeEvidence, function(result) {
                        geneModelUpdate('GENE_TYPE', null, null, null, null);
                        callback();
                    }, function(error) {
                        console.log('fail to update to database', error);
                        dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                        $scope.$emit('doneSaveDataToDatabase');
                    });
                }else {
                    geneModelUpdate('GENE_TYPE', null, null, null, null);
                }
            }
            function evidenceBatchUpdate(callback) {
                if($scope.status.isDesiredGene) {
                    DatabaseConnector.updateEvidenceBatch(myUpdatedEvidences, function(result) {
                        for(var i = 0; i < myUpdatedEvidenceModels.length; i++) {
                            geneModelUpdate(myUpdatedEvidenceModels[i][0], myUpdatedEvidenceModels[i][1], myUpdatedEvidenceModels[i][2], myUpdatedEvidenceModels[i][3], myUpdatedEvidenceModels[i][4]);
                        }
                        myUpdatedEvidenceModels = [];
                        callback();
                    }, function(error) {
                        console.log('fail to update to database', error);
                        dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                        $scope.$emit('doneSaveDataToDatabase');
                    });
                }else {
                    for(var i = 0; i < myUpdatedEvidenceModels.length; i++) {
                        geneModelUpdate(myUpdatedEvidenceModels[i][0], myUpdatedEvidenceModels[i][1], myUpdatedEvidenceModels[i][2], myUpdatedEvidenceModels[i][3], myUpdatedEvidenceModels[i][4]);
                    }
                    myUpdatedEvidenceModels = [];
                }

            }
            function evidenceDeleteUpdate(callback) {
                if($scope.status.isDesiredGene) {
                    DatabaseConnector.deleteEvidences(myDeletedEvidences, function(result) {
                        for(var i = 0; i < myDeletedEvidenceIndice.length; i++) {
                            removeInModel(myDeletedEvidenceIndice[i][0], myDeletedEvidenceIndice[i][1], myDeletedEvidenceIndice[i][2], myDeletedEvidenceIndice[i][3]);
                        }
                        for(var i = 0; i < myDeletedEvidences.length; i++) {
                            $rootScope.reviewMeta.delete(myDeletedEvidences[i]);
                        }
                        callback();
                    }, function(error) {
                        console.log('fail to update to database', error);
                        dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                        $scope.$emit('doneSaveDataToDatabase');
                    });
                }else {
                    for(var i = 0; i < myDeletedEvidenceIndice.length; i++) {
                        removeInModel(myDeletedEvidenceIndice[i][0], myDeletedEvidenceIndice[i][1], myDeletedEvidenceIndice[i][2], myDeletedEvidenceIndice[i][3]);
                    }
                    for(var i = 0; i < myDeletedEvidences.length; i++) {
                        $rootScope.reviewMeta.delete(myDeletedEvidences[i]);
                    }
                }
            }
            function formEvidenceItem(type, mutation, tumor, TI, treatment) {
                var dataUUID = '';
                var extraDataUUID = '';
                var data = {
                    additionalInfo: null,
                    alterations: null,
                    cancerType: null,
                    description: null,
                    evidenceType: type,
                    gene: {
                        hugoSymbol: $scope.gene.name.getText()
                    },
                    knownEffect: null,
                    lastEdit: new Date().getTime().toString(),
                    levelOfEvidence: null,
                    subtype: null,
                    articles: [],
                    clinicalTrials: [],
                    nccnGuidelines: null,
                    treatments: null
                };
                var levelMapping = {
                    '0': 'LEVEL_0',
                    '1': 'LEVEL_1',
                    '2A': 'LEVEL_2A',
                    '2B': 'LEVEL_2B',
                    '3A': 'LEVEL_3A',
                    '3B': 'LEVEL_3B',
                    '4': 'LEVEL_4',
                    'R1': 'LEVEL_R1',
                    'R2': 'LEVEL_R2',
                    'R3': 'LEVEL_R3'
                };
                var secondType = '';
                var extraData = _.clone(data);
                var i = 0;

                switch (type) {
                case 'GENE_SUMMARY':
                    dataUUID = $scope.gene.summary_uuid.getText();
                    data.description = $scope.gene.summary.getText();
                    break;
                case 'GENE_BACKGROUND':
                    dataUUID = $scope.gene.background_uuid.getText();
                    data.description = $scope.gene.background.getText();
                    break;
                case 'Clinical effect':
                    dataUUID = mutation.oncogenic_uuid.getText();
                    data.knownEffect = mutation.oncogenic.getText();
                    data.description = mutation.summary.getText();
                    data.evidenceType = 'ONCOGENIC';
                    if (checkReview(mutation.shortSummary_uuid)) {
                        extraDataUUID = mutation.shortSummary_uuid.getText();
                        extraData.description = mutation.shortSummary.getText();
                        extraData.evidenceType = 'MUTATION_SUMMARY';
                        extraData.alterations = parseMutationString(mutation.name.getText());
                        secondType = 'MUTATION_SUMMARY';
                    }
                    break;
                case 'MUTATION_EFFECT':
                    dataUUID = mutation.effect_uuid.getText();
                    data.knownEffect = mutation.effect.value.getText();
                    data.description = mutation.description.getText();
                    break;
                case 'TUMOR_TYPE_SUMMARY':
                    dataUUID = tumor.summary_uuid.getText();
                    data.description = tumor.summary.getText();
                    break;
                case 'PREVALENCE':
                    dataUUID = tumor.prevalence_uuid.getText();
                    data.description = tumor.prevalence.getText();
                    break;
                case 'PROGNOSTIC_IMPLICATION':
                    dataUUID = tumor.progImp_uuid.getText();
                    data.description = tumor.progImp.getText();
                    break;
                case 'NCCN_GUIDELINES':
                    dataUUID = tumor.nccn_uuid.getText();
                    data.description = tumor.nccn.description.getText();
                    data.nccnGuidelines = [
                        {
                            category: '',
                            description: tumor.nccn.description.getText(),
                            disease: tumor.nccn.disease.getText(),
                            pages: '',
                            version: tumor.nccn.version.getText()
                        }
                    ];
                    break;
                case 'Standard implications for sensitivity to therapy':
                    data.evidenceType = 'STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY';
                    data.knownEffect = 'Sensitive';
                    break;
                case 'Standard implications for resistance to therapy':
                    data.evidenceType = 'STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE';
                    data.knownEffect = 'Resistant';
                    break;
                case 'Investigational implications for sensitivity to therapy':
                    data.evidenceType = 'INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY';
                    data.knownEffect = 'Sensitive';
                    break;
                case 'Investigational implications for resistance to therapy':
                    data.evidenceType = 'INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE';
                    data.knownEffect = 'Resistant';
                    break;
                case 'CLINICAL_TRIAL':
                    dataUUID = tumor.trials_uuid.getText();
                    for (i = 0; i < tumor.trials.length; i++) {
                        data.clinicalTrials.push({
                            nctId: tumor.trials.get(i)
                        });
                    }
                    break;
                default:
                    break;
                }

                if (mutation !== null) {
                    data.alterations = parseMutationString(mutation.name.getText());
                }
                if (tumor !== null) {
                    data.subtype = tumor.cancerTypes.asArray()[0].oncoTreeCode.getText();
                    data.cancerType = tumor.cancerTypes.asArray()[0].cancerType.getText();
                }
                if (TI !== null) {
                    if (treatment === null) {
                        dataUUID = TI.description_uuid.getText();
                        data.description = TI.description.getText();
                    } else {
                        dataUUID = treatment.name_uuid.getText();
                        data.levelOfEvidence = levelMapping[treatment.level.getText()];
                        data.description = treatment.description.getText();
                        data.treatments = [];
                        var treatments = treatment.name.getText().split('+');
                        for (i = 0; i < treatments.length; i++) {
                            var drugs = treatments[i].split(',');
                            var drugList = [];
                            for (var j = 0; j < drugs.length; j++) {
                                drugList.push({
                                    drugName: drugs[j]
                                });
                            }
                            data.treatments.push({
                                approvedIndications: [treatment.indication.getText()],
                                drugs: drugList
                            });
                        }
                    }
                }
                if(!_.isNull(data.description)) {
                    data.description = stringUtils.getTextString(data.description);
                    var abstractResults = FindRegex.result(data.description);
                    var tempAbstract;
                    for(var i = 0; i < abstractResults.length; i++) {
                        tempAbstract = abstractResults[i];
                        switch(tempAbstract.type) {
                        case 'pmid':
                            data.articles.push({
                                pmid: tempAbstract.id
                            });
                            break;
                        case 'abstract':
                            data.articles.push({
                                abstract: tempAbstract.id,
                                link: tempAbstract.link
                            });
                            break;
                        case 'nct':
                            data.clinicalTrials.push({
                                nctId: tempAbstract.id
                            });
                            break;
                        }
                    }
                }
                return [dataUUID, data, extraDataUUID, extraData];
            }
            $scope.accept = function(event, type, mutation, tumor, TI, treatment, reviewObj) {
                if (event !== null) {
                    $scope.stopCollopse(event);
                }
                if (reviewObj.get('action')) {
                    return;
                }
                var evidenceResult = formEvidenceItem(type, mutation, tumor, TI, treatment);
                var dataUUID = evidenceResult[0];
                var data = evidenceResult[1];
                var extraDataUUID = evidenceResult[2];
                var extraData = evidenceResult[3];

                if (type === 'GENE_TYPE') {
                    var params = {
                        hugoSymbol: $scope.gene.name.getText(),
                        oncogene: $scope.gene.type.get('OCG').trim().length > 0,
                        tsg: $scope.gene.type.get('TSG').trim().length > 0
                    };
                    if ($scope.status.isDesiredGene) {
                        $scope.$emit('startSaveDataToDatabase');
                        DatabaseConnector.updateGeneType($scope.gene.name.getText(), params, function(result) {
                            geneModelUpdate(type, mutation, tumor, TI, treatment);
                            $scope.$emit('doneSaveDataToDatabase');
                        }, function(error) {
                            console.log('fail to update to database', error);
                            dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                            $scope.$emit('doneSaveDataToDatabase');
                        });
                    } else {
                        geneModelUpdate(type, mutation, tumor, TI, treatment);
                    }
                } else if (dataUUID.length > 0) {
                    if ($scope.status.isDesiredGene) {
                        $scope.$emit('startSaveDataToDatabase');
                        DatabaseConnector.updateEvidence(dataUUID, data, function(result) {
                            geneModelUpdate(type, mutation, tumor, TI, treatment);
                            if (extraDataUUID.length > 0) {
                                DatabaseConnector.updateEvidence(extraDataUUID, extraData, function(result) {
                                    geneModelUpdate(secondType, mutation, tumor, TI, treatment);
                                    $scope.$emit('doneSaveDataToDatabase');
                                }, function(error) {
                                    console.log('fail to update to database', error);
                                    dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                                    $scope.$emit('doneSaveDataToDatabase');
                                });
                            } else {
                                $scope.$emit('doneSaveDataToDatabase');
                            }
                        }, function(error) {
                            console.log('fail to update to database', error);
                            dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                            $scope.$emit('doneSaveDataToDatabase');
                        });
                    } else {
                        geneModelUpdate(type, mutation, tumor, TI, treatment);
                        if (extraDataUUID.length > 0) {
                            geneModelUpdate(secondType, mutation, tumor, TI, treatment);
                        }
                    }
                }
            };

            function setReviewModeInterval() {
                $interval.cancel($scope.reviewMoeInterval);
                $scope.reviewMoeInterval = $interval(function() {
                    if ($rootScope.reviewMode) {
                        $scope.review();
                        $interval.cancel($scope.reviewMoeInterval);
                    }
                }, 1000 * 60 * 15);
            }

            function acceptItem(uuid, reviewObj) {
                reviewObj.set('action', 'accepted');
                setReview(uuid, false);
                delete myUpdatedEvidences[uuid];
                reviewObj.delete('lastReviewed');
                reviewObj.delete('updatedBy');
                reviewObj.delete('updateTime');
            }

            function geneModelUpdate(type, mutation, tumor, TI, treatment) {
                switch (type) {
                case 'GENE_SUMMARY':
                    acceptItem($scope.gene.summary_uuid, $scope.gene.summary_review);
                    break;
                case 'GENE_BACKGROUND':
                    acceptItem($scope.gene.background_uuid, $scope.gene.background_review);
                    break;
                case 'GENE_TYPE':
                    acceptItem($scope.gene.type_uuid, $scope.gene.type_review);
                    break;
                case 'ONCOGENIC':
                    acceptItem(mutation.oncogenic_uuid, mutation.oncogenic_review);
                    acceptItem(mutation.summary_uuid, mutation.summary_review);
                    break;
                case 'MUTATION_SUMMARY':
                    acceptItem(mutation.shortSummary_uuid, mutation.shortSummary_review);
                    break;
                case 'MUTATION_EFFECT':
                    acceptItem(mutation.effect_uuid, mutation.effect_review);
                    acceptItem(mutation.description_uuid, mutation.description_review);
                    acceptItem(mutation.short_uuid, mutation.short_review);
                    break;
                case 'TUMOR_TYPE_SUMMARY':
                    acceptItem(tumor.summary_uuid, tumor.summary_review);
                    break;
                case 'PREVALENCE':
                    acceptItem(tumor.prevalence_uuid, tumor.prevalence_review);
                    acceptItem(tumor.shortPrevalence_uuid, tumor.shortPrevalence_review);
                    break;
                case 'PROGNOSTIC_IMPLICATION':
                    acceptItem(tumor.progImp_uuid, tumor.progImp_review, tumor.progImp, true);
                    acceptItem(tumor.shortProgImp_uuid, tumor.shortProgImp_review, tumor.shortProgImp, false);
                    break;
                case 'NCCN_GUIDELINES':
                    acceptItem(tumor.nccn_uuid, tumor.nccn_review);
                    acceptItem(tumor.nccn.therapy_uuid, tumor.nccn.therapy_review);
                    acceptItem(tumor.nccn.disease_uuid, tumor.nccn.disease_review);
                    acceptItem(tumor.nccn.version_uuid, tumor.nccn.version_review);
                    acceptItem(tumor.nccn.description_uuid, tumor.nccn.description_review);
                    acceptItem(tumor.nccn.short_uuid, tumor.nccn.short_review);
                    break;
                case 'Standard implications for sensitivity to therapy':
                case 'Standard implications for resistance to therapy':
                case 'Investigational implications for sensitivity to therapy':
                case 'Investigational implications for resistance to therapy':
                    if (treatment === null) {
                        acceptItem(TI.description_uuid, TI.description_review);
                    } else {
                        acceptItem(treatment.name_uuid, treatment.name_review);
                        acceptItem(treatment.level_uuid, treatment.level_review);
                        acceptItem(treatment.indication_uuid, treatment.indication_review);
                        acceptItem(treatment.description_uuid, treatment.description_review);
                        acceptItem(treatment.short_uuid, treatment.short_review);
                    }
                    break;
                case 'CLINICAL_TRIAL':
                    acceptItem(tumor.trials_uuid, tumor.trials_review);
                    break;
                default:
                    break;
                }
            }

            function rejectItem(uuid, reviewObj, obj, content) {
                reviewObj.set('action', 'rejected');
                setReview(uuid, false);
                delete myUpdatedEvidences[uuid];
                if (!content && reviewObj.has('lastReviewed')) {
                    obj.setText(reviewObj.get('lastReviewed'));
                } else if (content === 'trials') {
                    obj.clear();
                    obj.pushAll(reviewObj.get('lastReviewed'));
                } else if (content === 'type') {
                    var lastReviewedContent = reviewObj.get('lastReviewed');
                    obj.set('TSG', lastReviewedContent.TSG);
                    obj.set('OCG', lastReviewedContent.OCG);
                }
                reviewObj.delete('lastReviewed');
                reviewObj.delete('updatedBy');
                reviewObj.delete('updateTime');
            }

            $scope.reject = function(event, type, mutation, tumor, TI, treatment, reviewObj) {
                if (event !== null) {
                    $scope.stopCollopse(event);
                }
                if (reviewObj.get('action')) {
                    return;
                }
                var dlg = dialogs.confirm('Reminder', 'Are you sure you want to reject this change?');
                dlg.result.then(function() {
                    switch (type) {
                    case 'GENE_SUMMARY':
                        rejectItem($scope.gene.summary_uuid, $scope.gene.summary_review, $scope.gene.summary);
                        break;
                    case 'GENE_BACKGROUND':
                        rejectItem($scope.gene.background_uuid, $scope.gene.background_review, $scope.gene.background);
                        break;
                    case 'GENE_TYPE':
                        rejectItem($scope.gene.type_uuid, $scope.gene.type_review, $scope.gene.type, 'type');
                        break;
                    case 'Clinical effect':
                        rejectItem(mutation.oncogenic_uuid, mutation.oncogenic_review, mutation.oncogenic);
                        rejectItem(mutation.summary_uuid, mutation.summary_review, mutation.summary);
                        rejectItem(mutation.shortSummary_uuid, mutation.shortSummary_review, mutation.shortSummary);
                        break;
                    case 'MUTATION_EFFECT':
                        rejectItem(mutation.effect_uuid, mutation.effect_review, mutation.effect.value);
                        rejectItem(mutation.description_uuid, mutation.description_review, mutation.description);
                        rejectItem(mutation.short_uuid, mutation.short_review, mutation.short);
                        break;
                    case 'TUMOR_TYPE_SUMMARY':
                        rejectItem(tumor.summary_uuid, tumor.summary_review, tumor.summary);
                        break;
                    case 'PREVALENCE':
                        rejectItem(tumor.prevalence_uuid, tumor.prevalence_review, tumor.prevalence);
                        rejectItem(tumor.shortPrevalence_uuid, tumor.shortPrevalence_review, tumor.shortPrevalence);
                        break;
                    case 'PROGNOSTIC_IMPLICATION':
                        rejectItem(tumor.progImp_uuid, tumor.progImp_review, tumor.progImp);
                        rejectItem(tumor.shortProgImp_uuid, tumor.shortProgImp_review, tumor.shortProgImp);
                        break;
                    case 'NCCN_GUIDELINES':
                        rejectItem(tumor.nccn_uuid, tumor.nccn_review, null, true);
                        rejectItem(tumor.nccn.therapy_uuid, tumor.nccn.therapy_review, tumor.nccn.therapy);
                        rejectItem(tumor.nccn.disease_uuid, tumor.nccn.disease_review, tumor.nccn.disease);
                        rejectItem(tumor.nccn.version_uuid, tumor.nccn.version_review, tumor.nccn.version);
                        rejectItem(tumor.nccn.description_uuid, tumor.nccn.description_review, tumor.nccn.description);
                        rejectItem(tumor.nccn.short_uuid, tumor.nccn.short_review, tumor.nccn.short);
                        break;
                    case 'Standard implications for sensitivity to therapy':
                    case 'Standard implications for resistance to therapy':
                    case 'Investigational implications for sensitivity to therapy':
                    case 'Investigational implications for resistance to therapy':
                        if (treatment === null) {
                            rejectItem(TI.description_uuid, TI.description_review, TI.description, true);
                        } else {
                            rejectItem(treatment.name_uuid, treatment.name_review, null, true, true);
                            rejectItem(treatment.level_uuid, treatment.level_review, treatment.level);
                            rejectItem(treatment.indication_uuid, treatment.indication_review, treatment.indication);
                            rejectItem(treatment.description_uuid, treatment.description_review, treatment.description);
                            rejectItem(treatment.short_uuid, treatment.short_review, treatment.short);
                        }
                        break;
                    case 'CLINICAL_TRIAL':
                        rejectItem(tumor.trials_uuid, tumor.trials_review, tumor.trials, 'trials');
                        break;
                    default:
                        break;
                    }
                });
            };

            $scope.updateGene = function() {
                $scope.docStatus.savedGene = false;

                var gene = stringUtils.getGeneData(this.gene, true, true, true, false);
                var vus = stringUtils.getVUSFullData(this.vus, true);
                var params = {};

                if (gene) {
                    params.gene = JSON.stringify(gene);
                }
                if (vus) {
                    params.vus = JSON.stringify(vus);
                }

                DatabaseConnector.updateGene(params, function(result) {
                    $scope.docStatus.savedGene = true;
                    DatabaseConnector
                        .updateGeneCache($scope.gene.name.getText(),
                            function() {
                                console.log('success', result);
                            },
                            function() {
                                console.log('error', result);
                                var errorMessage = 'An error has occurred ' +
                                    'when updating gene cache: ' +
                                    $scope.gene.name.getText();

                                $rootScope.$emit('oncokbError',
                                    {
                                        message: errorMessage,
                                        reason: JSON.stringify(result)
                                    });
                            });
                    changeLastUpdate();
                }, function(result) {
                    $scope.docStatus.savedGene = true;
                    var errorMessage = 'An error has occurred when saving ' +
                        'data, please contact the developer.';

                    // dialogs.error('Error', errorMessage);
                    $rootScope.$emit('oncokbError',
                        {
                            message: 'An error has occurred when saving data. ' +
                            'Gene: ' + $scope.gene.name.getText(),
                            reason: JSON.stringify(result)
                        });
                    changeLastUpdate();
                });
            };

            function changeLastUpdate() {
                if ($scope.gene.status_timeStamp.has('lastUpdate')) {
                    $scope.gene.status_timeStamp.get('lastUpdate').value.setText(new Date().getTime().toString());
                    $scope.gene.status_timeStamp.get('lastUpdate').by.setText(Users.getMe().name);
                } else {
                    var timeStamp;
                    $scope.realtimeDocument.getModel().beginCompoundOperation();
                    timeStamp = $scope.realtimeDocument.getModel().create('TimeStamp');
                    timeStamp.value.setText(new Date().getTime().toString());
                    timeStamp.by.setText(Users.getMe().name);
                    $scope.gene.status_timeStamp.set('lastUpdate', timeStamp);
                    $scope.realtimeDocument.getModel().endCompoundOperation();
                }
                $scope.docStatus.updateGene = true;
            }

            $scope.addTumorType = function(mutation, mutationIndex) {
                var newTumorTypesName = getNewCancerTypesName($scope.meta.newCancerTypes);

                if (mutation && newTumorTypesName) {
                    var _tumorType = '';
                    var exists = false;
                    var model = $scope.realtimeDocument.getModel();

                    mutation.tumors.asArray().forEach(function(e) {
                        if ($scope.getCancerTypesName(e.cancerTypes).toLowerCase() === newTumorTypesName.toLowerCase()) {
                            exists = true;
                        }
                    });

                    if (exists) {
                        dialogs.notify('Warning', 'Tumor type exists.');
                    } else {
                        model.beginCompoundOperation();
                        _tumorType = model.create(OncoKB.Tumor);

                        _.each($scope.meta.newCancerTypes, function(ct) {
                            if (ct.mainType && ct.mainType.name) {
                                var cancerType = model.create(OncoKB.CancerType);
                                cancerType.cancerType.setText(ct.mainType.name);
                                if (ct.subtype) {
                                    if (ct.subtype.code) {
                                        cancerType.oncoTreeCode.setText(ct.subtype.code);
                                    }
                                    if (ct.subtype.name) {
                                        cancerType.subtype.setText(ct.subtype.name);
                                    }
                                }
                                cancerType.cancerType_eStatus.set('obsolete', 'false');
                                cancerType.subtype_eStatus.set('obsolete', 'false');
                                cancerType.oncoTreeCode_eStatus.set('obsolete', 'false');
                                _tumorType.cancerTypes.push(cancerType);
                            }
                        });
                        _tumorType.nccn.category.setText('2A');
                        for (var i = 0; i < 4; i++) {
                            var __ti = model.create(OncoKB.TI);
                            var __status = i < 2 ? 1 : 0; // 1: Standard, 0: Investigational
                            var __type = i % 2 === 0 ? 1 : 0; // 1: sensitivity, 0: resistance
                            var __name = (__status ? 'Standard' : 'Investigational') + ' implications for ' + (__type ? 'sensitivity' : 'resistance') + ' to therapy';

                            __ti.types.set('status', __status.toString());
                            __ti.types.set('type', __type.toString());
                            __ti.name.setText(__name);
                            _tumorType.TI.push(__ti);
                        }
                        mutation.tumors.push(_tumorType);
                        model.endCompoundOperation();
                        $scope.meta.newCancerTypes = [{
                            mainType: '',
                            subtype: '',
                            oncoTreeTumorTypes: angular.copy($scope.oncoTree.allTumorTypes)
                        }];
                        $scope.geneStatus[mutationIndex][mutation.tumors.length - 1] = new GeneStatusSingleton();
                    }
                }
            };

            $scope.modifyTumorType = function(tumorType) {
                var dlg = dialogs.create('views/modifyTumorTypes.html', 'ModifyTumorTypeCtrl', {
                    model: $scope.realtimeDocument.getModel(),
                    cancerTypes: tumorType.cancerTypes,
                    oncoTree: $scope.oncoTree
                }, {
                    size: 'lg'
                });
                dlg.result.then(function(name) {
                    $scope.name = name;
                }, function() {
                    $scope.name = 'You decided not to enter in your name, that makes me sad.';
                });
            };

            // Add new therapeutic implication
            $scope.addTI = function(ti, index, newTIName, mutationIndex, tumorIndex, tiIndex) {
                if (ti && newTIName) {
                    var _treatment = '';
                    var exists = false;
                    newTIName = newTIName.toString().trim();

                    ti.treatments.asArray().forEach(function(e) {
                        if (e.name.getText().toLowerCase() === newTIName.toLowerCase()) {
                            exists = true;
                        }
                    });

                    if (exists) {
                        dialogs.notify('Warning', 'Therapy exists.');
                    } else {
                        $scope.realtimeDocument.getModel().beginCompoundOperation();
                        _treatment = $scope.realtimeDocument.getModel().create(OncoKB.Treatment);
                        _treatment.name.setText(newTIName);
                        _treatment.type.setText('Therapy');
                        if ($scope.checkTI(ti, 1, 1)) {
                            _treatment.level.setText('1');
                        } else if ($scope.checkTI(ti, 0, 1)) {
                            _treatment.level.setText('4');
                        } else if ($scope.checkTI(ti, 1, 0)) {
                            _treatment.level.setText('1');
                        } else if ($scope.checkTI(ti, 0, 0)) {
                            _treatment.level.setText('4');
                        }
                        ti.treatments.push(_treatment);
                        $scope.realtimeDocument.getModel().endCompoundOperation();
                        $scope.geneStatus[mutationIndex][tumorIndex][tiIndex][ti.treatments.length - 1] = new GeneStatusSingleton();
                    }
                }
            };

            $scope.onFocus = function(e) {
                $timeout(function() {
                    $(e.target).trigger('input');
                    $(e.target).trigger('change'); // for IE
                });
            };

            // Add new therapeutic implication
            $scope.addTrial = function(trials, newTrial, trialsReview, trialsUuid) {
                if (trials && newTrial) {
                    if (trials.indexOf(newTrial) === -1) {
                        if (newTrial.match(/NCT[0-9]+/ig)) {
                            if (trialsReview && !trialsReview.get('lastReviewed')) {
                                trialsReview.set('lastReviewed', trials.asArray().slice(0));
                            }
                            setReview(trialsUuid, true);
                            trialsReview.set('updatedBy', User.name);
                            trialsReview.set('updateTime', new Date().toLocaleString());
                            trials.push(newTrial);
                        } else {
                            dialogs.notify('Warning', 'Please check your trial ID format. (e.g. NCT01562899)');
                        }
                    } else {
                        dialogs.notify('Warning', 'Trial exists.');
                    }
                }
            };
            $scope.removeTrial = function(trials, index, trialsReview, trialsUuid) {
                trialsReview.set('lastReviewed', trials.asArray().slice(0));
                setReview(trialsUuid, true);
                trialsReview.set('updatedBy', User.name);
                trialsReview.set('updateTime', new Date().toLocaleString());
                trials.remove(index);
            };

            $scope.addVUSItem = function(newVUSName, newVUSTime) {
                if (newVUSName) {
                    var notExist = true;
                    newVUSName = newVUSName.trim();
                    $scope.gene.mutations.asArray().forEach(function(e, i) {
                        if (e.name.getText().trim().toLowerCase() === newVUSName.toLowerCase()) {
                            notExist = false;
                        }
                    });

                    if (notExist && !containVariantInVUS(newVUSName)) {
                        $scope.realtimeDocument.getModel().beginCompoundOperation();
                        var vus = $scope.realtimeDocument.getModel().create(OncoKB.VUSItem);
                        var timeStamp = $scope.realtimeDocument.getModel().create(OncoKB.TimeStampWithCurator);

                        if (!newVUSTime) {
                            newVUSTime = new Date().getTime().toString();
                        }

                        timeStamp.value.setText(newVUSTime);
                        timeStamp.by.name.setText(User.name);
                        timeStamp.by.email.setText(User.email);
                        vus.name.setText(newVUSName);
                        vus.time.push(timeStamp);
                        $scope.vus.push(vus);
                        $scope.realtimeDocument.getModel().endCompoundOperation();
                    } else {
                        dialogs.notify('Warning', 'Variant exists.');
                    }
                }
            };

            $scope.cleanTrial = function(trials) {
                var cleanTrials = {};
                trials.asArray().forEach(function(e, index) {
                    if (cleanTrials.hasOwnProperty(e)) {
                        cleanTrials[e].push(index);
                    } else {
                        cleanTrials[e] = [];
                    }
                });
                for (var key in cleanTrials) {
                    if (cleanTrials[key].length > 0) {
                        cleanTrials[key].forEach(function() {
                            trials.removeValue(key);
                        });
                    }
                }
                console.log(cleanTrials);
            };

            $scope.addTrialStr = function(trials) {
                if (trials && this.trialsStr) {
                    var _trials = this.trialsStr.split(/\s+/);
                    _trials.forEach(function(e) {
                        if (trials.indexOf(e) === -1) {
                            trials.push(e);
                        }
                    });
                    this.trialsStr = '';
                }
            };

            $scope.checkScope = function() {
                console.log($scope.gene);
                // console.log($scope.gene.mutations.get(0).tumors.get(0));
                console.log($scope.geneStatus);

                console.log('Num of watchers: ' + checkNumWatchers());
                console.log($scope.gene.status_timeStamp.get('lastUpdate').value);

                $scope.gene.mutations.asArray().forEach(function(e) {
                    console.log('------------------');
                    // console.log(e);
                    // console.log(e.shortSummary);
                    // console.log(e.shortSummary_eStatus);
                    // console.log(e.shortSummary_eStatus.get('curated'));
                    // console.log(e.effect);
                    // console.log(e.oncogenic);
                    // console.log(e.description);]
                    e.tumors.asArray().forEach(function(tumortype) {
                        console.log(tumortype);
                        // tumortype.cancerTypes.asArray().forEach(function(cancerType) {
                        //     console.log(cancerType);
                        // })
                    });
                    console.log('------------------');
                });
            };

            $scope.getCancerTypesName = function(cancerTypes) {
                var list = [];
                cancerTypes.asArray().forEach(function(cancerType) {
                    if (cancerType.subtype.length > 0) {
                        var str = cancerType.subtype.getText();
                        // if (cancerType.oncoTreeCode.length > 0) {
                        //     str += '(' + cancerType.oncoTreeCode + ')';
                        // }
                        list.push(str);
                    } else if (cancerType.cancerType.length > 0) {
                        list.push(cancerType.cancerType.getText());
                    }
                });
                return list.join(', ');
            };

            function getNewCancerTypesName(cancerTypes) {
                var list = [];
                _.each(cancerTypes, function(cancerType) {
                    if (cancerType.subtype && cancerType.subtype.name && cancerType.subtype.name.length > 0) {
                        var str = cancerType.subtype.name;
                        if (cancerType.subtype.code.length > 0) {
                            str += '(' + cancerType.subtype.code + ')';
                        }
                        list.push(str);
                    } else if (cancerType.mainType && cancerType.mainType.name && cancerType.mainType.name.length > 0) {
                        list.push(cancerType.mainType.name);
                    }
                });
                return list.join(', ');
            }

            $scope.updateGeneColor = function() {
                if ($scope.gene && $scope.document && $scope.document.hasOwnProperty('modifiedDate')) {
                    if (new Date($scope.document.modifiedDate).getTime() > Number($scope.gene.status_timeStamp.get('lastUpdate').value.text)) {
                        return 'red';
                    }
                    return 'black';
                }
                return 'black';
            };

            $scope.remove = function(event, mutationIndex, tumorTypeIndex, therapyCategoryIndex, therapyIndex) {
                console.log(mutationIndex, tumorTypeIndex, therapyCategoryIndex, therapyIndex);
                $scope.stopCollopse(event);
                var dlg = dialogs.confirm('Confirmation', 'Are you sure you want to delete this entry?');
                dlg.result.then(function() {
                    var _index = -1;
                    myDeletedEvidenceIndice.push([mutationIndex, tumorTypeIndex, therapyCategoryIndex, therapyIndex]);
                    if (angular.isNumber(mutationIndex)) {
                        if (!isNaN(mutationIndex)) {
                            if (isNaN(tumorTypeIndex)) {
                                _index = Number(angular.copy(mutationIndex));
                                var mutation = $scope.gene.mutations.get(_index);
                                mutation.name_review.set('removed', true);
                                mutation.name_review.set('updatedBy', User.name);
                                mutation.name_review.set('updateTime', new Date().toLocaleString());
                                setReview($scope.gene.mutations.get(_index).name_uuid, true);
                            } else if (!isNaN(therapyCategoryIndex) && !isNaN(therapyIndex)) {
                                _index = Number(angular.copy(therapyIndex));
                                var treatment = $scope.gene.mutations.get(mutationIndex).tumors.get(tumorTypeIndex).TI.get(therapyCategoryIndex).treatments.get(_index);
                                treatment.name_review.set('removed', true);
                                treatment.name_review.set('updatedBy', User.name);
                                treatment.name_review.set('updateTime', new Date().toLocaleString());
                                setReview(treatment.name_uuid, true);
                            } else {
                                _index = Number(angular.copy(tumorTypeIndex));
                                var tumor = $scope.gene.mutations.get(mutationIndex).tumors.get(_index);
                                tumor.name_review.set('removed', true);
                                tumor.name_review.set('updatedBy', User.name);
                                tumor.name_review.set('updateTime', new Date().toLocaleString());
                                setReview(tumor.name_uuid, true);
                            }
                        }
                    }
                }, function() {
                });
            };
            function collectUUIDs(type, obj, uuids) {
                if (type === 'mutation') {
                    uuids.push(obj.oncogenic_uuid.getText());
                    uuids.push(obj.shortSummary_uuid.getText());
                    uuids.push(obj.effect_uuid.getText());
                    _.each(obj.tumors.asArray(), function(tumor) {
                        collectUUIDs('tumor', tumor, uuids);
                    });
                }
                if (type === 'tumor') {
                    uuids.push(obj.summary_uuid.getText());
                    uuids.push(obj.prevalence_uuid.getText());
                    uuids.push(obj.progImp_uuid.getText());
                    uuids.push(obj.nccn_uuid.getText());
                    _.each(obj.TI.asArray(), function(ti) {
                        uuids.push(ti.description_uuid.getText());
                        _.each(ti.treatments.asArray(), function(treatment) {
                            collectUUIDs('treatment', treatment, uuids);
                        });
                    });
                    uuids.push(obj.trials_uuid.getText());
                }
                if (type === 'treatment') {
                    uuids.push(obj.name_uuid.getText());
                }
                return uuids;
            }

            function removeInModel(mutationIndex, tumorTypeIndex, therapyCategoryIndex, therapyIndex) {
                var _index = -1;
                console.log(mutationIndex, tumorTypeIndex, therapyCategoryIndex, therapyIndex);
                if (angular.isNumber(mutationIndex)) {
                    if (!_.isUndefined(mutationIndex)) {
                        if (_.isUndefined(tumorTypeIndex)) {
                            _index = Number(angular.copy(mutationIndex));
                            $scope.gene.mutations.remove(_index);
                            delete $scope.geneStatus[_index];
                            $scope.geneStatus = migrateGeneStatusPosition($scope.geneStatus, _index);
                        } else if (!_.isUndefined(therapyCategoryIndex) && !_.isUndefined(therapyIndex)) {
                            _index = Number(angular.copy(therapyIndex));
                            var treatments = $scope.gene.mutations.get(mutationIndex).tumors.get(tumorTypeIndex).TI.get(therapyCategoryIndex).treatments;
                            treatments.remove(_index);
                            // this needs to be put back after fix the treatment index bug
                             delete $scope.geneStatus[mutationIndex][tumorTypeIndex][therapyCategoryIndex][_index];
                             $scope.geneStatus[mutationIndex][tumorTypeIndex][therapyCategoryIndex] = migrateGeneStatusPosition($scope.geneStatus[mutationIndex][tumorTypeIndex][therapyCategoryIndex], _index);
                        } else {
                            _index = Number(angular.copy(tumorTypeIndex));
                            var tumors = $scope.gene.mutations.get(mutationIndex).tumors;
                            tumors.remove(_index);
                            delete $scope.geneStatus[mutationIndex][_index];
                            $scope.geneStatus[mutationIndex] = migrateGeneStatusPosition($scope.geneStatus[mutationIndex], _index);
                        }
                    }
                }
            }

            $scope.confirmDelete = function(event, type, obj, mutationIndex, tumorTypeIndex, therapyCategoryIndex, therapyIndex) {
                $scope.stopCollopse(event);
                var uuids = collectUUIDs(type, obj, []);
                if($scope.status.isDesiredGene) {
                    // make the api call to delete evidences
                    $scope.$emit('startSaveDataToDatabase');
                    DatabaseConnector.deleteEvidences(uuids, function(result) {
                        removeInModel(mutationIndex, tumorTypeIndex, therapyCategoryIndex, therapyIndex);
                        _.each(uuids, function(uuid) {
                            $rootScope.reviewMeta.delete(uuid);
                        });
                        myDeletedEvidences = _.difference(myDeletedEvidences, uuids);
                        $scope.$emit('doneSaveDataToDatabase');
                    }, function(error) {
                        console.log('fail to update to database', error);
                        dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                        $scope.$emit('doneSaveDataToDatabase');
                    });
                } else {
                    removeInModel(mutationIndex, tumorTypeIndex, therapyCategoryIndex, therapyIndex);
                    _.each(uuids, function(uuid) {
                        $rootScope.reviewMeta.delete(uuid);
                    });
                    myDeletedEvidences = _.difference(myDeletedEvidences, uuids);
                }
            }

            $scope.cancelDelete = function(event, type, obj, mutationIndex, tumorTypeIndex, therapyCategoryIndex, therapyIndex) {
                $scope.stopCollopse(event);
                var dlg = dialogs.confirm('Reminder', 'Are you sure you want to reject this change?');
                dlg.result.then(function() {
                    var uuids = collectUUIDs(type, obj, []);
                    myDeletedEvidences = _.difference(myDeletedEvidences, uuids);
                    var _index = -1;
                    if (angular.isNumber(mutationIndex)) {
                        if (!isNaN(mutationIndex)) {
                            if (isNaN(tumorTypeIndex)) {
                                _index = Number(angular.copy(mutationIndex));
                                var mutation = $scope.gene.mutations.get(_index);
                                mutation.name_review.set('removed', false);
                                mutation.name_review.set('updatedBy', null);
                                mutation.name_review.set('updateTime', null);
                                setReview($scope.gene.mutations.get(_index).name_uuid, false);
                            } else if (!isNaN(therapyCategoryIndex) && !isNaN(therapyIndex)) {
                                _index = Number(angular.copy(therapyIndex));
                                var treatment = $scope.gene.mutations.get(mutationIndex).tumors.get(tumorTypeIndex).TI.get(therapyCategoryIndex).treatments.get(_index);
                                treatment.name_review.set('removed', false);
                                treatment.name_review.set('updatedBy', null);
                                treatment.name_review.set('updateTime', null);
                                setReview(treatment.name_uuid, false);
                            } else {
                                _index = Number(angular.copy(tumorTypeIndex));
                                var tumor = $scope.gene.mutations.get(mutationIndex).tumors.get(_index);
                                tumor.name_review.set('removed', false);
                                tumor.name_review.set('updatedBy', null);
                                tumor.name_review.set('updateTime', null);
                                setReview(tumor.name_uuid, false);
                            }
                        }
                    }
                });
            };

            $scope.commentClick = function(event) {
                $scope.stopCollopse(event);
            };

            function fetchResults(data) {
                var PMIDs = [];
                var abstracts = [];
                _.each(data, function(item) {
                    if (item.type === 'pmid') {
                        PMIDs.push(item.id);
                    } else if (item.type === 'abstract') {
                        abstracts.push(item.id);
                    }
                });
                PMIDs.sort();
                abstracts.sort();
                return {PMIDs: PMIDs, abstracts: abstracts};
            }

            $scope.getAllCitations = function() {
                var results = [];
                var geneData = JSON.stringify(stringUtils.getGeneData(this.gene, true, true, true));
                results = fetchResults(FindRegex.result(geneData));
                var annotationPMIDs = results.PMIDs;
                var annotationAbstracts = results.abstracts;

                var vusData = JSON.stringify(stringUtils.getVUSFullData(this.vus));
                results = fetchResults(FindRegex.result(vusData));
                var vusPMIDs = results.PMIDs;
                var vusAbstracts = results.abstracts;
                var hasAnnotation = annotationPMIDs.length + annotationAbstracts.length > 0;
                var hasVUS = vusPMIDs.length + vusAbstracts.length > 0;

                // we only seperate citations information to tabs when both annotation and vus citations exist and there are too much info to fit in one tab
                var tabFlag = hasAnnotation && hasVUS && (annotationPMIDs.length > 80 || annotationAbstracts.length > 10 || vusPMIDs.length > 80 || vusAbstracts.length > 10);
                var messageContent = [];
                if (!hasAnnotation && !hasVUS) {
                    messageContent.push('No information available!');
                } else if (tabFlag) {
                    messageContent.push('<ul class="nav nav-tabs">');
                    if (hasAnnotation) {
                        messageContent.push('<li class="active"><a data-toggle="tab" href="#home"><h4>Annotation</h4></a></li>');
                    }
                    if (hasVUS) {
                        messageContent.push('<li><a data-toggle="tab" href="#menu1"><h4>VUS</h4></a></li>');
                    }
                    messageContent.push('</ul><div class="tab-content">');
                    if (hasAnnotation) {
                        messageContent.push('<div id="home" class="tab-pane fade in active"><h4>PMIDs (' + annotationPMIDs.length + ')</h4><p>' + annotationPMIDs.join(', ') + '</p>');
                        if (annotationAbstracts.length > 0) {
                            messageContent.push('<h4>Abstracts (' + annotationAbstracts.length + ')</h4><p>' + annotationAbstracts.join(', ') + '</p>');
                        }
                        messageContent.push('</div>');
                    }
                    if (hasVUS) {
                        messageContent.push('<div id="menu1" class="tab-pane fade"><h4>PMIDs (' + vusPMIDs.length + ')</h4><p>' + vusPMIDs.join(', ') + '</p>');
                        if (vusAbstracts.length > 0) {
                            messageContent.push('<h4>Abstracts (' + vusAbstracts.length + ')</h4><p>' + vusAbstracts.join(', ') + '</p>');
                        }
                        messageContent.push('</div>');
                    }
                    messageContent.push('</div>');
                } else {
                    if (hasAnnotation) {
                        messageContent.push('<h3 style="color:black">Annotation</h3>');
                        if (annotationPMIDs.length > 0) {
                            messageContent.push('<h4>PMIDs (' + annotationPMIDs.length + ')</h4><p>' + annotationPMIDs.join(', ') + '</p>');
                        }
                        if (annotationAbstracts.length > 0) {
                            messageContent.push('<h4>Abstracts (' + annotationAbstracts.length + ')</h4><p>' + annotationAbstracts.join(', ') + '</p>');
                        }
                    }

                    if (hasVUS) {
                        messageContent.push('<hr/><h3 style="color:black">VUS</h3>');
                        if (vusPMIDs.length > 0) {
                            messageContent.push('<h4>PMIDs (' + vusPMIDs.length + ')</h4><p>' + vusPMIDs.join(', ') + '</p>');
                        }
                        if (vusAbstracts.length > 0) {
                            messageContent.push('<h4>Abstracts (' + vusAbstracts.length + ')</h4><p>' + vusAbstracts.join(', ') + '</p>');
                        }
                    }
                }
                dialogs.notify('All Citations', messageContent.join(''), {size: 'lg'});
            };
            $scope.curatorsName = function() {
                return this.gene.curators.asArray().map(function(d) {
                    return d.name;
                }).join(', ');
            };

            $scope.curatorsEmail = function() {
                return this.gene.curators.asArray().map(function(d) {
                    return d.email;
                }).join(', ');
            };

            $scope.removeCurator = function(index) {
                $scope.gene.curators.remove(index);
            };

            $scope.checkTI = function(TI, status, type) {
                var _status = TI.types.get('status').toString();
                var _type = TI.types.get('type').toString();
                status = status.toString();
                type = type.toString();
                if (_status === status && _type === type) {
                    return true;
                }
                return false;
            };

            $scope.mutationEffectChanged = function(mutationEffect) {
                mutationEffect.addOn.setText('');
            };

            $scope.move = function(driveList, index, moveIndex, event) {
                var tmpStatus;
                var moveStatusIndex;
                var indexes = [];
                var geneStatus = angular.copy($scope.geneStatus);
                var key;
                var numKey;
                $scope.stopCollopse(event);

                index = parseInt(index, 10);
                moveIndex = parseInt(moveIndex, 10);

                if (moveIndex <= index) {
                    if (moveIndex <= 0) {
                        moveIndex = moveStatusIndex = 0;
                    } else {
                        moveIndex = moveStatusIndex = moveIndex - 1;
                    }
                } else {
                    moveStatusIndex = moveIndex - 1;
                }

                if (moveIndex > driveList.length) {
                    moveIndex = driveList.length;
                    moveStatusIndex = moveIndex - 1;
                }

                tmpStatus = angular.copy($scope.geneStatus[index]);

                if (index < moveStatusIndex) {
                    for (key in geneStatus) {
                        if (!isNaN(key)) {
                            numKey = Number(key);
                            if (numKey <= moveStatusIndex && numKey > index) {
                                indexes.push(numKey);
                            }
                        }
                    }
                    indexes.sort(function(a, b) {
                        return a - b;
                    }).forEach(function(e) {
                        geneStatus[e - 1] = geneStatus[e];
                    });
                } else {
                    for (key in geneStatus) {
                        if (!isNaN(key)) {
                            numKey = Number(key);
                            if (numKey >= moveStatusIndex && numKey < index) {
                                indexes.push(numKey);
                            }
                        }
                    }
                    indexes.sort(function(a, b) {
                        return b - a;
                    }).forEach(function(e) {
                        geneStatus[e + 1] = geneStatus[e];
                    });
                }

                geneStatus[moveStatusIndex] = tmpStatus;

                $scope.geneStatus = geneStatus;

                driveList.move(index, moveIndex);
            };

            $scope.stopCollopse = function(event) {
                if (event.stopPropagation) {
                    event.stopPropagation();
                }
                if (event.preventDefault && event.type !== 'keypress') {
                    event.preventDefault();
                }
            };

            $scope.generatePDF = function() {
                jspdf.create(stringUtils.getGeneData(this.gene, true, true, true));
            };

            $scope.isOpenFunc = function(type) {
                var processKey = '';
                var targetStatus = '';
                var geneStatus = $scope.geneStatus;
                var specialEscapeKeys = ['isOpen', 'hideEmpty'];
                var flag;
                if (type === 'expand') {
                    targetStatus = true;
                    processKey = 'isOpen';
                } else if (type === 'collapse') {
                    targetStatus = false;
                    processKey = 'isOpen';
                } else if (type === 'hideEmpty') {
                    targetStatus = true;
                    processKey = 'hideEmpty';
                } else if (type === 'showEmpty') {
                    targetStatus = false;
                    processKey = 'hideEmpty';
                }

                // for: mutation
                for (var key in geneStatus) {
                    if (!isNaN(key)) {
                        geneStatus[key][processKey] = targetStatus;
                    }

                    // for: tumor type
                    for (var _key in geneStatus[key]) {
                        // for: therapeutic implications
                        if (specialEscapeKeys.indexOf(_key) === -1) {
                            flag = targetStatus;
                            if (isNaN(_key) && flag) {
                                if (processKey === 'isOpen') {
                                    flag = $scope.gene.mutations.get(Number(key))[_key].text ? targetStatus : false;
                                } else {
                                    flag = targetStatus;
                                }
                            }
                            geneStatus[key][_key][processKey] = flag;

                            for (var __key in geneStatus[key][_key]) {
                                if (geneStatus[key][_key].hasOwnProperty(__key)) {
                                    flag = targetStatus;
                                    if (specialEscapeKeys.indexOf(__key) === -1) {
                                        if (isNaN(__key)) {
                                            if (processKey === 'isOpen') {
                                                if (__key === 'nccn') {
                                                    flag = $scope.hasNccn($scope.gene.mutations.get(Number(key)).tumors.get(Number(_key)).nccn) ? targetStatus : false;
                                                } else if (__key === 'trials') {
                                                    flag = $scope.gene.mutations.get(Number(key)).tumors.get(Number(_key)).trials.length > 0 ? targetStatus : false;
                                                } else {
                                                    flag = $scope.gene.mutations.get(Number(key)).tumors.get(Number(_key))[__key].text ? targetStatus : false;
                                                }
                                            } else {
                                                flag = targetStatus;
                                            }
                                            geneStatus[key][_key][__key][processKey] = flag;
                                        } else if (!isNaN(__key)) {
                                            if ($scope.gene.mutations.get(Number(key)).tumors.get(Number(_key)).TI.get(Number(__key)).treatments.length > 0) {
                                                // for: treatments
                                                geneStatus[key][_key][__key][processKey] = flag;
                                                for (var ___key in geneStatus[key][_key][__key]) {
                                                    if (specialEscapeKeys.indexOf(___key) === -1) {
                                                        geneStatus[key][_key][__key][___key][processKey] = flag;
                                                    }
                                                }
                                            } else if ($scope.gene.mutations.get(Number(key)).tumors.get(Number(_key)).TI.get(Number(__key)).description.text) {
                                                geneStatus[key][_key][__key][processKey] = flag;
                                            } else if (processKey === 'isOpen') {
                                                geneStatus[key][_key][__key].isOpen = false;
                                            } else {
                                                geneStatus[key][_key][__key][processKey] = flag;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                $scope.geneStatus = geneStatus;
            };
            /* eslint no-unused-vars: 0*/
            $scope.changeIsOpen = function(target) {
                target = !target;
            };

            $scope.checkEmpty = function(mutation, type) {
                if (type === 'mutationEffect') {
                    if (mutation.effect.value.text === '' && mutation.description.text === '' && mutation.short.text === '') {
                        return true;
                    }
                    return false;
                } else if (type === 'oncogenicity') {
                    if ((mutation.oncogenic.text === '' || mutation.oncogenic.text === 'false') && mutation.shortSummary.text === '') {
                        return true;
                    }
                    return false;
                }
                return false;
            };

            $scope.hasNccn = function(nccn) {
                if (nccn) {
                    if (nccn.disease.text && nccn.disease.text !== 'NA') {
                        return true;
                    }
                }
                return false;
            };

            $scope.curatedIconClick = function(event, status) {
                $scope.stopCollopse(event);
                status.set('curated', !status.get('curated'));
            };

            $scope.mutationNameEditable = function(mutationName) {
                return $scope.fileEditable && !($scope.userRole !== 8 &&
                    $scope.suggestedMutations.indexOf(mutationName) !== -1);
            };

            $scope.updateVUS = function() {
                var vus = $scope.realtimeDocument.getModel().getRoot().get('vus');
                var vusData = stringUtils.getVUSFullData(vus, true);
                $scope.$emit('startSaveDataToDatabase');
                DatabaseConnector.updateVUS($scope.gene.name, JSON.stringify(vusData), function(result) {
                    $scope.$emit('doneSaveDataToDatabase');
                }, function(error) {
                    console.log(error);
                    dialogs.error('Error', 'An error has occurred while saving VUS to database, please contact the developer. Thanks.');
                    $scope.$emit('doneSaveDataToDatabase');
                });
            };

            // Calculate number of 'number' elements within the object
            function getNoNKeys(object) {
                var count = 0;
                for (var key in object) {
                    if (!isNaN(key)) {
                        count++;
                    }
                }
                return count;
            }

            // Only do the simple check wheter the numebr of array has been changed.
            // It's a little triky to monitor all content.
            function regenerateGeneStatus() {
                var geneStatus = {};
                var mutationKeys = ['oncogenic'];
                var tumorKeys = ['prevalence', 'progImp', 'nccn', 'trials'];

                var changeMutation = false;

                if ($scope.gene.mutations.length !== getNoNKeys($scope.geneStatus)) {
                    changeMutation = true;
                }
                $scope.gene.mutations.asArray().forEach(function(mutation, mutationIndex) {
                    if (changeMutation) {
                        geneStatus[mutationIndex] = $.extend($scope.geneStatus[mutationIndex], new GeneStatusSingleton());
                        mutationKeys.forEach(function(key) {
                            if (mutation[key]) {
                                geneStatus[mutationIndex][key] = new GeneStatusSingleton();
                            }
                        });
                    } else {
                        geneStatus[mutationIndex] = $scope.geneStatus[mutationIndex];
                    }

                    if (mutation.tumors.length > 0) {
                        var changeTT = false;

                        if (mutation.tumors.length !== getNoNKeys($scope.geneStatus[mutationIndex])) {
                            changeTT = true;
                        }
                        mutation.tumors.asArray().forEach(function(tumor, tumorIndex) {
                            if (changeTT) {
                                geneStatus[mutationIndex][tumorIndex] = $.extend($scope.geneStatus[mutationIndex][tumorIndex], new GeneStatusSingleton());
                            }
                            tumorKeys.forEach(function(key) {
                                if (tumor[key] && changeTT) {
                                    geneStatus[mutationIndex][tumorIndex][key] = new GeneStatusSingleton();
                                }
                                tumor.TI.asArray(function(therapyType, therapyTypeIndex) {
                                    geneStatus[mutationIndex][tumorIndex][therapyTypeIndex] = $scope.geneStatus[mutationIndex][tumorIndex][therapyTypeIndex];
                                    var changeT = false;

                                    if (therapyType.treatment.length !== getNoNKeys($scope.geneStatus[mutationIndex][tumorIndex][therapyTypeIndex])) {
                                        changeT = true;
                                    }
                                    therapyType.treatments.asArray(function(treatment, treatmentIndex) {
                                        geneStatus[mutationIndex][tumorIndex][therapyTypeIndex][treatmentIndex] = changeT ? new GeneStatusSingleton() : $scope.geneStatus[mutationIndex][tumorIndex][therapyTypeIndex][treatmentIndex];
                                    });
                                });
                            });
                        });
                    }
                });
                $scope.geneStatus = geneStatus;
            }

            function migrateGeneStatusPosition(object, indexRemoved) {
                if (angular.isNumber(indexRemoved)) {
                    var indexes = [];
                    for (var key in object) {
                        if (!isNaN(key) && Number(key) > indexRemoved) {
                            indexes.push(Number(key));
                        }
                    }

                    indexes.sort(function(a, b) {
                        return a - b;
                    }).forEach(function(e) {
                        object[e - 1] = object[e];
                    });

                    delete object[indexes.pop()];
                    return object;
                }
                return false;
            }

            function checkNumWatchers() {
                var root = angular.element(document.getElementsByTagName('body'));

                var watchers = [];

                var f = function(element) {
                    angular.forEach(['$scope', '$isolateScope'], function(scopeProperty) {
                        if (element.data() && element.data().hasOwnProperty(scopeProperty)) {
                            angular.forEach(element.data()[scopeProperty].$$watchers, function(watcher) {
                                watchers.push(watcher);
                            });
                        }
                    });

                    angular.forEach(element.children(), function(childElement) {
                        f(angular.element(childElement));
                    });
                };

                f(root);

                // Remove duplicate watchers
                var watchersWithoutDuplicates = [];
                angular.forEach(watchers, function(item) {
                    if (watchersWithoutDuplicates.indexOf(item) < 0) {
                        watchersWithoutDuplicates.push(item);
                    }
                });

                console.log(watchersWithoutDuplicates);

                return watchersWithoutDuplicates.length;
            }

            function sendEmail(subject, content) {
                if ($scope.userRole < 8) {
                    var param = {subject: subject, content: content};

                    DatabaseConnector.sendEmail(
                        param,
                        function(result) {
                            console.log('success', result);
                        },
                        function(result) {
                            console.log('failed', result);
                        }
                    );
                }
            }

            function getSuggestedMutations() {
                var defaultPlaceHolder = 'No suggestion found. Please curate according to literature.';
                DatabaseConnector.getSuggestedVariants()
                    .then(function(resp) {
                        if (resp && _.isArray(resp.data) && resp.data.length > 0) {
                            $scope.suggestedMutations = resp.data;
                        } else {
                            $scope.suggestedMutations = [];
                        }
                    }, function() {
                        $scope.suggestedMutations = [];
                    })
                    .finally(function() {
                        if ($scope.suggestedMutations.length === 0) {
                            $scope.addMutationPlaceholder = defaultPlaceHolder;
                        }
                    });
            }

            function loadMetaFile(callback) {
                storage.retrieveMeta().then(function(result) {
                    if (result && result.error) {
                        dialogs.error('Error', 'Fail to retrieve meta file! Please stop editing and contact the developer!');
                    } else {
                        storage.getMetaRealtimeDocument(result[0].id).then(function(metaRealtime) {
                            if (metaRealtime && metaRealtime.error) {
                                dialogs.error('Error', 'Fail to get meta document! Please stop editing and contact the developer!');
                                $scope.fileEditable = false;
                            } else {
                                $rootScope.metaRealtime = metaRealtime;
                                $rootScope.metaModel = metaRealtime.getModel();
                                if (!$rootScope.metaModel.getRoot().get('review').get($scope.fileTitle)) {
                                    var tempMap = $rootScope.metaModel.createMap();
                                    $rootScope.metaModel.getRoot().get('review').set($scope.fileTitle, tempMap);
                                }
                                $rootScope.reviewMeta = $rootScope.metaModel.getRoot().get('review').get($scope.fileTitle);
                            }
                            callback();
                        });
                    }
                });
            }

            function bindDocEvents() {
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.COLLABORATOR_JOINED, displayCollaboratorEvent);
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.COLLABORATOR_LEFT, displayCollaboratorEvent);
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED, saveStateChangedEvent);
                $scope.model.addEventListener(gapi.drive.realtime.EventType.UNDO_REDO_STATE_CHANGED, onUndoStateChanged);
                $scope.gene.addEventListener(gapi.drive.realtime.EventType.VALUE_CHANGED, valueChangedEvent);
                $rootScope.metaRealtime.addEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED, saveMetaChangedEvent);
                $rootScope.metaRealtime.addEventListener(gapi.drive.realtime.EventType.VALUE_CHANGED, metaValueChanged);
            }

            function saveStateChangedEvent(evt) {
                // set gene document to readable only when it s in review
                if (underOthersReview()) {
                    $scope.$emit('interruptedDueToOtherReview');
                } else {
                    // If document is editable again, need to notify the user.
                    if (!$scope.fileEditable && $scope.document.editable) {
                        dialogs.notify('Notification',
                            'You can now continue editing the document. Thanks.');
                    }
                    $scope.fileEditable = $scope.document.editable;
                }
                if ($scope.$$phase) {
                    updateDocStatus(evt);
                } else {
                    $scope.$apply(function() {
                        updateDocStatus(evt);
                    });
                }
            }

            function updateDocStatus(evt) {
                if (evt.isSaving) {
                    documentSaving();
                } else if (!evt.isSaving && !evt.currentTarget.isClosed) {
                    documentSaved();
                } else {
                    documentClosed();
                }
            }

            function saveMetaChangedEvent(evt) {
                if ($rootScope.$$phase) {
                    updateMetaDocStatus(evt);
                } else {
                    $rootScope.$apply(function() {
                        updateMetaDocStatus(evt);
                    });
                }
            }

            function metaValueChanged(evt) {
                console.log('Here you go!');
            }

            function updateMetaDocStatus(evt) {
                if (evt.isSaving) {
                    documentSaving('meta');
                } else if (!evt.isSaving && !evt.currentTarget.isClosed) {
                    documentSaved('meta');
                } else {
                    documentClosed('meta');
                }
            }

            function afterCreateGeneModel() {
                var file = Documents.get({title: $scope.fileTitle});
                var timeStamp;
                file = file[0];
                // if (!$scope.gene.status_timeStamp.has('lastEdit')) {
                //     $scope.realtimeDocument.getModel().beginCompoundOperation();
                //     timeStamp = $scope.realtimeDocument.getModel().create('TimeStamp');
                //     timeStamp.value.setText(new Date().getTime().toString());
                //     timeStamp.by.setText(Users.getMe().name);
                //     $scope.gene.status_timeStamp.set('lastEdit', timeStamp);
                //     $scope.realtimeDocument.getModel().endCompoundOperation();
                // }
                if (!$scope.gene.status_timeStamp.has('lastUpdate')) {
                    $scope.realtimeDocument.getModel().beginCompoundOperation();
                    timeStamp = $scope.realtimeDocument.getModel().create('TimeStamp');
                    timeStamp.value.setText(new Date().getTime().toString());
                    timeStamp.by.setText(Users.getMe().name);
                    $scope.gene.status_timeStamp.set('lastUpdate', timeStamp);
                    $scope.realtimeDocument.getModel().endCompoundOperation();
                }
                $scope.document = file;
                $scope.fileEditable = file.editable;
                addVUS();
                $scope.status.rendering = false;
                displayAllCollaborators($scope.realtimeDocument, bindDocEvents);

                if (underReview()) {
                    // This will only happen if the currentReviewer is not empty
                    $scope.fileEditable = false;
                }
                // Add timeout until the collaborator join event is triggered.
                $timeout(function() {
                    if (underOthersReview()) {
                        $scope.$emit('interruptedDueToOtherReview');
                    } else {
                        // if no other is reviewing the current document,
                        // need to reset the document to initial state.
                        $scope.exitReview();
                    }
                }, 2000);
            }

            function valueChangedEvent(evt) {
                console.log('valueChanged', evt);
                if ($scope.gene) {
                    var hasCurator = false;
                    if ($scope.gene.curators && angular.isArray($scope.gene.curators.asArray()) && $scope.gene.curators.asArray().length > 0) {
                        var _array = $scope.gene.curators.asArray();
                        for (var i = 0; i < _array.length; i++) {
                            if (_array[i].email.text === User.email) {
                                hasCurator = true;
                                break;
                            }
                        }

                        if (!hasCurator) {
                            $scope.realtimeDocument.getModel().beginCompoundOperation();
                            var __curator = $scope.realtimeDocument.getModel().create(OncoKB.Curator, User.name, User.email);
                            $scope.gene.curators.push(__curator);
                            $scope.realtimeDocument.getModel().endCompoundOperation();
                        }
                    } else {
                        $scope.realtimeDocument.getModel().beginCompoundOperation();
                        var _curator = $scope.realtimeDocument.getModel().create(OncoKB.Curator, User.name, User.email);
                        $scope.gene.curators.push(_curator);
                        $scope.realtimeDocument.getModel().endCompoundOperation();
                    }
                }
            }

            function displayCollaboratorEvent(evt) {
                switch (evt.type) {
                case 'collaborator_left':
                    removeCollaborator(evt.collaborator);
                    break;
                case 'collaborator_joined':
                    addCollaborator(evt.collaborator);
                    break;
                default:
                    console.info('Unknown event:', evt);
                    break;
                }
                $scope.$apply($scope.collaborators);
            }

            function underOthersReview() {
                var currentReviewer = $scope.gene.name_review.get('currentReviewer');
                if (currentReviewer) {
                    var _name = currentReviewer.getText();
                    if (_name &&
                        _name.toUpperCase() !== User.name.toUpperCase() &&
                        hasCollaborator(currentReviewer.getText())) {
                        return true;
                    }
                }
                return false;
            }

            function hasCollaborator(name) {
                var collaborators = $scope.realtimeDocument.getCollaborators();
                if (_.isArray(collaborators)) {
                    for (var i = 0; i < collaborators.length; i++) {
                        if (collaborators[i].displayName.toUpperCase() === name.toUpperCase()) {
                            return true;
                        }
                    }
                }
                return false;
            }

            function underReview() {
                var currentReviewer = $scope.gene.name_review.get('currentReviewer');
                if (currentReviewer && currentReviewer.getText()) {
                    return true;
                }
                return false;
            }

            function addCollaborator(user) {
                if (!$scope.collaborators.hasOwnProperty(user.userId)) {
                    $scope.collaborators[user.sessionId] = {};
                }
                $scope.collaborators[user.sessionId] = user;
            }

            function removeCollaborator(user) {
                if ($scope.collaborators.hasOwnProperty(user.sessionId)) {
                    delete $scope.collaborators[user.sessionId];
                } else {
                    console.log('Unknown collaborator:', user);
                }
            }

            function displayAllCollaborators(document, callback) {
                var collaborators = document.getCollaborators();
                var collaboratorCount = collaborators.length;
                var _user = {};
                for (var i = 0; i < collaboratorCount; i++) {
                    var user = collaborators[i];
                    if (!$scope.collaborators.hasOwnProperty(user.userId)) {
                        $scope.collaborators[user.sessionId] = {};
                    }
                    $scope.collaborators[user.sessionId] = user;
                    if (user.isMe) {
                        _user = user;
                    }
                }

                if (User.email === 'N/A') {
                    storage.getUserInfo(_user.userId).then(function(userInfo) {
                        User.name = userInfo.displayName;
                        if (angular.isArray(userInfo.emails)) {
                            if (userInfo.emails.length > 0) {
                                User.email = userInfo.emails[0].value;
                            } else {
                                User.email = 'N/A';
                            }
                        } else {
                            User.email = userInfo.emails;
                        }
                        callback();
                    });
                } else {
                    callback();
                }
            }

            function onUndoStateChanged(evt) {
                if (evt.canUndo) {
                    $scope.canUndo = true;
                } else {
                    $scope.canUndo = false;
                }
                if (evt.canRedo) {
                    $scope.canRedo = true;
                } else {
                    $scope.canRedo = false;
                }
            }

            function documentSaving(type) {
                $scope.docStatus.saving = true;
                $scope.docStatus.saved = false;
                $scope.docStatus.closed = false;

                if (type === 'meta') {
                    $scope.metaDocStatus.saved = false;
                }

                if ($rootScope.reviewMode) {
                    setReviewModeInterval();
                }
            }

            function documentSaved(type) {
                // if (!$scope.docStatus.updateGene) {
                //     $scope.gene.status_timeStamp.get('lastEdit').value.setText(new Date().getTime().toString());
                //     $scope.gene.status_timeStamp.get('lastEdit').by.setText(Users.getMe().name);
                // }
                $scope.docStatus.saving = false;
                $scope.docStatus.saved = true;
                $scope.docStatus.closed = false;
                $scope.docStatus.updateGene = false;

                if (type === 'meta') {
                    $scope.metaDocStatus.saved = true;
                }
            }

            function documentClosed(type) {
                $scope.docStatus.closed = true;
                $scope.docStatus.saving = false;
                $scope.docStatus.saved = false;
                $scope.fileEditable = false;

                if (type === 'meta') {
                    $scope.metaDocStatus.saved = false;
                }
            }

            function getOncoTreeMainTypes() {
                DatabaseConnector.getOncoTreeMainTypes()
                    .then(function(result) {
                        if (result.data) {
                            $scope.oncoTree.mainTypes = result.data;
                            $scope.oncoTree.mainTypes.push({
                                id: -1,
                                name: 'All Liquid Tumors'
                            });
                            $scope.oncoTree.mainTypes.push({
                                id: -2,
                                name: 'All Solid Tumors'
                            });
                            $scope.oncoTree.mainTypes.push({
                                id: -3,
                                name: 'All Tumors'
                            });
                            $scope.oncoTree.mainTypes.push({
                                id: -4,
                                name: 'Germline Disposition'
                            });
                            $scope.oncoTree.mainTypes.push({
                                id: -5,
                                name: 'All Pediatric Tumors'
                            });
                            $scope.oncoTree.mainTypes.push({
                                id: -5,
                                name: 'Other Tumor Types'
                            });
                            DatabaseConnector.getOncoTreeTumorTypesByMainTypes(_.map(result.data, function(mainType) {
                                return mainType.name;
                            })).then(function(data) {
                                if (_.isObject(data) && _.isArray(data.data)) {
                                    if (data.data.length === result.data.length) {
                                        var tumorTypes = {};
                                        var allTumorTypes = [];
                                        _.each(result.data, function(mainType, i) {
                                            tumorTypes[mainType.name] = data.data[i];
                                            allTumorTypes = _.union(allTumorTypes, data.data[i]);
                                        });
                                        $scope.oncoTree.tumorTypes = tumorTypes;
                                        $scope.oncoTree.allTumorTypes = allTumorTypes;
                                        $scope.meta = {
                                            newCancerTypes: [{
                                                mainType: '',
                                                subtype: '',
                                                oncoTreeTumorTypes: allTumorTypes
                                            }]
                                        };
                                    } else {
                                        console.error('The number of returned tumor types is not matched with number of main types.');
                                    }
                                }
                            }, function() {
                                // TODO: if OncoTree server returns error.
                            });
                        }
                    }, function(error) {
                        console.log(error);
                    });
            }

            function getLevels() {
                var desS = {
                    '': '',
                    '0': $rootScope.meta.levelsDesc['0'],
                    '1': $rootScope.meta.levelsDesc['1'],
                    '2A': $rootScope.meta.levelsDesc['2A'],
                    '2B': $rootScope.meta.levelsDesc['2B'],
                    '3A': $rootScope.meta.levelsDesc['3A'],
                    '3B': $rootScope.meta.levelsDesc['3B'],
                    '4': $rootScope.meta.levelsDesc['4']
                };

                var desR = {
                    '': '',
                    'R1': $rootScope.meta.levelsDesc.R1,
                    'R2': $rootScope.meta.levelsDesc.R2,
                    'R3': $rootScope.meta.levelsDesc.R3
                };

                var levels = {};

                var levelsCategories = {
                    SS: ['', '0', '1', '2A'],
                    SR: ['R1'],
                    IS: ['', '2B', '3A', '3B', '4'],
                    IR: ['R2', 'R3']
                };

                _.each(levelsCategories, function(item, key) {
                    levels[key] = [];
                    for (var i = 0; i < item.length; i++) {
                        var __datum = {};
                        __datum.label = item[i] + (item[i] === '' ? '' : ' - ') + ((['SS', 'IS'].indexOf(key) === -1) ? desR[item[i]] : desS[item[i]]);
                        __datum.value = item[i];
                        levels[key].push(__datum);
                    }
                });
                return levels;
            }

            function GeneStatusSingleton() {
                this.isOpen = false;
                this.hideEmpty = false;
            }

            function containVariantInVUS(variantName) {
                var size = $scope.vus.length;

                for (var i = 0; i < size; i++) {
                    if ($scope.vus.get(i).name.getText() === variantName) {
                        return true;
                    }
                }

                return false;
            }

            function addVUS() {
                var model = $scope.realtimeDocument.getModel();
                var vus;
                if (model.getRoot().get('vus')) {
                    vus = model.getRoot().get('vus');
                } else {
                    vus = model.createList();
                    model.getRoot().set('vus', vus);
                }
                $scope.vus = vus;
            }

            function checkWhetherIsDesiredGene() {
                var _geneName = $scope.gene.name.getText();
                for (var i = 0; i < OncoKB.global.genes.length; i++) {
                    if (OncoKB.global.genes[i].hugoSymbol === _geneName) {
                        $scope.status.isDesiredGene = true;
                        break;
                    }
                }
            }

            $scope.fileTitle = $routeParams.geneName;
            $scope.gene = '';
            $scope.vus = '';
            $scope.comments = '';
            $scope.newGene = {};
            $scope.collaborators = {};
            $scope.checkboxes = {
                oncogenic: ['Yes', 'Likely', 'Likely Neutral', 'Inconclusive'],
                mutationEffect: ['Gain-of-function', 'Likely Gain-of-function', 'Loss-of-function', 'Likely Loss-of-function', 'Switch-of-function', 'Likely Switch-of-function', 'Neutral', 'Likely Neutral', 'Inconclusive'],
                hotspot: ['TRUE', 'FALSE'],
                TSG: ['Tumor Suppressor'],
                OCG: ['Oncogene']
            };
            $scope.nccnDiseaseTypes = ['', 'Acute Lymphoblastic Leukemia', 'Acute Myeloid Leukemia      20th Annual Edition!', 'Anal Carcinoma', 'Bladder Cancer', 'Bone Cancer', 'Breast Cancer', 'Cancer of Unknown Primary (See Occult Primary)', 'Central Nervous System Cancers', 'Cervical Cancer', 'Chronic Myelogenous Leukemia', 'Colon/Rectal Cancer', 'Colon Cancer      20th Annual Edition!', 'Rectal Cancer      20th Annual Edition!', 'Cutaneous Melanoma (See Melanoma)', 'Endometrial Cancer (See Uterine Neoplasms)', 'Esophageal and Esophagogastric Junction Cancers', 'Fallopian Tube Cancer (See Ovarian Cancer)', 'Gastric Cancer', 'Head and Neck Cancers', 'Hepatobiliary Cancers', 'Hodgkin Lymphoma', 'Kidney Cancer', 'Malignant Pleural Mesothelioma', 'Melanoma', 'Multiple Myeloma/Other Plasma Cell Neoplasms', 'Multiple Myeloma', 'Systemic Light Chain Amyloidosis', 'Waldenström\'s Macroglobulinemia / Lymphoplasmacytic Lymphoma', 'Myelodysplastic Syndromes', 'Neuroendocrine Tumors', 'Non-Hodgkin\'s Lymphomas', 'Non-Melanoma Skin Cancers', 'Basal Cell Skin Cancer', 'Dermatofibrosarcoma Protuberans', 'Merkel Cell Carcinoma', 'Squamous Cell Skin Cancer', 'Non-Small Cell Lung Cancer      20th Annual Edition!', 'Occult Primary', 'Ovarian Cancer', 'Pancreatic Adenocarcinoma', 'Penile Cancer', 'Primary Peritoneal Cancer (See Ovarian Cancer)', 'Prostate Cancer      20th Annual Edition!', 'Small Cell Lung Cancer      20th Annual Edition!', 'Soft Tissue Sarcoma', 'Testicular Cancer', 'Thymomas and Thymic Carcinomas', 'Thyroid Carcinoma', 'Uterine Neoplasms'];
            $scope.nccnCategories = [
                {
                    label: '',
                    value: ''
                },
                {
                    label: 'Category 1: Based upon high-level evidence, there is uniform NCCN consensus that the intervention is appropriate.',
                    value: '1'
                },
                {
                    label: 'Category 2A: Based upon lower-level evidence, there is uniform NCCN consensus that the intervention is appropriate.',
                    value: '2A'
                },
                {
                    label: 'Category 2B: Based upon lower-level evidence, there is NCCN consensus that the intervention is appropriate.',
                    value: '2B'
                },
                {
                    label: 'Category 3: Based upon any level of evidence, there is major NCCN disagreement that the intervention is appropriate.',
                    value: '3'
                }
            ];
            $scope.levels = getLevels();
            $scope.fileEditable = false;
            $scope.docStatus = {
                saved: true,
                saving: false,
                closed: false,
                savedGene: true,
                updateGene: false
            };
            $scope.metaDocStatus = {
                saved: true,
                saving: false
            };
            $scope.addMutationPlaceholder = 'Mutation Name';
            $scope.userRole = Users.getMe().role;
            $scope.levelExps = {
                SR: $sce.trustAsHtml('<div><strong>Level R1:</strong> ' + $rootScope.meta.levelsDescHtml.R1 + '.<br/>Example 1: Colorectal cancer with KRAS mutation → resistance to cetuximab<br/>Example 2: EGFR-L858R or exon 19 mutant lung cancers with coincident T790M mutation → resistance to erlotinib</div>'),
                IR: $sce.trustAsHtml('<div><strong>Level R2:</strong> ' + $rootScope.meta.levelsDescHtml.R2 + '.<br/>Example: Resistance to crizotinib in a patient with metastatic lung adenocarcinoma harboring a CD74-ROS1 rearrangement (PMID: 23724914).<br/><strong>Level R3:</strong> ' + $rootScope.meta.levelsDescHtml.R3 + '.<br/>Example: Preclinical evidence suggests that BRAF V600E mutant thyroid tumors are insensitive to RAF inhibitors (PMID: 23365119).<br/></div>')
            };
            $scope.showHideButtons = [
                {key: 'prevelenceShow', display: 'Prevalence'},
                {key: 'proImShow', display: 'Prognostic implications'},
                {key: 'nccnShow', display: 'NCCN guidelines'},
                {
                    key: 'ssShow',
                    display: 'Standard implications for sensitivity to therapy'
                },
                {
                    key: 'srShow',
                    display: 'Standard implications for resistance to therapy'
                },
                {
                    key: 'isShow',
                    display: 'Investigational implications for sensitivity to therapy'
                },
                {
                    key: 'irShow',
                    display: 'Investigational implications for resistance to therapy'
                },
                {key: 'trialsShow', display: 'Ongoing clinical trials'}
            ];
            $scope.list = [];
            $scope.sortableOptions = {
                stop: function(e, ui) {
                    console.log('dropindex', ui.dropindex);
                    console.log('index', ui.index);
                    console.log(e, ui);
                },
                beforeStop: function(e, ui) {
                    console.log('dropindex', ui.dropindex);
                    console.log('index', ui.index);
                    console.log(e, ui);
                }
                // handle: '> .myHandle'
            };
            $scope.selfParams = {};
            $scope.geneStatus = {};
            $scope.oncoTree = {
                mainTypes: [],
                tumorTypes: {}
            };
            $scope.suggestedMutations = [];
            $scope.meta = {
                newCancerTypes: [{
                    mainType: '',
                    subtype: '',
                    oncoTreeTumorTypes: []
                }]
            };
            $scope.status = {
                expandAll: false,
                hideAllEmpty: false,
                rendering: true,
                numAccordion: 0,
                isDesiredGene: false
            };

            $scope.$watch('meta.newCancerTypes', function(n) {
                if (n.length > 0 && (n[n.length - 1].mainType || n[n.length - 1].subtype)) {
                    $scope.meta.newCancerTypes.push({
                        mainType: '',
                        subtype: '',
                        oncoTreeTumorTypes: angular.copy($scope.oncoTree.allTumorTypes)
                    });
                }
                for (var i = n.length - 2; i >= 0; i--) {
                    if (!n[i].mainType && !n[i].subtype) {
                        n.splice(i, 1);
                        i--;
                    }
                }
                function callback(index, result, type) {
                    if (type === 'mainType') {
                        n[index].oncoTreeTumorTypes = result;
                    } else {
                        n[index].mainType = result;
                    }
                    var next = index + 1;
                    if (next < n.length - 1) {
                        if (n[next].subtype) {
                            findMainTypeBySubtype(next, n[next].subtype, callback);
                        } else {
                            findTumorTypeByMainType(next, n[next].mainType, callback);
                        }
                    }
                }

                if (n.length > 1) {
                    if (n[0].subtype) {
                        findMainTypeBySubtype(0, n[0].subtype, callback);
                    } else {
                        findTumorTypeByMainType(0, n[0].mainType, callback);
                    }
                }
            }, true);

            function findTumorTypeByMainType(index, mainType, callback) {
                if (mainType && mainType.name) {
                    if ($scope.oncoTree.tumorTypes.hasOwnProperty(mainType.name)) {
                        if (_.isFunction(callback)) {
                            callback(index, $scope.oncoTree.tumorTypes[mainType.name], 'mainType');
                        }
                    } else {
                        DatabaseConnector.getOncoTreeTumorTypesByMainType(mainType.name)
                            .then(function(result) {
                                if (result.data) {
                                    $scope.oncoTree.tumorTypes[mainType.name] = result.data;
                                    if (_.isFunction(callback)) {
                                        callback(index, result.data, 'mainType');
                                    }
                                }
                            }, function() {
                                if (_.isFunction(callback)) {
                                    callback(index, '', 'mainType');
                                }
                            });
                    }
                } else if (_.isFunction(callback)) {
                    callback(index, '', 'mainType');
                }
            }

            function findMainTypeBySubtype(index, subtype, callback) {
                if (subtype && subtype.mainType && subtype.mainType.name) {
                    var match = -1;
                    for (var i = 0; i < $scope.oncoTree.mainTypes.length; i++) {
                        if ($scope.oncoTree.mainTypes[i].name === subtype.mainType.name) {
                            match = i;
                            break;
                        }
                    }
                    if (_.isFunction(callback)) {
                        callback(index, match > -1 ? $scope.oncoTree.mainTypes[match] : '', 'subtype');
                    }
                } else if (_.isFunction(callback)) {
                    callback(index, '', 'subtype');
                }
            }

            if ($scope.userRole === 8) {
                $scope.status.hideAllObsolete = false;
            } else {
                $scope.status.hideAllObsolete = true;
            }

            $scope.$watch('status.hideAllEmpty', function(n, o) {
                if (n !== o) {
                    if (n) {
                        $scope.isOpenFunc('hideEmpty');
                    } else {
                        $scope.isOpenFunc('showEmpty');
                    }
                }
            });

            $scope.$watch('status.expandAll', function(n, o) {
                if (n !== o) {
                    if (n) {
                        $scope.isOpenFunc('expand');
                    } else {
                        $scope.isOpenFunc('collapse');
                    }
                }
            });

            $scope.$watch('meta.newMainType', function(n) {
                if (_.isArray(n) && n.length > 0) {
                    var _tumorTypes = [];
                    var locks = 0;
                    _.each(n, function(mainType) {
                        if ($scope.oncoTree.tumorTypes.hasOwnProperty(mainType.name)) {
                            _tumorTypes = _.union(_tumorTypes, $scope.oncoTree.tumorTypes[mainType.name]);
                        } else {
                            locks++;
                            DatabaseConnector.getOncoTreeTumorTypesByMainType(mainType.name)
                                .then(function(result) {
                                    if (result.data) {
                                        $scope.oncoTree.tumorTypes[mainType.name] = result.data;
                                        _tumorTypes = _.union(_tumorTypes, result.data);
                                    }
                                    locks--;
                                }, function() {
                                    locks--;
                                });
                        }
                    });
                    var interval = $interval(function() {
                        if (locks === 0) {
                            $scope.meta.currentOncoTreeTumorTypes = _tumorTypes;
                            $interval.cancel(interval);
                        }
                    }, 100);
                }
            });
            getOncoTreeMainTypes();
            $interval(function() {
                storage.requireAuth(true).then(function(result) {
                    if (result && !result.error) {
                        console.log('\t checked token', new Date().getTime(), gapi.auth.getToken());
                    } else {
                        documentClosed();
                        $rootScope.$emit('realtimeDoc.token_refresh_required');
                        console.log('error when renew token in interval func.');
                    }
                });
            }, 600000);

            loadFile()
                .then(function(file) {
                    $scope.realtimeDocument = file;
                    var _documents = Documents.get({title: $scope.fileTitle});
                    if (_.isArray(_documents) && _documents.length > 0) {
                        $scope.document = _documents[0];
                    }

                    if ($scope.fileTitle) {
                        var model = $scope.realtimeDocument.getModel();
                        if (model.getRoot().get('gene')) {
                            var numAccordion = 0;
                            model.getRoot().get('gene').mutations.asArray().forEach(function(mutation) {
                                numAccordion += mutation.tumors.length;
                                mutation.tumors.asArray().forEach(function(tumor) {
                                    numAccordion += 8;
                                    tumor.TI.asArray().forEach(function(ti) {
                                        numAccordion += ti.treatments.length;
                                    });
                                });
                            });
                            console.log(numAccordion);
                            $scope.status.numAccordion = numAccordion;
                            $scope.gene = model.getRoot().get('gene');
                            $scope.model = model;
                            loadMetaFile(afterCreateGeneModel);
                        } else {
                            var gene = model.create('Gene');
                            model.getRoot().set('gene', gene);
                            $scope.gene = gene;
                            $scope.gene.name.setText($scope.fileTitle);
                            $scope.model = model;
                            loadMetaFile(afterCreateGeneModel);
                        }
                    } else {
                        $scope.model = '';
                    }
                    $scope.gene.name_review.addEventListener(gapi.drive.realtime.EventType.VALUE_CHANGED, valueChangedEvent);
                })
                .finally(function() {
                    getSuggestedMutations();
                    if (_.isArray(OncoKB.global.genes)) {
                        checkWhetherIsDesiredGene();
                    } else {
                        DatabaseConnector.getAllGene(function(data) {
                            OncoKB.global.genes = data;
                            checkWhetherIsDesiredGene();
                        });
                    }
                });

            // Token expired, refresh
            $rootScope.$on('realtimeDoc.token_refresh_required', function() {
                var errorMessage = 'An error has occurred. This page will be redirected to Genes page.';
                dialogs.error('Error', errorMessage);
                documentClosed();
                $location.path('/genes');
            });

            // Other unidentify error
            $rootScope.$on('realtimeDoc.other_error', function() {
                var errorMessage = 'An error has occurred. This page will be redirected to Genes page.';
                dialogs.error('Error', errorMessage);
                documentClosed();
                $location.path('/genes');
            });

            // Realtime documet not found
            $rootScope.$on('realtimeDoc.client_error', function() {
                var errorMessage = 'An error has occurred. This page will be redirected to Genes page.';
                dialogs.error('Error', errorMessage);
                documentClosed();
                $location.path('/genes');
            });

            // Realtime documet not found
            $rootScope.$on('realtimeDoc.not_found', function() {
                var errorMessage = 'An error has occurred. This page will be redirected to Genes page.';
                dialogs.error('Error', errorMessage);
                documentClosed();
                $location.path('/genes');
            });

            $scope.$on('interruptedDueToOtherReview', function() {
                // if previously the document is editable, need to notify
                // the current user.
                if ($scope.fileEditable) {
                    dialogs.notify('Warning',
                        $scope.gene.name_review.get('currentReviewer') +
                        ' started to review the document, ' +
                        'you can not change anything at this moment. ' +
                        'We will notify you once the reviewer finished ' +
                        'the editing. Thanks. ' +
                        'Sorry for any inconvinience.');
                }
                $scope.fileEditable = false;
            });

            $scope.$on('startSaveDataToDatabase', function() {
                $scope.status.saveDataToDatabase = true;
                $scope.geneMainDivStyle.opacity = 0.1;
            });

            $scope.$on('doneSaveDataToDatabase', function() {
                $scope.status.saveDataToDatabase = false;
                $scope.geneMainDivStyle.opacity = 1;
            });

            $scope.$on('$locationChangeStart', function() {
                storage.closeDocument();
                documentClosed();
            });
            $window.onbeforeunload = function() {
                // If in the review mode, exit the review mode first then
                // close the tab.
                if ($rootScope.reviewMode) {
                    $scope.exitReview();
                }
            };
        }]
    )
    .controller('ModifyTumorTypeCtrl', function($scope, $modalInstance, data, _, OncoKB) {
        $scope.meta = {
            model: data.model,
            oncoTree: data.oncoTree,
            cancerTypes: data.cancerTypes,
            newCancerTypes: []
        };

        $scope.cancel = function() {
            $modalInstance.dismiss('canceled');
        }; // end cancel

        $scope.save = function() {
            $scope.meta.model.beginCompoundOperation();

            $scope.meta.cancerTypes.clear();
            _.each($scope.meta.newCancerTypes, function(ct) {
                if (ct.mainType.name) {
                    var cancerType = $scope.meta.model.create(OncoKB.CancerType);
                    cancerType.cancerType.setText(ct.mainType.name);
                    if (ct.subtype) {
                        if (ct.subtype.code) {
                            cancerType.oncoTreeCode.setText(ct.subtype.code);
                        }
                        if (ct.subtype.name) {
                            cancerType.subtype.setText(ct.subtype.name);
                        }
                    }
                    cancerType.cancerType_eStatus.set('obsolete', 'false');
                    cancerType.subtype_eStatus.set('obsolete', 'false');
                    cancerType.oncoTreeCode_eStatus.set('obsolete', 'false');
                    console.log(cancerType);
                    $scope.meta.cancerTypes.push(cancerType);
                }
            });

            $scope.meta.model.endCompoundOperation();

            $modalInstance.close();
        }; // end save

        $scope.hitEnter = function(evt) {
            if (angular.equals(evt.keyCode, 13) && !(angular.equals($scope.name, null) || angular.equals($scope.name, ''))) {
                $scope.save();
            }
        };

        $scope.$watch('meta.newCancerTypes', function(n) {
            // console.log('meta.newcancertypes watch has been called.',n, o);
            if (n.length > 0 && (n[n.length - 1].mainType || n[n.length - 1].subtype)) {
                $scope.meta.newCancerTypes.push({
                    mainType: '',
                    subtype: '',
                    oncoTreeTumorTypes: angular.copy($scope.meta.oncoTree.allTumorTypes)
                });
            }
            for (var i = n.length - 2; i >= 0; i--) {
                if (!n[i].mainType) {
                    if (n[i].mainType !== '') {
                        n.splice(i, 1);
                        i--;
                    }
                }
            }
            function callback(index, mainType, subType, oncoTreeTumorTypes) {
                n[index].oncoTreeTumorTypes = oncoTreeTumorTypes ? oncoTreeTumorTypes : $scope.meta.oncoTree.allTumorTypes;

                if (mainType) {
                    n[index].mainType = mainType;
                }

                var next = index + 1;
                if (next < n.length - 1) {
                    findCancerType(next, n[next].mainType, n[next].subtype, callback);
                }
            }

            if (n.length > 1) {
                findCancerType(0, n[0].mainType, n[0].subtype, callback);
            }
        }, true);

        initNewCancerTypes();

        function findCancerType(index, mainType, subtype, callback) {
            var list;
            var _mainType;
            if (mainType && mainType.name) {
                list = $scope.meta.oncoTree.tumorTypes[mainType.name];
            }
            if (!mainType && subtype) {
                _mainType = findMainType(subtype.mainType.name);
            }
            callback(index, _mainType, subtype, list);
        }

        function initNewCancerTypes() {
            var newCancerTypes = [];
            _.each($scope.meta.cancerTypes.asArray(), function(cancerType) {
                var mainType = findMainType(cancerType.cancerType.getText());
                var subtype = findSubtype(cancerType.subtype.getText());
                newCancerTypes.push({
                    mainType: mainType,
                    oncoTreeCode: cancerType.oncoTreeCode.getText(),
                    subtype: subtype,
                    oncoTreeTumorTypes: []
                });
            });

            newCancerTypes.push({
                mainType: '',
                subtype: '',
                oncoTreeTumorTypes: angular.copy($scope.meta.oncoTree.allTumorTypes)
            });
            $scope.meta.newCancerTypes = newCancerTypes;
        }

        function findMainType(name) {
            for (var i = 0; i < $scope.meta.oncoTree.mainTypes.length; i++) {
                if ($scope.meta.oncoTree.mainTypes[i].name === name) {
                    return $scope.meta.oncoTree.mainTypes[i];
                }
            }
            return '';
        }

        function findSubtype(name) {
            for (var i = 0; i < $scope.meta.oncoTree.allTumorTypes.length; i++) {
                if ($scope.meta.oncoTree.allTumorTypes[i].name === name) {
                    return $scope.meta.oncoTree.allTumorTypes[i];
                }
            }
            return '';
        }
    });
