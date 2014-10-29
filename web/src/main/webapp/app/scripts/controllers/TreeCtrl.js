var treeEvidence = [];
var processedData;

angular.module('webappApp').controller('TreeCtrl', [
    '$scope',
    '$location',
    '$timeout',
    'Evidence', 
    'AnalysisEvidence', 
    function ($scope, $location, $timeout, Evidence, AnalysisEvidence) {

    'use strict';

    $scope.init = function() {
        $scope.treeType = 'separated';
        $scope.rendering = true;
        $scope.searchKeywords = '';
        $scope.searchResult = '';

        if(treeEvidence.length === 0) {
            // Evidence.getFromFile().success(function(data) {
            Evidence.getFromServer().success(function(data) {
                treeEvidence = angular.copy(data);
                drawTree(data);
            });
        }else {
            $timeout(function(){
                drawTree(treeEvidence);
            }, 100);
        }
    };

    function drawTree(data) {
        if(typeof processedData === 'undefined' || processedData) {
            processedData = AnalysisEvidence.init($scope.treeType, data);
        }
        
        $scope.rendering = true;
        $scope.genes = processedData.genes;
        $scope.descriptions = processedData.descriptions;

        Tree.init( processedData.treeInfo, $scope.descriptions);
        
        $scope.rendering = false;
    }

    $scope.search = function() {
        var result = Tree.search($scope.searchKeywords),
            resutlLength = result.length,
            infoText = (resutlLength === 0 ? 'No' : resutlLength) + ' result' + (resutlLength <= 1 ? '' :'s' );
        
        if($scope.searchKeywords !== '') {
            $scope.searchResult = infoText;
        }else {
            $scope.searchResult = '';
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
        if(treeType !== $scope.treeType) {
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
        if( route instanceof Array) {
            for (var i = route.length - 1; i >= 0; i--) {
                if(route[i] === $location.path()) {
                    return true;
                }
            }
            return false;
        }else {
            return route === $location.path();
        }
    };
}]);