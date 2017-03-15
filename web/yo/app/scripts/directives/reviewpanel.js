'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:eStatus
 * @description
 * # eStatus
 */
angular.module('oncokbApp')
    .directive('reviewPanel', function($rootScope, DatabaseConnector, dialogs) {
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
                obj: '=', // termporoary object
                confirmDeleteInGene: '&confirmDelete',
                cancelDeleteInGene: '&cancelDelete',
                getEvidenceInGene: '&getEvidence',
                modelUpdateInGene: '&modelUpdate'
            },
            replace: true,
            link: function postLink(scope, element) {
                scope.reviewMode = $rootScope.reviewMode;
                $rootScope.$watch('reviewMode', function(n, o) {
                    scope.reviewMode = n;
                });
            },
            controller: function($scope) {
                $scope.signatureCheck = function() {
                    if (!$scope.reviewMode) {
                        return false;
                    } else if ($scope.mt && $scope.mt.name_review.get('removed') || $scope.tm && $scope.tm.name_review.get('removed') || $scope.tt && $scope.tt.name_review.get('removed')) {
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
                    if(!$scope.reviewMode) {
                        return false;
                    }
                    if ($scope.mt && $scope.mt.name_review.get('removed') || $scope.tm && $scope.tm.name_review.get('removed') || $scope.tt && $scope.tt.name_review.get('removed')) {
                        return false;
                    }
                    if (type === 'accept') {
                        return !$scope.loading && $scope.rs.get('action') !== 'rejected' && !$scope.rs.get('rollback') && !$scope.removedItem;
                    } else if (type === 'reject') {
                        return !$scope.loading && $scope.rs.get('action') !== 'accepted' && !$scope.rs.get('rollback') && !$scope.removedItem;
                    } else if (type == 'loading') {
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
                    var dataUUID = evidenceResult[0];
                    var data = evidenceResult[1];
                    var extraDataUUID = evidenceResult[2];
                    var extraData = evidenceResult[3];

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
                    } else if (dataUUID.length > 0) {
                        if ($rootScope.isDesiredGene) {
                            $scope.loading = true;
                            DatabaseConnector.updateEvidence(dataUUID, data, function(result) {
                                if (extraDataUUID.length > 0) {
                                    DatabaseConnector.updateEvidence(extraDataUUID, extraData, function(result) {
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
                                    $scope.loading = false;
                                    $scope.rs.set('action', 'accepted');
                                }
                            }, function(error) {
                                console.log('fail to update to database', error);
                                dialogs.error('Error', 'Failed to update to database! Please contact the developer.');
                                $scope.loading = false;
                            });
                        } else {
                            $scope.modelUpdate($scope.tp, $scope.mt, $scope.tm, $scope.ti, $scope.tt);
                            $scope.rs.set('action', 'accepted');
                        }
                    }
                };
                function rejectItem(arr) {
                    _.each(arr, function(item) {
                        if($rootScope.reviewMeta.get(item.uuid.getText()) && $rootScope.reviewMeta.get(item.uuid.getText()).get('review')) {
                            if(item.obj) {
                                item.obj.setText(item.reviewObj.get('lastReviewed'));
                            }
                            item.reviewObj.clear();
                            item.reviewObj.set('review', false);
                            item.reviewObj.set('action', 'rejected');
                            $rootScope.reviewMeta.get(item.uuid.getText()).set('review', false);
                        }
                    });
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
                        switch ($scope.tp) {
                        case 'GENE_SUMMARY':
                            rejectItem([{obj: $scope.obj.summary, reviewObj: $scope.obj.summary_review, uuid: $scope.obj.summary_uuid}]);
                            break;
                        case 'GENE_BACKGROUND':
                            rejectItem([{obj: $scope.obj.background, reviewObj: $scope.obj.background_review, uuid: $scope.obj.background_uuid}]);
                            break;
                        case 'GENE_TYPE':
                            $scope.obj.type.set('TSG', $scope.obj.type_review.get('lastReviewed').TSG);
                            $scope.obj.type.set('OCG', $scope.obj.type_review.get('lastReviewed').OCG);
                            rejectItem([{reviewObj: $scope.obj.type_review, uuid: $scope.obj.type_uuid}]);
                            break;
                        case 'ONCOGENIC':
                            rejectItem([{obj: $scope.mt.oncogenic, reviewObj: $scope.mt.oncogenic_review, uuid: $scope.mt.oncogenic_uuid},
                                {obj: $scope.mt.summary, reviewObj: $scope.mt.summary_review, uuid: $scope.mt.summary_uuid},
                                {obj: $scope.mt.shortSummary, reviewObj: $scope.mt.shortSummary_review, uuid: $scope.mt.shortSummary_uuid}]);
                            break;
                        case 'MUTATION_EFFECT':
                            rejectItem([{obj: $scope.mt.effect.value, reviewObj: $scope.mt.effect_review, uuid: $scope.mt.effect_uuid},
                                {obj: $scope.mt.description, reviewObj: $scope.mt.description_review, uuid: $scope.mt.description_uuid},
                                {obj: $scope.mt.short, reviewObj: $scope.mt.short_review, uuid: $scope.mt.short_uuid}]);
                            break;
                        case 'TUMOR_TYPE_SUMMARY':
                            rejectItem([{obj: $scope.tm.summary, reviewObj: $scope.tm.summary_review, uuid: $scope.tm.summary_uuid}]);
                            break;
                        case 'PREVALENCE':
                            rejectItem([{obj: $scope.tm.prevalence, reviewObj: $scope.tm.prevalence_review, uuid: $scope.tm.prevalence_uuid},
                                {obj: $scope.tm.shortPrevalence, reviewObj: $scope.tm.shortPrevalence_review, uuid: $scope.tm.shortPrevalence_uuid}]);
                            break;
                        case 'PROGNOSTIC_IMPLICATION':
                            rejectItem([{obj: $scope.tm.progImp, reviewObj: $scope.tm.progImp_review, uuid: $scope.tm.progImp_uuid},
                                {obj: $scope.tm.shortProgImp, reviewObj: $scope.tm.shortProgImp_review, uuid: $scope.tm.shortProgImp_uuid}]);
                            break;
                        case 'NCCN_GUIDELINES':
                            rejectItem([{reviewObj: $scope.tm.nccn_review, uuid: $scope.tm.nccn_uuid},
                                {obj: $scope.tm.nccn.therapy, reviewObj: $scope.tm.nccn.therapy_review, uuid: $scope.tm.nccn.therapy_uuid},
                                {obj: $scope.tm.nccn.disease, reviewObj: $scope.tm.nccn.disease_review, uuid: $scope.tm.nccn.disease_uuid},
                                {obj: $scope.tm.nccn.version, reviewObj: $scope.tm.nccn.version_review, uuid: $scope.tm.nccn.version_uuid},
                                {obj: $scope.tm.nccn.description, reviewObj: $scope.tm.nccn.description_review, uuid: $scope.tm.nccn.description_uuid},
                                {obj: $scope.tm.nccn.short, reviewObj: $scope.tm.nccn.short_review, uuid: $scope.tm.nccn.short_uuid}]);
                            break;
                        case 'Standard implications for sensitivity to therapy':
                        case 'Standard implications for resistance to therapy':
                        case 'Investigational implications for sensitivity to therapy':
                        case 'Investigational implications for resistance to therapy':
                            if ($scope.tt === null) {
                                rejectItem([{obj: $scope.ti.description, reviewObj: $scope.ti.description_review, uuid: $scope.ti.description_uuid}]);
                            } else {
                                var lastReviewedPropagation = $scope.tt.level_review.get('lastReviewedPropagation');
                                rejectItem([{obj: $scope.tt.name, reviewObj: $scope.tt.name_review, uuid: $scope.tt.name_uuid},
                                    {obj: $scope.tt.level, reviewObj: $scope.tt.level_review, uuid: $scope.tt.level_uuid},
                                    {obj: $scope.tt.indication, reviewObj: $scope.tt.indication_review, uuid: $scope.tt.indication_uuid},
                                    {obj: $scope.tt.description, reviewObj: $scope.tt.description_review, uuid: $scope.tt.description_uuid},
                                    {obj: $scope.tt.short, reviewObj: $scope.tt.short_review, uuid: $scope.tt.short_uuid}]);
                                if(!lastReviewedPropagation) {
                                    $scope.tt.level_review.clear();
                                    $scope.tt.level_review.set('review', false);
                                    if ($rootScope.reviewMeta.get($scope.tt.level_uuid.getText())) {
                                        $rootScope.reviewMeta.get($scope.tt.level_uuid.getText()).set('review', false);
                                    }
                                    $scope.tt.name_eStatus.set('propagation', lastReviewedPropagation);
                                }
                            }
                            break;
                        case 'CLINICAL_TRIAL':
                            $scope.tm.trials.clear();
                            $scope.tm.trials.pushAll($scope.tm.trials_review.get('lastReviewed'));
                            rejectItem([{reviewObj: $scope.tm.trials_review, uuid: $scope.tm.trials_uuid}]);
                            break;
                        default:
                            break;
                        }
                        $scope.rs.set('action', 'rejected');
                    });
                };
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
