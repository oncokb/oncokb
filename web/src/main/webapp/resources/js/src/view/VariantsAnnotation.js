var VariantsAnnotation = (function() {
    "use strict";
    
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

    function initWithData(data) {
        $("#variantDisplayResult").append("<div id='resultSummary'><h2>Result Summary:</h2></div>");
        $("#resultSummary").append("<table class='table'><thead><tr style='background-color: lightgrey;font-weight: bold;'>"
                +"<td>Gene Name</td>"
                +"<td>Mutation</td>"
                +"<td>Tumor Type</td>"
                +"</tr></thead>"
                +"<tbody><tr>"
                +"<td>"+ $("#variantGeneName").val() +"</td>"
                +"<td>"+ $("#variantMutation").val() +"</td>"
                +"<td>"+ $("#tumorTypesDropDown").val() +"</td>"
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
                var _datum = $.extend(true, {}, data["cancer_type"][i]) ;

                
                var _em = new BootStrapPanelCollapse();
                
                var clinicalTrials = [];
                
                _em.init({
                    divs: {
                        Id: "collapse-"+i,
                        parentId: "cancer-type",
                        title: displayProcess(_datum["type"]),
                        titleAddition: _datum["relevant_to_patient_disease"] !== "No" ? "<span style='color: green; font-size:12px; font-weight:bold'><i>&nbsp;&nbsp;&nbsp;RELEVANT</i></span>": "",
                        body:  "<table class='table table-bordered'><tbody>"
                                +"<tr><td nowrap><b>Treatment Implications</b></td><td>None.</td></tr>"
                                +"<tr><td nowrap><b>Clinical Trials</b></td><td>None.</td></tr>"
                                +"</tbody></table>"
                    },
                    styles: {
                        
                    }
                });
                
                
                delete _datum.type;
                delete _datum.relevant_to_patient_disease;
                if(_datum.hasOwnProperty("clinical_trial")) {
                    clinicalTrials = $.extend(true, [], _datum.clinical_trial);
                    delete _datum.clinical_trial;
                    _em.empty(".panel-body table tr:nth-child(2) td:nth-child(2)");
                    _em.append(".panel-body table tr:nth-child(2) td:nth-child(2)", displayAttr(clinicalTrials, "cancer-type-" + i, _em.find(".panel-body table tr:nth-child(2) td:nth-child(2)"), 0));
                }

                _em.empty(".panel-body table tr:nth-child(1) td:nth-child(2)");
                _em.append(".panel-body table tr:nth-child(1) td:nth-child(2)", displayAttr(_datum, "cancer-type-" + i, _em.find(".panel-body table tr:nth-child(1) td:nth-child(2)"), 0));
                _cancerType.append(_em.get());
            }
            $("#variantDisplayResult").append(_cancerType);
            $("#cancer-type").collapse();
        }
    }

    function displayProcess(str) {
        var specialUpperCasesWords = ["NCCN"];
        var specialLowerCasesWords = ["of", "for"];

        str = str.replace(/_/g, " ");
        str = str.replace(
            /\w\S*/g,
            function(txt) {
                var _upperCase = txt.toUpperCase(),
                    _lowerCase = txt.toLowerCase();

                if( specialUpperCasesWords.indexOf(_upperCase) !== -1 ) {
                    return _upperCase;
                }

                if( specialLowerCasesWords.indexOf(_lowerCase) !== -1 ) {
                    return _lowerCase;
                }

                return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
            }
        );
        return str;
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
                            _newE.append("<h4>" + displayProcess(key1) + "</h4>");
                            _newE = displayAttr(datum[key1], _id, _newE, leftMargin+10);
                        }else {
                            if(key1 === "eligibility_criteria") {
                                var _em = new BootStrapPanelCollapse();
                                _em.init({
                                    divs: {
                                        Id: _id + "-panel",
                                        parentId: _id,
                                        title: displayProcess(key1),
                                        titleAddition: "",
                                        body:  findRegex(datum[key1])
                                    },
                                    styles: {

                                    }
                                });
                                _newE.append(_em.get());
                            }else{
                                _newE.append("<span><b>" + displayProcess(key1) + ":</b> "+ findRegex(datum[key1]) +"</span>");
                            }
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
        var regex = [/PMID:\s*([0-9]+,*\s*)+/ig, /NCT[0-9]+/ig],
            links = ["http://www.ncbi.nlm.nih.gov/pubmed/",
                     "http://clinicaltrials.gov/show/"];
        for (var j = 0, regexL = regex.length; j < regexL; j++) {
            var result = str.match(regex[j]);

            if(result) {
                var uniqueResult = result.filter(function(elem, pos) {
                        return result.indexOf(elem) == pos;
                    }); 
                for(var i = 0, resultL = uniqueResult.length; i < resultL; i++) {
                    var _datum = uniqueResult[i];
                    
                    switch(j) {
                        case 0:
                            var _number = _datum.split(":")[1].trim();
                            _number = _number.replace(/\s+/g, "");
                            str = str.replace(new RegExp(_datum, "g"), "<a class='withUnderScore' target='_blank' href='"+ links[j] + _number+"'>" + _datum + "</a>");
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
    
    function variantSearch(keyWords) {
        $("#variant_loader").css('display', 'block');
        $("#variantDisplayResult").empty();
        DataProxy.generateVariant(keyWords, tab2ClickCallBack);
    }
    
    function initHTML(d) {
        for(var i = 0, tumorTypesL = d.length; i < tumorTypesL; i++) {
            var datum = d[i];
            $("#tumorTypesDropDown").append("<option value='"+ datum.name +"'>" + datum.name + "</option>");
        }
        $("#tumorTypesDropDown").chosen({width: "100%"});
    }
    
    function tab2ClickCallBack(data) {
        $("#variant_loader").css('display', 'none');
        if(data) {
            initWithData(data);
        }else {
            $("#variantDisplayResult").append("<h4>No result found.</h4>");
        }
    }
    return {
        init: initHTML,
        variantSearch: variantSearch
    };
})();