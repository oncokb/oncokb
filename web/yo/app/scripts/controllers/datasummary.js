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
            this.numClinicalTrials = 0;
            this.numSS = 0;
            this.numSR = 0;
            this.numIS = 0;
            this.numIR = 0;

            this.levels = {
                '1': 0,
                '2a': 0,
                '2b': 0,
                '3': 0,
                '4': 0,
                'r1': 0,
                'r2': 0,
                'r3': 0
            };

            this.highestLevelS = 'N/A'; //highest level of sensitivity
            this.highestLevelR = 'N/A'; //highest level of resistance

            this.hasBackground = false;
            this.hasSummary = false;
            this.hasStanderTherapy = false;
            this.hasInvestigationalTherapy = false;

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
            var therapies = {
                'STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY': 'numSS',
                'STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE': 'numSR',
                'INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY': 'numIS',
                'INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE': 'numIR'
            };
            var sensitTherapies = [
                'STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY',
                'INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY'
            ];
            for(var key in data){
                var gene = new Gene(key);
                var datum = data[key];
                var combination = 0;

                if(datum.hasOwnProperty('attrs')){
                    gene.hasSummary = datum.attrs.hasSummary==="TRUE"?'Y':'N';
                    gene.hasBackground = datum.attrs.hasBackground==="TRUE"?'Y':'N';

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
                        var tumorO = mutationO[tumor];

                        if(gene.tumors.indexOf(tumor) === -1){
                            gene.tumors.push(tumor);
                        }

                        for(var tumorAttrs in tumorO){
                            var tumorAttrsO = tumorO[tumorAttrs];
                            if(therapies.hasOwnProperty(tumorAttrs)){
                                tumorAttrsO.forEach(function(e){
                                    if(e){
                                        if(e.hasOwnProperty('treatment')){
                                            gene[therapies[tumorAttrs]]++;
                                        }

                                        if(e.hasOwnProperty('level')){
                                            var _level = e.level.level || '';
                                            if(sensitTherapies.indexOf(tumorAttrs) !== -1){
                                                gene.highestLevelS = compare(_level, gene.highestLevelS, 'sensitivity');
                                            }else{
                                                gene.highestLevelR = compare(_level, gene.highestLevelR, 'resistance');
                                            }
                                            if(!gene.levels.hasOwnProperty(_level)){
                                                gene.levels[_level] = 0;
                                            }
                                            gene.levels[_level]++;
                                        }else{
                                            console.log(gene.name, mutation, tumor, tumorAttrs);
                                        }
                                    }else{
                                        console.log(gene.name, mutation, tumor, tumorAttrs, 'null tumorAttrs');
                                    }
                                });
                            }

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

        function compare(n, o, type){
            n = String(n);
            o = String(o);

            var levelS = ['4', '3', '2b', '2a', '1'];
            var levelR = ['r3', 'r2', 'r1'];
            var levels = type === "sensitivity"?levelS:levelR;
            var nI = levels.indexOf(n);
            var oI = levels.indexOf(o);

            if(nI < oI) {
                return o;
            }else {
                return n;
            }
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
            DTColumnDefBuilder.newColumnDef(5),
            DTColumnDefBuilder.newColumnDef(6),
            DTColumnDefBuilder.newColumnDef(7),
            DTColumnDefBuilder.newColumnDef(8),
            DTColumnDefBuilder.newColumnDef(9),
            DTColumnDefBuilder.newColumnDef(10),
            DTColumnDefBuilder.newColumnDef(11),
            DTColumnDefBuilder.newColumnDef(12),
            DTColumnDefBuilder.newColumnDef(13),
            DTColumnDefBuilder.newColumnDef(14),
            DTColumnDefBuilder.newColumnDef(15),
            DTColumnDefBuilder.newColumnDef(16),
            DTColumnDefBuilder.newColumnDef(17)
        ];

        $scope.data = [];
        $scope.init = init;
    });
