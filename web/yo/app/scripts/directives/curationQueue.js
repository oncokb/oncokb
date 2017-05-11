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
    .directive('curationQueue', function(DTColumnDefBuilder, DTOptionsBuilder, user, DatabaseConnector, $rootScope, $timeout, users, mainUtils, dialogs, _, $q) {
        return {
            templateUrl: 'views/curationQueue.html',
            restrict: 'E',
            scope: {
                specifyAnnotationInGene: '&specifyAnnotation'
            },
            replace: true,
            link: {
                pre: function preLink(scope) {
                    scope.queue = [];
                    scope.email = {
                        status: {sending: false},
                        returnMessage: ''
                    };
                    scope.queueModel = $rootScope.model.getRoot().get('queue');
                    _.each(scope.queueModel.asArray(), function(item) {
                        scope.queue.push({
                            article: item.get('article'),
                            pmid: item.get('pmid'),
                            pmidString: 'PMID: ' + item.get('pmid'),
                            link: item.get('link'),
                            variant: item.get('variant'),
                            addedBy: item.get('addedBy'),
                            addedAt: item.get('addedAt'),
                            curated: item.get('curated'),
                            curator: item.get('curator')
                        });
                    });
                    scope.getCuratorsList();
                    scope.getArticleList();
                    scope.dtOptions = {
                        hasBootstrap: true,
                        paging: false,
                        scrollCollapse: true,
                        aaSorting: [[0, 'asc']]
                    };
                    scope.dtColumns = [
                        DTColumnDefBuilder.newColumnDef(0),
                        DTColumnDefBuilder.newColumnDef(1),
                        DTColumnDefBuilder.newColumnDef(2),
                        DTColumnDefBuilder.newColumnDef(3).withOption('sType', 'date'),
                        DTColumnDefBuilder.newColumnDef(4),
                        DTColumnDefBuilder.newColumnDef(5),
                        DTColumnDefBuilder.newColumnDef(6)
                    ];
                },
                post: function postLink(scope) {
                    scope.$watch('article', function(n, o) {
                        if (n !== o) {
                            $timeout.cancel(scope.articleTimeoutPromise);
                            scope.articleTimeoutPromise = $timeout(function() {
                                if (/^[\d]*$/.test(scope.article)) {
                                    scope.getArticle(scope.article);
                                }
                            }, 500);
                        }
                    });
                }
            },
            controller: function($scope) {
                $scope.allCuration = false;
                DatabaseConnector.getOncokbInfo(function(oncokbInfo) {
                    if (oncokbInfo && oncokbInfo.users) {
                        $scope.curators = oncokbInfo.users;
                    }
                });
                $scope.userRole = users.getMe().role;
                $scope.getCuratorsList = function() {
                    var tempArr = [];
                    _.each($scope.queue, function(item) {
                        if (!item.curated && item.curator) {
                            tempArr.push(item.curator);
                        }
                    });
                    $scope.curatorNotificationList = _.uniq(tempArr);
                };
                $scope.getArticleList = function() {
                    var tempArr = [];
                    _.each($scope.queue, function(item) {
                        if (item.pmid) {
                            tempArr.push(item.pmid);
                        } else if (item.article) {
                            tempArr.push(item.article);
                        }
                    });
                    $scope.articleList = _.uniq(tempArr);
                };
                $scope.addCuration = function() {
                    if ($scope.articleList.indexOf($scope.article) === -1) {
                        addConfirmedCuration();
                    } else {
                        var dlg = dialogs.confirm('Confirmation', $scope.article + ' has already been curated or added. Are you sure you want to add this?');
                        dlg.result.then(function() {
                            addConfirmedCuration();
                        }, function() {
                            console.log('canceled');
                        });
                    }
                };
                function addConfirmedCuration() {
                    var item = $rootScope.model.createMap({
                        link: $scope.link,
                        variant: $scope.variant,
                        curator: $scope.curator ? $scope.curator.name : '',
                        curated: false,
                        addedBy: user.name,
                        addedAt: new Date().getTime()
                    });
                    if ($scope.predictedArticle && $scope.validPMID) {
                        item.set('article', $scope.predictedArticle);
                        item.set('pmid', $scope.article);
                        item.set('pmidString', 'PMID: ' + $scope.article);
                    } else {
                        item.set('article', $scope.article);
                    }
                    $scope.queueModel.push(item);
                    $scope.queue.push({
                        article: item.get('article'),
                        pmid: item.get('pmid'),
                        pmidString: 'PMID: ' + item.get('pmid'),
                        link: item.get('link'),
                        variant: item.get('variant'),
                        addedBy: item.get('addedBy'),
                        addedAt: item.get('addedAt'),
                        curated: item.get('curated'),
                        curator: item.get('curator')
                    });
                    $scope.article = '';
                    $scope.link = '';
                    $scope.variant = '';
                    $scope.curator = '';
                    $scope.predictedArticle = '';
                    $scope.validPMID = false;
                    $scope.getCuratorsList();
                    $scope.getArticleList();
                }

                $scope.editCuration = function(index) {
                    if (!$scope.queue[index]) {
                        return;
                    }
                    $scope.queue[index].editable = true;
                    if ($scope.queue[index].curator) {
                        for (var i = 0; i < $scope.curators.length; i++) {
                            if ($scope.curators[i].name === $scope.queue[index].curator) {
                                $scope.modifiedCurator = $scope.curators[i];
                                break;
                            }
                        }
                    }
                };
                $scope.updateCuration = function(index, x) {
                    if (!$scope.queue[index]) {
                        return;
                    }
                    var queueModelItem = $scope.queueModel.get(index);
                    var queueItem = $scope.queue[index];
                    if (queueModelItem.get('addedAt') === queueItem.addedAt) {
                        if (x.article !== queueModelItem.get('article') && $scope.articleList.indexOf(x.article) !== -1) {
                            var dlg = dialogs.confirm('Confirmation', x.article + ' has already been curated or added. Are you sure you want to modify to this?');
                            dlg.result.then(function() {
                                updateConfirmedCuration(index, x);
                            }, function() {
                                $scope.queue[index].editable = false;
                                x.article = queueModelItem.get('article');
                                x.variant = queueModelItem.get('variant');
                                x.curator = queueModelItem.get('curator');
                            });
                        } else {
                            updateConfirmedCuration(index, x);
                        }
                    }
                };
                function updateConfirmedCuration(index, x) {
                    var queueModelItem = $scope.queueModel.get(index);
                    $scope.queue[index].editable = false;
                    $scope.queue[index].curator = $scope.modifiedCurator ? $scope.modifiedCurator.name : '';
                    if (!x.pmid) {
                        queueModelItem.set('article', x.article);
                        $scope.getArticleList();
                    }
                    queueModelItem.set('variant', x.variant);
                    queueModelItem.set('curator', $scope.modifiedCurator ? $scope.modifiedCurator.name : '');
                    $scope.getCuratorsList();
                }

                $scope.synchronize = function(modifiedCurator) {
                    $scope.modifiedCurator = modifiedCurator;
                };
                $scope.completeCuration = function(index) {
                    var queueModelItem = $scope.queueModel.get(index);
                    var queueItem = $scope.queue[index];
                    if (queueModelItem.get('addedAt') === queueItem.addedAt) {
                        queueModelItem.set('curated', true);
                        queueItem.curated = true;
                    }
                };
                $scope.deleteCuration = function(index) {
                    if ($scope.queueModel.get(index).get('addedAt') === $scope.queue[index].addedAt) {
                        $scope.queueModel.remove(index);
                        $scope.queue.splice(index, 1);
                        $scope.getCuratorsList();
                        $scope.getArticleList();
                    }
                };
                $scope.getArticle = function(pmid) {
                    if (!pmid) {
                        $scope.predictedArticle = '';
                        $scope.validPMID = false;
                        $scope.link = '';
                        return;
                    }
                    DatabaseConnector.getPubMedArticle([pmid], function(data) {
                        var articleData = data.result[pmid];
                        if (!articleData || articleData.error) {
                            $scope.predictedArticle = '<p style="color: red">Invalid PMID</p>';
                            $scope.validPMID = false;
                            $scope.link = '';
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
                            $scope.predictedArticle = articleStr;
                            $scope.validPMID = true;
                            $scope.link = 'https://www.ncbi.nlm.nih.gov/pubmed/' + pmid;
                        }
                    }, function() {
                        console.log('error');
                    });
                };
                $scope.sendEmail = function(curatorsToNotify) {
                    $scope.email.status.sending = true;
                    _.each(curatorsToNotify, function(name) {
                        var email;
                        for (var i = 0; i < $scope.curators.length; i++) {
                            if (name === $scope.curators[i].name) {
                                email = $scope.curators[i].email;
                                break;
                            }
                        }
                        if (email) {
                            var articles = [];
                            _.each($scope.queue, function(item) {
                                if (name === item.curator) {
                                    articles.push({
                                        link: item.link,
                                        article: item.article
                                    });
                                }
                            });
                            if (articles.length > 0) {
                                generateEmail(email, name, user.name, articles, new Date().getTime())
                                    .then(function() {
                                        $scope.email.status.sending = false;
                                        $scope.email.returnMessage = 'Email Sent';
                                    }, function(error) {
                                        $scope.email.status.sending = false;
                                        $scope.email.returnMessage = 'Failed to send Email. Please mention following message to developer: ' + error;
                                    });
                            }
                        }
                    });
                };
                function generateEmail(email, curatorName, adminName, articles, time) {
                    var deferred = $q.defer();
                    var content = 'Dear ' + curatorName.split(' ')[0] + ',\n\n';
                    content += adminName + ' of OncoKB would like you curate the following publications:\n\n';
                    _.each(articles, function(article, index) {
                        content += (index + 1) + ') ' + article.article + ' (' + article.link + ')\n';
                    });
                    content += '\nPlease try to curate this literature within two weeks (' + new Date(time + 12096e5).toDateString() + ') and remember to log your hours for curating this data.\n\n';
                    content += 'If you have any questions or concerns please email or slack ' + adminName + '.\n\n';
                    content += 'Thank you, \nOncoKB Admin';
                    var subject = 'OncoKB Curation Assignment';
                    mainUtils.sendEmail(email, subject, content)
                        .then(function() {
                            deferred.resolve();
                        }, function(error) {
                            deferred.reject(error);
                        });
                    return deferred.promise;
                }

                $scope.prepareSendEmail = function() {
                    $scope.email.returnMessage = '';
                };
                $scope.toggleCompletedCuration = function() {
                    return function(item) {
                        if ($scope.allCuration) {
                            return true;
                        }
                        return !item.curated;
                    };
                };
                var annotationLocation = $scope.specifyAnnotationInGene();
                $scope.getAnnotationLocation = function(x) {
                    if (x.pmid && annotationLocation[x.pmid]) {
                        return annotationLocation[x.pmid].join('; ');
                    } else if (x.article && annotationLocation[x.article]) {
                        return annotationLocation[x.article].join('; ');
                    }
                };
            }
        };
    })
;
