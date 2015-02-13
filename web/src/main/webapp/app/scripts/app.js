'use strict';

/**
 * @ngdoc overview
 * @name oncokb
 * @description
 * # oncokb
 *
 * Main module of the application.
 */
var OncoKB = {};

//Global variables
OncoKB.global = {};
//OncoKB.global.genes
//OncoKB.global.alterations
//OncoKB.global.tumorTypes
//OncoKB.global.treeEvidence
//OncoKB.global.processedData

//Variables for tree tab
OncoKB.tree = {};
//processedData

OncoKB.config = {
    clientId: '19500634524-r0jf2v73enc62qo83cs5rnrm7eb0qndt.apps.googleusercontent.com',
    scopes: [
        'https://www.googleapis.com/auth/plus.login',
        'https://www.googleapis.com/auth/plus.profile.emails.read',
        'https://www.googleapis.com/auth/drive',
        'https://www.googleapis.com/auth/drive.file',
        'https://www.googleapis.com/auth/drive.install'
    ],
    folderId: '0BzBfo69g8fP6fmdkVnlOQWdpLWtHdFM4Ml9vNGxJMWpNLTNUM0lhcEc2MHhKNkVfSlZjMkk',
    userRoles: {
        'public': 1, // 0001
        'user':   2, // 0010
        'curator':4, // 0100
        'admin':  8  // 1000
    },
    accessLevels: {}
}

OncoKB.config.accessLevels.public = OncoKB.config.userRoles.public | OncoKB.config.userRoles.user  | OncoKB.config.userRoles.curator | OncoKB.config.userRoles.admin;
OncoKB.config.accessLevels.user = OncoKB.config.userRoles.user  | OncoKB.config.userRoles.curator | OncoKB.config.userRoles.admin;
OncoKB.config.accessLevels.curator = OncoKB.config.userRoles.curator | OncoKB.config.userRoles.admin;
OncoKB.config.accessLevels.admin = OncoKB.config.userRoles.admin;


OncoKB.curateInfo = {
    'Gene': {
        'name': {
            type: 'string'
        },
        'summary': {
            type: 'string',
            display: 'Summary'
        },
        'background': {
            type: 'string',
            display: 'Background'
        },
        'mutations': {
            type: 'list'
        },
        'curators': {
            type: 'list'
        }
    },
    'Mutation': {
        'name': {
            type: 'string'
        },
        'oncogenic': {
            type: 'string',
            display: 'Oncogenic'
        },
        'effect': {
            type: 'string',
            display: 'Mutation Effect'
        },
        'description': {
            type: 'string',
            display: 'Description of mutation effect'
        },
        'tumors': {
            type: 'list'
        }
    },
    'Curator': {
        'name': {
            type: 'string'
        },
        'email': {
            type: 'string'
        }
    },
    'NCCN': {
        'disease': {
            type: 'string',
            display: 'Diease'
        },
        'version': {
            type: 'string',
            display: 'Version'
        },
        'pages': {
            type: 'string',
            display: 'Pages'
        },
        'category': {
            type: 'string',
            display: 'Recommendation category'
        },
        'description': {
            type: 'string',
            display: 'Description of evidence'
        }
    },
    'InteractAlts': {
        'alterations': {
            type: 'string',
            display: 'Alterations'
        },
        'description': {
            type: 'string',
            display: 'Description of evidence'
        }
    },
    'Tumor': {
        'name': {
            type: 'string'
        },
        'prevalence': {
            type: 'string',
            display: 'Prevalence'
        },
        'progImp': {
            type: 'string',
            display: 'Prognostic implications'
        },
        'trials': {
            type: 'list'
        },
        'TI': {
            type: 'list'
        },
        'nccn': {
            type: 'NCCN'
        },
        'interactAlts': {
            type: 'InteractAlts'
        }
    },
    'TI': {
        'name': {
            type: 'string'
        },
        'types': {
        },
        'treatments': {
            type: 'list'
        },
        'description': {
            type: 'string',
            display: 'Description of evidence'
        }
    },
    'Treatment': {
        'name': {
            type: 'string'
        },
        'type': {
            type: 'string'
        },
        'level': {
            type: 'string',
            display: 'Highest level of evidence'
        },
        'indication': {
            type: 'string',
            display: 'Approved Indication'
        },
        'description': {
            type: 'string',
            display: 'Description of evidence'
        },
        'trials': {
            type: 'list'
        }
    }
};

