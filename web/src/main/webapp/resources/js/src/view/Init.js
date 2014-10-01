var Init = (function() {
    "use strict";
    var paramsExist = false;
    
    function init() {
        var params = getURLParameters();
        
        DocumentEvents.init();
        if(typeof params !== "undefined" && params && params !== "") {
            paramsExist = true;
        }
        setDefaultTab();
        if(paramsExist) {
            DataProxy.initTumorTypes(function() {
                VariantsAnnotation.init(DataProxy.getTumorTypes(), params);
                DataProxy.initEvidence(initTree); 
            });
        }else {
            DataProxy.init(function() {
                VariantsAnnotation.init(DataProxy.getTumorTypes(), params);
                initTree();
            });
        }
    }
    
    function initTree() {
        Tree.init(DataProxy.getTreeInfo(), DataProxy.getDescription());
        JqueryEvents.init();
        Utils.backToTop();
        JqueryEvents.initQtips();
        $("#tree_loader").addClass('_hidden');
        $("#tree").removeClass('_hidden');
    }
    
    function getURLParameters() {
        var hash = window.location.hash;
        if(hash !== "") {
            return {
                "hugoSymbol" : match("hugoSymbol", hash),
                "alteration": match("alteration", hash),
                "tumorType": match("tumorType", hash),
                "alterationType": "MUTATION"
            };
        }else {
            return null;
        }
    }
    
    function setDefaultTab() {
        if(paramsExist) {
            $("#tab-1").removeClass("active");
            $("#mainTree").removeClass("active");
            $("#mainTree").removeClass("in");
            $("#tab-2").addClass("active");
            $("#variantDisplay").addClass("active");
            $("#variantDisplay").addClass("in");
        }else {
            $("#tab-2").removeClass("active");
            $("#variantDisplay").removeClass("active");
            $("#variantDisplay").removeClass("in");
            $("#tab-1").addClass("active");
            $("#mainTree").addClass("active");
            $("#mainTree").addClass("in");
        }
    }
    
    function match(param, hash) {
        var regexp = new RegExp(param + "=([\\w+\\s*]+)","i");
        var match = hash.match(regexp);
        return(match ? match[1] : "");
    }
    
    return {
        init: init
    };
})();