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
        function Levels(){
            return {
                '1': {},
                '2a': {},
                '2b': {},
                '3': {},
                '4': {},
                'r1': {},
                'r2': {},
                'r3': {}
            };
        }

        function Gene(geneName) {
            this.name = geneName || '';
            this.numMutations = 0;
            this.numCancerTypes = 0;
            this.numVCConbinations = 0;
            this.numClinicalTrials = 0;
            this.SS = {};
            this.SR = {};
            this.IS = {};
            this.IR = {};

            this.therapyLevels = new Levels();
            this.treatmentLevels = new Levels();

            this.highestLevelS = 'N/A'; //highest level of sensitivity
            this.highestLevelR = 'N/A'; //highest level of resistance

            this.hasBackground = false;
            this.hasSummary = false;
            this.hasStanderTherapy = false;
            this.hasInvestigationalTherapy = false;

            this.mutations = [];
            this.tumors = [];
            this.mtMap = {}; //mutation tumor mapping

            this.trials = {};
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
                'STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY': 'SS',
                'STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE': 'SR',
                'INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY': 'IS',
                'INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE': 'IR'
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
                                        if(e.hasOwnProperty('treatments') && e.hasOwnProperty('name') && e.name){
                                            if(!gene[therapies[tumorAttrs]].hasOwnProperty(e.name)){
                                                gene[therapies[tumorAttrs]][e.name] = [];
                                            }
                                            gene[therapies[tumorAttrs]][e.name].push({
                                                gene: gene.name,
                                                mutation: mutation,
                                                tumorType: tumor,
                                                therapy: e
                                            });
                                        }

                                        if(e.hasOwnProperty('level')){
                                            var _level = e.level.level || '';
                                            if(sensitTherapies.indexOf(tumorAttrs) !== -1){
                                                gene.highestLevelS = compare(_level, gene.highestLevelS, 'sensitivity');
                                            }else{
                                                gene.highestLevelR = compare(_level, gene.highestLevelR, 'resistance');
                                            }
                                            if(!gene.therapyLevels.hasOwnProperty(_level)){
                                                gene.therapyLevels[_level] = {};
                                            } if(!gene.treatmentLevels.hasOwnProperty(_level)){
                                                gene.treatmentLevels[_level] = {};
                                            }
                                            if(!gene.therapyLevels[_level].hasOwnProperty(e.name)){
                                                gene.therapyLevels[_level][e.name]=[];
                                            }
                                            gene.therapyLevels[_level][e.name].push({
                                                gene: gene.name,
                                                mutation: mutation,
                                                tumorType: tumor,
                                                type: tumorAttrs
                                            });
                                            e.treatments.forEach(function(e1){
                                                if(!gene.treatmentLevels[_level].hasOwnProperty(e1.name)){
                                                    gene.treatmentLevels[_level][e1.name]=[];
                                                }
                                                gene.treatmentLevels[_level][e1.name].push({
                                                    gene: gene.name,
                                                    mutation: mutation,
                                                    tumorType: tumor,
                                                    type: tumorAttrs
                                                });
                                            });
                                        }else{
                                            console.log(gene.name, mutation, tumor, tumorAttrs);
                                        }
                                    }else{
                                        console.log(gene.name, mutation, tumor, tumorAttrs, 'null tumorAttrs');
                                    }
                                });
                            }

                            if(tumorAttrs === 'trials'){
                                tumorAttrsO.forEach(function(trial){
                                    if(!gene.trials.hasOwnProperty(trial)){
                                        gene.trials[trial] = [];
                                    }
                                    gene.trials[trial].push({
                                        gene: gene.name,
                                        mutation: mutation,
                                        tumorType: tumor
                                    });
                                });
                            }
                        }
                        gene.mtMap[mutation].push(tumor);
                        combination++;
                    }

                }

                gene.mutations.sort();
                gene.tumors.sort();
                gene.trials.keys = Object.keys(gene.trials).sort();
                gene.trials.num = gene.trials.keys.length;
                gene.numMutations = gene.mutations.length;
                gene.numCancerTypes = gene.tumors.length;
                gene.numVCConbinations = combination;
                for(var key in therapies){
                    gene[therapies[key]].keys = Object.keys(gene[therapies[key]]).sort();
                    gene[therapies[key]].num = gene[therapies[key]].keys.length;
                }
                for(var level in gene.treatmentLevels){
                    gene.treatmentLevels[level].keys = Object.keys(gene.treatmentLevels[level]).sort();
                    gene.treatmentLevels[level].num = gene.treatmentLevels[level].keys.length;
                }
                for(var level in gene.therapyLevels){
                    gene.therapyLevels[level].keys = Object.keys(gene.therapyLevels[level]).sort();
                    gene.therapyLevels[level].num = gene.therapyLevels[level].keys.length;
                }
                if(gene.name === 'HRAS' || gene.name === 'BRAF' ){
                    console.log(gene);
                }
                genes.push(gene);
            }

            $scope.genes = genes;
            //console.log(genes);
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
            DTColumnDefBuilder.newColumnDef(10)
        ];
        $scope.dtColumnsLevels =  [
            DTColumnDefBuilder.newColumnDef(0),
            DTColumnDefBuilder.newColumnDef(1),
            DTColumnDefBuilder.newColumnDef(2),
            DTColumnDefBuilder.newColumnDef(3),
            DTColumnDefBuilder.newColumnDef(4),
            DTColumnDefBuilder.newColumnDef(5),
            DTColumnDefBuilder.newColumnDef(6),
            DTColumnDefBuilder.newColumnDef(7),
            DTColumnDefBuilder.newColumnDef(8)
        ];

        $scope.therapyCategories = ['SS','SR','IS','IR'];
        $scope.levelCategories = ['1', '2a', '2b', '3', '4', 'r1', 'r2', 'r3'];
        $scope.data = [];
        $scope.init = init;
    });
