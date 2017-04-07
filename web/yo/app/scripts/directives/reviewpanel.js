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
                rs: '=', // review object
                tp: '=',  // evidence type
                mt: '=', // mutation
                tm: '=', // tumor
                ti: '=', // therapy category
                tt: '=', // treatment
                rt: '=', // review panel type
                obj: '=', // temporary object
                confirmDeleteInGene: '&confirmDelete',
                cancelDeleteInGene: '&cancelDelete',
                getEvidenceInGene: '&getEvidence',
                modelUpdateInGene: '&modelUpdate'
            },
            replace: true,
            link: function postLink(scope, element) {
                scope.reviewMode = $rootScope.reviewMode;
                $rootScope.$watch('reviewMode', function(n, o) {
                    if(n !== o) {
                        scope.reviewMode = n;
                    }
                });
            },
            controller: function($scope) {
                $scope.signatureCheck = function() {
                    if ($scope.mt && $scope.mt.name_review.get('removed') || $scope.tm && $scope.tm.name_review.get('removed') || $scope.tt && $scope.tt.name_review.get('removed')) {
                        return false;
                    } else if ($scope.rs.get('action') || $scope.loading || $scope.rs.get('rollback')) {
                        return false;
                    }
                    return true;
                };
                $scope.iconClass = function(type) {
                    if (!$scope.rs.get('action')) {
                        if (type === 'accept') {
                            return 'fa-comments-red';
                        }
                        if (type === 'reject') {
                            return 'fa-comments-grey';
                        }
                    } else if (type === 'accept' && $scope.rs.get('action') === 'accepted' || type === 'reject' && $scope.rs.get('action') === 'rejected') {
                        return 'reviewed';
                    }
                };
                $scope.iconExist = function(type) {
                    if ($scope.mt && $scope.mt.name_review.get('removed') || $scope.tm && $scope.tm.name_review.get('removed') || $scope.tt && $scope.tt.name_review.get('removed')) {
                        return false;
                    }
                    if (type === 'accept') {
                        return !$scope.loading && $scope.rs.get('action') !== 'rejected' && !$scope.rs.get('rollback') && !$scope.removedItem;
                    } else if (type === 'reject') {
                        return !$scope.loading && $scope.rs.get('action') !== 'accepted' && !$scope.rs.get('rollback') && !$scope.removedItem;
                    } else if (type === 'loading') {
                        return $scope.loading;
                    }
                };
                $scope.getEvidence = function(type, mutation, tumor, TI, treatment) {
                    return $scope.getEvidenceInGene({
                        type: type, mutation: mutation, tumor: tumor, TI: TI, treatment: treatment
                    });
                };

                $scope.accept = function(event) {
                    if (event !== null) {
                        $scope.$parent.stopCollopse(event);
                    }
                    if ($scope.rs.get('action')) {
                        return;
                    }
                    var evidenceResult = $scope.getEvidence($scope.tp, $scope.mt, $scope.tm, $scope.ti, $scope.tt);
                    if ($scope.tp === 'GENE_TYPE') {
                        var params = {
                            hugoSymbol: $scope.obj.name.getText(),
                            oncogene: $scope.obj.type.get('OCG').trim().length > 0,
                            tsg: $scope.obj.type.get('TSG').trim().length > 0
                        };
                        if ($rootScope.isDesiredGene) {
                            $scope.loading = true;
                            DatabaseConnector.updateGeneType($scope.obj.name.getText(), params, function(result) {
                                $scope.modelUpdate($scope.tp, $scope.mt, $scope.tm, $scope.ti, $scope.tt);
                                $scope.loading = false;
                                $scope.rs.set('action', 'accepted');
                            }, function(error) {
                                console.log('fail to update to database', error);
                                dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                                $scope.loading = false;
                            });
                        } else {
                            $scope.modelUpdate($scope.tp, $scope.mt, $scope.tm, $scope.ti, $scope.tt);
                            $scope.rs.set('action', 'accepted');
                        }
                    } else {
                        if($rootScope.isDesiredGene) {
                            $scope.loading = true;
                            DatabaseConnector.updateEvidenceBatch(evidenceResult, function(result) {
                                $scope.modelUpdate($scope.tp, $scope.mt, $scope.tm, $scope.ti, $scope.tt);
                                $scope.loading = false;
                                $scope.rs.set('action', 'accepted');
                            }, function(error) {
                                console.log('fail to update to database', error);
                                dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                            });
                        } else {
                            $scope.modelUpdate($scope.tp, $scope.mt, $scope.tm, $scope.ti, $scope.tt);
                            $scope.rs.set('action', 'accepted');
                        }
                    }
                };
                function rejectItem(arr) {
                    _.each(arr, function(item) {
                        if($rootScope.geneMetaData.get(item.uuid.getText()) && $rootScope.geneMetaData.get(item.uuid.getText()).get('review')) {
                            if(item.obj) {
                                item.obj.setText(item.reviewObj.get('lastReviewed'));
                            }
                            var tempTime = $scope.lastUpdateTime;
                            if(!tempTime) {
                                tempTime = new Date().toLocaleString();
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
                    if($scope.rs) {
                        $scope.rs.set('action', 'rejected');
                    }
                    if($scope.bothChanged) {
                        effectSection();
                        $scope.bothChanged = false;
                    }
                }
                function effectSection() {
                    $scope.mt.oncogenic_review.set('updateTime', $scope.oncogenicLastUpdateTime);
                    $scope.mt.effect_review.set('updateTime', $scope.effectLastUpdateTime);
                }
                $scope.reject = function(event) {
                    if (event !== null) {
                        $scope.$parent.stopCollopse(event);
                    }
                    if ($scope.rs.get('action')) {
                        return;
                    }
                    var dlg = dialogs.confirm('Reminder', 'Are you sure you want to reject this change?');
                    dlg.result.then(function() {
                        var uuid;
                        var items = [];
                        switch ($scope.tp) {
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
                            var oncogenicUUID = $scope.mt.oncogenic_uuid.getText();
                            var effectUUID = $scope.mt.effect_uuid.getText();
                            $scope.oncogenicLastUpdateTime = new Date().toLocaleString();
                            $scope.effectLastUpdateTime = new Date().toLocaleString();
                            items = [{obj: $scope.mt.oncogenic, reviewObj: $scope.mt.oncogenic_review, uuid: $scope.mt.oncogenic_uuid},
                                {obj: $scope.mt.effect.value, reviewObj: $scope.mt.effect_review, uuid: $scope.mt.effect_uuid},
                                {obj: $scope.mt.description, reviewObj: $scope.mt.description_review, uuid: $scope.mt.description_uuid}];

                            var oncogenicChange = mainUtils.needReview($scope.mt.oncogenic_uuid);
                            var effectChange = mainUtils.needReview($scope.mt.effect_uuid) || mainUtils.needReview($scope.mt.description_uuid);
                            if(oncogenicChange && effectChange) {
                                $scope.bothChanged = true;
                                DatabaseConnector.getEvidencesByUUIDs([oncogenicUUID, effectUUID], function(result) {
                                    var resultJSON = JSON.parse(result.status);
                                    if (_.isArray(resultJSON)) {
                                        if(resultJSON[0]) {
                                            setUpdateTimeEffectSection(resultJSON[0], oncogenicUUID, effectUUID);
                                        }
                                        if(resultJSON[1]) {
                                            setUpdateTimeEffectSection(resultJSON[1], oncogenicUUID, effectUUID);
                                        }
                                    }
                                    specialCases();
                                    rejectItem(items);
                                }, function(error) {
                                    console.log('Failed to fetch evidence based on uuid', error);
                                });
                            } else {
                                if(oncogenicChange) uuid = oncogenicUUID;
                                if(effectChange) uuid = effectUUID;
                            }
                            break;
                        case 'TUMOR_TYPE_SUMMARY':
                            uuid = $scope.tm.summary_uuid;
                            items = [{obj: $scope.tm.summary, reviewObj: $scope.tm.summary_review, uuid: $scope.tm.summary_uuid}];
                            break;
                        case 'PREVALENCE':
                            uuid = $scope.tm.prevalence_uuid;
                            items = [{obj: $scope.tm.prevalence, reviewObj: $scope.tm.prevalence_review, uuid: $scope.tm.prevalence_uuid}];
                            break;
                        case 'PROGNOSTIC_IMPLICATION':
                            uuid = $scope.tm.progImp_uuid;
                            items = [{obj: $scope.tm.progImp, reviewObj: $scope.tm.progImp_review, uuid: $scope.tm.progImp_uuid}];
                            break;
                        case 'NCCN_GUIDELINES':
                            uuid = $scope.tm.nccn_uuid;
                            items = [{obj: $scope.tm.nccn.therapy, reviewObj: $scope.tm.nccn.therapy_review, uuid: $scope.tm.nccn.therapy_uuid},
                                {obj: $scope.tm.nccn.disease, reviewObj: $scope.tm.nccn.disease_review, uuid: $scope.tm.nccn.disease_uuid},
                                {obj: $scope.tm.nccn.version, reviewObj: $scope.tm.nccn.version_review, uuid: $scope.tm.nccn.version_uuid},
                                {obj: $scope.tm.nccn.description, reviewObj: $scope.tm.nccn.description_review, uuid: $scope.tm.nccn.description_uuid}];
                            break;
                        case 'Standard implications for sensitivity to therapy':
                        case 'Standard implications for resistance to therapy':
                        case 'Investigational implications for sensitivity to therapy':
                        case 'Investigational implications for resistance to therapy':
                            if (!$scope.tt) {
                                uuid = $scope.ti.description_uuid;
                                items = [{obj: $scope.ti.description, reviewObj: $scope.ti.description_review, uuid: $scope.ti.description_uuid}];
                            } else {
                                uuid = $scope.tt.name_uuid;
                                items = [{obj: $scope.tt.name, reviewObj: $scope.tt.name_review, uuid: $scope.tt.name_uuid},
                                    {obj: $scope.tt.indication, reviewObj: $scope.tt.indication_review, uuid: $scope.tt.indication_uuid},
                                    {obj: $scope.tt.description, reviewObj: $scope.tt.description_review, uuid: $scope.tt.description_uuid}];
                            }
                            break;
                        case 'CLINICAL_TRIAL':
                            uuid = $scope.tm.trials_uuid;
                            items = [{reviewObj: $scope.tm.trials_review, uuid: $scope.tm.trials_uuid}];
                            break;
                        case 'MUTATION_NAME_CHANGE':
                            uuid = '';
                            items = [{obj: $scope.mt.name, reviewObj: $scope.mt.name_review, uuid: $scope.mt.name_uuid}];
                            break;
                        case 'TUMOR_NAME_CHANGE':
                            uuid = '';
                            items = [{reviewObj: $scope.tm.cancerTypes_review, uuid: $scope.tm.name_uuid, tumorNameReview: $scope.tm.name_review}];
                            break;
                        case 'TREATMENT_NAME_CHANGE':
                            uuid = '';
                            items = [{obj: $scope.tt.name, reviewObj: $scope.tt.name_review, uuid: $scope.tt.name_uuid}];
                            break;
                        default:
                            break;
                        }
                        if ($rootScope.isDesiredGene && uuid) {
                            uuid = uuid.getText();
                            DatabaseConnector.getEvidencesByUUID(uuid, function(result) {
                                var resultJSON = JSON.parse(result.status);
                                if (_.isArray(resultJSON) && resultJSON.length > 0) {
                                    var eviFromDB = resultJSON[0];
                                    $scope.lastUpdateTime = eviFromDB.lastEdit;
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
                    switch ($scope.tp) {
                    case 'GENE_TYPE':
                        $scope.obj.type.set('TSG', $scope.obj.type_review.get('lastReviewed').TSG);
                        $scope.obj.type.set('OCG', $scope.obj.type_review.get('lastReviewed').OCG);
                        break;
                    case 'NCCN_GUIDELINES':
                        $scope.tm.nccn_review.clear();
                        $scope.tm.nccn_review.set('review', false);
                        break;
                    case 'Standard implications for sensitivity to therapy':
                    case 'Standard implications for resistance to therapy':
                    case 'Investigational implications for sensitivity to therapy':
                    case 'Investigational implications for resistance to therapy':
                        if ($scope.tt) {
                            // handle level specifically because level and propagation share the same uuid and review object
                            var levelChanged = $rootScope.geneMetaData.get($scope.tt.level_uuid.getText()) && $rootScope.geneMetaData.get($scope.tt.level_uuid.getText()).get('review');
                            if(levelChanged) {
                                var lastReviewedLevel = $scope.tt.level_review.get('lastReviewed');
                                var lastReviewedPropagation = $scope.tt.level_review.get('lastReviewedPropagation');
                                if(lastReviewedLevel !== null) {
                                    $scope.tt.level.setText(lastReviewedLevel);
                                }
                                if(lastReviewedPropagation !== null) {
                                    $scope.tt.name_eStatus.set('propagation', lastReviewedPropagation);
                                }
                                $scope.tt.level_review.clear();
                                $scope.tt.level_review.set('review', false);
                                $rootScope.geneMetaData.get($scope.tt.level_uuid.getText()).set('review', false);
                            }
                        }
                        break;
                    case 'CLINICAL_TRIAL':
                        $scope.tm.trials.clear();
                        $scope.tm.trials.pushAll($scope.tm.trials_review.get('lastReviewed'));
                        break;
                    case 'TUMOR_NAME_CHANGE':
                        var lastReviewed = $rootScope.model.createList();
                        _.each($scope.tm.cancerTypes_review.get('lastReviewed'), function(ct) {
                            var cancerType = $rootScope.model.create(OncoKB.CancerType);
                            cancerType.cancerType.setText(ct.cancerType);
                            cancerType.subtype.setText(ct.subtype);
                            cancerType.cancerType_eStatus.set('obsolete', 'false');
                            cancerType.subtype_eStatus.set('obsolete', 'false');
                            cancerType.oncoTreeCode_eStatus.set('obsolete', 'false');
                            lastReviewed.push(cancerType);
                        });
                        $scope.tm.cancerTypes = lastReviewed;
                        break;
                    default:
                        break;
                    }
                }
                $scope.confirmDelete = function(event, type, mutation, tumor, ti, treatment) {
                    $scope.confirmDeleteInGene({
                        event: event, type: type, mutation: mutation, tumor: tumor, ti: ti, treatment: treatment
                    });
                };
                $scope.cancelDelete = function(event, type, mutation, tumor, ti, treatment) {
                    $scope.cancelDeleteInGene({
                        event: event, type: type, mutation: mutation, tumor: tumor, ti: ti, treatment: treatment
                    });
                };
                $scope.modelUpdate = function(type, mutation, tumor, ti, treatment) {
                    $scope.modelUpdateInGene({
                        type: type, mutation: mutation, tumor: tumor, ti: ti, treatment: treatment
                    });
                };
            }
        };
    });
