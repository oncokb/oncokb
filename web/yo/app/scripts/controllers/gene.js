'use strict';

angular.module('oncokbApp')
    .controller('GeneCtrl', ['_', 'S', '$resource', '$interval', '$timeout', '$scope', '$rootScope', '$location', '$route', '$routeParams', '$window', '$q', 'dialogs', 'OncoKB', 'DatabaseConnector', 'SecretEmptyKey', '$sce', 'jspdf', 'FindRegex', 'mainUtils', 'ReviewResource', 'loadFiles', '$firebaseObject', '$firebaseArray', 'FirebaseModel', 'firebaseConnector', 'user', 'numOfReviewItems', 'checkNameChange', 'drugMapUtils', 'firebasePathUtils',
        function (_, S, $resource, $interval, $timeout, $scope, $rootScope, $location, $route, $routeParams, $window, $q, dialogs, OncoKB, DatabaseConnector, SecretEmptyKey, $sce, jspdf, FindRegex, mainUtils, ReviewResource, loadFiles, $firebaseObject, $firebaseArray, FirebaseModel, firebaseConnector, user, numOfReviewItems, checkNameChange, drugMapUtils, firebasePathUtils) {
            checkReadPermission();
            // Check permission for user who can only read and write specific genes.
            if(!$rootScope.drugList){
                // Loading all drugs info
                $firebaseObject(firebaseConnector.ref("Drugs/")).$bindTo($rootScope, "drugList").then(function () {
                }, function (error) {
                    dialogs.error('Error', 'Failed to load drugs information. Please Contact developer and stop curation.');
                });
            }
            function checkReadPermission() {
                if (_.isUndefined($rootScope.metaData)) {
                    loadFiles.load(['meta']).then(function() {
                        checkValidUrl();
                    }, function() {
                        console.log('fail to load meta file');
                    });
                } else {
                    checkValidUrl();
                }
                if (!$rootScope.me.admin && $rootScope.me.genes.read !== 'all' && !$rootScope.me.genes.read.includes($routeParams.geneName)) {
                    dialogs.notify('Warning', 'Sorry, you don\'t have permission to read this gene.');
                    $location.url('/genes');
                }
            }
            // Remove current collaborator when user changes url directly.
            $scope.$on('$locationChangeStart', function(event, next, current) {
                // Once user logged out, we do not need to remove current gene since we'll clear Meta/collaborators/currentUser in logout().
                if ($rootScope.isAuthorizedUser && next && next !== current) {
                    removeCollaborator();
                }
            });
            // Remove current collaborator when user closes tab.
            $window.onbeforeunload = function () {
                removeCollaborator();
            };
            function checkValidUrl() {
                $scope.hugoSymbols = _.without(_.keys($rootScope.metaData), 'collaborators');
                if (!$scope.hugoSymbols.includes($routeParams.geneName)) {
                    $location.url('/genes');
                } else {
                    checkNameChange.clear();
                    window.localStorage.geneName = $routeParams.geneName;
                    populateBindings();
                    getSuggestedMutations();
                    getOncoTreeMainTypes();
                }
            }
            function removeCollaborator() {
                var myName = $rootScope.me.name.toLowerCase();
                var genesOpened = _.without($scope.collaboratorsMeta[myName], $routeParams.geneName);
                firebase.database().ref('Meta/collaborators/' + myName).set(genesOpened).then(function (result) {
                }).catch(function (error) {
                    console.log(error);
                });
            }
            function isValidVariant(originalVariantName) {
                var variantName = originalVariantName.trim().toLowerCase();
                var validMutation = true;
                var message = originalVariantName + ' ';
                var mutationNameBlackList = [
                    'activating mutations',
                    'activating mutation',
                    'inactivating mutations',
                    'inactivating mutation'
                ];
                if (mutationNameBlackList.indexOf(variantName) !== -1) {
                    validMutation = false;
                    message += 'is a not allowed name!';
                }
                if (validMutation) {
                    _.some($scope.gene.mutations, function (mutation) {
                        if (mutation.name.toLowerCase() === variantName) {
                            validMutation = false;
                            if (mutation.name_review && mutation.name_review.removed === true) {
                                message += 'just got removed, we will reuse the old one';
                                delete mutation.name_review.removed;
                                mainUtils.deleteUUID(mutation.name_uuid);
                            } else {
                                message += 'has already been added in the mutation section!';
                            }
                            return true;
                        }
                    });
                }
                if (validMutation) {
                    _.some($scope.vusItems, function (vusItem) {
                        if (vusItem.name.toLowerCase() === variantName) {
                            validMutation = false;
                            message += 'has already been added in the VUS section!';
                            return true;
                        }
                    });
                }
                if (!validMutation) {
                    dialogs.notify('Warning', message);
                }
                return validMutation;
            }
            $scope.addMutation = function (newMutationName) {
                if (isValidVariant(newMutationName)) {
                    var mutation = new FirebaseModel.Mutation(newMutationName);
                    mutation.name_review = {
                        updatedBy: $rootScope.me.name,
                        updateTime: new Date().getTime(),
                        added: true
                    };
                    if (!$scope.gene.mutations) {
                        $scope.gene.mutations = [mutation];
                    } else {
                        $scope.gene.mutations.push(mutation);
                    }
                    mainUtils.setUUIDInReview(mutation.name_uuid);
                }
            };
            /**
             * This function is used to calculate 2 types of mutation messages we want to indicate in the mutation section header.
             * The first one is about the mutation name validation result such as duplicated mutation or existed in VUS section. The result is stored in mutationMessages, and updated in real time as editing.
             * The other one is about the detailed mutation content inside when first loaded the gene page, and the result is stored in mutationContent.
             * **/
            var sortedLevel = _.keys($rootScope.meta.levelsDesc).sort();
            $scope.getMutationMessages = function () {
                $scope.mutationContent = {};
                for (var i = 0; i < $scope.mutations.length; i++) {
                    var mutation = $scope.mutations[i];
                    $scope.indicateMutationContent(mutation);
                }
            };
            $scope.indicateMutationContent = function(mutation) {
                var uuid = mutation.name_uuid;
                $scope.mutationContent[uuid] = {
                    TT: 0,
                    levels: [],
                    TTS: 0,
                    DxS: 0,
                    PxS: 0
                };
                if (mutation.tumors) {
                    for (var j = 0; j < mutation.tumors.length; j++) {
                        var tumor = mutation.tumors[j];
                        if (!(tumor.cancerTypes_review && tumor.cancerTypes_review.removed)) {
                            $scope.mutationContent[uuid].TT++;
                            if (tumor.summary) {
                                $scope.mutationContent[uuid].TTS++;
                            }
                            if (tumor.diagnosticSummary) {
                                $scope.mutationContent[uuid].DxS++;
                            }
                            if (tumor.prognosticSummary) {
                                $scope.mutationContent[uuid].PxS++;
                            }
                            for (var m = 0; m < tumor.TIs.length; m++) {
                                var ti = tumor.TIs[m];
                                if (ti.treatments) {
                                    for (var n = 0; n < ti.treatments.length; n++) {
                                        var treatment = ti.treatments[n];
                                        if (!(treatment.name_review && treatment.name_review.removed)) {
                                            $scope.mutationContent[uuid].levels.push(treatment.level);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if ($scope.mutationContent[uuid].TT > 0) {
                    $scope.mutationContent[uuid].levels.sort(function (a, b) {
                        return sortedLevel.indexOf(a) - sortedLevel.indexOf(b);
                    });
                    $scope.mutationContent[uuid].result = $scope.mutationContent[uuid].TT + 'x TT';
                    if ($scope.mutationContent[uuid].TTS > 0) {
                        $scope.mutationContent[uuid].result += ', ' + $scope.mutationContent[uuid].TTS + 'x TTS';
                    }
                    if ($scope.mutationContent[uuid].DxS > 0) {
                        $scope.mutationContent[uuid].result += ', ' + $scope.mutationContent[uuid].DxS + 'x DxS';
                    }
                    if ($scope.mutationContent[uuid].PxS > 0) {
                        $scope.mutationContent[uuid].result += ', ' + $scope.mutationContent[uuid].PxS + 'x PxS';
                    }
                    if ($scope.mutationContent[uuid].levels.length > 0) {
                        $scope.mutationContent[uuid].levels = _.map(_.uniq($scope.mutationContent[uuid].levels), function (level) {
                            return '<span style="color: ' + $rootScope.meta.colorsByLevel['Level_' + level] + '">' + level + '</span>';
                        });
                        $scope.mutationContent[uuid].result += ', Levels: ' + $scope.mutationContent[uuid].levels.join(', ') + '</span>';
                    }
                }
            }
            /**
             * This function is used to calculate 2 types of tumor messages we want to indicate in the tumor section header.
             * The first one is about the tumor name validation result such as duplicated tumor. The result is stored in tumorMessages, and updated in real time as editing.
             * The other one is about the detailed treatment info inside when first open the tumor section, and the result is stored in tumorContent.
             * **/
            $scope.getTumorContent = function (mutation) {
                $scope.tumorContent = {};
                if (!mutation.tumors) {
                    return;
                }
                for (var j = 0; j < mutation.tumors.length; j++) {
                    var tumor = mutation.tumors[j];
                    $scope.indicateTumorContent(tumor);
                }
            };
            $scope.indicateTumorContent = function(tumor) {
                var uuid = tumor.cancerTypes_uuid;
                $scope.tumorContent[uuid] = {
                    result: ''
                };
                if (tumor.summary) {
                    $scope.tumorContent[uuid].result = '1x TTS';
                }
                if (tumor.diagnosticSummary) {
                    $scope.tumorContent[uuid].result += $scope.tumorContent[uuid].result.length > 0 ? ', 1x DxS' : '1x DxS';
                }
                if (tumor.prognosticSummary) {
                    $scope.tumorContent[uuid].result += $scope.tumorContent[uuid].result.length > 0 ? ', 1x PxS' : '1x PxS';
                }
                for (var m = 0; m < tumor.TIs.length; m++) {
                    var ti = tumor.TIs[m];
                    if (!ti.treatments) {
                        continue;
                    }
                    for (var n = 0; n < ti.treatments.length; n++) {
                        var treatment = ti.treatments[n];
                        if (!(treatment.name_review && treatment.name_review.removed)) {
                            var tempLevel = treatment.level;
                            if ($scope.tumorContent[uuid][tempLevel]) {
                                $scope.tumorContent[uuid][tempLevel]++;
                            } else {
                                $scope.tumorContent[uuid][tempLevel] = 1;
                            }
                        }
                    }
                }
                var levels = _.without(_.keys($scope.tumorContent[uuid]), 'result');
                if (levels.length > 0) {
                    levels.sort(function (a, b) {
                        return sortedLevel.indexOf(a) - sortedLevel.indexOf(b);
                    });
                    var result = [];
                    _.each(levels, function (level) {
                        result.push('<span>' + $scope.tumorContent[uuid][level] + 'x </span><span style="color: ' + $rootScope.meta.colorsByLevel['Level_' + level] + '">Level ' + level + '</span>');
                    });
                    if ($scope.tumorContent[uuid].result) {
                        $scope.tumorContent[uuid].result += ', ';
                    }
                    $scope.tumorContent[uuid].result += result.join('; ');
                }
            }
            $scope.getTreatmentMessages = function (mutation, tumor, ti) {
                var mutationName = mutation.name.toLowerCase();
                var tumorName = $scope.getCancerTypesName(tumor).toLowerCase();
                var tiName = ti.name.toLowerCase();
                var tempNameList = [];
                if (!$scope.treatmentMessages) {
                    $scope.treatmentMessages = {};
                }
                if (!$scope.treatmentMessages[mutationName]) {
                    $scope.treatmentMessages[mutationName] = {};
                }
                if (!$scope.treatmentMessages[mutationName][tumorName]) {
                    $scope.treatmentMessages[mutationName][tumorName] = {};
                }
                $scope.treatmentMessages[mutationName][tumorName][tiName] = {};
                for (var n = 0; n < ti.treatments.length; n++) {
                    var treatmentName = ti.treatments.get(n).name.toLowerCase();
                    if (tempNameList.indexOf(treatmentName) === -1) {
                        tempNameList.push(treatmentName);
                    } else {
                        $scope.treatmentMessages[mutationName][tumorName][tiName][treatmentName] = 'Therapy exists';
                    }
                }
            }

            $scope.stateComparator = function (state, viewValue) {
                return viewValue === SecretEmptyKey || (String(state)).toLowerCase().indexOf((String(viewValue)).toLowerCase()) > -1;
            };
            $scope.vusUpdate = function () {
                if ($scope.status.vusUpdateTimeout) {
                    $timeout.cancel($scope.status.vusUpdateTimeout);
                }
                $scope.status.vusUpdateTimeout = $timeout(function () {
                    var vusData = JSON.stringify(mainUtils.getVUSData($scope.vusItems, true));
                    DatabaseConnector.updateVUS($routeParams.geneName, vusData, function(result) {
                        console.log('success saving vus to database');
                        mainUtils.updateLastSavedToDB();
                    }, function(error) {
                        console.log('error happened when saving VUS to DB', error);
                    });
                }, 2000);
            };
            function parseMutationString(mutationStr) {
                mutationStr = mutationStr.replace(/\([^\)]+\)/g, '');
                var parts = _.map(mutationStr.split(','), function (item) {
                    return item.trim();
                });
                var altResults = [];
                var proteinChange = '';
                var displayName = '';

                for (var i = 0; i < parts.length; i++) {
                    if (!parts[i]) continue;
                    if (parts[i].indexOf('[') === -1) {
                        proteinChange = parts[i].trim();
                        displayName = parts[i].trim();
                    } else {
                        var l = parts[i].indexOf('[');
                        var r = parts[i].indexOf(']');
                        proteinChange = parts[i].substring(0, l).trim();
                        displayName = parts[i].substring(l + 1, r).trim();
                    }

                    if (proteinChange.indexOf('/') === -1) {
                        altResults.push({
                            alteration: proteinChange,
                            name: displayName,
                            gene: {
                                hugoSymbol: $scope.gene.name
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
                                    hugoSymbol: $scope.gene.name
                                }
                            });
                        }
                    }
                }
                return altResults;
            }

            $rootScope.reviewMode = false;
            /**
             * Check if a section needs to be displayed or not.
             * For instance, would be used to check if Mutation Effect section needs to be displayed.
             * If it is a section with only one item, we still treat it as section by using displayCheck(), e.g. Prevelance
             * However, if it is just a single item without section frame work, we use displayPrecisely(), e.g. Tumor Summary, TI Description
             * ***/
            $scope.displayCheck = function (uuid, reviewObj) {
                // regular mode check
                if (!$rootScope.reviewMode) {
                    if (reviewObj && reviewObj.removed === true) {
                        return false;
                    }
                    return true;
                }
                // review mode check
                return uuid && ($scope.sectionUUIDs.indexOf(uuid) !== -1 || mainUtils.processedInReview('inside', uuid));
            };
            /**
             * Check if each item inside a section needs to be displayed or not
             * For instance, there are three items Oncogenic, Effect and Description inside Mutation Effect section.
             * And this function will be used to check each item needs to be displayed or not.
             * ***/
            $scope.displayPrecisely = function (uuid) {
                if (!$rootScope.reviewMode) return true;
                else {
                    // review mode logic checks
                    if (mainUtils.processedInReview('inside', uuid)) {
                        return true;
                    } else if ($scope.geneMeta.review[uuid]) {
                        if (!mainUtils.processedInReview('precise', uuid)) {
                            ReviewResource.precise.push(uuid);
                        }
                        return true;
                    } else {
                        return mainUtils.processedInReview('precise', uuid);
                    }
                }
            };
            $scope.review = function () {
                if ($rootScope.reviewMode) {
                    $scope.exitReview();
                } else {
                    numOfReviewItems.clear();
                    var collaborators = $rootScope.collaborators;
                    var otherCollaborators = [];
                    _.each(collaborators, function (collaborator) {
                        if (collaborator && collaborator.name !== $rootScope.me.name) {
                            otherCollaborators.push(collaborator.name);
                        }
                    });
                    if (otherCollaborators.length > 0) {
                        var dlg = dialogs.confirm('Reminder', otherCollaborators.join(', ') + ((otherCollaborators.length > 1) ? ' are' : ' is') + ' currently working on this gene document. Entering review mode will disable them from editing.');
                        dlg.result.then(function () {
                            if (!$scope.geneMeta.review.currentReviewer) {
                                prepareReviewItems();
                            }
                        });
                    } else {
                        prepareReviewItems();
                    }
                }
            };
            $scope.exitReview = function () {
                numOfReviewItems.clear();
                checkNameChange.clear();
                $scope.geneMeta.review.currentReviewer = '';
                $rootScope.fileEditable = true;
                evidencesAllUsers = {};
                // close all mutations
                $timeout(function() {
                    $rootScope.reviewMode = false;
                    $scope.setSectionOpenStatus('close', $scope.sectionUUIDs);
                }, 200);
            };
            function resetReviewResources() {
                ReviewResource.accepted = [];
                ReviewResource.rejected = [];
                ReviewResource.rollback = [];
                ReviewResource.loading = [];
                ReviewResource.inside = [];
                ReviewResource.updated = [];
                ReviewResource.nameChanged = [];
                ReviewResource.added = [];
                ReviewResource.removed = [];
                ReviewResource.mostRecent = {};
                ReviewResource.precise = [];
            }
            $scope.developerCheck = function () {
                return mainUtils.developerCheck($rootScope.me.name);
            };
            $scope.getNameStyle = function(type) {
                if (!$scope.reviewMode) {
                    return {float: 'left'};
                } else if (type === 'mutation') {
                    return {'margin-top': '20px'};
                }
            };
            /**
             * This function is used to find the most recent update from a section change. e.g. There are 3 items under Prognostic section, and they might get changed at very different time.
             * And we will find the one changed most recently and store them in ReviewResource.mostRecent mapping, so it could be shared across directives and controllers
             * */
            function setUpdatedSignature(tempArr, uuid) {
                if (uuid) {
                    var uuidString = uuid;
                    var mostRecent = mainUtils.mostRecentItem(tempArr);
                    ReviewResource.mostRecent[uuidString] = {
                        updatedBy: tempArr[mostRecent].updatedBy,
                        updateTime: tempArr[mostRecent].updateTime
                    };
                    userNames.push(tempArr[mostRecent].updatedBy);
                }
            }

            var evidencesAllUsers = {};

            function formEvidencesPerUser(userName, type, mutation, tumor, TI, treatment) {
                var getEvidenceResult = $scope.getEvidence(type, mutation, tumor, TI, treatment);
                var evidences = getEvidenceResult.evidences;
                var historyData = getEvidenceResult.historyData;
                if (!_.isEmpty(evidences)) {
                    evidencesAllUsers[userName].updatedEvidences = _.extend(evidencesAllUsers[userName].updatedEvidences, evidences);
                    evidencesAllUsers[userName].historyData.update.push(historyData);
                    evidencesAllUsers[userName].updatedEvidenceModels.push([type, mutation, tumor, TI, treatment]);
                }
            }
            $scope.getButtonContent = function (x) {
                if (x) {
                    return $scope.status[x].savingAll ? 'Saving ' + '<i class="fa fa-spinner fa-spin"></i>' : 'Accept All Changes from <b>' + x + '</b>';
                }
                return '';
            };
            function isChangedSection(uuids) {
                var result = false;
                _.some(uuids, function (uuid) {
                    if (uuid && $scope.geneMeta.review[uuid]) {
                        result = true;
                        return true;
                    }
                });
                return result;
            }
            var userNames = [];
            function getReviewInfo() {
                var sectionUUIDs_ = [];
                var hasReviewContent_ = false;
                var mutationChanged_ = false;
                userNames = [];
                var geneEviKeys = ['summary', 'type', 'background'];
                _.each(geneEviKeys, function (item) {
                    var changeHappened = false;
                    var userName = '';
                    if (item === 'type') {
                        if ($scope.geneMeta.review[$scope.gene.type.tsg_uuid] || $scope.geneMeta.review[$scope.gene.type.ocg_uuid]) {
                            setUpdatedSignature([$scope.gene.type.ocg_review, $scope.gene.type.tsg_review], $scope.gene.type_uuid);
                            changeHappened = true;
                        }
                    } else {
                        if ($scope.geneMeta.review[$scope.gene[item + '_uuid']] && $scope.gene[item + '_review'].updatedBy) {
                            userName = $scope.gene[item + '_review'].updatedBy;
                            changeHappened = true;
                        }
                    }
                    if (changeHappened === true) {
                        hasReviewContent_ = true;
                        userNames.push(userName);
                        sectionUUIDs_.push($scope.gene[item + '_uuid']);
                        ReviewResource.updated.push($scope.gene[item + '_uuid']);
                    }
                });
                var mutationChanged = false;
                var tumorChanged = false;
                var tiChanged = false;
                var treatmentChanged = false;
                var mutationSectionChanged = false;
                var tumorSectionChanged = false;
                var treatmentSectionChanged = false;
                var tempArr = [];
                _.each($scope.mutations, function (mutation) {
                    mutationSectionChanged = false;
                    if (mutation.name_review) {
                        if (mutation.name_review.added) {
                            ReviewResource.added.push(mutation.name_uuid);
                            mutationSectionChanged = true;
                        }
                        if (mutation.name_review.removed) {
                            ReviewResource.removed.push(mutation.name_uuid);
                            mutationSectionChanged = true;
                        }
                    }
                    if (mutationSectionChanged) {
                        hasReviewContent_ = true;
                        userNames.push(mutation.name_review.updatedBy);
                        tempArr = collectUUIDs('mutation', mutation, [], 'sectionOnly');
                        sectionUUIDs_ = _.union(sectionUUIDs_, tempArr);
                        tempArr = collectUUIDs('mutation', mutation, [], 'insideOnly');
                        ReviewResource.inside = _.union(ReviewResource.inside, tempArr);
                        mutationChanged_ = true;
                        return {
                            sectionUUIDs: sectionUUIDs_,
                            hasReviewContent: hasReviewContent_,
                            mutationChanged: mutationChanged_
                        };
                    }
                    if (isChangedSection([mutation.mutation_effect.oncogenic_uuid, mutation.mutation_effect.effect_uuid, mutation.mutation_effect.description_uuid])) {
                        tempArr = [mutation.mutation_effect.oncogenic_review, mutation.mutation_effect.effect_review, mutation.mutation_effect.description_review];
                        sectionUUIDs_.push(mutation.mutation_effect_uuid);
                        ReviewResource.updated.push(mutation.mutation_effect_uuid);
                        mutationChanged = true;
                        setUpdatedSignature(tempArr, mutation.mutation_effect_uuid);
                    }
                    _.each(mutation.tumors, function (tumor) {
                        tumorSectionChanged = false;
                        if (tumor.cancerTypes_review) {
                            if (tumor.cancerTypes_review.added) {
                                ReviewResource.added.push(tumor.cancerTypes_uuid);
                                tumorSectionChanged = true;
                            }
                            if (tumor.cancerTypes_review.removed) {
                                ReviewResource.removed.push(tumor.cancerTypes_uuid);
                                tumorSectionChanged = true;
                            }
                        }
                        if (tumorSectionChanged) {
                            mutationChanged = true;
                            userNames.push(tumor.cancerTypes_review.updatedBy);
                            tempArr = collectUUIDs('tumor', tumor, [], 'sectionOnly');
                            sectionUUIDs_ = _.union(sectionUUIDs_, tempArr);
                            tempArr = collectUUIDs('tumor', tumor, [], 'insideOnly');
                            ReviewResource.inside = _.union(ReviewResource.inside, tempArr);
                            return {
                                sectionUUIDs: sectionUUIDs_,
                                hasReviewContent: hasReviewContent_,
                                mutationChanged: mutationChanged_
                            };
                        }
                        if (isChangedSection([tumor.prognostic.level_uuid, tumor.prognostic.description_uuid])) {
                            tempArr = [tumor.prognostic.description_review, tumor.prognostic.level_review];
                            sectionUUIDs_.push(tumor.prognostic_uuid);
                            ReviewResource.updated.push(tumor.prognostic_uuid);
                            tumorChanged = true;
                            setUpdatedSignature(tempArr, tumor.prognostic_uuid);
                        }
                        if (isChangedSection([tumor.diagnostic.level_uuid, tumor.diagnostic.description_uuid])) {
                            tempArr = [tumor.diagnostic.description_review, tumor.diagnostic.level_review];
                            sectionUUIDs_.push(tumor.diagnostic_uuid);
                            ReviewResource.updated.push(tumor.diagnostic_uuid);
                            tumorChanged = true;
                            setUpdatedSignature(tempArr, tumor.diagnostic_uuid);
                        }
                        if (isChangedSection([tumor.summary_uuid])) {
                            tumorChanged = true;
                            userNames.push(tumor.summary_review.updatedBy);
                            ReviewResource.updated.push(tumor.summary_uuid);
                        }
                        if (isChangedSection([tumor.diagnosticSummary_uuid])) {
                            tumorChanged = true;
                            userNames.push(tumor.diagnosticSummary_review.updatedBy);
                            ReviewResource.updated.push(tumor.diagnosticSummary_uuid);
                        }
                        if (isChangedSection([tumor.prognosticSummary_uuid])) {
                            tumorChanged = true;
                            userNames.push(tumor.prognosticSummary_review.updatedBy);
                            ReviewResource.updated.push(tumor.prognosticSummary_uuid);
                        }

                        _.each(tumor.TIs, function (ti) {
                            _.each(ti.treatments, function (treatment) {
                                treatmentSectionChanged = false;
                                if (treatment.name_review) {
                                    if (treatment.name_review.added) {
                                        tiChanged = true;
                                        userNames.push(treatment.name_review.updatedBy);
                                        ReviewResource.added.push(treatment.name_uuid);
                                        treatmentSectionChanged = true;
                                    }
                                    if (treatment.name_review.removed) {
                                        tiChanged = true;
                                        userNames.push(treatment.name_review.updatedBy);
                                        ReviewResource.removed.push(treatment.name_uuid);
                                        treatmentSectionChanged = true;
                                    }
                                }
                                if (treatmentSectionChanged) {
                                    tempArr = collectUUIDs('treatment', treatment, [], 'sectionOnly');
                                    sectionUUIDs_ = _.union(sectionUUIDs_, tempArr);
                                    tempArr = collectUUIDs('treatment', treatment, [], 'insideOnly');
                                    ReviewResource.inside = _.union(ReviewResource.inside, tempArr);
                                    return {
                                        sectionUUIDs: sectionUUIDs_,
                                        hasReviewContent: hasReviewContent_,
                                        mutationChanged: mutationChanged_
                                    };
                                }
                                if (isChangedSection([treatment.level_uuid, treatment.propagation_uuid, treatment.indication_uuid, treatment.description_uuid])) {
                                    tempArr = [treatment.name_review, treatment.level_review, treatment.propagation_review, treatment.indication_review, treatment.description_review];
                                    treatmentChanged = true;
                                    setUpdatedSignature([treatment.level_review, treatment.propagation_review, treatment.indication_review, treatment.description_review], treatment.name_uuid);
                                    ReviewResource.updated.push(treatment.name_uuid);
                                } else if (isChangedSection([treatment.name_uuid])) {
                                    treatmentChanged = true;
                                    userNames.push(treatment.name_review.updatedBy);
                                    ReviewResource.nameChanged.push(treatment.name_uuid);
                                }
                                if (treatmentChanged) {
                                    sectionUUIDs_.push(treatment.name_uuid);
                                    tiChanged = true;
                                }
                                treatmentChanged = false;
                            });
                            if (tiChanged) {
                                sectionUUIDs_.push(ti.name_uuid);
                                tumorChanged = true;
                            }
                            tiChanged = false;
                        });
                        if (isChangedSection([tumor.cancerTypes_uuid])) {
                            tumorChanged = true;
                            userNames.push(tumor.cancerTypes_review.updatedBy);
                            ReviewResource.nameChanged.push(tumor.cancerTypes_uuid);
                        }
                        if (tumorChanged) {
                            sectionUUIDs_.push(tumor.cancerTypes_uuid);
                            mutationChanged = true;
                        }
                        tumorChanged = false;
                    });
                    if (isChangedSection([mutation.name_uuid])) {
                        mutationChanged = true;
                        userNames.push(mutation.name_review.updatedBy);
                        ReviewResource.nameChanged.push(mutation.name_uuid);
                    }
                    if (mutationChanged) {
                        sectionUUIDs_.push(mutation.name_uuid);
                        hasReviewContent_ = true;
                        mutationChanged_ = true;
                    }
                    mutationChanged = false;
                });
                return {
                    sectionUUIDs: sectionUUIDs_,
                    hasReviewContent: hasReviewContent_,
                    mutationChanged: mutationChanged_
                };
            }

            function prepareReviewItems() {
                resetReviewResources();
                var reviewInfo = getReviewInfo();
                $scope.sectionUUIDs = reviewInfo.sectionUUIDs;
                if (reviewInfo.hasReviewContent === false) {
                    $scope.geneMeta.review.currentReviewer = '';
                    // This is to increase the fault tolerance of the platform. UUIDs are supposed to be cleaned up after acception or rejection.
                    // If after scaning whole gene document and found nothing need to be reviewed, then we clean up everything in the review EXCEPT currentReviewer
                    var reviewUUIDs = _.without(_.keys($scope.geneMeta.review), 'currentReviewer');
                    if (reviewUUIDs.length > 0) {
                        _.each(reviewUUIDs, function(key) {
                            mainUtils.deleteUUID(key);
                        });
                    }

                    dialogs.notify('Warning', 'No changes need to be reviewed');
                } else {
                    $scope.geneMeta.review.currentReviewer = $rootScope.me.name;
                    $rootScope.reviewMode = true;
                    if (reviewInfo.mutationChanged) {
                        $scope.setSectionOpenStatus('open', reviewInfo.sectionUUIDs);
                    }
                    var validUsers = [];
                    _.each(_.uniq(userNames), function (userName) {
                        if (userName) {
                            $scope.status[userName] = {};
                            validUsers.push(userName);
                        }
                    });
                    $scope.namesWithChanges = validUsers;
                }
            }
            function doneSaving(userName) {
                $scope.status[userName].savingAll = false;
                numOfReviewItems.set(userName, 0);
                evidencesAllUsers[userName] = {};
            };
            $scope.acceptChangesByPerson = function (userName) {
                if (!userName) {
                    dialogs.error('Error', 'Can not accept changes from invalid user name. Please contact the developer.');
                    return false;
                }
                $scope.status[userName].savingAll = true;
                collectChangesByPerson(userName);
                var apiCalls = [];
                if (!_.isEmpty(evidencesAllUsers[userName].geneTypeEvidence)) {
                    apiCalls.push(geneTypeUpdate(userName));
                }
                if (!_.isEmpty(evidencesAllUsers[userName].updatedEvidences)) {
                    apiCalls.push(evidenceBatchUpdate(userName));
                }
                if (!_.isEmpty(evidencesAllUsers[userName].deletedEvidences)) {
                    apiCalls.push(evidenceDeleteUpdate(userName));
                }
                if (apiCalls.length === 0) {
                    doneSaving(userName);
                } else {
                    $q.all(apiCalls)
                        .then(function (result) {
                            doneSaving(userName);
                        }, function (error) {
                            dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                            $scope.status[userName].savingAll = false;
                        });
                }
            };
            $scope.isNoReviewItems = function (userName) {
                var reviewItemsCount = numOfReviewItems.get();
                if (!_.isEmpty(reviewItemsCount) && !_.isUndefined(reviewItemsCount[userName])) {
                    return reviewItemsCount[userName] === 0 ? true : false;
                } else {
                    return false;
                }
            };
            function processAddedSection(userName, type, mutation, tumor, ti, treatment) {
                var tempEvidences = formSectionEvidencesByType(type, mutation, tumor, ti, treatment);
                var evidences = tempEvidences.evidences;
                var historyData = tempEvidences.historyData;
                if (!_.isEmpty(evidences)) {
                    evidencesAllUsers[userName].updatedEvidences = _.extend(evidencesAllUsers[userName].updatedEvidences, evidences);
                    evidencesAllUsers[userName].historyData.update.push(historyData);
                    evidencesAllUsers[userName].updatedEvidenceModels.push([type, mutation, tumor, ti, treatment]);
                } else {
                    var data = $scope.getRefs(mutation, tumor, ti, treatment);
                    // for empty section
                    acceptSection(type, data.mutation, data.tumor, data.ti, data.treatment);
                }
            }
            /*****
             * This function is designed to check if a section has been changed or not, and if it is changed by a certain person
             *  ****/
            function isChangedBy(type, uuid, userName, reviewObj) {
                if (uuid) {
                    if (type === 'section') {
                        return uuid && $scope.sectionUUIDs.indexOf(uuid) !== -1 && ReviewResource.mostRecent[uuid] && ReviewResource.mostRecent[uuid].updatedBy === userName;
                    } else if (type === 'precise') {
                        return $scope.geneMeta.review[uuid] && reviewObj && reviewObj.updatedBy === userName;
                    }
                } else {
                    return false;
                }
            }
            function collectChangesByPerson(userName) {
                // This function can only be called in the review mode, in which, mostRecent has already been set in the prepareReview function
                evidencesAllUsers[userName] = {
                    updatedEvidences: {},
                    historyData: {
                        hugoSymbol: $scope.gene.name,
                        geneType: [],
                        update: [],
                        deletion: []
                    },
                    deletedEvidences: [],
                    geneTypeEvidence: {},
                    updatedEvidenceModels: [],
                    deletedEvidenceModels: []
                };
                if (isChangedBy('precise', $scope.gene.summary_uuid, userName, $scope.gene.summary_review)) {
                    formEvidencesPerUser(userName, 'GENE_SUMMARY', null, null, null, null);
                }
                if (isChangedBy('precise', $scope.gene.background_uuid, userName, $scope.gene.background_review)) {
                    formEvidencesPerUser(userName, 'GENE_BACKGROUND', null, null, null, null);
                }
                if (isChangedBy('precise', $scope.gene.type.tsg_uuid, userName, $scope.gene.type.tsg_review)
                    || isChangedBy('precise', $scope.gene.type.ocg_uuid, userName, $scope.gene.type.ocg_review)) {
                    evidencesAllUsers[userName].geneTypeEvidence = {
                        hugoSymbol: $scope.gene.name,
                        oncogene: $scope.gene.type.ocg ? true : false,
                        tsg: $scope.gene.type.tsg ? true : false
                    };
                    var updatedBy = '';
                    if (_.isUndefined($scope.gene.type.tsg_review)) {
                        updatedBy = $scope.gene.type.ocg_review.updatedBy;
                    } else {
                        updatedBy = $scope.gene.type.tsg_review.updatedBy;
                    }
                    var newContent = $scope.gene.type.tsg + '  ' + $scope.gene.type.ocg;
                    var oldContent = mainUtils.getOldGeneType($scope.gene.type);
                    evidencesAllUsers[userName].historyData.geneType = [{
                        lastEditBy: updatedBy,
                        operation: 'update',
                        uuids: $scope.gene.type_uuid,
                        location: 'Gene Type',
                        new: newContent.trim(),
                        old: oldContent.trim()
                    }];
                }
                _.each($scope.mutations, function (mutation) {
                    // collect changes that happened in mutation level
                    if (mutation.name_review && mutation.name_review.updatedBy === userName) {
                        if (mainUtils.processedInReview('remove', mutation.name_uuid)) {
                            evidencesAllUsers[userName].deletedEvidences = collectUUIDs('mutation', mutation, evidencesAllUsers[userName].deletedEvidences, 'evidenceOnly');
                            evidencesAllUsers[userName].deletedEvidenceModels.push({type: 'mutation', mutation: mutation});
                            evidencesAllUsers[userName].historyData.deletion.push({ operation: 'delete', lastEditBy: mutation.name_review.updatedBy, location: mutation.name, old: mutation });
                            $scope.updateDrugMap('accept', 'delete', 'mutation', mutation);
                            return true;
                        } else if (mainUtils.processedInReview('add', mutation.name_uuid)) {
                            processAddedSection(userName, 'mutation', mutation);
                            return true;
                        } else if (mainUtils.processedInReview('name', mutation.name_uuid)) {
                            formEvidencesPerUser(userName, 'MUTATION_NAME_CHANGE', mutation, null, null, null);
                        }

                    }
                    // collect changes happened inside mutation, similar logics are applied to tumor and treatment
                    if (isChangedBy('section', mutation.mutation_effect_uuid, userName)) {
                        formEvidencesPerUser(userName, 'MUTATION_EFFECT', mutation, null, null, null);
                    }
                    _.each(mutation.tumors, function (tumor) {
                        if (tumor.cancerTypes_review && tumor.cancerTypes_review.updatedBy === userName) {
                            if (mainUtils.processedInReview('remove', tumor.cancerTypes_uuid)) {
                                evidencesAllUsers[userName].deletedEvidences = collectUUIDs('tumor', tumor, evidencesAllUsers[userName].deletedEvidences, 'evidenceOnly');
                                evidencesAllUsers[userName].deletedEvidenceModels.push({type: 'tumor', mutation: mutation, tumor: tumor});
                                evidencesAllUsers[userName].historyData.deletion.push({ operation: 'delete', lastEditBy: tumor.cancerTypes_review.updatedBy, location: historyStr(mutation, tumor), old: tumor });
                                $scope.updateDrugMap('accept', 'delete', 'tumor', mutation, tumor);
                                return true;
                            } else if (mainUtils.processedInReview('add', tumor.cancerTypes_uuid)) {
                                processAddedSection(userName, 'tumor', mutation, tumor);
                                return true;
                            }
                        }
                        if (tumor.cancerTypes_review && tumor.cancerTypes_review.updatedBy === userName && mainUtils.processedInReview('name', tumor.cancerTypes_uuid)) {
                            formEvidencesPerUser(userName, 'TUMOR_NAME_CHANGE', mutation, tumor, null, null);
                        }
                        if (isChangedBy('section', tumor.prognostic_uuid, userName)) {
                            formEvidencesPerUser(userName, 'PROGNOSTIC_IMPLICATION', mutation, tumor, null, null);
                        }
                        if (isChangedBy('section', tumor.diagnostic_uuid, userName)) {
                            formEvidencesPerUser(userName, 'DIAGNOSTIC_IMPLICATION', mutation, tumor, null, null);
                        }
                        _.each(tumor.TIs, function (ti) {
                            _.each(ti.treatments, function (treatment) {
                                if (treatment.name_review && treatment.name_review.updatedBy === userName) {
                                    var refContent = $scope.getRefs(mutation, tumor, ti ,treatment);
                                    var mapPath = 'Genes/' + $scope.gene.name + '/mutations/' + refContent.mutationIndex + '/tumors/' + refContent.tumorIndex + '/TIs/' + refContent.tiIndex + '/treatments/' + refContent.treatmentIndex;
                                    if (mainUtils.processedInReview('remove', treatment.name_uuid)) {
                                        evidencesAllUsers[userName].deletedEvidences = collectUUIDs('treatment', treatment, evidencesAllUsers[userName].deletedEvidences, 'evidenceOnly');
                                        evidencesAllUsers[userName].deletedEvidenceModels.push({type: 'treatment', mutation: mutation, tumor: tumor, ti: ti, treatment: treatment});
                                        evidencesAllUsers[userName].historyData.deletion.push({ operation: 'delete', lastEditBy: treatment.name_review.updatedBy, location: historyStr(mutation, tumor) + ', ' + ti.name + ', ' + treatment.name, old: treatment });
                                        $scope.updateDrugMap('accept', 'delete', 'treatment', mutation, tumor, treatment);
                                        return true;
                                    } else if (mainUtils.processedInReview('add', treatment.name_uuid)) {
                                        processAddedSection(userName, 'treatment', mutation, tumor, ti, treatment);
                                        $scope.updateDrugMap('accept', 'add', 'treatment', mutation, tumor, treatment);
                                        return true;
                                    } else if (mainUtils.processedInReview('name', treatment.name_uuid)) {
                                        formEvidencesPerUser(userName, 'TREATMENT_NAME_CHANGE', mutation, tumor, ti, treatment);
                                        $scope.updateDrugMap('accept', 'name', 'treatment', mutation, tumor, treatment, treatment.name_review.lastReviewed);
                                    }
                                }
                                if (isChangedBy('section', treatment.name_uuid, userName)) {
                                    formEvidencesPerUser(userName, ti.name, mutation, tumor, ti, treatment);
                                }
                            });
                        });
                        if (isChangedBy('precise', tumor.summary_uuid, userName, tumor.summary_review)) {
                            formEvidencesPerUser(userName, 'TUMOR_TYPE_SUMMARY', mutation, tumor, null, null);
                        }
                        if (isChangedBy('precise', tumor.diagnosticSummary_uuid, userName, tumor.diagnosticSummary_review)) {
                            formEvidencesPerUser(userName, 'DIAGNOSTIC_SUMMARY', mutation, tumor, null, null);
                        }
                        if (isChangedBy('precise', tumor.prognosticSummary_uuid, userName, tumor.prognosticSummary_review)) {
                            formEvidencesPerUser(userName, 'PROGNOSTIC_SUMMARY', mutation, tumor, null, null);
                        }
                    });
                });
            }
            function geneTypeUpdate(userName) {
                var deferred = $q.defer();
                var geneTypeEvidence = evidencesAllUsers[userName].geneTypeEvidence;
                var historyData = evidencesAllUsers[userName].historyData.geneType;
                historyData.hugoSymbol = evidencesAllUsers[userName].historyData.hugoSymbol;
                DatabaseConnector.updateGeneType($scope.gene.name, geneTypeEvidence, historyData, function (result) {
                    $scope.modelUpdate('GENE_TYPE', null, null, null, null);
                    deferred.resolve();
                }, function (error) {
                    deferred.reject(error);
                });
                return deferred.promise;
            }

            function evidenceBatchUpdate(userName) {
                var deferred = $q.defer();
                var updatedEvidenceModels = evidencesAllUsers[userName].updatedEvidenceModels;
                var updatedEvidences = evidencesAllUsers[userName].updatedEvidences;
                var historyData = evidencesAllUsers[userName].historyData.update;
                historyData.hugoSymbol = evidencesAllUsers[userName].historyData.hugoSymbol;
                DatabaseConnector.updateEvidenceBatch(updatedEvidences, historyData, function (result) {
                    for (var i = 0; i < updatedEvidenceModels.length; i++) {
                        $scope.modelUpdate(updatedEvidenceModels[i][0], updatedEvidenceModels[i][1], updatedEvidenceModels[i][2], updatedEvidenceModels[i][3], updatedEvidenceModels[i][4]);
                    }
                    deferred.resolve();
                }, function (error) {
                    deferred.reject(error);
                });
                return deferred.promise;
            }
            function evidenceDeleteUpdate(userName) {
                var deferred = $q.defer();
                var deletedEvidenceModels = evidencesAllUsers[userName].deletedEvidenceModels;
                var deletedEvidences = evidencesAllUsers[userName].deletedEvidences;
                var historyData = evidencesAllUsers[userName].historyData.deletion;
                historyData.hugoSymbol = evidencesAllUsers[userName].historyData.hugoSymbol;
                DatabaseConnector.deleteEvidences(deletedEvidences, historyData, function (result) {
                    _.each(deletedEvidenceModels, function (item) {
                        var data = $scope.getRefs(item.mutation, item.tumor, item.ti, item.treatment);
                        var indicies = [data.mutationIndex, data.tumorIndex, data.tiIndex, data.treatmentIndex];
                        removeModel({indicies: indicies, uuids: deletedEvidences, type: item.type});
                    });
                    deferred.resolve();
                }, function (error) {
                    deferred.reject(error);
                });
                return deferred.promise;
            }
            function historyStr(mutation, tumor) {
                if (mutation && tumor) {
                    return mutation.name + ', ' + $scope.getCancerTypesName(tumor);
                }
            }
            $scope.getEvidence = function (type, mutation, tumor, TI, treatment) {
                // The reason we are cheking again if a change has been made to a section is that, there might be many empty content in a newly added section.
                // We need to identify the evidences having input
                var historyData = { operation: 'update' };
                var historyUUIDs = [];
                var tempReviewObjArr;
                var tempRecentIndex;
                var evidences = {};
                var dataUUID = '';
                var extraDataUUID = '';
                var reviewObj;
                var data = {
                    additionalInfo: null,
                    alterations: null,
                    cancerType: null,
                    description: null,
                    evidenceType: type,
                    gene: {
                        hugoSymbol: $scope.gene.name
                    },
                    knownEffect: null,
                    lastEdit: null,
                    levelOfEvidence: null,
                    subtype: null,
                    articles: [],
                    treatments: null,
                    propagation: null
                };
                if ($scope.meta.gene) {
                    data.gene.entrezGeneId = $scope.meta.gene.entrezGeneId;
                }
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
                    'R3': 'LEVEL_R3',
                    'no': 'NO',
                    'Px1': 'LEVEL_Px1',
                    'Px2': 'LEVEL_Px2',
                    'Px3': 'LEVEL_Px3',
                    'Dx1': 'LEVEL_Dx1',
                    'Dx2': 'LEVEL_Dx2',
                    'Dx3': 'LEVEL_Dx3'
                };
                var extraData = _.clone(data);
                var i = 0;
                var uuids = [];
                switch (type) {
                    case 'GENE_SUMMARY':
                        data.description = $scope.gene.summary;
                        dataUUID = $scope.gene.summary_uuid;
                        data.lastEdit = $scope.gene.summary_review.updateTime;
                        historyData.location = 'Gene Summary';
                        historyData.new = $scope.gene.summary;
                        historyData.old = $scope.gene.summary_review.lastReviewed;
                        reviewObj = $scope.gene.summary_review;
                        break;
                    case 'GENE_BACKGROUND':
                        data.description = $scope.gene.background;
                        dataUUID = $scope.gene.background_uuid;
                        data.lastEdit = $scope.gene.background_review.updateTime;
                        historyData.location = 'Gene Background';
                        historyData.new = $scope.gene.background;
                        historyData.old = $scope.gene.background_review.lastReviewed;
                        reviewObj = $scope.gene.background_review;
                        break;
                    case 'MUTATION_EFFECT':
                        var MEObj = mutation.mutation_effect;
                        historyData.new = {};
                        historyData.old = {};
                        if ($scope.geneMeta.review[MEObj.oncogenic_uuid]) {
                            data.evidenceType = 'ONCOGENIC';
                            data.knownEffect = MEObj.oncogenic;
                            dataUUID = MEObj.oncogenic_uuid;
                            data.lastEdit = MEObj.oncogenic_review.updateTime;
                            historyData.location = mutation.name + ', Mutation Effect';
                            historyData.new.oncogenic = MEObj.oncogenic;
                            historyData.old.oncogenic = MEObj.oncogenic_review.lastReviewed;
                            reviewObj = MEObj.oncogenic_review;
                        }
                        // tempFlag is set to true when MUTATION_EFFECT evidence exists which means either mutation effect or mutation description got changed.
                        var tempFlag = false;
                        if ($scope.geneMeta.review[MEObj.effect_uuid]) {
                            tempFlag = true;
                            historyData.new.effect = MEObj.effect;
                            historyData.old.effect = MEObj.effect_review.lastReviewed;
                        }
                        if ($scope.geneMeta.review[MEObj.description_uuid]) {
                            tempFlag = true;
                            historyData.new.description = MEObj.description;
                            historyData.old.description = MEObj.description_review.lastReviewed;
                        }
                        if (tempFlag) {
                            tempReviewObjArr = [MEObj.effect_review, MEObj.description_review];
                            tempRecentIndex = mainUtils.mostRecentItem(tempReviewObjArr, true);
                            extraData.knownEffect = MEObj.effect;
                            extraDataUUID = MEObj.effect_uuid;
                            // We have to calculate the lastEdit time specifically here because ReviewResource.mostRecent[mutation.mutation_effect.oncogenic_uuid].updateTime is the most recent time among three items: oncogenic, mutation effect and description
                            // But here we only need the most recent time from mutation effect and description
                            extraData.lastEdit = tempReviewObjArr[tempRecentIndex].updateTime;
                            extraData.description = MEObj.description;
                            extraData.evidenceType = 'MUTATION_EFFECT';
                            historyData.location = mutation.name + ', Mutation Effect';
                            if (!reviewObj) {
                                reviewObj = tempReviewObjArr[tempRecentIndex];
                            }
                        }
                        break;
                    case 'TUMOR_TYPE_SUMMARY':
                        if ($scope.geneMeta.review[tumor.summary_uuid]) {
                            data.description = tumor.summary;
                            dataUUID = tumor.summary_uuid;
                            data.lastEdit = tumor.summary_review.updateTime;
                            historyData.location = historyStr(mutation, tumor) + ', Tumor Type Summary';
                            historyData.new = tumor.summary;
                            historyData.old = tumor.summary_review.lastReviewed;
                            reviewObj = tumor.summary_review;
                        }
                        break;
                    case 'DIAGNOSTIC_SUMMARY':
                        if ($scope.geneMeta.review[tumor.diagnosticSummary_uuid]) {
                            data.description = tumor.diagnosticSummary;
                            dataUUID = tumor.diagnosticSummary_uuid;
                            data.lastEdit = tumor.diagnosticSummary_review.updateTime;
                            historyData.location = historyStr(mutation, tumor) + ', Diagnostic Summary';
                            historyData.new = tumor.diagnosticSummary;
                            historyData.old = tumor.diagnosticSummary_review.lastReviewed;
                            reviewObj = tumor.diagnosticSummary_review;
                        }
                        break;
                    case 'PROGNOSTIC_SUMMARY':
                        if ($scope.geneMeta.review[tumor.prognosticSummary_uuid]) {
                            data.description = tumor.prognosticSummary;
                            dataUUID = tumor.prognosticSummary_uuid;
                            data.lastEdit = tumor.prognosticSummary_review.updateTime;
                            historyData.location = historyStr(mutation, tumor) + ', Prognostic Summary';
                            historyData.new = tumor.prognosticSummary;
                            historyData.old = tumor.prognosticSummary_review.lastReviewed;
                            reviewObj = tumor.prognosticSummary_review;
                        }
                        break;
                    case 'PROGNOSTIC_IMPLICATION':
                        var hasUpdate = false;
                        historyData.new = {};
                        historyData.old = {};
                        if ($scope.geneMeta.review[tumor.prognostic.description_uuid]) {
                            hasUpdate = true;
                            historyData.new.description = tumor.prognostic.description;
                            historyData.old.description = tumor.prognostic.description_review.lastReviewed;
                        }
                        if ($scope.geneMeta.review[tumor.prognostic.level_uuid]) {
                            hasUpdate = true;
                            historyData.new.level = tumor.prognostic.level;
                            historyData.old.level = tumor.prognostic.level_review.lastReviewed;
                        }
                        if (hasUpdate) {
                            data.description = tumor.prognostic.description;
                            data.levelOfEvidence = levelMapping[tumor.prognostic.level];
                            dataUUID = tumor.prognostic_uuid;
                            if (!ReviewResource.mostRecent[dataUUID]) {
                                setUpdatedSignature([tumor.prognostic.description_review, tumor.prognostic.level_review], tumor.prognostic_uuid);
                            }
                            data.lastEdit = ReviewResource.mostRecent[dataUUID].updateTime;
                            historyData.location = historyStr(mutation, tumor) + ', Prognostic';
                        }
                        break;
                    case 'DIAGNOSTIC_IMPLICATION':
                        var hasUpdate = false;
                        historyData.new = {};
                        historyData.old = {};
                        if ($scope.geneMeta.review[tumor.diagnostic.description_uuid]) {
                            hasUpdate = true;
                            historyData.new.description = tumor.diagnostic.description;
                            historyData.old.description = tumor.diagnostic.description_review.lastReviewed;
                        }
                        if ($scope.geneMeta.review[tumor.diagnostic.level_uuid]) {
                            hasUpdate = true;
                            historyData.new.level = tumor.diagnostic.level;
                            historyData.old.level = tumor.diagnostic.level_review.lastReviewed;
                        }
                        if (hasUpdate) {
                            data.description = tumor.diagnostic.description;
                            data.levelOfEvidence = levelMapping[tumor.diagnostic.level];
                            dataUUID = tumor.diagnostic_uuid;
                            if (!ReviewResource.mostRecent[dataUUID]) {
                                setUpdatedSignature([tumor.diagnostic.description_review, tumor.diagnostic.level_review], tumor.diagnostic_uuid);
                            }
                            data.lastEdit = ReviewResource.mostRecent[dataUUID].updateTime;
                            historyData.location = historyStr(mutation, tumor) + ', Diagnostic';
                        }
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
                    case 'MUTATION_NAME_CHANGE':
                        uuids = collectUUIDs('mutation', mutation, [], 'evidenceOnly');
                        data.evidenceType = null;
                        historyData.location = mutation.name;
                        historyData.new = mutation.name;
                        historyData.old = mutation.name_review.lastReviewed;
                        break;
                    case 'TUMOR_NAME_CHANGE':
                        uuids = collectUUIDs('tumor', tumor, [], 'evidenceOnly');
                        data.evidenceType = null;
                        historyData.location = historyStr(mutation, tumor);
                        historyData.new = $scope.getCancerTypesName(tumor);
                        historyData.old = mainUtils.getCancerTypesName(tumor.cancerTypes_review.lastReviewed);
                        break;
                    case 'TREATMENT_NAME_CHANGE':
                        uuids = collectUUIDs('treatment', treatment, [], 'evidenceOnly');
                        data.evidenceType = null;
                        historyData.location = historyStr(mutation, tumor) + ', ' + data.evidenceType + ', ' + treatment.name;
                        historyData.new = treatment.name;
                        historyData.old = treatment.name_review.lastReviewed;
                        break;
                    default:
                        break;
                }
                if (tumor && type !== 'TREATMENT_NAME_CHANGE') {
                    var tempArr1 = [];
                    var tempArr2 = [];
                    if ($scope.geneMeta.review[tumor.cancerTypes_uuid] && _.isArray(tumor.cancerTypes_review.lastReviewed) && tumor.cancerTypes_review.lastReviewed.length > 0 && type !== 'TUMOR_NAME_CHANGE' && !tumor.cancerTypes_review.added) {
                        _.each(tumor.cancerTypes_review.lastReviewed, function (item) {
                            tempArr1.push(item.mainType);
                            tempArr2.push(item.code ? item.code : 'null');
                        });
                    } else {
                        _.each(tumor.cancerTypes, function (item) {
                            tempArr1.push(item.mainType);
                            tempArr2.push(item.code ? item.code : 'null');
                        });
                    }
                    if (tempArr1.length > 0) {
                        data.cancerType = tempArr1.join(';');
                        data.subtype = tempArr2.join(';');
                    }
                }
                if (TI) {
                    dataUUID = treatment.name_uuid;
                    if (!ReviewResource.mostRecent[dataUUID]) {
                        setUpdatedSignature([treatment.name_review, treatment.level_review, treatment.indication_review, treatment.description_review], treatment.name_uuid);
                    }
                    historyData.new = {};
                    historyData.old = {};
                    if ($scope.geneMeta.review[treatment.description_uuid]) {
                        historyData.new.description = treatment.description;
                        historyData.old.description = treatment.description_review.lastReviewed;
                    }
                    if ($scope.geneMeta.review[treatment.indication_uuid]) {
                        historyData.new.indication = treatment.indication;
                        historyData.old.indication = treatment.indication_review.lastReviewed;
                    }
                    if ($scope.geneMeta.review[treatment.level_uuid] &&
                        !_.isUndefined(treatment.level) && !_.isUndefined(treatment.level_review.lastReviewed)) {
                        historyData.new.level = treatment.level;
                        historyData.old.level = treatment.level_review.lastReviewed;
                    }
                    if ($scope.geneMeta.review[treatment.propagation_uuid]) {
                        if (!_.isUndefined(treatment.propagation)) {
                            historyData.new.propagation = treatment.propagation;
                        }
                        if (!_.isUndefined(treatment.propagation_review.lastReviewed)) {
                            historyData.old.propagation = treatment.propagation_review.lastReviewed;
                        }
                    }
                    data.lastEdit = ReviewResource.mostRecent[dataUUID].updateTime;
                    data.levelOfEvidence = levelMapping[treatment.level];
                    data.description = treatment.description;
                    data.propagation = levelMapping[treatment.propagation];
                    data.treatments = [];
                    var treatments = treatment.name.split(',');
                    var priorities = getNewPriorities(TI.treatments, [dataUUID]);
                    for (i = 0; i < treatments.length; i++) {
                        var drugs = treatments[i].split('+');
                        var drugList = [];
                        for (var j = 0; j < drugs.length; j++) {
                            var drugName = drugs[j].trim();
                            var drugObj = $rootScope.drugList[drugName];
                            if (drugObj) {
                                drugObj.priority = j + 1;
                                drugList.push(drugObj);
                            } else {
                                throw new Error('Drug is not available.' + drugName);
                            }
                        }
                        data.treatments.push({
                            approvedIndications: [treatment.indication],
                            drugs: drugList,
                            priority: priorities[dataUUID][drugList.map(function (drug) {
                                return drug.drugName;
                            }).join(' + ')]
                        });
                    }
                    historyData.location = historyStr(mutation, tumor) + ', ' + data.evidenceType + ', ' + treatment.name;
                }
                if (mutation && ['TUMOR_NAME_CHANGE', 'TREATMENT_NAME_CHANGE'].indexOf(type) === -1) {
                    var mutationStr;
                    if ($scope.geneMeta.review[mutation.name_uuid] && mutation.name_review.lastReviewed && type !== 'MUTATION_NAME_CHANGE' && !mutation.name_review.added) {
                        mutationStr = mainUtils.getTextString(mutation.name_review.lastReviewed);
                    } else {
                        mutationStr = mutation.name;
                    }
                    var mutationStrResult = parseMutationString(mutationStr);
                    if (dataUUID || type === 'MUTATION_NAME_CHANGE') {
                        data.alterations = mutationStrResult;
                    }
                    if (extraDataUUID) {
                        extraData.alterations = mutationStrResult;
                    }
                }
                if (data.description) {
                    formArticles(data);
                }
                if (extraData.description) {
                    formArticles(extraData);
                }
                if (data.lastEdit) {
                    data.lastEdit = validateTimeFormat(data.lastEdit);
                }
                if (extraData.lastEdit) {
                    extraData.lastEdit = validateTimeFormat(extraData.lastEdit);
                }
                if (dataUUID) {
                    evidences[dataUUID] = data;
                    historyUUIDs.push(dataUUID);
                }
                if (extraDataUUID) {
                    evidences[extraDataUUID] = extraData;
                    historyUUIDs.push(extraDataUUID);
                }
                if (historyUUIDs.length > 0) {
                    historyData.uuids = historyUUIDs.join(',');
                    if (dataUUID && ReviewResource.mostRecent[dataUUID]) {
                        historyData.lastEditBy = ReviewResource.mostRecent[dataUUID].updatedBy;
                    } else if (reviewObj) {
                        historyData.lastEditBy = reviewObj.updatedBy;
                    }
                }
                if (['MUTATION_NAME_CHANGE', 'TUMOR_NAME_CHANGE', 'TREATMENT_NAME_CHANGE'].indexOf(type) !== -1) {
                    _.each(uuids, function (uuid) {
                        evidences[uuid] = data;
                    });
                    historyData.operation = 'name change';
                    switch (type) {
                        case 'MUTATION_NAME_CHANGE':
                            historyData.uuids = mutation.name_uuid;
                            historyData.lastEditBy = mutation.name_review.updatedBy;
                            break;
                        case 'TUMOR_NAME_CHANGE':
                            historyData.uuids = tumor.cancerTypes_uuid;
                            historyData.lastEditBy = tumor.cancerTypes_review.updatedBy;
                            break;
                        case 'TREATMENT_NAME_CHANGE':
                            historyData.uuids = treatment.name_uuid;
                            historyData.lastEditBy = treatment.name_review.updatedBy;
                            break;
                    }
                }
                if (!historyData.lastEditBy) {
                    historyData.lastEditBy = '';
                }
                return { evidences: evidences, historyData: historyData };
            };
            function formArticles(data) {
                var description = data.description;
                var abstractResults = FindRegex.result(description);
                var tempAbstract;
                for (var i = 0; i < abstractResults.length; i++) {
                    tempAbstract = abstractResults[i];
                    switch (tempAbstract.type) {
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
                    }
                }
            }
            function validateTimeFormat(updateTime) {
                var tempTime = new Date(updateTime);
                if (tempTime instanceof Date && !isNaN(tempTime.getTime())) {
                    updateTime = tempTime.getTime();
                } else {
                    // handle the case of time stamp in string format
                    tempTime = new Date(Number(updateTime));
                    if (tempTime instanceof Date && !isNaN(tempTime.getTime())) {
                        updateTime = tempTime.getTime();
                    } else {
                        updateTime = new Date().getTime();
                    }
                }
                return updateTime.toString();
            }
            function acceptItem(arr, uuid) {
                _.each(arr, function (item) {
                    if (item.reviewObj) {
                        delete item.reviewObj.lastReviewed;
                    }
                    mainUtils.deleteUUID(item.uuid);
                    ReviewResource.accepted.push(item.uuid);
                });
                if (uuid) {
                    ReviewResource.accepted.push(uuid);
                }
            }
            $scope.modelUpdate = function (type, mutationCopy, tumorCopy, tiCopy, treatmentCopy) {
                var data = $scope.getRefs(mutationCopy, tumorCopy, tiCopy, treatmentCopy);
                var mutation = data.mutation;
                var tumor = data.tumor;
                var ti = data.ti;
                var treatment = data.treatment;
                switch (type) {
                    case 'GENE_SUMMARY':
                        acceptItem([{ reviewObj: $scope.gene.summary_review, uuid: $scope.gene.summary_uuid }], $scope.gene.summary_uuid);
                        break;
                    case 'GENE_BACKGROUND':
                        acceptItem([{ reviewObj: $scope.gene.background_review, uuid: $scope.gene.background_uuid }], $scope.gene.background_uuid);
                        break;
                    case 'GENE_TYPE':
                        acceptItem([{ reviewObj: $scope.gene.type.tsg_review, uuid: $scope.gene.type.tsg_uuid }, { reviewObj: $scope.gene.type.ocg_review, uuid: $scope.gene.type.ocg_uuid }], $scope.gene.type_uuid);
                        break;
                    case 'MUTATION_EFFECT':
                        acceptItem([{ reviewObj: mutation.mutation_effect.oncogenic_review, uuid: mutation.mutation_effect.oncogenic_uuid },
                        { reviewObj: mutation.mutation_effect.effect_review, uuid: mutation.mutation_effect.effect_uuid },
                        { reviewObj: mutation.mutation_effect.description_review, uuid: mutation.mutation_effect.description_uuid }], mutation.mutation_effect_uuid);
                        break;
                    case 'TUMOR_TYPE_SUMMARY':
                        acceptItem([{ reviewObj: tumor.summary_review, uuid: tumor.summary_uuid }], tumor.summary_uuid);
                        break;
                    case 'DIAGNOSTIC_SUMMARY':
                        acceptItem([{ reviewObj: tumor.diagnosticSummary_review, uuid: tumor.diagnosticSummary_uuid }], tumor.diagnosticSummary_uuid);
                        break;
                    case 'PROGNOSTIC_SUMMARY':
                        acceptItem([{ reviewObj: tumor.prognosticSummary_review, uuid: tumor.prognosticSummary_uuid }], tumor.prognosticSummary_uuid);
                        break;
                    case 'PROGNOSTIC_IMPLICATION':
                        acceptItem([{ reviewObj: tumor.prognostic.description_review, uuid: tumor.prognostic.description_uuid },
                        { reviewObj: tumor.prognostic.level_review, uuid: tumor.prognostic.level_uuid }], tumor.prognostic_uuid);
                        break;
                    case 'DIAGNOSTIC_IMPLICATION':
                        acceptItem([{ reviewObj: tumor.diagnostic.description_review, uuid: tumor.diagnostic.description_uuid },
                        { reviewObj: tumor.diagnostic.level_review, uuid: tumor.diagnostic.level_uuid }], tumor.diagnostic_uuid);
                        break;
                    case 'Standard implications for sensitivity to therapy':
                    case 'Standard implications for resistance to therapy':
                    case 'Investigational implications for sensitivity to therapy':
                    case 'Investigational implications for resistance to therapy':
                        acceptItem([{ reviewObj: treatment.name_review, uuid: treatment.name_uuid },
                            { reviewObj: treatment.level_review, uuid: treatment.level_uuid },
                            { reviewObj: treatment.propagation_review, uuid: treatment.propagation_uuid },
                            { reviewObj: treatment.indication_review, uuid: treatment.indication_uuid },
                            { reviewObj: treatment.description_review, uuid: treatment.description_uuid }], treatment.name_uuid);
                        break;
                    case 'MUTATION_NAME_CHANGE':
                        acceptItem([{ reviewObj: mutation.name_review, uuid: mutation.name_uuid }], mutation.name_uuid);
                        break;
                    case 'TUMOR_NAME_CHANGE':
                        acceptItem([{ reviewObj: tumor.cancerTypes_review, uuid: tumor.cancerTypes_uuid }], tumor.cancerTypes_uuid);
                        break;
                    case 'TREATMENT_NAME_CHANGE':
                        $scope.updateDrugMap('accept', 'name', 'treatment', mutation, tumor, treatment, treatment.name_review.lastReviewed);
                        acceptItem([{ reviewObj: treatment.name_review, uuid: treatment.name_uuid }], treatment.name_uuid);
                        break;
                    case 'mutation':
                    case 'tumor':
                    case 'treatment':
                        acceptSection(type, mutation, tumor, ti, treatment);
                        break;
                    default:
                        break;
                }
                mainUtils.updateLastSavedToDB();
            };
            /*
            * This function is used to collect uuids for specified section.
            * */
            function getUUIDsByType(type, mutation, tumor, TI, treatment) {
                switch (type) {
                    case 'mutation':
                        return collectUUIDs(type, mutation, []);
                    case 'tumor':
                        return collectUUIDs(type, tumor, []);
                    case 'TI':
                        return collectUUIDs(type, TI, []);
                    case 'treatment':
                        return collectUUIDs(type, treatment, []);
                    case 'GENE_SUMMARY':
                        return [$scope.gene.summary_uuid];
                    case 'GENE_BACKGROUND':
                        return [$scope.gene.background_uuid];
                    case 'MUTATION_EFFECT':
                        return [mutation.mutation_effect.oncogenic_uuid, mutation.mutation_effect.effect_uuid, mutation.mutation_effect.description_uuid];
                    case 'PROGNOSTIC_IMPLICATION':
                        return [tumor.prognostic_uuid];
                    case 'DIAGNOSTIC_IMPLICATION':
                        return [tumor.diagnostic_uuid];
                }
            };
            /*
            * This function is used to form evidence models, which would be used for api call
            * */
            function formSectionEvidencesByType(type, mutation, tumor, TI, treatment) {
                var evidences = {};
                var historyData = { operation: 'add' };
                switch (type) {
                    case 'mutation':
                        historyData.location = mutation.name;
                        historyData.lastEditBy = mutation.name_review.updatedBy;
                        historyData.new = mutation;
                        formSectionEvidences(type, mutation, tumor, TI, treatment, evidences, historyData);
                        break;
                    case 'tumor':
                        historyData.location = historyStr(mutation, tumor);
                        historyData.lastEditBy = tumor.cancerTypes_review.updatedBy;
                        historyData.new = tumor;
                        formSectionEvidences(type, mutation, tumor, TI, treatment, evidences, historyData);
                        break;
                    case 'TI':
                        formSectionEvidences(type, mutation, tumor, TI, treatment, evidences, historyData);
                        break;
                    case 'treatment':
                        historyData.location = historyStr(mutation, tumor) + ', ' + TI.name + ', ' + treatment.name;
                        historyData.lastEditBy = treatment.name_review.updatedBy;
                        historyData.new = treatment;
                        formSectionEvidences(type, mutation, tumor, TI, treatment, evidences, historyData);
                        break;
                    case 'GENE_SUMMARY':
                    case 'GENE_BACKGROUND':
                        formEvidencesByType([type], null, null, null, null, evidences, historyData);
                        break;
                    case 'MUTATION_EFFECT':
                    case 'PROGNOSTIC_IMPLICATION':
                    case 'DIAGNOSTIC_IMPLICATION':
                        formEvidencesByType([type], mutation, tumor, TI, treatment, evidences, historyData);
                        break;
                }
                return { evidences: evidences, historyData: historyData };
            }
            function formSectionEvidences(type, mutation, tumor, ti, treatment, evidences, historyData) {
                var typeArr = [];
                var dataArr = [];
                var tempType = '';
                if (type === 'mutation') {
                    typeArr = ['MUTATION_EFFECT'];
                    dataArr = mutation.tumors;
                    tempType = 'tumor';
                }
                if (type === 'tumor') {
                    typeArr = ['TUMOR_TYPE_SUMMARY', 'DIAGNOSTIC_SUMMARY', 'PROGNOSTIC_SUMMARY', 'PROGNOSTIC_IMPLICATION', 'DIAGNOSTIC_IMPLICATION'];
                    dataArr = tumor.TIs;
                    tempType = 'TI';
                }
                if (type === 'TI') {
                    dataArr = ti.treatments;
                    tempType = 'treatment';
                }
                if (type === 'treatment') {
                    typeArr = [ti.name];
                }
                formEvidencesByType(typeArr, mutation, tumor, ti, treatment, evidences, historyData);
                _.each(dataArr, function (item) {
                    if (type === 'mutation') tumor = item;
                    if (type === 'tumor') ti = item;
                    if (type === 'TI') treatment = item;
                    formSectionEvidences(tempType, mutation, tumor, ti, treatment, evidences, historyData);
                });
            };
            function formEvidencesByType(types, mutation, tumor, TI, treatment, evidences, historyData) {
                _.each(types, function (type) {
                    var getEvidenceResult = $scope.getEvidence(type, mutation, tumor, TI, treatment);
                    var tempEvidences = getEvidenceResult.evidences;
                    var historyDataItem = getEvidenceResult.historyData;
                    if (!_.isEmpty(tempEvidences)) {
                        evidences = _.extend(evidences, tempEvidences);
                        if (!historyData.uuids) {
                            historyData.uuids = historyDataItem.uuids;
                        } else {
                            historyData.uuids += ',' + historyDataItem.uuids;
                        }
                    }

                });
            };
            function acceptSection(type, mutation, tumor, ti, treatment) {
                var tempUUIDs = getUUIDsByType(type, mutation, tumor, ti, treatment);
                ReviewResource.accepted = _.union(ReviewResource.accepted, tempUUIDs);
                removeUUIDs(tempUUIDs);
                acceptSectionItems(type, mutation, tumor, ti, treatment, true);
            }
            function clearReview(arr) {
                _.each(arr, function (item) {
                    if (item) {
                        delete item.lastReviewed;
                    }
                });
            }
            function acceptSectionItems(type, mutationCopy, tumorCopy, tiCopy, treatmentCopy, firstLayer) {
                var data = $scope.getRefs(mutationCopy, tumorCopy, tiCopy, treatmentCopy);
                var mutation = data.mutation;
                var tumor = data.tumor;
                var ti = data.ti;
                var treatment = data.treatment;
                switch (type) {
                    case 'mutation':
                        ReviewResource.accepted.push(mutation.name_uuid);
                        delete mutation.name_review.added;
                        clearReview([mutation.name_review, mutation.mutation_effect.oncogenic_review, mutation.mutation_effect.effect_review, mutation.mutation_effect.description_review]);
                        _.each(mutation.tumors, function (tumor) {
                            acceptSectionItems('tumor', mutation, tumor, ti, treatment);
                        });
                        break;
                    case 'tumor':
                        ReviewResource.accepted.push(tumor.cancerTypes_uuid);
                        delete tumor.cancerTypes_review.added;
                        clearReview([tumor.cancerTypes_review, tumor.summary_review, tumor.prognostic.level_review, tumor.prognostic.description_review, tumor.diagnostic.level_review, tumor.diagnostic.description_review]);
                        _.each(tumor.TIs, function (ti) {
                            _.each(ti.treatments, function (treatment) {
                                acceptSectionItems('treatment', mutation, tumor, ti, treatment);
                            });
                        });
                        break;
                    case 'treatment':
                        $scope.updateDrugMap('accept', 'add', type, mutation, tumor, treatment);
                        ReviewResource.accepted.push(treatment.name_uuid);
                        delete treatment.name_review.added;
                        clearReview([treatment.name_review, treatment.level_review, treatment.propagation_review, treatment.indication_review, treatment.description_review]);
                        if (firstLayer) {
                            $scope.updatePriority(ti.treatments);
                        }
                        break;
                }
            }

            function changeTherapyNameInMap(dataType, geneName, mutationUuid, mutationName, cancerTypeUuid, therapyUuid, drugArray, content){
                switch (dataType){
                    case 'mutation':
                        break;
                    case 'tumor':
                        break;
                    case 'treatment':
                        var mapPath = '';
                        var chainDrug = $q.when();
                        _.forEach(drugArray, function (drug) {
                            chainDrug = chainDrug.then(function(){
                                mapPath = firebasePathUtils.getTherapyNameMapPath(drug, geneName, mutationUuid, cancerTypeUuid, therapyUuid);
                                firebaseConnector.setMap(mapPath, content);
                            });
                        });
                        break;
                    default:
                        break;
                }
            }

            function removeMapWithoutValidatingStatus (geneName, mutationUuid, cancerTypeUuid, therapyUuid, therapyUuids){
                var mapPath = '';
                _.each(therapyUuids, function (drug) {
                    mapPath = firebasePathUtils.getTherapyMapPath(drug, geneName, mutationUuid, cancerTypeUuid, therapyUuid);
                    firebaseConnector.removeMap(mapPath).then(function(){
                        firebaseConnector.ref(firebasePathUtils.getCancerTypesMapPath(drug, geneName, mutationUuid)).once('value', function (doc) {
                            if (!doc.val()) {
                                mapPath = firebasePathUtils.getMutationNameMapPath(drug, geneName, mutationUuid);
                                firebaseConnector.removeMap(mapPath);
                            }
                        })
                    })
                });
            }

            function changeTherapyStatusInMap(dataType, geneName, mutationUuid, cancerTypeUuid, therapyUuid, therapyUuids) {
                switch (dataType) {
                    case 'mutation':
                        break;
                    case 'tumor':
                        break;
                    case 'treatment':
                        _.each(therapyUuids, function (drug) {
                            var mapPath = firebasePathUtils.getTherapyMapPath(drug, geneName, mutationUuid, cancerTypeUuid, therapyUuid) + '/status';
                            firebaseConnector.setMap(mapPath, 'reviewed');
                        });
                        break;
                    default:
                        break;
                }
            }

            function updateDrugMapWhenAcceptDeletionOrRejectAddition(dataType, mutation, tumor, treatment, mapInfo){
                switch (dataType) {
                    case 'mutation':
                        _.each(mutation.tumors, function (tumor) {
                            var mapInfo = {
                                'geneName': $scope.gene.name,
                                'mutationUuid': mutation.name_uuid,
                                'cancerTypeUuid': null
                            };
                            updateDrugMapWhenAcceptDeletionOrRejectAddition('tumor', mutation, tumor, null, mapInfo);
                        });
                        break;
                    case 'tumor':
                        if(!mapInfo){
                            var mapInfo = {
                                'geneName': $scope.gene.name,
                                'mutationUuid': mutation.name_uuid,
                                'cancerTypeUuid': tumor.cancerTypes_uuid
                            }
                        }
                        else{
                            var mapInfo = mapInfo;
                            mapInfo.cancerTypeUuid = tumor.cancerTypes_uuid;
                        }
                        _.each(tumor.TIs, function(ti){
                            _.each(ti.treatments, function(treatment){
                                updateDrugMapWhenAcceptDeletionOrRejectAddition('treatment', mutation, tumor, treatment, mapInfo);
                            })
                        });
                        break;
                    case 'treatment':
                        if(!mapInfo){
                            var mapInfo = {
                                'geneName': $scope.gene.name,
                                'mutationUuid': mutation.name_uuid,
                                'cancerTypeUuid': tumor.cancerTypes_uuid
                            }
                        }
                        var therapyUuids = _.flatten(drugMapUtils.therapyStrToArr(treatment.name));
                        if(treatment.name_review.lastReviewed){
                            var reviewedTherapyUuids = _.flatten(drugMapUtils.therapyStrToArr(treatment.name_review.lastReviewed));
                            removeMapWithoutValidatingStatus(mapInfo.geneName, mapInfo.mutationUuid, mapInfo.cancerTypeUuid, treatment.name_uuid, reviewedTherapyUuids);
                        }
                        removeMapWithoutValidatingStatus(mapInfo.geneName, mapInfo.mutationUuid, mapInfo.cancerTypeUuid, treatment.name_uuid, therapyUuids);
                        break;
                    default:
                        break;
                }
            }

            function updateDrugMapWhenAcceptName(dataType, mutation, tumor, treatment, difference) {
                switch (dataType) {
                    case 'mutation':
                        break;
                    case 'tumor':
                        break;
                    case 'treatment':
                        removeMapWithoutValidatingStatus($scope.gene.name, mutation.name_uuid, tumor.cancerTypes_uuid, treatment.name_uuid, difference.extraDrugsInOld);
                        changeTherapyStatusInMap(dataType, $scope.gene.name, mutation.name_uuid, tumor.cancerTypes_uuid, treatment.name_uuid, difference.extraDrugsInNew);
                        changeTherapyNameInMap(dataType, $scope.gene.name, mutation.name_uuid, mutation.name, tumor.cancerTypes_uuid, treatment.name_uuid, difference.sameDrugs, treatment.name);
                        break;
                    default:
                        break;
                }
            }

            function updateDrugMapWhenRejectName(dataType, mutation, tumor, treatment, difference){
                switch (dataType) {
                    case 'mutation':
                        break;
                    case 'tumor':
                        break;
                    case 'treatment':
                        removeMapWithoutValidatingStatus($scope.gene.name, mutation.name_uuid, tumor.cancerTypes_uuid, treatment.name_uuid, difference.extraDrugsInNew);
                        break;
                    default:
                        break;
                }
            }

            $scope.updateDrugMap = function (reviewerDecision, actionType, dataType, mutation, tumor, treatment, oldContent) {
                switch (dataType) {
                    case 'treatment':
                        var therapyUuids = _.flatten(drugMapUtils.therapyStrToArr(treatment.name));
                        break;
                }
                switch (reviewerDecision) {
                    case 'accept':
                        switch (actionType) {
                            case 'add':
                                changeTherapyStatusInMap(dataType, $scope.gene.name, mutation.name_uuid, tumor.cancerTypes_uuid, treatment.name_uuid, therapyUuids);
                                break;
                            case 'delete':
                                updateDrugMapWhenAcceptDeletionOrRejectAddition(dataType, mutation, tumor, treatment);
                                break;
                            case 'name':
                                var difference = drugMapUtils.checkDifferenceBetweenTherapies(oldContent, treatment.name);
                                updateDrugMapWhenAcceptName(dataType, mutation, tumor, treatment, difference);
                                break;
                            case 'update':
                                // Currently, we don't record 'update' operation for treatments.
                                break;
                            default:
                                break;
                        }
                        break;
                    case 'reject':
                        switch (actionType) {
                            case 'add':
                                updateDrugMapWhenAcceptDeletionOrRejectAddition(dataType, mutation, tumor, treatment);
                                break;
                            case 'delete':
                                //Map won't be changed when reviewer reject deletion.
                                break;
                            case 'name':
                                var difference = drugMapUtils.checkDifferenceBetweenTherapies(oldContent, treatment.name);
                                updateDrugMapWhenRejectName(dataType, mutation, tumor, treatment, difference);
                                break;
                            case 'update':
                                // Currently, we don't record 'update' operation for treatments.
                                break;
                            default:
                                break;

                        }
                }
            };

            $scope.getRefs = function(mutationCopy, tumorCopy, tiCopy, treatmentCopy) {
                if (!mutationCopy) {
                    // this is the gene level update such as gene summary, background and gene type
                    return true;
                }
                var mutation = '';
                var tumor = '';
                var ti = '';
                var treatment = '';
                var mutationIndex = -1;
                var tumorIndex = -1;
                var tiIndex = -1;
                var treatmentIndex = -1;
                _.some($scope.gene.mutations, function (mutationRef, mutationInd) {
                    if (mutationRef.name_uuid === mutationCopy.name_uuid) {
                        mutation = mutationRef;
                        mutationIndex = mutationInd;
                        if (tumorCopy) {
                            _.some(mutationRef.tumors, function (tumorRef, tumorInd) {
                                if (tumorCopy.cancerTypes_uuid === tumorRef.cancerTypes_uuid) {
                                    tumor = tumorRef;
                                    tumorIndex = tumorInd;
                                    if (tiCopy) {
                                        _.some(tumorRef.TIs, function (tiRef, tiInd) {
                                            if (tiCopy.name_uuid === tiRef.name_uuid) {
                                                ti = tiRef;
                                                tiIndex = tiInd;
                                                if (treatmentCopy) {
                                                    _.some(tiRef.treatments, function (treatmentRef, treatmentInd) {
                                                        if (treatmentCopy.name_uuid === treatmentRef.name_uuid) {
                                                            treatment = treatmentRef;
                                                            treatmentIndex = treatmentInd;
                                                            return true;
                                                        }
                                                    });
                                                }
                                                return true;
                                            }
                                        });
                                    }
                                    return true;
                                }
                            });
                        }
                        return true;
                    }
                });
                return {
                    mutation: mutation,
                    tumor: tumor,
                    ti: ti,
                    treatment: treatment,
                    mutationIndex: mutationIndex,
                    tumorIndex: tumorIndex,
                    tiIndex: tiIndex,
                    treatmentIndex: treatmentIndex
                }
            }

            $scope.acceptAdded = function (type, mutation, tumor, ti, treatment, updatedBy) {
                var tempEvidences = formSectionEvidencesByType(type, mutation, tumor, ti, treatment);
                var evidences = tempEvidences.evidences;
                var loadingUUID;
                switch (type) {
                    case 'mutation':
                        loadingUUID = mutation.name_uuid;
                        tempEvidences.historyData.new = mutation;
                        break;
                    case 'tumor':
                        loadingUUID = tumor.cancerTypes_uuid;
                        tempEvidences.historyData.new = tumor;
                        break;
                    case 'treatment':
                        loadingUUID = treatment.name_uuid;
                        tempEvidences.historyData.new = treatment;
                        break;
                }
                var historyData = [tempEvidences.historyData];
                historyData.hugoSymbol = $scope.gene.name;
                if (_.isEmpty(evidences)) {
                    acceptSection(type, mutation, tumor, ti, treatment);
                    numOfReviewItems.minus(updatedBy);
                    // Add history record for newly added empty mutation.
                    DatabaseConnector.updateHistory(historyData);
                    return;
                }
                if (loadingUUID) {
                    ReviewResource.loading.push(loadingUUID);
                }
                DatabaseConnector.updateEvidenceBatch(evidences, historyData, function (result) {
                    acceptSection(type, mutation, tumor, ti, treatment);
                    ReviewResource.loading = _.without(ReviewResource.loading, loadingUUID);
                    numOfReviewItems.minus(updatedBy);
                }, function (error) {
                    ReviewResource.loading = _.without(ReviewResource.loading, loadingUUID);
                    dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                });
            };
            $scope.rejectAdded = function (type, mutation, tumor, ti, treatment, updatedBy) {
                var dlg = dialogs.confirm('Reminder', 'Are you sure you want to reject this change?');
                dlg.result.then(function () {
                    var data = $scope.getRefs(mutation, tumor, ti, treatment);
                    var indicies = [data.mutationIndex, data.tumorIndex, data.tiIndex, data.treatmentIndex];
                    var tempUUIDs = getUUIDsByType(type, mutation, tumor, ti, treatment);
                    numOfReviewItems.minus(updatedBy);
                    $scope.updateDrugMap('reject', 'add', type, mutation, tumor, treatment);
                    removeModel({type: type, uuids: tempUUIDs, indicies: indicies});
                });
            };
            function collectingGeneInfo() {
                var gene = mainUtils.getGeneData($scope.gene, true, true, $rootScope.drugList);
                var vus = mainUtils.getVUSData($scope.vusItems, true);
                var params = {};

                if (gene) {
                    params.gene = JSON.stringify(gene);
                }
                if (vus) {
                    params.vus = JSON.stringify(vus);
                }
                return params;
            }

            $scope.updateGene = function () {
                $scope.status.savedGene = false;
                var params = collectingGeneInfo();
                DatabaseConnector.updateGene(params, function (result) {
                    $scope.status.savedGene = true;
                    mainUtils.updateLastSavedToDB();
                }, function (error) {
                    $scope.status.savedGene = true;
                    var errorMessage = 'An error has occurred when saving data.' + error;
                    dialogs.error('Error', errorMessage);
                });
            };
            $scope.removeGeneFromDB = function() {
                $scope.status.removingGene = true;
                DatabaseConnector.removeGeneFromDB($scope.gene.name, function(data) {
                    $scope.status.removingGene = false;
                    $scope.status.geneReleased = 'no';
                }, function() {
                    $scope.status.removingGene = false;
                });
            };
            $scope.releaseGene = function() {
                function confirmCallback() {
                    var defer = $q.defer();
                    $scope.status.releasingGene = true;
                    var params = collectingGeneInfo();
                    params.releaseGene = true;
                    DatabaseConnector.updateGene(params, function(result) {
                        mainUtils.updateLastSavedToDB();
                        $scope.status.geneReleased = 'yes';
                        $scope.status.releasingGene = false;
                        defer.resolve();
                    }, function(error) {
                        var errorMessage = 'An error has occurred when saving data.' + error;
                        dialogs.error('Error', errorMessage);
                        $scope.status.releasingGene = false;
                        defer.reject(error);
                    });
                    return defer.promise;
                }

                var statusChecks = [];

                // Check gene summary
                var reviewInfo = getReviewInfo();
                var check = reviewInfo.hasReviewContent;
                if (check) {
                    statusChecks.push({
                        message: 'Please review all content before releasing',
                        status: 'error'
                    });
                } else {
                    statusChecks.push({
                        message: 'No content needs to be reviewed',
                        status: 'success'
                    });
                }

                check = _.isEmpty($scope.gene.summary);
                if (check) {
                    statusChecks.push({
                        message: 'Gene summary is empty',
                        status: 'error'
                    });
                } else {
                    statusChecks.push({
                        message: 'Gene summary is not empty',
                        status: 'success'
                    });
                }

                // Check gene background
                check = _.isEmpty($scope.gene.background);
                if (check) {
                    statusChecks.push({
                        message: 'Gene background is empty',
                        status: 'error'
                    });
                } else {
                    statusChecks.push({
                        message: 'Gene background is not empty',
                        status: 'success'
                    });
                }

                // Check gene summary
                check = _.isEmpty($scope.gene.type.tsg) && _.isEmpty($scope.gene.type.ocg);
                if (check) {
                    statusChecks.push({
                        message: 'Gene type is not specified',
                        status: 'warning'
                    });
                } else {
                    statusChecks.push({
                        message: 'Gene type is specified',
                        status: 'success'
                    });
                }

                dialogs.create('views/releaseGeneDialog.html', 'ReleaseGeneDialogCtrl', {
                    statusChecks: statusChecks,
                    confirmCallback: confirmCallback
                }, {
                    size: 'sm'
                });

            };

            $scope.validateTumor = function (mutation, tumor) {
                var exists = false;
                var removed = false;
                var tempTumor;
                var newTumorTypesName = mainUtils.getCancerTypesName($scope.meta.newCancerTypes).toLowerCase();
                _.some(mutation.tumors, function (e) {
                    if ($scope.getCancerTypesName(e).toLowerCase() === newTumorTypesName) {
                        exists = true;
                        if (e.cancerTypes_review.removed) {
                            removed = true;
                            tempTumor = e;
                        } else {
                            removed = false;
                            return true;
                        }
                    }
                });
                if (exists) {
                    if (removed) {
                        delete tempTumor.cancerTypes_review.removed;
                        mainUtils.deleteUUID(tempTumor.cancerTypes_uuid);
                        dialogs.notify('Warning', 'This tumor just got removed, we will reuse the old one.');
                    } else {
                        dialogs.notify('Warning', 'Tumor type exists.');
                    }
                    return false;
                } else {
                    return true;
                }
            };
            $scope.getTumorDuplication = function (mutation, tumor) {
                var mutationName = mutation.name.toLowerCase();
                var tumorName = $scope.getCancerTypesName(tumor).toLowerCase();
                if ($scope.tumorMessages[mutationName] && $scope.tumorMessages[mutationName][tumorName]) {
                    return $scope.tumorMessages[mutationName][tumorName];
                } else return '';
            };
            /**
             * check the to be added cancer types are empty or not.
             * It is used to disable Add Tumor Types button if applicable
             * **/
            $scope.emptyTT = function () {
                var result = true;
                for (var i = 0; i < $scope.meta.newCancerTypes.length; i++) {
                    var ct = $scope.meta.newCancerTypes[i];
                    if (ct.mainType && ct.mainType.name || ct.subtype && ct.subtype.name) {
                        result = false;
                        break;
                    }
                }
                return result;
            };
            function isValidTumor(mutationIndex, newTumorTypesName) {
                var isValid = true;
                var message = '';
                _.some($scope.mutations[mutationIndex].tumors, function (tumor) {
                    if ($scope.getCancerTypesName(tumor) === newTumorTypesName) {
                        isValid = false;
                        message = newTumorTypesName + ' has already been added';
                        return true;
                    }
                });
                if (!isValid) {
                    dialogs.notify('Warning', message);
                }
                return isValid;
            }
            $scope.addTumorType = function (index) {
                var newTumorTypesName = mainUtils.getNewCancerTypesName($scope.meta.newCancerTypes);
                if (isValidTumor(index, newTumorTypesName)) {
                    var cancerTypes = [];
                    _.each($scope.meta.newCancerTypes, function (ct) {
                        if (ct.mainType.name) {
                            var tempCode = ct.subtype.code ? ct.subtype.code : '';
                            var tempSubtype = ct.subtype.name ? ct.subtype.name : '';
                            var cancerType = new FirebaseModel.Cancertype(ct.mainType.name, tempSubtype, tempCode);
                            cancerTypes.push(cancerType);
                        }
                    });
                    var tumor = new FirebaseModel.Tumor(cancerTypes);
                    tumor.cancerTypes_review = {
                        updatedBy: $rootScope.me.name,
                        updateTime: new Date().getTime(),
                        added: true
                    };
                    if (!$scope.gene.mutations[index].tumors) {
                        $scope.gene.mutations[index].tumors = [];
                    }
                    $scope.gene.mutations[index].tumors.push(tumor);
                    $scope.meta.newCancerTypes = [{
                        subtype: '',
                        oncoTreeTumorTypes: angular.copy($scope.oncoTree.allTumorTypes)
                    }];
                    $scope.indicateMutationContent($scope.gene.mutations[index]);
                    mainUtils.setUUIDInReview(tumor.cancerTypes_uuid);
                }
            };

            $scope.modifyTumorType = function (mutation, tumor, path) {
                var indices = getIndexByPath(path);
                var tumorRef = $scope.gene.mutations[indices[0]].tumors[indices[1]];
                var dlg = dialogs.create('views/modifyTumorTypes.html', 'ModifyTumorTypeCtrl', {
                    mutation: mutation,
                    tumor: tumor,
                    tumorRef: tumorRef,
                    oncoTree: $scope.oncoTree
                    }, {
                        size: 'lg'
                });
            };
            $scope.modifyTherapy = function (path) {
                var indices = getIndexByPath(path);
                var mutationRef = $scope.gene.mutations[indices[0]];
                var tumorRef = $scope.gene.mutations[indices[0]].tumors[indices[1]];
                var tiRef = $scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]];
                var treatmentRef = $scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]].treatments[indices[3]];
                var geneName = $scope.gene.name;
                var dlgfortherapy = dialogs.create('views/modifyTherapy.html', 'ModifyTherapyCtrl', {
                    modifyName: true,
                    geneName: geneName,
                    mutationRef: mutationRef,
                    tumorRef: tumorRef,
                    tiRef: tiRef,
                    treatmentRef: treatmentRef
                }, {
                    size: 'lg'
                });

            };

            $scope.initTherapyComponent = function(path){
                var indices = getIndexByPath(path) ;
                $scope.mutationRef = $scope.gene.mutations[indices[0]];
                $scope.tumorRef = $scope.gene.mutations[indices[0]].tumors[indices[1]];
                $scope.tiRef = $scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]];
                $scope.path = path;
                $scope.modifyName = false;
            }

            $scope.saveTherapiesCallback = function(newTreatmentName){
                var indices = $scope.indices;
                var geneName = $scope.gene.name;
                var mutationUuid = $scope.mutationRef.name_uuid;
                var mutationName = $scope.mutationRef.name;
                var cancerTypeUuid = $scope.tumorRef.cancerTypes_uuid;
                var treatment = new FirebaseModel.Treatment(newTreatmentName);
                treatment.name_review = {
                    updatedBy: $rootScope.me.name,
                    updateTime: new Date().getTime(),
                    added: true
                };
                if (!$scope.tiRef.treatments) {
                    firebaseConnector.addTreatment($scope.path, treatment);
                }
                else {
                    $scope.tiRef.treatments.push(treatment);
                }
                drugMapUtils.changeMapByCurator('add', 'treatment', geneName, mutationUuid, mutationName, cancerTypeUuid, treatment.name_uuid, treatment.name);
                $scope.indicateTumorContent($scope.tumorRef);
                mainUtils.setUUIDInReview(treatment.name_uuid);
            }

            $scope.onFocus = function (e) {
                $timeout(function () {
                    $(e.target).trigger('input');
                    $(e.target).trigger('change'); // for IE
                });
            };
            /**
             * This function is used to check if the review mode header and comparison should be displayed or not.
             * */
            $scope.reviewContentDisplay = function (uuid, name) {
                var result = $rootScope.reviewMode && !mainUtils.processedInReview('accept', uuid) && !mainUtils.processedInReview('reject', uuid) && !mainUtils.processedInReview('inside', uuid) && !mainUtils.processedInReview('add', uuid) && !mainUtils.processedInReview('remove', uuid);
                if (name) {
                    result = result && mainUtils.processedInReview('name', uuid);
                }
                return result;
            };

            $scope.addVUSItem = function (newVUSName) {
                if (newVUSName) {
                    if (isValidVariant(newVUSName)) {
                        var vusItem = new FirebaseModel.VUSItem(newVUSName, $rootScope.me.name, $rootScope.me.email);
                        $scope.vusItems.$add(vusItem).then(function(variant) {
                            $scope.vusUpdate();
                        });
                    }
                }
            };
            $scope.removeVUS = function(variant) {
                var dlg = dialogs.confirm('Confirmation', 'Are you sure you want to delete this entry?');
                dlg.result.then(function() {
                    $scope.vusItems.$remove(variant).then(function() {
                        $scope.vusUpdate();
                    });
                }, function() {
                });
            };
            $scope.refreshVUS = function(variant) {
                var obj = $firebaseObject(firebase.database().ref('VUS/' + $routeParams.geneName + '/' + variant.$id + '/time'));
                obj.value = new Date().getTime();
                obj.by = {
                    email: $rootScope.me.email,
                    name: $rootScope.me.name
                };
                obj.$save().then(function(ref) {
                    $scope.vusUpdate();
                    console.log('data refreshed');
                }, function(error) {
                    console.log("Error:", error);
                });
            };
            $scope.getVUSClass = function(time) {
                var dt = new Date(time);
                var _month = new Date().getMonth();
                var _year = new Date().getYear();
                var _monthDiff = (_year - dt.getYear()) * 12 + _month - dt.getMonth();
                if (_monthDiff > 3) {
                    return 'danger';
                } else if (_monthDiff > 1) {
                    return 'warning';
                } else {
                    return '';
                }
            };

            $scope.getCancerTypesNameInReview = function(tumor, uuid, reviewMode){
                if(mainUtils.processedInReview('remove', uuid) && reviewMode && tumor.cancerTypes_review.lastReviewed){
                    return $scope.getLastReviewedCancerTypesName(tumor);
                }
                else{
                    return $scope.getCancerTypesName(tumor);
                }
            };

            $scope.getCancerTypesName = function (tumor) {
                return mainUtils.getCancerTypesName(tumor.cancerTypes);
            };

            $scope.getLastReviewedCancerTypesName = function (tumor) {
                return mainUtils.getCancerTypesName(tumor.cancerTypes_review.lastReviewed);
            };

            $scope.remove = function (type, path) {
                $scope.status.processing = true;
                var deletionMessage = 'Are you sure you want to delete this entry?';
                var directlyRemove = false;
                var obj = '';
                var uuid = '';
                var indices = getIndexByPath(path);
                if (type === 'mutation') {
                    obj = $scope.gene.mutations[indices[0]];
                    uuid = $scope.gene.mutations[indices[0]].name_uuid;
                } else if (type === 'tumor') {
                    obj = $scope.gene.mutations[indices[0]].tumors[indices[1]];
                    uuid = $scope.gene.mutations[indices[0]].tumors[indices[1]].cancerTypes_uuid;
                } else if (type === 'treatment') {
                    obj = $scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]].treatments[indices[3]];
                    uuid = $scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]].treatments[indices[3]].name_uuid;
                }
                if (type === 'tumor') {
                    if (obj.cancerTypes_review && obj.cancerTypes_review.added) {
                        directlyRemove = true;
                    }
                } else if (obj.name_review && obj.name_review.added) {
                    directlyRemove = true;
                }
                if (directlyRemove) {
                    deletionMessage += ' <p style="color:red">This section will be removed directly since it is newly added and not yet reviewed.</p>';
                }
                var dlg = dialogs.confirm('Confirmation', deletionMessage);
                dlg.result.then(function () {
                    if (directlyRemove) {
                        var uuids = collectUUIDs(type, obj, []);
                        removeModel({ type: type, path: path, uuids: uuids });
                    } else {
                        if (type === 'tumor') {
                            // Do not delete lastReviewed when curators delete a treatment.
                            // If a reviewer reject this change, we can rollback to previous reviewed name.
                            if (_.isUndefined(obj.cancerTypes_review)) {
                                obj.cancerTypes_review = {
                                    updatedBy: $rootScope.me.name,
                                    updateTime: new Date().getTime(),
                                    removed: true
                                };
                            } else {
                                obj.cancerTypes_review.updatedBy = $rootScope.me.name;
                                obj.cancerTypes_review.updateTime = new Date().getTime();
                                obj.cancerTypes_review.removed = true;
                            }
                        } else {
                            if (_.isUndefined(obj.name_review)) {
                                obj.name_review = {
                                    updatedBy: $rootScope.me.name,
                                    updateTime: new Date().getTime(),
                                    removed: true
                                };
                            } else {
                                obj.name_review.updatedBy = $rootScope.me.name;
                                obj.name_review.updateTime = new Date().getTime();
                                obj.name_review.removed = true;
                            }
                        }
                        $scope.geneMeta.review[uuid] = true;
                    }
                    var geneName = $scope.gene.name;
                    var mutation = $scope.gene.mutations[indices[0]];
                    if (type === 'tumor') {
                        if(obj.cancerTypes_review.added){
                            drugMapUtils.changeMapWhenDeleteSection('tumor', geneName, mutation, obj);
                        }
                        $scope.indicateMutationContent($scope.gene.mutations[indices[0]]);
                    } else if (type === 'treatment') {
                        var tumor = $scope.gene.mutations[indices[0]].tumors[indices[1]];
                        drugMapUtils.changeMapWhenDeleteSection('treatment', geneName, mutation, tumor, obj);
                        $scope.indicateTumorContent($scope.gene.mutations[indices[0]].tumors[indices[1]]);
                    } else if (type === 'mutation') {
                        if(obj.name_review.added){
                            drugMapUtils.changeMapWhenDeleteSection('mutation', geneName, obj);
                        }
                    }
                }, function () {
                });
            };
            /**
             * This function is desgined to collect uuid list from a section
             * @param type: one of the three: mutation, tumor or treatment
             * @param obj: corresponding object
             * @param uuids: the array that you want to append uuids to. usually, pass in a empty array
             * @param inside: boolean value, set it to true to exclude its own uuid from getting collected. Otherwise this function will collect all of the uuids
             * @param evidenceUUIDsOnly: boolean value, set it to true to indicate only want to collect evidences uuid inside one section. If not specified, will return all UUIDs besides evidences UUIDs
             * */
            function collectUUIDs(type, obj, uuids, uuidType) {
                if (type === 'mutation') {
                    switch(uuidType) {
                        case 'insideOnly':
                            uuids.push(obj.mutation_effect.oncogenic_uuid);
                            uuids.push(obj.mutation_effect.effect_uuid);
                            uuids.push(obj.mutation_effect.description_uuid);
                            break;
                        case 'evidenceOnly':
                            uuids.push(obj.mutation_effect.oncogenic_uuid);
                            uuids.push(obj.mutation_effect.effect_uuid);
                            break;
                        case 'sectionOnly':
                            uuids.push(obj.name_uuid);
                            uuids.push(obj.mutation_effect_uuid);
                            break;
                        default:
                            uuids.push(obj.name_uuid);
                            uuids.push(obj.mutation_effect_uuid);
                            uuids.push(obj.mutation_effect.oncogenic_uuid);
                            uuids.push(obj.mutation_effect.effect_uuid);
                            uuids.push(obj.mutation_effect.description_uuid);
                            break;
                    }
                    _.each(obj.tumors, function (tumor) {
                        collectUUIDs('tumor', tumor, uuids, uuidType);
                    });
                }
                if (type === 'tumor') {
                    switch(uuidType) {
                        case 'insideOnly':
                            uuids.push(obj.summary_uuid);
                            uuids.push(obj.prognostic.level_uuid);
                            uuids.push(obj.prognostic.description_uuid);
                            uuids.push(obj.diagnostic.level_uuid);
                            uuids.push(obj.diagnostic.description_uuid);
                            break;
                        case 'evidenceOnly':
                            uuids.push(obj.summary_uuid);
                            uuids.push(obj.prognostic_uuid);
                            uuids.push(obj.diagnostic_uuid);
                            break;
                        case 'sectionOnly':
                            uuids.push(obj.cancerTypes_uuid);
                            uuids.push(obj.prognostic_uuid);
                            uuids.push(obj.diagnostic_uuid);
                            break;
                        default:
                            uuids.push(obj.cancerTypes_uuid);
                            uuids.push(obj.summary_uuid);
                            uuids.push(obj.prognostic.level_uuid);
                            uuids.push(obj.prognostic.description_uuid);
                            uuids.push(obj.diagnostic.level_uuid);
                            uuids.push(obj.diagnostic.description_uuid);
                            break;
                    }
                    _.each(obj.TIs, function (ti) {
                        collectUUIDs('TI', ti, uuids, uuidType);
                    });
                }
                if (type === 'TI') {
                    switch(uuidType) {
                        case 'insideOnly':
                        case 'evidenceOnly':
                            break;
                        case 'sectionOnly':
                            uuids.push(obj.name_uuid);
                            break;
                        default:
                            uuids.push(obj.name_uuid);
                            break;
                    }
                    _.each(obj.treatments, function (treatment) {
                        collectUUIDs('treatment', treatment, uuids, uuidType);
                    });
                }
                if (type === 'treatment') {
                    switch(uuidType) {
                        case 'insideOnly':
                            uuids.push(obj.level_uuid);
                            uuids.push(obj.propagation_uuid);
                            uuids.push(obj.indication_uuid);
                            uuids.push(obj.description_uuid);
                            break;
                        case 'evidenceOnly':
                            uuids.push(obj.name_uuid);
                            break;
                        case 'sectionOnly':
                            uuids.push(obj.name_uuid);
                            break;
                        default:
                            uuids.push(obj.name_uuid);
                            uuids.push(obj.level_uuid);
                            uuids.push(obj.propagation_uuid);
                            uuids.push(obj.indication_uuid);
                            uuids.push(obj.description_uuid);
                            break;
                    }
                }
                return uuids;
            }
            $scope.confirmDelete = function (type, mutation, tumor, ti, treatment, updatedBy) {
                var location = '';
                var obj;
                switch (type) {
                    case 'mutation':
                        obj = mutation;
                        location = mutation.name;
                        break;
                    case 'tumor':
                        obj = tumor;
                        location = historyStr(mutation, tumor);
                        break;
                    case 'treatment':
                        obj = treatment;
                        location = historyStr(mutation, tumor) + ', ' + ti.name + ', ' + treatment.name;
                        break;
                }
                var data = $scope.getRefs(mutation, tumor, ti, treatment);
                var indicies = [data.mutationIndex, data.tumorIndex, data.tiIndex, data.treatmentIndex];
                var allUUIDs = collectUUIDs(type, obj, []);
                var evidenceUUIDs = collectUUIDs(type, obj, [], 'evidenceOnly');
                var historyData = [{ operation: 'delete', lastEditBy: (type === 'tumor' ? obj.cancerTypes_review : obj.name_review).updatedBy, location: location, old: obj }];
                historyData.hugoSymbol = $scope.gene.name;
                // make the api call to delete evidences
                var loadingUUID = (type === 'tumor' ? obj.cancerTypes_uuid : obj.name_uuid);
                if (loadingUUID) {
                    ReviewResource.loading.push(loadingUUID);
                }
                DatabaseConnector.deleteEvidences(evidenceUUIDs, historyData, function (result) {
                    removeModel({ type: type, indicies: indicies, uuids: allUUIDs });
                    $scope.updateDrugMap('accept', 'delete', type, mutation, tumor, treatment);
                    ReviewResource.loading = _.without(ReviewResource.loading, loadingUUID);
                    numOfReviewItems.minus(updatedBy);
                    if (type === 'mutation') {
                        checkNameChange.set(true);
                    }
                }, function (error) {
                    dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                    ReviewResource.loading = _.without(ReviewResource.loading, loadingUUID);
                });
            };
            function removeModel(data) {
                var indices = [];
                if (data.indicies) {
                    indices = data.indicies;
                } else if (data.path) {
                    indices = getIndexByPath(data.path);
                }
                if (data.type === 'mutation') {
                    $scope.gene.mutations.splice(indices[0], 1);
                } else if (data.type === 'tumor') {
                    $scope.gene.mutations[indices[0]].tumors.splice(indices[1], 1);
                    $scope.indicateMutationContent($scope.gene.mutations[indices[0]]);
                } else if (data.type === 'treatment') {
                    $scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]].treatments.splice(indices[3], 1);
                    $scope.indicateTumorContent($scope.gene.mutations[indices[0]].tumors[indices[1]]);
                    // Update all priority if one of treatments is deleted.
                    $scope.updatePriority($scope.gene.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]].treatments);
                }
                _.each(data.uuids, function (uuid) {
                    if ($scope.geneMeta.review[uuid]) {
                        mainUtils.deleteUUID(uuid);
                    }
                });
            }
            function removeUUIDs(uuids) {
                if (uuids && _.isArray(uuids)) {
                    _.each(uuids, function (uuid) {
                        if (uuid) {
                            mainUtils.deleteUUID(uuid);
                        }
                    });
                }
            }
            function getIndexByPath(path) {
                var indices = [];
                _.each(path.split('/'), function (item) {
                    var tempNum = parseInt(item);
                    if (_.isNumber(tempNum) && !_.isNaN(tempNum)) {
                        indices.push(tempNum);
                    }
                });
                for (var i = indices.length; i < 4; i++) {
                    indices.push(-1);
                }
                return indices;
            }
            $scope.cancelDelete = function (type, mutation, tumor, ti, treatment, updatedBy) {
                var dlg = dialogs.confirm('Reminder', 'Are you sure you want to reject this change?');
                dlg.result.then(function () {
                    var tempUUIDs = getUUIDsByType(type, mutation, tumor, ti, treatment);
                    ReviewResource.rejected = _.union(ReviewResource.rejected, tempUUIDs);
                    numOfReviewItems.minus(updatedBy);
                    cancelDeleteSection(type, mutation, tumor, ti, treatment);
                });
            };
            function cancelDeleteSection(type, mutationCopy, tumorCopy, tiCopy, treatmentCopy) {
                var data = $scope.getRefs(mutationCopy, tumorCopy, tiCopy, treatmentCopy);
                var mutation = data.mutation;
                var tumor = data.tumor;
                var ti = data.ti;
                var treatment = data.treatment;
                switch (type) {
                    case 'mutation':
                        cancelDeleteItem(mutation.name_review, mutation.name_uuid);
                        if(mutation.name_review.lastReviewed){
                            mutation.name = mutation.name_review.lastReviewed;
                            delete mutation.name_review.lastReviewed;
                        }
                        _.each(mutation.tumors, function (tumor) {
                            if (tumor.cancerTypes_review && tumor.cancerTypes_review.removed) {
                                cancelDeleteSection('tumor', mutation, tumor, ti, treatment);
                            }
                        });
                        break;
                    case 'tumor':
                        cancelDeleteItem(tumor.cancerTypes_review, tumor.cancerTypes_uuid);
                        if(tumor.cancerTypes_review.lastReviewed){
                            tumor.cancerTypes = tumor.cancerTypes_review.lastReviewed;
                            delete tumor.cancerTypes_review.lastReviewed;
                        }
                        _.each(tumor.TIs, function (ti) {
                            _.each(ti.treatments, function (treatment) {
                                if (treatment.name_review.removed) {
                                    cancelDeleteSection('treatment', mutation, tumor, ti, treatment);
                                }
                            });
                        });
                        $scope.indicateMutationContent(mutation);
                        break;
                    case 'treatment':
                        cancelDeleteItem(treatment.name_review, treatment.name_uuid);
                        if(treatment.name_review.lastReviewed){
                            treatment.name = treatment.name_review.lastReviewed;
                            delete treatment.name_review.lastReviewed;
                        }
                        $scope.indicateMutationContent(mutation);
                        $scope.indicateTumorContent(tumor);
                        break;
                }
            }
            function cancelDeleteItem(reviewObj, uuid) {
                delete reviewObj.removed;
                mainUtils.deleteUUID(uuid);
                ReviewResource.removed = _.without(ReviewResource.removed, uuid);
            }
            function fetchResults(data) {
                var PMIDs = [];
                var abstracts = [];
                _.each(data, function (item) {
                    if (item.type === 'pmid') {
                        PMIDs.push(item.id);
                    } else if (item.type === 'abstract') {
                        abstracts.push(item.id);
                    }
                });
                PMIDs.sort();
                abstracts.sort();
                return { PMIDs: PMIDs, abstracts: abstracts };
            }

            $scope.getAllCitations = function () {
                var results = [];
                var geneData = JSON.stringify($scope.gene);
                results = fetchResults(FindRegex.result(geneData));
                var annotationPMIDs = results.PMIDs;
                var annotationAbstracts = results.abstracts;

                var vusData = JSON.stringify($scope.vusItems);
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
                dialogs.notify('All Citations', messageContent.join(''), { size: 'lg' });
            };
            $scope.specifyAnnotation = function () {
                var annotationLocation = {};
                setAnnotationResult(annotationLocation, fetchResults(FindRegex.result(this.gene.background)), 'Gene Background');
                _.each($scope.gene.mutations, function (mutation) {
                    setAnnotationResult(annotationLocation, fetchResults(FindRegex.result(JSON.stringify(mutation))), mutation.name);
                });
                return annotationLocation;
            };
            function setAnnotationResult(annotationLocation, results, location) {
                _.each([results.PMIDs, results.abstracts], function (annotations) {
                    _.each(annotations, function (annotation) {
                        annotation = annotation.trim();
                        if (_.has(annotationLocation, annotation)) {
                            annotationLocation[annotation].push(location);
                        } else {
                            annotationLocation[annotation] = [location];
                        }
                    });
                });
            }
            $scope.movingInfo = {
                style: {
                    color: 'gray'
                },
                message: 'Click to move',
                uuid: '',
                type: '',
                startIndex: -1,
                preMoving: true
            };
            $scope.displayMoveIcon = function(sectionType, type, uuid, index) {
                if (type === 'initial') {
                    if (uuid === $scope.movingInfo.uuid) {
                        return true;
                    } else {
                        return $scope.movingInfo.preMoving;
                    }
                } else {
                    if ($scope.movingInfo.preMoving === true) {
                        return false;
                    } else {
                        if (uuid === $scope.movingInfo.uuid) {
                            return false;
                        }
                        if (sectionType !== $scope.movingInfo.type) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                }
            };
            $scope.startMoving = function(sectionType, path, uuid, index) {
                mainUtils.updateMovingFlag(true);
                $scope.movingInfo.preMoving = !$scope.movingInfo.preMoving;
                if ($scope.movingInfo.uuid) {
                    delete $scope.movingInfo[$scope.movingInfo.uuid];
                }
                $scope.movingInfo.uuid = uuid;
                $scope.movingInfo.type = sectionType;
                $scope.movingInfo.startIndex = index;
                if ($scope.movingInfo.preMoving) {
                    $scope.movingInfo.style.color = 'gray';
                    $scope.movingInfo.message = 'Click to move';
                } else {
                    $scope.movingInfo.style.color = 'orange';
                    $scope.movingInfo.message = 'Click again to cancel';
                }
                if (!$scope.movingInfo.preMoving) {
                    $scope.movingInfo[$scope.movingInfo.uuid] = {
                        'border-left-style':'solid',
                        'border-left-color':'red'
                    };
                }
            };
            $rootScope.moving = false;
            $scope.endMoving = function(path, moveType) {
                if ($scope.movingInfo.startIndex === -1) {
                    return false;
                }
                $rootScope.moving = true;
                var dataList;
                var endIndex;
                var type = '';
                var indicies = getIndexByPath(path);
                if (indicies[1] === -1) {
                    // mutation section
                    dataList = this.gene.mutations;
                    endIndex = indicies[0];
                } else if (indicies[2] === -1) {
                    // tumor section
                    dataList = this.gene.mutations[indicies[0]].tumors;
                    endIndex = indicies[1];
                } else if (indicies[3] !== -1) {
                    // treatment section
                    dataList = this.gene.mutations[indicies[0]].tumors[indicies[1]].TIs[indicies[2]].treatments;
                    endIndex = indicies[3];
                    type = 'treatment';
                }
                var movingSectionCopy = _.clone(dataList[$scope.movingInfo.startIndex]);
                if ($scope.movingInfo.startIndex > endIndex) {
                    for (var i = $scope.movingInfo.startIndex-1; i >= endIndex+1; i--) {
                        dataList[i+1] = _.clone(dataList[i]);
                    }
                    if (moveType === 'up') {
                        dataList[endIndex+1] = _.clone(dataList[endIndex]);
                        dataList[endIndex] = movingSectionCopy;
                    } else if (moveType === 'down') {
                        dataList[endIndex+1] = movingSectionCopy;
                    }
                } else if ($scope.movingInfo.startIndex < endIndex) {
                    for (var i = $scope.movingInfo.startIndex+1; i <= endIndex-1; i++) {
                        dataList[i-1] = _.clone(dataList[i]);
                    }
                    if (moveType === 'up') {
                        dataList[endIndex-1] = movingSectionCopy;
                    } else if (moveType === 'down') {
                        dataList[endIndex-1] = _.clone(dataList[endIndex]);
                        dataList[endIndex] = movingSectionCopy;
                    }
                }
                if (type === 'treatment') {
                    $scope.updatePriority(dataList);
                }
                $scope.movingInfo.preMoving = true;
                $scope.movingInfo.style.color = 'gray';
                $scope.movingInfo.message = 'Click to move';
                $scope.movingInfo[$scope.movingInfo.uuid] = {
                    'border-left-style':'solid',
                    'border-left-color':'red'
                };
                var tempUUID = '';
                _.each(dataList, function(item) {
                    if (type === 'tumor') {
                        tempUUID = item.cancerTypes_uuid;
                    } else {
                        tempUUID = item.name_uuid;
                    }
                    $scope.initialOpen[tempUUID] = false;
                });
            };
            $scope.generatePDF = function () {
                jspdf.create(mainUtils.getGeneData(this.gene, true, false, $rootScope.drugList));
            };
            // emptySectionsUUIDs is still TBD in terms of where it should be used
            var emptySectionsUUIDs = {};
            $scope.isEmptySection = function (obj, type) {
                if ($scope.reviewMode) {
                    return false;
                }
                if (type === 'mutation') {
                    if (obj.mutation_effect.oncogenic || obj.mutation_effect.effect || obj.mutation_effect.description || obj.mutation_effect.short || !_.isUndefined(obj.tumors)) {
                        return false;
                    }
                    emptySectionsUUIDs[obj.name_uuid] = true;
                } else if (type === 'mutation_effect') {
                    if (obj.oncogenic || obj.effect || obj.description || obj.short) {
                        return false;
                    }
                    emptySectionsUUIDs[obj.mutation_effect_uuid] = true;
                } else if (type === 'diagnostic' || type === 'prognostic') {
                    if (obj[type].level || obj[type].description || obj[type].short) {
                        return false;
                    }
                    emptySectionsUUIDs[obj[type + '_uuid']] = true;
                } else if (type === 'ti') {
                    if (obj.description || obj.treatments) {
                        return false;
                    }
                    emptySectionsUUIDs[obj.name_uuid] = true;
                } else if (type === 'treatment') {
                    if (obj.level || obj.indication || obj.description || obj.short) {
                        return false;
                    }
                    emptySectionsUUIDs[obj.name_uuid] = true;
                }
                return true;
            };
            /**
             * Get priorities based on uuid and treatment name.
             *
             * @param Array list Google drive collaborative list
             * @param Object unapprovedUuids List of uuids that even unapproved, when calculate the priority should be incldued. This will be used when user approves the section.
             * @return Object
             */
            function getNewPriorities(list, unapprovedUuids) {
                var priorities = {};
                var count = 1;

                if (!_.isArray(unapprovedUuids)) {
                    unapprovedUuids = [];
                }
                _.each(list, function (treatmentSec, index) {
                    var name = treatmentSec.name_review && treatmentSec.name_review.lastReviewed ? treatmentSec.name_review.lastReviewed : treatmentSec.name;
                    var uuid = treatmentSec.name_uuid;
                    var notNewlyAdded = true;
                    if (treatmentSec.name_review && treatmentSec.name_review.added) {
                        notNewlyAdded = false;
                    }
                    if (notNewlyAdded || unapprovedUuids.indexOf(uuid) !== -1) {
                        priorities[uuid] = {};
                        _.each(name.split(','), function (t) {
                            var treatment = t.trim();
                            priorities[uuid][treatment] = count;
                            count++;
                        });
                    }
                });
                return priorities;
            }

            /**
             * Update treatment priority
             * @param list list Google drive collaborative list
             * @param integer index Original index
             * @param integer moveIndex Index is about move before that index
             * @return Promise
             */
            $scope.updatePriority = function (list) {
                var deferred = $q.defer();
                var postData = getNewPriorities(list);
                if (Object.keys(postData).length > 0) {
                    DatabaseConnector
                        .updateEvidenceTreatmentPriorityBatch(
                            postData, function () {
                                // Nothing needs to be done here
                                console.log('Succeed to update priority.');
                                deferred.resolve();
                            }, function (error) {
                                // Something goes wrong, this needs to be stored into meta file for future update.
                                console.log('Failed to update priority.');
                                DatabaseConnector.sendEmail({
                                    sendTo: 'dev.oncokb@gmail.com',
                                    subject: 'Error when updating treatments\' priority',
                                    content: JSON.stringify(postData)
                                },
                                    function (result) {
                                        deferred.rejected(error);
                                    },
                                    function (error) {
                                        deferred.rejected(error);
                                    }
                                );
                            });
                } else {
                    deferred.resolve();
                }
                return deferred.promise;
            }
            function getSuggestedMutations() {
                var defaultPlaceHolder = 'No suggestion found. Please curate according to literature.';
                DatabaseConnector.getSuggestedVariants()
                    .then(function (resp) {
                        if (_.isArray(resp.data) && resp.data.length > 0) {
                            $scope.suggestedMutations = resp.data;
                        } else {
                            $scope.suggestedMutations = [];
                        }
                    }, function () {
                        $scope.suggestedMutations = [];
                    })
                    .finally(function () {
                        if ($scope.suggestedMutations.length === 0) {
                            $scope.addMutationPlaceholder = defaultPlaceHolder;
                        }
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

                _.each(levelsCategories, function (item, key) {
                    levels[key] = [];
                    for (var i = 0; i < item.length; i++) {
                        var __datum = {};
                        __datum.label = item[i] + (item[i] === '' ? '' : ' - ') + ((['SS', 'IS'].indexOf(key) === -1) ? desR[item[i]] : desS[item[i]]);
                        __datum.value = item[i];
                        levels[key].push(__datum);
                    }
                });
                levels.prognostic = [{
                    value: 'Px1',
                    label: 'Px1 - FDA and/or professional guideline-recognized biomarker prognostic in this indication based on well-powered studies'
                }, {
                    value: 'Px2',
                    label: 'Px2 - FDA and/or professional guideline-recognized biomarker prognostic in this indication based on a single or multiple small studies'
                }, {
                    value: 'Px3',
                    label: 'Px3 - Clinical evidence based on well-powered studies that supports the biomarker as being prognostic in this indication'
                }, {
                    value: 'Px4',
                    label: 'Px4 - Clinical evidence based on single or multiple small studies that supports the biomarker as being prognostic in this indication'
                }];
                levels.diagnostic = [{
                    value: 'Dx1',
                    label: 'Dx1 - FDA and/or professional guideline-recognized biomarker indicative of diagnosis in this indication'
                }, {
                    value: 'Dx2',
                    label: 'Dx2 - FDA and/or professional guideline-recognized biomarker that strongly supports diagnosis in this indication'
                }, {
                    value: 'Dx3',
                    label: 'Dx3 - Biomarker that may assist disease diagnosis in this indication based on clinical evidence'
                }];
                return levels;
            }
            $scope.gene = '';
            $rootScope.collaborators = {};
            $scope.checkboxes = {
                oncogenic: ['Yes', 'Likely', 'Likely Neutral', 'Inconclusive'],
                mutationEffect: ['Gain-of-function', 'Likely Gain-of-function', 'Loss-of-function', 'Likely Loss-of-function', 'Switch-of-function', 'Likely Switch-of-function', 'Neutral', 'Likely Neutral', 'Inconclusive'],
                hotspot: ['TRUE', 'FALSE'],
                TSG: ['Tumor Suppressor'],
                OCG: ['Oncogene']
            };
            $scope.levels = getLevels();
            $rootScope.fileEditable = false;
            $scope.addMutationPlaceholder = 'Mutation Name';
            $scope.userRole = $rootScope.me.role;
            $rootScope.userRole = $rootScope.me.role;
            $scope.levelExps = {
                SR: $sce.trustAsHtml('<div><strong>Level R1:</strong> ' + $rootScope.meta.levelsDescHtml.R1 + '.<br/>Example 1: Colorectal cancer with KRAS mutation → resistance to cetuximab<br/>Example 2: EGFR-L858R or exon 19 mutant lung cancers with coincident T790M mutation → resistance to erlotinib</div>'),
                IR: $sce.trustAsHtml('<div><strong>Level R2:</strong> ' + $rootScope.meta.levelsDescHtml.R2 + '.<br/>Example: Resistance to crizotinib in a patient with metastatic lung adenocarcinoma harboring a CD74-ROS1 rearrangement (PMID: 23724914).<br/><strong>Level R3:</strong> ' + $rootScope.meta.levelsDescHtml.R3 + '.<br/>Example: Preclinical evidence suggests that BRAF V600E mutant thyroid tumors are insensitive to RAF inhibitors (PMID: 23365119).<br/></div>')
            };
            $scope.showHideButtons = [
                { key: 'proImShow', display: 'Prognostic implications' },
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
                }
            ];
            $scope.list = [];
            $scope.selfParams = {};
            $scope.geneStatus = {};
            $scope.oncoTree = {
                mainTypes: [],
                tumorTypes: {}
            };
            $scope.suggestedMutations = [];
            $scope.meta = {
                gene: {}, // Gene meta info from database
                newCancerTypes: [{
                    mainType: '',
                    subtype: '',
                    oncoTreeTumorTypes: []
                }]
            };
            $scope.status = {
                expandAll: false,
                rendering: true,
                numAccordion: 0,
                processing: false,
                fileEditable : false,
                geneReleased: '',
                releasingGene: false,
                removingGene: false,
                savedGene: true
            };

            $scope.$watch('meta.newCancerTypes', function (n) {
                if (n.length > 0 && (n[n.length - 1].mainType || n[n.length - 1].subtype)) {
                    $scope.meta.newCancerTypes.push({
                        mainType: '',
                        subtype: '',
                        oncoTreeTumorTypes: angular.copy($scope.oncoTree.allTumorTypes)
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
                    n[index].oncoTreeTumorTypes = oncoTreeTumorTypes ? oncoTreeTumorTypes : $scope.oncoTree.allTumorTypes;

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
            function findCancerType(index, mainType, subtype, callback) {
                var list;
                var _mainType;
                if (mainType && mainType.name) {
                    list = $scope.oncoTree.tumorTypes[mainType.name];
                }
                if (!mainType && subtype) {
                    _mainType = findMainType(subtype.mainType.name);
                }
                callback(index, _mainType, subtype, list);
            }
            function findMainType(name) {
                for (var i = 0; i < $scope.oncoTree.mainTypes.length; i++) {
                    if ($scope.oncoTree.mainTypes[i].name === name) {
                        return $scope.oncoTree.mainTypes[i];
                    }
                }
                return '';
            }

            function findTumorTypeByMainType(index, mainType, callback) {
                if (mainType && mainType.name) {
                    if ($scope.oncoTree.tumorTypes.hasOwnProperty(mainType.name)) {
                        if (_.isFunction(callback)) {
                            callback(index, $scope.oncoTree.tumorTypes[mainType.name], 'mainType');
                        }
                    } else {
                        DatabaseConnector.getOncoTreeTumorTypesByMainType(mainType.name)
                            .then(function (result) {
                                if (result.data) {
                                    $scope.oncoTree.tumorTypes[mainType.name] = result.data;
                                    if (_.isFunction(callback)) {
                                        callback(index, result.data, 'mainType');
                                    }
                                }
                            }, function () {
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
            $scope.tumorsByMutation = {};
            $scope.TIsByTumor = {};
            $scope.treatmentsByII = {};
            $scope.bindTumors = function (obj) {
                $scope.tumorsByMutation[obj.uuid] = $firebaseArray(firebase.database().ref(getRefByPath(obj.path)));
            };
            $scope.bindTIs = function (obj) {
                $scope.TIsByTumor[obj.uuid] = $firebaseArray(firebase.database().ref(getRefByPath(obj.path)));
            };
            $scope.bindTreatments = function (obj) {
                $scope.treatmentsByII[obj.uuid] = $firebaseArray(firebase.database().ref(getRefByPath(obj.path)));
            }
            function getAllCollaborators() {
                var defer = $q.defer();
                user.getAllUsers().then(function (allUsersInfo) {
                    firebase.database().ref('Meta/collaborators').on('value', function (collaborators) {
                        var allColl = collaborators.val();
                        $scope.collaboratorsMeta = allColl;
                        var tempCollaborators = {};
                        _.each(_.keys(allColl), function (key) {
                            if (allColl[key].indexOf($routeParams.geneName) !== -1) {
                                _.some(allUsersInfo, function(user) {
                                    if (!_.isUndefined(user.name) && user.name.toLowerCase() === key) {
                                        tempCollaborators[key] = user;
                                        return true;
                                    }
                                });
                            }
                        });
                        $rootScope.collaborators = tempCollaborators;
                        //If an admin enter the review mode and left gene page directly without click Review Complete button, we need to reset the currentReviewer
                        if (!$rootScope.collaborators[$rootScope.me.name.toLowerCase()] && $rootScope.reviewMode &&
                            !_.isUndefined($scope.geneMeta.review) && $scope.geneMeta.review.currentReviewer === $rootScope.me.name) {
                            firebase.database().ref('Meta/' + $routeParams.geneName + '/review').update({currentReviewer: ''}).then(function (result) {}).catch(function (error) {});
                        }
                        defer.resolve();
                    }, function (error) {
                        defer.reject(error);
                    });
                }, function (error) {
                    console.log(error);
                    defer.reject(error);
                });

                return defer.promise;
            }
            function getRefByPath(path) {
                var indicies = getIndexByPath(path);
                var result = 'Genes/' + $routeParams.geneName + '/mutations/';
                if (indicies[0] !== -1) {
                    result += indicies[0] + '/tumors';
                    if (indicies[1] !== -1) {
                        result += '/' + indicies[1] + '/TIs';
                        if (indicies[2] !== -1) {
                            result += '/' + indicies[2] + '/treatments';
                        }
                    }
                }
                return result;
            }
            $scope.toggleSection = function (uuid, mutationEffectUUID) {
                if ($scope.status.processing) {
                    $scope.status.processing = false;
                    return;
                }
                if (!$scope.initialOpen[uuid]) {
                    $scope.initialOpen[uuid] = true;
                } else {
                    var panel = document.getElementById(uuid);
                    if (panel.style.display === "none") {
                        panel.style.display = "block";
                    } else {
                        panel.style.display = "none";
                    }
                }
            };
            $scope.setSectionOpenStatus = function (type, uuids) {
                _.each(uuids, function (uuid) {
                    if (type === 'open') {
                        $scope.initialOpen[uuid] = true;
                    }
                    var panel = document.getElementById(uuid);
                    if (panel) {
                        panel.style.display = type === "open" ? "block" : "none";
                    }
                });
            }
            $scope.getMutationPanelClass = function(name) {
                var className = 'headerLevel1';
                if (name.length > 80) {
                    if ($scope.reviewMode) {
                        className += ' longMutationNameReview';
                    } else {
                        className += ' longMutationName';
                    }
                }
                return className;
            }
            $scope.getAngleClass = function (uuid) {
                var result = "fa fa-angle-right";
                if ($scope.initialOpen[uuid]) {
                    var panel = document.getElementById(uuid);
                    if (!panel || panel.style.display !== "none") {
                        result = "fa fa-angle-down";
                    }
                }
                result += " angleIconStyle";
                return result;
            };
            $scope.getData = function() {
            };
            $scope.$on('$destroy', function iVeBeenDismissed() {
                $firebaseObject(firebase.database().ref('Meta/' + $routeParams.geneName)).$destroy(function() {
                    console.log('destroyed firebase object', $routeParams.geneName);
                });
                firebase.database().ref('Meta/' + $routeParams.geneName).off();
            });
            $scope.initialOpen = {};
            $scope.mutIndexByUUID = {};
            function populateBindings() {
                $scope.mapScope = {};
                var deferred1 = $q.defer();
                $firebaseObject(firebase.database().ref("Genes/" + $routeParams.geneName)).$bindTo($scope, "gene").then(function () {
                    DatabaseConnector.getAllInternalGenes().then(function(genes) {
                        $scope.status.geneReleased = (_.find(genes.data, function(gene) {
                            return gene.hugoSymbol === $scope.gene.name;
                        }) === undefined) ? 'no' : 'yes';
                    }, function(reason) {
                        // nothing really needs to be done
                    });
                    deferred1.resolve();
                }, function (error) {
                    deferred1.reject(error);
                });
                var deferred2 = $q.defer();
                loadFiles.load(['reviewMeta', 'movingSection'], $routeParams.geneName).then(function() {
                    deferred2.resolve('success');
                }, function(error) {
                    deferred2.reject(error);
                });
                $scope.vusItems = $firebaseArray(firebase.database().ref('VUS/' + $routeParams.geneName));
                var deferred3 = $q.defer();
                $scope.mutations = $firebaseArray(firebase.database().ref('Genes/' + $routeParams.geneName + '/mutations'));
                $scope.mutations.$loaded().then(function (success) {
                    $scope.getMutationMessages();
                    _.each($scope.mutations, function (mutation, index) {
                        $scope.initialOpen[mutation.name_uuid] = false;
                        $scope.mutIndexByUUID[mutation.name_uuid] = index;
                    });
                    deferred3.resolve();
                }, function (error) {
                    deferred3.reject(error);
                });
                var deferred4 = $q.defer();
                $firebaseObject(firebase.database().ref('Meta/' + $routeParams.geneName)).$bindTo($scope, "geneMeta").then(function () {
                    if (_.isUndefined($scope.geneMeta.review)) {
                        $scope.geneMeta.review = { currentReviewer: ''};
                    } else if (_.isUndefined($scope.geneMeta.review.currentReviewer)) {
                        $scope.geneMeta.review.currentReviewer = '';
                    }
                    getAllCollaborators().then(function() {
                        deferred4.resolve('success');
                    }, function() {
                        deferred4.reject('fail to get collaborators info');
                    });
                }, function (error) {
                    deferred4.reject('Failed to bind meta by gene');
                });
                var bindingAPI = [deferred1.promise, deferred2.promise, deferred3.promise, deferred4.promise];
                $q.all(bindingAPI)
                    .then(function (result) {
                        user.setFileeditable([$routeParams.geneName]).then(function (result) {
                            $scope.status.fileEditable = result[$routeParams.geneName];
                            if ($scope.geneMeta.review.currentReviewer && $rootScope.collaborators[$scope.geneMeta.review.currentReviewer.toLowerCase()]) {
                                $rootScope.fileEditable = false;
                            } else {
                                $rootScope.fileEditable = $scope.status.fileEditable;
                            }
                            $scope.status.rendering = false;
                            watchCurrentReviewer();
                        }, function (error) {
                            $rootScope.fileEditable = false;
                            $scope.status.rendering = false;
                        });
                    }, function (error) {
                        console.log('Error happened', error);
                    });
            }
            function watchCurrentReviewer() {
                $scope.$watch('geneMeta.review.currentReviewer', function(n, o) {
                    if (n !== o) {
                        if ($rootScope.collaborators && $rootScope.collaborators[$rootScope.me.name.toLowerCase()]) {
                            if (!n) {
                                if ($scope.status.fileEditable === true && $rootScope.fileEditable === false) {
                                    $rootScope.fileEditable = $scope.status.fileEditable;
                                    console.log('reset happening');
                                }
                            } else if (n !== $rootScope.me.name) {
                                $rootScope.fileEditable = false;
                            }
                        }
                    }
                });
            }
            $scope.getObservePath = function (data) {
                if (data.type === 'gene') {
                    return 'Genes/' + $routeParams.geneName;
                } else if (data.type === 'geneType') {
                    return 'Genes/' + $routeParams.geneName + '/type';
                } else if (data.type === 'mutation') {
                    return 'Genes/' + $routeParams.geneName + '/mutations/' + data.index;
                } else if (data.type === 'mutation_effect') {
                    return data.path + '/mutation_effect';
                } else if (data.type === 'tumor') {
                    return data.path + '/tumors/' + data.index;
                } else if (data.type === 'diagnostic') {
                    return data.path + '/diagnostic';
                } else if (data.type === 'prognostic') {
                    return data.path + '/prognostic';
                } else if (data.type === 'ti') {
                    return data.path + '/TIs/' + data.index;
                } else if (data.type === 'treatment') {
                    return data.path + '/treatments/' + data.index;
                } else if (data.type === 'vus') {
                    return 'VUS/' + $routeParams.geneName + '/' + data.variant.$id;
                }
            };
            $scope.getUUID = function (data) {
                var obj;
                if (data.type === 'gene') {
                    obj = $scope.gene;
                } else {
                    var indices = getIndexByPath(data.path);
                    if (data.type === 'mutation') {
                        obj = $scope.mutations[indices[0]];
                    } else if (data.type === 'mutation_effect') {
                        obj = $scope.mutations[indices[0]].mutation_effect;
                    } else if (data.type === 'tumor') {
                        obj = $scope.mutations[indices[0]].tumors[indices[1]];
                    } else if (data.type === 'diagnostic') {
                        obj = $scope.mutations[indices[0]].tumors[indices[1]].diagnostic;
                    } else if (data.type === 'prognostic') {
                        obj = $scope.mutations[indices[0]].tumors[indices[1]].prognostic;
                    } else if (data.type === 'ti') {
                        obj = $scope.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]];
                    } else if (data.type === 'treatment') {
                        obj = $scope.mutations[indices[0]].tumors[indices[1]].TIs[indices[2]].treatments[indices[3]];
                    }
                }
                return obj[data.key + '_uuid'];
            };

            $scope.subIsSameWithMain = function(cancerType, type) {
                cancerType.message = '';
                if (type === 'sub' || !cancerType.mainType) {
                    return;
                }
                var mainTypeName = cancerType.mainType.name;
                _.some(cancerType.oncoTreeTumorTypes, function(subtypeItem) {
                    if (subtypeItem.name === mainTypeName) {
                        cancerType.message = 'same name also exist in Subtype';
                        return true;
                    }
                });
            };
            function getOncoTreeMainTypes() {
                mainUtils.getOncoTreeMainTypes().then(function(result) {
                    var mainTypesReturned = result.mainTypes,
                        tumorTypesReturned = result.tumorTypes;
                    if (mainTypesReturned) {
                        $scope.oncoTree.mainTypes = mainTypesReturned;
                        if (_.isArray(tumorTypesReturned)) {
                            var tumorTypes = {};
                            var allTumorTypes = [];
                            _.each(mainTypesReturned, function(mainType, i) {
                                tumorTypes[mainType.name] = tumorTypesReturned[i];
                                allTumorTypes = _.union(allTumorTypes, tumorTypesReturned[i]);
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
                        }
                    }
                }, function(error) {
                });
            }
        }]
    )

    .controller('ReleaseGeneDialogCtrl', function($scope, $modalInstance, data) {
        $scope.statusChecks = data.statusChecks || [];
        $scope.confirmingRelease = false;
        $scope.error='';

        _.each($scope.statusChecks, function(statusCheck) {
            if (!statusCheck.status) {
                if (statusCheck.promise) {
                    statusCheck.status = 'checking';
                    statusCheck.promise.then(function(message) {
                        statusCheck.status = 'success';
                        if (message) {
                            statusCheck.message = message;
                        }
                    }, function(message) {
                        statusCheck.status = 'error';
                        if (message) {
                            statusCheck.message = message;
                        }
                    });
                } else {
                    statusCheck.status = 'warning';
                }
            }
        });

        $scope.cancel = function () {
            $modalInstance.dismiss('canceled');
            (data.cancelCallback || function(){})();
        };

        $scope.confirm = function() {
            $scope.confirmingRelease = true;
            setTimeout(function() {
                if (data.confirmCallback) {
                    data.confirmCallback().then(function() {
                        $modalInstance.dismiss('canceled');
                    }, function(error) {
                        $scope.error = 'Some error happened: ' + error.message;
                    }).finally(function() {
                        $scope.confirmingRelease = false;
                    })
                } else {
                    $modalInstance.dismiss('canceled');
                }
            }, 200);
        };

        $scope.confirmButtonDisabled = function() {
            return !_.every($scope.statusChecks, function(statusCheck) {
                return statusCheck.status === 'success' || statusCheck.status === 'warning';
            }) || $scope.error;
        };

    })
    .controller('ModifyTherapyCtrl', function ($scope, $modalInstance, data, _, OncoKB, $rootScope, drugMapUtils, mainUtils) {
        $scope.modifyName = data.modifyName;
        $scope.mutationRef = data.mutationRef;
        $scope.tiRef = data.tiRef;
        $scope.tumorRef = data.tumorRef;
        $scope.treatmentRef = data.treatmentRef;
        var geneName = data.geneName;

        $scope.saveTherapiesCallback = function(newTreatmentName, oldContent){
            var name_uuid = $scope.treatmentRef.name_uuid;
            if(!_.isEmpty($scope.treatmentRef.name)) {
                if (_.isUndefined($scope.treatmentRef.name_review)) {
                    $scope.treatmentRef.name_review = {
                        'updatedBy' : $rootScope.me.name,
                        'updateTime':  new Date().getTime(),
                    };
                }
                else if (_.isUndefined($scope.treatmentRef.name_review.updatedBy) || _.isUndefined($scope.treatmentRef.name_review.updateTime)) {
                    $scope.treatmentRef.name_review.updatedBy = $rootScope.me.name;
                    $scope.treatmentRef.name_review.updateTime = new Date().getTime();
                }
                if (_.isUndefined($scope.treatmentRef.name_review.lastReviewed)&&_.isUndefined($scope.treatmentRef.name_review.added)) {
                    $scope.treatmentRef.name_review.lastReviewed = $scope.treatmentRef.name;
                }
                mainUtils.setUUIDInReview(name_uuid);
            }
            $scope.treatmentRef.name = newTreatmentName;
            var mutationUuid = $scope.mutationRef.name_uuid;
            var mutationName = $scope.mutationRef.name;
            var cancerTypeUuid = $scope.tumorRef.cancerTypes_uuid;
            drugMapUtils.changeMapByCurator('name', 'treatment', geneName, mutationUuid, mutationName, cancerTypeUuid, name_uuid, newTreatmentName, oldContent);
        }

        $scope.closeWindow = function () {
            $modalInstance.dismiss('canceled');
        }
    })

    .controller('ModifyTumorTypeCtrl', function ($scope, $modalInstance, data, _, OncoKB, $rootScope, mainUtils, FirebaseModel, $timeout) {
        $scope.meta = {
            cancerTypes: data.tumor.cancerTypes,
            originalTumorName: mainUtils.getFullCancerTypesNames(data.tumor.cancerTypes),
            newCancerTypes: [],
            cancerTypes_review: data.tumor.cancerTypes_review,
            cancerTypes_uuid: data.tumor.cancerTypes_uuid,
            oncoTree: data.oncoTree,
            mutation: data.mutation,
            invalid: true,
            message: ''
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('canceled');
        };

        $scope.save = function () {
            var cancerTypes = [];
            if(!_.isEmpty($scope.meta.cancerTypes)) {
                if (_.isUndefined(data.tumorRef.cancerTypes_review)) {
                    data.tumorRef.cancerTypes_review = {};
                }
                if (_.isUndefined(data.tumorRef.cancerTypes_review.updatedBy)) {
                    data.tumorRef.cancerTypes_review.updatedBy = $rootScope.me.name;
                }
                if (_.isUndefined(data.tumorRef.cancerTypes_review.updateTime)) {
                    data.tumorRef.cancerTypes_review.updateTime = new Date().getTime();
                }
                if (_.isUndefined(data.tumorRef.cancerTypes_review.lastReviewed)) {
                    data.tumorRef.cancerTypes_review.lastReviewed = $scope.meta.cancerTypes;
                }
                mainUtils.setUUIDInReview($scope.meta.cancerTypes_uuid);
            }
            _.each($scope.meta.newCancerTypes, function (ct) {
                if (ct.mainType.name) {
                    var tempSubtype = ct.subtype && ct.subtype.name ? ct.subtype.name : '';
                    var tempCode = ct.subtype && ct.subtype.code ? ct.subtype.code : '';
                    var cancerType = new FirebaseModel.Cancertype(ct.mainType.name, tempSubtype, tempCode);
                    cancerTypes.push(cancerType);
                }
            });
            data.tumorRef.cancerTypes = cancerTypes;
            $modalInstance.close();
        };
        $scope.$watch('meta.newCancerTypes', function (n) {
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
            $scope.tumorValidationCheck();
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
            _.each(data.tumor.cancerTypes, function (cancerType) {
                newCancerTypes.push({
                    mainType: {
                        name: cancerType.mainType
                    },
                    subtype: {
                        name: cancerType.subtype,
                        code: cancerType.code
                    },
                    oncoTreeTumorTypes: angular.copy($scope.meta.oncoTree.allTumorTypes)
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
        // This function is desgined to handle the case that maintype name is the same with subtype
        // For example, previous cancerTypes are "Melanoma(maintype), Colorectal Cancer", the current cancerTypes are "Melanoma(subtype), Colorectal Cancer"
        // In that case, we should allow save changes even though the tumor type name stay the same
        function changedSubSameWithMainItem(newCancerTypes, oldCancerTypes) {
            var result = false;
            _.some(newCancerTypes, function(item1) {
                if (item1.mainType && item1.subtype && item1.mainType.name === item1.subtype.name) {
                    _.some(oldCancerTypes, function(item2) {
                        if (item2.mainType && item2.mainType === item1.mainType.name && !item2.subtype) {
                            result = true;
                            return true;
                        }
                    });
                    return true;
                }
            });
            if (result) {
                return result;
            }
            _.some(oldCancerTypes, function(item1) {
                if (item1.mainType && item1.subtype && item1.mainType === item1.subtype) {
                    _.some(newCancerTypes, function(item2) {
                        if (item2.mainType && item2.mainType.name === item1.mainType && !(item2.subtype && item2.subtype.name)) {
                            result = true;
                            return true;
                        }
                    });
                    return true;
                }
            });
            return result;
        }
        $scope.tumorValidationCheck = function () {
            var tumorNameList = [];
            _.each($scope.meta.mutation.tumors, function (tumor) {
                tumorNameList.push(mainUtils.getFullCancerTypesName(tumor.cancerTypes));
            });
            var currentTumorStr = mainUtils.getFullCancerTypesNames($scope.meta.newCancerTypes);
            if (!currentTumorStr) {
                $scope.meta.message = 'Please input cancer type';
                $scope.meta.invalid = true;
            } else if (currentTumorStr === $scope.meta.originalTumorName) {
                var exceptionResult = changedSubSameWithMainItem($scope.meta.newCancerTypes, data.tumor.cancerTypes);
                if (exceptionResult) {
                    $scope.meta.message = '';
                    $scope.meta.invalid = false;
                } else {
                    $scope.meta.message = 'Same with original tumor type';
                    $scope.meta.invalid = true;
                }
            } else if (mainUtils.hasDuplicateCancerTypes($scope.meta.newCancerTypes)) {
                $scope.meta.message = 'Remove duplication in the cancer type input';
                $scope.meta.invalid = true;
            } else if (tumorNameList.indexOf(currentTumorStr) !== -1) {
                $scope.meta.message = 'Same tumor type already exists';
                $scope.meta.invalid = true;
            } else {
                $scope.meta.message = '';
                $scope.meta.invalid = false;
            }
        };
    });
