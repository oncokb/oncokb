oncokbApp.factory('TumorType', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('tumorType.json');
    }

    function getFromFile() {
        return $http.get('data/tumorType.json');
    }

    function getAnnotation(params) {
        var _params = angular.copy(params),
            _url = "var_annotation?";

        for(var _key in _params) {
            if(typeof _params[_key] !== 'undefined' && _params[_key] && _params[_key] !== '') {
                _url += _key + "=" + _params[_key] + "&";
            }
        }
        _url = _url.substring(0, _url.length - 1);
        console.log(_url);
        return $http.get(_url);
    }

    function postAnnotation(params) {
        return $http({
            url: "var_annotation", 
            method: "POST",
            params: params
        });
    }

    function annotationFromFile(params) {
        return $http.get('data/annotation.xml');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile,
        getAnnotation: getAnnotation,
        postAnnotation: postAnnotation,
        annotationFromFile: annotationFromFile
    };
}]);

oncokbApp.factory(('SearchVariant'), ['$http', function($http) {

}]);