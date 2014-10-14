var DocumentEvents = (function() {
    "use strict";
    
    function init() {
//        keypress();
    }
    
    function keypress() {
        $(document).keypress(function(e) {
            if(e.which === 13) {
                $("#displayTabs ul").find("li").each(function(index, item) {
                    if($(item).hasClass('active')) {
                        switch(index) {
                            case 0:
                                Utils.search();
                                Utils.backToTop();
                                break;
                            case 1:
                                VariantsAnnotation.variantSearch(JqueryEvents.getSearchInput());
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        });
    }
    return {
        init: init
    };
})();