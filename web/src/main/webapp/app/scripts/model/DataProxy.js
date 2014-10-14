(function() {
    'use strict';
    
    var app = angular.module('webappApp');
    var genes = {},
        tumorTypes = [],
        treeInfo = [],
        treeInfoDul = {},
        description = {},
        jsonData = [],
        jsonDataL = 0,
        treeType = 'separated';//Combined || Separated'
        
    // app.factory('EvidenceAndTumorType', ['$http', '$q', function ($http, $q) {
    //     var tumorTypeRequest = $http.get('tumorType.json');
    //     var evidenceRequest = $http.get('evidence.json');
       
       
    //     $q.all([evidenceRequest, tumorTypeRequest]).then(function(values) {
    //         console.log(values);
    //         jsonData = values[0].data;
    //         jsonDataL = jsonData.length;
    //         tumorTypes = $.extend(true, [], values[1].data);
    //         analysisData();
    //     },function() {
    //         tumorTypeRequest = $http.get('data/tumorType.json');
    //         evidenceRequest = $http.get('data/evidence.json');
           
    //         $q.all([evidenceRequest, tumorTypeRequest]).then(function(values) {
    //             jsonData = values[0].data;
    //             jsonDataL = jsonData.length;
    //             tumorTypes = $.extend(true, [], values[1].data);
    //             console.log(_.extend({}, jsonData));
    //             analysisData();
    //         });
    //     });
    // }]);
    
    // app.factory('Evidence', ['$http', function ($http) {
    //     $http.get('evidence.json')
    //         .success(function(data, status) {
    //                jsonData = data;
    //                 jsonDataL = jsonData.length;
    //                 analysisData();
    //         })
    //         .error(function(data, status) {
    //             $http.get('data/evidence.json')
    //                 .success(function(data, status) {
    //                     jsonData = data;
    //                     jsonDataL = jsonData.length;
    //                     analysisData();
    //             });
    //     });
    // }]);

    // app.factory('TumorType', ['$http',  function ($http) {
    //     $http.get('tumorType.json')
    //         .success(function(data, status) {
    //            tumorTypes = data;
    //         })
    //         .error(function(data, status) {
    //             $http.get('data/tumorType.json')
    //                 .success(function(data, status) {
    //                     tumorTypes = data;
    //             });
    //     });
    // }]);
    
    // function generateVariant(params, callback) {
    //     var _params = $.extend(true, {}, params);
    //     $.ajax({
    //         type: 'POST', 
    //         url: 'var_annotation',
    //         data: _params,
    //         dataType:'xml'
    //     })
    //     .success(function(data) {
    //         var _variantJson = $.xml2json(data);
    //         callback(_variantJson);
    //     })
    //     .fail(function(data) {
    //         callback(null);
    //     });
    // }
    
    function analysisData(callback) {
        genes = {},
        treeInfo = [],
        description = {},
        generateTreeInfo();
        clusterEvidences();
        generateDes();
        Tree.init(treeInfo, description);
    }
    
    function initTreeTreatments(_datum, treeInfodatum) {
        var _treeInfodatum = _.deepExtend({}, treeInfodatum);
        for(var j = 0, treatmentsL = _datum.treatments.length; j < treatmentsL; j++) {
            var _treatmentName = _datum.treatments[j].drugs[0].drugName || '';
            var _datumName = '';

            for(var m = 1, _drugsL = _datum.treatments[j].drugs.length; m < _drugsL; m++) {
                _treatmentName += ',' + _datum.treatments[j].drugs[m].drugName;
            }
            _treeInfodatum.treatment = _treatmentName;
            for( var _key in _treeInfodatum) {
                _datumName += _treeInfodatum[_key] + ',';
            }
            treeInfoDul[_datumName] = _treeInfodatum;
        }
        return _treeInfodatum;
    }

    function initTreeAlteration(_datum, _treeInfodatum) {
        var _datumName = '',
            _treeInfoDatum = _.deepExtend({}, _treeInfodatum);

        if( _datum.tumorType &&
            _datum.tumorType.name) {
            _treeInfoDatum.tumorType = _datum.tumorType.name;
            
            if( _datum.treatments &&
                _datum.treatments.length > 0) {
                initTreeTreatments(_datum, _treeInfoDatum);
            }else {
                for( var _key in _treeInfoDatum) {
                    _datumName += _treeInfoDatum[_key] + ',';
                }
                treeInfoDul[_datumName] = _treeInfoDatum;
            }
        }else {
            for( var _key in _treeInfoDatum) {
                _datumName += _treeInfoDatum[_key] + ',';
            }
            treeInfoDul[_datumName] = _treeInfoDatum;
        }
    }
        
    function generateTreeInfo() {
        for(var i = 0 ; i < jsonDataL ; i ++) {
            var _treeInfodatum = {},
                _datum = jsonData[i],
                alterationsL = _datum.alterations.length,
                _datumName = '';

            if( _datum.gene &&
                _datum.gene.hugoSymbol) {
                _treeInfodatum.gene = _datum.gene.hugoSymbol;
            }

            if( _datum.alterations &&
                _datum.alterations.length > 0) {
                if(treeType === 'combined') {
                    var _alteration = _datum.alterations[0].alteration;
                    for(var j = 1; j < alterationsL; j++) {
                        _alteration = _alteration + ',' + _datum.alterations[j].alteration;
                    }
                    _treeInfodatum.alteration = _alteration;
                    initTreeAlteration(_datum, _treeInfodatum);
                }else {
                    for(var n = 0; n < alterationsL; n++) {
                        
                        _treeInfodatum.alteration = _datum.alterations[n].alteration;
                        initTreeAlteration(_datum, _treeInfodatum);
                    }
                }
            }else {
                _datumName = '';
                for( var _key in _treeInfodatum) {
                    _datumName += _treeInfodatum[_key] + ',';
                }
                treeInfoDul[_datumName] = _treeInfodatum;
            }
            _treeInfodatum = null;
        }
        
        for(var _key in treeInfoDul) {
            treeInfo.push(treeInfoDul[_key]);
        }
        
        treeInfoDul = {};
    }
    
    //Cluster evidences by gene name
    function clusterEvidences() {
        for(var i = 0 ; i < jsonDataL ; i ++) {
            var _datum = jsonData[i];
            if( _datum.gene &&
                _datum.gene.hugoSymbol) {
                if(!genes.hasOwnProperty(_datum.gene.hugoSymbol)) {
                    genes[_datum.gene.hugoSymbol] = [];
                }
                genes[_datum.gene.hugoSymbol].push(_datum);
            }else {
                console.log('No gene name found.');
            }
        }
    }
    
    //Generate description
    function generateDes() {
        for(var _geneName in genes) {
            var _geneDatum = genes[_geneName],
                _geneDatumL = _geneDatum.length;
            if(!description.hasOwnProperty(_geneName)){
                description[_geneName] = {};
            }

            for(var i = 0; i < _geneDatumL; i++) {
                var _datum = _geneDatum[i],
                    _altName = '',
                    _displayAttr = {
                        evidenceType: 'Evidence Type',
                        description: 'Description',
                        knownEffect: 'Known Effect',
                        levelOfEvidence: 'Level Of Evidence'
                    },
                    _attrDatum = {};
                
                
                for(var _attr in _displayAttr) {
                    _attrDatum[_displayAttr[_attr]] = _datum[_attr];
                }
                
                if(_datum.alterations && _datum.alterations.length > 0) {
                    if(treeType === 'combined') {
                        _altName = _datum.alterations[0].alteration || '';
                        for(var j = 1, _altL = _datum.alterations.length; j < _altL; j++) {
                            _altName += ',' + _datum.alterations[j].alteration;
                        }
                        initDesAlterations(_datum, _attrDatum, _geneName, _altName);
                    }else {
                        for(var n = 0, _altL = _datum.alterations.length; n < _altL; n++) {
                            _altName = _datum.alterations[n].alteration;
                            initDesAlterations(_datum, _attrDatum, _geneName, _altName);
                        }
                    }
                }else {
                    if(!description[_geneName].hasOwnProperty('description')) {
                        description[_geneName].description = [];
                    }
                    description[_geneName].description.push(_attrDatum);
                }

                _datum = null;
            }
            _geneDatum = null;
        }
    }
    
    function initDesTreatments(datum, attrDatum, _geneName, _altName, _tumorTypeName) {
        var _treatmentsName = '',
            _datum = _.deepExtend( {}, datum),
            _attrDatum = _.deepExtend( {}, attrDatum);
        for(var j = 0, _treatmentsL = _datum.treatments.length; j < _treatmentsL; j++){
            _treatmentsName = _datum.treatments[j].drugs[0].drugName || '';
            for(var m = 1, _drugsL = _datum.treatments[j].drugs.length; m < _drugsL; m++) {
                _treatmentsName += ',' + _datum.treatments[j].drugs[m].drugName;
            }
            if(!description[_geneName][_altName][_tumorTypeName].hasOwnProperty(_treatmentsName)){
                description[_geneName][_altName][_tumorTypeName][_treatmentsName] = {};
            }
            if(!description[_geneName][_altName][_tumorTypeName][_treatmentsName].hasOwnProperty('description')) {
                description[_geneName][_altName][_tumorTypeName][_treatmentsName].description = [];
            }

            description[_geneName][_altName][_tumorTypeName][_treatmentsName].description.push(_attrDatum);
        }
    }
    
    function initDesTumorType(datum, attrDatum, _geneName, _altName) {
        var _datum = _.deepExtend({}, datum),
            _attrDatum = _.deepExtend({}, attrDatum),
            _tumorTypeName = _datum.tumorType.name;
        
        if(!description[_geneName][_altName].hasOwnProperty(_tumorTypeName)){
            description[_geneName][_altName][_tumorTypeName] = {};
        }

        if(_datum.treatments && _datum.treatments.length > 0) {
            initDesTreatments(_datum, _attrDatum, _geneName, _altName, _tumorTypeName);
        }else {

            if(!description[_geneName][_altName][_tumorTypeName].hasOwnProperty('description')) {
                description[_geneName][_altName][_tumorTypeName].description = [];
            }
            description[_geneName][_altName][_tumorTypeName].description.push(_attrDatum);
        }
    }
    function initDesAlterations(_datum, _attrDatum, _geneName, _altName) {
        if(!description[_geneName].hasOwnProperty(_altName)){
            description[_geneName][_altName] = {};
        }

        if(_datum.tumorType) {
           initDesTumorType(_datum, _attrDatum, _geneName, _altName);
        }else {
            if(!description[_geneName][_altName].hasOwnProperty('description')) {
                description[_geneName][_altName].description = [];
            }
            description[_geneName][_altName].description.push(_attrDatum);
        }
    }
    
    return {
    };
}());