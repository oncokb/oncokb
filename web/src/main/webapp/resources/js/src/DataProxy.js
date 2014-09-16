var DataProxy = (function() {
    
    var genes = {},
        treeInfo = [],
        description = {};
        
    function getDataFunc(_callback){
         $.when(  
                $.ajax({type: "POST", url: "/evidence.json"})
                )
            .done(function(a1){
                generateTreeInfo(a1);
                clusterEvidences(a1);
                generateDes();
                _callback();
            });
    };
    
    function generateTreeInfo(data) {
        var resultL = data.length;
                
        for(var i = 0 ; i < resultL ; i ++) {
            var _treeInfodatum = {};

            if( data[i].gene &&
                data[i].gene.hugoSymbol) {
                _treeInfodatum.gene = data[i].gene.hugoSymbol;
            }

            if( data[i].alterations &&
                data[i].alterations.length > 0) {
                var _alteration = data[i].alterations[0].alteration;
                for(var j = 1, alterationsL = data[i].alterations.length; j < alterationsL; j++) {
                    _alteration = _alteration + "," + data[i].alterations[j].alteration;
                }
                _treeInfodatum.alteration = _alteration;
            }

            if( data[i].tumorType &&
                data[i].tumorType.name) {
                _treeInfodatum.tumorType = data[i].tumorType.name;
            }

            if( data[i].treatments &&
                data[i].treatments.length > 0) {
                for(var j = 0, treatmentsL = data[i].treatments.length; j < treatmentsL; j++) {
                    var _treatmentName = data[i]['treatments'][j]['drugs'][0]['drugName'] || "";
                    for(var m = 1, _drugsL = data[i]["treatments"][j]["drugs"].length; m < _drugsL; m++) {
                        _treatmentName += "," + data[i]['treatments'][j]['drugs'][m]['drugName'];
                    }
                    _treeInfodatum.treatment = _treatmentName;
                    treeInfo.push(_treeInfodatum);
                }
            }else {
                treeInfo.push(_treeInfodatum);
            }
            _treeInfodatum = null;
        }
    }
    
    //Cluster evidences by gene name
    function clusterEvidences(data) {
        var resultL = data.length;
        for(var i = 0 ; i < resultL ; i ++) {
            if( data[i].gene &&
                data[i].gene.hugoSymbol) {
                if(!genes.hasOwnProperty(data[i].gene.hugoSymbol)) {
                    genes[data[i].gene.hugoSymbol] = [];
                }
                genes[data[i].gene.hugoSymbol].push(data[i]);
            }else {
                console.log("No gene name found.");
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
                    _altName = "",
                    _tumorTypeName = "",
                    _treatmentsName = "",
                    _displayAttr = {
                        evidenceType: "Evidence Type",
                        description: "Description",
                        knownEffect: "Known Effect",
                        levelOfEvidence: "Level Of Evidence"
                    },
                    _attrDatum = {};
                
                for(var _attr in _displayAttr) {
                    _attrDatum[_displayAttr[_attr]] = _datum[_attr];
                }
                
                if(_datum.alterations && _datum.alterations.length > 0) {
                    _altName = _datum.alterations[0]['alteration'] || "";

                    for(var j = 1, _altL = _datum.alterations.length; j < _altL; j++) {
                        _altName += "," + _datum.alterations[j]['alteration'];
                    }

                    if(!description[_geneName].hasOwnProperty(_altName)){
                        description[_geneName][_altName] = {};
                    }

                    if(_datum.tumorType) {
                        _tumorTypeName = _datum.tumorType.name;

                        if(!description[_geneName][_altName].hasOwnProperty(_tumorTypeName)){
                            description[_geneName][_altName][_tumorTypeName] = {};
                        }

                        if(_datum.treatments && _datum.treatments.length > 0) {
                            for(var j = 0, _treatmentsL = _datum.treatments.length; j < _treatmentsL; j++){
                                _treatmentsName = _datum.treatments[j]['drugs'][0]['drugName'] || "";
                                for(var m = 1, _drugsL = _datum.treatments[j]["drugs"].length; m < _drugsL; m++) {
                                    _treatmentsName += "," + _datum.treatments[j]["drugs"][m]['drugName'];
                                }
                                if(!description[_geneName][_altName][_tumorTypeName].hasOwnProperty(_treatmentsName)){
                                    description[_geneName][_altName][_tumorTypeName][_treatmentsName] = {};
                                }
                                if(!description[_geneName][_altName][_tumorTypeName][_treatmentsName].hasOwnProperty("description")) {
                                    description[_geneName][_altName][_tumorTypeName][_treatmentsName]["description"] = [];
                                }
                                
                                description[_geneName][_altName][_tumorTypeName][_treatmentsName]["description"].push(_attrDatum);
                            }
                        }else {

                            if(!description[_geneName][_altName][_tumorTypeName].hasOwnProperty("description")) {
                                description[_geneName][_altName][_tumorTypeName]["description"] = [];
                            }
                            description[_geneName][_altName][_tumorTypeName]["description"].push(_attrDatum);
                        }
                    }else {
                        if(!description[_geneName][_altName].hasOwnProperty("description")) {
                            description[_geneName][_altName]["description"] = [];
                        }
                        description[_geneName][_altName]["description"].push(_attrDatum);
                    }
                }else {
                    if(!description[_geneName].hasOwnProperty("description")) {
                        description[_geneName]["description"] = [];
                    }
                    description[_geneName]["description"].push(_attrDatum);
                }

                _datum = null;
            }
            _geneDatum = null;
        }
    }
    
    return {
        init: function(callbackFunc){
            getDataFunc(callbackFunc);
        },
        
        getTreeInfo: function(){ return treeInfo;},
        getDescription: function() { return description;}
    };
}());