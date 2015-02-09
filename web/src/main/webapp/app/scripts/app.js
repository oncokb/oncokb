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
    folderId: '0BzBfo69g8fP6fmdkVnlOQWdpLWtHdFM4Ml9vNGxJMWpNLTNUM0lhcEc2MHhKNkVfSlZjMkk'
}

//Google Realtime data module for annotation curation
//Gene is the main entry
OncoKB.Gene = function () {

};

OncoKB.Mutation = function() {

};

OncoKB.Tumor = function() {

};

OncoKB.Curator = function() {

};

OncoKB.TI = function() {

};

OncoKB.NCCN = function() {

};

OncoKB.InteractAlts = function() {

};

OncoKB.Treatment = function() {

};

/**
 * Initializer for constructing via the realtime API
 * @param  {string} gene_name [description]
 * @return {[type]}           [description]
 */
OncoKB.Gene.prototype.initialize = function (name) {
    var model = gapi.drive.realtime.custom.getModel(this);
    this.name = model.createString(name);
    this.summary = model.createString('');
    this.background = model.createString('');
    this.mutations = model.createList();
    this.curators = model.createList();
};

/**
 * [initialize description]
 * @param  {[type]} mutation_name [description]
 * @return {[type]}               [description]
 */
OncoKB.Curator.prototype.initialize = function () {
    var model = gapi.drive.realtime.custom.getModel(this);
    this.name = model.createString('');
    this.email = model.createString('');
}

/**
 * [initialize description]
 * @param  {[type]} mutation_name [description]
 * @return {[type]}               [description]
 */
OncoKB.Mutation.prototype.initialize = function (name) {
    var model = gapi.drive.realtime.custom.getModel(this);
    this.name = model.createString(name);
    this.oncogenic = '';
    this.effect = model.createString('');
    this.description = model.createString('');
    this.tumors = model.createList();
}

OncoKB.Mutation.prototype.setup = function() {
    Object.defineProperty(this.name, 'text', {
        set: this.name.setText,
        get: this.name.getText
    });
};

/**
 * Therapeutic Implications
 * @param  {[type]} mutation_name [description]
 * @return {[type]}               [description]
 */
OncoKB.InteractAlts.prototype.initialize = function () {
    var model = gapi.drive.realtime.custom.getModel(this);
    this.alterations = model.createString('');
    this.description = model.createString('');
}

/**
 * Therapeutic Implications
 * @param  {[type]} mutation_name [description]
 * @return {[type]}               [description]
 */
OncoKB.NCCN.prototype.initialize = function () {
    var model = gapi.drive.realtime.custom.getModel(this);
    this.disease = model.createList();
    this.version = model.createString('');
    this.pages = model.createList();
    this.category = model.createString(''); //Recommendation category
    this.description = model.createString('');
}

/**
 * Therapeutic Implications
 * @param  {[type]} mutation_name [description]
 * @return {[type]}               [description]
 */
OncoKB.Treatment.prototype.initialize = function (name, types) {
    var model = gapi.drive.realtime.custom.getModel(this);
    this.name = model.createString(name);
    this.types = model.createMap({'status': types.status, 'type': types.type});
    this.level = model.createString('');
    this.indication = model.createString('');
    this.trials = model.createList();
    this.description = model.createString(''); //Prognostic implication
}

/**
 * Therapeutic Implications
 * @param  {[type]} mutation_name [description]
 * @return {[type]}               [description]
 */
OncoKB.TI.prototype.initialize = function (name, types) {
    var model = gapi.drive.realtime.custom.getModel(this);
    this.name = model.createString(name);
    this.types = model.createMap({'status': '', 'type': ''});
    this.treatments = model.createList();
    this.description = model.createString(''); //Prognostic implication
}

/**
 * [initialize description]
 * @param  {[type]} mutation_name [description]
 * @return {[type]}               [description]
 */
OncoKB.Tumor.prototype.initialize = function (name) {
    var model = gapi.drive.realtime.custom.getModel(this);
    this.name = model.createString(mutation_name);
    this.prevalence = model.createString('');
    this.progImp = model.createString(''); //Prognostic implication
    this.TI = model.createList(); //Standard therapeutic implications for drug sensitivity
    this.nccn = model.create('NCCN');
    this.trials = model.createList();
    this.interactAlts = model.create('InteractAlts');
}

/**
 * Adds a "text" property to collaborative strings for ng-model compatibility
 * after a model is created or loaded.
 */
// OncoKB.Gene.prototype.setup = function() {
//     Object.defineProperty(this.name, 'text', {
//         set: this.name.setText,
//         get: this.name.getText
//     });
// };

// OncoKB.Mutation.prototype.setup = function() {
//     Object.defineProperty(this.name, 'text', {
//         set: this.name.setText,
//         get: this.name.getText
//     });
// };

OncoKB.loadFile = function ($route, storage) {
    var id = $route.current.params.fileId;
    var userId = $route.current.params.user;
        return storage.requireAuth(true, userId).then(function () {
        return storage.getRealtimeDocument(id);
    });
};

