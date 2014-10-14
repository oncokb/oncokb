'use strict';

/**
 * @ngdoc overview
 * @name webappApp
 * @description
 * # webappApp
 *
 * Main module of the application.
 */
var oncokbApp = angular
 .module('webappApp', [
   'ngAnimate',
   'ngCookies',
   'ngResource',
   'ngRoute',
   'ngSanitize',
   'ngTouch',
   'ui.bootstrap',
   'localytics.directives'
 ])
 .config(function ($routeProvider) {
   $routeProvider
     .when('/', {
       templateUrl: 'views/tree.html',
       controller: 'TreeCtrl'
     })
     .when('/variant', {
       templateUrl: 'views/variant.html',
       controller: 'VariantCtrl'
     })
     // .when('/', {
     //   templateUrl: 'home.html'
     // })
     .otherwise({
       redirectTo: '/'
     });
 });
