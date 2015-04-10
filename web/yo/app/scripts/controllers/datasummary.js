'use strict';

/**
 * @ngdoc function
 * @name oncokbApp.controller:DatasummaryCtrl
 * @description
 * # DatasummaryCtrl
 * Controller of the oncokbApp
 */
angular.module('oncokbApp')
    .controller('DatasummaryCtrl', function ($scope, DTColumnDefBuilder, DTOptionsBuilder, DatabaseConnector) {
      function Gene(geneName) {
        this.name = geneName || '';
        this.numMutations = 0;
        this.numCancerTypes = 0;
        this.numVCConbinations = 0;
        this.highestLevelS = 'N/A'; //highest level of sensitivity
        this.highestLevelR = 'N/A'; //highest level of resistance
        this.hasBackground = false;
        this.hasSummary = false;
        this.hasStanderTherapy = false;
        this.hasInvestigationalTherapy = false;
        this.numClinicalTrials = 0;
        this.mutations = [];
        this.tumors = [];
        this.mtMap = {}; //mutation tumor mapping
      }

      function init(){
        DatabaseConnector.getDataSummary().then(function(result){
          if(result && result.error){
            $scope.data = {};
          }else{
            $scope.data = result;
          }
          console.log(result);
          parseGene(result);
        });
      }

      function parseGene(data){
        var genes = [];
        for(var key in data){
          var gene = new Gene(key);
          var datum = data[key];
          var combination = 0;

          if(datum.hasOwnProperty('attrs')){
            gene.hasSummary = datum.attrs.hasSummary==="TRUE"?true:false;
            gene.hasBackground = datum.attrs.hasBackground==="TRUE"?true:false;

            delete datum.attrs;
          }

          for(var mutation in datum) {
            var mutationO = datum[mutation]; //mutation object

            gene.mutations.push(mutation);
            gene.mtMap[mutation] = [];

            if(mutationO.hasOwnProperty('attrs')){
              delete mutationO.attrs;
            }

            for(var tumor in mutationO){
              if(gene.tumors.indexOf(tumor) === -1){
                gene.tumors.push(tumor);
              }
              gene.mtMap[mutation].push(tumor);
              combination++;
            }

          }

          gene.numMutations = gene.mutations.length;
          gene.numCancerTypes = gene.tumors.length;
          gene.numVCConbinations = combination;

          genes.push(gene);
        }

        $scope.genes = genes;
        console.log(genes);
      }

      $scope.dtOptions = DTOptionsBuilder
          .newOptions()
          .withDOM('ifrtlp')
          .withBootstrap();

      $scope.dtColumns =  [
        DTColumnDefBuilder.newColumnDef(0),
        DTColumnDefBuilder.newColumnDef(1),
        DTColumnDefBuilder.newColumnDef(2),
        DTColumnDefBuilder.newColumnDef(3),
        DTColumnDefBuilder.newColumnDef(4),
        DTColumnDefBuilder.newColumnDef(5)
      ];
      $scope.data = [];
      $scope.init = init;
    });
