var $ = window.$;

angular.module('oncokbApp').factory('TumorType', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';

    function getFromServer() {
        return $http.get(OncoKB.config.apiLink + 'tumorType.json');
    }

    function getFromFile() {
        return $http.get('data/tumorType.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokbApp').factory('Gene', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';

    function getFromServer() {
        return $http.get(OncoKB.config.curationLink + 'gene.json');
    }

    function getFromFile() {
        return $http.get('data/gene.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokbApp').factory('DataSummary', ['$http', function($http) {
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

angular.module('oncokbApp').factory('Alteration', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';

    function getFromServer() {
        return $http.get(OncoKB.config.apiLink + 'alteration.json');
    }

    function getFromFile() {
        return $http.get('data/alteration.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokbApp').factory('DriveOncokbInfo', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';

    function getFromServer() {
        return $http.get(OncoKB.config.curationLink + 'oncokbInfo.json');
    }

    function getFromFile() {
        return $http.get('data/oncokbInfo.json');
    }

    return {
        getFromServer: getFromServer,
        getFromFile: getFromFile
    };
}]);

angular.module('oncokbApp').config(function($httpProvider) {
    $httpProvider.interceptors.push('xmlHttpInterceptor');
}).factory(('SearchVariant'), ['$http', 'OncoKB', function($http, OncoKB) {
    function getAnnotation(params) {
        var _params = angular.copy(params);
        var _url = OncoKB.config.apiLink + 'var_annotation?';

        for (var _key in _params) {
            if (typeof _params[_key] !== 'undefined' && _params[_key] && _params[_key] !== '') {
                _url += _key + '=' + _params[_key] + '&';
            }
        }
        _url = _url.substring(0, _url.length - 1);
        return $http.get(_url);
    }

    function postAnnotation(params) {
        return $http({
            url: OncoKB.config.apiLink + 'var_annotation',
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

angular.module('oncokbApp').factory('GenerateDoc', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';
    var transform = function(data) {
        return $.param(data);
    };

    function getDoc(params) {
        return $http.post(
            OncoKB.config.apiLink + 'generateGoogleDoc',
            {reportParams: JSON.stringify(params)},
            {
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            });
    }

    function createFolder(params) {
        return $http.post(
            OncoKB.config.apiLink + 'createGoogleFolder',
            params,
            {
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            });
    }

    return {
        getDoc: getDoc,
        createFolder: createFolder
    };
}]);

angular.module('oncokbApp').factory('SendEmail', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';
    var transform = function(data) {
        return $.param(data);
    };

    function init(params) {
        return $http.post(
            OncoKB.config.curationLink + 'sendEmail',
            params,
            {
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            });
    }

    return {
        init: init
    };
}]);

angular.module('oncokbApp').factory('DriveAnnotation', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';
    var transform = function(data) {
        return $.param(data);
    };

    function updateGene(data) {
        return $http.post(
            OncoKB.config.apiLink + 'driveAnnotation',
            data,
            {
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            });
    }

    function updateGeneType(hugoSymbol, data) {
        return $http.post(
            OncoKB.config.apiLink + 'genes/update/' + hugoSymbol,
            data,
            {
                transformResponse: function(result) {
                    return {status: result};
                }
            });
    }

    function updateEvidence(uuid, data) {
        return $http.post(
            OncoKB.config.apiLink + 'evidences/update/' + uuid,
            data,
            {
                transformResponse: function(result) {
                    return {status: result};
                }
            });
    }

    function deleteEvidences(data) {
        return $http.post(
            OncoKB.config.apiLink + 'evidences/delete',
            data);
    }

    function updateVUS(hugoSymbol, data) {
        return $http.post(
            OncoKB.config.apiLink + 'vus/update/' + hugoSymbol,
            data,
            {
                transformResponse: function(result) {
                    return {status: result};
                }
            });
    }
    function updateEvidenceBatch(data) {
        return $http.post(
            OncoKB.config.apiLink + 'evidences/update',
            data,
            {
                transformResponse: function(result) {
                    return {status: result};
                }
            });
    }
    function getEvidencesByUUID(uuid) {
        return $http.get(
            OncoKB.config.publicApiLink + 'evidences/' + uuid,
            {
                transformResponse: function(result) {
                    return {status: result};
                }
            });
    }
    function getEvidencesByUUIDs(uuids) {
        return $http.post(
            OncoKB.config.publicApiLink + 'evidences',
            uuids,
            {
                transformResponse: function(result) {
                    return {status: result};
                }
            });
    }
    function getPubMedArticle(pubMedIDs) {
        return $http.get('https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&retmode=json&id=' + pubMedIDs.join(','));
    }
    return {
        updateGene: updateGene,
        updateGeneType: updateGeneType,
        updateEvidence: updateEvidence,
        deleteEvidences: deleteEvidences,
        updateVUS: updateVUS,
        updateEvidenceBatch: updateEvidenceBatch,
        getEvidencesByUUID: getEvidencesByUUID,
        getEvidencesByUUIDs: getEvidencesByUUIDs,
        getPubMedArticle: getPubMedArticle
    };
}]);

angular.module('oncokbApp').factory('InternalAccess', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';
    function hasAccess() {
        return $http.get(OncoKB.config.apiLink + 'access');
    }

    return {
        hasAccess: hasAccess
    };
}]);

angular.module('oncokbApp').factory('Cache', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';
    var transform = function(data) {
        return $.param(data);
    };

    function setStatus(status) {
        return $http.post(
            OncoKB.config.apiLink + 'cache',
            {cmd: status},
            {
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            }
        );
    }

    function getStatus() {
        return $http({
            url: OncoKB.config.apiLink + 'cache',
            method: 'GET',
            params: {cmd: 'getStatus'}
        });
    }

    function updateGene(hugoSymbol) {
        var transform = function(data) {
            return $.param(data);
        };
        return $http.post(
            OncoKB.config.apiLink + 'cache', {
                cmd: 'updateGene',
                hugoSymbol: hugoSymbol
            }, {
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                transformRequest: transform
            });
    }

    return {
        reset: function() {
            return setStatus('reset');
        },
        enable: function() {
            return setStatus('enable');
        },
        disable: function() {
            return setStatus('disable');
        },
        getStatus: getStatus,
        updateGene: updateGene
    };
}]);

angular.module('oncokbApp').factory('OncoTree', ['$http', 'OncoKB', '_', function($http, OncoKB, _) {
    'use strict';

    function getMainType() {
        return $http.get(OncoKB.config.oncoTreeLink + 'mainTypes?version=oncokb');
    }

    function getTumorTypeByMainType(mainType) {
        return $http.get(OncoKB.config.oncoTreeLink +
            'tumorTypes/search/maintype/' + mainType + '?exactMatch=true&version=oncokb');
    }

    function getTumorType(type, query, exactMatch) {
        if (!type || !query) {
            return null;
        }
        exactMatch = _.isBoolean(exactMatch) ? exactMatch : true;
        return $http.get(OncoKB.config.oncoTreeLink +
            'tumorTypes/search/' + type + '/' + query + '?exactMatch=' + exactMatch + '&version=oncokb');
    }

    function getTumorTypesByMainTypes(mainTypes) {
        var queries = _.map(mainTypes, function(mainType) {
            return {
                type: 'maintype',
                query: mainType,
                exactMatch: true
            };
        });
        return $http.post(OncoKB.config.oncoTreeLink + 'tumorTypes/search',
            {
                queries: queries,
                version: 'oncokb'
            }, {
                headers: {'Content-Type': 'application/json'}
            });
    }

    return {
        getMainType: getMainType,
        getTumorTypeByMainType: getTumorTypeByMainType,
        getTumorType: getTumorType,
        getTumorTypesByMainTypes: getTumorTypesByMainTypes
    };
}]);

angular.module('oncokbApp').factory('ApiUtils', ['$http', function($http) {
    'use strict';

    function getIsoforms() {
        return $http.get('data/isoformMskcc.json');
    }

    function getOncogeneTSG() {
        return $http.get('data/oncogeneTSG.json');
    }

    return {
        getIsoforms: getIsoforms,
        getOncogeneTSG: getOncogeneTSG
    };
}]);

angular.module('oncokbApp')
    .factory('PrivateApiUtils', ['$http', 'OncoKB', function($http, OncoKB) {
        'use strict';

        function getSuggestedVariants() {
            return $http.get(OncoKB.config.privateApiLink +
                'utils/suggestedVariants');
        }

        function isHotspot(hugoSymbol, variant) {
            if (!hugoSymbol || !variant) {
                return null;
            }
            return $http.get(OncoKB.config.privateApiLink +
                'utils/isHotspot?hugoSymbol=' +
                hugoSymbol + '&variant=' + variant);
        }

        return {
            getSuggestedVariants: getSuggestedVariants,
            isHotspot: isHotspot
        };
    }]);
