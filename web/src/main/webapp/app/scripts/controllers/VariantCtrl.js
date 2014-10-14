'use strict';

/**
 * @ngdoc function
 * @name webappApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the webappApp
 */
angular.module('webappApp')
  	.controller('VariantCtrl', ['$scope', '$filter', 'TumorType', function ($scope, $filter, TumorType) {

	  	$scope.init = function () {

    		$scope.rendering = false;
	    	$scope.displayParts = {
		        geneAnnotation: {
		            displayName: "Gene Annotation",
		            objectName: "gene_annotation"
		        },
		        variantEffect: {
		            displayName: "Variant Effect",
		            objectName: "variant_effect"
		        }
		    };
	    
	    	$scope.summaryTableTitles = [
	            "Treatment Implications", 
	            "Clinical Trials", 
	            "Additional Information", 
	            "FDA Approved Drugs in Tumor Type", 
	            "FDA Approved Drugs in Other Tumor Type"];
	            
	        $scope.summaryTableTitlesContent = {
	            "Treatment Implications": [
	                "nccn_guidelines",
	                "standard_therapeutic_implications",
	                "investigational_therapeutic_implications"],
	            "Clinical Trials": ["clinical_trial"], 
	            "Additional Information": ["prevalence", "prognostic_implications"], 
	            "FDA Approved Drugs in Tumor Type": [], 
	            "FDA Approved Drugs in Other Tumor Type": []
	        };
	  		TumorType.getFromServer().success(function(data) {
	  			// var _data = [];
	  			// for (var i = 0, dataL = data.length; i < dataL; i++) {
	  			// 	_data.push({name: data[i].name});
	  			// };
		    	$scope.tumorTypes = data;
		    });

	  	};
    	
    	$scope.search = function() {
    		var params = {
    			'hugoSymbol': $scope.geneName,
    			'alteration': $scope.mutation,
    			'tumorType': $scope.selectedTumorType.name,
            	'alterationType': "MUTATION"
    		};

    		TumorType.postAnnotation(params).success(function(data) {
    			var annotation = {};

    			$scope.annotation = xml2json.parser(data).xml;

    			console.log(annotation);

    			// if(annotation.hasOwnProperty('annotation_summary')) {
    			// 	$scope.annotationSummary = annotation.annotation_summary;
    			// }

    			// if(annotation.hasOwnProperty('cancer_type')) {
    			// 	$scope.cancerType = annotation.cancer_type;
    			// }

    			// if(annotation.hasOwnProperty('gene_annotation')) {
    			// 	$scope.geneAnnotation = annotation.gene_annotation;
    			// }

    			// if(annotation.hasOwnProperty('variant_effect')) {
    			// 	$scope.variantEffect = annotation.variant_effect;
    			// }

    			console.log($scope);
    			$scope.rendering = false;
    		});
    	};

    	$scope.useExample = function() {
    		$scope.rendering = true;
    		$scope.geneName = "BRAF";
    		$scope.mutation = "V600E";
    		$scope.selectedTumorType = $scope.tumorTypes[$filter('getIndexByObjectNameInArray')($scope.tumorTypes, 'name', 'lung cancer')];
    		$scope.search();
    	}

    	$scope.getSelectedTumorType = function() {
    		console.log($scope.selectedTumorType);
    	}
  	}]);