OncoKB.setUp = function(object) {
    if(OncoKB.curateInfo.hasOwnProperty(object.attr)){
        for(var key1 in OncoKB.curateInfo[object.attr]){
            if(OncoKB.curateInfo[object.attr][key1].hasOwnProperty('display')) {
                object[key1].display = OncoKB.curateInfo[object.attr][key1].display;
            }
            if(object[key1].type === 'EditableString') {
                Object.defineProperty(object[key1], 'text', {
                    set: object[key1].setText,
                    get: object[key1].getText
                });
            }
        }
    }
};

OncoKB.initialize = function() {
    var nonSetUp = ['TI'];
    var keys = _.keys(OncoKB.curateInfo);
    var keysL = keys.length;

    for (var i = 0; i < keysL; i++) {
        var _key = keys[i];
        var _keys = _.keys(OncoKB.curateInfo[_key]);
        var _keysL = _keys.length;

        //Google Realtime data module for annotation curation
        //Gene is the main entry
        OncoKB[_key] = function() {};

        OncoKB[_key].prototype.attr = _key;

        OncoKB[_key].prototype.setUp = function() {
            OncoKB.setUp(this);
        };

        OncoKB[_key].prototype.initialize = function () {
            var model = gapi.drive.realtime.custom.getModel(this);
            var id = this.attr;
            var atrrs = _.keys(OncoKB.curateInfo[id]);
            var atrrsL = atrrs.length;

            for(var j = 0; j < atrrsL; j++) {
                var __key = atrrs[j];
                if(__key === 'types' && id === 'TI') {
                    this.types = model.createMap({'status': '0', 'type': '0'});
                }else {
                    if(OncoKB.curateInfo[id][__key].hasOwnProperty('type')) {
                        switch (OncoKB.curateInfo[id][__key].type) {
                            case 'string':
                                this[__key] = model.createString('');
                                break;
                            case 'list':
                                this[__key] = model.createList();
                                break;
                            default:
                                this[__key] = model.create(OncoKB.curateInfo[id][__key].type);
                                break;
                        }
                    }
                }
            }
            this.setUp();
        }

        //Register every field of OncoKB into document
        for(var j=0; j<_keysL; j++) {
            OncoKB[_key].prototype[_keys[j]] = gapi.drive.realtime.custom.collaborativeField(_key + '_' + _keys[j]);
        }

        //Register custom type
        gapi.drive.realtime.custom.registerType(OncoKB[_key], _key);

        //Set realtime API initialize function for each type, this function only runs one time when create new data model
        gapi.drive.realtime.custom.setInitializer(OncoKB[_key], OncoKB[_key].prototype.initialize);

        //Set on loaded function, this function will be loaded everyone the document been pulled from google drive
        if(nonSetUp.indexOf(_key) !== -1) {
            gapi.drive.realtime.custom.setOnLoaded(OncoKB[_key]);
        }else {
            gapi.drive.realtime.custom.setOnLoaded(OncoKB[_key], OncoKB[_key].prototype.setUp);
        }
    }
};

