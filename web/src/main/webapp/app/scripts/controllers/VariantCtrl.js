angular.module('webappApp')
  	.controller('VariantCtrl', [
        '$scope', 
        '$filter',
        '$location',
        'TumorType', 
        'SearchVariant', 
        function ($scope, $filter, $location, TumorType, SearchVariant) {

        'use strict';

        var changedAttr = ['nccn_guidelines', 'clinical_trial', 'sensitive_to', 'resistant_to', 'treatment', 'drug'];
        
	  	$scope.init = function () {

    		$scope.rendering = false;

            //Control UI-Bootstrap angularjs, open one at a time
            $scope.oneAtATime = true;

            $scope.isCollapsed = {};

            $scope.displayParts = {
		        geneAnnotation: {
		            displayName: 'Gene Annotation',
		            objectName: 'gene_annotation'
		        },
		        variantEffect: {
		            displayName: 'Variant Effect',
		            objectName: 'variant_effect'
		        }
		    };
	    
	    	$scope.summaryTableTitles = [
	            'Treatment Implications', 
	            'Clinical Trials', 
	            'Additional Information', 
	            'FDA Approved Drugs in Tumor Type', 
	            'FDA Approved Drugs in Other Tumor Type'];
	            
	        $scope.summaryTableTitlesContent = {
	            'Treatment Implications': [
	                'nccn_guidelines',
	                'standard_therapeutic_implications',
	                'investigational_therapeutic_implications'],
	            'Clinical Trials': ['clinical_trial'], 
	            'Additional Information': ['prevalence', 'prognostic_implications'], 
	            'FDA Approved Drugs in Tumor Type': [], 
	            'FDA Approved Drugs in Other Tumor Type': []
	        };

            $scope.specialAttr = ['investigational_therapeutic_implications', 'standard_therapeutic_implications'];

	  		TumorType.getFromServer().success(function(data) {
	  			$scope.tumorTypes = data;
                if($location.url() !== $location.path()) {
                    var urlVars = $location.search();
                    $scope.rendering = true;
                    if(urlVars.hasOwnProperty('hugoSymbol')){
                        $scope.geneName = urlVars.hugoSymbol;
                    }
                    if(urlVars.hasOwnProperty('alteration')){
                        $scope.mutation = urlVars.alteration;
                    }
                    if(urlVars.hasOwnProperty('tumorType')){
                        $scope.selectedTumorType = $scope.tumorTypes[$filter('getIndexByObjectNameInArray')($scope.tumorTypes, 'name', urlVars['tumorType'].toLowerCase() || '')];
                    }
                    $scope.search();
                }
		    });
	  	};
    	
        $scope.fdaApproved = function(drug) {
            if (typeof drug.fda_approved === 'string' && drug.fda_approved.toLowerCase() === 'yes'){
                return true;
            }else{
                return false;
            }
        }
        $scope.setCollapsed = function(trial, attr) {
            $scope.isCollapsed[trial.trial_id][attr] = !$scope.isCollapsed[trial.trial_id][attr];
        };

        $scope.getCollapseIcon = function(trial, attr) {
            if(typeof $scope.isCollapsed[trial.trial_id] === 'undefined' || $scope.isCollapsed[trial.trial_id][attr] ) {
                return "images/add.svg";
            }else{
                return "images/subtract.svg";
            }
        }

        $scope.generateTrial = function(trial) {
            var str = '';
            var purposeStr = '';

            if(typeof $scope.isCollapsed[trial.trial_id] === 'undefined') {
                $scope.isCollapsed[trial.trial_id] = {
                    purpose: true,
                    eligibility_criteria: true
                };
            }

            str += trial.hasOwnProperty('trial_id')?('TRIAL ID: ' + $scope.findRegex(trial.trial_id) + (trial.hasOwnProperty('phase')?(' / ' + trial.phase): '') + '<br/>'):'';
            str += trial.hasOwnProperty('title')?('TITLE: ' + trial.title + '<br/>'):'';
            
            // str += trial.hasOwnProperty('description')?('<br>' + $scope.findRegex(trial.description) + '<br/>'):'';
            return str;
        };

        $scope.generateNCCN = function(nccn) {
            var str = '<i>';

            str += nccn.hasOwnProperty('disease')?('Disease: ' + nccn.disease):'';
            str += nccn.hasOwnProperty('version')?(' Version: ' + nccn.version):'';
            str += nccn.hasOwnProperty('pages')?(' Pages: ' + nccn.pages):'';

            str += '</i>';
            str += nccn.hasOwnProperty('description')?('<br>' + $scope.findRegex(nccn.description) + '<br/>'):'';

            return str;
        };

        $scope.isArray = function(_var) {
            if(_var instanceof Array) {
                return true;
            }else {
                return false;
            }
        };

        $scope.isObject = function(_var) {
            if(typeof _var === 'object') {
                return true;
            }else {
                return false;
            }
        };
        
        function formatDatum(value, key) {
            if($scope.isArray(value) || (!$scope.isArray(value) && $scope.isObject(value) && changedAttr.indexOf(key) !== -1)) {
                if(!$scope.isArray(value) && $scope.isObject(value) && changedAttr.indexOf(key) !== -1) {
                    value = [value];
                }

                for (var i = 0; i < value.length; i++) {
                    value[i] = formatDatum(value[i], i);
                }
            }else if($scope.isObject(value)) {
                for(var _key in value) {
                    value[_key] = formatDatum(value[_key], _key);
                }
            }
            
            return value;
        }
    
        $scope.displayProcess = function(str) {
            var specialUpperCasesWords = ['NCCN'];
            var specialLowerCasesWords = ['of', 'for'];

            str = str.replace(/_/g, ' ');
            str = str.replace(
                /\w\S*/g,
                function(txt) {
                    var _upperCase = txt.toUpperCase(),
                        _lowerCase = txt.toLowerCase();

                    if( specialUpperCasesWords.indexOf(_upperCase) !== -1 ) {
                        return _upperCase;
                    }

                    if( specialLowerCasesWords.indexOf(_lowerCase) !== -1 ) {
                        return _lowerCase;
                    }

                    return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
                }
            );
            return str;
        };

        $scope.findRegex = function(str) {

            if(typeof str === 'string' && str !== '') {
                var regex = [/PMID:\s*([0-9]+,*\s*)+/ig, /NCT[0-9]+/ig],
                    links = ['http://www.ncbi.nlm.nih.gov/pubmed/',
                             'http://clinicaltrials.gov/show/'];
                for (var j = 0, regexL = regex.length; j < regexL; j++) {
                    var result = str.match(regex[j]);

                    if(result) {
                        var uniqueResult = result.filter(function(elem, pos) {
                            return result.indexOf(elem) === pos;
                        }); 
                        for(var i = 0, resultL = uniqueResult.length; i < resultL; i++) {
                            var _datum = uniqueResult[i];
                            
                            switch(j) {
                                case 0:
                                    var _number = _datum.split(':')[1].trim();
                                    _number = _number.replace(/\s+/g, '');
                                    str = str.replace(new RegExp(_datum, 'g'), '<a class="withUnderScore" target="_blank" href="'+ links[j] + _number+'">' + _datum + '</a>');
                                    break;
                                default:
                                    str = str.replace(_datum, '<a class="withUnderScore" target="_blank" href="'+ links[j] + _datum+'">' + _datum + '</a>');
                                    break;
                            }
                            
                        }
                    }
                }
            }
            return str;
        };

    	$scope.search = function() {
    		var params = {'alterationType': 'MUTATION'};
            var paramsContent = {
                'hugoSymbol': 'geneName',
                'alteration': 'mutation'
            }

            for (var key in paramsContent) {
                if($scope.hasOwnProperty(paramsContent[key])) {
                    params[key] = $scope[paramsContent[key]];
                }
            }
            if($scope.hasOwnProperty('selectedTumorType')) {
                params['tumorType'] = $scope.selectedTumorType.name;
            }                

    		// SearchVariant.annotationFromFile(params).success(function(data) {
            SearchVariant.postAnnotation(params).success(function(data) {
    			var annotation = {};

    			annotation = xml2json.parser(data).xml;

                for(var key in annotation) {
                    annotation[key] = formatDatum(annotation[key], key);
                }

                $scope.annotation = annotation;
    			$scope.rendering = false;
    		});
    	};

    	$scope.useExample = function() {
    		$scope.rendering = true;
    		$scope.geneName = 'BRAF';
    		$scope.mutation = 'V600E';
    		$scope.selectedTumorType = $scope.tumorTypes[$filter('getIndexByObjectNameInArray')($scope.tumorTypes, 'name', 'lung cancer')];
    		$scope.search();
    	};
  	}]);