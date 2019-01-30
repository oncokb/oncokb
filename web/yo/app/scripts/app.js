'use strict';
/**
 * @ngdoc overview
 * @name oncokb
 * @description
 * # oncokb
 *
 * Main module of the application.
 */
var OncoKB = {
    global: {},
    config: {},
    backingUp: false
};
function getString(string) {
    if(!string || !_.isString(string)) {
        return '';
    }
    var tmp = window.document.createElement('DIV');
    var processdStr = string.replace(/(\r\n|\n|\r)/gm, '');
    var processdStr = processdStr.replace(/<style>.*<\/style>/i, '');
    tmp.innerHTML = processdStr;
    /* eslint new-cap: 0*/
    var _string = tmp.textContent || tmp.innerText || S(string).stripTags().s;
    string = S(_string).collapseWhitespace().s;
    string = string.replace(/<!--.*-->/g, '');
    return string;
}
OncoKB.utils = {
    getString: getString
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
    'xml',
    'contenteditable',
    'datatables',
    'datatables.bootstrap',
    'ui.sortable',
    'firebase',
    'daterangepicker',
    'ngTagsInput'
])
    .value('OncoKB', OncoKB)
    // This is used for typeahead
    .constant('SecretEmptyKey', '[$empty$]')
    .constant('loadingScreen', window.loadingScreen)
    .constant('S', window.S)
    .constant('_', window._)
    .constant('Levenshtein', window.Levenshtein)
    .constant('PDF', window.jsPDF)
    .constant('UUIDjs', window.UUIDjs)
    .config(function($provide, $locationProvider, $routeProvider, $sceProvider, dialogsProvider, $animateProvider, x2jsProvider) {

        $routeProvider
            .when('/', {
                templateUrl: 'views/welcome.html',
                internalUse: false
            })
            .when('/variant', {
                templateUrl: 'views/variant.html',
                controller: 'VariantCtrl',
                reloadOnSearch: false,
                internalUse: true
            })
            .when('/tools', {
                templateUrl: 'views/tools.html',
                controller: 'ToolsCtrl',
                internalUse: true
            })
            .when('/genes', {
                templateUrl: 'views/genes.html',
                controller: 'GenesCtrl',
                internalUse: false
            })
            .when('/gene/:geneName', {
                templateUrl: 'views/gene.html',
                controller: 'GeneCtrl',
                internalUse: false
            })
            .when('/feedback', {
                templateUrl: 'views/feedback.html',
                internalUse: true
            })
            .when('/queues', {
                templateUrl: 'views/queues.html'
            })
            .when('/drugs', {
                templateUrl: 'views/drugs.html',
                controller: 'DrugsCtrl'
            })
            .otherwise({
                redirectTo: '/genes'
            });

        dialogsProvider.useBackdrop(true);
        dialogsProvider.useEscClose(true);
        dialogsProvider.useCopy(false);
        dialogsProvider.setSize('sm');

        $animateProvider.classNameFilter(/^((?!(fa-spinner)).)*$/);

        x2jsProvider.config = {
            attributePrefix: '$'
        };

        $provide.decorator('$exceptionHandler', function($delegate, $injector) {
            return function(exception, cause) {
                var $rootScope = $injector.get('$rootScope');
                $rootScope.addError({
                    message: 'Exception',
                    reason: exception,
                    case: cause
                });
                $rootScope.$emit('oncokbError', {message: 'Exception', reason: exception, case: cause});
                if (!OncoKB.config.production && exception) {
                    $delegate(exception, cause);
                }
            };
        });

        $sceProvider.enabled(false);
    });

