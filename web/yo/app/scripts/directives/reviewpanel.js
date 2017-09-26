'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:reviewpanel
 * @description
 * # reviewpanel
 * This directive is designed specifically for review mode, which cotains change info text, accept icon, reject icon and loading bar
 */
angular.module('oncokbApp')
    .directive('reviewPanel', function($rootScope, DatabaseConnector, dialogs, _, OncoKB, mainUtils, ReviewResource) {
        return {
            templateUrl: 'views/reviewPanel.html',
            restrict: 'AE',
            scope: {
                reviewObj: '=', // review object
                evidenceType: '=',  // evidence type
                mutation: '=', // mutation
                tumor: '=', // tumor
                therapyCategory: '=', // therapy category
                treatment: '=', // treatment
                uuid: '=',
                obj: '=', // temporary object
                confirmDeleteInGene: '&confirmDelete',
                cancelDeleteInGene: '&cancelDelete',
                getEvidenceInGene: '&getEvidence',
                modelUpdateInGene: '&modelUpdate',
                acceptAddedInGene: '&acceptAdded',
                rejectAddedInGene: '&rejectAdded'
            },
            link: function(scope, element, attrs) {
            },
            replace: true,
            controller: function($scope) {
                $scope.operationsName = {'update': 'Updated', 'name': 'Name Changed', 'add': 'Added', 'delete': 'Deleted'};
                $scope.adjustedEvidenceType = $scope.evidenceType;
                $scope.panelType = '';
                $scope.acceptChanges = function(event) {
                    switch($scope.panelType) {
                    case 'update':
                        $scope.accept(event);
                        break;
                    case 'name':
                        $scope.accept(event);
                        break;
                    case 'delete':
                        $scope.confirmDelete(event, $scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                        break;
                    case 'add':
                        $scope.acceptAdded(event, $scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                        break;
                    }
                };
                $scope.rejectChanges = function(event) {
                    switch($scope.panelType) {
                    case 'update':
                        $scope.reject(event);
                        break;
                    case 'name':
                        $scope.reject(event);
                        break;
                    case 'delete':
                        $scope.cancelDelete(event, $scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                        break;
                    case 'add':
                        $scope.rejectAdded(event, $scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                        break;
                    }
                };
                $scope.panelExist = function() {
                    if (mainUtils.isProcessed('inside', $scope.uuid) || mainUtils.isProcessed('rollback', $scope.uuid) || !$scope.panelType) {
                        return false;
                    } else {
                        return true;
                    }
                };
                function isTreatmentType() {
                    return $scope.therapyCategory && $scope.evidenceType === $scope.therapyCategory.name.getText();
                }
                $scope.assignPanelType = function() {
                    // The panel type is assigned in the priority of remove, add, name change and update. Caution should be exercised when adjusting the order, which is reflected in the following if else statements
                    if (mainUtils.isProcessed('remove', $scope.uuid)) {
                        $scope.panelType = 'delete';
                        if (isTreatmentType()) {
                            $scope.adjustedEvidenceType = 'treatment';
                        }
                    } else if (mainUtils.isProcessed('add', $scope.uuid)) {
                        $scope.panelType = 'add';
                        if (isTreatmentType()) {
                            $scope.adjustedEvidenceType = 'treatment';
                        }
                    } else if (mainUtils.isProcessed('name', $scope.uuid)) {
                        $scope.panelType = 'name';
                        if ($scope.evidenceType === 'mutation') {
                            $scope.adjustedEvidenceType = 'MUTATION_NAME_CHANGE';
                        } else if ($scope.evidenceType === 'tumor') {
                            $scope.adjustedEvidenceType = 'TUMOR_NAME_CHANGE';
                        } else if (isTreatmentType()) {
                            $scope.adjustedEvidenceType = 'TREATMENT_NAME_CHANGE';
                        }
                    } else if (mainUtils.isProcessed('update', $scope.uuid)) {
                        $scope.panelType = 'update';
                    }
                };
                $scope.signatureCheck = function() {
                    // Prepare values for panel signature text
                    if ($scope.uuid && ReviewResource.mostRecent[$scope.uuid.getText()]) {
                        // If there are mutiple items inside one section, we use the most recent one, which is calculated in prepareReviewItems() in gene.js and stored in ReviewResource.mostRecent
                        $scope.updatedBy = ReviewResource.mostRecent[$scope.uuid.getText()].updatedBy;
                        $scope.updateTime = ReviewResource.mostRecent[$scope.uuid.getText()].updateTime;
                    } else if ($scope.adjustedEvidenceType === 'TUMOR_NAME_CHANGE') {
                        // For tumor name change, the review info is stored in cancerTypes_review
                        $scope.updatedBy = $scope.tumor.cancerTypes_review.get('updatedBy');
                        $scope.updateTime = $scope.tumor.cancerTypes_review.get('updateTime');
                    } else {
                        $scope.updatedBy = $scope.reviewObj.get('updatedBy');
                        $scope.updateTime = $scope.reviewObj.get('updateTime');
                    }
                    // If any decision hasn't been made yet, we display the panel signature which is a text describing what kind of change is made by who at what time
                    // on the other hand, if the evidence already got accepted or rejected, we hide the panel signature
                    if (!mainUtils.isProcessed('accept', $scope.uuid) && !mainUtils.isProcessed('reject', $scope.uuid)) {
                        return true;
                    } else {
                        return false;
                    }
                };
                $scope.iconClass = function(type) {
                    if (mainUtils.isProcessed('accept', $scope.uuid) || mainUtils.isProcessed('reject', $scope.uuid)) {
                        return 'reviewed';
                    } else if (type === 'accept') {
                        return 'fa-comments-red';
                    } else if (type === 'reject') {
                        return 'fa-comments-grey';
                    }
                };
                $scope.iconExist = function(type) {
                    switch(type) {
                    case 'accept':
                        return !mainUtils.isProcessed('reject', $scope.uuid) && !mainUtils.isProcessed('loading', $scope.uuid);
                    case 'reject':
                        return !mainUtils.isProcessed('accept', $scope.uuid) && !mainUtils.isProcessed('loading', $scope.uuid);
                    case 'loading':
                        return mainUtils.isProcessed('loading', $scope.uuid);
                    }
                };
                $scope.panelClass = function(type) {
                    if (type === 'text') {
                        // only deleted evidence signature are in red, otherwise regular style text
                        if ($scope.panelType === 'delete') {
                            return 'sectionText';
                        } else {
                            return 'updateText';
                        }
                    }
                    // If the panel type is add, remove or name changed, or treatment level update, we need to add 'panelMargin' class to shift it down
                    if (type === 'panel') {
                        if ($scope.panelType !== 'update' || isTreatmentType()) {
                            return 'panelMargin';
                        } else {
                            return '';
                        }
                    }
                };
                $scope.getEvidence = function(type, mutation, tumor, therapyCategory, treatment) {
                    return $scope.getEvidenceInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };

                $scope.accept = function(event) {
                    if (event !== null) {
                        $scope.$parent.stopCollopse(event);
                    }
                    if (mainUtils.isProcessed('accept', $scope.uuid) || mainUtils.isProcessed('reject', $scope.uuid)) {
                        return;
                    }
                    if ($scope.adjustedEvidenceType === 'GENE_TYPE') {
                        var params = {
                            hugoSymbol: $scope.obj.name.getText(),
                            oncogene: $scope.obj.type.get('OCG').trim().length > 0,
                            tsg: $scope.obj.type.get('TSG').trim().length > 0
                        };
                        var historyData = [{
                            lastEditBy: $scope.obj.type_review.get('updatedBy'),
                            operationName: 'update',
                            uuids: $scope.obj.type_uuid.getText()
                        }];
                        if ($rootScope.isDesiredGene) {
                            ReviewResource.loading.push($scope.uuid.getText());
                            DatabaseConnector.updateGeneType($scope.obj.name.getText(), params, historyData, function(result) {
                                $scope.modelUpdate($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                                ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid.getText());
                            }, function(error) {
                                console.log('fail to update to database', error);
                                dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                                ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid.getText());
                            });
                        } else {
                            $scope.modelUpdate($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                        }
                    } else {
                        if($rootScope.isDesiredGene) {
                            ReviewResource.loading.push($scope.uuid.getText());
                            var getEvidenceResult = $scope.getEvidence($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                            var evidences = getEvidenceResult.evidences;
                            var historyData = [getEvidenceResult.historyData];
                            DatabaseConnector.updateEvidenceBatch(evidences, historyData, function(result) {
                                $scope.modelUpdate($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                                ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid.getText());
                            }, function(error) {
                                console.log('fail to update to database', error);
                                dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                                ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid.getText());
                            });
                        } else {
                            $scope.modelUpdate($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                        }
                    }
                };
                function rejectItem(arr) {
                    _.each(arr, function(item) {
                        if(mainUtils.needReview(item.uuid)) {
                            if(item.obj && item.reviewObj.has('lastReviewed')) {
                                item.obj.setText(item.reviewObj.get('lastReviewed'));
                            }
                            var tempTime = $scope.lastUpdateTime;
                            if(!tempTime) {
                                tempTime = new Date().getTime();
                            }
                            item.reviewObj.delete('lastReviewed');
                            // use the updateTime fetched from database to update reviewObj
                            item.reviewObj.set('updateTime', tempTime);
                            $rootScope.geneMetaData.get(item.uuid.getText()).set('review', false);
                            ReviewResource.rejected.push(item.uuid.getText());
                        }
                    });
                    if($scope.bothChanged) {
                        effectSection();
                    }
                }
                function effectSection() {
                    $scope.mutation.oncogenic_review.set('updateTime', $scope.oncogenicLastUpdateTime);
                    $scope.mutation.effect_review.set('updateTime', $scope.effectLastUpdateTime);
                }
                $scope.reject = function(event) {
                    if (event !== null) {
                        $scope.$parent.stopCollopse(event);
                    }
                    if (mainUtils.isProcessed('accept', $scope.uuid) || mainUtils.isProcessed('reject', $scope.uuid)) {
                        return;
                    }
                    var dlg = dialogs.confirm('Reminder', 'Are you sure you want to reject this change?');
                    dlg.result.then(function() {
                        var uuid; // uuid that is used as evidence identifier in the database
                        var items = [];
                        $scope.bothChanged = false;
                        switch ($scope.adjustedEvidenceType) {
                        case 'GENE_SUMMARY':
                            uuid = $scope.obj.summary_uuid;
                            items = [{obj: $scope.obj.summary, reviewObj: $scope.obj.summary_review, uuid: $scope.obj.summary_uuid}];
                            break;
                        case 'GENE_BACKGROUND':
                            uuid = $scope.obj.background_uuid;
                            items = [{obj: $scope.obj.background, reviewObj: $scope.obj.background_review, uuid: $scope.obj.background_uuid}];
                            break;
                        case 'GENE_TYPE':
                            // since gene type is not stored in evidence table, there is no need to fetch it.
                            uuid = '';
                            items = [{reviewObj: $scope.obj.type_review, uuid: $scope.obj.type_uuid}];
                            break;
                        case 'ONCOGENIC':
                            var oncogenicUUID = $scope.mutation.oncogenic_uuid.getText();
                            var effectUUID = $scope.mutation.effect_uuid.getText();
                            $scope.oncogenicLastUpdateTime = new Date().getTime();
                            $scope.effectLastUpdateTime = new Date().getTime();
                            items = [{obj: $scope.mutation.oncogenic, reviewObj: $scope.mutation.oncogenic_review, uuid: $scope.mutation.oncogenic_uuid},
                                {obj: $scope.mutation.effect.value, reviewObj: $scope.mutation.effect_review, uuid: $scope.mutation.effect_uuid},
                                {obj: $scope.mutation.description, reviewObj: $scope.mutation.description_review, uuid: $scope.mutation.description_uuid}];

                            var oncogenicChange = mainUtils.needReview($scope.mutation.oncogenic_uuid);
                            var effectChange = mainUtils.needReview($scope.mutation.effect_uuid) || mainUtils.needReview($scope.mutation.description_uuid);
                            if(oncogenicChange && effectChange) {
                                $scope.bothChanged = true;
                                if ($rootScope.isDesiredGene) {
                                    DatabaseConnector.getEvidencesByUUIDs([oncogenicUUID, effectUUID], function(result) {
                                        if (result && result.status) {
                                            var resultJSON = JSON.parse(result.status);
                                            if (_.isArray(resultJSON)) {
                                                _.each(resultJSON, function(eviFromDB) {
                                                    if(eviFromDB) {
                                                        setUpdateTimeEffectSection(eviFromDB, oncogenicUUID, effectUUID);
                                                    }
                                                });
                                            }
                                        }
                                        rejectItem(items);
                                    }, function(error) {
                                        console.log('Failed to fetch evidence based on uuid', error);
                                    });
                                } else {
                                    rejectItem(items);
                                }
                            } else {
                                if(oncogenicChange) uuid = $scope.mutation.oncogenic_uuid;
                                if(effectChange) uuid = $scope.mutation.effect_uuid;
                            }
                            break;
                        case 'TUMOR_TYPE_SUMMARY':
                            uuid = $scope.tumor.summary_uuid;
                            items = [{obj: $scope.tumor.summary, reviewObj: $scope.tumor.summary_review, uuid: $scope.tumor.summary_uuid}];
                            break;
                        case 'PREVALENCE':
                            uuid = $scope.tumor.prevalence_uuid;
                            items = [{obj: $scope.tumor.prevalence, reviewObj: $scope.tumor.prevalence_review, uuid: $scope.tumor.prevalence_uuid}];
                            break;
                        case 'PROGNOSTIC_IMPLICATION':
                            uuid = $scope.tumor.prognostic_uuid;
                            items = [{obj: $scope.tumor.prognostic.description, reviewObj: $scope.tumor.prognostic.description_review, uuid: $scope.tumor.prognostic.description_uuid},
                                {obj: $scope.tumor.prognostic.level, reviewObj: $scope.tumor.prognostic.level_review, uuid: $scope.tumor.prognostic.level_uuid}];
                            break;
                        case 'DIAGNOSTIC_IMPLICATION':
                            uuid = $scope.tumor.diagnostic_uuid;
                            items = [{obj: $scope.tumor.diagnostic.description, reviewObj: $scope.tumor.diagnostic.description_review, uuid: $scope.tumor.diagnostic.description_uuid},
                                {obj: $scope.tumor.diagnostic.level, reviewObj: $scope.tumor.diagnostic.level_review, uuid: $scope.tumor.diagnostic.level_uuid}];
                            break;
                        case 'NCCN_GUIDELINES':
                            uuid = $scope.tumor.nccn_uuid;
                            items = [{obj: $scope.tumor.nccn.therapy, reviewObj: $scope.tumor.nccn.therapy_review, uuid: $scope.tumor.nccn.therapy_uuid},
                                {obj: $scope.tumor.nccn.disease, reviewObj: $scope.tumor.nccn.disease_review, uuid: $scope.tumor.nccn.disease_uuid},
                                {obj: $scope.tumor.nccn.version, reviewObj: $scope.tumor.nccn.version_review, uuid: $scope.tumor.nccn.version_uuid},
                                {obj: $scope.tumor.nccn.description, reviewObj: $scope.tumor.nccn.description_review, uuid: $scope.tumor.nccn.description_uuid}];
                            break;
                        case 'Standard implications for sensitivity to therapy':
                        case 'Standard implications for resistance to therapy':
                        case 'Investigational implications for sensitivity to therapy':
                        case 'Investigational implications for resistance to therapy':
                            if (!$scope.treatment) {
                                uuid = $scope.therapyCategory.description_uuid;
                                items = [{obj: $scope.therapyCategory.description, reviewObj: $scope.therapyCategory.description_review, uuid: $scope.therapyCategory.description_uuid}];
                            } else {
                                uuid = $scope.treatment.name_uuid;
                                items = [{obj: $scope.treatment.name, reviewObj: $scope.treatment.name_review, uuid: $scope.treatment.name_uuid},
                                    {obj: $scope.treatment.indication, reviewObj: $scope.treatment.indication_review, uuid: $scope.treatment.indication_uuid},
                                    {obj: $scope.treatment.description, reviewObj: $scope.treatment.description_review, uuid: $scope.treatment.description_uuid}];
                            }
                            break;
                        case 'CLINICAL_TRIAL':
                            uuid = $scope.tumor.trials_uuid;
                            items = [{reviewObj: $scope.tumor.trials_review, uuid: $scope.tumor.trials_uuid}];
                            break;
                        case 'MUTATION_NAME_CHANGE':
                            uuid = '';
                            items = [{obj: $scope.mutation.name, reviewObj: $scope.mutation.name_review, uuid: $scope.mutation.name_uuid}];
                            break;
                        case 'TUMOR_NAME_CHANGE':
                            uuid = '';
                            items = [{reviewObj: $scope.tumor.cancerTypes_review, uuid: $scope.tumor.name_uuid, tumorNameReview: $scope.tumor.name_review}];
                            break;
                        case 'TREATMENT_NAME_CHANGE':
                            uuid = '';
                            items = [{obj: $scope.treatment.name, reviewObj: $scope.treatment.name_review, uuid: $scope.treatment.name_uuid}];
                            break;
                        default:
                            break;
                        }
                        if (uuid) {
                            uuid = uuid.getText();
                            ReviewResource.rejected.push(uuid);
                        }
                        if ($rootScope.isDesiredGene && uuid) {
                            DatabaseConnector.getEvidencesByUUID(uuid, function(result) {
                                if (result && result.status) {
                                    var resultJSON = JSON.parse(result.status);
                                    if (_.isArray(resultJSON) && resultJSON.length > 0) {
                                        var eviFromDB = resultJSON[0];
                                        $scope.lastUpdateTime = eviFromDB.lastEdit;
                                    }
                                }
                                specialCases();
                                rejectItem(items);
                            }, function(error) {
                                console.log('Failed to fetch evidence based on uuid', error);
                            });
                        } else if(!$scope.bothChanged) {
                            specialCases();
                            rejectItem(items);
                        }
                    });
                };
                function setUpdateTimeEffectSection(eviFromDB, oncogenicUUID, effectUUID) {
                    if(eviFromDB.lastEdit) {
                        if(eviFromDB.uuid === oncogenicUUID) {
                            $scope.oncogenicLastUpdateTime = eviFromDB.lastEdit;
                        } else if(eviFromDB.uuid === effectUUID) {
                            $scope.effectLastUpdateTime = eviFromDB.lastEdit;
                        }
                    }
                }
                function specialCases() {
                    switch ($scope.adjustedEvidenceType) {
                    case 'GENE_TYPE':
                        $scope.obj.type.set('TSG', $scope.obj.type_review.get('lastReviewed').TSG);
                        $scope.obj.type.set('OCG', $scope.obj.type_review.get('lastReviewed').OCG);
                        break;
                    case 'Standard implications for sensitivity to therapy':
                    case 'Standard implications for resistance to therapy':
                    case 'Investigational implications for sensitivity to therapy':
                    case 'Investigational implications for resistance to therapy':
                        if ($scope.treatment) {
                            // handle level specifically because level and propagation share the same uuid and review object
                            var levelChanged =  mainUtils.isProcessed('precise', $scope.treatment.level_uuid);
                            if(levelChanged) {
                                var lastReviewedLevel = $scope.treatment.level_review.get('lastReviewed');
                                var lastReviewedPropagation = $scope.treatment.level_review.get('lastReviewedPropagation');
                                if(lastReviewedLevel !== null) {
                                    $scope.treatment.level.setText(lastReviewedLevel);
                                }
                                $scope.treatment.name_eStatus.set('propagation', lastReviewedPropagation);
                                $rootScope.geneMetaData.get($scope.treatment.level_uuid.getText()).set('review', false);
                                ReviewResource.rejected.push($scope.treatment.level_uuid.getText());
                                $scope.treatment.level_review.delete('lastReviewed');
                            }
                        }
                        break;
                    case 'CLINICAL_TRIAL':
                        $scope.tumor.trials.clear();
                        if ($scope.tumor.trials_review.has('lastReviewed')) {
                            $scope.tumor.trials.pushAll($scope.tumor.trials_review.get('lastReviewed'));
                        }
                        break;
                    case 'TUMOR_NAME_CHANGE':
                        var lastReviewed = $rootScope.model.createList();
                        _.each($scope.tumor.cancerTypes_review.get('lastReviewed'), function(ct) {
                            var cancerType = $rootScope.model.create(OncoKB.CancerType);
                            cancerType.cancerType.setText(ct.cancerType);
                            cancerType.subtype.setText(ct.subtype);
                            cancerType.oncoTreeCode.setText(ct.oncoTreeCode);
                            lastReviewed.push(cancerType);
                        });
                        $scope.tumor.cancerTypes = lastReviewed;
                        break;
                    default:
                        break;
                    }
                }
                $scope.confirmDelete = function(event, type, mutation, tumor, therapyCategory, treatment) {
                    $scope.confirmDeleteInGene({
                        event: event, type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };
                $scope.cancelDelete = function(event, type, mutation, tumor, therapyCategory, treatment) {
                    $scope.cancelDeleteInGene({
                        event: event, type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };
                $scope.acceptAdded = function(event, type, mutation, tumor, therapyCategory, treatment) {
                    $scope.acceptAddedInGene({
                        event: event, type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };
                $scope.rejectAdded = function(event, type, mutation, tumor, therapyCategory, treatment) {
                    $scope.rejectAddedInGene({
                        event: event, type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };
                $scope.modelUpdate = function(type, mutation, tumor, therapyCategory, treatment) {
                    $scope.modelUpdateInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };
            }
        };
    });
