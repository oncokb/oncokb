/**
 * Created by jiaojiao on 4/24/17.
 */
'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:curationQueue
 * @description
 * # curationQueue
 */
angular.module('oncokbApp')
    .directive('curationQueue', function(DTColumnDefBuilder, DTOptionsBuilder, DatabaseConnector, $rootScope, $timeout, mainUtils, dialogs, _, $q, loadFiles, user) {
        return {
            templateUrl: 'views/curationQueue.html',
            restrict: 'E',
            scope: {
                location: '=',
                hugoSymbol: '=',
                specifyAnnotationInGene: '&specifyAnnotation'
            },
            replace: true,
            link: {
                pre: function preLink(scope) {
                    scope.me = $rootScope.me;
                    scope.data = {
                        allCurations: false,
                        curators: [],
                        modifiedCurator: {},
                        modifiedSubType: {},
                        sectionList: ['Mutation Effect', 'Prognostic implications', 'Standard sensitivity', 'Standard resistance', 'Investigational sensitivity', 'Investigational resistance'],
                        modifiedSection: '',
                        subTypes: [],
                        formExpanded: false,
                        editing: false,
                        hugoVariantMapping: {},
                        resendEmail: false,
                        queueItemInEditing: '',
                        invalidData: false,
                        hugoSymbols: [],
                        loading: true
                    };
                    scope.resendEmail = false;
                    scope.input = {
                        article: '',
                        link: '',
                        hugoSymbols: '',
                        variant: '',
                        mainType: '',
                        subType: '',
                        section: '',
                        curator: '',
                        dueDay: '',
                        comment: ''
                    };
                    scope.dtOptions = {
                        hasBootstrap: true,
                        paging: false,
                        scrollCollapse: true,
                        scrollY: 800,
                        scrollX: true,
                        aaSorting: [[0, 'asc']]
                    };
                    scope.dtColumns = [
                        DTColumnDefBuilder.newColumnDef(0),
                        DTColumnDefBuilder.newColumnDef(1),
                        DTColumnDefBuilder.newColumnDef(2),
                        DTColumnDefBuilder.newColumnDef(3),
                        DTColumnDefBuilder.newColumnDef(4),
                        null,null,null,
                        DTColumnDefBuilder.newColumnDef(8),
                        DTColumnDefBuilder.newColumnDef(9),
                        DTColumnDefBuilder.newColumnDef(10)
                    ];
                    scope.queue = [];
                    loadFiles.load(['queues', 'meta']).then(function(result) {
                        scope.data.hugoSymbols = _.without(_.keys($rootScope.metaData), 'collaborators');
                        if (scope.location === 'gene') {
                            scope.queue = scope.getQueuesByGene(scope.hugoSymbol);
                        } else if (scope.location === 'queues') {
                            _.each(_.keys($rootScope.firebaseQueues), function(key) {
                                scope.queue = scope.queue.concat($rootScope.firebaseQueues[key].queue);
                            });
                        }
                        scope.data.loading = false;
                        scope.secondTimeAutoNotify();
                    });
                    // The column difference in terms of curation queue location in queues page or gene page it that,
                    // gene page has an unique column 'Previously curated in', and queues page has an unique column 'Gene'
                    if (scope.location === 'gene') {
                        scope.dtColumns[5] = DTColumnDefBuilder.newColumnDef(5).withOption('sType', 'date');
                        scope.dtColumns[6] = DTColumnDefBuilder.newColumnDef(6).withOption('sType', 'date-html');
                        scope.dtColumns[7] = DTColumnDefBuilder.newColumnDef(7);
                    } else if (scope.location === 'queues') {
                        scope.dtColumns[5] = DTColumnDefBuilder.newColumnDef(5);
                        scope.dtColumns[6] = DTColumnDefBuilder.newColumnDef(6).withOption('sType', 'date');
                        scope.dtColumns[7] = DTColumnDefBuilder.newColumnDef(7).withOption('sType', 'date-html');
                    }
                },
                post: function postLink(scope) {
                    scope.$watch('input.article', function(n, o) {
                        if (n !== o) {
                            $timeout.cancel(scope.articleTimeoutPromise);
                            scope.articleTimeoutPromise = $timeout(function() {
                                if (/^[\d]*$/.test(scope.input.article)) {
                                    scope.getArticle(scope.input.article);
                                }
                            }, 500);
                        }
                    });
                }
            },
            controller: function($scope) {
                user.getAllUsers().then(function(users) {
                    var tempArr = [];
                    _.each(users, function(user) {
                        tempArr.push({
                            name: user.name,
                            email: user.email
                        });
                    });
                    $scope.data.curators = tempArr;
                });
                $scope.getButtonHtml = function (type, addedAt) {
                    var result = '';
                    switch(type) {
                    case 'add':
                        if ($scope.data.editing) {
                            result = 'Save modified curation';
                        } else {
                            result = 'Add';
                        }
                        break;
                    case 'complete':
                        result = '<i class="fa fa-check"></i>';
                        break;
                    case 'update':
                        result = '<i class="fa fa-check"></i>';
                        break;
                    case 'delete':
                        result = '<i class="fa fa-trash-o"></i>';
                        break;
                    }
                    return result;
                };
                $scope.processCuration = function() {
                    if ($scope.data.editing) {
                        saveModifiedCuration();
                    } else if ($scope.location === 'queues') {
                        var tempArr = $scope.input.variant.split(';');
                        _.each(tempArr, function(pair) {
                            if (pair) {
                                var tempIndex = pair.indexOf(':');
                                var hugoSymbol = pair.substring(0, tempIndex);
                                var variant = pair.substring(tempIndex+1);
                                if (hugoSymbol && variant) {
                                    $scope.data.hugoVariantMapping[hugoSymbol.trim()] = variant.trim();
                                }
                            }
                        });
                        _.each($scope.input.hugoSymbols, function(hugoSymbol) {
                            if (tempArr.length === 1) {
                                // This is the case where variants are not entered following the format of GeneA:VariantA;GeneB:VariantB
                                // In this case, we will use the input variant string for all genes directly
                                $scope.data.hugoVariantMapping[hugoSymbol] = $scope.input.variant.trim();
                            }
                            addCuration(hugoSymbol);
                        });
                    } else if ($scope.location === 'gene') {
                        addCuration($scope.hugoSymbol);
                    }
                    $scope.clearInput();
                };
                function addCuration(hugoSymbol) {
                    var currentQueues = $scope.getQueuesByGene(hugoSymbol);
                    var item = {
                        link: $scope.input.link,
                        mainType: $scope.input.mainType ? $scope.input.mainType : '',
                        subType: $scope.input.subType ? $scope.input.subType.name : '',
                        section: $scope.input.section ? $scope.input.section.join() : '',
                        curator: $scope.input.curator ? $scope.input.curator.name : '',
                        curated: false,
                        addedBy: $rootScope.me.name,
                        addedAt: new Date().getTime(),
                        dueDay: $scope.input.dueDay ? new Date($scope.input.dueDay).getTime() : '',
                        comment: $scope.input.comment,
                        notified: false,
                        hugoSymbol: hugoSymbol
                    };
                    if ($scope.location === 'gene') {
                        item.variant = $scope.input.variant;
                    } else if ($scope.location === 'queues') {
                        if ($scope.data.hugoVariantMapping[hugoSymbol]) {
                            item.variant = $scope.data.hugoVariantMapping[hugoSymbol];
                        } else {
                            item.variant = '';
                        }
                    }
                    if ($scope.predictedArticle && $scope.validPMID) {
                        item.article = $scope.predictedArticle;
                        item.pmid = $scope.input.article;
                        item.pmidString = 'PMID: ' + item.pmid;
                    } else {
                        item.article = $scope.input.article;
                    }
                    currentQueues.push(item);
                    $scope.updateQueueInDB(hugoSymbol, currentQueues).then(function(result) {
                        $scope.queue.push(item);
                        if (item.curator) {
                            $scope.sendEmail(item);
                        }
                    });
                }
                $scope.initialProcess = function(x, type) {
                    var hugoSymbol;
                    if ($scope.location === 'queues') {
                        hugoSymbol = x.hugoSymbol;
                    } else {
                        hugoSymbol = $scope.hugoSymbol;
                    }
                    var queueItem;
                    for (var i = 0; i < $scope.queue.length; i++) {
                        if ($scope.queue[i].addedAt === x.addedAt) {
                            queueItem = $scope.queue[i];
                            break;
                        }
                    }
                    switch (type) {
                    case 'edit':
                        editCuration(queueItem);
                        break;
                    case 'delete':
                        deleteCuration(queueItem);
                        break;
                    case 'complete':
                        completeCuration(queueItem);
                        break;
                    }
                };

                function editCuration(queueItem) {
                    $scope.data.resendEmail = false;
                    $scope.data.editing = true;
                    $scope.data.queueItemInEditing = queueItem;
                    $scope.data.modifiedCurator = {};
                    if (queueItem.curator) {
                        for (var i = 0; i < $scope.data.curators.length; i++) {
                            if ($scope.data.curators[i].name === queueItem.curator) {
                                $scope.data.modifiedCurator = $scope.data.curators[i];
                                break;
                            }
                        }
                    }
                    $scope.data.modifiedMainType = '';
                    $scope.data.modifiedSubType = {};
                    if (queueItem.mainType) {
                        for (var i = 0;i < $scope.data.mainTypes.length; i++) {
                            if ($scope.data.mainTypes[i] === queueItem.mainType) {
                                $scope.data.modifiedMainType = $scope.data.mainTypes[i];
                                break;
                            }
                        }
                    }
                    if ($scope.data.modifiedMainType && queueItem.subType) {
                        for (var i = 0;i < $scope.data.subTypes[$scope.data.modifiedMainType].length; i++) {
                            if ($scope.data.subTypes[$scope.data.modifiedMainType][i].name === queueItem.subType) {
                                $scope.data.modifiedSubType = $scope.data.subTypes[$scope.data.modifiedMainType][i];
                                break;
                            }
                        }
                    }
                    $scope.data.formExpanded = true;
                    $scope.input = {
                        article: queueItem.article,
                        link: queueItem.link,
                        variant: queueItem.variant,
                        comment: queueItem.comment,
                        hugoSymbols: [queueItem.hugoSymbol]
                    };
                    if (!_.isEmpty($scope.data.modifiedCurator)) {
                        $scope.input.curator = $scope.data.modifiedCurator;
                    }
                    if ($scope.data.modifiedMainType) {
                        $scope.input.mainType = $scope.data.modifiedMainType;
                    }
                    if (!_.isEmpty($scope.data.modifiedSubType)) {
                        $scope.input.subType = $scope.data.modifiedSubType;
                    }
                    if (queueItem.section) {
                        $scope.input.section = queueItem.section.split(',');
                    }
                    if (queueItem.dueDay) {
                        $scope.input.dueDay = $scope.getFormattedDate(queueItem.dueDay);
                    }
                    $timeout(function() {
                        var dueDay = angular.element(document.querySelector('#datepicker'));
                        dueDay.datepicker();
                    }, 1000);
                }
                $scope.getFormattedDate = function(timeStamp) {
                    var tempTime = new Date(timeStamp);
                    var month = tempTime.getMonth() + 1;
                    var day = tempTime.getDate();
                    var year = tempTime.getFullYear();
                    return month + "/" + day + "/" + year;
                }
                function saveModifiedCuration() {
                    var queueItem = $scope.data.queueItemInEditing;
                    var hugoSymbol = queueItem.hugoSymbol;
                    var currentQueues = $scope.getQueuesByGene(hugoSymbol);
                    var item = {};
                    for (var i = 0; i < currentQueues.length; i++) {
                        if (currentQueues[i].addedAt === queueItem.addedAt) {
                            item = angular.copy(queueItem);
                            item.link = $scope.input.link;
                            item.subType = $scope.input.subType ? $scope.input.subType.name : '';
                            item.section = $scope.input.section ? $scope.input.section.join() : '';
                            item.dueDay = $scope.input.dueDay ? new Date($scope.input.dueDay).getTime() : '';
                            item.comment = $scope.input.comment;
                            item.variant = $scope.input.variant;
                            item.curator = $scope.input.curator ? $scope.input.curator.name : '';
                            if ($scope.predictedArticle && $scope.validPMID) {
                                item.article = $scope.predictedArticle;
                                item.pmid = $scope.input.article;
                                item.pmidString = 'PMID: ' + item.pmid;
                            } else {
                                item.article = $scope.input.article;
                            }
                            currentQueues[i] = item;
                            break;
                        }
                    }
                    $scope.updateQueueInDB(hugoSymbol, currentQueues).then(function(result) {
                        _.each(_.keys(item), function(key) {
                            $scope.data.queueItemInEditing[key] = item[key];
                        });
                        if ($scope.resendEmail) {
                            $scope.sendEmail(queueItem);
                        }
                    });
                }
                function completeCuration(queueItem) {
                    var hugoSymbol = queueItem.hugoSymbol;
                    var currentQueues = $scope.getQueuesByGene(hugoSymbol);
                    for (var i = 0; i < currentQueues.length; i++) {
                        if (currentQueues[i].addedAt === queueItem.addedAt) {
                            currentQueues[i].curated = true;
                            break;
                        }
                    }
                    $scope.updateQueueInDB(hugoSymbol, currentQueues).then(function(result) {
                        queueItem.curated = true;
                    });
                };
                function setCurationNotified(queueItem) {
                    var hugoSymbol = queueItem.hugoSymbol;
                    var currentQueues = $scope.getQueuesByGene(hugoSymbol);
                    var currentTimeStamp = new Date().getTime();
                    for (var i = 0; i < currentQueues.length; i++) {
                        if (currentQueues[i].addedAt === queueItem.addedAt) {
                            currentQueues[i].notified = currentTimeStamp;
                            break;
                        }
                    }
                    $scope.updateQueueInDB(hugoSymbol, currentQueues).then(function(result) {
                        queueItem.notified = currentTimeStamp;
                    });
                };
                function deleteCuration(queueItem) {
                    var hugoSymbol = queueItem.hugoSymbol;
                    var currentQueues = $scope.getQueuesByGene(hugoSymbol);
                    var updatedQueues = [];
                    _.each(currentQueues, function(item) {
                        if (item.addedAt !== queueItem.addedAt) {
                            updatedQueues.push(item);
                        }
                    });
                    $scope.updateQueueInDB(hugoSymbol, updatedQueues).then(function(result) {
                        $scope.queue = _.without($scope.queue, queueItem);
                    });
                };
                $scope.getArticle = function(pmid) {
                    if (!pmid) {
                        $scope.predictedArticle = '';
                        $scope.validPMID = false;
                        $scope.input.link = '';
                        return;
                    }
                    DatabaseConnector.getPubMedArticle([pmid], function(data) {
                        var articleData = data.result[pmid];
                        if (!articleData || articleData.error) {
                            $scope.predictedArticle = '<p style="color: red">Invalid PMID</p>';
                            $scope.validPMID = false;
                            $scope.input.link = '';
                        } else {
                            var tempArticle = articleData.title.trim();
                            // for some articles, the tile start with '[', and end with '].' we need to trim it in such cases
                            if (/^\[.*\]\.$/.test(tempArticle)) {
                                tempArticle = tempArticle.substring(1, tempArticle.length - 2);
                            }
                            var articleStr = tempArticle + ' ';
                            if (articleData && _.isArray(articleData.authors) && articleData.authors.length > 0) {
                                articleStr += articleData.authors[0].name + ' et al. ';
                            }
                            if (articleData.source) {
                                articleStr += articleData.source + '.';
                            }
                            if (articleData.pubdate) {
                                articleStr += (new Date(articleData.pubdate)).getFullYear();
                            }
                            $scope.pmid = pmid;
                            $scope.predictedArticle = mainUtils.getString(articleStr);
                            $scope.validPMID = true;
                            $scope.input.link = 'https://www.ncbi.nlm.nih.gov/pubmed/' + pmid;
                        }
                    }, function() {
                        console.log('error');
                    });
                };
                $scope.sendEmail = function(queueItem) {
                    var expiredCuration = false;
                    if ($scope.isExpiredCuration(queueItem.dueDay)) {
                        expiredCuration = true;
                    }
                    var email = '';
                    for (var i = 0; i < $scope.data.curators.length; i++) {
                        if (queueItem.curator === $scope.data.curators[i].name) {
                            email = $scope.data.curators[i].email;
                            break;
                        }
                    }
                    if (!email) return;
                    var content = 'Dear ' + queueItem.curator.split(' ')[0] + ',\n\n';
                    if (expiredCuration) {
                        content += 'You have not completed curation of the assigned publication: ' + queueItem.article;
                        if (queueItem.link) {
                            content += '(' + queueItem.link + ')';
                        }
                        content += ' which was due on ' + $scope.getFormattedDate(queueItem.dueDay) + '. Please complete this assignment as soon as possible and let us know when you have done this. \n\nIf you have already completed this task, please remember to CLICK THE GREEN CHECK BOX BUTTON at the Curation Queue page or the bottom of the gene page (this will let us know the task is complete). If you have any questions or concerns please email or slack us as needed.';
                        content += 'Thank you, \nOncoKB Admin';
                    } else {
                        content += queueItem.addedBy + ' of OncoKB would like you curate the following publications in the indicated alteration, tumor type and section:\n\n';
                        var tempArr = [queueItem.article];
                        if (queueItem.link) {
                            tempArr = tempArr.concat(['(', queueItem.link, ')']);
                        }
                        if (queueItem.variant) {
                            tempArr = tempArr.concat(['Alteration:', queueItem.variant + ',']);
                        }
                        if (queueItem.subType) {
                            tempArr = tempArr.concat(['Tumor type:', queueItem.subType + ',']);
                        }
                        if (queueItem.section) {
                            tempArr = tempArr.concat(['Section:', queueItem.section]);
                        }
                        content += tempArr.join(' ') + '\n';
                        if (queueItem.comment) {
                            content += queueItem.comment + '\n';
                        }
                        content += '\nPlease try to curate this literature before ' + $scope.getFormattedDate(queueItem.dueDay) + ' and remember to log your hours for curating this data.\n\n';
                        content += 'IMPORTANT: Please remember to CLICK THE GREEN CHECK BOX BUTTON at the Curation Queue page or the bottom of the gene page (this will let us know the task is complete).\n\n';
                        content += 'If you have any questions or concerns please email or slack ' + queueItem.addedBy + '.\n\n';
                        content += 'Thank you, \nOncoKB Admin';
                    }
                    var subject = 'OncoKB Curation Assignment';
                    mainUtils.sendEmail(email, subject, content).then(function() {
                        if (expiredCuration) {
                            setCurationNotified(queueItem);
                        }
                    }, function(error) {
                        dialogs.error('Error', 'Failed to notify curator automatically. Please send curator email manually.');
                    });
                };

                var annotationLocation = $scope.specifyAnnotationInGene();
                $scope.getAnnotationLocation = function(x) {
                    if (x.pmid && annotationLocation[x.pmid]) {
                        return annotationLocation[x.pmid].join('; ');
                    } else if (x.article && annotationLocation[x.article]) {
                        return annotationLocation[x.article].join('; ');
                    }
                };
                function getOncoTreeMainTypes() {
                    mainUtils.getOncoTreeMainTypes().then(function(result) {
                        var mainTypesReturned = result.mainTypes,
                            tumorTypesReturned = result.tumorTypes;
                        if (mainTypesReturned) {
                            $scope.data.mainTypes = _.map(mainTypesReturned, function(item) {
                                return item.name;
                            });
                            if (_.isArray(tumorTypesReturned)) {
                                var tumorTypes = {};
                                var allTumorTypes = [];
                                _.each(mainTypesReturned, function(mainType, i) {
                                    tumorTypes[mainType.name] = tumorTypesReturned[i];
                                });
                                $scope.data.subTypes = tumorTypes;
                            }
                        }
                    }, function(error) {
                    });
                }
                getOncoTreeMainTypes();
                $scope.toggleForm = function() {
                    $scope.data.formExpanded = !$scope.data.formExpanded;
                    $timeout(function() {
                        var dueDay = angular.element(document.querySelector('#datepicker'));
                        // set 2 weeks as the default due day
                        $scope.input.dueDay = $scope.getFormattedDate(new Date().getTime() + 14*8.64e+7);
                        dueDay.datepicker();
                    }, 1000);
                };
                $scope.clearInput = function() {
                    $scope.input = {
                        article: '',
                        link: '',
                        hugoSymbols: '',
                        variant: '',
                        subType: '',
                        section: '',
                        curator: '',
                        dueDay: '',
                        comment: ''
                    };
                    $scope.data.formExpanded = false;
                    $scope.data.editing = false;
                    $scope.predictedArticle = '';
                    $scope.validPMID = false;
                    $scope.data.resendEmail = false;
                };
                $scope.isExpiredCuration = mainUtils.isExpiredCuration;
                $scope.checkInput = function() {
                    var queueItem = $scope.data.queueItemInEditing;
                    if ($scope.input.dueDay && $scope.isExpiredCuration(new Date($scope.input.dueDay).getTime())) {
                        $scope.data.invalidData = true;
                    } else {
                        $scope.data.invalidData = false;
                    }
                    if ($scope.data.editing && !$scope.data.invalidData) {
                        if ($scope.input.curator && queueItem.curator !== $scope.input.curator.name ||
                            $scope.input.dueDay && queueItem.dueDay !== new Date($scope.input.dueDay).getTime()) {
                            $scope.data.resendEmail = true;
                        } else {
                            $scope.data.resendEmail = false;
                        }
                    }
                    $scope.resendEmail = $scope.data.resendEmail;
                };
                $scope.secondTimeAutoNotify = function() {
                    _.each($scope.queue, function (queueItem) {
                        var hugoSymbol = queueItem.hugoSymbol;
                        if (hugoSymbol && queueItem.curator && !queueItem.curated && mainUtils.isExpiredCuration(queueItem.dueDay) && !queueItem.notified) {
                            $scope.sendEmail(queueItem);
                        }
                    });
                };
                $scope.getQueuesByGene = function(hugoSymbol) {
                    return $rootScope.firebaseQueues[hugoSymbol] ? angular.copy($rootScope.firebaseQueues[hugoSymbol].queue) : [];
                };
                $scope.updateQueueInDB = function (hugoSymbol, updatedQueues) {
                    var defer = $q.defer();
                    firebase.database().ref('Queues/' + hugoSymbol).set({
                        queue: updatedQueues
                    }).then(function(result) {
                        defer.resolve('success');
                    }).catch(function(error) {
                        dialogs.error('Error', 'Fail to save changes to database. Please contact developer!');
                        defer.reject(error);
                    });
                    return defer.promise;
                }
                jQuery.extend(jQuery.fn.dataTableExt.oSort, {
                    'date-html-asc': function(a, b) {
                        a = $(a).text();
                        b = $(b).text();
                        return mainUtils.getTimeStamp(a) - mainUtils.getTimeStamp(b);
                    },
                    'date-html-desc': function(a, b) {
                        a = $(a).text();
                        b = $(b).text();
                        return mainUtils.getTimeStamp(b) - mainUtils.getTimeStamp(a);
                    }
                });
            }
        };
    })
;
