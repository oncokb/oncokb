'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.mainUtils
 * @description
 * # mainUtils
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .factory('mainUtils', function(OncoKB, _, $q, DatabaseConnector, $rootScope, ReviewResource) {
        var isoforms = {};
        var oncogeneTSG = {};

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
        /**
         * Util to send email to developer account
         * @param {string} subject The email subject
         * @param {string} content The email content
         * @return Promise
         * */
        function notifyDeveloper(subject, content) {
            sendEmail('dev.oncokb@gmail.com', subject, content);
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
            DatabaseConnector.getOncoTreeMainTypes()
                .then(function(result) {
                    if (result.data) {
                        var mainTypeList = result.data;
                        mainTypeList = _.union(mainTypeList, ['All Liquid Tumors', 'All Solid Tumors', 'All Tumors', 'Germline Disposition', 'All Pediatric Tumors', 'Other Tumor Types']);
                        var mainTypeResult = _.map(mainTypeList, function(item) {
                            return {
                                name: item,
                                id: 0
                            }
                        });
                        DatabaseConnector.getOncoTreeTumorTypesByMainTypes(mainTypeList).then(function(tumorTypesResult) {
                            if (mainTypeList.length !== tumorTypesResult.data.length) {
                                deferred.reject('The number of returned tumor types is not matched with number of main types.');
                            } else {
                                var subtypeResult = [];
                                _.each(tumorTypesResult.data, function(items) {
                                    _.each(items, function(item) {
                                        item.mainType = {
                                            name: item.mainType
                                        };
                                    });                                    
                                    subtypeResult.push(items);
                                });
                                deferred.resolve({
                                    mainTypes: mainTypeResult,
                                    tumorTypes: subtypeResult
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
            $rootScope.geneMeta.lastModifiedBy = $rootScope.me.name;
            $rootScope.geneMeta.lastModifiedAt = new Date().getTime();
        }
        function updateLastSavedToDB() {
            $rootScope.geneMeta.lastSavedBy = $rootScope.me.name;
            $rootScope.geneMeta.lastSavedAt = new Date().getTime();
        }
        return {
            getCancerTypesName: getCancerTypesName,
            containMainType: containMainType,
            getIsoform: getIsoform,
            getOncogeneTSG: getOncogeneTSG,
            getLastReviewedCancerTypesName: getLastReviewedCancerTypesName,
            sendEmail: sendEmail,
            needReview: needReview,
            developerCheck: developerCheck,
            getOncoTreeMainTypes: getOncoTreeMainTypes,
            isExpiredCuration: isExpiredCuration,
            processedInReview: processedInReview,
            notifyDeveloper: notifyDeveloper,
            updateLastModified: updateLastModified,
            updateLastSavedToDB: updateLastSavedToDB
        };
    });
