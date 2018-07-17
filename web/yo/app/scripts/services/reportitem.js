'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.reportItem
 * @description
 * # reportItem
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .service('reportItem', function($rootScope, _) {
        var specialKeyChars = 'o_n_c_o_k_b';

        function Item(geneName, mutation, tumorType) {
            this.geneName = geneName || 'N/A';
            this.mutation = mutation || 'N/A';
            this.tumorType = tumorType || 'N/A';
            this.overallInterpretation = 'None.';
            this.alterType = 'Somatic Mutation';
            this.mutationFreq = 'N/A';
            this.tumorTypeDrugs = 'N/A';
            this.nonTumorTypeDrugs = 'N/A';
            this.hasClinicalTrial = 'N/A';
            this.treatment = 'None.';
            this.fdaApprovedInTumor = 'None.';
            this.fdaApprovedInOtherTumor = 'None.';
            this.clinicalTrials = 'None.';
            this.additionalInfo = 'None.';
        }

        function init(geneName, mutation, tumorType, annotation, relevantCancerType) {
            var params = new Item(geneName, mutation, tumorType);
            params.hasClinicalTrial = 'NO';

            params.overallInterpretation = (geneName + ' ' + mutation + ' SUMMARY\n' +
                annotation.annotation_summary) || 'None.';
            // + '\nOTHER GENES\nNo additional somatic mutations were detected in this patient sample in the other sequenced gene regions.'
            params.geneName = geneName;
            params.mutation = mutation;
            params.diagnosis = tumorType;
            params.tumorTissueType = params.diagnosis;
            var _treatment = constructTreatment(annotation, geneName, mutation, tumorType, relevantCancerType);
            params.treatment = _treatment.length > 0 ? _treatment : 'None.';

            var _fdaInfo = constructfdaInfo(annotation, geneName, mutation, tumorType, relevantCancerType);
            if (_fdaInfo.approved.length > 0) {
                params.tumorTypeDrugs = 'YES';
                params.fdaApprovedInTumor = _fdaInfo.approved;
            } else {
                params.tumorTypeDrugs = 'NO';
                params.fdaApprovedInTumor = 'None.';
            }
            if (_fdaInfo.approvedInOther.length > 0) {
                params.nonTumorTypeDrugs = 'YES';
                params.fdaApprovedInOtherTumor = _fdaInfo.approvedInOther;
            } else {
                params.nonTumorTypeDrugs = 'NO';
                params.fdaApprovedInOtherTumor = 'None.';
            }

            var _additionalInfo = constructAdditionalInfo(annotation, geneName, mutation, tumorType, relevantCancerType);
            params.additionalInfo = _additionalInfo.length > 0 ? _additionalInfo : 'None.';

            // Set the mutation type to MUTATION, need to change after type available
            params.alterType = 'Somatic Mutation';
            return params;
        }

        function constructTreatment(annotation, geneName, mutation, tumorType, relevantCancerType) {
            var treatment = [];
            var key = '';
            var value = [];
            var object = {};
            var cancerTypeInfo = relevantCancerType || {};
            var description = '';
            var _datum;

            if (annotation.annotation_summary) {
                key = geneName + ' ' + mutation + ' SUMMARY';
                value.push({description: annotation.annotation_summary});
                object[key] = value;
                treatment.push(object);
            }

            if (cancerTypeInfo.standard_therapeutic_implications) {
                object = {};

                // Add standard therapy implication description
                if (cancerTypeInfo.standard_therapeutic_implications.general_statement && cancerTypeInfo.standard_therapeutic_implications.general_statement.sensitivity) {
                    if (angular.isArray(cancerTypeInfo.standard_therapeutic_implications.general_statement.sensitivity)) {
                        description = 'NOTICE: Found multiple general statements.';
                    } else {
                        description = cancerTypeInfo.standard_therapeutic_implications.general_statement.sensitivity.description;
                    }
                    value = [];
                    key = 'STANDARD THERAPEUTIC IMPLICATIONS';
                    if (typeof description === 'string') {
                        description = description.trim();
                    }
                    value.push({description: description});
                    object[key] = value;
                    treatment.push(object);
                } else if (cancerTypeInfo.standard_therapeutic_implications.resistant_to) {
                    // If cancer type does not have description but resistant to treatments, the header
                    // should also be added

                    object['STANDARD THERAPEUTIC IMPLICATIONS'] = [];
                    treatment.push(object);
                }

                if (cancerTypeInfo.standard_therapeutic_implications.resistant_to) {
                    _datum = cancerTypeInfo.standard_therapeutic_implications.resistant_to;

                    object = {};
                    object = findByLevelEvidence(_datum, object, '', '', ' (Resistance)');

                    _.each(object, function(item, _key) {
                        var _object = {};
                        var _newKey = _key.replace(new RegExp(specialKeyChars, 'g'), '');
                        _object[_newKey] = item;
                        treatment.push(_object);
                        _object = null;
                    });
                }
            }

            if (cancerTypeInfo.prognostic_implications && checkDescription(cancerTypeInfo.prognostic_implications)) {
                description = cancerTypeInfo.prognostic_implications.description;
                value = [];
                key = 'PROGNOSTIC IMPLICATIONS';
                object = {};
                if (angular.isString(description)) {
                    description = description.trim();
                } else if (angular.isArray(description)) {
                    var str = [];
                    var allTumors = [];

                    description.forEach(function(e) {
                        if (e['Cancer type'].toString().toLowerCase() === 'all tumors' && str.length > 0) {
                            allTumors.push(e.value.toString().trim());
                        } else {
                            str.push(e.value.toString().trim());
                        }
                    });
                    description = _.union(str, allTumors).join(' ');
                } else {
                    description = '';
                    console.log('PROGNOSTIC IMPLICATIONS --- not string --- not array');
                }
                value.push({description: description});
                object[key] = value;
                treatment.push(object);
            }

            return treatment;
        }

        function findApprovedDrug(datum, object, tumorType, key, valueExtend) {
            for (var m = 0, datumL = datum.length; m < datumL; m++) {
                var _subDatum = datum[m];
                var _key = '';
                var _obj = {};
                var _level;

                if (typeof key !== 'undefined') {
                    _key = key;
                }

                if (_subDatum.treatment) {
                    for (var i = 0; i < _subDatum.treatment.length; i++) {
                        var _treatment = _subDatum.treatment[i];
                        if (_treatment.drug) {
                            for (var j = 0; j < _treatment.drug.length; j++) {
                                var _drug = _treatment.drug[j];
                                _key += _drug.name + ' + ';
                            }
                        }
                        _key = _key.substr(0, _key.length - 3);
                        _key += ', ';
                    }
                }

                _key = _key.substr(0, _key.length - 2);

                if (valueExtend !== undefined) {
                    _key += valueExtend;
                }

                while (object.hasOwnProperty(_key)) {
                    _key += specialKeyChars;
                }

                if (_subDatum.level_of_evidence_for_patient_indication && _subDatum.level_of_evidence_for_patient_indication.level) {
                    _level = _subDatum.level_of_evidence_for_patient_indication.level;
                    _obj['Level of evidence'] = isNaN(_level) ? _level.toUpperCase() : _level;
                }
                if (checkDescription(_subDatum)) {
                    _obj.description = _subDatum.description;
                }
                if (typeof tumorType !== 'undefined' && tumorType !== '') {
                    _obj['Cancer Type'] = tumorType;
                }
                object[_key] = [_obj];
            }

            return object;
        }

        function findByLevelEvidence(datum, object, tumorType, key, valueExtend) {
            for (var m = 0, datumL = datum.length; m < datumL; m++) {
                var _subDatum = datum[m];
                var _key = '';
                var _obj = {};
                var _level;

                if (typeof key !== 'undefined') {
                    _key = key;
                }

                if (_subDatum.treatment) {
                    for (var i = 0; i < _subDatum.treatment.length; i++) {
                        var _treatment = _subDatum.treatment[i];
                        if (_treatment.drug) {
                            for (var j = 0; j < _treatment.drug.length; j++) {
                                var _drug = _treatment.drug[j];
                                _key += _drug.name + ' + ';
                            }
                        }

                        _key = _key.substr(0, _key.length - 3);
                        _key += ', ';
                    }
                }

                _key = _key.substr(0, _key.length - 2);

                if (valueExtend !== undefined) {
                    _key += valueExtend;
                }

                while (object.hasOwnProperty(_key)) {
                    _key += specialKeyChars;
                }

                if (_subDatum.level_of_evidence_for_patient_indication && _subDatum.level_of_evidence_for_patient_indication.level) {
                    _level = _subDatum.level_of_evidence_for_patient_indication.level;
                    _obj['Level of evidence'] = isNaN(_level) ? _level.toUpperCase() : _level;
                }
                if (checkDescription(_subDatum)) {
                    _obj.description = _subDatum.description;
                }
                if (typeof tumorType !== 'undefined' && tumorType !== '') {
                    _obj['Cancer Type'] = tumorType;
                }
                object[_key] = [_obj];
            }
            return object;
        }

        function constructfdaInfo(annotation, geneName, mutation, tumorType, relevantCancerType) {
            var fdaApproved = [];
            var fdaApprovedInOther = [];
            var object = {};
            var cancerTypeInfo = relevantCancerType || {};
            var attrsToDisplay = ['sensitive_to', 'resistant_to'];
            var i = 0;
            var _datum;

            if (cancerTypeInfo.standard_therapeutic_implications) {
                for (i = 0; i < attrsToDisplay.length; i++) {
                    if (cancerTypeInfo.standard_therapeutic_implications[attrsToDisplay[i]]) {
                        _datum = cancerTypeInfo.standard_therapeutic_implications[attrsToDisplay[i]];

                        object = {};
                        if (attrsToDisplay[i] === 'sensitive_to') {
                            object = findApprovedDrug(_datum, object);
                        }

                        _.each(object, function(item, _key) {
                            var _object = {};
                            var _newKey = _key.replace(new RegExp(specialKeyChars, 'g'), '');
                            _object[_newKey] = item;
                            fdaApproved.push(_object);
                            _object = null;
                        });
                    }
                }
            }

            if (annotation.cancer_type) {
                object = {};

                for (i = 0; i < annotation.cancer_type.length; i++) {
                    if (!(relevantCancerType && relevantCancerType.$type) || isNRCT(relevantCancerType.$type, annotation.cancer_type[i].$type)) {
                        if (annotation.cancer_type[i].standard_therapeutic_implications) {
                            for (var j = 0; j < attrsToDisplay.length; j++) {
                                if (annotation.cancer_type[i].standard_therapeutic_implications[attrsToDisplay[j]]) {
                                    _datum = annotation.cancer_type[i].standard_therapeutic_implications[attrsToDisplay[j]];
                                    if (attrsToDisplay[j] === 'sensitive_to') {
                                        object = findApprovedDrug(_datum, object, annotation.cancer_type[i].$type);
                                    }
                                }
                            }
                        }
                    }
                }

                _.each(object, function(item, _key) {
                    var _object = {};
                    var _newKey = _key.replace(new RegExp(specialKeyChars, 'g'), '');
                    _object[_newKey] = item;

                    for (i = 0; i < _object[_newKey].length; i++) {
                        delete _object[_newKey][i]['Level of evidence'];
                        delete _object[_newKey][i].description;
                    }

                    fdaApprovedInOther.push(_object);
                    _object = null;
                });
            }
            return {approved: fdaApproved, approvedInOther: fdaApprovedInOther};
        }

        // Is not relevant cancer type
        function isNRCT(relevent, type) {
            if (angular.isString(type)) {
                type = type.trim();
                if (typeof relevent === 'object') {
                    if (relevent instanceof Array) {
                        for (var i = 0; i < relevent.length; i++) {
                            if (relevent[i]['Cancer type'] === type) {
                                return false;
                            }
                        }
                        return true;
                    } else if (relevent.type === type) {
                        return false;
                    }
                    return true;
                } else if (angular.isString(relevent)) {
                    if (relevent.trim() === type) {
                        return false;
                    }
                    return true;
                }
                return null;
            }
            return null;
        }

        function checkDescription(datum) {
            if (datum && datum.hasOwnProperty('description') && (angular.isString(datum.description) || datum.description instanceof Array)) {
                return true;
            }
            return false;
        }

        function constructAdditionalInfo(annotation, geneName, mutation, tumorType, relevantCancerType) {
            /* eslint camelcase: ["error", {properties: "never"}]*/
            var additionalInfo = [];
            var key = '';
            var value = [];
            var object = {};

            if (annotation.gene_annotation && checkDescription(annotation.gene_annotation)) {
                value = [];
                key = 'BACKGROUND';
                object = {};
                value.push({description: removeCharsInDescription(annotation.gene_annotation.description)});
                object[key] = value;
                additionalInfo.push(object);
            }
            if (annotation.variant_effect) {
                if (angular.isObject(annotation.variant_effect)) {
                    if (angular.isArray(annotation.variant_effect)) {
                        annotation.variant_effect.forEach(function(effect) {
                            object = getMutationEffect(effect);

                            if (object) {
                                additionalInfo.push(object);
                            }
                        });
                    } else {
                        object = getMutationEffect(annotation.variant_effect);

                        if (object) {
                            additionalInfo.push(object);
                        }
                    }
                }
            }
            return additionalInfo;
        }

        function getMutationEffect(variantEffect) {
            var key = 'MUTATION EFFECT';
            var object = {};
            var value = [];

            if (angular.isObject(variantEffect) && !angular.isArray(variantEffect)) {
                var effect = variantEffect.effect || '';
                if (effect === 'null') {
                    effect = '';
                }
                value.push({
                    effect: effect,
                    description: variantEffect.description ? removeCharsInDescription(variantEffect.description) : ''
                });
                object[key] = value;
                return object;
            }
            return false;
        }

        function removeCharsInDescription(str) {
            if (typeof str === 'undefined') {
                return '';
            }
            str = str.trim();
            str = str.replace(/(\r\n|\n|\r)/gm, '');
            str = str.replace(/(\s\s*)/g, ' ');
            return str;
        }

        // Public API here
        return {
            init: init
        };
    });
