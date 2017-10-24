'use strict';

angular.module('oncokbApp')
    .controller('GeneCtrl', ['_', 'S', '$resource', '$interval', '$timeout', '$scope', '$rootScope', '$location', '$route', '$routeParams', '$window', '$q', 'dialogs', 'importer', 'storage', 'loadFile', 'user', 'users', 'documents', 'OncoKB', 'gapi', 'DatabaseConnector', 'SecretEmptyKey', '$sce', 'jspdf', 'FindRegex', 'stringUtils', 'mainUtils', 'ReviewResource',
        function(_, S, $resource, $interval, $timeout, $scope, $rootScope, $location, $route, $routeParams, $window, $q, dialogs, importer, storage, loadFile, User, Users, Documents, OncoKB, gapi, DatabaseConnector, SecretEmptyKey, $sce, jspdf, FindRegex, stringUtils, mainUtils, ReviewResource) {
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
                    if ($scope.validateMutation(newMutationName) === true) {
                        // This check is for the case that a mutation got deleted and confirmed. And then add the same name mutation
                        $scope.getMutationMessages();
                        $scope.realtimeDocument.getModel().beginCompoundOperation();
                        var _mutation = $scope.realtimeDocument.getModel().create(OncoKB.Mutation);
                        var filteredContent = [];
                        _.each(newMutationName.split(','), function(item) {
                            item = item.trim();
                            if (item.length > 0) {
                                filteredContent.push(item);
                            }
                        });
                        _mutation.name.setText(filteredContent.join(','));
                        _mutation.oncogenic_eStatus.set('obsolete', 'false');
                        _mutation.name_review.set('added', true);
                        _mutation.name_review.set('updatedBy', User.name);
                        _mutation.name_review.set('updateTime', new Date().getTime());
                        this.gene.mutations.push(_mutation);
                        $scope.realtimeDocument.getModel().endCompoundOperation();
                        $scope.initGeneStatus(_mutation);
                    }
                }
            };
            $scope.getMutationMessages = function() {
                $scope.mutationMessages = {};
                var vusList = [];
                $scope.vus.asArray().forEach(function(e) {
                    vusList.push(e.name.getText().trim().toLowerCase());
                });
                var mutationNameBlackList = [
                    'activating mutations',
                    'activating mutation',
                    'inactivating mutations',
                    'inactivating mutation'
                ];

                var tempNameList = [];
                for (var i = 0; i < $scope.gene.mutations.length; i++) {
                    var mutationName = $scope.gene.mutations.get(i).name.getText().trim().toLowerCase();
                    if (mutationNameBlackList.indexOf(mutationName) !== -1) {
                        $scope.mutationMessages[mutationName] = 'This mutation name is not allowed';
                    } else if (vusList.indexOf(mutationName) !== -1) {
                        $scope.mutationMessages[mutationName] = 'Mutation exists in VUS list';
                    } else if (tempNameList.indexOf(mutationName) !== -1) {
                        $scope.mutationMessages[mutationName] = 'Mutation exists';
                    }  else if (tempNameList.indexOf(mutationName) === -1) {
                        tempNameList.push(mutationName);
                        $scope.mutationMessages[mutationName] = '';
                    }
                }
            }
            $rootScope.getTumorMessages = function(mutation) {
                var mutationName = mutation.name.text.toLowerCase();
                if (!$scope.tumorMessages) {
                    $scope.tumorMessages = {};
                }
                $scope.tumorMessages[mutationName] = {};
                var tempNameList = [];
                for (var j = 0; j < mutation.tumors.length; j++) {
                    var tumor = mutation.tumors.get(j);
                    var tumorName = $scope.getCancerTypesName(tumor.cancerTypes).toLowerCase();
                    if (tempNameList.indexOf(tumorName) === -1) {
                        tempNameList.push(tumorName);
                        $scope.tumorMessages[mutationName][tumorName] = '';
                    } else {
                        $scope.tumorMessages[mutationName][tumorName] = 'Tumor exists';
                    }
                }
            }
            $scope.getTreatmentMessages = function(mutation, tumor, ti) {
                var mutationName = mutation.name.text.toLowerCase();
                var tumorName = $scope.getCancerTypesName(tumor.cancerTypes).toLowerCase();
                var tiName = ti.name.text.toLowerCase();
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
                    var treatmentName = ti.treatments.get(n).name.text.toLowerCase();
                    if (tempNameList.indexOf(treatmentName) === -1) {
                        tempNameList.push(treatmentName);
                    } else {
                        $scope.treatmentMessages[mutationName][tumorName][tiName][treatmentName] = 'Therapy exists';
                    }
                }
            }
            $scope.validateMutation = function(newMutationName) {
                newMutationName = newMutationName.toLowerCase();
                var exists = false;
                var removed = false;
                var tempMutation;
                var isVUS = false;
                var mutationNameBlackList = [
                    'activating mutations',
                    'activating mutation',
                    'inactivating mutations',
                    'inactivating mutation'
                ];
                var vusList = [];
                $scope.vus.asArray().forEach(function(e) {
                    vusList.push(e.name.text.toLowerCase());
                });
                if (vusList.indexOf(newMutationName) !== -1) {
                    isVUS = true;
                }
                _.some($scope.gene.mutations.asArray(), function(e) {
                    if (e.name.getText().toLowerCase() === newMutationName) {
                        exists = true;
                        if(e.name_review.get('removed')) {
                            removed = true;
                            tempMutation = e;
                        } else {
                            // set 'removed' to false to make sure we only put removed mutation back when there is only duplicated mutation
                            removed = false;
                            return true;
                        }
                    }
                });
                if (mutationNameBlackList
                        .indexOf(newMutationName) !== -1) {
                    dialogs.notify('Warning',
                        'This mutation name is not allowed.');
                    return false;
                } else if (isVUS) {
                    dialogs.notify('Warning', 'Mutation is in VUS list.');
                    return false;
                } else if (exists) {
                    if(removed) {
                        dialogs.notify('Warning', 'This mutation just got removed, we will reuse the old one.');
                        tempMutation.name_review.set('removed', false);
                        $rootScope.geneMetaData.delete(tempMutation.name_uuid.getText());
                        return false;
                    } else {
                        dialogs.notify('Warning', 'Mutation exists.');
                        return false;
                    }
                } else {
                    return true;
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
            $scope.vusUpdate = function(message) {
                if ($scope.status.isDesiredGene && $rootScope.internal) {
                    if ($scope.status.vusUpdateTimeout) {
                        $timeout.cancel($scope.status.vusUpdateTimeout);
                    }
                    $scope.status.vusUpdateTimeout = $timeout(function() {
                        var vus = $scope.realtimeDocument.getModel().getRoot().get('vus');
                        var vusData = stringUtils.getVUSData(vus);
                        DatabaseConnector.updateVUS($scope.gene.name, JSON.stringify(vusData), function(result) {
                            console.log('success saving vus to database');
                        }, function(error) {
                            console.log('error happened when saving VUS to DB', error);
                            var subject = 'VUS update Error for ' + $scope.gene.name.getText();
                            var content = 'Error happened when ' + message + '. The system error returned is ' + error;
                            mainUtils.sendEmail('dev.oncokb@gmail.com', subject, content);
                        });
                    }, 2000);
                }
            };

            $scope.getData = function() {
            };
            function parseMutationString(mutationStr) {
                mutationStr = mutationStr.replace(/\([^\)]+\)/g, '');
                var parts = _.map(mutationStr.split(','), function(item) {
                    return item.trim();
                });
                var altResults = [];
                var proteinChange = '';
                var displayName = '';

                for (var i = 0; i < parts.length; i++) {
                    if (!parts[i])continue;
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
            /**
             * Check if a section needs to be displayed or not.
             * For instance, would be used to check if Mutation Effect section needs to be displayed.
             * If it is a section with only one item, we still treat it as section by using displayCheck(), e.g. Prevelance, Clinical Trials
             * However, if it is just a single item without section frame work, we use displayPrecisely(), e.g. Tumor Summary, TI Description
             * ***/
            $scope.displayCheck = function(uuid, reviewObj) {
                // regular mode check
                if (!$rootScope.reviewMode) {
                    if (reviewObj && reviewObj.get('removed')) {
                        return false;
                    }
                    return true;
                }
                // review mode check
                return uuid && $scope.sectionUUIDs.indexOf(uuid.getText()) !== -1 || mainUtils.processedInReview('inside', uuid);
            };
            /**
             * Check if each item inside a section needs to be displayed or not
             * For instance, there are three items Oncogenic, Effect and Description inside Mutation Effect section.
             * And this function will be used to check each item needs to be displayed or not.
             * ***/
            $scope.displayPrecisely = function(uuid) {
                if (!$rootScope.reviewMode) return true;
                else {
                    // review mode logic checks
                    if (mainUtils.processedInReview('inside', uuid)) {
                        return true;
                    } else if (mainUtils.needReview(uuid)) {
                        if (!mainUtils.processedInReview('precise', uuid)) {
                            ReviewResource.precise.push(uuid.getText());
                        }
                        return true;
                    } else return mainUtils.processedInReview('precise', uuid);
                }
            };
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
                        var dlg = dialogs.confirm('Reminder', Object.keys(otherCollaborators).join(', ') + ((Object.keys(otherCollaborators).length > 1 ) ? ' are' : ' is') + ' currently working on this gene document. Entering review mode will disable them from editing.');
                        dlg.result.then(function() {
                            prepareReviewItems();
                        });
                    } else {
                        prepareReviewItems();
                    }
                }
            };
            $scope.exitReview = function() {
                $rootScope.geneMetaData.get('currentReviewer').setText('');
                $rootScope.reviewMode = false;
                ReviewResource.reviewMode = false;
                $scope.fileEditable = true;
                evidencesAllUsers = {};
                $interval.cancel($scope.reviewMoeInterval);
                _.each($scope.geneStatus, function(item) {
                    item.isOpen = false;
                });
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
            };
            $scope.developerCheck = function() {
                return mainUtils.developerCheck(Users.getMe().name);
            };
            $scope.geneMainDivStyle = {
                opacity: '1'
            };
            function setReview(uuid, flag) {
                uuid = uuid.getText();
                if (flag) {
                    if ($rootScope.geneMetaData.get(uuid)) {
                        $rootScope.geneMetaData.get(uuid).set('review', true);
                    } else {
                        var temp = $rootScope.metaModel.createMap();
                        temp.set('review', true);
                        $rootScope.geneMetaData.set(uuid, temp);
                    }
                } else if (!flag) {
                    if ($rootScope.geneMetaData.get(uuid)) {
                        $rootScope.geneMetaData.get(uuid).set('review', false);
                    }
                }
            }
            /**
             * This function is used to find the most recent update from a section change. e.g. There are 4 items under NCCN section, and they might get changed at very different time.
             * And we will find the one changed most recently and store them in ReviewResource.mostRecent mapping, so it could be shared across directives and controllers
             * */
            function setUpdatedSignature(tempArr, uuid) {
                if (uuid) {
                    var uuidString = uuid.getText();
                    var mostRecent = stringUtils.mostRecentItem(tempArr);
                    ReviewResource.mostRecent[uuidString] = {
                        updatedBy: tempArr[mostRecent].get('updatedBy'),
                        updateTime: tempArr[mostRecent].get('updateTime')
                    };
                    userNames.push(tempArr[mostRecent].get('updatedBy'));
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
            $scope.getButtonContent = function(x) {
                if(x) {
                    return $scope.status[x].savingAll ? 'Saving ' + '<i class="fa fa-spinner fa-spin"></i>' : 'Accept All Changes from <b>' + x + '</b>';
                }
                return '';
            };
            var userNames = [];
            function prepareReviewItems() {
                $scope.sectionUUIDs = []; // sectionUUIDs is used to store uuid per section.
                $scope.status.noChanges = false;
                $scope.status.hasReviewContent = false;
                $scope.status.mutationChanged = false;
                userNames = [];
                var geneEviKeys = ['summary', 'type', 'background'];
                _.each(geneEviKeys, function(item) {
                    if(mainUtils.needReview($scope.gene[item + '_uuid'])) {
                        $scope.status.hasReviewContent = true;
                        userNames.push($scope.gene[item + '_review'].get('updatedBy'));
                        $scope.sectionUUIDs.push($scope.gene[item + '_uuid'].getText());
                        ReviewResource.updated.push($scope.gene[item + '_uuid'].getText());
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
                for (var i = 0; i < $scope.gene.mutations.length; i++) {
                    var mutation = $scope.gene.mutations.get(i);
                    if(isObsoleted(mutation)) {
                        continue;
                    }
                    mutationSectionChanged = false;
                    if (mutation.name_review.get('added')) {
                        ReviewResource.added.push(mutation.name_uuid.getText());
                        mutationSectionChanged = true;
                    }
                    if (mutation.name_review.get('removed')) {
                        ReviewResource.removed.push(mutation.name_uuid.getText());
                        mutationSectionChanged = true;
                    }
                    if (mutationSectionChanged) {
                        $scope.status.hasReviewContent = true;
                        userNames.push(mutation.name_review.get('updatedBy'));
                        tempArr = collectUUIDs('mutation', mutation, [], true);
                        ReviewResource.inside = _.union(ReviewResource.inside, tempArr);
                        $scope.sectionUUIDs.push(mutation.name_uuid.getText());
                        continue;
                    }
                    if (mainUtils.needReview(mutation.oncogenic_uuid) || mainUtils.needReview(mutation.effect_uuid) || mainUtils.needReview(mutation.description_uuid)) {
                        tempArr = [mutation.oncogenic_review, mutation.effect_review, mutation.description_review];
                        $scope.sectionUUIDs.push(mutation.oncogenic_uuid.getText());
                        ReviewResource.updated.push(mutation.oncogenic_uuid.getText());
                        mutationChanged = true;
                        setUpdatedSignature(tempArr, mutation.oncogenic_uuid);
                    }
                    for (var j = 0; j < mutation.tumors.length; j++) {
                        var tumor = mutation.tumors.get(j);
                        if(isObsoleted(tumor)) {
                            continue;
                        }
                        tumorSectionChanged = false;
                        if (tumor.name_review.get('added')) {
                            ReviewResource.added.push(tumor.name_uuid.getText());
                            tumorSectionChanged = true;
                        }
                        if (tumor.name_review.get('removed')) {
                            ReviewResource.removed.push(tumor.name_uuid.getText());
                            tumorSectionChanged = true;
                        }
                        if (tumorSectionChanged) {
                            mutationChanged = true;
                            userNames.push(tumor.name_review.get('updatedBy'));
                            tempArr = collectUUIDs('tumor', tumor, [], true);
                            ReviewResource.inside = _.union(ReviewResource.inside, tempArr);
                            $scope.sectionUUIDs.push(tumor.name_uuid.getText());
                            continue;
                        }
                        if (mainUtils.needReview(tumor.prevalence_uuid)) {
                            tempArr = [tumor.prevalence_review];
                            $scope.sectionUUIDs.push(tumor.prevalence_uuid.getText());
                            ReviewResource.updated.push(tumor.prevalence_uuid.getText());
                            tumorChanged = true;
                            setUpdatedSignature(tempArr, tumor.prevalence_uuid);
                        }
                        if (mainUtils.needReview(tumor.prognostic.description_uuid) || mainUtils.needReview(tumor.prognostic.level_uuid)) {
                            tempArr = [tumor.prognostic.description_review, tumor.prognostic.level_review];
                            $scope.sectionUUIDs.push(tumor.prognostic_uuid.getText());
                            ReviewResource.updated.push(tumor.prognostic_uuid.getText());
                            tumorChanged = true;
                            setUpdatedSignature(tempArr, tumor.prognostic_uuid);
                        }
                        if (mainUtils.needReview(tumor.diagnostic.description_uuid) || mainUtils.needReview(tumor.diagnostic.level_uuid)) {
                            tempArr = [tumor.diagnostic.description_review, tumor.diagnostic.level_review];
                            $scope.sectionUUIDs.push(tumor.diagnostic_uuid.getText());
                            ReviewResource.updated.push(tumor.diagnostic_uuid.getText());
                            tumorChanged = true;
                            setUpdatedSignature(tempArr, tumor.diagnostic_uuid);
                        }
                        if (mainUtils.needReview(tumor.nccn.therapy_uuid) || mainUtils.needReview(tumor.nccn.disease_uuid) || mainUtils.needReview(tumor.nccn.version_uuid) || mainUtils.needReview(tumor.nccn.description_uuid)) {
                            tempArr = [tumor.nccn.therapy_review, tumor.nccn.disease_review, tumor.nccn.version_review, tumor.nccn.description_review];
                            $scope.sectionUUIDs.push(tumor.nccn_uuid.getText());
                            ReviewResource.updated.push(tumor.nccn_uuid.getText());
                            tumorChanged = true;
                            setUpdatedSignature(tempArr, tumor.nccn_uuid);
                        }
                        if(mainUtils.needReview(tumor.summary_uuid)) {
                            tumorChanged = true;
                            userNames.push(tumor.summary_review.get('updatedBy'));
                            ReviewResource.updated.push(tumor.summary_uuid.getText());
                        }
                        if(mainUtils.needReview(tumor.trials_uuid)) {
                            tumorChanged = true;
                            $scope.sectionUUIDs.push(tumor.trials_uuid.getText());
                            userNames.push(tumor.trials_review.get('updatedBy'));
                            ReviewResource.updated.push(tumor.trials_uuid.getText());
                            tempArr = [tumor.trials_review];
                            setUpdatedSignature(tempArr, tumor.trials_uuid);
                        }

                        for (var k = 0; k < tumor.TI.length; k++) {
                            var ti = tumor.TI.get(k);
                            if(isObsoleted(ti)) {
                                continue;
                            }
                            for (var m = 0; m < ti.treatments.length; m++) {
                                var treatment = ti.treatments.get(m);
                                if(isObsoleted(treatment)) {
                                    continue;
                                }
                                treatmentSectionChanged = false;
                                if (treatment.name_review.get('added')) {
                                    tiChanged = true;
                                    userNames.push(treatment.name_review.get('updatedBy'));
                                    ReviewResource.added.push(treatment.name_uuid.getText());
                                    treatmentSectionChanged = true;
                                }
                                if (treatment.name_review.get('removed')) {
                                    tiChanged = true;
                                    userNames.push(treatment.name_review.get('updatedBy'));
                                    ReviewResource.removed.push(treatment.name_uuid.getText());
                                    treatmentSectionChanged = true;
                                }
                                if (treatmentSectionChanged) {
                                    tempArr = collectUUIDs('treatment', treatment, [], true);
                                    ReviewResource.inside = _.union(ReviewResource.inside, tempArr);
                                    tempArr.push(treatment.name_uuid.getText());
                                    $scope.sectionUUIDs = _.union($scope.sectionUUIDs, tempArr);
                                    continue;
                                }
                                if (mainUtils.needReview(treatment.level_uuid) || mainUtils.needReview(treatment.indication_uuid) || mainUtils.needReview(treatment.description_uuid)) {
                                    tempArr = [treatment.name_review, treatment.level_review, treatment.indication_review, treatment.description_review];
                                    treatmentChanged = true;
                                    setUpdatedSignature([treatment.level_review, treatment.indication_review, treatment.description_review], treatment.name_uuid);
                                    ReviewResource.updated.push(treatment.name_uuid.getText());
                                } else if (mainUtils.needReview(treatment.name_uuid)) {
                                    treatmentChanged = true;
                                    userNames.push(treatment.name_review.get('updatedBy'));
                                    ReviewResource.nameChanged.push(treatment.name_uuid.getText());
                                }
                                if(treatmentChanged) {
                                    $scope.sectionUUIDs.push(treatment.name_uuid.getText());
                                    tiChanged = true;
                                }
                                treatmentChanged = false;
                            }
                            if (mainUtils.needReview(ti.description_uuid)) {
                                ReviewResource.updated.push(ti.description_uuid.getText());
                                userNames.push(ti.description_review.get('updatedBy'));
                                tiChanged = true;
                            }
                            if (tiChanged) {
                                $scope.sectionUUIDs.push(ti.name_uuid.getText());
                                tumorChanged = true;
                            }
                            tiChanged = false;
                        }
                        if(mainUtils.needReview(tumor.name_uuid)) {
                            tumorChanged = true;
                            userNames.push(tumor.name_review.get('updatedBy'));
                            ReviewResource.nameChanged.push(tumor.name_uuid.getText());
                        }
                        if (tumorChanged) {
                            $scope.sectionUUIDs.push(tumor.name_uuid.getText());
                            mutationChanged = true;
                        }
                        tumorChanged = false;
                    }
                    if(mainUtils.needReview(mutation.name_uuid)) {
                        mutationChanged = true;
                        userNames.push(mutation.name_review.get('updatedBy'));
                        ReviewResource.nameChanged.push(mutation.name_uuid.getText());
                    }
                    if (mutationChanged) {
                        $scope.sectionUUIDs.push(mutation.name_uuid.getText());
                        $scope.status.hasReviewContent = true;
                        $scope.status.mutationChanged = true;
                    }
                    mutationChanged = false;
                }

                if($scope.status.hasReviewContent === false) {
                    var incompleteCount = $rootScope.geneMetaData.get('CurationQueueArticles'),
                        allCount = $rootScope.geneMetaData.get('AllArticles');
                    $rootScope.geneMetaData.clear();
                    $rootScope.geneMetaData.set('currentReviewer', $rootScope.metaModel.createString(''));
                    if (_.isNumber(incompleteCount)) {
                        $rootScope.geneMetaData.set('CurationQueueArticles', incompleteCount);
                    }
                    if (_.isNumber(allCount)) {
                        $rootScope.geneMetaData.set('AllArticles', allCount);
                    }
                    dialogs.notify('Warning', 'No changes need to be reviewed');
                } else {
                    $rootScope.geneMetaData.get('currentReviewer').setText(User.name);
                    $rootScope.reviewMode = true;
                    ReviewResource.reviewMode = true;
                    if($scope.status.mutationChanged) {
                        openChangedSections();
                    }
                    var validUsers = [];
                    _.each(_.uniq(userNames), function(userName) {
                        if(userName) {
                            $scope.status[userName] = {};
                            validUsers.push(userName);
                        }
                    });
                    $scope.namesWithChanges = validUsers;
                }
            }
            function openChangedSections() {
                for (var i = 0; i < $scope.gene.mutations.length; i++) {
                    var mutation = $scope.gene.mutations.get(i);
                    if($scope.sectionUUIDs.indexOf(mutation.name_uuid.getText()) === -1) {
                        continue;
                    }
                    if(!$scope.geneStatus[i]) {
                        $scope.initGeneStatus(mutation);
                    }
                    $scope.geneStatus[i].isOpen = true;
                    if($scope.sectionUUIDs.indexOf(mutation.oncogenic_uuid.getText()) !== -1) {
                        $scope.geneStatus[i].oncogenic.isOpen = true;
                    }
                    for(var j = 0; j < mutation.tumors.length; j++) {
                        var tumor = mutation.tumors.get(j);
                        if($scope.sectionUUIDs.indexOf(tumor.name_uuid.getText()) === -1) {
                            continue;
                        }
                        if(!$scope.geneStatus[i][j]) {
                            $scope.initGeneStatus(mutation, tumor);
                        }
                        $scope.geneStatus[i][j].isOpen = true;
                        if($scope.sectionUUIDs.indexOf(tumor.prevalence_uuid.getText()) !== -1) {
                            $scope.geneStatus[i][j].prevalence.isOpen = true;
                        }
                        if($scope.sectionUUIDs.indexOf(tumor.prognostic_uuid.getText()) !== -1) {
                            $scope.geneStatus[i][j].prognostic.isOpen = true;
                        }
                        if($scope.sectionUUIDs.indexOf(tumor.diagnostic_uuid.getText()) !== -1) {
                            $scope.geneStatus[i][j].diagnostic.isOpen = true;
                        }
                        if($scope.sectionUUIDs.indexOf(tumor.nccn_uuid.getText()) !== -1) {
                            $scope.geneStatus[i][j].nccn.isOpen = true;
                        }
                        if($scope.sectionUUIDs.indexOf(tumor.trials_uuid.getText()) !== -1) {
                            $scope.geneStatus[i][j].trials.isOpen = true;
                        }
                        for(var k = 0; k < tumor.TI.length; k++) {
                            var ti = tumor.TI.get(k);
                            if($scope.sectionUUIDs.indexOf(ti.name_uuid.getText()) === -1) {
                                continue;
                            }
                            if (!$scope.geneStatus[i][j][k]) {
                                $scope.initGeneStatus(mutation, tumor, ti);
                            }
                            $scope.geneStatus[i][j][k].isOpen = true;
                            for(var m = 0; m < ti.treatments.length; m++) {
                                var treatment = ti.treatments.get(m);
                                if($scope.sectionUUIDs.indexOf(treatment.name_uuid.getText()) === -1) {
                                    continue;
                                }
                                if(!$scope.geneStatus[i][j][k][m]) {
                                    $scope.initGeneStatus(mutation, tumor, ti, treatment);
                                }
                                $scope.geneStatus[i][j][k][m].isOpen = true;
                            }
                        }
                    }
                }
            };
            function doneSaving(userName) {
                $scope.status[userName].savingAll = false;
                $scope.status[userName].noChanges = true;
                evidencesAllUsers[userName] = {};
            };
            $scope.acceptChangesByPerson = function(userName) {
                if(!userName) {
                    dialogs.error('Error', 'Can not accept changes from invalid user name. Please contact the developer.');
                    return false;
                }
                if ($scope.status.isDesiredGene) {
                    $scope.status[userName].savingAll = true;
                }
                collectChangesByPerson(userName);
                var apiCalls = [];
                if(!_.isEmpty(evidencesAllUsers[userName].geneTypeEvidence)) {
                    apiCalls.push(geneTypeUpdate(userName));
                }
                if(!_.isEmpty(evidencesAllUsers[userName].updatedEvidences)) {
                    apiCalls.push(evidenceBatchUpdate(userName));
                }
                if(!_.isEmpty(evidencesAllUsers[userName].deletedEvidences)) {
                    apiCalls.push(evidenceDeleteUpdate(userName));
                }
                if (apiCalls.length === 0) {
                    doneSaving(userName);
                } else {
                    $q.all(apiCalls)
                        .then(function(result) {
                            doneSaving(userName);
                        }, function(error) {
                            doneSaving(userName);
                            dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                        });
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
                    // for empty section
                    acceptSection(type, mutation, tumor, ti, treatment);
                }
            }
            /*****
             * This function is designed to check if a section has been changed or not, and if it is changed by a certain person
             *  ****/
            function isChangedBy(type, uuid, userName, reviewObj) {
                if (uuid) {
                    if (type === 'section') {
                        uuid = uuid.getText();
                        return uuid && $scope.sectionUUIDs.indexOf(uuid) !== -1 && ReviewResource.mostRecent[uuid] && ReviewResource.mostRecent[uuid].updatedBy === userName;
                    } else if (type === 'precise') {
                        return mainUtils.processedInReview('precise', uuid) && reviewObj && reviewObj.get('updatedBy') === userName;
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
                if (isChangedBy('precise', $scope.gene.type_uuid, userName, $scope.gene.type_review)) {
                    evidencesAllUsers[userName].geneTypeEvidence = {
                        hugoSymbol: $scope.gene.name.getText(),
                        oncogene: $scope.gene.type.get('OCG') ? true : false,
                        tsg: $scope.gene.type.get('TSG') ? true : false
                    };
                    evidencesAllUsers[userName].historyData.geneType = [{
                        lastEditBy: $scope.gene.type_review.get('updatedBy'),
                        operation: 'update',
                        uuids: $scope.gene.type_uuid.getText(),
                        location: 'Gene Type'
                   }];
                }
                for (var i = 0; i < $scope.gene.mutations.length; i++) {
                    var mutation = $scope.gene.mutations.get(i);
                    if(isObsoleted(mutation)) {
                        continue;
                    }
                    // collect changes that happened in mutation level
                    if (mutation.name_review.get('updatedBy') === userName) {
                        if (mainUtils.processedInReview('remove', mutation.name_uuid)) {
                            evidencesAllUsers[userName].deletedEvidences = collectUUIDs('mutation', mutation, evidencesAllUsers[userName].deletedEvidences);
                            evidencesAllUsers[userName].deletedEvidenceModels.push(['mutation', mutation]);
                            evidencesAllUsers[userName].historyData.deletion.push({operation: 'delete', lastEditBy: mutation.name_review.get('updatedBy'), location: mutation.name.getText()});
                            continue;
                        } else if (mainUtils.processedInReview('add', mutation.name_uuid)) {
                            processAddedSection(userName, 'mutation', mutation);
                            continue;
                        } else if (mainUtils.processedInReview('name', mutation.name_uuid)) {
                            formEvidencesPerUser(userName, 'MUTATION_NAME_CHANGE', mutation, null, null, null);
                        }

                    }
                    // collect changes happened inside mutation, similar logics are applied to tumor and treatment
                    if (isChangedBy('section', mutation.oncogenic_uuid, userName)) {
                        formEvidencesPerUser(userName, 'ONCOGENIC', mutation, null, null, null);
                    }
                    for (var j = 0; j < mutation.tumors.length; j++) {
                        var tumor = mutation.tumors.get(j);
                        if(isObsoleted(tumor)) {
                            continue;
                        }
                        if (tumor.name_review.get('updatedBy') === userName) {
                            if (mainUtils.processedInReview('remove', tumor.name_uuid)) {
                                evidencesAllUsers[userName].deletedEvidences = collectUUIDs('tumor', tumor, evidencesAllUsers[userName].deletedEvidences);
                                evidencesAllUsers[userName].deletedEvidenceModels.push(['tumor', mutation, tumor]);
                                evidencesAllUsers[userName].historyData.deletion.push({operation: 'delete', lastEditBy: tumor.name_review.get('updatedBy'), location: historyStr(mutation, tumor)});
                                continue;
                            } else if (mainUtils.processedInReview('add', tumor.name_uuid)) {
                                processAddedSection(userName, 'tumor', mutation, tumor);
                                continue;
                            }
                        }
                        if(tumor.cancerTypes_review.get('updatedBy') === userName && mainUtils.processedInReview('name', tumor.name_uuid)) {
                            formEvidencesPerUser(userName, 'TUMOR_NAME_CHANGE', mutation, tumor, null, null);
                        }
                        if (isChangedBy('section', tumor.prevalence_uuid, userName)) {
                            formEvidencesPerUser(userName, 'PREVALENCE', mutation, tumor, null, null);
                        }
                        if (isChangedBy('section', tumor.prognostic_uuid, userName)) {
                            formEvidencesPerUser(userName, 'PROGNOSTIC_IMPLICATION', mutation, tumor, null, null);
                        }
                        if (isChangedBy('section', tumor.diagnostic_uuid, userName)) {
                            formEvidencesPerUser(userName, 'DIAGNOSTIC_IMPLICATION', mutation, tumor, null, null);
                        }
                        if (isChangedBy('section', tumor.nccn_uuid, userName)) {
                            formEvidencesPerUser(userName, 'NCCN_GUIDELINES', mutation, tumor, null, null);
                        }
                        for (var k = 0; k < tumor.TI.length; k++) {
                            var ti = tumor.TI.get(k);
                            if(isObsoleted(ti)) {
                                continue;
                            }
                            for (var m = 0; m < ti.treatments.length; m++) {
                                var treatment = ti.treatments.get(m);
                                if(isObsoleted(treatment)) {
                                    continue;
                                }
                                if (treatment.name_review.get('updatedBy') === userName) {
                                    if (mainUtils.processedInReview('remove', treatment.name_uuid)) {
                                        evidencesAllUsers[userName].deletedEvidences = collectUUIDs('treatment', treatment, evidencesAllUsers[userName].deletedEvidences);
                                        evidencesAllUsers[userName].deletedEvidenceModels.push(['treatment', mutation, tumor, ti, treatment]);
                                        evidencesAllUsers[userName].historyData.deletion.push({operation: 'delete', lastEditBy: treatment.name_review.get('updatedBy'), location: historyStr(mutation, tumor) + ', ' + ti.name.getText() + ', ' + treatment.name.getText()});
                                        continue;
                                    } else if (mainUtils.processedInReview('add', treatment.name_uuid)) {
                                        processAddedSection(userName, 'treatment', mutation, tumor, ti, treatment);
                                        continue;
                                    } else if (mainUtils.processedInReview('name', treatment.name_uuid)) {
                                        formEvidencesPerUser(userName, 'TREATMENT_NAME_CHANGE', mutation, tumor, ti, treatment);
                                    }
                                }
                                if (isChangedBy('section', treatment.name_uuid, userName)) {
                                    formEvidencesPerUser(userName, ti.name.getText(), mutation, tumor, ti, treatment);
                                }
                            }
                            if (isChangedBy('precise', ti.description_uuid, userName, ti.description_review)) {
                                formEvidencesPerUser(userName, ti.name.getText(), mutation, tumor, ti, null);
                            }
                        }
                        if(isChangedBy('precise', tumor.summary_uuid, userName, tumor.summary_review)) {
                            formEvidencesPerUser(userName, 'TUMOR_TYPE_SUMMARY', mutation, tumor, null, null);
                        }
                        if(isChangedBy('section', tumor.trials_uuid, userName)) {
                            formEvidencesPerUser(userName, 'CLINICAL_TRIAL', mutation, tumor, null, null);
                        }
                    }
                }
            }
            function geneTypeUpdate(userName) {
                var deferred = $q.defer();
                if ($scope.status.isDesiredGene) {
                    var geneTypeEvidence = evidencesAllUsers[userName].geneTypeEvidence;
                    var historyData = evidencesAllUsers[userName].historyData.geneType;
                    DatabaseConnector.updateGeneType($scope.gene.name.getText(), geneTypeEvidence, historyData, function(result) {
                        $scope.modelUpdate('GENE_TYPE', null, null, null, null);
                        deferred.resolve();
                    }, function(error) {
                        deferred.reject(error);
                    });
                } else {
                    $scope.modelUpdate('GENE_TYPE', null, null, null, null);
                    deferred.resolve();
                }
                return deferred.promise;
            }

            function evidenceBatchUpdate(userName) {
                var deferred = $q.defer();
                var updatedEvidenceModels = evidencesAllUsers[userName].updatedEvidenceModels;
                if ($scope.status.isDesiredGene) {
                    var updatedEvidences = evidencesAllUsers[userName].updatedEvidences;
                    var historyData = evidencesAllUsers[userName].historyData.update;
                    _.each(_.keys(updatedEvidences), function(uuid) {
                        if ($rootScope.geneMetaData.get(uuid) && ! $rootScope.geneMetaData.get(uuid).get('review')) {
                            delete updatedEvidences[uuid];
                        }
                    });
                    DatabaseConnector.updateEvidenceBatch(updatedEvidences, historyData, function(result) {
                        for (var i = 0; i < updatedEvidenceModels.length; i++) {
                            $scope.modelUpdate(updatedEvidenceModels[i][0], updatedEvidenceModels[i][1], updatedEvidenceModels[i][2], updatedEvidenceModels[i][3], updatedEvidenceModels[i][4]);
                        }
                        deferred.resolve();
                    }, function(error) {
                        deferred.reject(error);
                    });
                } else {
                    for (var i = 0; i < updatedEvidenceModels.length; i++) {
                        $scope.modelUpdate(updatedEvidenceModels[i][0], updatedEvidenceModels[i][1], updatedEvidenceModels[i][2], updatedEvidenceModels[i][3], updatedEvidenceModels[i][4]);
                    }
                    deferred.resolve();
                }
                return deferred.promise;
            }
            function evidenceDeleteUpdate(userName) {
                var deferred = $q.defer();
                var deletedEvidenceModels = evidencesAllUsers[userName].deletedEvidenceModels;
                if ($scope.status.isDesiredGene) {
                    var deletedEvidences = evidencesAllUsers[userName].deletedEvidences;
                    var historyData = evidencesAllUsers[userName].historyData.deletion;
                    DatabaseConnector.deleteEvidences(deletedEvidences, historyData, function(result) {
                        _.each(deletedEvidenceModels, function(item) {
                            removeModel(item[0], item[1], item[2], item[3], item[4], deletedEvidences);
                        });
                        deferred.resolve();
                    }, function(error) {
                        deferred.reject(error);
                    });
                } else {
                    _.each(deletedEvidenceModels, function(item) {
                        removeModel(item[0], item[1], item[2], item[3], item[4], deletedEvidences);
                    });
                    deferred.resolve();
                }
                return deferred.promise;
            }
            function historyStr(mutation, tumor) {
                if (mutation && tumor) {
                    return mutation.name.getText() + ', ' + $scope.getCancerTypesName(tumor.cancerTypes);
                }
            }
            $scope.getEvidence = function(type, mutation, tumor, TI, treatment) {
                // The reason we are cheking again if a change has been made to a section is that, there might be many empty content in a newly added section.
                // We need to identify the evidences having input
                var historyData = {operation: 'update'};
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
                        hugoSymbol: $scope.gene.name.getText()
                    },
                    knownEffect: null,
                    lastEdit: null,
                    levelOfEvidence: null,
                    subtype: null,
                    articles: [],
                    clinicalTrials: [],
                    nccnGuidelines: null,
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
                    'P1': 'LEVEL_P1',
                    'P2': 'LEVEL_P2',
                    'P3': 'LEVEL_P3',
                    'P4': 'LEVEL_P4',
                    'D1': 'LEVEL_D1',
                    'D2': 'LEVEL_D2',
                    'D3': 'LEVEL_D3'
                };
                var extraData = _.clone(data);
                var i = 0;
                var uuids = [];
                switch (type) {
                case 'GENE_SUMMARY':
                    data.description = $scope.gene.summary.text;
                    dataUUID = $scope.gene.summary_uuid.getText();
                    data.lastEdit = $scope.gene.summary_review.get('updateTime');
                    historyData.location = 'Gene Summary';
                    reviewObj = $scope.gene.summary_review;
                    break;
                case 'GENE_BACKGROUND':
                    data.description = $scope.gene.background.text;
                    dataUUID = $scope.gene.background_uuid.getText();
                    data.lastEdit = $scope.gene.background_review.get('updateTime');
                    historyData.location = 'Gene Background';
                    reviewObj = $scope.gene.background_review;
                    break;
                case 'ONCOGENIC':
                    if(mainUtils.needReview(mutation.oncogenic_uuid)) {
                        data.knownEffect = mutation.oncogenic.getText();
                        dataUUID = mutation.oncogenic_uuid.getText();
                        data.lastEdit = mutation.oncogenic_review.get('updateTime');
                        historyData.location = mutation.name.getText() + ', Mutation Effect';
                        reviewObj = mutation.oncogenic_review;
                    }
                    // tempFlag is set to true when MUTATION_EFFECT evidence exists which means either mutation effect or mutation description got changed.
                    var tempFlag = false;
                    if (mainUtils.needReview(mutation.effect_uuid) || mainUtils.needReview(mutation.description_uuid)) {
                        tempFlag = true;
                    }
                    if (tempFlag) {
                        tempReviewObjArr = [mutation.effect_review, mutation.description_review];
                        tempRecentIndex = stringUtils.mostRecentItem(tempReviewObjArr, true);
                        extraData.knownEffect = mutation.effect.value.getText();
                        extraDataUUID = mutation.effect_uuid.getText();
                        // We have to calculate the lastEdit time specifically here because ReviewResource.mostRecent[mutation.oncogenic_uuid.getText()].updateTime is the most recent time among three items: oncogenic, mutation effect and description
                        // But here we only need the most recent time from mutation effect and description
                        extraData.lastEdit = tempReviewObjArr[tempRecentIndex].get('updateTime');
                        extraData.description = mutation.description.text;
                        extraData.evidenceType = 'MUTATION_EFFECT';
                        historyData.location = mutation.name.getText() + ', Mutation Effect';
                        if (!reviewObj) {
                            if (mutation.effect_review.has('updatedBy')) {
                                reviewObj = mutation.effect_review;
                            } else  if (mutation.description_review.has('updatedBy')) {
                                reviewObj = mutation.description_review;
                            }
                        }
                    }
                    break;
                case 'TUMOR_TYPE_SUMMARY':
                    if (mainUtils.needReview(tumor.summary_uuid)) {
                        data.description = tumor.summary.text;
                        dataUUID = tumor.summary_uuid.getText();
                        data.lastEdit = tumor.summary_review.get('updateTime');
                        historyData.location = historyStr(mutation, tumor) + ', Tumor Type Summary';
                        reviewObj = tumor.summary_review;
                    }
                    break;
                case 'PREVALENCE':
                    if (mainUtils.needReview(tumor.prevalence_uuid)) {
                        data.description = tumor.prevalence.text;
                        dataUUID = tumor.prevalence_uuid.getText();
                        data.lastEdit = tumor.prevalence_review.get('updateTime');
                        historyData.location = historyStr(mutation, tumor) + ', Prevalence';
                    }
                    break;
                case 'PROGNOSTIC_IMPLICATION':
                    if (mainUtils.needReview(tumor.prognostic.description_uuid) || mainUtils.needReview(tumor.prognostic.level_uuid)) {
                        data.description = tumor.prognostic.description.text;
                        data.levelOfEvidence = levelMapping[tumor.prognostic.level.getText()];
                        dataUUID = tumor.prognostic_uuid.getText();
                        if (!ReviewResource.mostRecent[dataUUID]) {
                            setUpdatedSignature([tumor.prognostic.description_review, tumor.prognostic.level_review], tumor.prognostic_uuid);
                        }
                        data.lastEdit = ReviewResource.mostRecent[dataUUID].updateTime;
                        historyData.location = historyStr(mutation, tumor) + ', Prognostic';
                    }
                    break;
                case 'DIAGNOSTIC_IMPLICATION':
                    if (mainUtils.needReview(tumor.diagnostic.description_uuid) || mainUtils.needReview(tumor.diagnostic.level_uuid)) {
                        data.description = tumor.diagnostic.description.text;
                        data.levelOfEvidence = levelMapping[tumor.diagnostic.level.getText()];
                        dataUUID = tumor.diagnostic_uuid.getText();
                        if (!ReviewResource.mostRecent[dataUUID]) {
                            setUpdatedSignature([tumor.diagnostic.description_review, tumor.diagnostic.level_review], tumor.diagnostic_uuid);
                        }
                        data.lastEdit = ReviewResource.mostRecent[dataUUID].updateTime;
                        historyData.location = historyStr(mutation, tumor) + ', Diagnostic';
                    }
                    break;
                case 'NCCN_GUIDELINES':
                    if (mainUtils.needReview(tumor.nccn.therapy_uuid) || mainUtils.needReview(tumor.nccn.disease_uuid) || mainUtils.needReview(tumor.nccn.version_uuid) || mainUtils.needReview(tumor.nccn.description_uuid)) {
                        data.description = tumor.nccn.description.text;
                        data.nccnGuidelines = [
                            {
                                therapy: tumor.nccn.therapy.text,
                                description: tumor.nccn.description.text,
                                disease: tumor.nccn.disease.text,
                                version: tumor.nccn.version.text
                            }
                        ];
                        dataUUID = tumor.nccn_uuid.getText();
                        if (!ReviewResource.mostRecent[dataUUID]) {
                            setUpdatedSignature([tumor.nccn.therapy_review, tumor.nccn.disease_review, tumor.nccn.version_review, tumor.nccn.description_review], tumor.nccn_uuid);
                        }
                        data.lastEdit = ReviewResource.mostRecent[dataUUID].updateTime;
                        historyData.location = historyStr(mutation, tumor) + ', NCCN';
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
                case 'CLINICAL_TRIAL':
                    if (mainUtils.needReview(tumor.trials_uuid)) {
                        for (i = 0; i < tumor.trials.length; i++) {
                            data.clinicalTrials.push({
                                nctId: tumor.trials.get(i)
                            });
                        }
                        dataUUID = tumor.trials_uuid.getText();
                        data.lastEdit = tumor.trials_review.get('updateTime');
                        historyData.location = historyStr(mutation, tumor) + ', Clinical Trials';
                    }
                    break;
                case 'MUTATION_NAME_CHANGE':
                    uuids = collectUUIDs('mutation', mutation, [], true, true);
                    data.evidenceType = null;
                    historyData.location = mutation.name.getText();
                    break;
                case 'TUMOR_NAME_CHANGE':
                    uuids = collectUUIDs('tumor', tumor, [], true, true);
                    data.evidenceType = null;
                    historyData.location = historyStr(mutation, tumor);
                    break;
                case 'TREATMENT_NAME_CHANGE':
                    uuids = collectUUIDs('treatment', treatment, [], true, true);
                    data.evidenceType = null;
                    historyData.location = historyStr(mutation, tumor) + ', ' + data.evidenceType + ', ' + treatment.name.getText();
                    break;
                default:
                    break;
                }
                if (tumor && type !== 'TREATMENT_NAME_CHANGE') {
                    var tempArr1 = [];
                    var tempArr2 = [];
                    if (mainUtils.needReview(tumor.name_uuid) && _.isArray(tumor.cancerTypes_review.get('lastReviewed')) && tumor.cancerTypes_review.get('lastReviewed').length > 0 && type !== 'TUMOR_NAME_CHANGE' && !tumor.name_review.get('added')) {
                        _.each(tumor.cancerTypes_review.get('lastReviewed'), function(item) {
                            tempArr1.push(item.cancerType);
                            tempArr2.push(item.oncoTreeCode ? item.oncoTreeCode : 'null');
                        });
                    } else {
                        _.each(tumor.cancerTypes.asArray(), function(item) {
                            tempArr1.push(item.cancerType.getText());
                            tempArr2.push(item.oncoTreeCode.getText() ? item.oncoTreeCode.getText() : 'null');
                        });
                    }
                    if(tempArr1.length > 0) {
                        data.cancerType = tempArr1.join(';');
                        data.subtype = tempArr2.join(';');
                    }
                }
                if (TI) {
                    if (!treatment) {
                        if (mainUtils.needReview(TI.description_uuid)) {
                            data.description = TI.description.text;
                            dataUUID = TI.description_uuid.getText();
                            data.lastEdit = TI.description_review.get('updateTime');
                            historyData.location = historyStr(mutation, tumor) + ', ' + data.evidenceType + ', Description';
                            reviewObj = TI.description_review;
                        }
                    } else {
                        dataUUID = treatment.name_uuid.getText();
                        if (!ReviewResource.mostRecent[dataUUID]) {
                            setUpdatedSignature([treatment.name_review, treatment.level_review, treatment.indication_review, treatment.description_review], treatment.name_uuid);
                        }
                        data.lastEdit = ReviewResource.mostRecent[dataUUID].updateTime;
                        data.levelOfEvidence = levelMapping[treatment.level.getText()];
                        data.description = treatment.description.text;
                        data.propagation = levelMapping[treatment.name_eStatus.get('propagation')];
                        data.treatments = [];
                        var treatments = treatment.name.text.split(',');
                        for (i = 0; i < treatments.length; i++) {
                            var drugs = treatments[i].split('+');
                            var drugList = [];
                            for (var j = 0; j < drugs.length; j++) {
                                drugList.push({
                                    drugName: drugs[j].trim()
                                });
                            }
                            data.treatments.push({
                                approvedIndications: [treatment.indication.text],
                                drugs: drugList
                            });
                        }
                        historyData.location = historyStr(mutation, tumor) + ', ' + data.evidenceType + ', ' + treatment.name.getText();
                    }
                }
                if (mutation && ['TUMOR_NAME_CHANGE', 'TREATMENT_NAME_CHANGE'].indexOf(type) === -1) {
                    var mutationStr;
                    if (mainUtils.needReview(mutation.name_uuid) && mutation.name_review.get('lastReviewed') && type !== 'MUTATION_NAME_CHANGE' && !mutation.name_review.get('added')) {
                        mutationStr = stringUtils.getTextString(mutation.name_review.get('lastReviewed'));
                    } else {
                        mutationStr = mutation.name.text;
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
                if(data.lastEdit) {
                    data.lastEdit = validateTimeFormat(data.lastEdit);
                }
                if(extraData.lastEdit) {
                    extraData.lastEdit = validateTimeFormat(extraData.lastEdit);
                }
                if(dataUUID) {
                    evidences[dataUUID] = data;
                    historyUUIDs.push(dataUUID);
                }
                if(extraDataUUID) {
                    evidences[extraDataUUID] = extraData;
                    historyUUIDs.push(extraDataUUID);
                }
                if (historyUUIDs.length > 0) {
                    historyData.uuids = historyUUIDs.join(',');
                    if (dataUUID && ReviewResource.mostRecent[dataUUID]) {
                        historyData.lastEditBy = ReviewResource.mostRecent[dataUUID].updatedBy;
                    } else if (reviewObj) {
                        historyData.lastEditBy = reviewObj.get('updatedBy');
                    }
                }
                if(['MUTATION_NAME_CHANGE', 'TUMOR_NAME_CHANGE', 'TREATMENT_NAME_CHANGE'].indexOf(type) !== -1) {
                    _.each(uuids, function(uuid) {
                        evidences[uuid] = data;
                    });
                    historyData.operation = 'name change';
                    switch(type) {
                    case 'MUTATION_NAME_CHANGE':
                        historyData.uuids = mutation.name_uuid.getText();
                        historyData.lastEditBy = mutation.name_review.get('updatedBy');
                        break;
                    case 'TUMOR_NAME_CHANGE':
                        historyData.uuids = tumor.name_uuid.getText();
                        historyData.lastEditBy = tumor.cancerTypes_review.get('updatedBy');
                        break;
                    case 'TREATMENT_NAME_CHANGE':
                        historyData.uuids = treatment.name_uuid.getText();
                        historyData.lastEditBy = treatment.name_review.get('updatedBy');
                        break;
                    }
                }
                return {evidences: evidences, historyData: historyData};
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
                    case 'nct':
                        data.clinicalTrials.push({
                            nctId: tempAbstract.id
                        });
                        break;
                    }
                }
            }
            function validateTimeFormat(updateTime) {
                var tempTime = new Date(updateTime);
                if(tempTime instanceof Date && !isNaN(tempTime.getTime())) {
                    updateTime = tempTime.getTime();
                } else {
                    // handle the case of time stamp in string format
                    tempTime = new Date(Number(updateTime));
                    if(tempTime instanceof Date && !isNaN(tempTime.getTime())) {
                        updateTime = tempTime.getTime();
                    } else {
                        updateTime = new Date().getTime();
                    }
                }
                return updateTime.toString();
            }

            function setReviewModeInterval() {
                $interval.cancel($scope.reviewMoeInterval);
                $scope.reviewMoeInterval = $interval(function() {
                    if ($rootScope.reviewMode) {
                        $scope.review();
                        $interval.cancel($scope.reviewMoeInterval);
                    }
                }, 1000 * 60 * 15);
            }
            function acceptItem(arr, uuid) {
                _.each(arr, function(item) {
                    if (mainUtils.needReview(item.uuid)) {
                        item.reviewObj.delete('lastReviewed');
                        $rootScope.geneMetaData.get(item.uuid.getText()).set('review', false);
                        ReviewResource.accepted.push(item.uuid.getText());
                    }
                });
                if (uuid) {
                    ReviewResource.accepted.push(uuid.getText());
                }
            }
            $scope.modelUpdate = function(type, mutation, tumor, ti, treatment) {
                switch (type) {
                case 'GENE_SUMMARY':
                    acceptItem([{reviewObj: $scope.gene.summary_review, uuid: $scope.gene.summary_uuid}], $scope.gene.summary_uuid);
                    break;
                case 'GENE_BACKGROUND':
                    acceptItem([{reviewObj: $scope.gene.background_review, uuid: $scope.gene.background_uuid}], $scope.gene.background_uuid);
                    break;
                case 'GENE_TYPE':
                    acceptItem([{reviewObj: $scope.gene.type_review, uuid: $scope.gene.type_uuid}], $scope.gene.type_uuid);
                    break;
                case 'ONCOGENIC':
                    acceptItem([{reviewObj: mutation.oncogenic_review, uuid: mutation.oncogenic_uuid},
                        {reviewObj: mutation.effect_review, uuid: mutation.effect_uuid},
                        {reviewObj: mutation.description_review, uuid: mutation.description_uuid}], mutation.oncogenic_uuid);
                    break;
                case 'TUMOR_TYPE_SUMMARY':
                    acceptItem([{reviewObj: tumor.summary_review, uuid: tumor.summary_uuid}], tumor.summary_uuid);
                    break;
                case 'PREVALENCE':
                    acceptItem([{reviewObj: tumor.prevalence_review, uuid: tumor.prevalence_uuid}], tumor.prevalence_uuid);
                    break;
                case 'PROGNOSTIC_IMPLICATION':
                    acceptItem([{reviewObj: tumor.prognostic.description_review, uuid: tumor.prognostic.description_uuid},
                        {reviewObj: tumor.prognostic.level_review, uuid: tumor.prognostic.level_uuid}], tumor.prognostic_uuid);
                    break;
                case 'DIAGNOSTIC_IMPLICATION':
                    acceptItem([{reviewObj: tumor.diagnostic.description_review, uuid: tumor.diagnostic.description_uuid},
                        {reviewObj: tumor.diagnostic.level_review, uuid: tumor.diagnostic.level_uuid}], tumor.diagnostic_uuid);
                    break;
                case 'NCCN_GUIDELINES':
                    acceptItem([{reviewObj: tumor.nccn.therapy_review, uuid: tumor.nccn.therapy_uuid},
                        {reviewObj: tumor.nccn.disease_review, uuid: tumor.nccn.disease_uuid},
                        {reviewObj: tumor.nccn.version_review, uuid: tumor.nccn.version_uuid},
                        {reviewObj: tumor.nccn.description_review, uuid: tumor.nccn.description_uuid}], tumor.nccn_uuid);
                    break;
                case 'Standard implications for sensitivity to therapy':
                case 'Standard implications for resistance to therapy':
                case 'Investigational implications for sensitivity to therapy':
                case 'Investigational implications for resistance to therapy':
                    if (!treatment) {
                        acceptItem([{reviewObj: ti.description_review, uuid: ti.description_uuid}], ti.description_uuid);
                    } else {
                        acceptItem([{reviewObj: treatment.name_review, uuid: treatment.name_uuid},
                            {reviewObj: treatment.indication_review, uuid: treatment.indication_uuid},
                            {reviewObj: treatment.description_review, uuid: treatment.description_uuid}], treatment.name_uuid);
                        // handle level specifically because level and propagation share the same uuid and review object
                        var levelChanged = mainUtils.processedInReview('precise', treatment.level_uuid);
                        if(levelChanged) {
                            $rootScope.geneMetaData.get(treatment.level_uuid.getText()).set('review', false);
                            ReviewResource.accepted.push(treatment.level_uuid.getText());
                            treatment.level_review.delete('lastReviewed');
                        }
                    }
                    break;
                case 'CLINICAL_TRIAL':
                    acceptItem([{reviewObj: tumor.trials_review, uuid: tumor.trials_uuid}], tumor.trials_uuid);
                    break;
                case 'MUTATION_NAME_CHANGE':
                    acceptItem([{reviewObj: mutation.name_review, uuid: mutation.name_uuid}], mutation.name_uuid);
                    break;
                case 'TUMOR_NAME_CHANGE':
                    acceptItem([{reviewObj: tumor.cancerTypes_review, uuid: tumor.name_uuid}], tumor.name_uuid);
                    break;
                case 'TREATMENT_NAME_CHANGE':
                    acceptItem([{reviewObj: treatment.name_review, uuid: treatment.name_uuid}], treatment.name_uuid);
                    break;
                case 'mutation':
                case 'tumor':
                case 'treatment':
                    acceptSection(type, mutation, tumor, ti, treatment);
                    break;
                default:
                    break;
                }
            };
            /*
            * This function is used to collect uuids for obsoleted section.
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
                    return [$scope.gene.summary_uuid.getText()];
                case 'GENE_BACKGROUND':
                    return [$scope.gene.background_uuid.getText()];
                case 'ONCOGENIC':
                    return [mutation.oncogenic_uuid.getText(), mutation.effect_uuid.getText(), mutation.description_uuid.getText()];
                case 'PREVALENCE':
                    return [tumor.prevalence_uuid.getText()];
                case 'PROGNOSTIC_IMPLICATION':
                    return [tumor.prognostic_uuid.getText()];
                case 'DIAGNOSTIC_IMPLICATION':
                    return [tumor.diagnostic_uuid.getText()];
                case 'NCCN_GUIDELINES':
                    return [tumor.nccn_uuid.getText()];
                case 'CLINICAL_TRIAL':
                    return [tumor.trials_uuid.getText()];
                }
            };
            /*
            * When curators unobsoleted items, that would make api call to insert evidences to database
            * This function is used to form evidence models, which would be used for api call
            * */
            function formSectionEvidencesByType(type, mutation, tumor, TI, treatment) {
                var evidences = {};
                var historyData = {operation: 'add'};
                switch (type) {
                case 'mutation':
                    historyData.location = mutation.name.getText();
                    historyData.lastEditBy = mutation.name_review.get('updatedBy');
                    formSectionEvidences(type, mutation, tumor, TI, treatment, evidences, historyData);
                    break;
                case 'tumor':
                    if(isObsoleted(mutation)) return {};
                    historyData.location = historyStr(mutation, tumor);
                    historyData.lastEditBy = tumor.name_review.get('updatedBy');
                    formSectionEvidences(type, mutation, tumor, TI, treatment, evidences, historyData);
                    break;
                case 'TI':
                    if(isObsoleted(mutation) || isObsoleted(tumor)) return {};
                    formSectionEvidences(type, mutation, tumor, TI, treatment, evidences, historyData);
                    break;
                case 'treatment':
                    if(isObsoleted(mutation) || isObsoleted(tumor) || isObsoleted(TI)) return {};
                    historyData.location = historyStr(mutation, tumor) + ', ' + TI.name.getText() + ', ' + treatment.name.getText();
                    historyData.lastEditBy = treatment.name_review.get('updatedBy');
                    formSectionEvidences(type, mutation, tumor, TI, treatment, evidences, historyData);
                    break;
                case 'GENE_SUMMARY':
                case 'GENE_BACKGROUND':
                    formEvidencesByType([type], null, null, null, null, evidences, historyData);
                    break;
                case 'ONCOGENIC':
                case 'PREVALENCE':
                case 'PROGNOSTIC_IMPLICATION':
                case 'DIAGNOSTIC_IMPLICATION':
                case 'NCCN_GUIDELINES':
                case 'CLINICAL_TRIAL':
                    formEvidencesByType([type], mutation, tumor, TI, treatment, evidences, historyData);
                    break;
                }
                return {evidences: evidences, historyData: historyData};
            }
            function formSectionEvidences(type, mutation, tumor, ti, treatment, evidences, historyData) {
                var typeArr = [];
                var dataArr = [];
                var tempType = '';
                if (type === 'mutation') {
                    typeArr = ['ONCOGENIC'];
                    dataArr = mutation.tumors.asArray();
                    tempType = 'tumor';
                }
                if (type === 'tumor') {
                    typeArr = ['TUMOR_TYPE_SUMMARY', 'PREVALENCE', 'PROGNOSTIC_IMPLICATION', 'DIAGNOSTIC_IMPLICATION', 'NCCN_GUIDELINES', 'CLINICAL_TRIAL'];
                    dataArr = tumor.TI.asArray();
                    tempType = 'TI';
                }
                if(type === 'TI') {
                    typeArr = [ti.name.getText()];
                    dataArr = ti.treatments.asArray();
                    tempType = 'treatment';
                    formEvidencesByType(typeArr, mutation, tumor, ti, null, evidences, historyData);
                }
                if (type === 'treatment') {
                    typeArr = [ti.name.getText()];
                }
                formEvidencesByType(typeArr, mutation, tumor, ti, treatment, evidences, historyData);
                _.each(dataArr, function(item) {
                    if(type === 'mutation')tumor = item;
                    if(type === 'tumor')ti = item;
                    if(type === 'TI')treatment = item;
                    formSectionEvidences(tempType, mutation, tumor, ti, treatment, evidences, historyData);
                });
            };
            function formEvidencesByType(types, mutation, tumor, TI, treatment, evidences, historyData) {
                _.each(types, function(type) {
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
            function isObsoleted(object, key) {
                // set default key to be name
                if(!key)key = 'name';
                return object && object[key+'_eStatus'] && object[key+'_eStatus'].get('obsolete') === 'true';
            };
            function setModelForUnobsoleted(type, mutation, tumor, ti, treatment) {
                var reviewObjs = [];
                var sectionReviewObj;
                var uuids = [];
                switch (type) {
                case 'mutation':
                    sectionReviewObj = mutation.name_review;
                    setModelForUnobsoleted('ONCOGENIC', mutation, tumor, ti, treatment);
                    _.each(mutation.tumors.asArray(), function(tumorItem) {
                        if (!isObsoleted(tumorItem)) {
                            setModelForUnobsoleted('tumor', mutation, tumorItem, ti, treatment);
                        }
                    });
                    break;
                case 'tumor':
                    sectionReviewObj = tumor.name_review;
                    if (tumor.summary.text) {
                        reviewObjs.push(tumor.summary_review);
                        uuids.push(tumor.summary_uuid);
                    }
                    setModelForUnobsoleted('PREVALENCE', mutation, tumor, ti, treatment);
                    setModelForUnobsoleted('PROGNOSTIC_IMPLICATION', mutation, tumor, ti, treatment);
                    setModelForUnobsoleted('NCCN_GUIDELINES', mutation, tumor, ti, treatment);
                    setModelForUnobsoleted('CLINICAL_TRIAL', mutation, tumor, ti, treatment);
                    _.each(tumor.TI.asArray(), function(tiItem) {
                        if (!isObsoleted(tiItem)) {
                            setModelForUnobsoleted('TI', mutation, tumor, tiItem, treatment);
                        }
                    });
                    break;
                case 'TI':
                    if (ti.description.text) {
                        reviewObjs.push(ti.description_review);
                        uuids.push(ti.description_uuid);
                    }
                    _.each(ti.treatments.asArray(), function(treatmentItem) {
                        if (!isObsoleted(treatmentItem)) {
                            setModelForUnobsoleted('treatment', mutation, tumor, ti, treatmentItem);
                        }
                    });
                    break;
                case 'treatment':
                    sectionReviewObj = treatment.name_review;
                    uuids.push(treatment.name_uuid);
                    break;
                case 'GENE_SUMMARY':
                    reviewObjs = [$scope.gene.summary_review];
                    uuids = [$scope.gene.summary_uuid];
                    break;
                case 'GENE_BACKGROUND':
                    reviewObjs = [$scope.gene.background_review];
                    uuids = [$scope.gene.background_uuid];
                    break;
                case 'ONCOGENIC':
                    if (mutation.oncogenic.text) {
                        reviewObjs.push(mutation.oncogenic_review);
                        uuids.push(mutation.oncogenic_uuid);
                    }
                    if (mutation.effect.value.text) {
                        reviewObjs.push(mutation.effect_review);
                        uuids.push(mutation.effect_uuid);
                    }
                    if (mutation.description.text) {
                        reviewObjs.push(mutation.description_review);
                        uuids.push(mutation.description_uuid);
                    }
                    break;
                case 'PREVALENCE':
                    if (tumor.prevalence.text) {
                        reviewObjs = [tumor.prevalence_review];
                        uuids = [tumor.prevalence_uuid];
                    }
                    break;
                case 'PROGNOSTIC_IMPLICATION':
                    if (tumor.prognostic.description.text) {
                        reviewObjs = [tumor.prognostic_review];
                        uuids = [tumor.prognostic_uuid];
                    }
                    break;
                case 'NCCN_GUIDELINES':
                    if (tumor.nccn.therapy.text) {
                        reviewObjs.push(tumor.nccn.therapy_review);
                        uuids.push(tumor.nccn.therapy_uuid);
                    }
                    if (tumor.nccn.disease.text) {
                        reviewObjs.push(tumor.nccn.disease_review);
                        uuids.push(tumor.nccn.disease_uuid);
                    }
                    if (tumor.nccn.version.text) {
                        reviewObjs.push(tumor.nccn.version_review);
                        uuids.push(tumor.nccn.version_uuid);
                    }
                    if (tumor.nccn.description.text) {
                        reviewObjs.push(tumor.nccn.description_review);
                        uuids.push(tumor.nccn.description_uuid);
                    }
                    break;
                case 'CLINICAL_TRIAL':
                    if (tumor.trials.length > 0) {
                        reviewObjs = [tumor.trials_review];
                        uuids = [tumor.trials_uuid];
                    }
                    break;
                }
                if (sectionReviewObj) {
                    sectionReviewObj.set('added', true);
                    sectionReviewObj.set('updatedBy', User.name);
                    sectionReviewObj.set('updateTime', new Date().getTime());
                }
                _.each(reviewObjs, function(reviewObj) {
                    reviewObj.set('updatedBy', User.name);
                    reviewObj.set('updateTime', new Date().getTime());
                });

                if (uuids.length > 0) {
                    _.each(uuids, function(uuid) {
                        setUUIDInMeta(uuid.getText());
                    });
                }
            }
            function setUUIDInMeta(uuid) {
                if (!uuid) return;
                var tempMapping = $rootScope.metaModel.createMap();
                tempMapping.set('review', true);
                $rootScope.geneMetaData.set(uuid, tempMapping);
            }
            function isParentObsoleted(type, mutation, tumor, ti, treatment) {
                switch (type) {
                case 'ONCOGENIC':
                case 'tumor':
                    return isObsoleted(mutation);
                case 'TI':
                case 'PREVALENCE':
                case 'PROGNOSTIC_IMPLICATION':
                case 'NCCN_GUIDELINES':
                case 'CLINICAL_TRIAL':
                    return isObsoleted(mutation) || isObsoleted(tumor);
                case 'treatment':
                    return isObsoleted(mutation) || isObsoleted(tumor) || isObsoleted(ti);
                default:
                    return false;
                }
            }
            $scope.applyObsolete = function(eStatus, type, mutation, tumor, ti, treatment) {
                // we do not allow obsolete item any more, only unobsolete
                if(eStatus.get('obsolete') === 'true') {
                    eStatus.set('obsolete', 'false');
                    if (isParentObsoleted(type, mutation, tumor, ti, treatment)) {
                        dialogs.notify('Warning', 'This item is located in an obsoleted section. To display it in review mode, please unobsolete its parent section.');
                    } else {
                        // set obsoleted items as newly added
                        setModelForUnobsoleted(type, mutation, tumor, ti, treatment);
                    }
                }
            };
            function acceptSection(type, mutation, tumor, ti, treatment) {
                var tempUUIDs = getUUIDsByType(type, mutation, tumor, ti, treatment);
                ReviewResource.accepted = _.union(ReviewResource.accepted, tempUUIDs);
                removeUUIDs(tempUUIDs);
                acceptSectionItems(type, mutation, tumor, ti, treatment, true);
            }
            function clearReview(arr) {
                _.each(arr, function(item) {
                    item.delete('lastReviewed');
                });
            }
            function acceptSectionItems(type, mutation, tumor, ti, treatment, firstLayer) {
                switch(type) {
                case 'mutation':
                    ReviewResource.accepted.push(mutation.name_uuid.getText());
                    mutation.name_review.delete('added');
                    clearReview([mutation.name_review, mutation.oncogenic_review, mutation.effect_review, mutation.description_review]);
                    _.each(mutation.tumors.asArray(), function(tumor) {
                        if (!isObsoleted(tumor)) {
                            acceptSectionItems('tumor', mutation, tumor, ti, treatment);
                        }
                    });
                    break;
                case 'tumor':
                    ReviewResource.accepted.push(tumor.name_uuid.getText());
                    tumor.name_review.delete('added');
                    clearReview([tumor.name_review, tumor.summary_review, tumor.prevalence_review, tumor.prognostic.level_review, tumor.prognostic.description_review, tumor.diagnostic.level_review, tumor.diagnostic.description_review,
                        tumor.nccn.therapy_review, tumor.nccn.disease_review, tumor.nccn.version_review, tumor.nccn.description_review, tumor.trials_review]);
                    _.each(tumor.TI.asArray(), function(ti) {
                        clearReview([ti.description_review]);
                        _.each(ti.treatments.asArray(), function(treatment) {
                            if (!isObsoleted(treatment)) {
                                acceptSectionItems('treatment', mutation, tumor, ti, treatment);
                            }
                        });
                    });
                    break;
                case 'treatment':
                    treatment.name_review.delete('added');
                    ReviewResource.accepted = _.union(ReviewResource.accepted, [treatment.name_uuid.getText(), treatment.level_uuid.getText(), treatment.indication_uuid.getText(), treatment.description_uuid.getText()]);
                    clearReview([treatment.name_review, treatment.level_review, treatment.indication_review, treatment.description_review]);
                    break;
                }
            }


            $scope.acceptAdded = function(type, mutation, tumor, ti, treatment) {
                if (!$scope.status.isDesiredGene) {
                    acceptSection(type, mutation, tumor, ti, treatment);
                    return;
                }
                var tempEvidences = formSectionEvidencesByType(type, mutation, tumor, ti, treatment);
                var evidences = tempEvidences.evidences;
                var historyData = [tempEvidences.historyData];
                if (_.isEmpty(evidences)) {
                    acceptSection(type, mutation, tumor, ti, treatment);
                    return;
                }
                var loadingUUID;
                switch(type) {
                case 'mutation':
                    loadingUUID = mutation.name_uuid.getText();
                    break;
                case 'tumor':
                    loadingUUID = tumor.name_uuid.getText();
                    break;
                case 'treatment':
                    loadingUUID = treatment.name_uuid.getText();
                    break;
                }
                if (loadingUUID) {
                    ReviewResource.loading.push(loadingUUID);
                }
                DatabaseConnector.updateEvidenceBatch(evidences, historyData, function(result) {
                    acceptSection(type, mutation, tumor, ti, treatment);
                    ReviewResource.loading = _.without(ReviewResource.loading, loadingUUID);
                }, function(error) {
                    ReviewResource.loading = _.without(ReviewResource.loading, loadingUUID);
                    dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                });
            };
            $scope.rejectAdded = function (type, mutation, tumor, ti, treatment) {
                var dlg = dialogs.confirm('Reminder', 'Are you sure you want to reject this change?');
                dlg.result.then(function() {
                    removeModel(type, mutation, tumor, ti, treatment);
                    var tempUUIDs = getUUIDsByType(type, mutation, tumor, ti, treatment);
                    removeUUIDs(tempUUIDs);
                });
            };
            function clearRollbackLastReview(reviewObjs) {
                _.each(reviewObjs, function(reviewObj) {
                    if (reviewObj.get('rollback') === true) {
                        reviewObj.delete('lastReviewed');
                    }
                });
            }
            function clearUnnecessartLastReviewed() {
                clearRollbackLastReview([$scope.gene.summary_review, $scope.gene.type_review, $scope.gene.background_review]);
                for (var i = 0; i < $scope.gene.mutations.length; i++) {
                    var mutation = $scope.gene.mutations.get(i);
                    clearRollbackLastReview([mutation.name_review, mutation.oncogenic_review, mutation.effect_review, mutation.description_review]);
                    for (var j = 0; j < mutation.tumors.length; j++) {
                        var tumor = mutation.tumors.get(j);
                        clearRollbackLastReview([tumor.summary_review, tumor.prevalence_review, tumor.diagnostic_review, tumor.prognostic_review, tumor.nccn.therapy_review, tumor.nccn.disease_review, tumor.nccn.version_review, tumor.nccn.description_review]);
                        for (var k = 0; k < tumor.TI.length; k++) {
                            var ti = tumor.TI.get(k);
                            clearRollbackLastReview([ti.description_review]);
                            for (var m = 0; m < ti.treatments.length; m++) {
                                var treatment = ti.treatments.get(m);
                                clearRollbackLastReview([treatment.name_review, treatment.level_review, treatment.indication_review, treatment.description_review]);

                            }
                        }
                    }
                }
            }
            $scope.updateGene = function() {
                $scope.docStatus.savedGene = false;
                clearUnnecessartLastReviewed();
                var gene = stringUtils.getGeneData(this.gene, true, true, true, true);
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
            $scope.validateTumor = function(mutation, tumor) {
                var exists = false;
                var removed = false;
                var tempTumor;
                var newTumorTypesName = getNewCancerTypesName($scope.meta.newCancerTypes).toLowerCase();
                _.some(mutation.tumors.asArray(), function(e) {
                    if ($scope.getCancerTypesName(e.cancerTypes).toLowerCase() === newTumorTypesName) {
                        exists = true;
                        if(e.name_review.get('removed')) {
                            removed = true;
                            tempTumor = e;
                        } else {
                            removed = false;
                            return true;
                        }
                    }
                });
                if (exists) {
                    if(removed) {
                        dialogs.notify('Warning', 'This tumor just got removed, we will reuse the old one.');
                        tempTumor.name_review.set('removed', false);
                        $rootScope.geneMetaData.delete(tempTumor.name_uuid.getText());
                        return false;
                    } else {
                        dialogs.notify('Warning', 'Tumor type exists.');
                        return false;
                    }
                } else {
                    return true;
                }
            };
            $scope.getTumorDuplication = function(mutation, tumor) {
                var mutationName = mutation.name.text.toLowerCase();
                var tumorName = $scope.getCancerTypesName(tumor.cancerTypes).toLowerCase();
                if ($scope.tumorMessages[mutationName] && $scope.tumorMessages[mutationName][tumorName]) {
                    return $scope.tumorMessages[mutationName][tumorName];
                } else return '';
            };
            /**
             * check the to be added cancer types are empty or not.
             * It is used to disable Add Tumor Types button if applicable
             * **/
            $scope.emptyTT = function() {
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
            $scope.addTumorType = function(mutation) {
                if (mutation) {
                    if ($scope.validateTumor(mutation)) {
                        $rootScope.getTumorMessages(mutation);
                        var model = $scope.realtimeDocument.getModel();
                        model.beginCompoundOperation();
                        var _tumorType = model.create(OncoKB.Tumor);

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
                        _tumorType.name_review.set('added', true);
                        _tumorType.name_review.set('updatedBy', User.name);
                        _tumorType.name_review.set('updateTime', new Date().getTime());
                        mutation.tumors.push(_tumorType);
                        model.endCompoundOperation();
                        $scope.meta.newCancerTypes = [{
                            mainType: '',
                            subtype: '',
                            oncoTreeTumorTypes: angular.copy($scope.oncoTree.allTumorTypes)
                        }];
                        var mutationIndex = this.gene.mutations.indexOf(mutation);
                        $scope.initGeneStatus(mutation, _tumorType);
                    }
                }
            };

            $scope.modifyTumorType = function(tumorType, mutation) {
                var dlg = dialogs.create('views/modifyTumorTypes.html', 'ModifyTumorTypeCtrl', {
                    model: $scope.realtimeDocument.getModel(),
                    mutation: mutation,
                    cancerTypes: tumorType.cancerTypes,
                    oncoTree: $scope.oncoTree,
                    cancerTypes_review: tumorType.cancerTypes_review,
                    cancerTypes_uuid: tumorType.name_uuid
                }, {
                    size: 'lg'
                });
                dlg.result.then(function(name) {
                    console.log('successfully updated tumor type');
                    // write the old cancertype and subtypes to the review model
                }, function() {
                    console.log('failed to updated tumor type');
                });
            };
            $scope.validateTreatment = function(newTreatmentName, firstEnter, alert, mutation, tumor, ti) {
                var exists = false;
                var removed = false;
                var tempTreatment;
                newTreatmentName = newTreatmentName.toString().trim().toLowerCase();
                _.some(ti.treatments.asArray(), function(e) {
                    if (e.name.getText().toLowerCase() === newTreatmentName) {
                        exists = true;
                        if(e.name_review.get('removed')) {
                            removed = true;
                            tempTreatment = e;
                        } else {
                            removed = false;
                            return true;
                        }
                    }
                });
                if (exists) {
                    if(removed) {
                        dialogs.notify('Warning', 'This Therapy just got removed, we will reuse the old one.');
                        tempTreatment.name_review.set('removed', false);
                        $rootScope.geneMetaData.delete(tempTreatment.name_uuid.getText());
                        return false;
                    } else {
                        dialogs.notify('Warning', 'Therapy exists.');
                        return false;
                    }
                } else {
                    return true;
                }
            };

            // Add new therapeutic implication
            $scope.addTI = function(newTIName, mutation, tumor, ti) {
                if (ti && newTIName) {
                    if ($scope.validateTreatment(newTIName, false, true, mutation, tumor, ti) === true) {
                        $scope.getTreatmentMessages(mutation, tumor, ti);
                        $scope.realtimeDocument.getModel().beginCompoundOperation();
                        var _treatment = $scope.realtimeDocument.getModel().create(OncoKB.Treatment);
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
                        _treatment.name_review.set('added', true);
                        _treatment.name_review.set('updatedBy', User.name);
                        _treatment.name_review.set('updateTime', new Date().getTime());
                        ti.treatments.push(_treatment);
                        $scope.realtimeDocument.getModel().endCompoundOperation();
                        $scope.initGeneStatus(mutation, tumor, ti, _treatment);
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
                            trialsReview.set('updateTime', new Date().getTime());
                            trials.push(newTrial);
                            trialsRollBackCheck(trialsReview, trials, trialsUuid);
                        } else {
                            dialogs.notify('Warning', 'Please check your trial ID format. (e.g. NCT01562899)');
                        }
                    } else {
                        dialogs.notify('Warning', 'Trial exists.');
                    }
                }
            };
            /**
             * This function is used to check if the review mode header and comparison should be displayed or not.
             * */
            $scope.reviewContentDisplay = function(uuid, name) {
                var result = $rootScope.reviewMode && !mainUtils.processedInReview('accept', uuid) && !mainUtils.processedInReview('reject', uuid) && !mainUtils.processedInReview('inside', uuid) && !mainUtils.processedInReview('add', uuid) && !mainUtils.processedInReview('remove', uuid);
                if (name) {
                    result = result && mainUtils.processedInReview('name', uuid);
                }
                return result;
            };
            $scope.notDecisedYet = function(uuid) {
                return !mainUtils.processedInReview('accept', uuid) && !mainUtils.processedInReview('reject', uuid);
            }
            $scope.removeTrial = function(trials, index, trialsReview, trialsUuid) {
                if(trialsReview && !trialsReview.get('lastReviewed')) {
                    trialsReview.set('lastReviewed', trials.asArray().slice(0));
                }
                setReview(trialsUuid, true);
                trialsReview.set('updatedBy', User.name);
                trialsReview.set('updateTime', new Date().getTime());
                trials.remove(index);
                trialsRollBackCheck(trialsReview, trials, trialsUuid);
            };

            function trialsRollBackCheck(trialsReview, trials, trialsUuid) {
                if (trialsReview && trials.asArray().slice(0).sort().join() === trialsReview.get('lastReviewed').slice(0).sort().join()) {
                    trialsReview.delete('lastReviewed');
                    trialsReview.delete('review');
                    trialsReview.delete('updatedBy');
                    setReview(trialsUuid, false);
                }
            }

            $scope.addVUSItem = function(newVUSName, newVUSTime) {
                if (newVUSName) {
                    var notExist = true;
                    newVUSName = newVUSName.trim();
                    $scope.gene.mutations.asArray().forEach(function(e, i) {
                        if (!e.name_review.get('removed') && e.name.getText().trim().toLowerCase() === newVUSName.toLowerCase()) {
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
                        var tempMessage = User.name + ' tried to add ' + newVUSName + ' at ' + new Date().toLocaleString();
                        $scope.vusUpdate(tempMessage);
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

            $scope.getLastReviewedCancerTypesName = mainUtils.getLastReviewedCancerTypesName;

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

            $scope.remove = function(event, type, mutation, tumor, ti, treatment) {
                $scope.stopCollopse(event);
                var directlyRemove = false;
                var deletionMessage = 'Are you sure you want to delete this entry?';
                var obj;
                switch(type) {
                case 'mutation':
                    obj = mutation;
                    break;
                case 'tumor':
                    obj = tumor;
                    break;
                case 'treatment':
                    obj = treatment;
                    break;
                }
                if(isObsoleted(obj)) {
                    directlyRemove = true;
                    deletionMessage += ' This section will be removed directly since it is obsoleted.';
                }
                if(obj.name_review.get('added')) {
                    directlyRemove = true;
                    deletionMessage += ' This section will be removed directly since it is newly added.';
                }
                var dlg = dialogs.confirm('Confirmation', deletionMessage);
                dlg.result.then(function() {
                    // if this section is obsoleted or hasn't been accepted yet, we delete it directly
                    if(directlyRemove) {
                        removeModel(type, mutation, tumor, ti, treatment, []);
                    } else {
                        obj.name_review.set('removed', true);
                        obj.name_review.set('updatedBy', User.name);
                        obj.name_review.set('updateTime', new Date().getTime());
                        setReview(obj.name_uuid, true);
                    }
                }, function() {
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
            function collectUUIDs(type, obj, uuids, inside, evidenceUUIDsOnly) {
                if (type === 'mutation') {
                    if (!inside) {
                        uuids.push(obj.name_uuid.getText());
                    }
                    uuids.push(obj.oncogenic_uuid.getText());
                    uuids.push(obj.effect_uuid.getText());
                    if (!evidenceUUIDsOnly) {
                        uuids.push(obj.description_uuid.getText());
                    }
                    _.each(obj.tumors.asArray(), function(tumor) {
                        collectUUIDs('tumor', tumor, uuids);
                    });
                }
                if (type === 'tumor') {
                    if (!inside) {
                        uuids.push(obj.name_uuid.getText());
                    }
                    uuids.push(obj.summary_uuid.getText());
                    uuids.push(obj.prevalence_uuid.getText());
                    uuids.push(obj.prognostic_uuid.getText());
                    uuids.push(obj.diagnostic_uuid.getText());
                    uuids.push(obj.trials_uuid.getText());
                    uuids.push(obj.nccn_uuid.getText());
                    if (!evidenceUUIDsOnly) {
                        uuids.push(obj.prognostic.level_uuid.getText());
                        uuids.push(obj.prognostic.description_uuid.getText());
                        uuids.push(obj.diagnostic.level_uuid.getText());
                        uuids.push(obj.diagnostic.description_uuid.getText());
                        uuids.push(obj.nccn.therapy_uuid.getText());
                        uuids.push(obj.nccn.disease_uuid.getText());
                        uuids.push(obj.nccn.version_uuid.getText());
                        uuids.push(obj.nccn.description_uuid.getText());
                    }
                    _.each(obj.TI.asArray(), function(ti) {
                        collectUUIDs('TI', ti, uuids);
                    });
                }
                if(type === 'TI') {
                    if (!evidenceUUIDsOnly) {
                        uuids.push(obj.name_uuid.getText());
                    }
                    uuids.push(obj.description_uuid.getText());
                    _.each(obj.treatments.asArray(), function(treatment) {
                        collectUUIDs('treatment', treatment, uuids);
                    });
                }
                if (type === 'treatment') {
                    if (!inside) {
                        uuids.push(obj.name_uuid.getText());
                    }
                    uuids.push(obj.level_uuid.getText());
                    uuids.push(obj.indication_uuid.getText());
                    uuids.push(obj.description_uuid.getText());
                }
                return uuids;
            }
            $scope.confirmDelete = function(type, mutation, tumor, ti, treatment) {
                var location = '';
                var obj;
                switch(type) {
                case 'mutation':
                    obj = mutation;
                    location = mutation.name.getText();
                    break;
                case 'tumor':
                    obj = tumor;
                    location = historyStr(mutation, tumor);
                    break;
                case 'treatment':
                    obj = treatment;
                    location = historyStr(mutation, tumor) + ', ' + ti.name.getText() + ', ' + treatment.name.getText();
                    break;
                }
                var uuids = collectUUIDs(type, obj, []);
                if ($scope.status.isDesiredGene && !isObsoleted(obj)) {
                    var historyData = [{operation: 'delete', lastEditBy: obj.name_review.get('updatedBy'), location: location}];
                    // make the api call to delete evidences
                    var loadingUUID = obj.name_uuid.getText();
                    if (loadingUUID) {
                        ReviewResource.loading.push(loadingUUID);
                    }
                    DatabaseConnector.deleteEvidences(uuids, historyData, function(result) {
                        removeModel(type, mutation, tumor, ti, treatment, uuids);
                        ReviewResource.loading = _.without(ReviewResource.loading, loadingUUID);
                    }, function(error) {
                        dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                        ReviewResource.loading = _.without(ReviewResource.loading, loadingUUID);
                    });
                } else {
                    removeModel(type, mutation, tumor, ti, treatment, uuids);
                }
            };
            function removeModel(type, mutation, tumor, ti, treatment, uuids) {
                var indices = getIndex(mutation, tumor, ti, treatment);
                switch(type) {
                case 'mutation':
                    delete $scope.geneStatus[indices[0]];
                    $scope.geneStatus = migrateGeneStatusPosition($scope.geneStatus, indices[0]);
                    $scope.gene.mutations.removeValue(mutation);
                    break;
                case 'tumor':
                    delete $scope.geneStatus[indices[0]][indices[1]];
                    $scope.geneStatus[indices[0]] = migrateGeneStatusPosition($scope.geneStatus[indices[0]], indices[1]);
                    mutation.tumors.removeValue(tumor);
                    break;
                case 'treatment':
                    delete $scope.geneStatus[indices[0]][indices[1]][indices[2]][indices[3]];
                    $scope.geneStatus[indices[0]][indices[1]][indices[2]] = migrateGeneStatusPosition($scope.geneStatus[indices[0]][indices[1]][indices[2]], indices[3]);
                    ti.treatments.removeValue(treatment);
                    break;
                }
                removeUUIDs(uuids);
            }
            function removeUUIDs(uuids) {
                if (uuids && _.isArray(uuids)) {
                    _.each(uuids, function(uuid) {
                        if (uuid) {
                            $rootScope.geneMetaData.delete(uuid);
                        }
                    });
                }
            }

            function getIndex(mutation, tumor, ti, treatment) {
                var result = [-1, -1, -1, -1]; // Always return four elements array, standing for mutationIndex, tumorIndex, therapyCategoryIndex and treatmentIndex
                if (mutation) {
                    result[0] = $scope.gene.mutations.indexOf(mutation);
                    if (tumor) {
                        result[1] = mutation.tumors.indexOf(tumor);
                        if (ti) {
                            result[2] = tumor.TI.indexOf(ti);
                            if (treatment) {
                                result[3] = ti.treatments.indexOf(treatment);
                            }
                        }
                    }
                }
                return result;
            }

            $scope.initGeneStatus = function(mutation, tumor, ti, treatment) {
                var objects = [mutation, tumor, ti, treatment];
                var indices = getIndex(mutation, tumor, ti, treatment);
                $scope.geneStatus = loopInitGeneStatus(objects, $scope.geneStatus, indices, 0);
            };

            $scope.checkGeneStatus = function(mutation, tumor, ti, treatment, key, statusType) {
                if (!_.isString(statusType)) {
                    statusType = 'isOpen';
                }
                var indices = getIndex(mutation, tumor, ti, treatment);
                var result = loopCheckGeneStatus(false, $scope.geneStatus, indices, 0, key, statusType);
                return result;
            };

            $scope.getGeneStatusItem = function(mutation, tumor, ti, treatment, key) {
                var indices = getIndex(mutation, tumor, ti, treatment);
                var result = loopGetGeneStatusItem(null, $scope.geneStatus, indices, 0, key);
                return result;
            };

            function loopCheckGeneStatus(status, geneStatus, indices, index, key, statusType) {
                var strIndex = indices[index].toString();
                if (geneStatus.hasOwnProperty(strIndex)) {
                    var nextIndex = index + 1;
                    if (!_.isNumber(indices[nextIndex]) || indices[nextIndex] === -1) {
                        if (key) {
                            return geneStatus[strIndex][key] ? geneStatus[strIndex][key][statusType] : false;
                        }
                        return geneStatus[strIndex][statusType];
                    }
                    status = loopCheckGeneStatus(status, geneStatus[strIndex], indices, ++index, key, statusType);
                }
                return status;
            }

            function loopGetGeneStatusItem(ref, geneStatus, indices, index, key) {
                var strIndex = indices[index].toString();
                if (geneStatus.hasOwnProperty(strIndex)) {
                    var nextIndex = index + 1;
                    if (!_.isNumber(indices[nextIndex]) || indices[nextIndex] === -1) {
                        if (key) {
                            return geneStatus[strIndex][key] ? geneStatus[strIndex][key] : null;
                        }
                        return geneStatus[strIndex];
                    }
                    ref = loopGetGeneStatusItem(ref, geneStatus[strIndex], indices, ++index, key);
                }
                return ref;
            }

            function loopInitGeneStatus(objects, geneStatus, indices, index) {
                var defaultIsOpen = false;
                if (index < indices.length && indices[index] !== -1) {
                    var strIndex = indices[index].toString();
                    if (!geneStatus.hasOwnProperty(strIndex)) {
                        geneStatus[strIndex] = new GeneStatusSingleton(defaultIsOpen);
                        if (index === 0) {
                            geneStatus[strIndex].oncogenic = new GeneStatusSingleton(true);
                        } else if (index === 1) {
                            geneStatus[strIndex].prevalence = new GeneStatusSingleton(defaultIsOpen);
                            geneStatus[strIndex].prognostic = new GeneStatusSingleton(defaultIsOpen);
                            geneStatus[strIndex].diagnostic = new GeneStatusSingleton(defaultIsOpen);
                            geneStatus[strIndex].nccn = new GeneStatusSingleton(defaultIsOpen);
                            geneStatus[strIndex].trials = new GeneStatusSingleton(defaultIsOpen);
                        }
                    }
                    geneStatus[strIndex] = loopInitGeneStatus(objects, geneStatus[strIndex], indices, ++index);
                }
                return geneStatus;
            }

            $scope.cancelDelete = function(type, mutation, tumor, ti, treatment) {
                var dlg = dialogs.confirm('Reminder', 'Are you sure you want to reject this change?');
                dlg.result.then(function() {
                    var tempUUIDs = getUUIDsByType(type, mutation, tumor, ti, treatment);
                    ReviewResource.rejected = _.union(ReviewResource.rejected, tempUUIDs);
                    cancelDelteSection(type, mutation, tumor, ti, treatment);
                });
            };
            function cancelDelteSection(type, mutation, tumor, ti, treatment) {
                switch (type) {
                case 'mutation':
                    cancelDeleteItem(mutation);
                    _.each(mutation.tumors.asArray(), function(tumor) {
                        if (tumor.name_review.get('removed')) {
                            cancelDelteSection('tumor', mutation, tumor, ti, treatment);
                        }
                    });
                    break;
                case 'tumor':
                    cancelDeleteItem(tumor);
                    _.each(tumor.TI.asArray(), function(ti) {
                        _.each(ti.treatments.asArray(), function(treatment) {
                            if (treatment.name_review.get('removed')) {
                                cancelDelteSection('treatment', mutation, tumor, ti, treatment);
                            }
                        });
                    });
                    break;
                case 'treatment':
                    cancelDeleteItem(treatment);
                    break;
                }
            }
            function cancelDeleteItem(obj) {
                obj.name_review.delete('removed');
                setReview(obj.name_uuid, false);
                ReviewResource.removed = _.without(ReviewResource.removed, obj.name_uuid.getText());
            }

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
            $scope.specifyAnnotation = function() {
                var annotationLocation = {};
                setAnnotationResult(annotationLocation, fetchResults(FindRegex.result(this.gene.background.text)), 'Gene Background');
                var mutations = stringUtils.getGeneData(this.gene, false, false, false, false, true).mutations;
                _.each(mutations, function(mutation) {
                    setAnnotationResult(annotationLocation, fetchResults(FindRegex.result(JSON.stringify(mutation))), mutation.name);
                });
                return annotationLocation;
            };
            function setAnnotationResult(annotationLocation, results, location) {
                _.each([results.PMIDs, results.abstracts], function(annotations) {
                    _.each(annotations, function(annotation) {
                        annotation = annotation.trim();
                        if(_.has(annotationLocation, annotation)) {
                            annotationLocation[annotation].push(location);
                        } else {
                            annotationLocation[annotation] = [location];
                        }
                    });
                });
            }

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
                if (type === 'oncogenicity') {
                    if ((!mutation.oncogenic.text || mutation.oncogenic.text === 'false') && !mutation.effect.value.text && !mutation.description.text) {
                        return true;
                    }
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
                var tumorKeys = ['prevalence', 'prognostic', 'diagnostic', 'nccn', 'trials'];

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



            function getSuggestedMutations() {
                var defaultPlaceHolder = 'No suggestion found. Please curate according to literature.';
                DatabaseConnector.getSuggestedVariants()
                    .then(function(resp) {
                        if (_.isArray(resp) && resp.length > 0) {
                            $scope.suggestedMutations = resp;
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
                if(!$rootScope.metaData) {
                    storage.retrieveMeta().then(function(result) {
                        if (result && (result.error || !_.isArray(result) || result.length === 0)) {
                            dialogs.error('Error', 'Fail to retrieve meta file! Please stop editing and contact the developer!');
                            var sendTo = 'dev.oncokb@gmail.com';
                            var subject = 'Fail to retrieve meta file';
                            var content;
                            if(_.isArray(result) && result.length === 0) {
                                content = 'There is no meta file inside the Meta folder';
                            } else {
                                content = 'System error is ' + JSON.stringify(result.error);
                            }
                            mainUtils.sendEmail(sendTo, subject, content);
                            callback();
                        } else {
                            storage.getMetaRealtimeDocument(result[0].id).then(function(metaRealtime) {
                                if (metaRealtime && metaRealtime.error) {
                                    dialogs.error('Error', 'Fail to get meta document! Please stop editing and contact the developer!');
                                    $scope.fileEditable = false;
                                    callback();
                                } else {
                                    $rootScope.metaRealtime = metaRealtime;
                                    $rootScope.metaModel = metaRealtime.getModel();
                                    $rootScope.metaData = metaRealtime.getModel().getRoot().get('review');
                                    assignMeta(callback);
                                }
                            });
                        }
                    });
                } else {
                    assignMeta(callback);
                }

            }
            function assignMeta(callback) {
                if (!$rootScope.metaData.get($scope.fileTitle)) {
                    var tempMap = $rootScope.metaModel.createMap();
                    $rootScope.metaData.set($scope.fileTitle, tempMap);
                }
                $rootScope.geneMetaData = $rootScope.metaData.get($scope.fileTitle);
                if(!$rootScope.geneMetaData.has('currentReviewer') || $rootScope.geneMetaData.get('currentReviewer').type !== 'EditableString') {
                    $rootScope.geneMetaData.set('currentReviewer', $rootScope.metaModel.createString(''));
                }
                var tempReviewer = $rootScope.geneMetaData.get('currentReviewer');
                tempReviewer.addEventListener(gapi.drive.realtime.EventType.TEXT_INSERTED, reviewerChange);
                tempReviewer.addEventListener(gapi.drive.realtime.EventType.TEXT_DELETED, reviewerChange);
                callback();
            }

            function bindDocEvents() {
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.COLLABORATOR_JOINED, displayCollaboratorEvent);
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.COLLABORATOR_LEFT, displayCollaboratorEvent);
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED, saveStateChangedEvent);
                $scope.model.addEventListener(gapi.drive.realtime.EventType.UNDO_REDO_STATE_CHANGED, onUndoStateChanged);
                $scope.gene.addEventListener(gapi.drive.realtime.EventType.VALUE_CHANGED, valueChangedEvent);
                $rootScope.metaRealtime.addEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED, saveMetaChangedEvent);
            }
            function reviewerChange() {
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
            }
            function saveStateChangedEvent(evt) {
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
                // Only allow admins to edit Other Biomarkers Gene Sumarry, Background and Gene Type
                if ($scope.gene.name.getText().trim().toLowerCase() === 'other biomarkers' && $scope.userRole !== 8) {
                    $scope.geneEditable = false;
                }
                $scope.document = file;
                $scope.fileEditable = file.editable;
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
                    } else if(underReview()) {
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
                var currentReviewer = $rootScope.geneMetaData.get('currentReviewer');
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
                var currentReviewer = $rootScope.geneMetaData.get('currentReviewer');
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
                if (!$scope.docStatus.updateGene && type !== 'meta') {
                    $scope.gene.status_timeStamp.get('lastEdit').value.setText(new Date().getTime().toString());
                    $scope.gene.status_timeStamp.get('lastEdit').by.setText(Users.getMe().name);
                }
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
                levels.prognostic = [{
                    value: 'P1',
                    label: 'Px1 - WHO included criteria'
                }, {
                    value: 'P2',
                    label: 'Px2 - ELN included criteria (only for AML, may be combined with Px1)'
                }, {
                    value: 'P3',
                    label: 'Px3 - NCCN included criteria'
                }, {
                    value: 'P4',
                    label: 'Px4 - Compelling peer reviewed literature'
                }];
                levels.diagnostic = [{
                    value: 'D1',
                    label: 'Dx1 - WHO included criteria'
                }, {
                    value: 'D2',
                    label: 'Dx2 - NCCN included criteria'
                }, {
                    value: 'D3',
                    label: 'Dx3 - Compelling peer reviewed literature'
                }];
                return levels;
            }

            function GeneStatusSingleton(isOpen) {
                if (!_.isBoolean(isOpen)) {
                    isOpen = false;
                }
                this.isOpen = isOpen;
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

            function isDesiredGene() {
                var _geneName = $scope.gene.name.getText();
                for (var i = 0; i < OncoKB.global.genes.length; i++) {
                    if (OncoKB.global.genes[i].hugoSymbol === _geneName) {
                        $scope.status.isDesiredGene = true;
                        $scope.meta.gene = OncoKB.global.genes[i];
                        break;
                    }
                }
                $rootScope.isDesiredGene = $scope.status.isDesiredGene;
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
            $rootScope.userRole = Users.getMe().role;
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
                isDesiredGene: false,
                hasReviewContent: false, // indicate if any changes need to be reviewed
                mutationChanged: false // indicate there are changes in mutation section
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
            $rootScope.obsoletePermission = ($scope.userRole === 8 || Users.getMe().name.trim().toLowerCase() === 'philip jonsson') ? true : false;
            $scope.$watch('fileEditable', function(n, o) {
                if (n !== o) {
                    $scope.geneEditable = n;
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
                        $rootScope.model = model;
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
                    addVUS();
                    $scope.getMutationMessages();
                })
                .finally(function() {
                    getSuggestedMutations();
                    if (_.isArray(OncoKB.global.genes)) {
                        isDesiredGene();
                    } else {
                        DatabaseConnector.getAllGene(function(data) {
                            OncoKB.global.genes = data;
                            isDesiredGene();
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
                        $rootScope.geneMetaData.get('currentReviewer').getText() +
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
    .controller('ModifyTumorTypeCtrl', function($scope, $modalInstance, data, _, OncoKB, $rootScope, user) {
        $scope.meta = {
            model: data.model,
            mutation: data.mutation,
            oncoTree: data.oncoTree,
            cancerTypes: data.cancerTypes,
            newCancerTypes: [],
            cancerTypes_review: data.cancerTypes_review,
            cancerTypes_uuid: data.cancerTypes_uuid
        };

        $scope.cancel = function() {
            $modalInstance.dismiss('canceled');
        }; // end cancel

        $scope.save = function() {
            $scope.meta.model.beginCompoundOperation();
            var lastReviewed = [];
            for(var i = 0; i < $scope.meta.cancerTypes.length; i++) {
                var item = $scope.meta.cancerTypes.get(i);
                lastReviewed.push({cancerType: item.cancerType.getText(), subtype: item.subtype.getText(), oncoTreeCode: item.oncoTreeCode.getText()});
            }
            if ($scope.meta.cancerTypes_review && _.isNull($scope.meta.cancerTypes_review.get('lastReviewed'))) {
                $scope.meta.cancerTypes_review.set('lastReviewed', lastReviewed);
            }
            $scope.meta.cancerTypes_review.set('updatedBy', user.name);
            $scope.meta.cancerTypes_review.set('updateTime', new Date().getTime());
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

            var uuid = $scope.meta.cancerTypes_uuid.getText();
            if ($rootScope.geneMetaData.get(uuid)) {
                $rootScope.geneMetaData.get(uuid).set('review', true);
            } else {
                var temp = $rootScope.metaModel.createMap();
                temp.set('review', true);
                $rootScope.geneMetaData.set(uuid, temp);
            }
            $rootScope.getTumorMessages($scope.meta.mutation);
        }; // end save

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
        $scope.invalidTumor = false;
        $scope.tumorDuplicationCheck = function() {
            var tumorNameList = [];
            _.each($scope.meta.mutation.tumors.asArray(), function(tumor) {
                var tempTumorStr = '';
                _.each(tumor.cancerTypes.asArray(), function(cancerType) {
                    var mainType = cancerType.cancerType.getText();
                    var subtype = cancerType.subtype.getText();
                    var nonEmpty = false;
                    if (mainType) {
                        tempTumorStr += mainType;
                        nonEmpty = true;
                    }
                    if (subtype) {
                        tempTumorStr += subtype;
                        nonEmpty = true;
                    }
                    if (nonEmpty) {
                        tempTumorStr += ';';
                    }
                });
                tumorNameList.push(tempTumorStr);
            });
            var currentTumorStr = '';
            _.each($scope.meta.newCancerTypes, function(cancerType) {
                var mainType = cancerType.mainType;
                var subtype = cancerType.subtype;
                var nonEmpty = false;
                if (mainType) {
                    currentTumorStr += mainType.name;
                    nonEmpty = true;
                }
                if (subtype) {
                    currentTumorStr += subtype.name;
                    nonEmpty = true;
                }
                if (nonEmpty) {
                    currentTumorStr += ';';
                }
            });
            if (tumorNameList.indexOf(currentTumorStr) !== -1) {
                $scope.invalidTumor = true;
            } else {
                $scope.invalidTumor = false;
            }
        };
    });
