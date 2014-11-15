'use strict';

/**
 * @ngdoc overview
 * @name webappApp
 * @description
 * # webappApp
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


var oncokbApp = angular
 .module('webappApp', [
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
   'angularFileUpload'
 ])
 .config(function ($routeProvider, dialogsProvider) {
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
       controller: 'VariantCtrl'
     })
     .when('/reportGenerator', {
       templateUrl: 'views/reportgenerator.html',
       controller: 'ReportgeneratorCtrl'
     })
     .otherwise({
       redirectTo: '/'
     });

  dialogsProvider.useBackdrop(true);
  dialogsProvider.useEscClose(true);
  dialogsProvider.useCopy(false);
  dialogsProvider.setSize('md');
 });