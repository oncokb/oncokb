var JqueryEvents = (function() {
    "use strict";
    
    function initEvents() {
        $('#tumor_search button').click(function() {
            Utils.search();
        });

        $('#expand-nodes-btn').click(function() {
            Tree.expandAll();
            Utils.backToTop();
        });

        $('#collapse-nodes-btn').click(function() {
            Tree.collapseAll();
            Utils.backToTop();
        });

        $("#searchRemoveIcon").hide();

        $("#searchRemoveIcon").hover(function() {
            $(this).css('cursor', 'pointer');
        });

        $("#searchRemoveIcon").hover(function() {
            $(this).css('cursor', 'pointer');
        });

        $("#searchRemoveIcon").click(function() {
            $("#tumor_search input").val("");
            $("#searchRemoveIcon").hide();
            Utils.search();
            Utils.backToTop();
        });

        $("#tumor_search input").keyup(function() {
            var _content = $(this).val();
            if(_content.length > 0) {
                $("#searchRemoveIcon").show();
            }else {
                $("#searchRemoveIcon").hide();
                Utils.search();
                Utils.backToTop();
            }
        });
        
        $("#combined-variants-btn").click(function() {
            if(!$("#combined-variants-btn").hasClass('active')){
                $("#tree_loader").removeClass('_hidden');
                $("#tree").addClass('_hidden');
                setTimeout(function(){
                    DataProxy.generateDataByTreeType("combined", function(){
                        $("body svg").remove();
                        Tree.init(DataProxy.getTreeInfo(), DataProxy.getDescription());
                        $("#tree_loader").addClass('_hidden');
                        $("#tree").removeClass('_hidden');
                        $("#combined-variants-btn").addClass('active');
                        $("#separated-variants-btn").removeClass('active');
                    });
                },200);
            }
        });
        
        $("#separated-variants-btn").click(function() {
            if(!$("#separated-variants-btn").hasClass('active')){
                $("#tree_loader").removeClass('_hidden');
                $("#tree").addClass('_hidden');
                setTimeout(function(){
                    DataProxy.generateDataByTreeType("separated", function(){
                        $("body svg").remove();
                        Tree.init(DataProxy.getTreeInfo(), DataProxy.getDescription());
                        $("#tree_loader").addClass('_hidden');
                        $("#tree").removeClass('_hidden');
                        $("#separated-variants-btn").addClass('active');
                        $("#combined-variants-btn").removeClass('active');
                    });
                }, 200);
            }
        });
        
        $("#searchVariantBtn").click(function() {
            VariantsAnnotation.variantSearch(getSearchInput());
        });
        
        $("#useExampleBtn").click(function() {
            $("#variantGeneName").val(getString($("#variantGeneName").attr("placeholder")));
            $("#variantMutation").val(getString($("#variantMutation").attr("placeholder")));
            $("#tumorTypesDropDown").val("lung cancer").trigger("chosen:updated");
            VariantsAnnotation.variantSearch(getSearchInput());
        });
        
        $("#displayTabs").tab();
    }

    function getSearchInput() {
        return {
            hugoSymbol: $("#variantGeneName").val(),
            alteration: $("#variantMutation").val(),
            tumorType: $("#tumorTypesDropDown").val(),
            alterationType: "MUTATION"
        };
    }
    
    function initQtips() {
        $('#expand-nodes-btn').qtip({
            content:{text: "Expand all branches"},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-grey' },
            show: {event: "mouseover"},
            hide: {event: "mouseout"},
            position: {my:'bottom left',at:'top center', viewport: $(window)}
        });

        $('#collapse-nodes-btn').qtip({
            content:{text: "Collapse all branches"},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-grey' },
            show: {event: "mouseover"},
            hide: {event: "mouseout"},
            position: {my:'bottom left',at:'top center', viewport: $(window)}
        });
    }
    
    function getString(_str) {
        return _str.match(/\(eg.\s+([a-zA-Z0-9\s]+)\)/)[1];
    }

    return {
        init: initEvents,
        initQtips: initQtips,
        getSearchInput: getSearchInput
    };
})();