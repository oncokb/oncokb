'use strict';

/**
 * @ngdoc function
 * @name oncokbApp.controller:NavCtrl
 * @description
 * # NavCtrl
 * Controller of the oncokbApp
 */
angular.module('oncokb')
.controller('NavCtrl', function ($scope, $location, $rootScope, config, gapi, user, storage, access) {
    var tabs = {
        'tree': 'Tree',
        'variant': 'Variant Annotation',
        'genes': 'Genes'
    }

    var accessLevels = config.accessLevels;

    function loginCallback() {
        // console.log('In login callback.')
        if(!$scope.$$phase) {
            $scope.$apply(setParams);
        }else {
            setParams();
        }
    }

    function setParams() {
        var url = access.getURL();
        $scope.user = $rootScope.user;
        $scope.tabs = [];
        // if(access.authorize(accessLevels.admin)) {
        //     $scope.tabs.push({key: 'tree', value: tabs.tree});
        //     $scope.tabs.push({key: 'variant', value: tabs.variant});
        // }
        if(access.authorize(accessLevels.curator)) {
            $scope.tabs.push({key: 'genes', value: tabs.genes});
        }
        $scope.signedIn = access.isLoggedIn();

        if(url) {
            access.setURL('');
            $location.path(url);
        }
    }

    // When callback is received, we need to process authentication.
    $scope.signInCallback = function(authResult) {
        // console.log('Signed in.');
        $scope.processAuth(authResult);
    };

    // Render the sign in button.
    $scope.renderSignInButton = function() {
        gapi.signin.render('signInButton',
            {
                'callback': $scope.signInCallback, // Function handling the callback.
                'clientid': config.clientId, // CLIENT_ID from developer console which has been explained earlier.
                // 'requestvisibleactions': 'http://schemas.google.com/AddActivity', // Visible actions, scope and cookie policy wont be described now,
                                                                                  // as their explanation is available in Google+ API Documentation.
                'scope': config.scopes.join(' '),
                'cookiepolicy': 'single_host_origin'
            }
        );
    };

    $scope.signOut = function() {
        access.logout();
        $scope.signedIn = false;
        $scope.user = $rootScope.user;
        $location.path('/');
        gapi.auth.signOut();
    };

    $scope.tabIsActive = function(route) {
        if( route instanceof Array) {
            for (var i = route.length - 1; i >= 0; i--) {
                if(route[i] === $location.path()) {
                    return true;
                }
            }
            return false;
        }else {
            return route === $location.path();
        }
    };

    // Here we do the authentication processing and error handling.
    // Note that authResult is a JSON object.
    $scope.processAuth = function(authResult) {
        // Do a check if authentication has been successful.
        // console.log('In processAuth');
        if(authResult['access_token']) {
            // console.log('has token');
            // Successful sign in.
            // $scope.signedIn = true;

            access.login(loginCallback);
        } else if(authResult['error']) {
            // console.log('hasnt token');
            // Error while signing in.
            // $scope.signedIn = false;
            loginCallback();
            // Report error.
        }
    };

    // This flag we use to show or hide the button in our HTML.
    $scope.signedIn = false;
    $scope.user = $rootScope.user;

    $rootScope.$watch('user', setParams);
    // Call start function on load.
    $scope.init = $scope.renderSignInButton();
});
