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
    .constant('Sentry', window.Sentry)
    .constant('Levenshtein', window.Levenshtein)
    .constant('PDF', window.jsPDF)
    .constant('UUIDjs', window.UUIDjs)
    .config(function($provide, $locationProvider, $routeProvider, $sceProvider, dialogsProvider, $animateProvider, x2jsProvider, $httpProvider) {

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
                internalUse: false
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
                internalUse: false
            })
            .when('/queues', {
                templateUrl: 'views/queues.html',
                internalUse: false
            })
            .when('/therapies', {
                templateUrl: 'views/drugs.html',
                controller: 'DrugsCtrl',
                internalUse: false
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

        if(OncoKB.config.production) {
            $provide.decorator('$exceptionHandler', function($delegate, $injector) {
                return function(exception, cause) {
                    Sentry.captureException(exception);
                };
            });

            $httpProvider.interceptors.push('errorHttpInterceptor');
        }

        $sceProvider.enabled(false);
    });

angular.module('oncokbApp').run(
    ['$window', '$timeout', '$rootScope', '$location', '$q', 'loadingScreen', 'DatabaseConnector', 'dialogs', 'mainUtils', 'user', 'loadFiles', '$firebaseObject', 'firebaseConnector',
        function($window, $timeout, $rootScope, $location, $q, loadingScreen, DatabaseConnector, dialogs, mainUtils, user, loadFiles, $firebaseObject, firebaseConnector) {
            $rootScope.internal = true;
            $rootScope.meta = {
                levelsDesc: {
                    '0': '',
                    '1': 'FDA-recognized biomarker predictive of response to an FDA-approved drug in this indication',
                    '2A': 'Standard care biomarker predictive of response to an FDA-approved drug in this indication',
                    '2B': 'Standard care biomarker predictive of response to an FDA-approved drug in another indication but not standard care for this indication',
                    '3A': 'Compelling clinical evidence supports the biomarker as being predictive of response to a drug in this indication',
                    '3B': 'Compelling clinical evidence supports the biomarker as being predictive of response to a drug in another indication',
                    '4': 'Compelling biological evidence supports the biomarker as being predictive of response to a drug',
                    'R1': 'Standard care biomarker predictive of resistance to an FDA-approved drug in this indication',
                    'R2': 'Compelling clinical evidence supports the biomarker as being predictive of resistance to a drug',
                    'R3': '',
                    'Px1': 'FDA and/or professional guideline-recognized biomarker prognostic in this indication based on well-powered studie(s)',
                    'Px2': 'FDA and/or professional guideline-recognized biomarker prognostic in this indication based on a single or multiple small studies',
                    'Px3': 'Biomarker prognostic in this indication based on clinical evidence in well powered studies',
                    'Dx1': 'FDA and/or professional guideline-recognized biomarker required for diagnosis in this indication',
                    'Dx2': 'FDA and/or professional guideline-recognized biomarker that supports diagnosis in this indication',
                    'Dx3': 'Biomarker that may assist disease diagnosis in this indication based on clinical evidence',
                },
                levelsDescHtml: {
                    '0': '<span></span>',
                    '1': '<span><b>FDA-recognized</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
                    '2A': '<span><b>Standard care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
                    '2B': '<span><b>Standard care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in another indication</b> but not standard care for this indication</span>',
                    '3A': '<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in this indication</b></span>',
                    '3B': '<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in another indication</b></span>',
                    '4': '<span><b>Compelling biological evidence</b> supports the biomarker as being predictive of response to a drug</span>',
                    'R1': '<span><b>Standard care</b> biomarker predictive of <b>resistance</b> to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
                    'R2': '<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of <b>resistance</b> to a drug</span>',
                    'R3': '<span></span>'
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
                    Level_R3: '#FCD6D3',
                    Level_Px1: '#33A02C',
                    Level_Px2: '#1F78B4',
                    Level_Px3: '#984EA3',
                    Level_Dx1: '#33A02C',
                    Level_Dx2: '#1F78B4',
                    Level_Dx3: '#984EA3',
                }
            };

            // Load setting collection from firebase when the app is initialized.
            loadFiles.load('setting').then(function(result) {}, function(error) {});

            // Error loading the document, likely due revoked access. Redirect back to home/install page
            $rootScope.$on('$routeChangeError', function() {
                $location.url('/');
            });
            var loading = true;

            function testInternal() {
                var defer = $q.defer();
                DatabaseConnector.testAccess(function() {
                    $rootScope.internal = true;
                    defer.resolve();
                }, function(data, status, headers, config) {
                    $rootScope.internal = false;
                    defer.resolve();
                });
                return defer.promise;
            }

            testInternal().finally(function() {
                $rootScope.$broadcast('internalStateChange');
            });

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
                if (!$rootScope.isAuthorizedUser || (next.internalUse && !$rootScope.internal)) {
                    if (loading) {
                        loadingScreen.finish();
                        loading = false;
                    }
                    $location.path('/');
                }
            });
        }]);

/**
 * Bootstrap the app
 */
(function(_, angular, $) {
    /**
     * Get OncoKB configurations
     */
    function fetchData(callback) {
        var initInjector = angular.injector(['ng']);
        var $http = initInjector.get('$http');

        if (window.CurationPlatformConfigString) {
            callback(_.isString(window.CurationPlatformConfigString) ? JSON.parse(window.CurationPlatformConfigString) : window.CurationPlatformConfigString);
        } else {
            $http.get('data/config.json').then(function(response) {
                if (_.isObject(response.data)) {
                    callback(response.data);
                }
            }, function() {
                console.error('Failed to load JSON configuration file.');
            });
        }
    }

    /**
     * Bootstrap Angular application
     */
    function bootstrapApplication() {
        angular.element(document).ready(function() {
            angular.bootstrap(document, ['oncokbApp']);
        });
    }

    fetchData(function(serverSideConfigs) {
        OncoKB.config = $.extend(true, OncoKB.config, serverSideConfigs);
        firebase.initializeApp(OncoKB.config.firebaseConfig);
        bootstrapApplication();
    });
})(window._, window.angular, window.jQuery);
