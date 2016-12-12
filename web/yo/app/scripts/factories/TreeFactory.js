angular.module('oncokbApp').factory('Evidence', ['$http', function($http, config) {
    'use strict';

    function getFromServer() {
        return $http.get(config.apiLink + 'evidence.json');
    }

    function getFromFile() {
        return $http.get('data/evidence.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokbApp').factory('AnalysisEvidence', function(_) {
    'use strict';

    var genes = {};
    var treeInfo = [];
    var treeInfoDul = {};
    var description = {};
    var jsonData = [];
    var jsonDataL = 0;
    var treeType = 'separated';

    function analysisData(type, data) {
        if (typeof type !== 'undefined') {
            treeType = type;
        }
        if (typeof data !== 'undefined') {
            jsonData = data;
            jsonDataL = jsonData.length;
        }

        genes = {};
        treeInfo.length = 0;
        description = {};
        generateTreeInfo();
        clusterEvidences();
        generateDes();
        return {
            genes: genes,
            descriptions: description,
            treeInfo: treeInfo
        };
    }

    function initTreeTreatments(datum, treeInfodatum) {
        for (var j = 0, treatmentsL = datum.treatments.length; j < treatmentsL; j++) {
            var _treatmentName = datum.treatments[j].drugs[0].drugName || '';
            var _datumName = '';

            for (var m = 1, _drugsL = datum.treatments[j].drugs.length; m < _drugsL; m++) {
                _treatmentName += ',' + datum.treatments[j].drugs[m].drugName;
            }
            treeInfodatum.treatment = _treatmentName;
            _.each(treeInfodatum, function(item) {
                _datumName += item + ',';
            });
            treeInfoDul[_datumName] = treeInfodatum;
        }
        return treeInfodatum;
    }

    function initTreeAlteration(datum, treeInfo) {
        var _datumName = '';

        if (datum.cancerType) {
            treeInfo.cancerType = datum.cancerType;

            if (datum.treatments &&
                datum.treatments.length > 0) {
                initTreeTreatments(datum, treeInfo);
            } else {
                _.each(treeInfo, function(item) {
                    _datumName += item + ',';
                });
                treeInfoDul[_datumName] = treeInfo;
            }
        } else {
            _.each(treeInfo, function(item) {
                _datumName += item + ',';
            });
            treeInfoDul[_datumName] = treeInfo;
        }
    }

    function generateTreeInfo() {
        for (var i = 0; i < jsonDataL; i++) {
            var _treeInfodatum = {};
            var _datum = jsonData[i];
            var alterationsL = _datum.alterations.length;
            var _datumName = '';

            if (_datum.gene &&
                _datum.gene.hugoSymbol) {
                _treeInfodatum.gene = _datum.gene.hugoSymbol;
            }

            if (_datum.alterations &&
                _datum.alterations.length > 0) {
                if (treeType === 'combined') {
                    var _alteration = _datum.alterations[0].alteration;
                    for (var j = 1; j < alterationsL; j++) {
                        _alteration = _alteration + ',' + _datum.alterations[j].alteration;
                    }
                    _treeInfodatum.alteration = _alteration;
                    initTreeAlteration(_datum, _treeInfodatum);
                } else {
                    for (var n = 0; n < alterationsL; n++) {
                        _treeInfodatum.alteration = _datum.alterations[n].alteration;
                        initTreeAlteration(_datum, _treeInfodatum);
                    }
                }
            } else {
                _datumName = '';
                _.each(_treeInfodatum, function(item) {
                    _datumName += item + ',';
                });
                treeInfoDul[_datumName] = _treeInfodatum;
            }
            _treeInfodatum = null;
        }
        _.each(treeInfoDul, function(item) {
            treeInfo.push(item);
        });

        treeInfoDul = {};
    }

    // Cluster evidences by gene name
    function clusterEvidences() {
        for (var i = 0; i < jsonDataL; i++) {
            var _datum = jsonData[i];
            if (_datum.gene &&
                _datum.gene.hugoSymbol) {
                if (!genes.hasOwnProperty(_datum.gene.hugoSymbol)) {
                    genes[_datum.gene.hugoSymbol] = [];
                }
                genes[_datum.gene.hugoSymbol].push(_datum);
            } else {
                console.log('No gene name found.');
            }
        }
    }

    // Generate description
    function generateDes() {
        for (var _geneName in genes) {
            if (genes.hasOwnProperty(_geneName)) {
                var _geneDatum = genes[_geneName];
                var _geneDatumL = _geneDatum.length;
                if (!description.hasOwnProperty(_geneName)) {
                    description[_geneName] = {};
                }

                for (var i = 0; i < _geneDatumL; i++) {
                    var _datum = _geneDatum[i];
                    var _altName = '';
                    var _displayAttr = {
                        evidenceType: 'Evidence Type',
                        description: 'Description',
                        knownEffect: 'Known Effect',
                        levelOfEvidence: 'Level Of Evidence'
                    };
                    var _attrDatum = {};

                    _.each(_displayAttr, function(item, key) {
                        _attrDatum[item] = _datum[key];
                    });

                    if (_datum.alterations && _datum.alterations.length > 0) {
                        var _altL = _datum.alterations.length;
                        if (treeType === 'combined') {
                            _altName = _datum.alterations[0].alteration || '';
                            for (var j = 1; j < _altL; j++) {
                                _altName += ',' + _datum.alterations[j].alteration;
                            }
                            initDesAlterations(_datum, _attrDatum, _geneName, _altName);
                        } else {
                            for (var n = 0; n < _altL; n++) {
                                _altName = _datum.alterations[n].alteration;
                                initDesAlterations(_datum, _attrDatum, _geneName, _altName);
                            }
                        }
                    } else {
                        if (!description[_geneName].hasOwnProperty('description')) {
                            description[_geneName].description = [];
                        }
                        description[_geneName].description.push(_attrDatum);
                    }

                    _datum = null;
                }
            }
        }
    }

    function initDesTreatments(datum, attrDatum, _geneName, _altName, _cancerType) {
        var _treatmentsName = '';

        for (var j = 0, _treatmentsL = datum.treatments.length; j < _treatmentsL; j++) {
            _treatmentsName = datum.treatments[j].drugs[0].drugName || '';
            for (var m = 1, _drugsL = datum.treatments[j].drugs.length; m < _drugsL; m++) {
                _treatmentsName += ',' + datum.treatments[j].drugs[m].drugName;
            }
            if (!description[_geneName][_altName][_cancerType].hasOwnProperty(_treatmentsName)) {
                description[_geneName][_altName][_cancerType][_treatmentsName] = {};
            }
            if (!description[_geneName][_altName][_cancerType][_treatmentsName].hasOwnProperty('description')) {
                description[_geneName][_altName][_cancerType][_treatmentsName].description = [];
            }

            description[_geneName][_altName][_cancerType][_treatmentsName].description.push(attrDatum);
        }
    }

    function initDesTumorType(datum, attrDatum, _geneName, _altName) {
        var _cancerType = datum.cancerType;

        if (!description[_geneName][_altName].hasOwnProperty(_cancerType)) {
            description[_geneName][_altName][_cancerType] = {};
        }

        if (datum.treatments && datum.treatments.length > 0) {
            initDesTreatments(datum, attrDatum, _geneName, _altName, _cancerType);
        } else {
            if (!description[_geneName][_altName][_cancerType].hasOwnProperty('description')) {
                description[_geneName][_altName][_cancerType].description = [];
            }
            description[_geneName][_altName][_cancerType].description.push(attrDatum);
        }
    }

    function initDesAlterations(_datum, _attrDatum, _geneName, _altName) {
        if (!description[_geneName].hasOwnProperty(_altName)) {
            description[_geneName][_altName] = {};
        }

        if (_datum.cancerType) {
            initDesTumorType(_datum, _attrDatum, _geneName, _altName);
        } else {
            if (!description[_geneName][_altName].hasOwnProperty('description')) {
                description[_geneName][_altName].description = [];
            }
            description[_geneName][_altName].description.push(_attrDatum);
        }
    }

    return {
        init: analysisData
    };
});
