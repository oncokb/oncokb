'use strict';

/**
 * @ngdoc function
 * @name oncokbApp.controller:NavCtrl
 * @description
 * # NavCtrl
 * Controller of the oncokbApp
 */
angular.module('oncokbApp')
.controller('NavCtrl', function ($scope, $location, $rootScope, config, gapi, user, storage, access) {
    var tabs = {
        'tree': 'Tree',
        'variant': 'Variant Annotation',
        'genes': 'Genes',
        'reportGenerator': 'Tools'
    };

    var accessLevels = config.accessLevels;

    function loginCallback() {
        // console.log('In login callback.')
        if(!$scope.$$phase) {
            $scope.$apply(setParams);
        }else {
            setParams();
        }
        $rootScope.$apply(function() {
            var url = access.getURL();
            if(url) {
                if(access.isLoggedIn()){
                    access.setURL('');
                    $location.path(url);
                }
            }else {
                if (access.isLoggedIn() && !access.getURL() && access.authorize(config.accessLevels.curator)) {
                    $location.path('/genes');
                }else {
                    $location.path('/');
                }
            }
        });
    }

    function setParams() {
        var filterTabs = [];
        $scope.user = $rootScope.user;
        if(access.authorize(accessLevels.curator)) {
            filterTabs.push({key: 'genes', value: tabs.genes});
        }
        if(access.authorize(accessLevels.admin)) {
            var keys = ['tree', 'variant', 'reportGenerator'];

            keys.forEach(function(e){
                filterTabs.push({'key': e, 'value': tabs[e]});
            });
        }
        $scope.signedIn = access.isLoggedIn();
        $scope.tabs = filterTabs;
    }

    // Render the sign in button.
    $scope.renderSignInButton = function() {
        storage.requireAuth().then(function(result){
            $scope.signInCallback(result);
        });
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

    // When callback is received, we need to process authentication.
    $scope.signInCallback = function(authResult) {
        // Do a check if authentication has been successful.
        // console.log('In processAuth');

        if(authResult.access_token) {
            // Successful sign in.
            // $scope.signedIn = true;
            // console.log('access success', authResult);
            access.login(loginCallback);
        } else if(authResult.error) {
            // Error while signing in.
            // $scope.signedIn = false;
            // console.log('access failed', authResult);
            loginCallback();
            // Report error.
        }
    };

    // This flag we use to show or hide the button in our HTML.
    $scope.signedIn = false;
    $scope.user = $rootScope.user;

    $rootScope.$watch('user', setParams);
    // Call start function on load.
    // $scope.init = $scope.renderSignInButton();
});
