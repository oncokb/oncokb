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
var gapi = window.gapi;

// Global variables
OncoKB.global = {};
// OncoKB.global.genes
// OncoKB.global.alterations
// OncoKB.global.tumorTypes
// OncoKB.global.treeEvidence
// OncoKB.global.processedData

// Variables for tree tab
OncoKB.tree = {};
// processedData

// OncoKB configurations, reading from config.json
// All contents here are pointing to few examples files.
OncoKB.config = {
    clientId: '', // Your client ID from google developer console
    scopes: [
        'https://www.googleapis.com/auth/plus.profile.emails.read',
        'https://www.googleapis.com/auth/drive.file'
    ],
    folderId: '0BzBfo69g8fP6NkgtWGZxd0NjcWs', // Example folder
    userRoles: {
        public: 1,
        user: 2,
        curator: 4,
        admin: 8
    },
    backupFolderId: '0BzBfo69g8fP6LWozVE56Mk1RYkU',  // Example backup folder
    users: '', // The google spreadsheet ID which used to manage the user info. Please share this file to the service email address with view permission.
    apiLink: 'legacy-api/',
    curationLink: 'legacy-api/',
    oncoTreeLink: 'http://oncotree.mskcc.org/oncotree/api/',
    testing: true
};

OncoKB.curateInfo = {
    Gene: {
        name: {
            type: 'string'
        },
        status: {
            type: 'string'
        },
        shortSummary: {
            type: 'string',
            display: 'Short description of summary'
        },
        summary: {
            type: 'string',
            display: 'Summary'
        },
        shortBackground: {
            type: 'string',
            display: 'Short description of background'
        },
        background: {
            type: 'string',
            display: 'Background'
        },
        mutations: {
            type: 'list'
        },
        curators: {
            type: 'list'
        },
        transcripts: {
            type: 'list'
        },
        type: {
            type: 'map'
        }
    },
    Mutation: {
        name: {
            type: 'string'
        },
        summary: {
            type: 'string',
            display: 'Description of summary'
        },
        shortSummary: {
            type: 'string',
            display: 'Description of oncogenicity'
        },
        oncogenic: {
            type: 'string',
            display: 'Oncogenic'
        },
        effect: {
            type: 'ME'
        },
        short: {
            type: 'string',
            display: 'Short description of mutation effect'
        },
        description: {
            type: 'string',
            display: 'Description of mutation effect'
        },
        tumors: {
            type: 'list'
        }
    },
    Curator: {
        name: {
            type: 'string'
        },
        email: {
            type: 'string'
        }
    },
    NCCN: {
        therapy: {
            type: 'string',
            display: 'Therapy'
        },
        disease: {
            type: 'string',
            display: 'Disease'
        },
        version: {
            type: 'string',
            display: 'Version'
        },
        pages: {
            type: 'string',
            display: 'Pages'
        },
        category: {
            type: 'string',
            display: 'NCCN category of evidence and consensus'
        },
        short: {
            type: 'string',
            display: 'Short description of evidence'
        },
        description: {
            type: 'string',
            display: 'Description of evidence'
        }
    },
    InteractAlts: {
        alterations: {
            type: 'string',
            display: 'Alterations'
        },
        short: {
            type: 'string',
            display: 'Short description'
        },
        description: {
            type: 'string',
            display: 'Description'
        }
    },
    CancerType: {
        cancerType: {
            type: 'string'
        },
        subtype: {
            type: 'string'
        },
        oncoTreeCode: {
            type: 'string'
        },
        operation: {
            type: 'string' // TODO: May be used for exclude or other operation.
        }
    },
    Tumor: {
        name: {
            type: 'string'
        },
        cancerTypes: {
            type: 'list'
        },
        summary: {
            type: 'string'
        },
        shortSummary: {
            type: 'string',
            display: 'Short description of summary'
        },
        prevalence: {
            type: 'string',
            display: 'Prevalence'
        },
        shortPrevalence: {
            type: 'string',
            display: 'Short prevalence'
        },
        progImp: {
            type: 'string',
            display: 'Prognostic implications'
        },
        shortProgImp: {
            type: 'string',
            display: 'Short prognostic implications'
        },
        trials: {
            type: 'list'
        },
        TI: {
            type: 'list'
        },
        nccn: {
            type: 'NCCN'
        },
        interactAlts: {
            type: 'InteractAlts'
        }
    },
    TI: {
        name: {
            type: 'string'
        },
        types: {},
        treatments: {
            type: 'list'
        },
        description: {
            type: 'string',
            display: 'Description of evidence'
        },
        short: {
            type: 'string',
            display: 'Short description of evidence'
        }
    },
    Treatment: {
        name: {
            type: 'string'
        },
        type: {
            type: 'string'
        },
        level: {
            type: 'string',
            display: 'Highest level of evidence'
        },
        indication: {
            type: 'string',
            display: 'FDA approved indications'
        },
        short: {
            type: 'string',
            display: 'Short description of evidence'
        },
        description: {
            type: 'string',
            display: 'Description of evidence'
        },
        trials: {
            type: 'string'
        }
    },
    // Mutation effect
    ME: {
        value: {
            type: 'string'
        },
        addOn: {
            type: 'string'
        }
    },
    Comment: {
        date: {
            type: 'string'
        },
        userName: {
            type: 'string'
        },
        email: {
            type: 'string'
        },
        content: {
            type: 'string'
        },
        resolved: {
            type: 'string'
        }
    },
    TimeStamp: {
        value: {
            type: 'string'
        },
        // Edit by
        by: {
            type: 'string'
        }
    },
    TimeStampWithCurator: {
        value: {
            type: 'string'
        },
        // Edit by
        by: {
            type: 'Curator'
        }
    },
    EStatus: {
        value: {
            type: 'string'
        },
        by: {
            type: 'string'
        },
        date: {
            type: 'string'
        }
    },
    VUSItem: {
        name: {
            type: 'string'
        },
        time: {
            type: 'list'
        }
    },
    /* eslint camelcase: ["error", {properties: "never"}]*/
    ISOForm: {
        isoform_override: {
            type: 'string'
        },
        gene_name: {
            type: 'string'
        },
        dmp_refseq_id: {
            type: 'string'
        },
        ccds_id: {
            type: 'string'
        }
    }
};