angular.module('oncokbApp').run(
    ['$window', '$timeout', '$rootScope', '$location', 'loadingScreen', 'DatabaseConnector', 'dialogs', 'mainUtils', 'user', 'loadFiles',
        function($window, $timeout, $rootScope, $location, loadingScreen, DatabaseConnector, dialogs, mainUtils, user, loadFiles) {
            $rootScope.errors = [];
            $rootScope.internal = true;
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
                },
                colorsByLevel: {
                    Level_1: '#33A02C',
                    Level_2A: '#1F78B4',
                    Level_2B: '#80B1D3',
                    Level_3A: '#984EA3',
                    Level_3B: '#BE98CE',
                    Level_4: '#424242',
                    Level_R1: '#EE3424',
                    Level_R2: '#F79A92',
                    Level_R3: '#FCD6D3'
                }
            };

            // Load setting collection from firebase when the app is initialized.
            loadFiles.load('setting').then(function(result) {}, function(error) {});

            $rootScope.addError = function(error) {
                $rootScope.errors.push(error);
            };

            // Error loading the document, likely due revoked access. Redirect back to home/install page
            $rootScope.$on('$routeChangeError', function() {
                $location.url('/');
            });
            var loading = true;
            $rootScope.$on('$routeChangeStart', function(event, next) {
                var fromIndex = window.location.href.indexOf('/gene/');
                var hugoSymbol = '';
                var regex = /\/([^\/]+)\/?$/;
                if (fromIndex !== -1) {
                    //When the curator left the gene page
                    hugoSymbol = window.location.href.match(regex)[1];
                    window.localStorage.geneName = hugoSymbol;
                }
                var toIndex = $location.path().indexOf('/gene/');
                if (toIndex !== -1) {
                    //When the curator enter the gene page
                    hugoSymbol = $location.path().match(regex)[1];
                    window.localStorage.geneName = hugoSymbol;
                }
                if (toIndex === -1) {
                    var filteredUrl = $location.path().match(regex);
                    if (filteredUrl && filteredUrl.length > 1) {
                        window.localStorage.tab = filteredUrl[1];
                        if (fromIndex === -1) {
                            window.localStorage.geneName = '';
                        }
                    }
                }
                if ($rootScope.me && (fromIndex !== -1 || toIndex !== -1)) {
                    loadFiles.load(['collaborators']).then(function() {
                        var myName = $rootScope.me.name.toLowerCase();
                        if (!$rootScope.collaboratorsMeta) {
                            $rootScope.collaboratorsMeta = {};
                        }
                        if (fromIndex !== -1) {
                            var genesOpened = $rootScope.collaboratorsMeta[myName];
                            $rootScope.collaboratorsMeta[myName] = _.without(genesOpened, hugoSymbol);
                        }
                        if (toIndex !== -1) {
                            if (!$rootScope.collaboratorsMeta[myName]) {
                                $rootScope.collaboratorsMeta[myName] = [];
                            }
                            if ($rootScope.collaboratorsMeta[myName].indexOf(hugoSymbol) === -1) {
                                $rootScope.collaboratorsMeta[myName].push(hugoSymbol);
                            }
                        }
                    }, function(error) {
                        console.log(error);
                    });
                }
                if (!$rootScope.isAuthorizedUser) {
                    if (loading) {
                        loadingScreen.finish();
                        loading = false;
                    }
                    $location.path('/');
                }
            });
            // Other unidentify error
            $rootScope.$on('oncokbError', function(event, data) {
                var subject = 'OncoKB Bug.  Case Number:' + mainUtils.getCaseNumber() + ' ' + data.reason;
                var content = 'User: ' + JSON.stringify($rootScope.me) + '\n\nError message - reason:\n' + data.message;
                mainUtils.notifyDeveloper(subject, content);
            });
        }]);

/**
 * Bootstrap the app
 */
(function(_, angular, $) {
    /**
     * Get OncoKB configurations
     */
    function fetchData() {
        var initInjector = angular.injector(['ng']);
        var $http = initInjector.get('$http');

        $http.get('data/config.json').then(function(response) {
            if (_.isObject(response.data)) {
                OncoKB.config = $.extend(true, OncoKB.config, response.data);
                firebase.initializeApp(OncoKB.config.firebaseConfig);
                bootstrapApplication();
            }
        }, function() {
            console.error('Failed to load JSON configuration file.');
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
})(window._, window.angular, window.jQuery);
