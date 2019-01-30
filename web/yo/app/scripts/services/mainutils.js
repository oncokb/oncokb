'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.mainUtils
 * @description
 * # mainUtils
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .factory('mainUtils', function(OncoKB, _, $q, DatabaseConnector, $rootScope, ReviewResource, S, UUIDjs, $routeParams) {
        var isoforms = {};
        var oncogeneTSG = {};

        /**
         * Set ISO form and gene type for a new gene.
         * @param {string} hugoSymbol Gene Hugo Symbol
         * @param {Gene} gene model created from VariantFactory
         * @return {*|h.promise|promise|r.promise|d.promise} Promise
         */
        function setIsoFormAndGeneType(gene) {
            var deferred = $q.defer();
            var hugoSymbol = gene.name;
            if (_.isString(hugoSymbol)) {
                $q.all([getIsoform(hugoSymbol), getOncogeneTSG(hugoSymbol)]).then(function(result) {
                    if (_.isArray(result)) {
                        var isoform = result[0];
                        var geneType = result[1];
                        if (isoform && isoform.error) {
                            console.error('Error when getting isoforms.', hugoSymbol);
                        } else if (isoform && isoform.gene_name) {
                            gene.isoform_override = isoform.isoform_override;
                            gene.dmp_refseq_id = isoform.dmp_refseq_id;
                        } else {
                            console.error('No isoform found!', hugoSymbol);
                        }

                        if (geneType && geneType.error) {
                            console.error('Error when getting gene type.', hugoSymbol);
                        } else if (geneType && geneType.classification) {
                            switch (geneType.classification) {
                                case 'TSG':
                                    gene.type.tsg = 'Tumor Suppressor';
                                    break;
                                case 'Oncogene':
                                    gene.type.ocg = 'Oncogene';
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            console.log('No gene type found.', hugoSymbol);
                        }
                    }
                    deferred.resolve();
                }, function(error) {
                    console.error('Failed to load isoform/geneType', error);
                });
            }
            return deferred.promise;
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
            _.each(cancerTypes, function(cancerType) {
                if (cancerType.subtype) {
                    list.push(cancerType.subtype);
                } else if (cancerType.mainType) {
                    list.push(cancerType.mainType);
                }
            });
            list = list.sort();
            return list.join(', ');
        }
        function getFullCancerTypesName(cancerTypes) {
            if (!cancerTypes) {
                return null;
            }
            var list = [];
            _.each(cancerTypes, function(cancerType) {
                var name = ['', '', ''];
                var hasInfo = false;
                if (cancerType.mainType) {
                    name[0] = cancerType.mainType;
                    hasInfo = true;
                }
                if (cancerType.code) {
                    name[1] = cancerType.code;
                    hasInfo = true;
                }
                if (cancerType.subtype) {
                    name[2] = cancerType.subtype;
                    hasInfo = true;
                }
                if(hasInfo) {
                    list.push(name.join('-'));
                }
            });
            list = list.sort();
            return list.join(', ');
        }

        function getNewCancerTypesName(cancerTypes) {
            if (!cancerTypes) {
                return null;
            }
            var list = [];
            _.each(cancerTypes, function(cancerType) {
                if (cancerType.subtype && cancerType.subtype.name) {
                    list.push(cancerType.subtype.name);
                } else if (cancerType.mainType && cancerType.mainType.name){
                    list.push(cancerType.mainType.name);
                }
            });
            list = list.sort();
            return list.join(', ');
        }

        function getFullCancerTypesNames(cancerTypes) {
            if (!cancerTypes) {
                return null;
            }
            var list = [];
            _.each(cancerTypes, function(cancerType) {
                var name = ['', '', ''];
                var hasInfo = false;
                if (cancerType.mainType && cancerType.mainType.name) {
                    name[0] = cancerType.mainType.name;
                    hasInfo = true;
                }
                if (cancerType.subtype && cancerType.subtype.code) {
                    name[1] = cancerType.subtype.code;
                    hasInfo = true;
                }
                if (cancerType.subtype && cancerType.subtype.name) {
                    name[2] = cancerType.subtype.name;
                    hasInfo = true;
                }
                if(hasInfo) {
                    list.push(name.join('-'));
                }
            });
            list = list.sort();
            return list.join(', ');
        }

        function hasDuplicateCancerTypes(cancerTypes) {
            var result = false;
            if (!cancerTypes) {
                return false;
            }
            var list = [];
            var tempName = '';
            _.some(cancerTypes, function(cancerType) {
                tempName = '';
                if (cancerType.subtype && cancerType.subtype.name) {
                    tempName = cancerType.subtype.name;
                } else if (cancerType.mainType && cancerType.mainType.name) {
                    tempName = cancerType.mainType.name;
                }
                if (list.indexOf(tempName) !== -1) {
                    result = true;
                    return true;
                }
                if (tempName) {
                    list.push(tempName);
                }
            });
            return result;
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
        /**
         * Util to send email to developer account
         * @param {string} subject The email subject
         * @param {string} content The email content
         * @return Promise
         * */
        function notifyDeveloper(subject, content) {
            sendEmail('dev.oncokb@gmail.com', subject, content);
        }
        /**
         * Check whether user is developer
         * @param {string} userName The user name
         * @return {boolean} whether user is developer
         */
        function developerCheck(userName) {
            var result = false;
            if (!userName) {
                return result;
            }
            var developers = ['Hongxin Zhang', 'Jianjiong Gao', 'Jiaojiao Wang', 'Jing Su'];
            _.some(developers, function(item) {
                if (item.toLowerCase() === userName.toLowerCase()) {
                    result = true;
                    return true;
                }
            });
            return result;
        }

        /**
         * Get Oncotree main types and sub tumor types.
         * return {mainTypes: mainTypes, tumorTypes: tumorTypes}
         * **/
        function getOncoTreeMainTypes() {
            var deferred = $q.defer();
            if ($rootScope.meta.mainType && $rootScope.meta.tumorTypes) {
                deferred.resolve({
                    mainTypes: $rootScope.meta.mainType,
                    tumorTypes: $rootScope.meta.tumorTypes
                });
            } else {
                DatabaseConnector.getMainTypes().then(function(result1) {
                    var mainTypeResult = _.map(result1, function(item) {
                        return {
                            name: item,
                            code: 0
                        };
                    });
                    DatabaseConnector.getSubTypes().then(function(result2) {
                        var subtypeResult = [];
                        _.each(result1, function(mainTypeName) {
                            var tempArr = [];
                            _.each(result2, function(item) {
                                if (item.mainType && item.mainType.name && item.mainType.name === mainTypeName) {
                                    tempArr.push(item);
                                }
                            });
                            subtypeResult.push(tempArr);
                        });
                        $rootScope.meta.mainType = mainTypeResult;
                        $rootScope.meta.tumorTypes = subtypeResult;
                        deferred.resolve({
                            mainTypes: mainTypeResult,
                            tumorTypes: subtypeResult
                        });
                    }, function(error) {
                        deferred.reject(error);
                    });
                }, function(error) {
                    deferred.reject(error);
                });
            }
            return deferred.promise;
        }

        function exactSameTumorType(t1, t2) {
            if(t1 === t2) {
                return true;
            }
            if(JSON.stringify(t1) === JSON.stringify(t2)){
                return true;
            }
            if (t1.mainType && t2.mainType) {
                var flag = t1.mainType.name === t2.mainType.name;

                if (t1.subtype && t2.subtype) {
                    if(flag) {
                        if (t1.subtype.name === t2.subtype.name && t1.subtype.code === t2.subtype.code) {
                            return true;
                        }
                    }
                } else if (flag) {
                    return true;
                }
            }
            return false;
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
            // uuid = uuid.getText();
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
        function updateLastModified() {
            firebase.database().ref('Meta/' + $routeParams.geneName).update({
                lastModifiedBy: $rootScope.me.name,
                lastModifiedAt: new Date().getTime()
            });
        }
        function updateLastSavedToDB() {
            firebase.database().ref('Meta/' + $routeParams.geneName).update({
                lastSavedBy: $rootScope.me.name,
                lastSavedAt: new Date().getTime()
            });
        }
        function updateMovingFlag(flag) {
            firebase.database().ref('Meta/' + $routeParams.geneName).update({
                movingSection: flag
            });
        }
        function setUUIDInReview(uuid) {
            var tempObj = {};
            tempObj[uuid] = true;
            firebase.database().ref('Meta/' + $routeParams.geneName + '/review').update(tempObj);
        }
        function deleteUUID(uuid) {
            firebase.database().ref('Meta/' + $routeParams.geneName + '/review/' + uuid).remove();
        }
        function getVUSData(vus, excludeComments) {
            var vusData = angular.copy(vus);
            var vusDataArray = [];
            excludeComments = _.isBoolean(excludeComments) ? excludeComments : false;
            _.each(vusData, function(vusItem) {
                if (excludeComments) {
                    delete vusItem.name_comments;
                }
                vusDataArray.push(vusItem);
            });
            return vusDataArray;
        }

        // get history data
        function getHistoryData(history) {
            var result = {};
            if (history && _.isArray(history.keys())) {
                _.each(history.keys(), function(key) {
                    if (['api'].indexOf(key) !== -1) {
                        result[key] = Array.from(history.get(key));
                    } else {
                        result[key] = history.get(key);
                    }
                });
            }
            return result;
        }
        function processData(data, keys, excludeComments, onlyReviewedContent) {
            _.each(keys, function(key) {
                if (excludeComments) {
                    delete data[key+'_comments'];
                }
                if (onlyReviewedContent && data[key+'_review'] && !_.isUndefined(data[key+'_review'].lastReviewed)) {
                    data[key] = data[key+'_review'].lastReviewed;
                }
            });
        }
        function shouldExclude(onlyReviewedContent, reviewObj) {
            return reviewObj && (onlyReviewedContent && reviewObj.added == true || !onlyReviewedContent && reviewObj.removed == true);
        }
        function getGeneData(geneData, excludeComments, onlyReviewedContent) {
            var gene = angular.copy(geneData);
            excludeComments = _.isBoolean(excludeComments) ? excludeComments : false;
            onlyReviewedContent = _.isBoolean(onlyReviewedContent) ? onlyReviewedContent : false;
            processData(gene, ['summary', 'background'], excludeComments, onlyReviewedContent);
            processData(gene.type, ['tsg', 'ocg'], excludeComments, onlyReviewedContent);
            var tempMutations = [];
            var tempTumors = [];
            var tempTreatments = [];
            _.each(gene.mutations, function(mutation, mutationIndex) {
                if (shouldExclude(onlyReviewedContent, mutation.name_review)) {
                    tempMutations.push(mutation);
                    return true;
                }
                processData(mutation, ['name'], excludeComments, onlyReviewedContent);
                processData(mutation.mutation_effect, ['oncogenic', 'effect', 'description'], excludeComments, onlyReviewedContent);
                tempTumors = [];
                _.each(mutation.tumors, function(tumor, tumorIndex) {
                    if (shouldExclude(onlyReviewedContent, tumor.cancerTypes_review)) {
                        tempTumors.push(tumor);
                        return true;
                    }
                    // process tumor cancerTypes
                    processData(tumor, ['summary'], excludeComments, onlyReviewedContent);
                    processData(tumor.diagnostic, ['level', 'description'], excludeComments, onlyReviewedContent);
                    processData(tumor.prognostic, ['level', 'description'], excludeComments, onlyReviewedContent);
                    _.each(tumor.TIs, function(ti) {
                        processData(ti, ['description'], excludeComments, onlyReviewedContent);
                        tempTreatments = [];
                        _.each(ti.treatments, function(treatment, treatmentIndex) {
                            if (shouldExclude(onlyReviewedContent, treatment.name_review)) {
                                tempTreatments.push(treatment);
                                return true;
                            }
                            processData(treatment, ['name', 'level', 'propagation', 'indication', 'description'], excludeComments, onlyReviewedContent);
                        });
                        _.each(tempTreatments, function(item) {
                            var index = ti.treatments.indexOf(item);
                            if (index !== -1) {
                                ti.treatments.splice(index, 1);
                            }
                        });
                    });
                });
                _.each(tempTumors, function(item) {
                    var index = mutation.tumors.indexOf(item);
                    if (index !== -1) {
                        mutation.tumors.splice(index, 1);
                    }
                });
            });
            _.each(tempMutations, function(item) {
                var index = gene.mutations.indexOf(item);
                if (index !== -1) {
                    gene.mutations.splice(index, 1);
                }
            });
            return gene;
        }

        function mostRecentItem(reviewObjs, include) {
            var mostRecent = -1;
            for (var i = 0; i < reviewObjs.length; i++) {
                if (!include) {
                    // This is designed to handle the reviewObj with systematically set updatetime
                    // when 'include' equals true, it will use all reviewObj in the list
                    // otherwise, we will only use the reviewObj with updatedBy info.
                    if (!reviewObjs[i] || !reviewObjs[i].updatedBy) continue;
                }
                var currentItemTime;
                if (reviewObjs[i] && reviewObjs[i].updateTime) {
                    currentItemTime = new Date(reviewObjs[i].updateTime);
                }
                // we only continue to check if current item time is valid
                if (currentItemTime instanceof Date && !isNaN(currentItemTime.getTime())) {
                    if (mostRecent < 0) {
                        mostRecent = i;
                    } else {
                        // reset mostRect time when current item time is closer
                        var mostRecentTime = new Date(reviewObjs[mostRecent].updateTime);
                        if(mostRecentTime < currentItemTime) {
                            mostRecent = i;
                        }
                    }
                }
            }
            if (mostRecent < 0) {
                return 0;
            }
            return mostRecent;
        }
        function trimMutationName(mutation) {
            if (typeof mutation === 'string') {
                if (mutation.indexOf('p.') === 0) {
                    mutation = mutation.substring(2);
                }
            }
            return mutation;
        }
        function getCaseNumber() {
            var date = new Date();
            return date.getTime();
        }
        function getTimeStamp(str) {
            var date = new Date(str);
            if(date instanceof Date && !isNaN(date.getTime())) {
                return date.getTime();
            } else {
                return 0;
            }
        }
        function calculateDiff(oldContent, newContent) {
            var dmp = new diff_match_patch();
            var diff = dmp.diff_main(OncoKB.utils.getString(oldContent), OncoKB.utils.getString(newContent));
            dmp.diff_cleanupSemantic(diff);
            return dmp.diff_prettyHtml(diff);
        }
        function getOldGeneType(type) {
            var oldContent = '';
            if (_.isUndefined(type.tsg_review) || _.isUndefined(type.tsg_review.lastReviewed)) {
                oldContent = type.tsg;
            } else if (!_.isUndefined(type.tsg_review.lastReviewed)) {
                oldContent = type.tsg_review.lastReviewed;
            }
            if (_.isUndefined(type.ocg_review) || _.isUndefined(type.ocg_review.lastReviewed)) {
                oldContent = oldContent + '  ' + type.ocg;
            } else if (!_.isUndefined(type.ocg_review.lastReviewed)) {
                oldContent = oldContent + '  ' + type.ocg_review.lastReviewed;
            }

            return oldContent;
        }
        function clearCollaboratorsByName(myName) {
            firebase.database().ref('Meta/collaborators/' + myName).set([]).then(function (result) {
            }).catch(function (error) {
                console.log(error);
            });
        }
        function therapyStrToArr(key){
            return key.split(",").map(function(element){
                return element.trim().split(" + ")
            });
        }
        function getDrugNameByUuids(keys, drugList) {
            return keys.map(function (element) {
                return element.map(function (key) {
                    return drugList[key].drugName;
                }).join(" + ");
            }).join(", ");
        }
        function drugUuidtoName(key, drugList){
            if (key != undefined){
                var keys = therapyStrToArr(key);
                return getDrugNameByUuids(keys, drugList);
            }
        }
        function getKeysWithoutFirebasePrefix(array){
            return _.keys(array).filter(function (item) {
                return item.indexOf("$") !== 0;
            });
        }
        return {
            setIsoFormAndGeneType: setIsoFormAndGeneType,
            getCancerTypesName: getCancerTypesName,
            getFullCancerTypesName: getFullCancerTypesName,
            getNewCancerTypesName: getNewCancerTypesName,
            getFullCancerTypesNames: getFullCancerTypesNames,
            exactSameTumorType: exactSameTumorType,
            hasDuplicateCancerTypes: hasDuplicateCancerTypes,
            containMainType: containMainType,
            getIsoform: getIsoform,
            getOncogeneTSG: getOncogeneTSG,
            getLastReviewedCancerTypesName: getLastReviewedCancerTypesName,
            sendEmail: sendEmail,
            developerCheck: developerCheck,
            getOncoTreeMainTypes: getOncoTreeMainTypes,
            isExpiredCuration: isExpiredCuration,
            processedInReview: processedInReview,
            notifyDeveloper: notifyDeveloper,
            updateLastModified: updateLastModified,
            updateLastSavedToDB: updateLastSavedToDB,
            trimMutationName: trimMutationName,
            getCaseNumber: getCaseNumber,
            getGeneData: getGeneData,
            getVUSData: getVUSData,
            getTextString: OncoKB.utils.getString,
            mostRecentItem: mostRecentItem,
            getHistoryData: getHistoryData,
            setUUIDInReview: setUUIDInReview,
            deleteUUID: deleteUUID,
            updateMovingFlag: updateMovingFlag,
            processData: processData,
            shouldExclude: shouldExclude,
            getTimeStamp: getTimeStamp,
            calculateDiff: calculateDiff,
            getOldGeneType: getOldGeneType,
            clearCollaboratorsByName: clearCollaboratorsByName,
            therapyStrToArr: therapyStrToArr,
            getDrugNameByUuids: getDrugNameByUuids,
            drugUuidtoName: drugUuidtoName,
            getKeysWithoutFirebasePrefix: getKeysWithoutFirebasePrefix
        };
    });
