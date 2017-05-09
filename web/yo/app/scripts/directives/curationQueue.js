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
    .directive('curationQueue', function(DTColumnDefBuilder, DTOptionsBuilder, user, DatabaseConnector, $rootScope, $timeout, users, mainUtils) {
        return {
            templateUrl: 'views/curationQueue.html',
            restrict: 'E',
            scope: {
            },
            replace: true,
            link: {
                pre: function preLink(scope) {
                    scope.queue = [];
                    scope.queueModel = $rootScope.model.getRoot().get('queue');
                    _.each(scope.queueModel.asArray(), function(item) {
                        scope.queue.push({article: item.get('article'), pmid: item.get('pmid'), pmidString: 'PMID: ' + item.get('pmid'), link: item.get('link'), variant: item.get('variant'), addedBy: item.get('addedBy'), addedAt: item.get('addedAt'), curated: item.get('curated'), curator: item.get('curator')});
                    });
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
                        DTColumnDefBuilder.newColumnDef(5).withOption('sType', 'curated'),
                        DTColumnDefBuilder.newColumnDef(6)
                    ];
                },
                post: function postLink(scope) {
                    scope.$watch('article', function(n, o) {
                        if(n !== o) {
                            $timeout.cancel(scope.articleTimeoutPromise);
                            scope.articleTimeoutPromise = $timeout(function() {
                                if(/^[\d]*$/.test(scope.article)) {
                                    scope.getArticle(scope.article);
                                }
                            }, 500);
                        }
                    });
                }
            },
            controller: function($scope) {
                DatabaseConnector.getOncokbInfo(function(oncokbInfo) {
                    if (oncokbInfo && oncokbInfo.users) {
                        $scope.curators = oncokbInfo.users;
                    }
                });
                $scope.userRole = users.getMe().role;
                $scope.addCuration = function() {
                    var item = $rootScope.model.createMap({
                        link: $scope.link,
                        variant: $scope.variant,
                        curator: $scope.curator.name,
                        curated: false,
                        addedBy: user.name,
                        addedAt: new Date().getTime()
                    });
                    if($scope.predictedArticle && $scope.validPMID) {
                        item.set('article', $scope.predictedArticle);
                        item.set('pmid', $scope.article);
                        item.set('pmidString', 'PMID: ' + $scope.article);
                    } else {
                        item.set('article', $scope.article);
                    }
                    $scope.queueModel.push(item);
                    $scope.queue.push({article: item.get('article'), pmid: item.get('pmid'), pmidString: 'PMID: ' + item.get('pmid'), link: item.get('link'), variant: item.get('variant'), addedBy: item.get('addedBy'), addedAt: item.get('addedAt'), curated: item.get('curated'), curator: item.get('curator')});
                    $scope.article = '';
                    $scope.link = '';
                    $scope.variant = '';
                    $scope.curator = '';
                    $scope.predictedArticle = '';
                    $scope.validPMID = false;
                };
                $scope.editCuration = function(index) {
                    if (!$scope.queue[index]) return;
                    $scope.queue[index].editable = true;
                };
                $scope.updateCuration = function(index, x) {
                    if (!$scope.queue[index]) return;
                    $scope.queue[index].editable = false;
                    $scope.queue[index].curator = x.curator.name;
                    _.each($scope.queueModel.asArray(), function(item) {
                        if(item.get('addedAt') === $scope.queue[index].addedAt) {
                            if(!x.pmid) {
                                item.set('article', x.article);
                            }
                            item.set('variant', x.variant);
                            item.set('curator', x.curator);
                            item.set('addedBy', user.name);
                            item.set('addedAt', new Date().getTime());
                        }
                    });
                };
                $scope.updateCurated = function(index, curated) {
                    _.each($scope.queueModel.asArray(), function(item) {
                        if(item.get('addedAt') === $scope.queue[index].addedAt) {
                            item.set('curated', curated);
                        }
                    });
                }
                $scope.deleteCuration = function(index) {
                    _.each($scope.queueModel.asArray(), function(item, queueIndex) {
                        if(item.get('addedAt') === $scope.queue[index].addedAt) {
                            $scope.queueModel.remove(queueIndex);
                        }
                    });
                    $scope.queue.splice(index, 1);
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
                        if(!articleData || articleData.error) {
                            $scope.predictedArticle = '<p style="color: red">Invalid PMID</p>';
                            $scope.validPMID = false;
                            $scope.link = '';
                        } else {
                            var articleStr;
                            if(articleData && _.isArray(articleData.authors) && articleData.authors.length > 0) {
                                articleStr = articleData.authors[0].name + ' et al. ';
                            }
                            if(articleData.source) {
                                articleStr += articleData.source + '.';
                            }
                            if(articleData.pubdate) {
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
                    _.each(curatorsToNotify, function(curator) {
                        var articles = [];
                        _.each($scope.queue, function(item) {
                            if (curator.name === item.curator) {
                                articles.push({link: item.link, article: item.article});
                            }
                        });
                        if (articles.length > 0) {
                            generateEmail(curator.email, curator.name, user.name, articles, new Date().getTime());
                        }
                    });
                };
                function generateEmail(email, curatorName, adminName, articles, time) {
                    var content = 'Dear ' + curatorName.split(' ')[0] + ',\n';
                    content += adminName + ' of OncoKB would like you curate the following publications:\n';
                    _.each(articles, function(article, index) {
                        content += (index + 1) + ') ' + article.article + ' (' + article.link + ')\n';
                    });
                    content += 'Please try to curate this literature within two weeks (' + new Date(time + 12096e5).toDateString() + ') and remember to log your hours for curating this data.\n\n';
                    content += 'If you have any questions or concerns please email or slack ' + adminName + '.\n\n';
                    content += 'Thank you, \nOncoKB Admin';
                    var subject = 'OncoKB Curation Assignment';
                    mainUtils.sendEmail(email, subject, content);
                }
            }
        };
    })
;
