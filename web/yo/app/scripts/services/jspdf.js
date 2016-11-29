'use strict';

/**
 * @ngDOC service
 * @name oncokbApp.jspdf
 * @description
 * # jspdf
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .service('jspdf', function(PDF) {
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
        var tumorTypeAttrs = {
            shortPrevalence: 'Short Prevalence',
            prevalence: 'Prevalence',
            shortProgImp: 'Short Prognostic implications',
            progImp: 'Prognostic implications'
        };
        var nccnAttrs = {
            therapy: 'Therapy',
            disease: 'Disease',
            version: 'Version',
            pages: 'Pages',
            category: 'Recommendation category'
        };

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
            if (mutation.summary) {
                drawFunc('Summary:', '4', 'Bold');
                drawFunc(mutation.summary);
            }
            if (mutation.oncogenic) {
                drawFunc('Oncogenic: ' + mutation.oncogenic, '4', 'Bold');
            }
            if (mutation.shortSummary) {
                drawFunc('Summary of oncogenic: ' + mutation.shortSummary, '4', 'Bold');
            }
            if (mutation.effect.value || mutation.effect.addOn) {
                drawFunc('Mutation effect: ' + mutation.effect.value + mutation.effect.addOn, '4', 'Bold');
            }
            if (mutation.short) {
                drawFunc('Short description of mutation effect:', '4', 'Bold');
                drawFunc(mutation.short);
            }
            if (mutation.description) {
                drawFunc('Description of mutation effect:', '4', 'Bold');
                drawFunc(mutation.description);
            }
        }

        function tumorType(tumorTypes) {
            if (angular.isArray(tumorTypes)) {
                tumorTypes.forEach(function(e) {
                    drawFuncTumorType(e);
                });
            }
        }

        function getCancerTypesName(cancerTypes) {
            var list = [];
            cancerTypes.forEach(function(cancerType) {
                if (cancerType.subtype.length > 0) {
                    var str = cancerType.subtype;
                    list.push(str);
                } else if (cancerType.cancerType.length > 0) {
                    list.push(cancerType.cancerType);
                }
            });
            return list.join(', ');
        }

        function drawFuncTumorType(tumorType) {
            drawFunc('Tumor Type: ' + getCancerTypesName(tumorType.cancerTypes), '2', 'Bold');
            if (tumorType.summary) {
                drawFunc('Summary:', '4', 'Bold');
                drawFunc(tumorType.summary);
            }
            for (var key in tumorTypeAttrs) {
                if (tumorType[key]) {
                    drawFunc(tumorTypeAttrs[key] + ': ', '3', 'Bold');
                    drawFunc(tumorType[key]);
                }
            }

            if (tumorType.nccn && tumorType.nccn.disease) {
                nccnFunc(tumorType.nccn);
            }

            tumorType.TI.forEach(function(e) {
                var title = '';

                if (e.type) {
                    title = 'Sensitive to';
                } else {
                    title = 'Resistant to';
                }

                if (e.description || e.treatments.length) {
                    drawFunc(e.name, '3', 'Bold');
                    if (e.description) {
                        drawFunc(e.description);
                    }

                    if (e.treatments.length) {
                        e.treatments.forEach(function(e1) {
                            therapyFunc(e1, title);
                        });
                    }
                }
            });

            if (tumorType.trials.length) {
                trialsFunc(tumorType.trials);
            }
        }

        function trialsFunc(trials) {
            drawFunc('Ongoing clinical trials', '3', 'Bold');
            drawFunc(trials.join(', '));
        }

        function nccnFunc(nccn) {
            drawFunc('NCCN guidelines:', '3', 'Bold');
            for (var key in nccnAttrs) {
                if (nccn[key]) {
                    drawFunc(nccnAttrs[key] + ': ' + nccn[key], '4');
                }
            }
        }

        function therapyFunc(therapy, title) {
            drawFunc(title + ': ' + therapy.name, '4', 'Bold');
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

        this.create = create;
    });
