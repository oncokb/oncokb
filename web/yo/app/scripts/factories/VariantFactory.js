var $ = window.$;

angular.module('oncokbApp').factory('TumorType', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';

    function getFromServer() {
        return $http.get(OncoKB.config.apiLink + 'tumorType.json');
    }

    return {
        getFromServer: getFromServer
    };
}]);

angular.module('oncokbApp').factory('Gene', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';

    function getFromServer() {
        return $http.get(OncoKB.config.curationLink + 'gene.json');
    }

    return {
        getFromServer: getFromServer
    };
}]);

angular.module('oncokbApp').factory('DataSummary', ['$http', function($http) {
    'use strict';

    function getFromServer() {
        return $http.get('data/summary.json');
    }
    function getGeneType() {
        return $http.get(OncoKB.config.publicApiLink + 'genes');
    }
    function getEvidenceByType(type) {
        return $http.get(OncoKB.config.publicApiLink + 'evidences/lookup?source=oncotree&evidenceTypes=' + type);
    }
    return {
        getFromServer: getFromServer,
        getGeneType: getGeneType,
        getEvidenceByType: getEvidenceByType
    };
}]);

angular.module('oncokbApp').factory('Drugs', ['$http', 'OncoKB', function ($http, OncoKB) {
    'use strict';

    function searchDrugs(keyword) {
        return $http.get(OncoKB.config.privateApiLink + 'search/drugs?query=' + keyword);
    }

    return {
        searchDrugs: searchDrugs
    }
}]);

angular.module('oncokbApp').factory('Alteration', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';

    function getFromServer() {
        return $http.get(OncoKB.config.apiLink + 'alteration.json');
    }

    return {
        getFromServer: getFromServer
    };
}]);

