angular.module('oncokb').factory('TumorType', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('tumorType.json');
    }

    function getFromFile() {
        return $http.get('/data/tumorType.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokb').factory('Gene', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('gene.json');
    }

    function getFromFile() {
        return $http.get('/data/gene.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokb').factory('Alteration', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('alteration.json');
    }

    function getFromFile() {
        return $http.get('/data/alteration.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokb').factory('Users', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('users.json');
    }

    function getFromFile() {
        return $http.get('/data/users.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokb').factory('OncoTreeTumorTypes', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('oncoTreeTumorTypes.json');
    }

    function getFromFile() {
        return $http.get('/data/oncoTreeTumorTypes.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokb').factory(('SearchVariant'), ['$http', function($http) {
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
        return $http.get('/data/annotation.xml');
    }

    return {
        getAnnotation: getAnnotation,
        postAnnotation: postAnnotation,
        annotationFromFile: annotationFromFile
    };
}]);

angular.module('oncokb').factory('GenerateDoc', ['$http',  function ($http) {
    'use strict';
    var transform = function(data){
        return $.param(data);
    };
    
    function getDoc(params) {
        return $http.post(
            'generateGoogleDoc', 
            {'reportContent':JSON.stringify(params)},
            {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            });
    }
    
    function createFolder(params) {
        return $http.post(
            'createGoogleFolder',
            params,
            {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            });
    }
    return {
        getDoc: getDoc,
        createFolder: createFolder
    };
}]);