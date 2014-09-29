var BootStrapPanelCollapse = function() {
    var element = $(document.createElement('div')),
        params = {},
        styles = {};
    
    function init(_params) {
        params = _params.divs;
        styles = _params.styles;
        initDiv(params.Id, params.parentId);
        initTitle(params.title);
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
    
    function initTitle(content) {
        element.find(".panel-title a").text(content);
    }
    
    function initTitleAddition(content) {
        element.find(".panel-title").append(content);
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