OncoKB.loadFile.$inject = ['$route', 'storage'];

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
 .value('config', OncoKB.config)
 .constant('gapi', window.gapi)
 .config(function ($routeProvider, dialogsProvider, $animateProvider, x2jsProvider) {
    $routeProvider
        .when('/', {
           templateUrl: 'views/tree.html',
           controller: 'TreeCtrl'
        })
        .when('/tree', {
            templateUrl: 'views/tree.html',
            controller: 'TreeCtrl'
        })
        .when('/variant', {
            templateUrl: 'views/variant.html',
            controller: 'VariantCtrl',
            reloadOnSearch: false
        })
        .when('/reportGenerator', {
            templateUrl: 'views/reportgenerator.html',
            controller: 'ReportgeneratorCtrl'
        })
        .when('/curate', {
            templateUrl: 'views/curate.html',
            controller: 'CurateCtrl'
        })
        .when('/curate/:fileId', {
            templateUrl: 'views/curate.html',
            controller: 'CurateEditCtrl',
            resolve: {
              realtimeDocument: OncoKB.loadFile
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
angular.module('oncokb').run(['$rootScope', '$location', 'storage', function ($rootScope, $location, storage) {
    // Error loading the document, likely due revoked access. Redirect back to home/install page
    $rootScope.$on('$routeChangeError', function () {
        $location.url('/');
    });

    // Token expired, refresh
    $rootScope.$on('oncokb.token_refresh_required', function () {
        storage.requireAuth(true).then(function () {
            // no-op
        }, function () {
            $location.url('/curate');
        });
    });
}]);

/**
 * Bootstrap the app
 */
gapi.load('auth:client:drive-share:drive-realtime', function () {
    gapi.auth.init();

    // Register our class
    OncoKB.Gene.prototype.name = gapi.drive.realtime.custom.collaborativeField('gene_name');
    OncoKB.Gene.prototype.summary = gapi.drive.realtime.custom.collaborativeField('gene_summary');
    OncoKB.Gene.prototype.background = gapi.drive.realtime.custom.collaborativeField('gene_background');
    OncoKB.Gene.prototype.mutations = gapi.drive.realtime.custom.collaborativeField('mutations');
    OncoKB.Gene.prototype.curators = gapi.drive.realtime.custom.collaborativeField('curators');

    OncoKB.Mutation.prototype.name = gapi.drive.realtime.custom.collaborativeField('curator_name');
    OncoKB.Mutation.prototype.oncogenic = gapi.drive.realtime.custom.collaborativeField('mutation_oncogenic');
    OncoKB.Mutation.prototype.background = gapi.drive.realtime.custom.collaborativeField('mutation_background');

    OncoKB.Curator.prototype.name = gapi.drive.realtime.custom.collaborativeField('curator_name');
    OncoKB.Curator.prototype.email = gapi.drive.realtime.custom.collaborativeField('curator_email');

    gapi.drive.realtime.custom.registerType(OncoKB.Gene, 'Gene');
    gapi.drive.realtime.custom.registerType(OncoKB.Mutation, 'Mutation');
    gapi.drive.realtime.custom.registerType(OncoKB.Curator, 'Curator');
    gapi.drive.realtime.custom.registerType(OncoKB.InteractAlts, 'InteractAlts');
    gapi.drive.realtime.custom.registerType(OncoKB.NCCN, 'NCCN');
    gapi.drive.realtime.custom.registerType(OncoKB.Treatment, 'Treatment');
    gapi.drive.realtime.custom.registerType(OncoKB.TI, 'TI');
    gapi.drive.realtime.custom.registerType(OncoKB.Tumor, 'Tumor');

    gapi.drive.realtime.custom.setInitializer(OncoKB.Gene, OncoKB.Gene.prototype.initialize);
    gapi.drive.realtime.custom.setInitializer(OncoKB.Mutation, OncoKB.Mutation.prototype.initialize);
    gapi.drive.realtime.custom.setInitializer(OncoKB.Curator, OncoKB.Curator.prototype.initialize);
    gapi.drive.realtime.custom.setInitializer(OncoKB.InteractAlts, OncoKB.InteractAlts.prototype.initialize);
    gapi.drive.realtime.custom.setInitializer(OncoKB.NCCN, OncoKB.NCCN.prototype.initialize);
    gapi.drive.realtime.custom.setInitializer(OncoKB.Treatment, OncoKB.Treatment.prototype.initialize);
    gapi.drive.realtime.custom.setInitializer(OncoKB.TI, OncoKB.TI.prototype.initialize);
    gapi.drive.realtime.custom.setInitializer(OncoKB.Tumor, OncoKB.Tumor.prototype.initialize);

    gapi.drive.realtime.custom.setOnLoaded(OncoKB.Gene, function() {});
    gapi.drive.realtime.custom.setOnLoaded(OncoKB.Mutation, function() {});
    gapi.drive.realtime.custom.setOnLoaded(OncoKB.Curator, function() {});
    gapi.drive.realtime.custom.setOnLoaded(OncoKB.InteractAlts, function() {});
    gapi.drive.realtime.custom.setOnLoaded(OncoKB.NCCN, function() {});
    gapi.drive.realtime.custom.setOnLoaded(OncoKB.Treatment, function() {});
    gapi.drive.realtime.custom.setOnLoaded(OncoKB.TI, function() {});
    gapi.drive.realtime.custom.setOnLoaded(OncoKB.Tumor, function() {});

    angular.element(document).ready(function() {
        angular.bootstrap(document, ['oncokb']);
    });
});
