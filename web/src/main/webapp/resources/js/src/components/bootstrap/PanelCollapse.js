var BootStrapPanelCollapse = function() {
    var element = $(document.createElement('div')),
        params = {},
        styles = {};
    
    function init(_params) {
        params = _params.divs;
        styles = _params.styles;
        if(params.special) {
            initSpecialDiv(params.Id, params.parentId);
            initSpecialTitle(params.title)
        }else {
            initDiv(params.Id, params.parentId);
            initTitle(params.title);
        }
        if(params.titleAddition) initTitleAddition(params.titleAddition);
        initBodyContent(params.body);
    }
    
    
    function initDiv(Id, parentId) {
        element.addClass('panel panel-default');
        
        element.append("<div class='panel-heading'>"
                +"<h4 class='panel-title'>"
                +"<a data-toggle='collapse'"
                +" data-parent='"+ parentId +"' href='#"+ Id +"'></a></h4></div>"
                +"<div id='"+ Id +"' class='panel-coollapse collapse'>" 
                +"<div class='panel-body'></div></div>");
    }
    
    function initSpecialDiv(Id, parentId) {
        element.append("<div class='heading'>"
                +"<a data-toggle='collapse'"
                +" data-parent='"+ parentId +"' href='#"+ Id +"'><img src='resources/img/add.svg' class='icon iconExpand'></img><img src='resources/img/subtract.svg' class='icon iconCollapse'></img></a></div>"
                +"<div id='"+ Id +"' class='panel-coollapse collapse'>" 
                +"<div class='panel-body'></div></div>");
    }
    
    function initSpecialTitle(content) {
        element.find(".heading a").before("<b>" + content + "</b>");
    }
    
    function initTitle(content) {
        element.find(".panel-heading a").text(content);
    }
    
    function initTitleAddition(content) {
        element.find(".panel-heading a").after(content);
    }
    
    function initBodyContent(content) {
        element.find(".panel-body").append(content);
    }
    
    return {
        init: init,
        append: function(target, content) {
            element.find(target).append(content);
        },
        empty: function(target) {
            element.find(target).empty();
        },
        find: function(target) {
            return element.find(target);
        },
        get:function() {
            return element;
        }
    };
};