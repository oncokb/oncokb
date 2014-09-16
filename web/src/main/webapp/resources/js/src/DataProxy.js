var DataProxy = (function() {
    
    var treeInfo = [];
        
    function getDataFunc(_callback){
         $.when(  
                $.ajax({type: "POST", url: "/evidence.json"})
                )
            .done(function(a1){
                var resultL = a1.length;
                for(var i = 0 ; i < resultL ; i ++) {
                    var _datum = {};
                    if( a1[i].gene &&
                        a1[i].gene.hugoSymbol) {
                        _datum.gene = a1[i].gene.hugoSymbol;
                    }
                    
                    if( a1[i].alterations &&
                        a1[i].alterations.length > 0) {
                        var _alteration = a1[i].alterations[0].alteration;
                        for(var j = 1, alterationsL = a1[i].alterations.length; j < alterationsL; j++) {
                            _alteration = _alteration + "," + a1[i].alterations[j].alteration;
                        }
                        _datum.alteration = _alteration;
                    }
                    
                    if( a1[i].tumorType &&
                        a1[i].tumorType.name) {
                        _datum.tumorType = a1[i].tumorType.name;
                    }
                    
                    if( a1[i].tumorType &&
                        a1[i].tumorType.name) {
                        _datum.tumorType = a1[i].tumorType.name;
                    }
                    
                    if( a1[i].treatments &&
                        a1[i].treatments.length > 0) {
                        for(var j = 0, treatmentsL = a1[i].treatments.length; j < treatmentsL; j++) {
                            _datum.treatment = a1[i]['treatments'][j]['drugs'][0]['drugName'];
                            treeInfo.push(_datum);
                        }
                    }
                }
                _callback();
            });
    };

    return {
        init: function(callbackFunc){
            getDataFunc(callbackFunc);
        },
        
        getTreeInfo: function(){ return treeInfo;}
    };
}());