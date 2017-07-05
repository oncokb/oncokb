'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:reviewpanel
 * @description
 * # reviewpanel
 * This directive is designed specifically for review mode, which cotains change info text, accept icon, reject icon and loading bar
 */
angular.module('oncokbApp')
    .directive('reviewPanel', function($rootScope, DatabaseConnector, dialogs, _, OncoKB, mainUtils) {
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
                obj: '=', // temporary object
                confirmDeleteInGene: '&confirmDelete',
                cancelDeleteInGene: '&cancelDelete',
                getEvidenceInGene: '&getEvidence',
                modelUpdateInGene: '&modelUpdate',
                acceptAddedInGene: '&acceptAdded',
                rejectAddedInGene: '&rejectAdded'
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
                        $scope.reviewObj.set('loading', true);
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
                    if ($scope.reviewObj.get('removed') !== true && $scope.reviewObj.get('removedItem') ||
                        $scope.reviewObj.get('added') !== true && $scope.reviewObj.get('addedItem')) {
                        return false;
                    }
                    var sectionResult = {
                        mutationDeleted: false,
                        tumorDeleted: false,
                        treatmentDeleted: false,
                        mutationAdded: false,
                        tumorAdded: false,
                        treatmentAdded: false
                    };
                    var result = {
                        delete: false,
                        add: false,
                        name: false,
                        update: false
                    };
                    if ($scope.mutation && ($scope.mutation.name_review.get('removed') || $scope.mutation.name_review.get('action') === 'DELETION_REJECTED')) {
                        sectionResult.mutationDeleted = true;
                    }
                    if (!sectionResult.mutationDeleted && $scope.mutation && ($scope.mutation.name_review.get('added') || $scope.mutation.name_review.get('action') === 'ADD_ACCEPTED')) {
                        sectionResult.mutationAdded = true;
                    }
                    if (!sectionResult.mutationDeleted && !sectionResult.mutationAdded
                        && $scope.tumor && ($scope.tumor.name_review.get('removed') || $scope.tumor.cancerTypes_review.get('action') === 'DELETION_REJECTED')) {
                        sectionResult.tumorDeleted = true;
                    }
                    if (!sectionResult.mutationDeleted && !sectionResult.mutationAdded
                        && !sectionResult.tumorDeleted && $scope.tumor && ($scope.tumor.name_review.get('added') || $scope.tumor.cancerTypes_review.get('action') === 'ADD_ACCEPTED')) {
                        sectionResult.tumorAdded = true;
                    }
                    if (!sectionResult.mutationDeleted && !sectionResult.mutationAdded && !sectionResult.tumorDeleted && !sectionResult.tumorAdded
                        && $scope.treatment && ($scope.treatment.name_review.get('removed') || $scope.treatment.name_review.get('action') === 'DELETION_REJECTED')) {
                        sectionResult.treatmentDeleted = true;
                    }
                    if (!sectionResult.mutationDeleted && !sectionResult.mutationAdded && !sectionResult.tumorDeleted && !sectionResult.tumorAdded
                        && !sectionResult.treatmentDeleted && $scope.treatment && ($scope.treatment.name_review.get('added') || $scope.treatment.name_review.get('action') === 'ADD_ACCEPTED')) {
                        sectionResult.treatmentAdded = true;
                    }
                    result.delete = $scope.evidenceType === 'mutation' ? sectionResult.mutationDeleted : ($scope.evidenceType === 'tumor' ? sectionResult.tumorDeleted : ($scope.evidenceType === 'treatment' ? sectionResult.treatmentDeleted : false));
                    if (result.delete) {
                        $scope.panelType = 'delete';
                    } else {
                        result.add = $scope.evidenceType === 'mutation' ? sectionResult.mutationAdded : ($scope.evidenceType === 'tumor' ? sectionResult.tumorAdded : ($scope.evidenceType === 'treatment' ? sectionResult.treatmentAdded : false));
                        if (result.add) {
                            $scope.panelType = 'add';
                        } else {
                            switch($scope.evidenceType) {
                            case 'mutation':
                                result.name = $scope.reviewObj.has('lastReviewed') || $scope.reviewObj.get('action') === 'NAME_ACCEPTED' || $scope.reviewObj.get('action') === 'NAME_REJECTED';
                                if (result.name) {
                                    $scope.adjustedEvidenceType = 'MUTATION_NAME_CHANGE';
                                }
                                break;
                            case 'tumor':
                                result.name = $scope.tumor.cancerTypes_review.has('lastReviewed') || $scope.reviewObj.get('action') === 'NAME_ACCEPTED' || $scope.reviewObj.get('action') === 'NAME_REJECTED';
                                if (result.name) {
                                    $scope.adjustedEvidenceType = 'TUMOR_NAME_CHANGE';
                                }
                                break;
                            case 'treatment':
                                result.name = $scope.reviewObj.get('specialCase') === 'TREATMENT_NAME_CHANGE' || $scope.reviewObj.get('action') === 'NAME_ACCEPTED' || $scope.reviewObj.get('action') === 'NAME_REJECTED';
                                if (result.name) {
                                    $scope.adjustedEvidenceType = 'TREATMENT_NAME_CHANGE';
                                }
                                break;
                            }
                            if (result.name) {
                                $scope.panelType = 'name';
                            } else if ($scope.evidenceType === 'treatment') {
                                $scope.panelType = 'update';
                                $scope.adjustedEvidenceType = $scope.therapyCategory.name.getText();
                            } else if (['mutation', 'tumor', 'MUTATION_NAME_CHANGE', 'TUMOR_NAME_CHANGE', 'TREATMENT_NAME_CHANGE'].indexOf($scope.evidenceType) === -1) {
                                $scope.panelType = 'update';
                            } else {
                                $scope.panelType = '';
                            }
                        }
                    }
                    if ($scope.panelType) {
                        return true;
                    } else {
                        return false;
                    }
                };
                $scope.signatureCheck = function() {
                    if ($scope.reviewObj.has('mostRecent')) {
                        $scope.updatedBy = $scope.reviewObj.get('mostRecent').by.getText();
                        $scope.updateTime = $scope.reviewObj.get('mostRecent').value.getText();
                    } else if ($scope.adjustedEvidenceType === 'tumor') {
                        $scope.updatedBy = $scope.tumor.name_review.get('updatedBy');
                        $scope.updateTime = $scope.tumor.name_review.get('updateTime');
                    } else {
                        $scope.updatedBy = $scope.reviewObj.get('updatedBy');
                        $scope.updateTime = $scope.reviewObj.get('updateTime');
                    }

                    if ($scope.reviewObj.get('action') || $scope.loading || $scope.reviewObj.get('loading') || $scope.reviewObj.get('rollback')) {
                        return false;
                    } else {
                        return true;
                    }
                };
                $scope.iconClass = function(type) {
                    // before any decision is made
                    if (!$scope.reviewObj.get('action')) {
                        if (type === 'accept') {
                            return 'fa-comments-red';
                        }
                        if (type === 'reject') {
                            return 'fa-comments-grey';
                        }
                    }
                    // after they accept or reject an evidence
                    if ($scope.panelType === 'update' || $scope.panelType === 'name') {
                        if (type === 'accept' && ($scope.reviewObj.get('action') === 'accepted' || $scope.reviewObj.get('action') === 'NAME_ACCEPTED')
                            || type === 'reject' && $scope.reviewObj.get('action') === 'rejected' || $scope.reviewObj.get('action') === 'NAME_REJECTED') return 'reviewed';
                    } else if ($scope.panelType === 'delete') {
                        if (type === 'reject' && $scope.reviewObj.get('action') === 'DELETION_REJECTED') return 'reviewed';
                    } else if ($scope.panelType === 'add') {
                        if (type === 'accept' && $scope.reviewObj.get('action') === 'ADD_ACCEPTED') return 'reviewed';
                    }
                };
                $scope.iconExist = function(type) {
                    if (type === 'loading') {
                        return $scope.loading || $scope.reviewObj.get('loading');
                    }
                    if ($scope.loading || $scope.reviewObj.get('loading')) {
                        return false;
                    }
                    if (type === 'accept') {
                        switch ($scope.panelType) {
                        case 'update':
                            return $scope.reviewObj.get('action') !== 'rejected' && !$scope.reviewObj.get('rollback');
                        case 'name':
                            return $scope.reviewObj.get('action') !== 'rejected' && $scope.reviewObj.get('action') !== 'NAME_REJECTED' && !$scope.reviewObj.get('rollback');
                        case 'delete':
                            return $scope.reviewObj.get('action') !== 'DELETION_REJECTED';
                        case 'add':
                            return true;
                        }
                    } else if (type === 'reject') {
                        switch ($scope.panelType) {
                        case 'update':
                            return $scope.reviewObj.get('action') !== 'accepted' && !$scope.reviewObj.get('rollback');
                        case 'name':
                            return $scope.reviewObj.get('action') !== 'accepted' && $scope.reviewObj.get('action') !== 'NAME_ACCEPTED' && !$scope.reviewObj.get('rollback');
                        case 'delete':
                            return true;
                        case 'add':
                            return $scope.reviewObj.get('action') !== 'ADD_ACCEPTED';
                        }
                    }
                };
                $scope.panelClass = function(type) {
                    if ($scope.panelType === 'update') {
                        return type === 'text' ? 'updateText' : ($scope.treatment ? 'panelMargin' : '');
                    } else if ($scope.panelType === 'name') {
                        return type === 'text' ? 'updateText' : ($scope.treatment || $scope.reviewObj.get('action') ? 'panelMargin' : '');
                    } else if ($scope.panelType === 'add') {
                        if (type === 'text') return 'updateText';
                        else if (type === 'panel') return 'panelMargin';
                    } if ($scope.panelType === 'delete') {
                        if (type === 'text') return 'sectionText';
                        else if (type === 'panel') return 'panelMargin';
                    }
                }
                $scope.getEvidence = function(type, mutation, tumor, therapyCategory, treatment) {
                    return $scope.getEvidenceInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };

                $scope.accept = function(event) {
                    if (event !== null) {
                        $scope.$parent.stopCollopse(event);
                    }
                    if ($scope.reviewObj.get('action')) {
                        return;
                    }
                    var evidenceResult = $scope.getEvidence($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                    if ($scope.adjustedEvidenceType === 'GENE_TYPE') {
                        var params = {
                            hugoSymbol: $scope.obj.name.getText(),
                            oncogene: $scope.obj.type.get('OCG').trim().length > 0,
                            tsg: $scope.obj.type.get('TSG').trim().length > 0
                        };
                        if ($rootScope.isDesiredGene) {
                            $scope.loading = true;
                            DatabaseConnector.updateGeneType($scope.obj.name.getText(), params, function(result) {
                                $scope.modelUpdate($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                                $scope.loading = false;
                            }, function(error) {
                                console.log('fail to update to database', error);
                                dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                                $scope.loading = false;
                            });
                        } else {
                            $scope.modelUpdate($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                        }
                    } else {
                        if($rootScope.isDesiredGene) {
                            $scope.loading = true;
                            DatabaseConnector.updateEvidenceBatch(evidenceResult, function(result) {
                                $scope.modelUpdate($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                                $scope.loading = false;
                            }, function(error) {
                                console.log('fail to update to database', error);
                                dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                            });
                        } else {
                            $scope.modelUpdate($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                        }
                    }
                };
                function rejectItem(arr) {
                    _.each(arr, function(item) {
                        if($rootScope.geneMetaData.get(item.uuid.getText()) && $rootScope.geneMetaData.get(item.uuid.getText()).get('review')) {
                            if(item.obj && item.reviewObj.has('lastReviewed')) {
                                item.obj.setText(item.reviewObj.get('lastReviewed'));
                            }
                            var tempTime = $scope.lastUpdateTime;
                            if(!tempTime) {
                                tempTime = new Date().getTime();
                            }
                            item.reviewObj.clear();
                            item.reviewObj.set('review', false);
                            item.reviewObj.set('updateTime', tempTime);
                            // This check is for the case of Mutation/Tumor/Treatment Name change. Since they share the same uuid with deletion.
                            // We need to make sure not set review to false in meta if it also been removed.
                            var currentReviewObj = item.tumorNameReview ? item.tumorNameReview : item.reviewObj;
                            if(!currentReviewObj.get('removed')) {
                                $rootScope.geneMetaData.get(item.uuid.getText()).set('review', false);
                            }
                        }
                    });
                    if($scope.reviewObj) {
                        if ($scope.adjustedEvidenceType === 'MUTATION_NAME_CHANGE' || $scope.adjustedEvidenceType === 'TUMOR_NAME_CHANGE' || $scope.adjustedEvidenceType === 'TREATMENT_NAME_CHANGE') {
                            $scope.reviewObj.set('action', 'NAME_REJECTED');
                        } else {
                            $scope.reviewObj.set('action', 'rejected');
                        }
                    }
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
                    if ($scope.reviewObj.get('action')) {
                        return;
                    }
                    var dlg = dialogs.confirm('Reminder', 'Are you sure you want to reject this change?');
                    dlg.result.then(function() {
                        var uuid;
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
                            uuid = $scope.tumor.progImp_uuid;
                            items = [{obj: $scope.tumor.progImp, reviewObj: $scope.tumor.progImp_review, uuid: $scope.tumor.progImp_uuid}];
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
                        if ($rootScope.isDesiredGene && uuid) {
                            uuid = uuid.getText();
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
                    case 'NCCN_GUIDELINES':
                        $scope.tumor.nccn_review.clear();
                        $scope.tumor.nccn_review.set('review', false);
                        break;
                    case 'Standard implications for sensitivity to therapy':
                    case 'Standard implications for resistance to therapy':
                    case 'Investigational implications for sensitivity to therapy':
                    case 'Investigational implications for resistance to therapy':
                        if ($scope.treatment) {
                            // handle level specifically because level and propagation share the same uuid and review object
                            var levelChanged = $rootScope.geneMetaData.get($scope.treatment.level_uuid.getText()) && $rootScope.geneMetaData.get($scope.treatment.level_uuid.getText()).get('review');
                            if(levelChanged) {
                                var lastReviewedLevel = $scope.treatment.level_review.get('lastReviewed');
                                var lastReviewedPropagation = $scope.treatment.level_review.get('lastReviewedPropagation');
                                if(lastReviewedLevel !== null) {
                                    $scope.treatment.level.setText(lastReviewedLevel);
                                }
                                $scope.treatment.name_eStatus.set('propagation', lastReviewedPropagation);
                                $scope.treatment.level_review.clear();
                                $scope.treatment.level_review.set('review', false);
                                $rootScope.geneMetaData.get($scope.treatment.level_uuid.getText()).set('review', false);
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
                            cancerType.cancerType_eStatus.set('obsolete', 'false');
                            cancerType.subtype_eStatus.set('obsolete', 'false');
                            cancerType.oncoTreeCode_eStatus.set('obsolete', 'false');
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
