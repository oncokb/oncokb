'use strict';

/**
 * @ngdoc directive
 * @name webappApp.directive:reportView
 * @description
 * # reportView
 */
angular.module('webappApp')
  	.directive('reportView', function () {

  		function link($scope, $element, $attrs) {
  			$scope.isObject = function(obj) {
  				return angular.isObject(obj) && !angular.isArray(obj);
  			};
  			$scope.isArray = function(obj) {
  				return angular.isArray(obj);
  			};
  			$scope.isString = function(obj) {
  				return angular.isString(obj);
  			};
  			$scope.show = function(key, value) {
  				var disabledKey = ['description', 'trial'],
  					str = '';

  				if(disabledKey.indexOf(key) === -1) {
  					str += upperFirstLetter(key) + ': ' + $scope.findRegex(value);
  				}else {
  					str += $scope.findRegex(value);
  				}
    				str += '<br/>';
    				return str;
  			};
        $scope.resultSummaryInfoHeader = ['Gene Name', 'Mutation', 
          'ALteration Type', 'Mutation Frequency',
          'Tumor Type Drugs', 'Non-Tumor Type Drugs', 
          'Clinical Trails'];
        
        $scope.resultSummaryInfoContent = ['geneName','mutation',
          'alterType','mutationFreq','tumorTypeDrugs',
          'nonTumorTypeDrugs','hasClinicalTrial'];

        $scope.extraInfoTitles = ['PUBLICATIONS',
          'ADDITIONAL INFORMATION',
          'GENE REGIONS PASSING QC',
          'PERFORMING SITE'];

        $scope.extraInfoContents = ['Please visit http://ncbi.nlm.nih.gov and enter the PMID number to retrieve reference(s) cited in patient report.',
          'sing the Ion Torrent PGM sequencer). The current test interrogates the most commonly mutated exons in 34 genes. The genes (listed below) were selected based on actionability of mutations identified in those genes using currently available evidence from national and international guidelines and literature. Actionability is defined as information a clinician can use to help guide diagnosis, prognosis and/or treatment strategy for a patient. Results of the test should be correlated with clinical findings. Clinical trial information provided in this report is solely for informational purposes for the physician and does not constitute any endorsement or a recommendation for enrollment of patients in any trial by Quest Diagnostics, its affiliates, or its employees.<br/>Only mutations present in the interrogated exons of the genes are reported. The test does not identify any mutations present outside the interrogated regions, or larger rearrangements, or copy number changes (amplifications or whole exon/gene deletions). Normal population variations and synonymous single nucleotide polymorphisms (SNPs) are not included in this report (unless strong evidence is available indicating effect on splicing and impacting protein function). This test has sensitivity for detecting 5% SNVs and 10% INDEL mutated sequences in a background of non-mutated DNA sequence. The performance characteristics can change based on adequacy of tumor tissue or pre-analytical variables (This test was validated on FFPE, bone marrow and whole blood.)<br/>The genes examined are AKT1, ALK, BRAF, CTNNB1, DDR2, EGFR, ERBB2, ERBB4, FBXW7, FGFR2, FGFR3, FGFR4, FOXL2, GNA11, GNAQ, GNAS, HRAS, IDH1, IDH2, KIT, KRAS, MAP2K1, MET, NOTCH1, NRAS, PDGFRA, PIK3CA, PIK3R1, PTCH1, PTEN, RET, SMO, STK11, TP53.<br/>The Memorial Sloan Kettering Cancer Center name and logo are registered trademarks owned by Memorial Sloan Kettering Cancer Center, and used by Quest under license. Memorial Sloan Kettering Cancer Center makes available to Quest certain information from its genetic database to assist Quest in providing this service. This report provided by Quest, is the sole responsibility of Quest, and no relationship is created between the patient or referring physician/institution and Memorial Sloan Kettering Cancer Center or its physicians. This test was developed and its performance characteristics have been determined by Quest Diagnostics Nichols Institute, San Juan Capistrano. Performance characteristics refer to the analytical performance of the test.',
          'All relevant gene regions passed our minimal acceptability QC criteria for reporting results.<br/>This data was reviewed and interpreted by Feras M Hantash, PhD, DABMG, FACMG,',
          'EZ QUEST DIAGNOSTICS/NICHOLS SJC, 33608 ORTEGA HWY, SAN JUAN CAPISTRANO, CA 92675-2042 Laboratory Director: JON NAKAMOTO, MD PHD, CLIA: 05D0643352'];
  		}

      function stringProcess(str) {
        str = str.replace(/(\r\n|\n|\r)/gm, '<br/>');
        return str;
      }

  		function upperFirstLetter(str){
  			str = str.replace('_', ' ');
  			return str.charAt(0).toUpperCase() + str.substr(1).toLowerCase();
  		}
  		return {
	      	templateUrl: '/views/reportView.html',
	      	restrict: 'E',
	      	link: link,
      		scope: {
      			data: '=reportParams',
      			titles: '=summaryTableTitles',
      			params: '=reportMatchedParams',
      			findRegex: '='
	      	}
    	};
	});