OncoKB.setUp = function(object) {
    if (OncoKB.curateInfo.hasOwnProperty(object.attr)) {
        for (var key1 in OncoKB.curateInfo[object.attr]) {
            if (object[key1] && OncoKB.curateInfo[object.attr][key1].hasOwnProperty('display')) {
                object[key1].display = OncoKB.curateInfo[object.attr][key1].display;
            }

            if (object[key1] && object[key1].type === 'EditableString') {
                Object.defineProperty(object[key1], 'text', {
                    set: object[key1].setText,
                    get: object[key1].getText
                });
            }
        }
    }
};
OncoKB.keyMappings = {type: {TSG: '', OCG: ''}};

OncoKB.initialize = function() {
    var nonSetUp = ['TI'];
    var keys = window._.keys(OncoKB.curateInfo);
    var keysL = keys.length;

    for (var i = 0; i < keysL; i++) {
        var _key = keys[i];
        var _keys = window._.keys(OncoKB.curateInfo[_key]);
        var _keysL = _keys.length;

        // Google Realtime data module for annotation curation
        // Gene is the main entry
        OncoKB[_key] = function() {
        };

        OncoKB[_key].prototype.attr = _key;

        OncoKB[_key].prototype.setUp = function() {
            OncoKB.setUp(this);
        };

        OncoKB[_key].prototype.initialize = function() {
            var model = gapi.drive.realtime.custom.getModel(this);
            var id = this.attr;
            var atrrs = window._.keys(OncoKB.curateInfo[id]);
            var atrrsL = atrrs.length;

            for (var j = 0; j < atrrsL; j++) {
                var __key = atrrs[j];
                if (__key === 'types' && id === 'TI') {
                    this.types = model.createMap({
                        status: '0',
                        type: '0'
                    });
                } else if (OncoKB.curateInfo[id][__key].hasOwnProperty('type')) {
                    if (['Comment', 'TimeStamp', 'EStatus'].indexOf(id) === -1) {
                        this[__key + '_comments'] = model.createList();
                        this[__key + '_timeStamp'] = model.createMap();
                        this[__key + '_eStatus'] = model.createMap();
                    }
                    switch (OncoKB.curateInfo[id][__key].type) {
                    case 'string':
                        this[__key] = model.createString('');
                        break;
                    case 'list':
                        this[__key] = model.createList();
                        break;
                    case 'map':
                        this[__key] = model.createMap(OncoKB.keyMappings[__key]);
                        break;
                    default:
                        this[__key] = model.create(OncoKB.curateInfo[id][__key].type);
                        break;
                    }
                }
            }
            this.setUp();
        };

        // Register every field of OncoKB into document
        for (var j = 0; j < _keysL; j++) {
            OncoKB[_key].prototype[_keys[j]] = gapi.drive.realtime.custom.collaborativeField(_key + '_' + _keys[j]);
            if (['Comment', 'TimeStamp', 'EStatus'].indexOf(_key) === -1) {
                OncoKB[_key].prototype[_keys[j] + '_comments'] = gapi.drive.realtime.custom.collaborativeField(_key + '_' + _keys[j] + '_comments');
                OncoKB[_key].prototype[_keys[j] + '_timeStamp'] = gapi.drive.realtime.custom.collaborativeField(_key + '_' + _keys[j] + '_timeStamp');
                OncoKB[_key].prototype[_keys[j] + '_eStatus'] = gapi.drive.realtime.custom.collaborativeField(_key + '_' + _keys[j] + '_eStatus');
            }
        }

        // Register custom type
        gapi.drive.realtime.custom.registerType(OncoKB[_key], _key);

        // Set realtime API initialize function for each type, this function only runs one time when create new data model
        gapi.drive.realtime.custom.setInitializer(OncoKB[_key], OncoKB[_key].prototype.initialize);

        // Set on loaded function, this function will be loaded everyone the document been pulled from google drive
        if (nonSetUp.indexOf(_key) === -1) {
            gapi.drive.realtime.custom.setOnLoaded(OncoKB[_key], OncoKB[_key].prototype.setUp);
        } else {
            gapi.drive.realtime.custom.setOnLoaded(OncoKB[_key]);
        }
    }
};

