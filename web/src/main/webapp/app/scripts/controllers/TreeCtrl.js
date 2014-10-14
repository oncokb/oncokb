var treeEvidence = [];

oncokbApp.controller('TreeCtrl', [
    '$scope',
    '$location',
    'Evidence', 
    'AnalysisEvidence', 
    function ($scope, $location, Evidence, AnalysisEvidence) {

    'use strict';

    $scope.init = function() {
        $scope.treeType = 'separated';
        $scope.rendering = true;
        $scope.searchKeywords = '';
        $scope.searchResult = '';

        if(treeEvidence.length === 0) {
            Evidence.getFromServer().success(function(data) {
                treeEvidence = angular.copy(data);
                drawTree(data);
            });
        }else {
            drawTree(treeEvidence);
        }
    }

    function drawTree(data) {
        var processed = AnalysisEvidence.init($scope.treeType, data) ;
        $scope.rendering = true;
        $scope.genes = processed.genes;
        $scope.descriptions = processed.descriptions;

        Tree.init( processed.treeInfo, $scope.descriptions);
        
        $scope.rendering = false;
    }

    $scope.search = function() {
        var result = Tree.search($scope.searchKeywords),
            resutlLength = result.length,
            infoText = (resutlLength === 0 ? "No" : resutlLength) + " result" + (resutlLength <= 1 ? "" :"s" );
        
        if($scope.searchKeywords !== '') {
            $scope.searchResult = infoText;
        }else {
            $scope.searchResult = '';
        }
    }

    $scope.showSearchResult = function() {
        return $scope.searchResult !== '';
    }

    $scope.keywordsExist = function() {
        return $scope.searchKeywords !== '';
    }

    $scope.removeSearchKeywords = function() {
        $scope.searchKeywords = '';
        $scope.searchResult = '';
    }

    $scope.drawTree = function(treeType) {
        if(treeType !== $scope.treeType) {
            angular.element(document).find('#tree').empty();
            $scope.treeType = treeType;
            drawTree();
        }
    }

    $scope.expandAll = function() {
        Tree.expandAll();
    }

    $scope.collapseAll = function() {
        Tree.collapseAll();
    }

    $scope.tabIsActive = function(route) {
        if( route instanceof Array) {
            for (var i = route.length - 1; i >= 0; i--) {
                if(route[i] === $location.path()) {
                    return true;
                }
            };
            return false;
        }else {
            return route === $location.path();
        }
    }
    // var callback = function(data) {
    // 	jsonData = data;
    // 	jsonDataL = jsonData.length;
	   //  tumorTypes = TumorType;
	   //  analysisData();  
    // } 
    
    // app.factory('EvidenceAndTumorType', ['$http', '$q', function ($http, $q) {
    //     var tumorTypeRequest = $http.get('tumorType.json');
    //     var evidenceRequest = $http.get('evidence.json');
       
       
    //     $q.all([evidenceRequest, tumorTypeRequest]).then(function(values) {
    //         console.log(values);
    //         jsonData = values[0].data;
    //         jsonDataL = jsonData.length;
    //         tumorTypes = $.extend(true, [], values[1].data);
    //         analysisData();
    //     },function() {
    //         tumorTypeRequest = $http.get('data/tumorType.json');
    //         evidenceRequest = $http.get('data/evidence.json');
           
    //         $q.all([evidenceRequest, tumorTypeRequest]).then(function(values) {
    //             jsonData = values[0].data;
    //             jsonDataL = jsonData.length;
    //             tumorTypes = $.extend(true, [], values[1].data);
    //             console.log(_.extend({}, jsonData));
    //             analysisData();
    //         });
    //     });
    // }]);
    
    // app.factory('Evidence', ['$http', function ($http) {
    //     $http.get('evidence.json')
    //         .success(function(data, status) {
    //                jsonData = data;
    //                 jsonDataL = jsonData.length;
    //                 analysisData();
    //         })
    //         .error(function(data, status) {
    //             $http.get('data/evidence.json')
    //                 .success(function(data, status) {
    //                     jsonData = data;
    //                     jsonDataL = jsonData.length;
    //                     analysisData();
    //             });
    //     });
    // }]);

    // app.factory('TumorType', ['$http',  function ($http) {
    //     $http.get('tumorType.json')
    //         .success(function(data, status) {
    //            tumorTypes = data;
    //         })
    //         .error(function(data, status) {
    //             $http.get('data/tumorType.json')
    //                 .success(function(data, status) {
    //                     tumorTypes = data;
    //             });
    //     });
    // }]);
    
    // function generateVariant(params, callback) {
    //     var _params = $.extend(true, {}, params);
    //     $.ajax({
    //         type: 'POST', 
    //         url: 'var_annotation',
    //         data: _params,
    //         dataType:'xml'
    //     })
    //     .success(function(data) {
    //         var _variantJson = $.xml2json(data);
    //         callback(_variantJson);
    //     })
    //     .fail(function(data) {
    //         callback(null);
    //     });
    // }
}]);