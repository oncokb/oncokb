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
    
    var summaryTableTitles = [
            "Treatment Implications", 
            "Clinical Trials", 
            "Additional Information", 
            "FDA Approved Drugs in Tumor Type", 
            "FDA Approved Drugs in Other Tumor Type"],
        summaryTableTitlesContent = {
            "Treatment Implications": [
                "nccn_guidelines",
                "standard_therapeutic_implications",
                "investigational_therapeutic_implications"],
            "Clinical Trials": ["clinical_trial"], 
            "Additional Information": ["prevalence", "prognostic_implications"], 
            "FDA Approved Drugs in Tumor Type": [], 
            "FDA Approved Drugs in Other Tumor Type": []
        };
        //The drug information under clinical trials has information for FDA
        //Approved drugs
            
    function initWithData(data) {
        console.log(data);
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
                
                _em.init({
                    divs: {
                        Id: "collapse-"+i,
                        parentId: "cancer-type",
                        title: displayProcess(_datum["type"]),
                        titleAddition: _datum["relevant_to_patient_disease"] !== "No" ? "<span style='color: green; font-size:12px; font-weight:bold'><i>&nbsp;&nbsp;&nbsp;RELEVANT</i></span>": "",
                        body:  generateSummaryTable(),
                        special: false
                    },
                    styles: {
                        
                    }
                });
                
                delete _datum.type;
                delete _datum.relevant_to_patient_disease;
                
                for(var m = 0, summaryTableTitlesL = summaryTableTitles.length; m < summaryTableTitlesL; m++) {
                    var _title = summaryTableTitles[m];
                    var _attr = summaryTableTitlesContent[_title];
                    var _data;
                    var cellIndex = m +1;
                    
                    
                    if(_title === "Clinical Trials") {
                        _data = [];
                        _data = _datum["clinical_trial"];
                    }else {
                        _data = {};
                        for(var j = 0, _attrL = _attr.length; j < _attrL; j++) {
                            if(_datum.hasOwnProperty(_attr[j])) {
                                _data[_attr[j]] = _datum[_attr[j]];
                            }
                        }
                    }
                    
                    if((typeof _data === 'object' && Object.keys(_data).length > 0) || (_data instanceof Array && _data.length > 0)) {
                        _em.empty(".panel-body table tr:nth-child("+ cellIndex +") td:nth-child(2)");
                        _em.append(".panel-body table tr:nth-child("+ cellIndex +") td:nth-child(2)", displayAttr(_data, "cancer-type-" + i, _em.find(".panel-body table tr:nth-child("+ cellIndex +") td:nth-child(2)")), key);
                    }
                }
               
                _cancerType.append(_em.get());
            }
            $("#variantDisplayResult").append(_cancerType);
            $("#cancer-type").collapse();
        }
    }
    
    function generateSummaryTable() {
        var tableDiv = "<table class='table table-bordered'><tbody>";
        
        for(var i = 0, summaryTableTitlesL = summaryTableTitles.length; i < summaryTableTitlesL; i++) {
            tableDiv += "<tr><td><b>" + summaryTableTitles[i] + "</b></td><td>None.</td></tr>";
        }
        
        tableDiv += "</tbody></table>";
        
        return tableDiv;
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

    function displayAttr(datum, id, element, key) {
        var exception = ["eligibility_criteria","purpose"],
            exceptionL = exception.length;
        if(key === "investigational_therapeutic_implications" && datum.hasOwnProperty('sensitive_to')) {
            var formattedDatum = {};
            if(isArray(datum['sensitive_to'])) {
                _.each(datum['sensitive_to'], function(value, index) {
                    var sensitiveName = underTreatment(value, 'treatment');
                    
                    formattedDatum[sensitiveName] = {
                        "Highest level of evidence:": value['level_of_evidence_for_patient_indication']['level'],
                        "description": value['description']};
                });
            }else {
                var sensitiveName = underTreatment(datum['sensitive_to'], 'treatment');

                formattedDatum[sensitiveName] = {
                    "Highest level of evidence:": datum['sensitive_to']['level_of_evidence_for_patient_indication']['level'],
                    "description": datum['sensitive_to']['description']};
            }
            
            displayAttr(formattedDatum, id + "-" + i, element, "");
        }else {
            if(datum instanceof Array) {
                for(var i = 0, arrayL = datum.length; i < arrayL; i++) {
                    var _newE = $(document.createElement('div'));
                    element.append(displayAttr(datum[i], id + "-" + i, _newE, ""));
                    element.append("<br/><br/>");
                }
            }else {
                if(typeof datum === 'object'){
                    for(var key1 in datum) {
                        if(key1 !== "reference") {
                            var _id = id + "-" + key1;
                            var _newE = $(document.createElement('div'));

                            _newE.attr("id", _id);
                            if(key1 === "eligibility_criteria"){
                                _newE.css("width", "100%");
                            }
                            _newE.css("float", "left");
                            _newE.css("margin-right", "10px");


                            if(typeof datum[key1] === 'object') {
                                _newE.append("<h4>" + displayProcess(key1) + "</h4>");
                                _newE = displayAttr(datum[key1], _id, _newE, key1);
                            }else {
                                if(exception.indexOf(key1) === -1 && datum[key1] !== "") {
                                   _newE.append("<span>" + (key1 !== "description"? (key1 !== 'trial_id' ? ("<b>" + displayProcess(key1) + ":</b> " ) : "<br/><b>" + displayProcess(key1) + ":</b> " ) : "") + findRegex(datum[key1]) +"</span>");
                                }
                            }

                            element.append(_newE);
                        }
                    }
                    
                    for(var i = 0; i < exceptionL; i++) {
                        if(datum.hasOwnProperty(exception[i])) {
                            var _id = id + "-" + exception[i];
                            var _newE = $(document.createElement('div'));

                            _newE.attr("id", _id);
                            _newE.css("width", "100%");
                            _newE.css("float", "left");
                            _newE.css("margin-right", "10px");

                            var _em = new BootStrapPanelCollapse();
                            _em.init({
                                divs: {
                                    Id: _id + "-panel",
                                    parentId: _id,
                                    title: displayProcess(exception[i]),
                                    titleAddition: "",
                                    body:  findRegex(datum[exception[i]]),
                                    special: true
                                },
                                styles: {
                                    headerH4: {
                                        'font-size': "16px"
                                    }
                                }
                            });
                            _newE.append(_em.get());
                            element.append(_newE);
                        }
                    }
                }else {
                    element.append("<span>" + datum + "<span>");
                }
            }
        }
        return element;
    }
    
    function underTreatment(value, key) {
        var sensitiveName = "Sensitive to: ";
                    
        if(isArray(value[key])) {
            var _treatmentL = value[key].length;
            _.each(value[key], function(value1, index1) {
                sensitiveName = underDrug(value1, 'drug', sensitiveName);
                sensitiveName += (index1 + 1) === _treatmentL ? "" : " AND ";
            });
        }else {
            sensitiveName = underDrug(value[key], 'drug', sensitiveName);
        }
        
        return sensitiveName;
    }
    
    function underDrug(value, key, sensitiveName) {
        if(isArray(value[key])) {
            var _drugL = value[key].length;
            _.each(value[key], function(value1, index1) {
                sensitiveName += value1.name + (index1 + 1) === _drugL ? "" : "&";
            });
        } else {
            sensitiveName += value[key].name;
        }
        return sensitiveName;
    }
    
    function isArray(array) {
         if(array instanceof Array) {
             return true;
         }else {
             return false;
         }
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
            JqueryEvents.iconSet();
        }else {
            $("#variantDisplayResult").append("<h4>No result found.</h4>");
        }
    }
    return {
        init: initHTML,
        variantSearch: variantSearch
    };
})();