var oncokbApp = angular.module('oncokbApp', [
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
    'contenteditable',
    'datatables',
    'datatables.bootstrap',
    'localytics.directives',
    'ui.sortable'
])
    .value('user', {
        name: 'N/A',
        email: 'N/A'
    })
    .value('OncoKB', OncoKB)
    .constant('SecretEmptyKey', '[$empty$]')
    .constant('gapi', window.gapi)
    .constant('loadingScreen', window.loadingScreen)
    .constant('S', window.S)
    .constant('_', window._)
    .constant('Levenshtein', window.Levenshtein)
    .constant('XLSX', window.XLSX)
    .constant('PDF', window.jsPDF)
    .constant('gapi', window.gapi)
    .constant('Tree', window.Tree)
    .config(function($provide, $locationProvider, $routeProvider, $sceProvider, dialogsProvider, $animateProvider, x2jsProvider, config) {
        var access = config.accessLevels;

        // $locationProvider.html5Mode(true);
        $routeProvider
            .when('/', {
                templateUrl: 'views/welcome.html',
                access: access.public,
                internalUse: false
            })
            .when('/tree', {
                templateUrl: 'views/tree.html',
                controller: 'TreeCtrl',
                access: access.admin,
                internalUse: true
            })
            .when('/variant', {
                templateUrl: 'views/variant.html',
                controller: 'VariantCtrl',
                reloadOnSearch: false,
                access: access.admin,
                internalUse: true
            })
            .when('/reportGenerator', {
                templateUrl: 'views/reportgenerator.html',
                controller: 'ReportgeneratorCtrl',
                access: access.admin,
                internalUse: true
            })
            .when('/genes', {
                templateUrl: 'views/genes.html',
                controller: 'GenesCtrl',
                access: access.curator,
                internalUse: false
            })
            .when('/gene/:geneName', {
                templateUrl: 'views/gene.html',
                controller: 'GeneCtrl',
                access: access.curator,
                internalUse: false
            })
            .when('/feedback', {
                templateUrl: 'views/feedback.html',
                // controller: 'FeedbackCtrl',
                access: access.admin,
                internalUse: true
            })
            // .when('/vus', {
            //    templateUrl: 'views/vus.html',
            //    controller: 'VUSCtrl',
            //    access: access.admin,
            //    internalUse: true
            // })
            // .when('/dataSummary', {
            //    templateUrl: 'views/datasummary.html',
            //    controller: 'DatasummaryCtrl',
            //    access: access.admin,
            //    internalUse: true
            //
            // })
            .otherwise({
                redirectTo: '/'
            });

        dialogsProvider.useBackdrop(true);
        dialogsProvider.useEscClose(true);
        dialogsProvider.useCopy(false);
        dialogsProvider.setSize('sm');

        $animateProvider.classNameFilter(/^((?!(fa-spinner)).)*$/);

        x2jsProvider.config = {
            /*
             escapeMode               : true|false - Escaping XML characters. Default is true from v1.1.0+
             attributePrefix          : '<string>' - Prefix for XML attributes in JSon model. Default is '_'
             arrayAccessForm          : 'none'|'property' - The array access form (none|property). Use this property if you want X2JS generates an additional property <element>_asArray to access in array form for any XML element. Default is none from v1.1.0+
             emptyNodeForm            : 'text'|'object' - Handling empty nodes (text|object) mode. When X2JS found empty node like <test></test> it will be transformed to test : '' for 'text' mode, or to Object for 'object' mode. Default is 'text'
             enableToStringFunc       : true|false - Enable/disable an auxiliary function in generated JSON objects to print text nodes with text/cdata. Default is true
             arrayAccessFormPaths     : [] - Array access paths. Use this option to configure paths to XML elements always in 'array form'. You can configure beforehand paths to all your array elements based on XSD or your knowledge. Every path could be a simple string (like 'parent.child1.child2'), a regex (like /.*\.child2/), or a custom function. Default is empty
             skipEmptyTextNodesForObj : true|false - Skip empty text tags for nodes with children. Default is true.
             stripWhitespaces         : true|false - Strip whitespaces (trimming text nodes). Default is true.
             datetimeAccessFormPaths  : [] - Datetime access paths. Use this option to configure paths to XML elements for 'datetime form'. You can configure beforehand paths to all your array elements based on XSD or your knowledge. Every path could be a simple string (like 'parent.child1.child2'), a regex (like /.*\.child2/), or a custom function. Default is empty
             */
            attributePrefix: '$'
        };

        $provide.decorator('accordionDirective', function($delegate) {
            var directive = $delegate[0];
            directive.replace = true;
            return $delegate;
        });

        $provide.decorator('$exceptionHandler', function($delegate, $injector) {
            return function(exception, cause) {
                var $rootScope = $injector.get('$rootScope');
                $rootScope.addError({
                    message: 'Exception',
                    reason: exception,
                    case: cause
                });
                // $rootScope.$emit('oncokbError', {message: 'Exception', reason: exception, case: cause});
                if (config.testing) {
                    $delegate(exception, cause);
                }
            };
        });

        $sceProvider.enabled(false);
    });

