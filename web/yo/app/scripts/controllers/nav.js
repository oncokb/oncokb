'use strict';

/**
 * @ngdoc function
 * @name oncokbApp.controller:NavCtrl
 * @description
 * # NavCtrl
 * Controller of the oncokbApp
 */
angular.module('oncokbApp')
    .controller('NavCtrl', function($scope, $location, $rootScope, config, gapi, user, storage, access, DatabaseConnector) {
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
            $scope.user = $rootScope.user;
            if (access.authorize(accessLevels.curator)) {
                filterTabs.push({key: 'genes', value: tabs.genes});
            }
            if (access.authorize(accessLevels.admin) && $rootScope.internal) {
                var keys = ['tree', 'variant', 'reportGenerator', 'queues', 'feedback'];

                keys.forEach(function(e) {
                    filterTabs.push({key: e, value: tabs[e]});
                });
            }
            $scope.signedIn = access.isLoggedIn();
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

        // Render the sign in button.
        $scope.renderSignInButton = function(immediateMode) {
            if (immediateMode !== false) {
                immediateMode = true;
            }
            storage.requireAuth(immediateMode).then(function(result) {
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
        $scope.signedIn = false;
        $scope.user = $rootScope.user;

        $rootScope.$watch('user', setParams);

        $rootScope.$watch('dataLoaded', function(n) {
            if (n) {
                $scope.renderSignInButton();
            }
        });
    });
