'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.importer
 * @description
 * # importer
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .service('importer', function importer($timeout, documents, S, storage, OncoKB, $q, _) {
        var self = this;
        self.docs = [];
        self.docsL = 0;
        self.newFolder = '';
        self.parentFolder = OncoKB.config.backupFolderId; //backup folder under knowledgebase
        self.status = {
            backupIndex: -1,
            migrateIndex: -1
        };

        function backup(callback) {
            if (self.parentFolder) {
                if (!angular.isFunction(callback)) {
                    callback = undefined;
                }

                createFolder().then(function (result) {
                    if (result && result.error) {
                        console.error('Create folder failed.', result);
                        if (callback) {
                            callback();
                        }
                    } else {
                        // var docs = documents.get({title: 'BRAF'});
                        self.docs = documents.get();
                        self.docsL = self.docs.length;
                        copyData(0, callback);
                    }
                });
            } else {
                console.log('Backup folder ID needed.');
            }
        }

        function createFolder() {
            var deferred = $q.defer();

            storage.requireAuth(true).then(function (result) {
                if (result && !result.error) {
                    storage.createFolder(self.parentFolder).then(function (result) {
                        if (result.id) {
                            self.newFolder = result.id;
                            deferred.resolve(result);
                        } else {
                            deferred.reject(result);
                        }
                    });
                }
            });

            return deferred.promise;
        }

        function copyData(index, callback) {
            if (index < self.docs.length) {
                var doc = self.docs[index];
                copyFileData(self.newFolder, doc.id, doc.title, index).then(function () {
                    copyData(++index, callback);
                });
            } else {
                if (callback) {
                    callback();
                }
                console.log('finished');
            }
        }

        function migrate() {
            var destinationFolderId = '';
            var deferred = $q.defer();

            destinationFolderId = '0BzBfo69g8fP6RmczanJFSVlNSjA';

            if (destinationFolderId) {
                self.newFolder = destinationFolderId;
                self.docs = documents.get();
                self.docsL = self.docs.length;

                //copy foler permissions
                //assignPermission(OncoKB.config.folderId, self.newFolder)
                //    .then(function(result){
                //      if(result && result.error){
                //        console.log('Error when assigning folder permissions', result);
                //      }else{
                migrateOneFileProcess(0, deferred);
                //}
                //});
            } else {
                createFolder().then(function (result) {
                    if (result && result.error) {
                        console.log('Unable create folder.', result);
                    } else {
                        self.docs = documents.get();
                        self.docsL = self.docs.length;

                        //copy foler permissions
                        assignPermission(OncoKB.config.folderId, self.newFolder)
                            .then(function (result) {
                                if (result && result.error) {
                                    console.log('Error when assigning folder permissions', result);
                                } else {
                                    migrateOneFileProcess(0, deferred);
                                }
                            });
                    }
                });
            }

            return deferred.promise;
        }

        function migrateOneFileProcess(docIndex, deferred) {
            if (docIndex < self.docsL) {
                var doc = self.docs[docIndex];
                copyFileData(self.newFolder, doc.id, doc.title, docIndex)
                    .then(function (result) {
                        if (result && result.error) {
                            console.log(result);
                            migrateOneFileProcess(++docIndex, deferred);
                        } else {
                            assignPermission(doc.id, result.id)
                                .then(function (result) {
                                    if (result && result.error) {
                                        console.log('error', result);
                                    } else {
                                        migrateOneFileProcess(++docIndex, deferred);
                                    }
                                });
                        }
                    });
            } else {
                console.log('Migrating finished.');
                deferred.resolve();
            }
        }

        function assignPermission(oldDocId, newDocId) {
            var deferred = $q.defer();
            console.log('\t Giving permissions');
            storage.getPermission(oldDocId).then(function (result) {
                if (result && result.error) {
                    deferred.reject(result);
                } else {
                    assignIndividualPermission(newDocId, result.items, 0, deferred);
                    //var promises = [];
                    //promises = result.items.forEach(function(permission, i){
                    //  if(permission.id && permission.emailAddress && permission.role && permission.type && permission.role !== 'owner'){
                    //    promises.push();
                    //  }
                    //});
                    //$q.all(promises).then(function(){
                    //  deferred.resolve();
                    //});
                }
            });
            return deferred.promise;
        }

        function assignIndividualPermission(newDocId, items, itemIndex, deferred) {
            if (itemIndex < items.length) {
                var permission = items[itemIndex];
                console.log('\t\t\tp-', itemIndex + 1);
                if (permission.id && permission.emailAddress && permission.role && permission.type && permission.role !== 'owner') {
                    storage.insertPermission(newDocId, permission.emailAddress, permission.type, permission.role).then(function () {
                        //$timeout(function () {
                        assignIndividualPermission(newDocId, items, ++itemIndex, deferred);
                        //}, 100);
                    });
                } else {
                    console.log('\t\t\tskip-', permission);
                    //$timeout(function () {
                    assignIndividualPermission(newDocId, items, ++itemIndex, deferred);
                    //}, 100);
                }
            } else {
                deferred.resolve();
                console.log('\t\tAll permissions are assigned.');
            }
        }

        //Return all info
        function getVUSFullData(vus) {
            var vusData = [];
            if (vus) {
                vus.asArray().forEach(function (vusItem) {
                    var datum = {};
                    datum.name = vusItem.name.getText();
                    datum.time = [];
                    vusItem.time.asArray().forEach(function (time) {
                        var _time = {};
                        _time.value = time.value.getText();
                        _time.by = {};
                        _time.by.name = time.by.name.getText();
                        _time.by.email = time.by.email.getText();
                        datum.time.push(_time);
                    });
                    vusData.push(datum);
                });
            }
            return vusData;
        }

        //Only return last edit info
        function getVUSData(vus) {
            var vusData = [];
            if (vus) {
                vus.asArray().forEach(function (vusItem) {
                    var datum = {};
                    datum.name = vusItem.name.getText();
                    if(vusItem.time.length > 0) {
                        datum.lastEdit = vusItem.time.get(vusItem.time.length - 1).value.getText();
                    }
                    vusData.push(datum);
                });
            }
            return vusData;
        }

        function getGeneData(realtime, excludeObsolete) {
            /* jshint -W106 */
            var gene = {};
            var geneData = realtime;

            gene = combineData(gene, geneData, ['name', 'status', 'summary', 'background']);
            gene.mutations = [];
            gene.curators = [];

            geneData.curators.asArray().forEach(function (e) {
                var _curator = {};
                _curator = combineData(_curator, e, ['name', 'email']);
                gene.curators.push(_curator);
            });

            geneData.mutations.asArray().forEach(function (e) {
                var _mutation = {};
                _mutation.tumors = [];
                _mutation.effect = {};
                _mutation = combineData(_mutation, e, ['name', 'summary']);
                if (!(excludeObsolete !== undefined && excludeObsolete && e.shortSummary_eStatus && e.shortSummary_eStatus.has('obsolete') && e.shortSummary_eStatus.get('obsolete') === 'true')) {
                    _mutation = combineData(_mutation, e, ['shortSummary', 'oncogenic']);
                }
                if (!(excludeObsolete !== undefined && excludeObsolete && e.oncogenic_eStatus && e.oncogenic_eStatus.has('obsolete') && e.oncogenic_eStatus.get('obsolete') === 'true')) {
                    _mutation = combineData(_mutation, e, ['description', 'short']);
                    _mutation.effect = combineData(_mutation.effect, e.effect, ['value', 'addOn']);

                    _mutation.effect = combineData(_mutation.effect, e.effect, ['value', 'addOn']);

                    if (e.effect_comments) {
                        _mutation.effect_comments = getComments(e.effect_comments);
                    }
                }

                e.tumors.asArray().forEach(function (e1) {
                    var __tumor = {};
                    var selectedAttrs = ['name', 'summary'];

                    if (!(excludeObsolete !== undefined && excludeObsolete && e1.prevalence_eStatus && e1.prevalence_eStatus.has('obsolete') && e1.prevalence_eStatus.get('obsolete') === 'true')) {
                        selectedAttrs.push('prevalence', 'shortPrevalence');
                    }

                    if (!(excludeObsolete !== undefined && excludeObsolete && e1.progImp_eStatus && e1.progImp_eStatus.has('obsolete') && e1.progImp_eStatus.get('obsolete') === 'true')) {
                        selectedAttrs.push('progImp', 'shortProgImp');
                    }
                    __tumor = combineData(__tumor, e1, selectedAttrs);
                    __tumor.trials = [];
                    __tumor.TI = [];
                    __tumor.nccn = {};
                    __tumor.interactAlts = {};

                    if (!(excludeObsolete !== undefined && excludeObsolete && e1.nccn_eStatus && e1.nccn_eStatus.has('obsolete') && e1.nccn_eStatus.get('obsolete') === 'true')) {
                        __tumor.nccn = combineData(__tumor.nccn, e1.nccn, ['therapy', 'disease', 'version', 'pages', 'category', 'description', 'short']);
                    }

                    e1.trials.asArray().forEach(function (trial) {
                        __tumor.trials.push(trial);
                    });
                    if (e1.trials_comments) {
                        __tumor.trials_comments = getComments(e1.trials_comments);
                    }
                    e1.TI.asArray().forEach(function (e2) {
                        var ti = {};

                        ti = combineData(ti, e2, ['name', 'description', 'short']);
                        ti.status = getString(e2.types.get('status'));
                        ti.type = getString(e2.types.get('type'));
                        ti.treatments = [];

                        e2.treatments.asArray().forEach(function (e3) {
                            var treatment = {};
                            if (excludeObsolete !== undefined && excludeObsolete && e3.name_eStatus && e3.name_eStatus.has('obsolete') && e3.name_eStatus.get('obsolete') === 'true') {
                                return;
                            }
                            treatment = combineData(treatment, e3, ['name', 'type', 'level', 'indication', 'description', 'short']);
                            ti.treatments.push(treatment);
                        });
                        __tumor.TI.push(ti);
                    });

                    if (!(excludeObsolete !== undefined && excludeObsolete && e1.nccn_eStatus && e1.nccn_eStatus.has('obsolete') && e1.nccn_eStatus.get('obsolete') === 'true')) {
                        __tumor.nccn = combineData(__tumor.nccn, e1.nccn, ['therapy', 'disease', 'version', 'pages', 'category', 'description', 'short']);
                    }

                    __tumor.interactAlts = combineData(__tumor.interactAlts, e1.interactAlts, ['alterations', 'description']);
                    _mutation.tumors.push(__tumor);
                });

                gene.mutations.push(_mutation);
            });
            return gene;
            /* jshint +W106 */
        }

        function createVUSItem(vusItem, vusModel, model) {
            var vus = model.create(OncoKB.VUSItem);

            vus.name.setText(vusItem.name);
            _.each(vusItem.time, function (time) {
                var timeStamp = model.create(OncoKB.TimeStampWithCurator);
                timeStamp.value.setText(time.value);
                timeStamp.by.name.setText(time.by.name);
                timeStamp.by.email.setText(time.by.email);
                vus.time.push(timeStamp);
            });
            vusModel.push(vus);
        }

        function copyFileData(folderId, fileId, fileTitle, docIndex) {
            var deferred = $q.defer();
            storage.requireAuth(true).then(function () {
                storage.createDocument(fileTitle, folderId).then(function (file) {
                    console.log('Created file ', fileTitle, docIndex + 1);
                    storage.getRealtimeDocument(fileId).then(function (realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            console.log('\t Copying');
                            var gene = realtime.getModel().getRoot().get('gene');
                            var vus = realtime.getModel().getRoot().get('vus');
                            if (gene) {
                                var geneData = getGeneData(gene);
                                var vusData = getVUSFullData(vus);
                                storage.getRealtimeDocument(file.id).then(function (newRealtime) {
                                    var model = createModel(newRealtime.getModel());
                                    var geneModel = model.getRoot().get('gene');
                                    var vusModel = model.getRoot().get('vus');

                                    model.beginCompoundOperation();
                                    for (var key in geneData) {
                                        if (geneModel[key]) {
                                            geneModel = setValue(model, geneModel, geneData[key], key);
                                        }
                                    }

                                    if (vusData) {
                                        _.each(vusData, function (vusItem) {
                                            createVUSItem(vusItem, vusModel, newRealtime.getModel());
                                        });
                                    }
                                    model.endCompoundOperation();
                                    console.log('\t Done.');
                                    $timeout(function () {
                                        deferred.resolve(file);
                                    }, 500, false);
                                });
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function () {
                                    deferred.resolve(file);
                                }, 500, false);
                            }
                        }
                    });
                });
            });
            return deferred.promise;
        }

        function setValue(rootModel, model, value, key) {
            if (angular.isString(value)) {
                if (model[key] && model[key].type) {
                    model[key].setText(value);
                } else {
                    console.log('Unknown key', key);
                }
            } else if (angular.isArray(value)) {
                value.forEach(function (e) {
                    var _datum;
                    switch (key) {
                        case 'curators':
                            _datum = rootModel.create('Curator');
                            break;
                        case 'mutations':
                            _datum = rootModel.create('Mutation');
                            break;
                        case 'tumors':
                            _datum = rootModel.create('Tumor');
                            break;
                        case 'TI':
                            _datum = rootModel.create('TI');
                            break;
                        case 'treatments':
                            _datum = rootModel.create('Treatment');
                            break;
                        case 'trials':
                            _datum = e;
                            break;
                        default:
                            break;
                    }

                    if (key.indexOf('_comments') !== -1) {
                        _datum = rootModel.create('Comment');
                    }

                    if (key === 'TI') {
                        _datum.types.set('status', e.status);
                        _datum.types.set('type', e.type);
                        delete e.status;
                        delete e.type;
                    }
                    if (key !== 'trials') {
                        for (var _key in e) {
                            _datum = setValue(rootModel, _datum, e[_key], _key);
                        }
                    }
                    model[key].push(_datum);
                });
            } else if (angular.isObject(value)) {
                var _datum;
                switch (key) {
                    case 'nccn':
                        _datum = rootModel.create('NCCN');
                        break;
                    case 'effect':
                        _datum = rootModel.create('ME');
                        break;
                    case 'interactAlts':
                        _datum = rootModel.create('InteractAlts');
                        break;
                }

                if (key.indexOf('_eStatus') !== -1) {
                    for (var _key in value) {
                        model[key].set(_key, value[_key]);
                    }
                } else if (key.indexOf('_timeStamp') !== -1) {
                    for (var _key in value) {
                        _datum = rootModel.create('TimeStamp');
                        _datum.value.setText(value[_key].value || '');
                        _datum.by.setText(value[_key].by || '');
                        model[key].set(_key, _datum);
                    }
                } else {
                    for (var _key in value) {
                        _datum = setValue(rootModel, _datum, value[_key], _key);
                    }
                    model[key] = _datum;
                }
            } else {
                console.log('Error value type.');
            }
            return model;
        }

        function createModel(model) {
            model = createGeneModel(model);
            model = createVUSModel(model);
            return model;
        }

        function createGeneModel(model) {
            if (!model.getRoot().get('gene')) {
                var gene = model.create('Gene');
                model.getRoot().set('gene', gene);
            }
            return model;
        }

        function createVUSModel(model) {
            if (!model.getRoot().get('vus')) {
                var vus = model.createList();
                model.getRoot().set('vus', vus);
            }
            return model;
        }

        function combineData(object, model, keys) {
            keys.forEach(function (e) {
                if (model[e]) {
                    object[e] = getString(model[e].getText());
                    if (model[e + '_comments']) {
                        object[e + '_comments'] = getComments(model[e + '_comments']);
                    }
                    if (model[e + '_eStatus']) {
                        object[e + '_eStatus'] = getEvidenceStatus(model[e + '_eStatus']);
                    }
                    if (model[e + '_timeStamp']) {
                        object[e + '_timeStamp'] = getTimeStamp(model[e + '_timeStamp']);
                    }
                }
            });
            return object;
        }

        function getString(string) {
            var tmp = window.document.createElement('DIV');
            tmp.innerHTML = string;
            var _string = tmp.textContent || tmp.innerText || S(string).stripTags().s;
            string = S(_string).collapseWhitespace().s;
            return string;
        }

        function getComments(model) {
            var comments = [];
            var commentKeys = Object.keys(OncoKB.curateInfo.Comment);
            var comment = {};

            commentKeys.forEach(function (e) {
                comment[e] = '';
            });

            model.asArray().forEach(function (e) {
                var _comment = angular.copy(comment);
                for (var key in _comment) {
                    if (e[key]) {
                        _comment[key] = e[key].getText();
                    }
                }
                comments.push(_comment);
            });
            return comments;
        }

        function getEvidenceStatus(model) {
            var keys = model.keys();
            var status = {};

            keys.forEach(function (e) {
                status[e] = model.get(e);
            });
            return status;
        }

        function getTimeStamp(model) {
            var keys = model.keys();
            var status = {};

            keys.forEach(function (e) {
                status[e] = {
                    value: model.get(e).value.text,
                    by: model.get(e).by.text
                };
            });
            return status;
        }

        return {
            backup: backup,
            migrate: migrate,
            getGeneData: getGeneData,
            getVUSData: getVUSData,
            getVUSFullData: getVUSFullData
        };
    });
