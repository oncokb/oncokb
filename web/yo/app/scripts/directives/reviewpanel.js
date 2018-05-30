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
                data: '=',
                key: '=',
                evidenceType: '=',  // evidence type
                mutation: '=', // mutation
                tumor: '=', // tumor
                therapyCategory: '=', // therapy category
                treatment: '=', // treatment
                obj: '=', // temporary object
                confirmDeleteInGene: '&confirmDelete',
                cancelDeleteInGene: '&cancelDelete',
                getEvidenceInGene: '&getEvidence',
                updatePriorityInGene: '&updatePriority',
                modelUpdateInGene: '&modelUpdate',
                acceptAddedInGene: '&acceptAdded',
                rejectAddedInGene: '&rejectAdded',
                uuid: '=',
                reviewObj: '='
            },
            link: function(scope, element, attrs) {
            },
            replace: true,
            controller: function($scope) {
                $scope.operationsName = {'update': 'Updated', 'name': 'Name Changed', 'add': 'Added', 'delete': 'Deleted'};
                $scope.adjustedEvidenceType = $scope.evidenceType;
                $scope.panelType = '';
                $scope.processDecision = function(type) {
                    // Do nothing if the change has already been accepted or rejected
                    if (mainUtils.processedInReview('accept', $scope.uuid) || mainUtils.processedInReview('reject', $scope.uuid)) {
                        return;
                    }
                    if (type === 'accept') {
                        switch($scope.panelType) {
                        case 'update':
                            $scope.accept();
                            break;
                        case 'name':
                            $scope.accept();
                            break;
                        case 'delete':
                            $scope.confirmDelete($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                            break;
                        case 'add':
                            $scope.acceptAdded($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                            break;
                        }
                    } else if (type === 'reject') {
                        switch($scope.panelType) {
                        case 'update':
                            $scope.reject();
                            break;
                        case 'name':
                            $scope.reject();
                            break;
                        case 'delete':
                            $scope.cancelDelete($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                            break;
                        case 'add':
                            $scope.rejectAdded($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                            break;
                        }
                    }

                };
                $scope.panelExist = function() {
                    if (mainUtils.processedInReview('inside', $scope.uuid) || mainUtils.processedInReview('rollback', $scope.uuid) || !$scope.panelType) {
                        return false;
                    } else {
                        return true;
                    }
                };
                function isTreatmentType() {
                    return $scope.therapyCategory && $scope.evidenceType === $scope.therapyCategory.name;
                }
                $scope.assignPanelType = function() {
                    // The panel type is assigned in the priority of remove, add, name change and update. We need to pay special attentions when adjusting the order, which is reflected in the following if else statements
                    if (mainUtils.processedInReview('remove', $scope.uuid)) {
                        $scope.panelType = 'delete';
                        if (isTreatmentType()) {
                            $scope.adjustedEvidenceType = 'treatment';
                        }
                    } else if (mainUtils.processedInReview('add', $scope.uuid)) {
                        $scope.panelType = 'add';
                        if (isTreatmentType()) {
                            $scope.adjustedEvidenceType = 'treatment';
                        }
                    } else if (mainUtils.processedInReview('name', $scope.uuid)) {
                        $scope.panelType = 'name';
                        if ($scope.evidenceType === 'mutation') {
                            $scope.adjustedEvidenceType = 'MUTATION_NAME_CHANGE';
                        } else if ($scope.evidenceType === 'tumor') {
                            $scope.adjustedEvidenceType = 'TUMOR_NAME_CHANGE';
                        } else if (isTreatmentType()) {
                            $scope.adjustedEvidenceType = 'TREATMENT_NAME_CHANGE';
                        }
                    } else if (mainUtils.processedInReview('update', $scope.uuid)) {
                        $scope.panelType = 'update';
                    } else {
                        $scope.panelType = '';
                    }
                };
                $scope.signatureCheck = function() {
                    // Prepare values for panel signature text
                    if ($scope.uuid && ReviewResource.mostRecent[$scope.uuid]) {
                        // If there are mutiple items inside one section, we use the most recent one, which is calculated in prepareReviewItems() in gene.js and stored in ReviewResource.mostRecent
                        $scope.updatedBy = ReviewResource.mostRecent[$scope.uuid].updatedBy;
                        $scope.updateTime = ReviewResource.mostRecent[$scope.uuid].updateTime;
                    } else if ($scope.adjustedEvidenceType === 'TUMOR_NAME_CHANGE') {
                        // For tumor name change, the review info is stored in cancerTypes_review
                        $scope.updatedBy = $scope.tumor.cancerTypes_review.get('updatedBy');
                        $scope.updateTime = $scope.tumor.cancerTypes_review.get('updateTime');
                    } else {
                        $scope.updatedBy = $scope.reviewObj.updatedBy;
                        $scope.updateTime = $scope.reviewObj.updateTime;
                    }
                    // If any decision hasn't been made yet, we display the panel signature which is a text describing what kind of change is made by who at what time
                    // on the other hand, if the evidence already got accepted or rejected, we hide the panel signature
                    if (!mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid)) {
                        return true;
                    } else {
                        return false;
                    }
                };
                $scope.iconClass = function(type) {
                    if (mainUtils.processedInReview('accept', $scope.uuid) || mainUtils.processedInReview('reject', $scope.uuid)) {
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
                        return !mainUtils.processedInReview('reject', $scope.uuid) && !mainUtils.processedInReview('loading', $scope.uuid);
                    case 'reject':
                        return !mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('loading', $scope.uuid);
                    case 'loading':
                        return mainUtils.processedInReview('loading', $scope.uuid);
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

                $scope.accept = function() {
                    if ($scope.adjustedEvidenceType === 'GENE_TYPE') {
                        var params = {
                            hugoSymbol: $scope.data.name,
                            oncogene: $scope.data.type.ocg.trim().length > 0,
                            tsg: $scope.data.type.tsg.trim().length > 0
                        };
                        var historyData = [{
                            lastEditBy: ReviewResource.mostRecent[$scope.uuid].updatedBy,
                            operationName: 'update',
                            uuids: $scope.uuid
                        }];
                        if ($rootScope.isDesiredGene) {
                            ReviewResource.loading.push($scope.uuid);
                            DatabaseConnector.updateGeneType($scope.data.name, params, historyData, function(result) {
                                $scope.modelUpdate($scope.adjustedEvidenceType);
                                ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid);
                            }, function(error) {
                                console.log('fail to update to database', error);
                                dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                                ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid);
                            });
                        } else {
                            $scope.modelUpdate($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                        }
                    } else {
                        if($rootScope.isDesiredGene) {
                            ReviewResource.loading.push($scope.uuid);
                            var getEvidenceResult = $scope.getEvidence($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                            var evidences = getEvidenceResult.evidences;
                            var historyData = [getEvidenceResult.historyData];
                            DatabaseConnector.updateEvidenceBatch(evidences, historyData, function(result) {
                                $scope.modelUpdate($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                                if ($scope.adjustedEvidenceType === 'TREATMENT_NAME_CHANGE' && _.isFunction($scope.updatePriorityInGene)) {
                                    $scope.updatePriorityInGene({
                                        treatments: $scope.therapyCategory.treatments
                                    }).then(function() {

                                    }, function() {

                                    }).finally(function() {
                                        ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid);
                                    });
                                } else {
                                    ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid);
                                }
                            }, function(error) {
                                console.log('fail to update to database', error);
                                dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                                ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid);
                            });
                        } else {
                            $scope.modelUpdate($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                        }
                    }
                };
                function setRejectedUUIDs(uuids) {
                    _.each(uuids, function() {
                        $rootScope.rejectedUUIDs[$scope.uuid] = true;
                    });
                }
                function rejectItem() {

                }
                $scope.reject = function() {
                    var dlg = dialogs.confirm('Reminder', 'Are you sure you want to reject this change?');
                    dlg.result.then(function() {
                        var uuid = $scope.uuid; // uuid that is used as evidence identifier in the database
                        var items = [];
                        var uuids = [];
                        $scope.bothChanged = false;
                        switch ($scope.adjustedEvidenceType) {
                        case 'GENE_SUMMARY':
                            uuids.push($scope.obj.summary_uuid);
                            break;
                        case 'GENE_BACKGROUND':
                            uuids.push($scope.obj.background_uuid);
                            break;
                        case 'GENE_TYPE':
                            items = ['ocg', 'tsg'];
                            break;
                        case 'MUTATION_EFFECT':
                            var oncogenicUUID = $scope.mutation.mutation_effect.oncogenic_uuid;
                            var effectUUID = $scope.mutation.mutation_effect.effect_uuid;
                            var descriptionUUID = $scope.mutation.mutation_effect.description_uuid;
                            $scope.oncogenicLastUpdateTime = new Date().getTime();
                            $scope.effectLastUpdateTime = new Date().getTime();

                            var oncogenicChange = $rootScope.geneMeta.review[oncogenicUUID];
                            var effectChange = $rootScope.geneMeta.review[effectUUID] || $rootScope.geneMeta.review[descriptionUUID];
                            if(oncogenicChange && effectChange) {
                                uuids = [oncogenicUUID, effectUUID, descriptionUUID];
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
                                if(oncogenicChange) uuids.push(oncogenicUUID);
                                if(effectChange) uuids.push(effectUUID);
                            }
                            break;
                        case 'TUMOR_TYPE_SUMMARY':
                            uuid = $scope.tumor.summary_uuid;
                            items = [{obj: $scope.tumor.summary, reviewObj: $scope.tumor.summary_review, uuid: $scope.tumor.summary_uuid}];
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
                        setRejectedUUIDs(uuids);
                        if ($rootScope.isDesiredGene && $scope.adjustedEvidenceType !== 'GENE_TYPE') {
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
                    // case 'GENE_TYPE':
                    //     $scope.obj.type.set('TSG', $scope.obj.type_review.get('lastReviewed').TSG);
                    //     $scope.obj.type.set('OCG', $scope.obj.type_review.get('lastReviewed').OCG);
                    //     break;
                    case 'Standard implications for sensitivity to therapy':
                    case 'Standard implications for resistance to therapy':
                    case 'Investigational implications for sensitivity to therapy':
                    case 'Investigational implications for resistance to therapy':
                        if ($scope.treatment) {
                            // handle level specifically because level and propagation share the same uuid and review object
                            var levelChanged =  mainUtils.processedInReview('precise', $scope.treatment.level_uuid);
                            if(levelChanged) {
                                var lastReviewedLevel = $scope.treatment.level_review.get('lastReviewed');
                                var lastReviewedPropagation = $scope.treatment.level_review.get('lastReviewedPropagation');
                                if(lastReviewedLevel !== null) {
                                    $scope.treatment.level.setText(lastReviewedLevel);
                                }
                                $scope.treatment.name_eStatus.set('propagation', lastReviewedPropagation);
                                $rootScope.geneMetaData.get($scope.treatment.level_uuid).set('review', false);
                                ReviewResource.rejected.push($scope.treatment.level_uuid);
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
                $scope.confirmDelete = function(type, mutation, tumor, therapyCategory, treatment) {
                    $scope.confirmDeleteInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };
                $scope.cancelDelete = function(type, mutation, tumor, therapyCategory, treatment) {
                    $scope.cancelDeleteInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };
                $scope.acceptAdded = function(type, mutation, tumor, therapyCategory, treatment) {
                    $scope.acceptAddedInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };
                $scope.rejectAdded = function(type, mutation, tumor, therapyCategory, treatment) {
                    $scope.rejectAddedInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };
                $scope.modelUpdate = function(type, mutation, tumor, therapyCategory, treatment) {
                    $scope.modelUpdateInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };
                $scope.inReviewMode = function () {
                    return ReviewResource.reviewMode;
                };
            }
        };
    });