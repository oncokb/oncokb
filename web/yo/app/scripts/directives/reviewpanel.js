'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:reviewpanel
 * @description
 * # reviewpanel
 * This directive is designed specifically for review mode, which cotains change info text, accept icon, reject icon and loading bar
 */
angular.module('oncokbApp')
    .directive('reviewPanel', function($rootScope, DatabaseConnector, dialogs, _, OncoKB, mainUtils, ReviewResource, numOfReviewItems) {
        return {
            templateUrl: 'views/reviewPanel.html',
            restrict: 'AE',
            scope: {
                data: '=',
                key: '=',
                path: '=',
                evidenceType: '=',  // evidence type
                mutation: '=', // mutation
                tumor: '=', // tumor
                therapyCategory: '=', // therapy category
                treatment: '=', // treatment
                obj: '=', // temporary object
                hugoSymbol: '=', // gene name
                confirmDeleteInGene: '&confirmDelete',
                cancelDeleteInGene: '&cancelDelete',
                getEvidenceInGene: '&getEvidence',
                updatePriorityInGene: '&updatePriority',
                modelUpdateInGene: '&modelUpdate',
                acceptAddedInGene: '&acceptAdded',
                rejectAddedInGene: '&rejectAdded',
                updateDrugMapInGene: '&updateDrugMap',
                getRefsInGene: '&getRefs',
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
                        // We only record new and old content for accepted 'update' and 'name'(name change) operations.
                        // For 'add' operation, there is only 'new' content. For 'delete' operation, there is only 'old' content.
                        switch($scope.panelType) {
                            case 'update':
                                 $scope.accept();
                                break;
                            case 'name':
                                $scope.accept();
                                break;
                            case 'delete':
                                $scope.confirmDelete($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment, $scope.updatedBy);

                                break;
                            case 'add':
                                $scope.acceptAdded($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment, $scope.updatedBy);
                                break;
                        }
                    } else if (type === 'reject') {
                        switch($scope.panelType) {
                            case 'update':
                                $scope.reject();
                                break;
                            case 'name':
                                $scope.reject($scope.path);
                                break;
                            case 'delete':
                                $scope.cancelDelete($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment, $scope.updatedBy);
                                break;
                            case 'add':
                                $scope.rejectAdded($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment, $scope.updatedBy);
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
                        $scope.adjustedEvidenceType = $scope.evidenceType;
                        if (isTreatmentType()) {
                            $scope.adjustedEvidenceType = 'treatment';
                        }
                    } else if (mainUtils.processedInReview('add', $scope.uuid)) {
                        $scope.panelType = 'add';
                        $scope.adjustedEvidenceType = $scope.evidenceType;
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
                    setUpdatedByAndTime();

                    if ($scope.updatedBy && $scope.panelExist()) {
                        numOfReviewItems.add($scope.updatedBy);
                    }
                };
                $scope.signatureCheck = function() {
                    setUpdatedByAndTime();

                    // If any decision hasn't been made yet, we display the panel signature which is a text describing what kind of change is made by who at what time
                    // on the other hand, if the evidence already got accepted or rejected, we hide the panel signature
                    if (!mainUtils.processedInReview('accept', $scope.uuid) && !mainUtils.processedInReview('reject', $scope.uuid)) {
                        return true;
                    } else {
                        return false;
                    }
                };
                function setUpdatedByAndTime () {
                    // Prepare values for panel signature text
                    if ($scope.uuid && ReviewResource.mostRecent[$scope.uuid]) {
                        // If there are mutiple items inside one section, we use the most recent one, which is calculated in prepareReviewItems() in gene.js and stored in ReviewResource.mostRecent
                        $scope.updatedBy = ReviewResource.mostRecent[$scope.uuid].updatedBy;
                        $scope.updateTime = ReviewResource.mostRecent[$scope.uuid].updateTime;
                    } else if ($scope.adjustedEvidenceType === 'TUMOR_NAME_CHANGE') {
                        // For tumor name change, the review info is stored in cancerTypes_review
                        $scope.updatedBy = $scope.tumor.cancerTypes_review.updatedBy;
                        $scope.updateTime = $scope.tumor.cancerTypes_review.updateTime;
                    } else {
                        $scope.updatedBy = $scope.reviewObj && $scope.reviewObj.updatedBy ? $scope.reviewObj.updatedBy : '';
                        $scope.updateTime = $scope.reviewObj && $scope.reviewObj.updateTime ? $scope.reviewObj.updateTime : '';
                    }
                    if (!$scope.updatedBy) {
                        $scope.updatedBy = '';
                    }
                    if (!$scope.updateTime) {
                        $scope.updateTime = '';
                    }
                }
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
                            hugoSymbol: $scope.obj.name,
                            oncogene: !(!$scope.obj.type.ocg),
                            tsg: !(!$scope.obj.type.tsg)
                        };
                        var newContent = $scope.obj.type.tsg + '  ' + $scope.obj.type.ocg;
                        var oldContent = mainUtils.getOldGeneType($scope.obj.type);
                        var historyData = [{
                            lastEditBy: ReviewResource.mostRecent[$scope.uuid].updatedBy,
                            new: newContent.trim(),
                            old: oldContent.trim(),
                            location: 'Gene Type',
                            operation: 'update',
                            uuids: $scope.uuid
                        }];
                        historyData.hugoSymbol = $scope.obj.name;
                        ReviewResource.loading.push($scope.uuid);
                        DatabaseConnector.updateGeneType($scope.obj.name, params, historyData, function(result) {
                            $scope.modelUpdate($scope.adjustedEvidenceType);
                            ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid);
                            numOfReviewItems.minus($scope.updatedBy);
                        }, function(error) {
                            console.log('fail to update to database', error);
                            dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                            ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid);
                        });
                    } else {
                        ReviewResource.loading.push($scope.uuid);
                        var getEvidenceResult = $scope.getEvidence($scope.adjustedEvidenceType, $scope.mutation, $scope.tumor, $scope.therapyCategory, $scope.treatment);
                        var evidences = getEvidenceResult.evidences;
                        var historyData = [getEvidenceResult.historyData];
                        historyData.hugoSymbol = $scope.hugoSymbol;
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
                            numOfReviewItems.minus($scope.updatedBy);
                        }, function(error) {
                            console.log('fail to update to database', error);
                            dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                            ReviewResource.loading = _.without(ReviewResource.loading, $scope.uuid);
                        });
                    }
                };
                function rejectItems(rejectionItems) {
                    _.each(rejectionItems, function(item) {
                        if ($rootScope.reviewMeta[item.uuid]) {
                            mainUtils.deleteUUID(item.uuid);
                            ReviewResource.rejected.push(item.uuid);
                            if (item.obj && item.key && item.obj[item.key + '_review'] && !_.isUndefined(item.obj[item.key + '_review'].lastReviewed)) {
                                item.obj[item.key] = item.obj[item.key + '_review'].lastReviewed;
                                delete item.obj[item.key + '_review'].lastReviewed;
                            }
                        }
                    });
                }
                $scope.reject = function(mapPath) {
                    var dlg = dialogs.confirm('Reminder', 'Are you sure you want to reject this change?');
                    dlg.result.then(function() {
                        numOfReviewItems.minus($scope.updatedBy);
                        ReviewResource.rejected.push($scope.uuid);
                        var rejectionItems = [];
                        switch ($scope.adjustedEvidenceType) {
                            case 'GENE_SUMMARY':
                                rejectionItems.push({uuid: $scope.obj.summary_uuid, key: 'summary', obj: $scope.obj});
                                break;
                            case 'GENE_BACKGROUND':
                                rejectionItems.push({uuid: $scope.obj.background_uuid, key: 'background', obj: $scope.obj});
                                break;
                            case 'GENE_TYPE':
                                _.each(['tsg', 'ocg'], function(key) {
                                    ReviewResource.rejected.push($scope.obj.type[key + '_uuid']);
                                    rejectionItems.push({uuid: $scope.obj.type[key + '_uuid'], key: key, obj: $scope.obj.type});
                                });
                                break;
                            case 'MUTATION_EFFECT':
                                var mutation = $scope.getRefs($scope.mutation).mutation;
                                _.each(['oncogenic', 'effect', 'description'], function(key) {
                                    rejectionItems.push({uuid: mutation.mutation_effect[key+'_uuid'], key: key, obj: mutation.mutation_effect});
                                });
                                break;
                            case 'TUMOR_TYPE_SUMMARY':
                                var tumor =  $scope.getRefs($scope.mutation, $scope.tumor).tumor;
                                rejectionItems.push({uuid: tumor.summary_uuid, key: 'summary', obj: tumor});
                                break;
                            case 'PROGNOSTIC_IMPLICATION':
                                var tumor = $scope.getRefs($scope.mutation, $scope.tumor).tumor;
                                _.each(['level', 'description'], function(key) {
                                    rejectionItems.push({uuid: tumor.prognostic[key + '_uuid'], key: key, obj: tumor.prognostic});
                                });
                                break;
                            case 'DIAGNOSTIC_IMPLICATION':
                                var tumor = $scope.getRefs($scope.mutation, $scope.tumor).tumor;
                                _.each(['level', 'description'], function(key) {
                                    rejectionItems.push({uuid: tumor.diagnostic[key + '_uuid'], key: key, obj: tumor.diagnostic});
                                });
                                break;
                            case 'Standard implications for sensitivity to therapy':
                            case 'Standard implications for resistance to therapy':
                            case 'Investigational implications for sensitivity to therapy':
                            case 'Investigational implications for resistance to therapy':
                                var treatment = $scope.getRefs($scope.mutation, $scope.tumor, $scope.ti, $scope.treatment).treatment;
                                _.each(['name', 'level', 'propagation', 'indication', 'description'], function(key) {
                                    rejectionItems.push({uuid: treatment[key + '_uuid'], key: key, obj: treatment});
                                });
                                break;
                            case 'MUTATION_NAME_CHANGE':
                                var mutation = $scope.getRefs($scope.mutation).mutation;
                                rejectionItems.push({uuid: mutation.name_uuid, key: 'name', obj: mutation});
                                break;
                            case 'TUMOR_NAME_CHANGE':
                                var tumor = $scope.getRefs($scope.mutation, $scope.tumor, $scope.ti, $scope.treatment).tumor;
                                rejectionItems.push({uuid: tumor.cancerTypes_uuid, key: 'cancerTypes', obj: tumor});
                                break;
                            case 'TREATMENT_NAME_CHANGE':
                                var treatment = $scope.getRefs($scope.mutation, $scope.tumor, $scope.ti, $scope.treatment).treatment;
                                $scope.updateDrugMap('reject', 'name', 'treatment', $scope.mutation, $scope.tumor, $scope.treatment, treatment.name_review.lastReviewed);
                                rejectionItems.push({uuid: treatment.name_uuid, key: 'name', obj: treatment});


                                break;
                            default:
                                break;
                        }
                        rejectItems(rejectionItems);
                    });
                };
                $scope.confirmDelete = function(type, mutation, tumor, therapyCategory, treatment, updatedBy) {
                    $scope.confirmDeleteInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment, updatedBy: updatedBy
                    });
                };
                $scope.cancelDelete = function(type, mutation, tumor, therapyCategory, treatment, updatedBy) {
                    $scope.cancelDeleteInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment, updatedBy: updatedBy
                    });
                };
                $scope.acceptAdded = function(type, mutation, tumor, therapyCategory, treatment, updatedBy) {
                    $scope.acceptAddedInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment, updatedBy: updatedBy
                    });
                };
                $scope.rejectAdded = function(type, mutation, tumor, therapyCategory, treatment, updatedBy) {
                    $scope.rejectAddedInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment, updatedBy: updatedBy
                    });
                };
                $scope.modelUpdate = function(type, mutation, tumor, therapyCategory, treatment) {
                    $scope.modelUpdateInGene({
                        type: type, mutation: mutation, tumor: tumor, therapyCategory: therapyCategory, treatment: treatment
                    });
                };
                $scope.getRefs = function(mutation, tumor, therapyCategory, treatment) {
                    return $scope.getRefsInGene({
                        mutationCopy: mutation, tumorCopy: tumor, tiCopy: therapyCategory, treatmentCopy: treatment
                    });
                };
                $scope.updateDrugMap = function (decision, type, dataType, mutation, tumor, treatment, mapPath, oldContent) {
                    return $scope.updateDrugMapInGene({
                        decision: decision, type: type, dataType: dataType, mutation: mutation, tumor: tumor, treatment: treatment, mapPath: mapPath, oldContent: oldContent
                    });
                }
                $scope.inReviewMode = function () {
                    return $rootScope.reviewMode;
                };
            }
        };
    });
