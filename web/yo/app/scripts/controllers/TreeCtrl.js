angular.module('oncokbApp').controller('TreeCtrl', [
    '$scope',
    '$location',
    '$timeout',
    'DatabaseConnector',
    'AnalysisEvidence',
    'storage',
    'OncoKB',
    'Tree',
    function($scope, $location, $timeout, DatabaseConnector, AnalysisEvidence, storage, OncoKB, Tree) {
        'use strict';

        function drawTree(data) {
            if (typeof OncoKB.tree.processedData === 'undefined' || OncoKB.tree.processedData) {
                OncoKB.tree.processedData = AnalysisEvidence.init($scope.treeType, data);
            }

            $scope.rendering = true;
            $scope.genes = OncoKB.tree.processedData.genes;
            $scope.descriptions = OncoKB.tree.processedData.descriptions;

            Tree.init(OncoKB.tree.processedData.treeInfo, $scope.descriptions);

            $scope.rendering = false;
        }

        $scope.init = function() {
            $scope.treeType = 'separated';
            $scope.rendering = true;
            $scope.searchKeywords = '';
            $scope.searchResult = '';

            if (OncoKB.global.treeEvidence && OncoKB.global.treeEvidence.length > 0) {
                // Add timeout for letting page change to tab first instead of stucking on previous tab
                $timeout(function() {
                    drawTree(OncoKB.global.treeEvidence);
                }, 100);
            } else {
                DatabaseConnector.getAllEvidence(function(data) {
                    OncoKB.global.treeEvidence = data[0];
                    drawTree(data[0]);
                });
            }
        };

        $scope.search = function() {
            var result = Tree.search($scope.searchKeywords);
            var resutlLength = result.length;
            var infoText = (resutlLength === 0 ? 'No' : resutlLength) + ' result' + (resutlLength <= 1 ? '' : 's');

            if ($scope.searchKeywords === '') {
                $scope.searchResult = '';
            } else {
                $scope.searchResult = infoText;
            }
        };

        $scope.showSearchResult = function() {
            return $scope.searchResult !== '';
        };

        $scope.keywordsExist = function() {
            return $scope.searchKeywords !== '';
        };

        $scope.removeSearchKeywords = function() {
            $scope.searchKeywords = '';
            $scope.searchResult = '';
        };

        $scope.drawTree = function(treeType) {
            if (treeType !== $scope.treeType) {
                angular.element(document).find('#tree').empty();
                $scope.treeType = treeType;
                drawTree();
            }
        };

        $scope.expandAll = function() {
            Tree.expandAll();
        };

        $scope.collapseAll = function() {
            Tree.collapseAll();
        };

        $scope.tabIsActive = function(route) {
            if (route instanceof Array) {
                for (var i = route.length - 1; i >= 0; i--) {
                    if (route[i] === $location.path()) {
                        return true;
                    }
                }
                return false;
            }
            return route === $location.path();
        };
    }]);
