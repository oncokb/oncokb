angular.module('webappApp').factory('TumorType', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('tumorType.json');
    }

    function getFromFile() {
        return $http.get('data/tumorType.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('webappApp').factory(('SearchVariant'), ['$http', function($http) {
    'use strict';
    function getAnnotation(params) {
        var _params = angular.copy(params),
            _url = 'var_annotation?';

        for(var _key in _params) {
            if(typeof _params[_key] !== 'undefined' && _params[_key] && _params[_key] !== '') {
                _url += _key + '=' + _params[_key] + '&';
            }
        }
        _url = _url.substring(0, _url.length - 1);
        console.log(_url);
        return $http.get(_url);
    }

    function postAnnotation(params) {
        return $http({
            url: 'var_annotation', 
            method: 'POST',
            params: params
        });
    }

    function annotationFromFile() {
        return $http.get('data/annotation.xml');
    }

    return {
        getAnnotation: getAnnotation,
        postAnnotation: postAnnotation,
        annotationFromFile: annotationFromFile
    };
}]);

angular.module('webappApp').factory('GenerateDoc', ['$http',  function ($http) {
    'use strict';
    var transform = function(data){
        return $.param(data);
    }
    
    function getDoc(params) {
        return $http.post(
            'generateGoogleDoc', 
            {'reportContent':JSON.stringify(params)},
            {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            });
    }

    return {
        getDoc: getDoc
    };
}]);