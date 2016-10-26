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
        var isoForms = null;
        //   Create gene
        function createGene(parentFolderId, hugoSymbol) {
            var deferred = $q.defer();
            if (_.isString(hugoSymbol)) {
                storage.requireAuth().then(function() {
                    console.log(index, ' -> Creating gene ', hugoSymbol);
                    storage.createDocument(hugoSymbol, parentFolderId)
                        .then(function(result) {
                            if (result && result.error) {
                                console.log('Error when creating document.');
                                deferred.reject(result);
                            } else {
                                deferred.resolve(result);
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
        };

        function getIsoForm(hugoSymbol) {
            if (!isoForms) {
                DatabaseConnector.getIsoForms
                    .then(function(result) {
                        console.log(result);
                    })
            }
        }

        //var tsv is the TSV file with headers
        function tsvJSON(tsv) {

            var lines = tsv.split("\n");

            var result = [];

            var headers = lines[0].split("\t");

            for (var i = 1; i < lines.length; i++) {

                var obj = {};
                var currentline = lines[i].split("\t");

                for (var j = 0; j < headers.length; j++) {
                    obj[headers[j]] = currentline[j];
                }

                result.push(obj);

            }

            //return result; //JavaScript object
            return JSON.stringify(result); //JSON
        }

        return {
            getCancerTypesName: getCancerTypesName,
            createGene: createGene,
            createMutation: createMutation,
            createTumorType: createTumorType,
            createCancerType: createCancerType,
            createTreatment: createTreatment
        };
    });
