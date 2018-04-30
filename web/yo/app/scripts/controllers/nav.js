'use strict';

/**
 * @ngdoc function
 * @name oncokbApp.controller:NavCtrl
 * @description
 * # NavCtrl
 * Controller of the oncokbApp
 */
angular.module('oncokbApp')
    .controller('NavCtrl', function($scope, $location, $rootScope, config, gapi, user, storage, access, DatabaseConnector, $firebaseAuth, $firebaseObject, userFire) {
        var tabs = {
            // 'vus': 'VUS',
            tree: 'Tree',
            variant: 'Variant Annotation',
            genes: 'Genes',
            // 'dataSummary': 'Summary',
            reportGenerator: 'Tools',
            feedback: 'Feedback',
            queues: 'Curation Queue'
        };

        var accessLevels = config.accessLevels;
        function loginCallback() {
            // console.log('In login callback.');

            testInternal(function() {
                if ($scope.$$phase) {
                    setParams();
                } else {
                    $scope.$apply(setParams);
                }
                // $rootScope.$apply(function() {
                var url = access.getURL();
                // console.log('Current URL:', url);
                if (url) {
                    // console.log('is logged in? ', access.isLoggedIn());
                    if (access.isLoggedIn()) {
                        access.setURL('');
                        $location.path(url);
                    }
                } else if (access.isLoggedIn() && access.authorize(config.accessLevels.curator)) {
                    // console.log('logged in and has authorize.');
                    $location.path('/genes');
                } else {
                    // console.log('is logged in? ', access.isLoggedIn());
                    // console.log('does not have access? ', access.authorize(config.accessLevels.curator));
                    $location.path('/');
                }
                // });
            });
        }

        function setParams() {
            var filterTabs = [];
            filterTabs.push({key: 'genes', value: tabs.genes});
            filterTabs.push({key: 'queues', value: tabs.queues});
            if ($rootScope.me.role === 8) {
                var keys = ['tree', 'variant', 'reportGenerator', 'feedback'];
                keys.forEach(function(e) {
                    filterTabs.push({key: e, value: tabs[e]});
                });
            }
            $scope.tabs = filterTabs;
        }

        function testInternal(callback) {
            DatabaseConnector.testAccess(function() {
                $rootScope.internal = true;
                if (angular.isFunction(callback)) {
                    callback();
                }
            }, function(data, status, headers, config) {
                console.log(data, status, headers, config);
                $rootScope.internal = false;
                if (angular.isFunction(callback)) {
                    callback();
                }
            });
        }
        $firebaseAuth().$onAuthStateChanged(function(firebaseUser) {
            if (firebaseUser) {
                console.log("Signed in as:", firebaseUser.uid);
                console.log(firebaseUser);
                $rootScope.isSignedIn = true;
                userFire.setRole(firebaseUser).then(function() {
                    $scope.user = $rootScope.me;
                    setParams();
                }, function(error) {
                });
            } else {
                console.log('not logged in yet');
            }                
        });
        // Render the sign in button.
        $scope.renderSignInButton = function() {
            // if (!$rootScope.isSignedIn) {
                
            // }             
            console.log('ready to login');
            userFire.login().then(function() {
                console.log('finished to login');
                $scope.user = $rootScope.me;
                setParams();
            }, function(error) {
                console.log('failed to login', error);
            });
        };
        
        $scope.signOut = function() {
            userFire.logout().then(function() {
                $location.path('/');
                $scope.tabs = [];
            });
        };

        $scope.tabIsActive = function(route) {
            if (route instanceof Array) {
                for (var i = route.length - 1; i >= 0; i--) {
                    if (route[i] === $location.path()) {
                        return true;
                    }
                }
                return false;
            }
            return route === $location.path();
        };

        // When callback is received, we need to process authentication.
        $scope.signInCallback = function(authResult) {
            // Do a check if authentication has been successful.
            // console.log('In processAuth');

            if (authResult.access_token) {
                // Successful sign in.
                // $scope.signedIn = true;
                //  console.log('access success', authResult);
                access.login(loginCallback);
            } else if (authResult.error) {
                // Error while signing in.
                // $scope.signedIn = false;
                console.log('access failed', authResult);
                loginCallback();
                // Report error.
            } else {
                console.log('access failed and does not have error.', authResult);
                loginCallback();
            }
        };

        // This flag we use to show or hide the button in our HTML.
        // $scope.signedIn = false;

        $rootScope.$watch('isSignedIn', function(n, o) {
            if (n !== o) {
                $scope.isSignedIn = $rootScope.isSignedIn;
            }
        });

        $rootScope.$watch('dataLoaded', function(n) {
            if (n) {
                // $scope.renderSignInButton();
            }
        });
    });
