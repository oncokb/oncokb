'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.mainUtils
 * @description
 * # mainUtils
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .factory('mainUtils', function(OncoKB, _, storage, $q, DatabaseConnector) {
        var isoforms = {};
        var oncogeneTSG = {};

        /**
         * Create new Gene document
         * @param {string} hugoSymbol Gene Hugo Symbol
         * @param {string} parentFolderId The folder where to put new document
         * @returns {*|h.promise|promise|r.promise|d.promise}
         */
        function createGene(hugoSymbol, parentFolderId) {
            var deferred = $q.defer();
            if (_.isString(hugoSymbol)) {
                storage.requireAuth().then(function() {
                    console.log('Creating gene ', hugoSymbol);
                    storage.createDocument(hugoSymbol, parentFolderId)
                        .then(function(result) {
                            if (result && result.error) {
                                console.error('\tError when creating document.');
                                deferred.reject(result);
                            } else {
                                storage.getRealtimeDocument(result.id)
                                    .then(function(file) {
                                        var model = file.getModel();
                                        var gene = model.getRoot().get('gene');
                                        if (!gene) {
                                            gene = model.create('Gene');
                                            gene.name.setText(hugoSymbol);
                                            model.getRoot().set('gene', gene);
                                        }
                                        if (!model.getRoot().get('vus')) {
                                            var vus = model.createList();
                                            model.getRoot().set('vus', vus);
                                        }
                                        $q.all([getIsoform(hugoSymbol),
                                            getOncogeneTSG(hugoSymbol)])
                                            .then(function(result) {
                                                if (_.isArray(result)) {
                                                    var isoform = result[0];
                                                    var geneType = result[1];
                                                    if (isoform && isoform.error) {
                                                        console.error('\tError when getting isoforms.');
                                                    } else if (isoform && isoform.gene_name) {
                                                        var isoformModel = createIsoform(model, isoform);
                                                        gene.transcripts.push(isoformModel);
                                                    } else {
                                                        console.error('\tNo isoform found!!');
                                                    }

                                                    if (geneType && geneType.error) {
                                                        console.error('\tError when getting gene type.');
                                                    } else if (geneType && geneType.classification) {
                                                        var type = '';
                                                        var key = '';
                                                        switch (geneType.classification) {
                                                            case 'TSG':
                                                                type = 'Tumor Suppressor';
                                                                key = 'TSG'
                                                                break;
                                                            case 'Oncogene':
                                                                type = 'Oncogene';
                                                                key = 'OCG'
                                                                break;
                                                            default:
                                                                type = '';
                                                                break;
                                                        }
                                                        if (type) {
                                                            gene.type.set(key, type);
                                                        }
                                                    } else {
                                                        console.log('\tNo gene type found.');
                                                    }
                                                }
                                                deferred.resolve();
                                            }, function(error) {
                                                console.error('Failed to load isoform/geneType', error);
                                            })
                                    });
                            }
                        });
                });
            }
            return deferred.promise;
        }

        /**
         * Create Google realtime mutation
         * @param model Google realtime gene model
         * @param mutationName mutation name.
         * Could be multiple, separated by comma.
         * @returns {OncoKB.Mutation}
         */
        function createMutation(model, mutationName) {
            if (_.isString(mutationName)) {
                mutationName = _.filter(mutationName
                    .trim().split(','), function(item) {
                    return item ? true : false;
                }).join(',');
                var mutation = model.create(OncoKB.Mutation);
                mutation.name.setText(mutationName);
                mutation.oncogenic_eStatus.set('obsolete', 'false');
                mutation.shortSummary_eStatus.set('obsolete', 'false');
                return mutation;
            }
            return null;
        }

        /**
         * Create OncoKB Tumor, it is list of Cancer Type
         * @param model
         * @returns {OncoKB.Tumor}
         */
        function createTumorType(model) {
            var tumorType = model.create(OncoKB.Tumor);
            for (var i = 0; i < 4; i++) {
                var __ti = model.create(OncoKB.TI);
                var __status = i < 2 ? 1 : 0; // 1: Standard, 0: Investigational
                var __type = i % 2 === 0 ? 1 : 0; //1: sensitivity, 0: resistance
                var __name = (__status ? 'Standard' : 'Investigational') + ' implications for ' + (__type ? 'sensitivity' : 'resistance') + ' to therapy';

                __ti.types.set('status', __status.toString());
                __ti.types.set('type', __type.toString());
                __ti.name.setText(__name);
                tumorType.TI.push(__ti);
            }
            return tumorType;
        }

        /**
         * Create single OncoTree cancer type
         * @param model Google Realtime document gene model
         * @param cancerType OncoTree main type name
         * @param subtype OncoTree subtype name
         * @param oncoTreeCode Subtype code
         * @returns {OncoKB.CancerType} OncoKB.CancerType
         */
        function createCancerType(model, cancerType, subtype, oncoTreeCode) {
            if (_.isString(cancerType)) {
                var newCancerType = model.create(OncoKB.CancerType);
                newCancerType.cancerType.setText(cancerType);
                if (_.isString(oncoTreeCode)) {
                    newCancerType.oncoTreeCode.setText(oncoTreeCode);
                }
                if (_.isString(subtype)) {
                    newCancerType.subtype.setText(subtype);
                }
                newCancerType.cancerType_eStatus.set('obsolete', 'false');
                newCancerType.subtype_eStatus.set('obsolete', 'false');
                newCancerType.oncoTreeCode_eStatus.set('obsolete', 'false');
                return newCancerType;
            }
            return null;
        }

        /**
         * Create new treatment object
         * @param model Google realtime document
         * @param treatmentName New treatment name
         * @returns {OncoKB.Treatment}
         */
        function createTreatment(model, treatmentName) {
            if (_.isString(treatmentName) && treatmentName) {
                var treatment = model.create(OncoKB.Treatment);
                treatment.name.setText(treatmentName.trim());
                treatment.type.setText('Therapy');
                return treatment;
            }
            return null;
        }

        /**
         * Create Google Realtime isoform model
         * @param model Google realtime document
         * @param {object} isoform
         * @returns {*}
         */
        function createIsoform(model, isoform) {
            if (_.isObject(isoform) && model) {
                var Isoform = model.create(OncoKB.ISOForm);
                if (isoform.isoform_override) {
                    Isoform.isoform_override.setText(isoform.isoform_override);
                }
                if (isoform.gene_name) {
                    Isoform.gene_name.setText(isoform.gene_name);
                }
                if (isoform.dmp_refseq_id) {
                    Isoform.dmp_refseq_id.setText(isoform.dmp_refseq_id);
                }
                if (isoform.ccds_id) {
                    Isoform.ccds_id.setText(isoform.ccds_id);
                }
                return Isoform;
            }
            return null;
        }

        /**
         * Output cancer type name, either subtype or cancerType
         * @param cancerTypes
         * @returns {string}
         */
        function getCancerTypesName(cancerTypes) {
            if (!cancerTypes) {
                return null;
            }
            var list = [];
            cancerTypes.asArray().forEach(function(cancerType) {
                if (cancerType.subtype.length > 0) {
                    var str = cancerType.subtype.getText();
                    list.push(str);
                } else if (cancerType.cancerType.length > 0) {
                    list.push(cancerType.cancerType.getText());
                }
            });
            return list.join(', ');
        }

        /**
         * Util to find isoform info by giving hugo symbol
         * @param {string} hugoSymbol
         * @returns {*|h.promise|promise|r.promise|d.promise}
         */
        function getIsoform(hugoSymbol) {
            var deferred = $q.defer();
            if (Object.keys(isoforms).length == 0) {
                DatabaseConnector.getIsoforms()
                    .then(function(result) {
                        if (_.isArray(result)) {
                            _.each(result, function(item) {
                                if (_.isObject(item) &&
                                    _.isString(item.gene_name)) {
                                    isoforms[item.gene_name] = item;
                                }
                            });
                        }
                        return deferred.resolve(isoforms[hugoSymbol]);
                    }, function(error) {
                        deferred.reject();
                    });
            } else {
                deferred.resolve(isoforms[hugoSymbol]);
            }
            return deferred.promise;
        }

        /**
         * Util to find gene type by giving hugo symbol
         * @param {string} hugoSymbol
         * @returns {*|h.promise|promise|r.promise|d.promise}
         */
        function getOncogeneTSG(hugoSymbol) {
            var deferred = $q.defer();
            if (Object.keys(oncogeneTSG).length == 0) {
                DatabaseConnector.getOncogeneTSG(hugoSymbol)
                    .then(function(result) {
                        if (_.isArray(result)) {
                            _.each(result, function(item) {
                                if (_.isObject(item) &&
                                    _.isString(item.gene)) {
                                    oncogeneTSG[item.gene] = item;
                                }
                            });
                        }
                        deferred.resolve(oncogeneTSG[hugoSymbol]);
                    }, function(error) {
                        deferred.reject();
                    });
            } else {
                deferred.resolve(oncogeneTSG[hugoSymbol]);
            }
            return deferred.promise;
        }

        return {
            getCancerTypesName: getCancerTypesName,
            createGene: createGene,
            createMutation: createMutation,
            createTumorType: createTumorType,
            createCancerType: createCancerType,
            createTreatment: createTreatment,
            getIsoform: getIsoform,
            getOncogeneTSG: getOncogeneTSG
        };
    });
