
$(document).ready(function(){
    MainJS.init();
});

$(document).keypress(function(e) {
    if(e.which === 13) {
        $("#displayTabs ul").find("li").each(function(index, item) {
            if($(item).hasClass('active')) {
                switch(index) {
                    case 0:
                        console.log(index);
                        OutJS.search();
                        OutJS.backToTop();
                        break;
                    case 1:
                        MainJS.variantSearch();
                        break;
                    default:
                        break;
                }
            }
        });
    }
});

var MainJS = (function() {
    "use strict";
    
    function init() {
        DataProxy.init(function() {
            tree.init(DataProxy.getTreeInfo(), DataProxy.getDescription());
            initEvents();
            OutJS.backToTop();
            initQtips();
            $("#tree_loader").addClass('_hidden');
            $("#tree").removeClass('_hidden');
        }); 
    }
    
    function initEvents() {
    	$('#tumor_search button').click(function() {
	        OutJS.search();
	    });

        $('#expand-nodes-btn').click(function() {
            tree.expandAll();
            OutJS.backToTop();
        });

        $('#collapse-nodes-btn').click(function() {
            tree.collapseAll();
            OutJS.backToTop();
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
            OutJS.search();
            OutJS.backToTop();
        });

        $("#tumor_search input").keyup(function() {
            var _content = $(this).val();
            if(_content.length > 0) {
                            $("#searchRemoveIcon").show();
            }else {
                $("#searchRemoveIcon").hide();
                OutJS.search();
                OutJS.backToTop();
            }
        });
        
        $("#combined-variants-btn").click(function() {
            if(!$("#combined-variants-btn").hasClass('active')){
                $("#tree_loader").removeClass('_hidden');
                $("#tree").addClass('_hidden');
                setTimeout(function(){
                    DataProxy.generateDataByTreeType("combined", function(){
                        $("body svg").remove();
                        tree.init(DataProxy.getTreeInfo(), DataProxy.getDescription());
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
                        tree.init(DataProxy.getTreeInfo(), DataProxy.getDescription());
                        $("#tree_loader").addClass('_hidden');
                        $("#tree").removeClass('_hidden');
                        $("#separated-variants-btn").addClass('active');
                        $("#combined-variants-btn").removeClass('active');
                    });
                }, 200);
            }
        });
        
        $("#searchVariantBtn").click(function() {
            variantSearch();
        });
        
        $("#useExampleBtn").click(function() {
            $("#variantGeneName").val(getString($("#variantGeneName").attr("placeholder")));
            $("#variantMutation").val(getString($("#variantMutation").attr("placeholder")));
            $("#variantTumorType").val(getString($("#variantTumorType").attr("placeholder")));
            variantSearch();
        });
        
        $("#displayTabs").tab();
    
    }   
    
    function getString(_str) {
        return _str.match(/\(eg.\s+([a-zA-Z0-9\s]+)\)/)[1];
    }
    
    function variantSearch() {
        $("#variant_loader").css('display', 'block');
        $("#variantDisplayResult").empty();
        DataProxy.generateVariant({
            hugoSymbol: $("#variantGeneName").val(),
            alteration: $("#variantMutation").val(),
            tumorType: $("#variantTumorType").val(),
            alterationType: "MUTATION"
        }, tab2ClickCallBack);
    }
    
    function tab2ClickCallBack(data) {
        $("#variant_loader").css('display', 'none');
        if(data) {

            var displayParts = {
                    geneAnnotation: {
                        displayName: "Gene Annotation",
                        objectName: "gene_annotation"
                    },
                    variantEffect: {
                        displayName: "Variant Effect",
                        objectName: "variant_effect"
                    }
                };

            $("#variantDisplayResult").append("<div id='resultSummary'><h2>Result Summary:</h2></div>");
            $("#resultSummary").append("<table class='table'><thead><tr style='background-color: lightgrey;font-weight: bold;'>"
                    +"<td>Gene Name</td>"
                    +"<td>Mutation</td>"
                    +"<td>Tumor Type</td>"
                    +"</tr></thead>"
                    +"<tbody><tr>"
                    +"<td>"+ $("#variantGeneName").val() +"</td>"
                    +"<td>"+ $("#variantMutation").val() +"</td>"
                    +"<td>"+ $("#variantTumorType").val() +"</td>"
                    +"</tr></tbody>"
                    +"</table>");

            for(var key in displayParts) {
                if(data.hasOwnProperty(displayParts[key]["objectName"])){
                    var _newE = $(document.createElement('div')); //faster

                    _newE.attr("id", key);
                    _newE.append("<h2> " + displayParts[key]["displayName"] + "</h2>");
                    _newE.append($("<p/>").html(findRegex(data[displayParts[key]["objectName"]]["description"])));

                    $("#variantDisplayResult").append(_newE);
                }
            }
            
            if(data.hasOwnProperty("cancer_type")){
                var _cancerType = $(document.createElement('div')),
                    cancerTypeL = data.cancer_type.length; //faster

                    _cancerType.attr("id", "cancer-type");
                    _cancerType.addClass("panel-group");
                    _cancerType.append("<h2>Cancer Type</h2>");

                for(var i = 0; i < cancerTypeL; i++) {
                    var _datum = data["cancer_type"][i];
                    var _em = $(document.createElement('div')),
                        _emSub = $(document.createElement('div'));

                    _em.addClass('panel panel-default');
                    _em.append("<div class='panel-heading'>"
                            +"<h2 class='panel-title'>"
                            +"<a data-toggle='collapse' data-parent='#cancer-type' href='#collapse-"+i+"'>"
                            + _datum["type"] + "</a>"
                            + (_datum["relevant_to_patient_disease"] !== "No" ? "<span style='color: green; font-size:12px; font-weight:bold'><i>&nbsp;&nbsp;&nbsp;RELEVANT</i></span>": "") 
                            + "</h4></div>");
                    _emSub.attr("id", "collapse-"+i);
                    _emSub.addClass("panel-collapse");
                    _emSub.addClass("collapse");

        //            if(i === 0) {
        //                _emSub.addClass("in");
        //            }

                    _emSub.append("<div class='panel-body'></div");
                    _emSub.find("div").append(displayAttr(_datum, "cancer-type-" + i, _emSub.find("div"), 0));
                    _em.append(_emSub);

                    _cancerType.append(_em);
                }
                $("#variantDisplayResult").append(_cancerType);
                $("#cancer-type").collapse();
            }
        }else {
            $("#variantDisplayResult").append("<h3>No result found.</h3>");
        }
    }
    
    function displayAttr(datum, id, element, leftMargin) {
        if(datum instanceof Array) {
            for(var i = 0, arrayL = datum.length; i < arrayL; i++) {
                displayAttr(datum[i], id + "-" + i, element, leftMargin+10);
            }
        }else {
            if(typeof datum === 'object'){
                for(var key1 in datum) {
                    if(key1 !== "reference") {
                        var _id = id + "-" + key1;
                        var _newE = $(document.createElement('div'));

                        _newE.attr("id", _id);
                        _newE.css("margin-left", leftMargin+10);


                        if(typeof datum[key1] === 'object') {
                            _newE.append("<h4>" + key1 + "</h4>");
                            _newE = displayAttr(datum[key1], _id, _newE, leftMargin+10);
                        }else {
                            var _content = datum[key1];
                            if(key1 === "description") {
                                _content = findRegex(_content);
                            }
                            _newE.append("<span><b>" + key1 + ":</b> "+ _content +"</span>");
                        }

                        element.append(_newE);
                    }
                }
            }else {
                element.append("<span>" + datum + "<span>");
            }
        }
                return element;
    }
    
    function findRegex(str) {
        var regex = [/PMID:\s*[0-9]+,*\s*[0-9]+/ig, /NCT[0-9]+/ig],
            links = ["http://www.ncbi.nlm.nih.gov/pubmed/",
                     "http://clinicaltrials.gov/show/"];
        for (var j = 0, regexL = regex.length; j < regexL; j++) {
            var result = str.match(regex[j]);
            if(result) {
                for(var i = 0, resultL = result.length; i < resultL; i++) {
                    var _datum = result[i];
                    
                    switch(j) {
                        case 0:
                            var _number = _datum.split(":")[1].trim();
                            _number = _number.replace(/\s+/, "");
                            str = str.replace(_datum, "<a class='withUnderScore' target='_blank' href='"+ links[j] + _number+"'>" + _datum + "</a>");
                            break;
                        default:
                            str = str.replace(_datum, "<a class='withUnderScore' target='_blank' href='"+ links[j] + _datum+"'>" + _datum + "</a>");
                            break;
                    }
                    
                }
            }
        }
        return str;
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
    
    return {
        init: init,
        variantSearch: variantSearch
    };
})();

var OutJS = (function() {
	"use strict";

	function search() {
		var searchKeywards = $('#tumor_search input').val().toLowerCase(),
			result = tree.search(searchKeywards),
			resutlLength = result.length,
			infoText = (resutlLength === 0 ? "No" : resutlLength) + " result" + (resutlLength <= 1 ? "" :"s" );

		$("#searchResult").hide();
		$("#searchResult").css('z-index', 1);

		if(searchKeywards.length > 0) {
			$("#searchResult").text(infoText);
			$("#searchResult").css('z-index', 2);
        	$("#searchResult").show();
	    }
	    result = null;
	}

	function backToTop() {
		if ( 	($(window).height() + 100) < $(document).height() ||
				($(window).width() + 50) < $(document).width() ) {
		    $('#top-link-block').removeClass('hidden').affix({
		        offset: {top:100}
		    });
		}else {
			 $('#top-link-block').addClass('hidden');
		}
	}
	return {
		search: search,
		backToTop: backToTop
	};
})();