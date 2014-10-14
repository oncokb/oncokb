oncokbApp.factory('TumorType', ['$http',  function ($http) {
    'use strict';

    function getFromServe() {
        return $http.get('tumorType.json');
    }

    function getFromFile() {
        return $http.get('data/tumorType.json');
    }

    return {
        getFromServe: getFromServe,
        getFromFile: getFromFile
    };
}]);

oncokbApp.factory(('SearchVariant'), ['$http', function($http) {

}]);