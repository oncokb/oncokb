var DataProxy = (function() {
    
    var obtainDataObject = [];
        
    function getDataFunc(){
         $.when(  
                $.ajax({type: "POST", url: "/evidence.json"})
                )
            .done(function(a1){
                console.log(a1);
                var resultL = a1.length;
                for(var i = 0 ; i < resultL ; i ++) {
                    if( a1[i].alterations && 
                        a1[i].alterations.length > 0 &&
                        a1[i].gene.hugoSymbol === 'BRAF') {
                        if((typeof a1[i].tumorType !== undefined) && a1[i].tumorType) {
                            console.log(a1[i].gene.hugoSymbol + ": " + a1[i].tumorType.name);
                            console.log(a1[i]);
                        }else {
                            console.log(a1[i].gene.hugoSymbol + ": " + a1[i].tumorType);
                        }
                        
                    }
                }
            });
    };

    return {
        init: function(callbackFunc){
            getDataFunc(callbackFunc);
        },
        
        getArrData: function(){ return obtainDataObject['arr'];},
        getAttrData: function(){ return obtainDataObject['attr'];},
        getMutatedGenesData: function(){ return obtainDataObject['mutatedGenes'];},
        getCNAData: function(){return obtainDataObject['cna'];},
        getSampleidToPatientidMap: function(){return obtainDataObject['sampleidToPatientidMap'];}
    };
}());