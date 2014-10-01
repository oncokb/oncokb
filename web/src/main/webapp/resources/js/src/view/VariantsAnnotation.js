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
        var approvedDrugs = {};
    
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
                var _datum = $.extend(true, {}, data["cancer_type"][i]),
                    _tumorType = _datum['type'];
                
                if(_datum.hasOwnProperty("standard_therapeutic_implications")) {
                    var drugs = [];
                    drugs = getApprovedDrug(_datum["standard_therapeutic_implications"], ['sensitive_to','treatment','drug'],drugs);
                    approvedDrugs[_tumorType] = Utils.removeDuplicates(drugs);
                }
            }
            
            for(var i = 0; i < cancerTypeL; i++) {
                var _datum = $.extend(true, {}, data["cancer_type"][i]),
                    _em = new BootStrapPanelCollapse(),
                    _tumorType = _datum['type'];
                
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
                    
                    if(m === summaryTableTitlesL-2 && approvedDrugs.hasOwnProperty(_tumorType)){
                        _em.empty(".panel-body table tr:nth-child("+ cellIndex +") td:nth-child(2)");
                        _em.append(".panel-body table tr:nth-child("+ cellIndex +") td:nth-child(2)", approvedDrugs[_tumorType].join(" / "));
                    }
                    
                    if(m === summaryTableTitlesL-1){
                        var _drugs = $.extend(true, {}, approvedDrugs);
                        if(_drugs.hasOwnProperty(_tumorType)){ 
                            delete _drugs[_tumorType];
                        }
                        if(Object.keys(_drugs).length > 0) {
                            _em.empty(".panel-body table tr:nth-child("+ cellIndex +") td:nth-child(2)");
                            _em.append(".panel-body table tr:nth-child("+ cellIndex +") td:nth-child(2)", objToStr(_drugs));
                        }
                    }
                }
                
                _cancerType.append(_em.get());
            }
            $("#variantDisplayResult").append(_cancerType);
            $("#cancer-type").collapse();
        }
    }
    
    function objToStr(obj) {
        var string = "";
        for(var key in obj) {
            for(var i = 0, arrayL = obj[key].length; i < arrayL; i++){
                string += obj[key][i] + " / " + key + "<br/>";
            }
        }
        return string;
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
    
    function displaySpecial(datum, id, element) {
        for(var key in datum) {
            var formattedDatum = {};
            if(isArray(datum[key])) {
                _.each(datum[key], function(value, index) {
                    var name = key + ": ";

                    name = underTreatment(value, name, ['treatment', 'drug'], ['AND','&']);
                    formattedDatum = formatDatum(formattedDatum, name, value);
                });
            }else {
                var name = key + ": ";

                name = underTreatment(datum[key], name, ['treatment', 'drug'], ['/','&']);
                formattedDatum = formatDatum(formattedDatum, name, datum[key]);
            }

            displayRegular(formattedDatum, id, element);
        }
    }
    
    function formatDatum(formattedDatum, name, value) {
        formattedDatum[name] = {};
        
        if(value.hasOwnProperty('level_of_evidence_for_patient_indication')) {
            formattedDatum[name]["Highest level of evidence"] =  value['level_of_evidence_for_patient_indication']['level'];
        }

        formattedDatum[name]["description"] = value['description'];
        return formattedDatum;
    }
    
    function displayRegular(datum, id, element) {
        var exception = ["eligibility_criteria","purpose"],
            exceptionL = exception.length;
    
        if(datum instanceof Array) {
            for(var i = 0, arrayL = datum.length; i < arrayL; i++) {
                var _newE = $(document.createElement('div'));
                element.append(displayAttr(datum[i], id + "-" + i, _newE, ""));
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
                               _newE.append("<span>" + (key1 !== "description"? ("<b>" + displayProcess(key1) + ":</b> " ) : "") + findRegex(datum[key1]) +"</span>");
                            }
                        }

                        element.append(_newE);
                    }
                }

                for(var i = 0; i < exceptionL; i++) {
                    if(datum.hasOwnProperty(exception[i])) {
                        var _newE = createBootStrapPanelCollapse(
                            element, 
                            id + "-" + exception[i], 
                            exception[i], 
                            datum[exception[i]]);

                        element.append(_newE);
                    }
                }
            }else {
                element.append("<span>" + datum + "<span>");
            }
        }
    }
    
    function displayAttr(datum, id, element, key) {
        var specialItems = ['investigational_therapeutic_implications', 'standard_therapeutic_implications'],
            itiSpecialItems = ['sensitive_to', 'resistant_to'];
        
        if(specialItems.indexOf(key) !== -1) {
            var specialDatum = {},
                regularDatum = {};
            
            _.each(datum, function(value, key) {
                if(itiSpecialItems.indexOf(key) === -1) {
                    regularDatum[key] = value;
                }else {
                    specialDatum[key] = value;
                }
            });
            
            if (Object.keys(specialDatum).length > 0)
                displaySpecial(specialDatum, id, element);
            if (Object.keys(regularDatum).length > 0)
                displayRegular(regularDatum, id, element);
        }else {
            displayRegular(datum, id, element);
        }
        return element;
    }
    
    function createBootStrapPanelCollapse(element, Id, title, body) {
        var _newE = $(document.createElement('div'));

        _newE.attr("id", Id);
        _newE.css("width", "100%");
        _newE.css("float", "left");
        _newE.css("margin-right", "10px");

        var _em = new BootStrapPanelCollapse();
        _em.init({
            divs: {
                Id: Id + "-panel",
                parentId: Id,
                title: displayProcess(title),
                titleAddition: "",
                body:  findRegex(body),
                special: true
            },
            styles: {
                headerH4: {
                    'font-size': "16px"
                }
            }
        });
        _newE.append(_em.get());
        return _newE;
    }
    
    function getApprovedDrug(value, keys, drugs) {
        if(keys.length) {
            var key = keys[0];

            if(isArray(value[key])) {
                _.each(value[key], function(value1, index1) {
                    var _keys = $.extend(true, [], keys);
                    _keys.shift();
                    drugs = getApprovedDrug(value1, _keys, drugs);
                });
            }else {
                if(keys.length > 1) {
                    keys.shift();
                    drugs = getApprovedDrug(value[key], keys, drugs);
                }else {
                    if (value[key].fda_approved === "Yes") {
                        drugs.push(value[key].name);
                    }
                }
            }
        }else {
            if (value.fda_approved === "Yes") {
                drugs.push(value.name);
            }
        }
        return drugs;
    }
    
    function underTreatment(value, sensitiveName, keys, extendLetters) {
        
        if(keys.length === extendLetters.length) {
            
            if(keys.length) {
                var key = keys[0];
           
                if(isArray(value[key])) {
                    var valueL = value[key].length;
                    _.each(value[key], function(value1, index1) {
                        var _keys = $.extend(true, [], keys),
                            _extendLetters = $.extend(true, [], extendLetters),
                            shifted =  _extendLetters.shift();
                            
                        _keys.shift();
                        sensitiveName = 
                            underTreatment(
                                value1, 
                                sensitiveName,
                                _keys,
                                _extendLetters);
                        sensitiveName += (index1 + 1) === valueL ? "" : (" "+ shifted +" ");
                    });
                }else {
                    if(keys.length > 1) {
                        keys.shift();
                        extendLetters.shift();
                        sensitiveName = 
                            underTreatment(
                                value[key],
                                sensitiveName,
                                keys,
                                extendLetters);
                    }else {
                        var _extraInfo = "";
                        if (value[key].fda_approved === "Yes") {
                            _extraInfo = "<img src='resources/img/approved.svg' class='icon'></img>";
                        }
                        sensitiveName += value[key].name + _extraInfo;
                    }
                }
            }else {
                var _extraInfo = "";
                if (value.fda_approved === "Yes") {
                    _extraInfo = "<img src='resources/img/approved.svg' class='icon'></img>";
                }
                sensitiveName += value.name + _extraInfo;
            }
            return sensitiveName;
        }else {
            console.log("Error, length of keys not equal to length of extend letters");
            return false;
        }
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
    
    function init(tumorTypes, params) {
        initHTML(tumorTypes);
        if(typeof params !== "undefined" && params) {
            JqueryEvents.setVarientParams(params);
            variantSearch(params);
        }
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
        init: init,
        variantSearch: variantSearch
    };
})();