angular.module('oncokbApp').run(
    ['$timeout', '$rootScope', '$location', 'loadingScreen', 'storage', 'access', 'config', 'DatabaseConnector', 'users', 'driveOncokbInfo', 'dialogs', 'stringUtils',
        function($timeout, $rootScope, $location, loadingScreen, storage, Access, config, DatabaseConnector, Users, DriveOncokbInfo, dialogs, stringUtils) {
            $rootScope.errors = [];

            // If data is loaded, the watch in nav controller should be triggered.
            $rootScope.dataLoaded = false;

            $rootScope.internal = true;

            $rootScope.user = {
                role: config.userRoles.public
            };

            $rootScope.meta = {
                levelsDesc: {
                    '0': 'FDA-approved drug in this indication irrespective of gene/variant biomarker',
                    '1': 'FDA-recognized biomarker predictive of response to an FDA-approved drug in this indication',
                    '2A': 'Standard of care biomarker predictive of response to an FDA-approved drug in this indication',
                    '2B': 'Standard of care biomarker predictive of response to an FDA-approved drug in another indication but not standard of care for this indication',
                    '3A': 'Compelling clinical evidence supports the biomarker as being predictive of response to a drug in this indication but neither biomarker and drug are standard of care',
                    '3B': 'Compelling clinical evidence supports the biomarker as being predictive of response to a drug in another indication but neither biomarker and drug are standard of care',
                    '4': 'Compelling biological evidence supports the biomarker as being predictive of response to a drug but neither biomarker and drug are standard of care',
                    'R1': 'Standard of care biomarker predictive of resistance to an FDA-approved drug in this indication',
                    'R2': 'Not NCCN compendium-listed biomarker, but clinical evidence linking this biomarker to drug resistance',
                    'R3': 'Not NCCN compendium-listed biomarker, but preclinical evidence potentially linking this biomarker to drug resistance'
                },
                levelsDescHtml: {
                    '0': '<span>FDA-approved drug in this indication irrespective of gene/variant biomarker</span>',
                    '1': '<span><b>FDA-recognized</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
                    '2A': '<span><b>Standard of care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
                    '2B': '<span><b>Standard of care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in another indication</b> but not standard of care for this indication</span>',
                    '3A': '<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in this indication</b> but neither biomarker and drug are standard of care</span>',
                    '3B': '<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in another indication</b> but neither biomarker and drug are standard of care</span>',
                    '4': '<span><b>Compelling biological evidence</b> supports the biomarker as being predictive of response to a drug but neither biomarker and drug are standard of care</span>',
                    'R1': '<span><b>Standard of care</b> biomarker predictive of <b>resistance</b> to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
                    'R2': '<span>Not NCCN compendium-listed biomarker, but clinical evidence linking this biomarker to drug resistance</span>',
                    'R3': '<span>Not NCCN compendium-listed biomarker, but preclinical evidence potentially linking this biomarker to drug resistance</span>'
                }
            };

            $rootScope.addError = function(error) {
                $rootScope.errors.push(error);
            };

            DatabaseConnector.getOncokbInfo(function(oncokbInfo) {
                if (oncokbInfo) {
                    if (oncokbInfo.users) {
                        Users.setUsers(oncokbInfo.users);
                    }

                    if (oncokbInfo.suggestions) {
                        DriveOncokbInfo.setSuggestions(oncokbInfo.suggestions);
                    }

                    if (oncokbInfo.pubMed) {
                        DriveOncokbInfo.setPubMed(oncokbInfo.pubMed);
                    }

                    if (Access.isLoggedIn()) {
                        // console.log('Setting me');
                        Users.setMe(Users.getMe());
                        $rootScope.user = Users.getMe();
                    }
                } else {
                    dialogs.error('Error', 'OncoKB has error. Refresh page might solve the problem.');
                    $rootScope.$emit('oncokbError', {
                        message: 'Couldn\'t connect to server. Time:' + stringUtils.getCurrentTimeForEmailCase(),
                        reason: '',
                        case: stringUtils.getCaseNumber()
                    });
                }
                console.log('Data loaded.');
                $rootScope.dataLoaded = true;
                loadingScreen.finish();
            });

            // Error loading the document, likely due revoked access. Redirect back to home/install page
            $rootScope.$on('$routeChangeError', function() {
                $location.url('/');
            });

            $rootScope.$on('$routeChangeStart', function(event, next) {
                if (!Access.authorize(next.access) || (next.internalUse && !$rootScope.internal)) {
                    if (!Access.isLoggedIn()) {
                        Access.setURL($location.path());
                    }
                    $location.path('/');
                }
                if (Access.isLoggedIn() && Access.getURL()) {
                    $location.path(Access.getURL());
                    Access.setURL('');
                } else if (Access.isLoggedIn() && !Access.getURL() && Access.authorize(config.accessLevels.curator) && next.templateUrl === 'views/welcome.html') {
                    $location.path('/genes');
                }
            });

            // Other unidentify error
            $rootScope.$on('oncokbError', function(event, data) {
                DatabaseConnector.sendEmail({
                    sendTo: 'bugs.pro.exterminator@gmail.com',
                    subject: 'OncoKB Bug.  Case Number:' + stringUtils.getCaseNumber() + ' ' + data.reason,
                    content: 'User: ' + JSON.stringify($rootScope.user) + '\n\nError message - reason:\n' + data.message
                }, function() {
                }, function() {
                });
            });

            $rootScope.$watch('internal', function(n) {
                if (!n && $rootScope.user.role === OncoKB.config.userRoles.admin) {
                    dialogs.notify('Notification', 'Please notice the website can not connect to internal network. All admin features will not be available at this moment.');
                }
            });
        }]);

