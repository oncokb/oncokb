'use strict';

/**
 * @ngDOC service
 * @name oncokbApp.jspdf
 * @description
 * # jspdf
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .service('jspdf', function(PDF, mainUtils) {
        // AngularJS will instantiate a singleton by calling "new" on this function

        var FONT = ['Times', 'Roman'];

        // Font size for different levels, 4 is default
        var SIZE = {
            1: 23,
            2: 18,
            3: 13,
            4: 11
        };
        var MARGIN = 0.5;
        var Y = MARGIN;
        var DOC = '';
        function create(data) {
            Y = MARGIN;
            DOC = new PDF('p', 'in', 'letter'); // inches on a 8.5 x 11 inch sheet.
            gene(data);
            DOC.save(data.name + '.pdf');
        }

        function gene(data) {
            // Don't want to preset font, size to calculate the lines?
            // .splitTextToSize(text, maxsize, options)
            // allows you to pass an object with any of the following:
            // {
            //  'fontSize': 12
            //  , 'fontStyle': 'Italic'
            //  , 'fontName': 'Times'
            // }
            // Without these, .splitTextToSize will use current / default
            // font Family, Style, Size.

            drawFunc('Gene: ' + data.name, '1', 'Bold');
            drawFunc('Summary:', '2', 'Bold');
            drawFunc(data.summary);
            drawFunc('Background:', '2', 'Bold');
            drawFunc(data.background);

            mutation(data.mutations);
        }

        function drawFunc(data, fontSize, fontStyle) {
            var size = SIZE['4'];
            var style = FONT[1];

            if (!angular.isUndefined(fontSize)) {
                size = SIZE[fontSize.toString()];
            }

            if (!angular.isUndefined(fontStyle)) {
                style = fontStyle;
            }

            var lines = linesFunc(data, size, style);
            var _y = Y + ((lines.length * 1.1) + 0.5) * size / 72;
            if (_y > 10) {
                DOC.addPage();
                Y = MARGIN;
            }
            DOC.text(0.5, Y + size / 72, lines);
            Y += ((lines.length * 1.1) + 0.5) * size / 72;
        }

        function linesFunc(data, size, fontStyle) {
            var lines = DOC.setFont(FONT[0], fontStyle)
                .setFontSize(size)
                .splitTextToSize(data, 7.5);
            return lines;
        }

        function mutation(mutations) {
            if (angular.isArray(mutations)) {
                mutations.forEach(function(e) {
                    drawFuncMutation(e);
                    tumorType(e.tumors);
                });
            }
        }

        function drawFuncMutation(mutation) {
            drawFunc('Mutation: ' + mutation.name, '2', 'Bold');
            if (mutation.mutation_effect.oncogenic) {
                drawFunc('Oncogenic: ' + mutation.mutation_effect.oncogenic, '4', 'Bold');
            }
            if (mutation.mutation_effect.effect) {
                drawFunc('Mutation effect: ' + mutation.mutation_effect.effect, '4', 'Bold');
            }
            if (mutation.mutation_effect.description) {
                drawFunc('Description of mutation effect:', '4', 'Bold');
                drawFunc(mutation.mutation_effect.description);
            }
            if (mutation.mutation_effect.short) {
                drawFunc('Additional information: ' + mutation.mutation_effect.short, '4', 'Bold');
            }
        }

        function tumorType(tumorTypes) {
            if (angular.isArray(tumorTypes)) {
                tumorTypes.forEach(function(e) {
                    drawFuncTumorType(e);
                });
            }
        }
        function drawFuncImplications(tumor) {            
            var keys = ['diagnostic', 'prognostic'];
            _.each(keys, function(key) {
                var hasContent = tumor[key].level && tumor[key].description && tumor[key].short;
                if (hasContent) {
                    drawFunc(key[0].toUpperCase() + key.slice(1) + ' implications:', '4', 'Bold');
                    if (tumor[key].level) {
                        drawFunc('Level:', '4', 'Bold');
                        drawFunc(tumor[key].level);
                    }
                    if (tumor[key].description) {
                        drawFunc('description:', '4', 'Bold');
                        drawFunc(tumor[key].description);
                    }
                    if (tumor[key].short) {
                        drawFunc('Additional information:', '4', 'Bold');
                        drawFunc(tumor[key].short);
                    }
                }
            });
            
        }
        function drawFuncTumorType(tumor) {
            drawFunc('Tumor Type: ' + mainUtils.getCancerTypesName(tumor.cancerTypes), '3', 'Bold');
            if (tumor.summary) {
                drawFunc('Summary:', '4', 'Bold');
                drawFunc(tumor.summary);
            }
            if (tumor.diagnosticSummary) {
                drawFunc('Diagnostic Summary:', '4', 'Bold');
                drawFunc(tumor.diagnosticSummary);
            }
            if (tumor.prognosticSummary) {
                drawFunc('Prognostic Summary:', '4', 'Bold');
                drawFunc(tumor.prognosticSummary);
            }
            drawFuncImplications(tumor);
            tumor.TIs.forEach(function(e) {
                var title = '';

                if (e.type) {
                    title = 'Sensitive to';
                } else {
                    title = 'Resistant to';
                }

                if (e.description || e.treatments) {
                    drawFunc(e.name, '3', 'Bold');
                    if (e.description) {
                        drawFunc(e.description);
                    }

                    if (e.treatments) {
                        e.treatments.forEach(function(e1) {
                            therapyFunc(e1, title);
                        });
                    }
                }
            });
        }

        function therapyFunc(therapy, title) {
            if (therapy.name && therapy.name.length > 0) {
                drawFunc(title + ': ' + therapy.name.map(function(therapy) {
                    return therapy.map(function(drug) {
                        return drug.drugName;
                    }).join(' + ');
                }).join(', '), '4', 'Bold');
                drawFunc('Highest level of evidence: ' + therapy.level, '4', 'Bold');
                if (therapy.short) {
                    drawFunc('Short description of evidence: ', '4', 'Bold');
                    drawFunc(therapy.short);
                }
                if (therapy.description) {
                    drawFunc('Description of evidence: ', '4', 'Bold');
                    drawFunc(therapy.description);
                }
            }
        }

        return {
            create: create
        };
    });