angular.module('oncokbApp').factory('DriveOncokbInfo', ['$http', 'OncoKB', function($http, OncoKB) {
    'use strict';

    function getFromServer() {
        return $http.get(OncoKB.config.curationLink + 'oncokbInfo.json');
    }

    return {
        getFromServer: getFromServer
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

    function lookupVariants(body) {
        return $http.post(OncoKB.config.publicApiLink + 'variants/lookup', body);
    }

    return {
        getAnnotation: getAnnotation,
        postAnnotation: postAnnotation,
        annotationFromFile: annotationFromFile,
        lookupVariants: lookupVariants
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

angular.module('oncokbApp').factory('DriveAnnotation', ['$http', 'OncoKB', '_', function($http, OncoKB, _) {
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

    function updateEvidenceTreatmentPriorityBatch(data) {
        return $http.post(
            OncoKB.config.apiLink + 'evidences/priority/update',
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
        var validPubMedIDs = [];
        _.each(pubMedIDs, function(pubMedID) {
            if (!_.isNaN(Number(pubMedID))) {
                validPubMedIDs.push(pubMedID);
            }
        });
        return $http.get('https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&retmode=json&id=' + validPubMedIDs.join(','));
    }

    function getClinicalTrial(nctIds) {
        if (!nctIds || !_.isArray(nctIds) || nctIds.length === 0) {
            return {};
        } else {
            return $http.get(OncoKB.config.privateApiLink + 'utils/validation/trials?nctIds=' + nctIds.join());
        }
    }

    return {
        updateGene: updateGene,
        updateGeneType: updateGeneType,
        deleteEvidences: deleteEvidences,
        updateVUS: updateVUS,
        updateEvidenceBatch: updateEvidenceBatch,
        updateEvidenceTreatmentPriorityBatch: updateEvidenceTreatmentPriorityBatch,
        getEvidencesByUUID: getEvidencesByUUID,
        getEvidencesByUUIDs: getEvidencesByUUIDs,
        getPubMedArticle: getPubMedArticle,
        getClinicalTrial: getClinicalTrial
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

    function getTumorTypeByMainType(mainType) {
        return $http.get(OncoKB.config.oncoTreeLink +
            'tumorTypes/search/maintype/' + mainType + '?exactMatch=true&version=' + OncoKB.config.oncoTreeVersion);
    }

    function getMainTypes() {
        return $http.get(OncoKB.config.privateApiLink + 'utils/oncotree/mainTypes');
    }

    function getSubTypes() {
        return $http.get(OncoKB.config.privateApiLink + 'utils/oncotree/subtypes');
    }

    return {
        getTumorTypeByMainType: getTumorTypeByMainType,
        getMainTypes: getMainTypes,
        getSubTypes: getSubTypes
    };
}]);

angular.module('oncokbApp').factory('ApiUtils', ['$http', function($http) {
    'use strict';

    function getIsoforms(type) {
        if (type === 'msk') {
            return $http.get('data/isoformMskcc.json');
        }
        return $http.get('data/isoformUniport.json');
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
angular.module('oncokbApp')
    .factory('ReviewResource', ['$http', 'OncoKB', function() {
        'use strict';
        return {
            mostRecent: {}, // uuid string is the key, and value is an object with updateTime and updatedBy
            // the following attributes will be arrays with uuids as content
            accepted: [], // accepted section
            rejected: [], // rejected section
            rollback: [], // rolledback items
            loading: [], // loading section
            inside: [], // the items that is inside an added or removed section
            updated: [], // content updated sections
            nameChanged: [], // name changed sections
            added: [], // newly added sections
            removed: [], // deleted sections
            precise: [], // the exact item that has been changed
            reviewObjs: {}
        };
    }]);
angular.module('oncokbApp')
    .factory('FirebaseModel', ['$rootScope', function($rootScope) {
        'use strict';
        function getUUID() {
            return UUIDjs.create(4).toString();
        };
        function createTIs() {
            var result = [];
            for (var i = 0; i < 4; i++) {
                var ti = new TI();
                switch(i) {
                    case 0:
                        ti.type = 'SS';
                        ti.name = 'Standard implications for sensitivity to therapy';
                        break;
                    case 1:
                        ti.type = 'SR';
                        ti.name = 'Standard implications for resistance to therapy';
                        break;
                    case 2:
                        ti.type = 'IS';
                        ti.name = 'Investigational implications for sensitivity to therapy';
                        break;
                    case 3:
                        ti.type = 'IR';
                        ti.name = 'Investigational implications for resistance to therapy';
                        break;
                }
                result.push(ti);
            }
            return result;
        }
        function Gene(name) {
            this.name = name;
            this.summary = '';
            this.summary_uuid = getUUID();
            this.background = '';
            this.background_uuid = getUUID();
            this.isoform_override = '';
            this.dmp_refseq_id = '';
            this.type = {
                tsg: '',
                tsg_uuid: getUUID(),
                ocg: '',
                ocg_uuid: getUUID()
            };
            this.type_uuid = getUUID();
            this.mutations_uuid = getUUID();
        }
        function Mutation(name) {
            this.name = name;
            this.name_uuid = getUUID();
            this.mutation_effect = {
                oncogenic: '',
                oncogenic_uuid: getUUID(),
                effect: '',
                effect_uuid: getUUID(),
                description: '',
                description_uuid: getUUID(),
                short: ''
            };
            this.mutation_effect_uuid = getUUID();
            this.tumors_uuid = getUUID();
        };
        function Tumor(cancerTypes) {
            this.cancerTypes = cancerTypes;
            this.cancerTypes_uuid = getUUID();
            this.summary = '';
            this.summary_uuid = getUUID();
            this.prognostic = {
                level: '',
                level_uuid: getUUID(),
                description: '',
                description_uuid: getUUID(),
                short: ''
            };
            this.prognostic_uuid = getUUID();
            this.diagnostic = {
                level: '',
                level_uuid: getUUID(),
                description: '',
                description_uuid: getUUID(),
                short: ''
            };
            this.diagnostic_uuid = getUUID();
            this.TIs = createTIs();
        };
        function Cancertype(mainType, subtype, code) {
            this.mainType = mainType;
            this.subtype = subtype;
            this.code = code;
        }
        function TI() {
            this.name =  '';
            this.name_uuid = getUUID();
            this.type = '';
            this.treatments = [];
            this.treatments_uuid = getUUID();
        }
        function Treatment(name) {
            this.name = name;
            this.name_uuid = getUUID();
            this.level = '';
            this.level_uuid = getUUID();
            this.propagation = '';
            this.propagation_uuid = getUUID();
            this.indication = '';
            this.indication_uuid = getUUID();
            this.description = '';
            this.description_uuid = getUUID();
            this.short = '';
        };
        function Comment(userName, email, content) {
            this.date = (new Date()).getTime().toString();
            this.userName = userName;
            this.email = email;
            this.content = content;
            this.resolved = 'false';
        }
        function VUSItem(name, userName, userEmail) {
            this.name = name;
            this.time = {
                by: {
                    name: userName,
                    email: userEmail
                },
                value: new Date().getTime()
            };
        }
        function TimeStamp(userName, userEmail) {
            this.by = {
                name: userName,
                email: userEmail
            };
            this.value = (new Date()).getTime().toString();
        }
        function Meta() {
            this.lastModifiedBy = $rootScope.me.name;
            this.lastModifiedAt = (new Date()).getTime().toString();
            this.review = {
                currentReviewer: ''
            };
        }
        function Setting() {
            this.enableReview = true;
        }
        function Drug(drugName, ncitCode, synonyms, ncitName){
            this.drugName = drugName;
            this.ncitCode = ncitCode;
            this.uuid = getUUID();
            this.description = '';
            this.ncitName = ncitName;
            this.synonyms = synonyms;
        }
        return {
            Gene: Gene,
            Mutation: Mutation,
            Tumor: Tumor,
            Treatment: Treatment,
            Comment: Comment,
            Cancertype: Cancertype,
            VUSItem: VUSItem,
            TimeStamp: TimeStamp,
            Meta: Meta,
            Setting: Setting,
            Drug: Drug
        };
    }]);