var oncokbApp = angular
 .module('oncokb', [
   'ngAnimate',
   'ngCookies',
   'ngResource',
   'ngRoute',
   'ngSanitize',
   'ngTouch',
   'ui.bootstrap',
   'localytics.directives',
   'dialogs.main',
   'dialogs.default-translations',
   'RecursionHelper',
   'angularFileUpload',
   'xml',
   'contenteditable'
 ])
 .constant('config', OncoKB.config)
 .value('user', {
    name: 'N/A',
    email: 'N/A'
 })
 .constant('gapi', window.gapi)
 .config(function ($routeProvider, dialogsProvider, $animateProvider, x2jsProvider, config) {
    var access = config.accessLevels;

    $routeProvider
        .when('/', {
           templateUrl: 'views/welcome.html',
           access: access.public
        })
        .when('/tree', {
            templateUrl: 'views/tree.html',
            controller: 'TreeCtrl',
            access: access.admin
        })
        .when('/variant', {
            templateUrl: 'views/variant.html',
            controller: 'VariantCtrl',
            reloadOnSearch: false,
            access: access.admin
        })
        .when('/reportGenerator', {
            templateUrl: 'views/reportgenerator.html',
            controller: 'ReportgeneratorCtrl',
            access: access.admin
        })
        .when('/genes', {
            templateUrl: 'views/genes.html',
            controller: 'GenesCtrl',
            access: access.curator
        })
        .when('/gene/:geneName', {
            templateUrl: 'views/gene.html',
            controller: 'GeneCtrl',
            access: access.curator,
            resolve: {
              realtimeDocument: function(loadFile){
                return loadFile();
              }
            }
        })
        .otherwise({
            redirectTo: '/'
        });

  dialogsProvider.useBackdrop(true);
  dialogsProvider.useEscClose(true);
  dialogsProvider.useCopy(false);
  dialogsProvider.setSize('md');

  $animateProvider.classNameFilter(/^((?!(fa-spinner)).)*$/);
  
  x2jsProvider.config = {
    /*
    escapeMode               : true|false - Escaping XML characters. Default is true from v1.1.0+
    attributePrefix          : "<string>" - Prefix for XML attributes in JSon model. Default is "_"
    arrayAccessForm          : "none"|"property" - The array access form (none|property). Use this property if you want X2JS generates an additional property <element>_asArray to access in array form for any XML element. Default is none from v1.1.0+
    emptyNodeForm            : "text"|"object" - Handling empty nodes (text|object) mode. When X2JS found empty node like <test></test> it will be transformed to test : '' for 'text' mode, or to Object for 'object' mode. Default is 'text'
    enableToStringFunc       : true|false - Enable/disable an auxiliary function in generated JSON objects to print text nodes with text/cdata. Default is true
    arrayAccessFormPaths     : [] - Array access paths. Use this option to configure paths to XML elements always in "array form". You can configure beforehand paths to all your array elements based on XSD or your knowledge. Every path could be a simple string (like 'parent.child1.child2'), a regex (like /.*\.child2/), or a custom function. Default is empty
    skipEmptyTextNodesForObj : true|false - Skip empty text tags for nodes with children. Default is true.
    stripWhitespaces         : true|false - Strip whitespaces (trimming text nodes). Default is true.
    datetimeAccessFormPaths  : [] - Datetime access paths. Use this option to configure paths to XML elements for "datetime form". You can configure beforehand paths to all your array elements based on XSD or your knowledge. Every path could be a simple string (like 'parent.child1.child2'), a regex (like /.*\.child2/), or a custom function. Default is empty
    */
    attributePrefix : '$'
    };
 });

/**
 * Set up handlers for various authorization issues that may arise if the access token
 * is revoked or expired.
 */
angular.module('oncokb').run(['$rootScope', '$location', 'storage', 'access', 'config', function ($rootScope, $location, storage, Access, config) {
    $rootScope.user = {
        role: config.userRoles.public
    };

    // Error loading the document, likely due revoked access. Redirect back to home/install page
    $rootScope.$on('$routeChangeError', function () {
        $location.url('/');
    });

    // Token expired, refresh
    $rootScope.$on('oncokb.token_refresh_required', function () {
        storage.requireAuth(true).then(function () {
            // no-op
        }, function () {
            $location.url('/gene');
        });
    });

    $rootScope.$on("$routeChangeStart", function (event, next, current) {
        if (!Access.authorize(next.access)) {
            // if(Auth.isLoggedIn()){
                $location.path('/')
            // }else {
            //     $location.path('/');
            // }
        }
    });
}]);

/**
 * Bootstrap the app
 */
gapi.load('auth:client:drive-share:drive-realtime', function () {
    gapi.auth.init();
    OncoKB.initialize();

    angular.element(document).ready(function() {
        angular.bootstrap(document, ['oncokb']);
    });
});