/**
 * Bootstrap the app
 */
(function(_, gapi, angular, $) {
    /**
     * Get OncoKB configurations
     */
    function fetchData() {
        var initInjector = angular.injector(['ng']);
        var $http = initInjector.get('$http');

        gapi.load('auth:client:drive-share:drive-realtime', function() {
            $http.get('data/config.json').then(function(response) {
                gapi.auth.init();
                if (_.isObject(response.data)) {
                    OncoKB.config = $.extend(true, OncoKB.config, response.data);
                    OncoKB.config.accessLevels = {
                        public: OncoKB.config.userRoles.public | OncoKB.config.userRoles.user | OncoKB.config.userRoles.curator | OncoKB.config.userRoles.admin,
                        user: OncoKB.config.accessLevels.public,
                        curator: OncoKB.config.userRoles.curator | OncoKB.config.userRoles.admin,
                        admin: OncoKB.config.userRoles.admin
                    };
                    OncoKB.initialize();
                    oncokbApp.constant('config', OncoKB.config);
                    bootstrapApplication();
                }
            }, function() {
                console.error('Failed to load JSON configuration file.');
            });
        });
    }

    /**
     * Bootstrap Angular application
     */
    function bootstrapApplication() {
        angular.element(document).ready(function() {
            angular.bootstrap(document, ['oncokbApp']);
        });
    }

    fetchData();
})(window._, window.gapi, window.angular, window.jQuery);
