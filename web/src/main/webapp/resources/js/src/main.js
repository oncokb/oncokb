var appVersion = "10142014-1";

(function() {
    "use strict";
    
    require.config({
        urlArgs: appVersion
    });
    
    require(["../lib/jquery-1.11.1.min"], function(){
        require([
            '../lib/d3.min',
            '../lib/bootstrap',
            '../lib/chosen.jquery',
            '../lib/jquery.qtip.min',
            '../lib/jquery.xml2json',
            '../lib/underscore-min'
            ],
            function(){
                require(
                    constructJSarray(), 
                    function(){
                        callback();
                    }
                );
            }
        );
    });
    
    function callback() {
        Init.init();
    }
    
    //Put all self created js files into array
    function constructJSarray() {
        var jsFiles = [],
            folder = {
                components: {
                    bootstrap: [
                        "PanelCollapse"
                    ]
                },
                model: [
                    'DataProxy',
                    'Utils'
                ],
                view: [
                    'Init', 
                    'JqueryEvents',
                    'Tree',
                    'DocumentEvents',
                    'VariantsAnnotation'
                ]
            };
        
        jsFiles = buildArray(folder, [], "");
        
        return jsFiles;
    }
    
    function buildArray(obj, array, path) {
        if(obj instanceof Array) {
            for(var i = 0, objL = obj.length; i < objL; i++) {
                buildArray(obj[i], array, path);
            }
        }else if(typeof obj === 'object'){
            for(var key in obj){
                buildArray(obj[key], array, path + (path !== ""? "/" : "") + key);
            }
        }else {
            array.push(path + "/" + obj);
        }
        
        return array;
    }
})();