var $ = window.$;

angular.module('oncokbApp').factory('TumorType', ['$http',  function ($http) {
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

angular.module('oncokbApp').factory('Gene', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('gene.json');
    }

    function getFromFile() {
        return $http.get('data/gene.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokbApp').factory('DataSummary', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('data/summary.json');
    }

    function getFromFile() {
        return $http.get('data/summary.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokbApp').factory('GeneStatus', ['$http',  function ($http) {
    'use strict';

    function getFromServer(params) {
        console.log(params);
        return $http({
            url: 'geneStatus.json',
            method: 'GET',
            params: {geneId: params.geneId || ''}
        });
    }

    function setToServer(params) {
        console.log(params);
        var transform = function(data){
            return $.param(data);
        };
        return $http.post(
            'geneStatus.json',
            {geneId: params.geneId || '', status: params.status || 'Not ready'},
            {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            });
    }

    function getFromFile() {
        return $http.get('data/geneStatus.json');
    }

    function setToFile(params) {
        console.log(params);
        return $http.get('data/geneStatus.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile,
        setToServer: setToServer,
        setToFile: setToFile
    };
}]);

angular.module('oncokbApp').factory('Alteration', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('alteration.json');
    }

    function getFromFile() {
        return $http.get('data/alteration.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokbApp').factory('OncoTreeTumorTypes', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('oncoTreeTumorTypes.json');
    }

    function getFromFile() {
        return $http.get('data/oncoTreeTumorTypes.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokbApp').factory('DriveOncokbInfo', ['$http',  function ($http) {
    'use strict';

    function getFromServer() {
        return $http.get('oncokbInfo.json');
    }

    function getFromFile() {
        return $http.get('data/oncokbInfo.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokbApp')
    .config(function ($httpProvider) {
        $httpProvider.interceptors.push('xmlHttpInterceptor');
    }).factory(('SearchVariant'), ['$http', function($http) {
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
        return $http.get('data/annotation.xml');
    }

    return {
        getAnnotation: getAnnotation,
        postAnnotation: postAnnotation,
        annotationFromFile: annotationFromFile
    };
}]);

angular.module('oncokbApp').factory('GenerateDoc', ['$http',  function ($http) {
    'use strict';
    var transform = function(data){
        return $.param(data);
    };

    function getDoc(params) {
        return $http.post(
            'generateGoogleDoc', 
            {'reportParams':JSON.stringify(params)},
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

angular.module('oncokbApp').factory('SendEmail', ['$http',  function ($http) {
    'use strict';
    var transform = function(data){
        return $.param(data);
    };

    function init(params) {
        return $http.post(
            'sendEmail', 
            params,
            {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            });
    }

    return {
        init: init
    };
}]);

angular.module('oncokbApp').factory('DriveAnnotation', ['$http',  function ($http) {
    'use strict';
    var transform = function(data){
        return $.param(data);
    };

    function updateGene(geneString) {
        return $http.post(
            'driveAnnotation',
            {'gene': geneString},
            {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            });
    }
    return {
        updateGene: updateGene
    };
}]);