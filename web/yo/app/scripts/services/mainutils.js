'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.mainUtils
 * @description
 * # mainUtils
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .factory('mainUtils', function(OncoKB, _, storage, $q, DatabaseConnector, $rootScope, ReviewResource) {
        var isoforms = {};
        var oncogeneTSG = {};

        /**
         * Create new Gene document
         * @param {string} hugoSymbol Gene Hugo Symbol
         * @param {string} parentFolderId The folder where to put new document
         * @return {*|h.promise|promise|r.promise|d.promise} Promise
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
                                        if (!model.getRoot().get('history')) {
                                            var history = model.createMap();
                                            model.getRoot().set('history', history);
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
                                                            key = 'TSG';
                                                            break;
                                                        case 'Oncogene':
                                                            type = 'Oncogene';
                                                            key = 'OCG';
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
                                            });
                                    });
                            }
                        });
                });
            }
            return deferred.promise;
        }

        /**
         * Create Google realtime mutation
         * @param {object} model Google realtime gene model
         * @param {string} mutationName mutation name.
         * Could be multiple, separated by comma.
         * @return {OncoKB.Mutation} OncoKB.Mutation
         */
        function createMutation(model, mutationName) {
            if (_.isString(mutationName)) {
                mutationName = _.filter(mutationName
                    .trim().split(','), function(item) {
                    return _.isString(item);
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
         * @param {object} model Google Realtime document gene model
         * @return {OncoKB.Tumor} OncoKB.Tumor
         */
        function createTumorType(model) {
            var tumorType = model.create(OncoKB.Tumor);
            for (var i = 0; i < 4; i++) {
                var __ti = model.create(OncoKB.TI);
                var __status = i < 2 ? 1 : 0; // 1: Standard, 0: Investigational
                var __type = i % 2 === 0 ? 1 : 0; // 1: sensitivity, 0: resistance
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
         * @param {object} model Google Realtime document gene model
         * @param {string} cancerType OncoTree main type name
         * @param {string} subtype OncoTree subtype name
         * @param {stirng} oncoTreeCode Subtype code
         * @return {OncoKB.CancerType} OncoKB.CancerType
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
         * @param {object} model Google realtime document
         * @param {string} treatmentName New treatment name
         * @return {OncoKB.Treatment} OncoKB.Treatment
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
         * @param {object} model Google realtime document
         * @param {object} isoform gene isofom info
         * @return {*} return OncoKB.Isoform
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
         * @param {array} cancerTypes List of cancer types
         * @return {string} TumorType name
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
         * Check whether searched mainType in cancerTypes
         * @param {array} cancerTypes array of cancer types
         * @param {string} mainType searched main type
         * @return {boolean} whether exist
         */
        function containMainType(cancerTypes, mainType) {
            if (!cancerTypes || !mainType) {
                return null;
            }
            mainType = mainType.toLowerCase().trim();
            cancerTypes.asArray().forEach(function(cancerType) {
                if (cancerType.cancerType && cancerType.cancerType.text.toLowerCase().indexOf(mainType) !== -1) {
                    return true;
                }
            });
            return false;
        }

        /**
         * Output last reviewed cancer type name, either subtype or cancerType
         * @param {array} cancerTypes array of cancer types
         * @return {string} TumorType name
         */
        function getLastReviewedCancerTypesName(cancerTypes) {
            var list = [];
            if (_.isArray(cancerTypes)) {
                cancerTypes.forEach(function(cancerType) {
                    if (cancerType.subtype) {
                        var str = cancerType.subtype;
                        list.push(str);
                    } else if (cancerType.cancerType) {
                        list.push(cancerType.cancerType);
                    }
                });
            }
            return list.join(', ');
        }

        /**
         * Util to find isoform info by giving hugo symbol
         * @param {string} hugoSymbol Gene Hugo Symbol
         * @return {*|h.promise|promise|r.promise|d.promise} Promise
         */
        function getIsoform(hugoSymbol) {
            var deferred = $q.defer();
            if (Object.keys(isoforms).length === 0) {
                $q.all([DatabaseConnector.getIsoforms(), DatabaseConnector.getIsoforms('msk')])
                    .then(function(result) {
                        if (_.isArray(result)) {
                            var allGenes = [];
                            _.each(result, function(item) {
                                if (_.isArray(item)) {
                                    allGenes = allGenes.concat(item);
                                }
                            });
                            _.each(allGenes, function(item) {
                                if (_.isObject(item) &&
                                    _.isString(item.gene_name)) {
                                    isoforms[item.gene_name] = item;
                                }
                            });
                        }
                        deferred.resolve(isoforms[hugoSymbol]);
                    }, function() {
                        deferred.reject();
                    });
            } else {
                deferred.resolve(isoforms[hugoSymbol]);
            }
            return deferred.promise;
        }

        /**
         * Util to find gene type by giving hugo symbol
         * @param {string} hugoSymbol Gene Hugo Symbol
         * @return {*|h.promise|promise|r.promise|d.promise} Promise
         */
        function getOncogeneTSG(hugoSymbol) {
            var deferred = $q.defer();
            if (Object.keys(oncogeneTSG).length === 0) {
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
                    }, function() {
                        deferred.reject();
                    });
            } else {
                deferred.resolve(oncogeneTSG[hugoSymbol]);
            }
            return deferred.promise;
        }

        /**
         * Util to send email systematically
         * @param {string} sendTo The recipient
         * @param {string} subject The email subject
         * @param {string} content The email content
         * @return {*|h.promise|promise|r.promise|d.promise} Promise
        * */
        function sendEmail(sendTo, subject, content) {
            var deferred = $q.defer();
            var param = {sendTo: sendTo, subject: subject, content: content};
            DatabaseConnector.sendEmail(
                param,
                function(result) {
                    deferred.resolve(result);
                },
                function(result) {
                    deferred.reject(result);
                }
            );
            return deferred.promise;
        }

        /*
         *  Check if item needs to be reviewed or not
         *  @param {collaborative string object} uuid The uuid object for the item needs to be checked
         * */
        function needReview(uuid) {
            if (uuid) {
                uuid = uuid.getText();
                if ($rootScope.geneMetaData.get(uuid) && $rootScope.geneMetaData.get(uuid).get('review')) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check whether user is developer
         * @param {string} userName The user name
         * @return {boolean} whether user is developer
         */
        function developerCheck(userName) {
            if (!userName) {
                return false;
            }
            var developers = ['Hongxin Zhang', 'Jianjiong Gao', 'Jiaojiao Wang'];
            if (developers.indexOf(userName) !== -1) {
                return true;
            }
            return false;
        }

        /**
         * Get Oncotree main types and sub tumor types.
         * return {mainTypes: mainTypes, tumorTypes: tumorTypes}
         * **/
        function getOncoTreeMainTypes() {
            var deferred = $q.defer();
            DatabaseConnector.getOncoTreeMainTypes()
                .then(function(result) {
                    if (result.data) {
                        result.data.push({
                            id: -1,
                            name: 'All Liquid Tumors'
                        });
                        result.data.push({
                            id: -2,
                            name: 'All Solid Tumors'
                        });
                        result.data.push({
                            id: -3,
                            name: 'All Tumors'
                        });
                        result.data.push({
                            id: -4,
                            name: 'Germline Disposition'
                        });
                        result.data.push({
                            id: -5,
                            name: 'All Pediatric Tumors'
                        });
                        result.data.push({
                            id: -6,
                            name: 'Other Tumor Types'
                        });
                        var mainTypeList = _.map(result.data, function(mainType) {
                            return mainType.name;
                        });
                        DatabaseConnector.getOncoTreeTumorTypesByMainTypes(mainTypeList).then(function(tumorTypesResult) {
                            if (result.data.length !== tumorTypesResult.data.length) {
                                deferred.reject('The number of returned tumor types is not matched with number of main types.');
                            } else {
                                deferred.resolve({
                                    mainTypes:result.data,
                                    tumorTypes: tumorTypesResult.data
                                });
                            }
                        }, function() {
                            // TODO: if OncoTree server returns error.
                        });
                    }
                }, function(error) {
                    deferred.reject(error);
                });
            return deferred.promise;
        }

        /*
        * Check if the timeStamp passed in is at least one day behind or not, which means if it is 24 hours later than the current time stamp.
        * @param {string} userName The user name
        * @return {boolean} whether timeStamp expired
        * */
        function isExpiredCuration(timeStamp) {
            if (timeStamp && (new Date(timeStamp).getTime() + 8.64e+7) < new Date().getTime()) {
                return true;
            } else {
                return false;
            }
        };
        function processedInReview(type, uuid) {
            if (!type || !uuid) {
                return false;
            }
            uuid = uuid.getText();
            switch(type) {
            case 'accept':
                return ReviewResource.accepted.indexOf(uuid) !== -1;
            case 'reject':
                return ReviewResource.rejected.indexOf(uuid) !== -1;
            case 'rollback':
                return ReviewResource.rollback.indexOf(uuid) !== -1;
            case 'inside':
                return ReviewResource.inside.indexOf(uuid) !== -1;
            case 'update':
                return ReviewResource.updated.indexOf(uuid) !== -1;
            case 'name':
                return ReviewResource.nameChanged.indexOf(uuid) !== -1;
            case 'add':
                return ReviewResource.added.indexOf(uuid) !== -1;
            case 'remove':
                return ReviewResource.removed.indexOf(uuid) !== -1;
            case 'loading':
                return ReviewResource.loading.indexOf(uuid) !== -1;
            case 'precise':
                return ReviewResource.precise.indexOf(uuid) !== -1;
            default:
                return false;
            }
        }
        return {
            getCancerTypesName: getCancerTypesName,
            containMainType: containMainType,
            createGene: createGene,
            createMutation: createMutation,
            createTumorType: createTumorType,
            createCancerType: createCancerType,
            createTreatment: createTreatment,
            getIsoform: getIsoform,
            getOncogeneTSG: getOncogeneTSG,
            getLastReviewedCancerTypesName: getLastReviewedCancerTypesName,
            sendEmail: sendEmail,
            needReview: needReview,
            developerCheck: developerCheck,
            getOncoTreeMainTypes: getOncoTreeMainTypes,
            isExpiredCuration: isExpiredCuration,
            processedInReview: processedInReview
        };
    });
