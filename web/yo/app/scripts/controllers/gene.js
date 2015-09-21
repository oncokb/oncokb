'use strict';

/**
 * @ngdoc function
 * @name oncokb.controller:GeneCtrl
 * @description
 * # GeneCtrl
 * Controller of the oncokb
 */
angular.module('oncokbApp')
    .controller('GenesCtrl', ['$scope', '$rootScope', '$location', '$timeout', '$routeParams', '_', 'config', 'importer', 'storage', 'documents', 'users', 'DTColumnDefBuilder', 'DTOptionsBuilder', 'DatabaseConnector', 'OncoKB',
        function ($scope, $rootScope, $location, $timeout, $routeParams, _, config, importer, storage, Documents, users, DTColumnDefBuilder, DTOptionsBuilder, DatabaseConnector, OncoKB) {
            function saveGene(docs, docIndex, excludeObsolete, callback) {
                if (docIndex < docs.length) {
                    var fileId = docs[docIndex].id;
                    storage.getRealtimeDocument(fileId).then(function (realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            console.log(docs[docIndex].title, '\t\t', docIndex);
                            console.log('\t copying');
                            var gene = realtime.getModel().getRoot().get('gene');
                            if (gene) {
                                var geneData = importer.getData(gene, excludeObsolete);
                                DatabaseConnector.updateGene(JSON.stringify(geneData),
                                    function (result) {
                                        console.log('\t success', result);
                                        $timeout(function () {
                                            saveGene(docs, ++docIndex, excludeObsolete, callback);
                                        }, 200, false);
                                    },
                                    function (result) {
                                        console.log('\t failed', result);
                                        $timeout(function () {
                                            saveGene(docs, ++docIndex, excludeObsolete, callback);
                                        }, 200, false);
                                    }
                                );
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function () {
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

            $scope.getDocs = function () {
                var docs = Documents.get();
                if (docs.length > 0) {
                    // $scope.$apply(function() {
                    $scope.documents = Documents.get();
                    $scope.status.rendering = false;
                    // });
                } else {
                    if (OncoKB.global.genes) {
                        storage.requireAuth(true).then(function () {
                            storage.retrieveAllFiles().then(function (result) {
                                Documents.set(result);
                                Documents.setStatus(OncoKB.global.genes);
                                $scope.documents = Documents.get();
                                $scope.status.rendering = false;
                                // loading_screen.finish();
                            });
                        });
                    } else {
                        DatabaseConnector.getAllGene(function (data) {
                            OncoKB.global.genes = data;
                            storage.requireAuth(true).then(function () {
                                storage.retrieveAllFiles().then(function (result) {
                                    Documents.set(result);
                                    Documents.setStatus(OncoKB.global.genes);
                                    $scope.documents = Documents.get();
                                    $scope.status.rendering = false;
                                });
                            });
                        });
                    }
                }
            };

            $scope.backup = function () {
                $scope.status.backup = false;
                importer.backup(function () {
                    $scope.status.backup = true;
                });
            };

            $scope.redirect = function (path) {
                $location.path(path);
            };

            $scope.checkError = function () {
                console.log($rootScope.errors);
            };

            $scope.saveAllGenes = function () {
                $scope.status.saveAllGenes = false;
                saveGene($scope.documents, 0, true, function () {
                    $scope.status.saveAllGenes = true;
                });
            };

            $scope.userRole = users.getMe().role;

            var sorting = [[5, 'desc'], [1, 'asc'], [0, 'asc']];
            if (users.getMe().role === 8) {
                sorting = [[4, 'desc'], [1, 'asc'], [0, 'asc']];
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
            $scope.status = {
                backup: true,
                saveAllGenes: true,
                migrate: true,
                rendering: true
            };
            $scope.getDocs();

            var newGenes = [];

            $scope.migrate = function () {
                //console.log($scope.documents);
                $scope.status.migrate = false;
                importer
                    .migrate()
                    .then(function (result) {
                        if (result && result.error) {
                            $scope.status.migrate = true;
                        } else {
                            $scope.status.migrate = true;
                        }
                    });
            };

            $scope.create = function () {
                createDoc(0);
            };

            $scope.givePermission = function () {
                var testGene = {'test@gmail.com': '  AKT2, AKT3, ERBB3, FGFR1, ERBB2, FGFR3, ERBB4, FGFR2, FGFR4'};
                var genes = [];

                for (var key in testGene) {
                    /* jshint -W083 */
                    var _genes = testGene[key].trim().split(',').map(function (e) {
                        return e.trim();
                    });
                    _genes.forEach(function (_gene) {
                        genes.push({'email': key, 'gene': _gene});
                    });
                    /* jshint +W083 */
                }

                $scope.genesPermissions = genes;
                givePermissionSub(0);
            };

            function givePermissionSub(index) {
                if (index < $scope.genesPermissions.length) {
                    var permission = $scope.genesPermissions[index];
                    console.log(permission.gene, '\t', permission.email);
                    var _docs = Documents.get({title: permission.gene});
                    var _doc = _docs[0];
                    if (_doc.id) {
                        storage.requireAuth().then(function () {
                            storage.getPermission(_doc.id).then(function (result) {
                                if (result.items && angular.isArray(result.items)) {
                                    var permissionIndex = -1;
                                    result.items.forEach(function (permission, _index) {
                                        if (permission.emailAddress && permission.emailAddress === permission.email) {
                                            permissionIndex = _index;
                                        }
                                    });

                                    if (permissionIndex === -1) {
                                        storage.insertPermission(_doc.id, permission.email, 'user', 'writer').then(function (result) {
                                            if (result && result.error) {
                                                console.log('Error when insert permission.');
                                            } else {
                                                console.log('\tinsert writer to', permission.gene);
                                                $timeout(function () {
                                                    givePermissionSub(++index);
                                                }, 100);
                                            }
                                        });
                                    } else if (result.items[permissionIndex].role !== 'writer') {
                                        storage.updatePermission(_doc.id, result.items[permissionIndex].id, 'writer').then(function (result) {
                                            if (result && result.error) {
                                                console.log('Error when update permission.');
                                            } else {
                                                console.log('\tupdat  writer to', permission.gene);
                                                $timeout(function () {
                                                    givePermissionSub(++index);
                                                }, 100);
                                            }
                                        });
                                    }
                                }
                            });
                        });
                    }
                } else {
                    console.info('Done.....');
                }
            }

            $scope.giveFolderPermission = function () {
                var emails = ['cbioportal@gmail.com'];
                var folderId = config.folderId;

                emails.forEach(function (email) {
                    storage.requireAuth(true).then(function () {
                        storage.getDocument(folderId).then(function (e1) {
                            if (e1.id) {
                                storage.getPermission(e1.id).then(function (result) {
                                    if (result.items && angular.isArray(result.items)) {
                                        var permissionIndex = -1;
                                        result.items.forEach(function (permission, _index) {
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

            $scope.initialStatus = function () {
                initial($scope.documents, 0, function () {
                    $scope.status.saveAllGenes = true;
                });
            };

            $scope.removeHotspot = function () {
                removeHotspot($scope.documents, 0, function () {
                });
            };

            $scope.initialHotspot = function () {
                DatabaseConnector.getHotspotList(function (data) {
                    if (data) {
                        initHotspot(data, 0, function () {
                            console.log('finished......');
                        });
                    }
                });
            };

            $scope.initialAutoMutation = function () {
                DatabaseConnector.getAutoMutationList(function (data) {
                    if (data) {
                        initAutoMutation(data, 0, function () {
                            console.log('finished......');
                        });
                    }
                });
            };

            function getAlteration(codon, aa) {
                var alteration = [];
                if (codon) {
                    if (aa) {
                        var variants = aa.split(/\|/);
                        var filters = [];
                        variants.forEach(function (e, i) {
                            var components = e.split(':').map(function (str) {
                                return str.trim();
                            });
                            if (components.length === 2 && Number(components[1]) >= 5) {
                                filters.push(components[0]);
                            }
                        });

                        if (filters.length > 0) {
                            alteration = filters.map(function (e) {
                                return codon + e;
                            });
                        }
                    } else {
                        alteration.push(codon);
                    }
                }
                return alteration;
            }

            function initHotspot(list, listIndex, callback) {
                if (listIndex < list.length) {
                    var hugoSymbol = list[listIndex].hugoSymbol || null;
                    var codon = list[listIndex].codon || null;
                    var aa = list[listIndex].aa || null;
                    var alterations = getAlteration(codon, aa) || null;
                    var pmid = list[listIndex].pmid || null;
                    var qval = Number(list[listIndex].qval) || null;
                    var tumorType = list[listIndex].tumorType || null;
                    //console.log('Got gene symbol.', list[listIndex]);
                    if (hugoSymbol) {
                        var document = Documents.get({title: hugoSymbol});
                        //console.log('Got gene document.', document);

                        if (document instanceof Array && document.length > 0) {
                            if (document.length > 1) {
                                console.log('More than one matched document have been found: ', hugoSymbol);
                            }
                            if (alterations && alterations.length > 0) {
                                storage.getRealtimeDocument(document[0].id).then(function (realtime) {
                                    if (realtime && realtime.error) {
                                        console.log('did not get realtime document.');
                                    } else {
                                        console.log(list[listIndex].hugoSymbol, '\t\t', listIndex + 1);
                                        console.log('\t Initializing hotspot...');
                                        var model = realtime.getModel();
                                        var gene = model.getRoot().get('gene');
                                        var index = -1;
                                        //gene.mutations.clear();

                                        model.beginCompoundOperation();
                                        alterations.forEach(function (alt, i) {
                                            index = -1;
                                            gene.mutations.asArray().forEach(function (e, i) {
                                                if (e.name.getText().toLowerCase() === alt.toLowerCase()) {
                                                    console.log('\t\tAlteration already exists, ignore.' + e.name.getText());
                                                    index = i;
                                                }
                                            });
                                            if (index > -1) {
                                                var _mutation = gene.mutations.get(index);
                                                if (_mutation.oncogenic_eStatus.get('hotspot') === 'TRUE') {
                                                    console.log('\t\t\t\tCONTENT::::');
                                                    console.log('\t\t\t\tmutation effect: ', _mutation.effect.value.getText());
                                                    _mutation.effect.value.setText('');
                                                    console.log('\t\t\t\tmutation effect addon: ', _mutation.effect.addOn.getText());
                                                    _mutation.effect.addOn.setText('');
                                                    console.log('\t\t\t\toncogenic: ', _mutation.oncogenic.getText());
                                                    _mutation.oncogenic.setText('');
                                                    console.log('\t\t\t\tdescription: ', _mutation.description.getText());
                                                    _mutation.description.setText('');

                                                    if (pmid) {
                                                        _mutation.oncogenic_eStatus.set('hotspotAddon', (pmid !== 'mdanderson' ? 'PMID: ' : '') + pmid);
                                                        _mutation.oncogenic_eStatus.set('curated', false);
                                                    } else {
                                                        _mutation.oncogenic_eStatus.set('hotspotAddon', 'This mutated amino acid was identified as a recurrent hotspot (statistical significance, q-value < 0.01) in a set of 11,119 tumor samples of various cancer types (based on Chang M. et al. Nature Biotech. 2015).');
                                                        _mutation.oncogenic_eStatus.set('curated', true);
                                                    }
                                                } else {
                                                    console.log('\t\tThis mutation exists, but has hotspot marked as false change the mutation to hotspot mutation ', alt);

                                                    _mutation.oncogenic_eStatus.set('hotspot', 'TRUE');
                                                    if (pmid) {
                                                        _mutation.oncogenic_eStatus.set('hotspotAddon', (pmid !== 'mdanderson' ? 'PMID: ' : '') + pmid);
                                                    } else {
                                                        _mutation.oncogenic_eStatus.set('hotspotAddon', 'This mutated amino acid was identified as a recurrent hotspot (statistical significance, q-value < 0.01) in a set of 11,119 tumor samples of various cancer types (based on Chang M. et al. Nature Biotech. 2015).');
                                                    }
                                                }
                                            } else {
                                                var _mutation = '';
                                                _mutation = model.create(OncoKB.Mutation);
                                                _mutation.name.setText(alt);
                                                _mutation.oncogenic_eStatus.set('obsolete', 'false');
                                                _mutation.oncogenic_eStatus.set('vetted', 'uv');

                                                //if (qval !== null) {
                                                //    _mutation.oncogenic_eStatus.set('hotspotQvalue', qval);
                                                //}
                                                //if (tumorType !== null) {
                                                //    _mutation.oncogenic_eStatus.set('hotspotTumorType', tumorType);
                                                //}

                                                if (pmid) {
                                                    _mutation.oncogenic_eStatus.set('hotspotAddon', (pmid !== 'mdanderson' ? 'PMID: ' : '') + pmid);
                                                    _mutation.oncogenic_eStatus.set('curated', false);
                                                } else {
                                                    _mutation.oncogenic_eStatus.set('hotspotAddon', 'This mutated amino acid was identified as a recurrent hotspot (statistical significance, q-value < 0.01) in a set of 11,119 tumor samples of various cancer types (based on Chang M. et al. Nature Biotech. 2015).');
                                                    _mutation.oncogenic_eStatus.set('curated', true);
                                                }

                                                gene.mutations.push(_mutation);
                                                console.log('New mutation has been added: ', alt);
                                            }
                                        });
                                        model.endCompoundOperation();
                                        $timeout(function () {
                                            initHotspot(list, ++listIndex, callback);
                                        }, 200, false);
                                    }
                                });
                            } else {
                                console.log('No alteration has been fount on gene: ', hugoSymbol);
                                $timeout(function () {
                                    initHotspot(list, ++listIndex, callback);
                                }, 200, false);
                            }
                        } else {
                            console.log('No document has been fount on gene: ', hugoSymbol);
                            $timeout(function () {
                                initHotspot(list, ++listIndex, callback);
                            }, 200, false);
                        }
                    }

                } else {
                    if (callback) {
                        callback();
                    }
                    console.log('finished.');
                }
            }

            function initAutoMutation(list, listIndex, callback) {
                if (listIndex < list.length) {
                    var hugoSymbol = list[listIndex].hugoSymbol || null;
                    var mutation = list[listIndex].mutation || null;
                    var mutationEffect = list[listIndex].mutationEffect || null;
                    var mutationEffectAddon = list[listIndex].mutationEffectAddon || null;
                    var oncogenic = list[listIndex].oncogenic || null;
                    var curated = list[listIndex].curated || null;
                    var shortDescription = list[listIndex].shortDescription || null;
                    var fullDescription = list[listIndex].fullDescription || null;

                    if (hugoSymbol) {
                        var document = Documents.get({title: hugoSymbol});
                        //console.log('Got gene document.', document);

                        if (document instanceof Array && document.length > 0) {
                            if (document.length > 1) {
                                console.log('More than one matched document have been found: ', hugoSymbol);
                            }
                            if (mutation) {
                                storage.getRealtimeDocument(document[0].id).then(function (realtime) {
                                    if (realtime && realtime.error) {
                                        console.log('did not get realtime document.');
                                    } else {
                                        console.log(list[listIndex].hugoSymbol, '\t\t', listIndex + 1);
                                        console.log('\t Initializing status...');
                                        var model = realtime.getModel();
                                        var gene = model.getRoot().get('gene');
                                        var exists = false;
                                        //gene.mutations.clear();
                                        gene.mutations.asArray().forEach(function (e, i) {
                                            if (e.name.getText().toLowerCase() === mutation.toLowerCase()) {
                                                console.log('\t\tAlteration already exists, ignore.' + e.name.getText());
                                                exists = true;
                                            }
                                        });
                                        if (!exists) {
                                            model.beginCompoundOperation();
                                            var _mutation = '';
                                            _mutation = model.create(OncoKB.Mutation);
                                            _mutation.name.setText(mutation);
                                            _mutation.oncogenic_eStatus.set('obsolete', 'false');
                                            _mutation.oncogenic_eStatus.set('vetted', 'uv');

                                            if (oncogenic) {
                                                _mutation.oncogenic.setText(oncogenic);
                                            }

                                            if (curated !== null) {
                                                if (curated.toLowerCase() === 'false') {
                                                    curated = false;
                                                } else {
                                                    curated = true;
                                                }
                                                _mutation.oncogenic_eStatus.set('curated', curated);
                                            }
                                            if (mutationEffect) {
                                                _mutation.effect.value.setText(mutationEffect);
                                                if (mutationEffectAddon) {
                                                    _mutation.effect.addOn.setText(mutationEffectAddon);
                                                }
                                            }

                                            if (shortDescription) {
                                                _mutation.short.setText(shortDescription);
                                            }

                                            if (fullDescription) {
                                                _mutation.description.setText(fullDescription);
                                            }


                                            gene.mutations.push(_mutation);
                                            model.endCompoundOperation();
                                            console.log('\t\tNew mutation has been added: ', mutation);
                                        }
                                        $timeout(function () {
                                            initAutoMutation(list, ++listIndex, callback);
                                        }, 200, false);
                                    }
                                });
                            } else {
                                console.log('\tNo alteration has been fount on gene: ', hugoSymbol);
                                $timeout(function () {
                                    initAutoMutation(list, ++listIndex, callback);
                                }, 200, false);
                            }
                        } else {
                            console.log('\tNo document has been fount on gene: ', hugoSymbol);
                            $timeout(function () {
                                initAutoMutation(list, ++listIndex, callback);
                            }, 200, false);
                        }
                    }

                } else {
                    if (callback) {
                        callback();
                    }
                    console.log('finished.');
                }
            }

            function initial(docs, docIndex, callback) {
                if (docIndex < docs.length) {
                    var fileId = docs[docIndex].id;
                    storage.getRealtimeDocument(fileId).then(function (realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            realtime.addEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED, function (evt) {
                                if (!evt.isSaving) {
                                    realtime.removeEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED);
                                    storage.closeDocument();
                                    $timeout(function () {
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
                                gene.mutations.asArray().forEach(function (mutation) {
                                    if (!mutation.oncogenic_eStatus.has('obsolete')) {
                                        mutation.oncogenic_eStatus.set('obsolete', 'false');
                                    }
                                    if (!mutation.oncogenic_eStatus.has('hotspot')) {
                                        mutation.oncogenic_eStatus.set('hotspot', 'FALSE');
                                    }
                                    if (!mutation.oncogenic_eStatus.has('curated')) {
                                        mutation.oncogenic_eStatus.set('curated', true);
                                    }
                                    //console.log('Add mutation estatus');
                                    mutation.tumors.asArray().forEach(function (tumor) {
                                        if (!tumor.prevalence_eStatus.has('obsolete')) {
                                            tumor.prevalence_eStatus.set('obsolete', 'false');
                                        }
                                        if (!tumor.prevalence_eStatus.has('hotspot')) {
                                            tumor.prevalence_eStatus.set('hotspot', 'FALSE');
                                        }
                                        if (!tumor.progImp_eStatus.has('obsolete')) {
                                            tumor.progImp_eStatus.set('obsolete', 'false');
                                        }
                                        if (!tumor.nccn_eStatus.has('hotspot')) {
                                            tumor.nccn_eStatus.set('hotspot', 'FALSE');
                                        }

                                        //console.log('Add tumor estatus');
                                        tumor.TI.asArray().forEach(function (ti) {
                                            ti.treatments.asArray().forEach(function (treatment) {
                                                if (!treatment.name_eStatus.has('obsolete')) {
                                                    treatment.name_eStatus.set('obsolete', 'false');
                                                }
                                                if (!treatment.name_eStatus.has('hotspot')) {
                                                    treatment.name_eStatus.set('hotspot', 'FALSE');
                                                }
                                                //console.log('Add treatment estatus');
                                            })
                                        });
                                    });
                                });
                                model.endCompoundOperation();
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function () {
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

            function removeHotspot(docs, docIndex, callback) {
                if (docIndex < docs.length) {
                    var fileId = docs[docIndex].id;
                    storage.getRealtimeDocument(fileId).then(function (realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            realtime.addEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED, function (evt) {
                                if (!evt.isSaving) {
                                    realtime.removeEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED);
                                    storage.closeDocument();
                                    $timeout(function () {
                                        removeHotspot(docs, ++docIndex, callback);
                                    }, 200, false);
                                }
                            });
                            console.log(docs[docIndex].title, '\t\t', docIndex + 1);
                            console.log('\t Removing hotspot...');
                            var model = realtime.getModel();
                            var gene = model.getRoot().get('gene');
                            if (gene) {
                                var removeIndice = [];
                                model.beginCompoundOperation();
                                gene.mutations.asArray().forEach(function (mutation, index) {
                                    if (mutation.oncogenic_eStatus.has('hotspot')) {
                                        if (mutation.oncogenic_eStatus.get('hotspot') === 'TRUE') {
                                            console.log('\t\tMutation: ', mutation.name.getText());
                                            if (mutation.tumors.length === 0 && mutation.effect.value.getText().trim() === '' && mutation.effect.addOn.getText().trim() === '' && mutation.oncogenic.getText().trim() === '' && mutation.short.getText().trim() === '' && mutation.description.getText().trim() === '') {
                                                removeIndice.push(index);
                                                console.log('\t\t\tFound empty hotspot mutation.');
                                            } else {
                                                console.log('\t\t\tHotspot mutation, but has content in it');
                                                console.log('\t\t\t\tCONTENT::::');
                                                console.log('\t\t\t\tNumber of tumors:', mutation.tumors.length);
                                                console.log('\t\t\t\tmutation effect: "' + mutation.effect.value.getText() + '"');
                                                console.log('\t\t\t\tmutation effect addon: "' + mutation.effect.addOn.getText() + '"');
                                                console.log('\t\t\t\toncogenic: "' + mutation.oncogenic.getText() + '"');
                                                console.log('\t\t\t\tShort description: "' + mutation.short.getText() + '"');
                                                console.log('\t\t\t\tdescription: "' + mutation.description.getText() + '"');

                                                if (mutation.oncogenic_eStatus.has('hotspot')) {
                                                    console.log('\t\t\t\t\tRemove hotspot.', mutation.oncogenic_eStatus.get('hotspot'));
                                                    mutation.oncogenic_eStatus.delete('hotspot');
                                                }
                                                if (mutation.oncogenic_eStatus.has('hotspotQvalue')) {
                                                    console.log('\t\t\t\t\tRemove hotspot qvalue.', mutation.oncogenic_eStatus.get('hotspotQvalue'));
                                                    mutation.oncogenic_eStatus.delete('hotspotQvalue');
                                                }
                                                if (mutation.oncogenic_eStatus.has('hotspotTumorType')) {
                                                    console.log('\t\t\t\t\tRemove hotspot tumor type.', mutation.oncogenic_eStatus.get('hotspotTumorType'));
                                                    mutation.oncogenic_eStatus.delete('hotspotTumorType');
                                                }
                                                if (mutation.oncogenic_eStatus.has('hotspotAddon')) {
                                                    console.log('\t\t\t\t\tRemove hotspot addon.', mutation.oncogenic_eStatus.get('hotspotAddon'));
                                                    mutation.oncogenic_eStatus.delete('hotspotAddon');
                                                }
                                            }
                                        }
                                    }

                                    mutation.tumors.asArray().forEach(function (tumor) {
                                        if (tumor.prevalence_eStatus.has('hotspot')) {
                                            tumor.prevalence_eStatus.delete('hotspot');
                                        }
                                        if (tumor.progImp_eStatus.has('hotspot')) {
                                            tumor.progImp_eStatus.set('hotspot');
                                        }
                                        if (tumor.nccn_eStatus.has('hotspot')) {
                                            tumor.nccn_eStatus.set('hotspot');
                                        }

                                        //console.log('Add tumor estatus');
                                        tumor.TI.asArray().forEach(function (ti) {
                                            ti.treatments.asArray().forEach(function (treatment) {
                                                if (treatment.name_eStatus.has('hotspot')) {
                                                    treatment.name_eStatus.delete('hotspot');
                                                }
                                            })
                                        });
                                    });
                                });
                                removeIndice.sort(function (a, b) {
                                    return b - a;
                                });
                                removeIndice.forEach(function (index) {
                                    gene.mutations.remove(index);
                                });
                                console.log(removeIndice);
                                model.endCompoundOperation();
                                //$timeout(function () {
                                //    removeHotspot(docs, ++docIndex, callback);
                                //}, 200, false);
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function () {
                                    removeHotspot(docs, ++docIndex, callback);
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
                    storage.requireAuth().then(function () {
                        console.log(index, ' -> Creating', newGenes[index]);
                        // storage.createDocument(newGenes[index], '0BzBfo69g8fP6Mnk3RjVrZ0pJX3M').then(function (file) {
                        storage.createDocument(newGenes[index]).then(function (result) {
                            if (result && result.error) {
                                console.log('Error when creating docuemnt.');
                            } else {
                                $timeout(function () {
                                    createDoc(++index);
                                }, 2000);
                            }
                        });
                    });
                } else {
                    console.log('finished');
                }
            }
        }]
)
    .controller('GeneCtrl', ['_', 'S', '$resource', '$interval', '$timeout', '$scope', '$rootScope', '$location', '$route', '$routeParams', 'dialogs', 'importer', 'driveOncokbInfo', 'storage', 'loadFile', 'user', 'users', 'documents', 'OncoKB', 'gapi', 'DatabaseConnector', 'SecretEmptyKey', 'jspdf',
        function (_, S, $resource, $interval, $timeout, $scope, $rootScope, $location, $route, $routeParams, dialogs, importer, DriveOncokbInfo, storage, loadFile, User, Users, Documents, OncoKB, gapi, DatabaseConnector, SecretEmptyKey, jspdf) {
            $scope.test = function (event, a, b, c, d, e, f, g) {
                $scope.stopCollopse(event);
                console.log(a, b, c, d, e, f, g);
            };
            $scope.authorize = function () {
                storage.requireAuth(false).then(function () {
                    var target = $location.search().target;
                    if (target) {
                        $location.url(target);
                    } else {
                        storage.getDocument('1rFgBCL0ftynBxRl5E6mgNWn0WoBPfLGm8dgvNBaHw38').then(function (file) {
                            storage.downloadFile(file).then(function (text) {
                                $scope.curateFile = text;
                            });
                        });
                    }
                });
            };

            $scope.addMutation = function (newMutationName) {
                if (this.gene && newMutationName) {
                    newMutationName = newMutationName.toString().trim();
                    var exists = false;
                    this.gene.mutations.asArray().forEach(function (e) {
                        if (e.name.getText().toLowerCase() === newMutationName.toLowerCase()) {
                            exists = true;
                        }
                    });

                    if (exists) {
                        dialogs.notify('Warning', 'Mutation exists.');
                    } else {
                        var _mutation = '';
                        $scope.realtimeDocument.getModel().beginCompoundOperation();
                        _mutation = $scope.realtimeDocument.getModel().create(OncoKB.Mutation);
                        _mutation.name.setText(newMutationName);
                        _mutation.oncogenic_eStatus.set('obsolete', 'false');
                        _mutation.oncogenic_eStatus.set('hotspot', 'FALSE');

                        this.gene.mutations.push(_mutation);
                        $scope.realtimeDocument.getModel().endCompoundOperation();
                        $scope.geneStatus[this.gene.mutations.length - 1] = new GeneStatusSingleton();
                        sendEmail(this.gene.name.text + ': new MUTATION added -> ' + newMutationName, ' ');
                    }
                }
            };

            $scope.stateComparator = function (state, viewValue) {
                return viewValue === SecretEmptyKey || ('' + state).toLowerCase().indexOf(('' + viewValue).toLowerCase()) > -1;
            };

            $scope.getComments = function () {
                console.log($scope.comments);
            };

            $scope.addComment = function (object, key, string) {
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

            $scope.getData = function () {
                var gene = importer.getData(this.gene);
                console.log(gene);
            };

            $scope.updateGene = function () {
                $scope.docStatus.savedGene = false;

                var gene = importer.getData(this.gene, true);

                console.log(gene);
                // $timeout(function(){
                DatabaseConnector.updateGene(JSON.stringify(gene), function (result) {
                    $scope.docStatus.savedGene = true;
                    console.log('success', result);
                    changeLastUpdate();
                }, function (result) {
                    $scope.docStatus.savedGene = true;
                    console.log('failed', result);
                    changeLastUpdate();
                });
                // }, 1000);
            };

            function changeLastUpdate() {
                if (!$scope.gene.status_timeStamp.has('lastUpdate')) {
                    var timeStamp;
                    $scope.realtimeDocument.getModel().beginCompoundOperation();
                    timeStamp = $scope.realtimeDocument.getModel().create('TimeStamp');
                    timeStamp.value.setText(new Date().getTime().toString());
                    timeStamp.by.setText(Users.getMe().name);
                    $scope.gene.status_timeStamp.set('lastUpdate', timeStamp);
                    $scope.realtimeDocument.getModel().endCompoundOperation();

                } else {
                    $scope.gene.status_timeStamp.get('lastUpdate').value.setText(new Date().getTime().toString());
                    $scope.gene.status_timeStamp.get('lastUpdate').by.setText(Users.getMe().name);
                }
                $scope.docStatus.updateGene = true;
            }

            $scope.addTumorType = function (mutation, newTumorTypeName, mutationIndex) {
                if (mutation && newTumorTypeName) {
                    var _tumorType = '';
                    var exists = false;
                    newTumorTypeName = newTumorTypeName.toString().trim();

                    mutation.tumors.asArray().forEach(function (e) {
                        if (e.name.getText().toLowerCase() === newTumorTypeName.toLowerCase()) {
                            exists = true;
                        }
                    });

                    if (exists) {
                        dialogs.notify('Warning', 'Tumor type exists.');
                    } else {
                        $scope.realtimeDocument.getModel().beginCompoundOperation();
                        _tumorType = $scope.realtimeDocument.getModel().create(OncoKB.Tumor);
                        _tumorType.name.setText(newTumorTypeName);
                        _tumorType.nccn.category.setText('2A');
                        for (var i = 0; i < 4; i++) {
                            var __ti = $scope.realtimeDocument.getModel().create(OncoKB.TI);
                            var __status = i < 2 ? 1 : 0; // 1: Standard, 0: Investigational
                            var __type = i % 2 === 0 ? 1 : 0; //1: sensitivity, 0: resistance
                            var __name = (__status ? 'Standard' : 'Investigational') + ' implications for ' + (__type ? 'sensitivity' : 'resistance') + ' to therapy';

                            __ti.types.set('status', __status.toString());
                            __ti.types.set('type', __type.toString());
                            __ti.name.setText(__name);
                            _tumorType.TI.push(__ti);
                        }
                        mutation.tumors.push(_tumorType);
                        $scope.realtimeDocument.getModel().endCompoundOperation();
                        $scope.geneStatus[mutationIndex][mutation.tumors.length - 1] = new GeneStatusSingleton();
                        sendEmail(this.gene.name.text + ',' + mutation.name.text + ' new TUMOR TYPE added -> ' + newTumorTypeName, ' ');
                    }
                }
            };

            //Add new therapeutic implication
            $scope.addTI = function (ti, index, newTIName, mutationIndex, tumorIndex, tiIndex) {
                if (ti && newTIName) {
                    var _treatment = '';
                    var exists = false;
                    newTIName = newTIName.toString().trim();

                    ti.treatments.asArray().forEach(function (e) {
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

            $scope.onFocus = function (e) {
                $timeout(function () {
                    /* jshint -W117 */
                    $(e.target).trigger('input');
                    $(e.target).trigger('change'); // for IE
                    /* jshint +W117 */
                });
            };

            //Add new therapeutic implication
            $scope.addTrial = function (trials, newTrial) {
                if (trials && newTrial) {
                    if (trials.indexOf(newTrial) === -1) {
                        if (newTrial.match(/NCT[0-9]+/ig)) {
                            trials.push(newTrial);
                        } else {
                            dialogs.notify('Warning', 'Please check your trial ID format. (e.g. NCT01562899)');
                        }
                    } else {
                        dialogs.notify('Warning', 'Trial exists.');
                    }
                }
            };

            $scope.cleanTrial = function (trials) {
                var cleanTrials = {};
                trials.asArray().forEach(function (e, index) {
                    if (cleanTrials.hasOwnProperty(e)) {
                        cleanTrials[e].push(index);
                    } else {
                        cleanTrials[e] = [];
                    }
                });
                /*jshint -W083 */
                for (var key in cleanTrials) {
                    if (cleanTrials[key].length > 0) {
                        cleanTrials[key].forEach(function () {
                            trials.removeValue(key);
                        });
                    }
                }
                /*jshint +W083 */
                console.log(cleanTrials);
            };

            $scope.addTrialStr = function (trials) {
                if (trials && this.trialsStr) {
                    var _trials = this.trialsStr.split(/\s+/);
                    _trials.forEach(function (e) {
                        if (trials.indexOf(e) === -1) {
                            trials.push(e);
                        }
                    });
                    this.trialsStr = '';
                }
            };

            $scope.checkScope = function () {
                console.log($scope.gene);
                //console.log($scope.gene.mutations.get(0).tumors.get(0));
                console.log($scope.geneStatus);

                console.log('Num of watchers: ' + checkNumWatchers());
                console.log($scope.gene.status_timeStamp.get('lastEdit').value);
                console.log($scope.gene.status_timeStamp.get('lastUpdate').value);

                $scope.gene.mutations.asArray().forEach(function (e) {
                    console.log('------------------');
                    console.log(e);
                    console.log(e.oncogenic_eStatus);
                    console.log(e.oncogenic_eStatus.get('curated'));
                    if (e.oncogenic_eStatus.has('hotspotQvalue')) {
                        console.log(e.oncogenic_eStatus.get('hotspotQvalue'));
                    }
                    if (e.oncogenic_eStatus.has('hotspotTumorType')) {
                        console.log(e.oncogenic_eStatus.get('hotspotTumorType'));
                    }
                    console.log(e.effect);
                    console.log(e.oncogenic);
                    console.log(e.description);
                    console.log('------------------');
                });
            };

            $scope.updateGeneColor = function () {
                if ($scope.gene) {
                    if (Number($scope.gene.status_timeStamp.get('lastEdit').value.text) > Number($scope.gene.status_timeStamp.get('lastUpdate').value.text)) {
                        return 'red';
                    } else {
                        return 'black';
                    }
                }
            };

            $scope.remove = function (event, mutationIndex, tumorTypeIndex, therapyCategoryIndex, therapyIndex) {
                $scope.stopCollopse(event);
                var dlg = dialogs.confirm('Confirmation', 'Are you sure you want to delete this entry?');
                dlg.result.then(function () {
                    var _index = -1;
                    console.log(mutationIndex, tumorTypeIndex, therapyCategoryIndex, therapyIndex);
                    if (angular.isNumber(mutationIndex)) {
                        if (!isNaN(mutationIndex)) {
                            if (isNaN(tumorTypeIndex)) {
                                _index = Number(angular.copy(mutationIndex));
                                $scope.gene.mutations.remove(_index);
                                delete $scope.geneStatus[mutationIndex];
                                $scope.geneStatus = migrateGeneStatusPosition($scope.geneStatus, _index);
                            } else {
                                if (!isNaN(therapyCategoryIndex) && !isNaN(therapyIndex)) {
                                    _index = Number(angular.copy(therapyIndex));
                                    $scope.gene.mutations.get(mutationIndex).tumors.get(tumorTypeIndex).TI.get(therapyCategoryIndex).treatments.remove(therapyIndex);
                                    delete $scope.geneStatus[mutationIndex][tumorTypeIndex][therapyCategoryIndex][_index];
                                    $scope.geneStatus[mutationIndex][tumorTypeIndex][therapyCategoryIndex] = migrateGeneStatusPosition($scope.geneStatus[mutationIndex][tumorTypeIndex][therapyCategoryIndex], _index);
                                } else {
                                    _index = Number(angular.copy(tumorTypeIndex));
                                    $scope.gene.mutations.get(mutationIndex).tumors.remove(_index);
                                    delete $scope.geneStatus[mutationIndex][_index];
                                    $scope.geneStatus[mutationIndex] = migrateGeneStatusPosition($scope.geneStatus[mutationIndex], _index);
                                }
                            }
                        }
                    } else {
                        mutationIndex.remove(tumorTypeIndex);
                    }
                }, function () {
                });
            };

            $scope.commentClick = function (event) {
                $scope.stopCollopse(event);
            };

            $scope.redo = function () {
                $scope.model.redo();
                regenerateGeneStatus();
            };

            $scope.undo = function () {
                $scope.model.undo();
                regenerateGeneStatus();
            };

            $scope.curatorsName = function () {
                return this.gene.curators.asArray().map(function (d) {
                    return d.name;
                }).join(', ');
            };

            $scope.curatorsEmail = function () {
                return this.gene.curators.asArray().map(function (d) {
                    return d.email;
                }).join(', ');
            };

            $scope.removeCurator = function (index) {
                $scope.gene.curators.remove(index);
            };

            $scope.checkTI = function (TI, status, type) {
                var _status = TI.types.get('status').toString();
                var _type = TI.types.get('type').toString();
                status = status.toString();
                type = type.toString();
                if (_status === status && _type === type) {
                    return true;
                } else {
                    return false;
                }
            };

            $scope.mutationEffectChanged = function (mutationEffect) {
                mutationEffect.addOn.setText('');
            };

            $scope.move = function (driveList, index, moveIndex, event) {
                var tmpStatus;
                var moveStatusIndex;
                var indexes = [];
                var geneStatus = angular.copy($scope.geneStatus);
                var key, numKey;
                $scope.stopCollopse(event);

                index = parseInt(index);
                moveIndex = parseInt(moveIndex);

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
                    indexes.sort(function (a, b) {
                        return a - b;
                    }).forEach(function (e) {
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
                    indexes.sort(function (a, b) {
                        return b - a;
                    }).forEach(function (e) {
                        geneStatus[e + 1] = geneStatus[e];
                    });
                }

                geneStatus[moveStatusIndex] = tmpStatus;

                $scope.geneStatus = geneStatus;

                driveList.move(index, moveIndex);
            };

            $scope.stopCollopse = function (event) {
                if (event.stopPropagation) {
                    event.stopPropagation();
                }
                if (event.preventDefault) {
                    event.preventDefault();
                }
            };

            $scope.setGeneStatus = function () {
                var newStatus = {
                    geneId: $scope.gene.name.text,
                    status: $scope.gene.status.text
                };
                Documents.updateStatus(newStatus);
                DatabaseConnector.setGeneStatus(newStatus).then(function (result) {
                    if (result && result.error) {
                        console.error(result);
                    } else {
                        console.info(result);
                    }
                });
            };

            $scope.generatePDF = function () {
                jspdf.create(importer.getData(this.gene, true));
            };

            $scope.isOpenFunc = function (type) {
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

                //for: mutation
                for (var key in geneStatus) {
                    if (!isNaN(key)) {
                        geneStatus[key][processKey] = targetStatus;
                    }

                    //for: tumor type
                    for (var _key in geneStatus[key]) {
                        //for: therapeutic implications
                        if (specialEscapeKeys.indexOf(_key) === -1) {
                            flag = targetStatus;
                            if (isNaN(_key) && flag) {
                                flag = processKey === 'isOpen' ? ($scope.gene.mutations.get(Number(key))[_key].text ? targetStatus : false) : targetStatus;
                            }
                            geneStatus[key][_key][processKey] = flag;


                            for (var __key in geneStatus[key][_key]) {
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
                                            //for: treatments
                                            geneStatus[key][_key][__key][processKey] = flag;
                                            for (var ___key in geneStatus[key][_key][__key]) {
                                                if (specialEscapeKeys.indexOf(___key) === -1) {
                                                    geneStatus[key][_key][__key][___key][processKey] = flag;
                                                }
                                            }
                                        } else if ($scope.gene.mutations.get(Number(key)).tumors.get(Number(_key)).TI.get(Number(__key)).description.text) {
                                            geneStatus[key][_key][__key][processKey] = flag;
                                        } else {
                                            if (processKey === 'isOpen') {
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

            $scope.changeIsOpen = function (target) {
                target = !target;
            };

            $scope.mutationEffectEmpty = function (mutation) {
                if (mutation.oncogenic.text === '' && mutation.effect.value.text === '' && mutation.description.text === '') {
                    return true;
                } else {
                    return false;
                }
            };

            $scope.hasNccn = function (nccn) {
                if (nccn) {
                    if (nccn.disease.text && nccn.disease.text !== 'NA') {
                        return true;
                    }
                }
                return false;
            };

            $scope.curatedIconClick = function (event, status) {
                $scope.stopCollopse(event);
                status.set('curated', !status.get('curated'));
            };

            function regenerateGeneStatus() {
                var geneStatus = {};
                var mutationKeys = ['oncogenic'];
                var tumorKeys = ['prevalence', 'progImp', 'nccn', 'trials'];

                $scope.gene.mutations.asArray().forEach(function (mutation, mutationIndex) {
                    geneStatus[mutationIndex] = new GeneStatusSingleton();
                    mutationKeys.forEach(function (key) {
                        if (mutation[key]) {
                            geneStatus[mutationIndex][key] = new GeneStatusSingleton();
                        }
                    });

                    if (mutation.tumors.length > 0) {
                        mutation.tumors.asArray().forEach(function (tumor, tumorIndex) {
                            geneStatus[mutationIndex][tumorIndex] = new GeneStatusSingleton();
                            tumorKeys.forEach(function (key) {
                                if (tumor[key]) {
                                    geneStatus[mutationIndex][tumorIndex][key] = new GeneStatusSingleton();
                                }
                                tumor.TI.asArray(function (therapyType, therapyTypeIndex) {
                                    geneStatus[mutationIndex][tumorIndex][therapyTypeIndex] = new GeneStatusSingleton();
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

                    indexes.sort(function (a, b) {
                        return a - b;
                    }).forEach(function (e) {
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

                var f = function (element) {
                    angular.forEach(['$scope', '$isolateScope'], function (scopeProperty) {
                        if (element.data() && element.data().hasOwnProperty(scopeProperty)) {
                            angular.forEach(element.data()[scopeProperty].$$watchers, function (watcher) {
                                watchers.push(watcher);
                            });
                        }
                    });

                    angular.forEach(element.children(), function (childElement) {
                        f(angular.element(childElement));
                    });
                };

                f(root);

                // Remove duplicate watchers
                var watchersWithoutDuplicates = [];
                angular.forEach(watchers, function (item) {
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
                        function (result) {
                            console.log('success', result);
                        },
                        function (result) {
                            console.log('failed', result);
                        }
                    );
                }
            }

            function getDriveOncokbInfo() {
                var pubMedLinks = DriveOncokbInfo.getPubMed({gene: $scope.fileTitle});
                var pubMedLinksLength = 0;
                $scope.suggestedMutations = DriveOncokbInfo.getMutation($scope.fileTitle) || [];
                if ($scope.suggestedMutations.length === 0) {
                    $scope.addMutationPlaceholder = 'Based on our search criteria no hotspot mutation found. Please curate according to literature.';
                }

                $scope.pubMedLinks = {
                    gene: pubMedLinks.gene.pubMedLinks || [],
                    mutations: pubMedLinks.mutations.pubMedMutationLinks || {}
                };

                for (var key in $scope.pubMedLinks.mutations) {
                    pubMedLinksLength += $scope.pubMedLinks.mutations[key].length;
                }
                $scope.pubMedMutationLength = Object.keys($scope.pubMedLinks.mutations).length;
                $scope.pubMedMutationLinksLength = pubMedLinksLength;
                $scope.pubMedLinksLength = pubMedLinksLength + $scope.pubMedLinks.gene.length;
            }

            function bindDocEvents() {
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.COLLABORATOR_JOINED, displayCollaboratorEvent);
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.COLLABORATOR_LEFT, displayCollaboratorEvent);
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED, saveStateChangedEvent);
                $scope.model.addEventListener(gapi.drive.realtime.EventType.UNDO_REDO_STATE_CHANGED, onUndoStateChanged);
                $scope.gene.addEventListener(gapi.drive.realtime.EventType.VALUE_CHANGED, valueChangedEvent);
            }

            function saveStateChangedEvent(evt) {
                if (!$scope.$$phase) {
                    $scope.$apply(function () {
                        updateDocStatus(evt);
                    });
                } else {
                    updateDocStatus(evt);
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

            function afterCreateGeneModel() {
                var file = Documents.get({title: $scope.fileTitle});
                var timeStamp;
                file = file[0];
                if (!$scope.gene.status_timeStamp.has('lastEdit')) {
                    $scope.realtimeDocument.getModel().beginCompoundOperation();
                    timeStamp = $scope.realtimeDocument.getModel().create('TimeStamp');
                    timeStamp.value.setText(new Date().getTime().toString());
                    timeStamp.by.setText(Users.getMe().name);
                    $scope.gene.status_timeStamp.set('lastEdit', timeStamp);
                    $scope.realtimeDocument.getModel().endCompoundOperation();
                }
                if (!$scope.gene.status_timeStamp.has('lastUpdate')) {
                    $scope.realtimeDocument.getModel().beginCompoundOperation();
                    timeStamp = $scope.realtimeDocument.getModel().create('TimeStamp');
                    timeStamp.value.setText(new Date().getTime().toString());
                    timeStamp.by.setText(Users.getMe().name);
                    $scope.gene.status_timeStamp.set('lastUpdate', timeStamp);
                    $scope.realtimeDocument.getModel().endCompoundOperation();
                }
                $scope.document = file;
                $scope.fileEditable = file.editable ? true : false;
                $scope.status.rendering = false;
                displayAllCollaborators($scope.realtimeDocument, bindDocEvents);
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

            function addCollaborator(user) {
                if (!$scope.collaborators.hasOwnProperty(user.userId)) {
                    $scope.collaborators[user.sessionId] = {};
                }
                $scope.collaborators[user.sessionId] = user;
            }

            function removeCollaborator(user) {
                if (!$scope.collaborators.hasOwnProperty(user.sessionId)) {
                    console.log('Unknown collaborator:', user);
                } else {
                    delete $scope.collaborators[user.sessionId];
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
                    storage.getUserInfo(_user.userId).then(function (userInfo) {
                        User.name = userInfo.displayName;
                        User.email = angular.isArray(userInfo.emails) ? (userInfo.emails.length > 0 ? userInfo.emails[0].value : 'N/A') : userInfo.emails;
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

            function documentSaving() {
                $scope.docStatus.saving = true;
                $scope.docStatus.saved = false;
                $scope.docStatus.closed = false;
            }

            function documentSaved() {
                if (!$scope.docStatus.updateGene) {
                    $scope.gene.status_timeStamp.get('lastEdit').value.setText(new Date().getTime().toString());
                    $scope.gene.status_timeStamp.get('lastEdit').by.setText(Users.getMe().name);
                }
                $scope.docStatus.saving = false;
                $scope.docStatus.saved = true;
                $scope.docStatus.closed = false;
                $scope.docStatus.updateGene = false;
            }

            function documentClosed() {
                $scope.docStatus.closed = true;
                $scope.docStatus.saving = false;
                $scope.docStatus.saved = false;
                $scope.fileEditable = false;
            }

            function getOncoTreeTumortypes() {
                $scope.tumorTypes = ['Adrenocortical Carcinoma',
                    'Pheochromocytoma',
                    'Ampullary Carcinoma',
                    'Biliary Cancer',
                    'Bladder Cancer',
                    'Blastic Plasmacytoid Dendritic Cell Neoplasm',
                    'Histiocytosis',
                    'Leukemia',
                    'Multiple Myeloma',
                    'Myelodysplasia',
                    'Myeloproliferative Neoplasm',
                    'Chondroblastoma',
                    'Chondrosarcoma',
                    'Chordoma',
                    'Ewing Sarcoma',
                    'Giant Cell Tumor',
                    'Osteosarcoma',
                    'Anal Cancer',
                    'Melanoma',
                    'Appendiceal Cancer',
                    'Colorectal Cancer',
                    'Gastrointestinal Neuroendocrine Tumor',
                    'Small Bowel Cancer',
                    'Diffuse Glioma',
                    'Encapsulated Glioma',
                    'Ependymomal Tumor',
                    'Miscellaneous Neuroepithelial Tumor',
                    'Meningothelial Tumor',
                    'Embryonal Tumor',
                    'Sellar Tumor',
                    'Nerve Sheath Tumor',
                    'Choroid Plexus Tumor',
                    'Pineal Tumor',
                    'Germ Cell Tumor',
                    'Miscellaneous Brain Tumor',
                    'Breast Carcinoma',
                    'Cervical Cancer',
                    'Esophagogastric Carcinoma',
                    'Retinoblastoma',
                    'Head and Neck Carcinoma',
                    'Renal Cell Carcinoma',
                    'Wilms Tumor',
                    'Hepatocellular Carcinoma',
                    'Hodgkin\'s Lymphoma',
                    'Non-Hodgkin’s Lymphoma',
                    'Lung cancer',
                    'Mesothelioma',
                    'Ovarian Cancer',
                    'Pancreatic Cancer',
                    'Penile Cancer',
                    'Prostate Cancer',
                    'Skin Cancer, Non-Melanoma',
                    'Soft Tissue Sarcoma',
                    'Gastrointestinal Stromal Tumor',
                    'Thymic Tumor',
                    'Thyroid Cancer',
                    'Gestational Trophoblastic Disease',
                    'Endometrial Cancer',
                    'Uterine Sarcoma',
                    'Vulvar Carcinoma',
                    'Cancer of Unknown Primary',
                    'Mixed Cancer Types',
                    'Unknown Cancer Type'];
            }

            function getLevels() {
                var desS = {
                    '': '',
                    '0': 'FDA-approved drug in this indication irrespective of gene/variant biomarker.',
                    '1': 'FDA-approved biomarker and drug association in this indication.',
                    '2A': 'FDA-approved biomarker and drug association in another indication, and NCCN-compendium listed for this indication.',
                    '2B': 'FDA-approved biomarker in another indication, but not FDA or NCCN-compendium-listed for this indication.',
                    '3': 'Clinical evidence links this biomarker to drug response but no FDA-approved or NCCN compendium-listed biomarker and drug association.',
                    '4': 'Preclinical evidence potentially links this biomarker to response but no FDA-approved or NCCN compendium-listed biomarker and drug association.'
                };

                var desR = {
                    '': '',
                    'R1': 'NCCN-compendium listed biomarker for resistance to a FDA-approved drug.',
                    'R2': 'Not NCCN compendium-listed biomarker, but clinical evidence linking this biomarker to drug resistance.',
                    'R3': 'Not NCCN compendium-listed biomarker, but preclinical evidence potentially linking this biomarker to drug resistance.'
                };

                var levels = {};

                var levelsCategories = {
                    SS: ['', '0', '1', '2A'],
                    SR: ['R1'],
                    IS: ['', '2B', '3', '4'],
                    IR: ['R2', 'R3']
                };

                for (var key in levelsCategories) {
                    var _items = levelsCategories[key];
                    levels[key] = [];
                    for (var i = 0; i < _items.length; i++) {
                        var __datum = {};
                        __datum.label = _items[i] + (_items[i] === '' ? '' : ' - ') + ( (['SS', 'IS'].indexOf(key) !== -1) ? desS[_items[i]] : desR[_items[i]]);
                        __datum.value = _items[i];
                        levels[key].push(__datum);
                    }
                }
                return levels;
            }

            function GeneStatusSingleton() {
                this.isOpen = false;
                this.hideEmpty = false;
            }

            $scope.fileTitle = $routeParams.geneName;
            $scope.gene = '';
            $scope.comments = '';
            $scope.newGene = {};
            $scope.collaborators = {};
            $scope.checkboxes = {
                'oncogenic': ['YES', 'LIKELY', 'NO', 'UNKNOWN'],
                'mutation_effect': ['Activating', 'Inactivating', 'Other'],
                'geneStatus': ['Complete', 'Proceed with caution', 'Not ready'],
                'hotspot': ['TRUE', 'FALSE']
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
                },
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
            $scope.addMutationPlaceholder = 'Mutation Name';
            $scope.userRole = Users.getMe().role;
            $scope.levelExps = {
                SR: '<strong>Level R1:</strong> NCCN-compendium listed biomarker for resistance to a FDA-approved drug.<br/>Example 1: Colorectal cancer with KRAS mutation → resistance to cetuximab<br/>Example 2: EGFR-L858R or exon 19 mutant lung cancers with coincident T790M mutation → resistance to erlotinib',
                IR: '<strong>Level R2:</strong> Not NCCN compendium-listed biomarker, but clinical evidence linking this biomarker to drug resistance.<br/>Example: Resistance to crizotinib in a patient with metastatic lung adenocarcinoma harboring a CD74-ROS1 rearrangement (PMID: 23724914).<br/><strong>Level R3:</strong> Not NCCN compendium-listed biomarker, but preclinical evidence potentially linking this biomarker to drug resistance.<br/>Example: Preclinical evidence suggests that BRAF V600E mutant thyroid tumors are insensitive to RAF inhibitors (PMID: 23365119).<br/>'
            };
            $scope.showHideButtons = [
                {'key': 'prevelenceShow', 'display': 'Prevalence'},
                {'key': 'proImShow', 'display': 'Prognostic implications'},
                {'key': 'nccnShow', 'display': 'NCCN guidelines'},
                {'key': 'ssShow', 'display': 'Standard implications for sensitivity to therapy'},
                {'key': 'srShow', 'display': 'Standard implications for resistance to therapy'},
                {'key': 'isShow', 'display': 'Investigational implications for sensitivity to therapy'},
                {'key': 'irShow', 'display': 'Investigational implications for resistance to therapy'},
                {'key': 'trialsShow', 'display': 'Ongoing clinical trials'}
            ];
            $scope.list = [];
            $scope.sortableOptions = {
                stop: function (e, ui) {
                    console.log('dropindex', ui.dropindex);
                    console.log('index', ui.index);
                    console.log(e, ui);
                },
                beforeStop: function (e, ui) {
                    console.log('dropindex', ui.dropindex);
                    console.log('index', ui.index);
                    console.log(e, ui);
                }
                // handle: '> .myHandle'
            };
            $scope.selfParams = {};
            $scope.geneStatus = {};

            $scope.status = {
                expandAll: false,
                hideAllEmpty: false,
                rendering: true,
                numAccordion: 0
            };

            if ($scope.userRole === 8) {
                $scope.status.hideAllObsolete = true;
            } else {
                $scope.status.hideAllObsolete = false;
            }

            $scope.$watch('status.hideAllEmpty', function (n, o) {
                if (n !== o) {
                    if (n) {
                        $scope.isOpenFunc('hideEmpty');
                    } else {
                        $scope.isOpenFunc('showEmpty');
                    }
                }
            });

            $scope.$watch('status.expandAll', function (n, o) {
                if (n !== o) {
                    if (n) {
                        $scope.isOpenFunc('expand');
                    } else {
                        $scope.isOpenFunc('collapse');
                    }
                }
            });

            getDriveOncokbInfo();
            getOncoTreeTumortypes();
            var clock;
            clock = $interval(function () {
                storage.requireAuth(true).then(function (result) {
                    if (result && !result.error) {
                        console.log('\t checked token', new Date().getTime(), gapi.auth.getToken());
                    } else {
                        documentClosed();
                        console.log('error when renew token in interval func.');
                    }
                });
            }, 600000);


            loadFile().then(function (file) {
                $scope.realtimeDocument = file;

                if ($scope.fileTitle) {
                    var model = $scope.realtimeDocument.getModel();
                    if (!model.getRoot().get('gene')) {
                        var gene = model.create('Gene');
                        model.getRoot().set('gene', gene);
                        $scope.gene = gene;
                        $scope.gene.name.setText($scope.fileTitle);
                        $scope.model = model;
                        afterCreateGeneModel();
                    } else {
                        var numAccordion = 0;
                        model.getRoot().get('gene').mutations.asArray().forEach(function (mutation) {
                            numAccordion += mutation.tumors.length;
                            mutation.tumors.asArray().forEach(function (tumor) {
                                numAccordion += 8;
                                tumor.TI.asArray().forEach(function (ti) {
                                    numAccordion += ti.treatments.length;
                                });
                            });
                        });
                        console.log(numAccordion);
                        $scope.status.numAccordion = numAccordion;
                        $scope.gene = model.getRoot().get('gene');
                        $scope.model = model;
                        afterCreateGeneModel();
                    }
                } else {
                    $scope.model = '';
                }
            });


            // Token expired, refresh
            $rootScope.$on('realtimeDoc.token_refresh_required', function () {
                console.log('--token_refresh_required-- going to refresh page.');
                dialogs.error('Error', 'An error has occurred. This page will be redirected to genes page.');
                documentClosed();
                $location.path('/genes');
            });

            // Other unidentify error
            $rootScope.$on('realtimeDoc.other_error', function () {
                dialogs.error('Error', 'An error has occurred. This page will be redirected to genes page.');
                documentClosed();
                $location.path('/genes');
            });

            // Realtime documet not found
            $rootScope.$on('realtimeDoc.client_error', function () {
                dialogs.error('Error', 'An error has occurred. This page will be redirected to genes page.');
                documentClosed();
                $location.path('/genes');
            });

            // Realtime documet not found
            $rootScope.$on('realtimeDoc.not_found', function () {
                dialogs.error('Error', 'An error has occurred. This page will be redirected to genes page.');
                documentClosed();
                $location.path('/genes');
            });

            $scope.$on('$locationChangeStart', function () {
                storage.closeDocument();
                documentClosed();
            });

            // Get OncoTree primary/secondary/tertiary/quaternary types
            // DatabaseConnector.getAllOncoTreeTumorTypes(function(data){
            //   var tumorTypes = {};

            //   data.forEach(function(e, i){
            //     var key = e.primary;

            //     if(!_.endsWith(key, 'cancer')) {
            //       key += ' cancer';
            //     }

            //     if(tumorTypes.hasOwnProperty(key)) {
            //       var loop=['secondary', 'tertiary', 'quaternary'];
            //       loop.forEach(function(e1, i1){
            //         if(e[e1] && tumorTypes[key].indexOf(e[e1]) === -1) {
            //           tumorTypes[key].push(e[e1]);
            //         }
            //       });
            //     }else {
            //       tumorTypes[key] = [];
            //     }
            //   });

            //   var newTumorTypes = []
            //   for(var key in tumorTypes) {
            //     for(var i = 0; i < tumorTypes[key].length; i++) {
            //       var __datum = {
            //         'name': tumorTypes[key][i],
            //         'tissue': key
            //       }
            //       newTumorTypes.push(__datum);
            //     }
            //   }
            //   $scope.tumorTypes = newTumorTypes;
            // });

        }]
);
