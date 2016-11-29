'use strict';

/**
 * @ngdoc function
 * @name oncokbApp.controller:DatasummaryCtrl
 * @description
 * # DatasummaryCtrl
 * Controller of the oncokbApp
 */
angular.module('oncokbApp')
    .controller('DatasummaryCtrl', function($scope, DTColumnDefBuilder, DTOptionsBuilder, DTInstances, DatabaseConnector, OncoKB, $timeout, cbioDownloadUtil, _) {
        function Levels() {
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
            this.SS = {};
            this.SR = {};
            this.IS = {};
            this.IR = {};

            this.therapyLevels = new Levels();
            this.treatmentLevels = new Levels();

            this.highestLevelS = 'N/A'; // highest level of sensitivity
            this.highestLevelR = 'N/A'; // highest level of resistance

            this.oncoGenicVariants = {
                true: {
                    keys: [],
                    num: 0
                },
                false: {
                    keys: [],
                    num: 0
                },
                unknown: {
                    keys: [],
                    num: 0
                }
            };

            this.hasBackground = false;
            this.hasSummary = false;
            this.hasStanderTherapy = false;
            this.hasInvestigationalTherapy = false;

            this.mutations = {
                keys: [],
                num: 0
            };
            this.positionedMutations = {};
            this.tumors = {
                keys: [],
                num: 0
            };
            this.mtMap = {
                keys: [],
                num: 0
            }; // mutation tumor mapping
            this.positionedMtMap = {
                keys: [],
                num: 0
            }; // positioned mutation and tumor mapping

            this.trials = {};
        }

        function init() {
            $scope.rendering = true;
            $timeout(function() {
                // if(!OncoKB.dataSummaryGenes){
                DatabaseConnector.getDataSummary().then(function(result) {
                    if (result && result.error) {
                        $scope.data = {};
                    } else {
                        $scope.data = result;
                    }
                    console.log(result);
                    parseGene(result);
                });
                // }else{
                //    $scope.genes = OncoKB.dataSummaryGenes;
                // }
            }, 100);
        }

        function parseGene(data) {
            var genes = [];
            var therapies = {
                STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY: 'SS',
                STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE: 'SR',
                INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY: 'IS',
                INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_RESISTANCE: 'IR'
            };
            var sensitTherapies = [
                'STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY',
                'INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY'
            ];
            var positionedMutationRegex = /^([a-zA-Z]+\d+)[a-zA-Z]*$/im;
            for (var key in data) {
                if (data.hasOwnProperty(key)) {
                    var gene = new Gene(key);
                    var datum = data[key];

                    if (datum.hasOwnProperty('attrs')) {
                        gene.hasSummary = datum.attrs.hasSummary === 'TRUE' ? 'Y' : 'N';
                        gene.hasBackground = datum.attrs.hasBackground === 'TRUE' ? 'Y' : 'N';

                        delete datum.attrs;
                    }

                    for (var mutation in datum) {
                        if (datum.hasOwnProperty(mutation)) {
                            var mutationO = datum[mutation]; // mutation object
                            var position = positionedMutationRegex.exec(mutation);
                            var positionedMutation = '';

                            gene.mutations.keys.push(mutation);
                            gene.mtMap[mutation] = [];

                            if (position && position[1]) {
                                positionedMutation = position[1];
                            }

                            if (positionedMutation) {
                                if (!gene.positionedMutations.hasOwnProperty(positionedMutation)) {
                                    gene.positionedMutations[positionedMutation] = [];
                                    gene.positionedMtMap[positionedMutation] = [];
                                }
                                gene.positionedMutations[positionedMutation].push(position[0]);
                            }

                            if (mutationO.hasOwnProperty('attrs')) {
                                if (!gene.mutations.hasOwnProperty(mutation)) {
                                    var effects = _.union([], mutationO.attrs.mutationEffect);

                                    gene.mutations[mutation] = {
                                        effect: effects.join(', '),
                                        oncoGenic: Number(mutationO.attrs.oncoGenic),
                                        type: mutationO.attrs.mutationType || 'MUTATION'
                                    };

                                    if ([1, 2].indexOf(gene.mutations[mutation].oncoGenic) !== -1) {
                                        gene.oncoGenicVariants.true.keys.push(mutation);
                                    } else if (gene.mutations[mutation].oncoGenic === -1) {
                                        gene.oncoGenicVariants.false.keys.push(mutation);
                                    } else if (gene.mutations[mutation].oncoGenic === 0) {
                                        gene.oncoGenicVariants.unknown.keys.push(mutation);
                                    }
                                }
                                delete mutationO.attrs;
                            }

                            for (var tumor in mutationO) {
                                if (mutationO.hasOwnProperty(tumor)) {
                                    var tumorO = mutationO[tumor];

                                    if (gene.tumors.keys.indexOf(tumor) === -1) {
                                        gene.tumors.keys.push(tumor);
                                    }

                                    for (var tumorAttrs in tumorO) {
                                        if (tumorO.hasOwnProperty(tumorAttrs)) {
                                            var tumorAttrsO = tumorO[tumorAttrs];
                                            if (therapies.hasOwnProperty(tumorAttrs)) {
                                                tumorAttrsO.forEach(function(e) {
                                                    if (e) {
                                                        if (e.hasOwnProperty('treatments') && e.hasOwnProperty('name') && e.name) {
                                                            if (!gene[therapies[tumorAttrs]].hasOwnProperty(e.name)) {
                                                                gene[therapies[tumorAttrs]][e.name] = [];
                                                            }
                                                            gene[therapies[tumorAttrs]][e.name].push({
                                                                gene: gene.name,
                                                                mutation: mutation,
                                                                tumorType: tumor,
                                                                therapy: e
                                                            });
                                                        }

                                                        if (e.hasOwnProperty('level')) {
                                                            var _level = e.level.level || '';
                                                            if (sensitTherapies.indexOf(tumorAttrs) === -1) {
                                                                gene.highestLevelR = compare(_level, gene.highestLevelR, 'resistance');
                                                            } else {
                                                                gene.highestLevelS = compare(_level, gene.highestLevelS, 'sensitivity');
                                                            }
                                                            if (!gene.therapyLevels.hasOwnProperty(_level)) {
                                                                gene.therapyLevels[_level] = {};
                                                            }
                                                            if (!gene.treatmentLevels.hasOwnProperty(_level)) {
                                                                gene.treatmentLevels[_level] = {};
                                                            }
                                                            if (!gene.therapyLevels[_level].hasOwnProperty(e.name)) {
                                                                gene.therapyLevels[_level][e.name] = [];
                                                            }
                                                            gene.therapyLevels[_level][e.name].push({
                                                                gene: gene.name,
                                                                mutation: mutation,
                                                                tumorType: tumor,
                                                                type: tumorAttrs
                                                            });
                                                            e.treatments.forEach(function(e1) {
                                                                if (!gene.treatmentLevels[_level].hasOwnProperty(e1.name)) {
                                                                    gene.treatmentLevels[_level][e1.name] = [];
                                                                }
                                                                gene.treatmentLevels[_level][e1.name].push({
                                                                    gene: gene.name,
                                                                    mutation: mutation,
                                                                    tumorType: tumor,
                                                                    type: tumorAttrs
                                                                });
                                                            });
                                                        } else {
                                                            console.log(gene.name, mutation, tumor, tumorAttrs);
                                                        }
                                                    } else {
                                                        console.log(gene.name, mutation, tumor, tumorAttrs, 'null tumorAttrs');
                                                    }
                                                });
                                            } else if (tumorAttrs === 'trials') {
                                                tumorAttrsO.forEach(function(trial) {
                                                    if (!gene.trials.hasOwnProperty(trial)) {
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
                                    }
                                    if (positionedMutation && gene.positionedMtMap[positionedMutation].indexOf(tumor) === -1) {
                                        gene.positionedMtMap[positionedMutation].push(tumor);
                                        gene.positionedMtMap.keys.push(positionedMutation + ' ~ ' + tumor);
                                        gene.positionedMtMap.num++;
                                    }
                                    gene.mtMap[mutation].push(tumor);
                                    gene.mtMap.keys.push(mutation + ' ~ ' + tumor);
                                    gene.mtMap.num++;
                                }
                            }
                        }
                    }
                    gene.mutations.keys.sort();
                    gene.mutations.num = gene.mutations.keys.length;
                    gene.tumors.keys.sort();
                    gene.tumors.num = gene.tumors.keys.length;
                    gene.oncoGenicVariants.true.keys.sort();
                    gene.oncoGenicVariants.false.keys.sort();
                    gene.oncoGenicVariants.unknown.keys.sort();
                    gene.trials.keys = Object.keys(gene.trials).sort();
                    gene.oncoGenicVariants.true.num = gene.oncoGenicVariants.true.keys.length;
                    gene.oncoGenicVariants.false.num = gene.oncoGenicVariants.false.keys.length;
                    gene.oncoGenicVariants.unknown.num = gene.oncoGenicVariants.unknown.keys.length;
                    gene.trials.num = gene.trials.keys.length;
                    gene.positionedMutations.keys = Object.keys(gene.positionedMutations).sort();
                    gene.positionedMutations.num = gene.positionedMutations.keys.length;

                    _.each(therapies, function(item) {
                        gene[item].keys = Object.keys(gene[item]).sort();
                        gene[item].num = gene[item].keys.length;
                    });

                    _.each(gene.treatmentLevels, function(item) {
                        gene[item].keys = Object.keys(gene[item]).sort();
                        gene[item].num = gene[item].keys.length;
                    });

                    _.each(gene.therapyLevels, function(item) {
                        gene[item].keys = Object.keys(gene[item]).sort();
                        gene[item].num = gene[item].keys.length;
                    });
                    if (gene.name === 'HRAS' || gene.name === 'BRAF') {
                        console.log(gene);
                    }
                    genes.push(gene);
                }
            }

            $scope.genes = genes;
            OncoKB.dataSummaryGenes = genes;
            DTInstances.getLast().then(function() {
                $scope.rendering = false;
            });
            // console.log(genes);
        }

        function compare(n, o, type) {
            n = String(n);
            o = String(o);

            var levelS = ['4', '3b', '3a', '3', '2b', '2a', '1'];
            var levelR = ['r3', 'r2', 'r1'];
            var levels = type === 'sensitivity' ? levelS : levelR;
            var nI = levels.indexOf(n);
            var oI = levels.indexOf(o);

            return nI < oI ? o : n;
        }

        /**
         * Use download util from cbioportal created by Onur
         *
         * @param {string} type download file type
         * @param {integer} tableId 0: all from three tables; 1: main table; 2:
         */
        function download(type, tableId) {
            var content = '';
            var seperator;
            var downloadOpts = {
                filename: 'download.',
                contentType: 'text/plain;charset=utf-8',
                preProcess: false
            };

            var tableKeys = {
                common: {
                    header: [{
                        attr: 'name',
                        display: 'Gene'
                    }]
                },
                0: {
                    name: 'All information'
                },
                1: {
                    name: 'Main information',
                    header: [{
                        attr: 'hasSummary',
                        display: 'Summary'
                    }, {
                        attr: 'hasBackground',
                        display: 'Background'
                    }, {
                        attr: 'mutations.num',
                        display: '# Variants'
                    }, {
                        attr: 'oncoGenicVariants.true.num',
                        display: '# Oncogenic variants'
                    }, {
                        attr: 'oncoGenicVariants.false.num',
                        display: '# Non-oncogenic variants'
                    }, {
                        attr: 'positionedMutations.num',
                        display: '# Positioned variants'
                    }, {
                        attr: 'tumors.num',
                        display: '# Cancer type'
                    }, {
                        attr: 'mtMap.num',
                        display: '# Combination between variant and cancer type'
                    }, {
                        attr: 'positionedMtMap.num',
                        display: '# Combination between positioned variant and cancer type'
                    }, {
                        attr: 'SS.num',
                        display: '# Standard implications for sensitivity to therapy'
                    }, {
                        attr: 'SR.num',
                        display: '# Standard implications for resistance to therapy'
                    }, {
                        attr: 'IS.num',
                        display: '# Investigational implications for sensitivity to therapy'
                    }, {
                        attr: 'IR.num',
                        display: '# Investigational implications for resistance to therapy'
                    }, {
                        attr: 'trials.num',
                        display: '# Trials'
                    }]
                },
                2: {
                    name: 'Therapy levels',
                    header: [{
                        attr: 'therapyLevels.1.num',
                        display: '# Therapy L-1'
                    }, {
                        attr: 'therapyLevels.2a.num',
                        display: '# Therapy L-2A'
                    }, {
                        attr: 'therapyLevels.2b.num',
                        display: '# Therapy L-2B'
                    }, {
                        attr: 'therapyLevels.3.num',
                        display: '# Therapy L-3'
                    }, {
                        attr: 'therapyLevels.4.num',
                        display: '# Therapy L-4'
                    }, {
                        attr: 'therapyLevels.r1.num',
                        display: '# Therapy L-R1'
                    }, {
                        attr: 'therapyLevels.r2.num',
                        display: '# Therapy L-R2'
                    }, {
                        attr: 'therapyLevels.r3.num',
                        display: '# Therapy L-R3'
                    }]
                },
                3: {
                    name: 'Treatment levels',
                    header: [{
                        attr: 'treatmentLevels.1.num',
                        display: '# Treatment L-1'
                    }, {
                        attr: 'treatmentLevels.2a.num',
                        display: '# Treatment L-2A'
                    }, {
                        attr: 'treatmentLevels.2b.num',
                        display: '# Treatment L-2B'
                    }, {
                        attr: 'treatmentLevels.3.num',
                        display: '# Treatment L-3'
                    }, {
                        attr: 'treatmentLevels.4.num',
                        display: '# Treatment L-4'
                    }, {
                        attr: 'treatmentLevels.r1.num',
                        display: '# Treatment L-R1'
                    }, {
                        attr: 'treatmentLevels.r2.num',
                        display: '# Treatment L-R2'
                    }, {
                        attr: 'treatmentLevels.r3.num',
                        display: '# Treatment L-R3'
                    }]
                }
            };
            var headers = [];

            if (tableKeys.hasOwnProperty(tableId.toString())) {
                var _headers = [];
                switch (type) {
                case 'csv':
                    downloadOpts.filename += 'csv';
                    seperator = ',';
                    break;
                case 'tsv':
                    downloadOpts.filename += 'tsv';
                    seperator = '\t';
                    break;
                default:
                    downloadOpts.filename += 'tsv';
                    seperator = '\t';
                    break;
                }

                headers = tableKeys.common.header;
                switch (tableId) {
                case 0:
                    headers = headers.concat(tableKeys['1'].header, tableKeys['2'].header, tableKeys['3'].header);
                    break;
                case 1:
                    headers = headers.concat(tableKeys['1'].header);
                    break;
                case 2:
                    headers = headers.concat(tableKeys['2'].header);
                    break;
                case 3:
                    headers = headers.concat(tableKeys['3'].header);
                    break;
                default:
                    break;
                }

                console.log(headers);

                headers.forEach(function(header) {
                    _headers.push(header.display);
                });
                content += _headers.join(seperator);
                content += '\n';

                $scope.genes.forEach(function(gene) {
                    var _data = [];

                    headers.forEach(function(header) {
                        var __attrs = header.attr.split('.');
                        var __data = angular.copy(gene);

                        __attrs.forEach(function(__attr) {
                            __data = __data[__attr];
                        });

                        _data.push(__data);
                    });
                    content += _data.join(seperator);
                    content += '\n';
                });

                cbioDownloadUtil.initDownload(content, downloadOpts);
            }
        }

        $scope.dtOptions = DTOptionsBuilder
            .newOptions()
            .withDOM('ifrtlp')
            .withBootstrap();

        $scope.dtColumns = [
            DTColumnDefBuilder.newColumnDef(0),
            DTColumnDefBuilder.newColumnDef(1),
            DTColumnDefBuilder.newColumnDef(2),
            DTColumnDefBuilder.newColumnDef(3)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(4)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(5)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(6)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(7)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(8)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(9)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(10)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(11)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(12)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(13)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(14)
                .withOption('sType', 'num-html')
        ];
        $scope.dtColumnsLevels = [
            DTColumnDefBuilder.newColumnDef(0),
            DTColumnDefBuilder.newColumnDef(1)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(2)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(3)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(4)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(5)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(6)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(7)
                .withOption('sType', 'num-html'),
            DTColumnDefBuilder.newColumnDef(8)
                .withOption('sType', 'num-html')
        ];

        $scope.therapyCategories = ['SS', 'SR', 'IS', 'IR'];
        $scope.levelCategories = ['1', '2a', '2b', '3', '3a', '3b', '4', 'r1', 'r2', 'r3'];
        $scope.data = [];
        $scope.rendering = false;
        $scope.init = init;
        $scope.download = download;
    });
