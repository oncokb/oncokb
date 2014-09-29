var Init = (function() {
    "use strict";
    
    function init() {
        DocumentEvents.init();
        DataProxy.init(function() {
            VariantsAnnotation.init(DataProxy.getTumorTypes());
            Tree.init(DataProxy.getTreeInfo(), DataProxy.getDescription());
            JqueryEvents.init();
            Utils.backToTop();
            JqueryEvents.initQtips();
            $("#tree_loader").addClass('_hidden');
            $("#tree").removeClass('_hidden');
        }); 
    }
    
    return {
        init: init
    };
})();