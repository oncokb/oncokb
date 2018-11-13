'use strict';

/**
 * @ngdoc function
 * @name oncokbApp.controller:NavCtrl
 * @description
 * # NavCtrl
 * Controller of the oncokbApp
 */
angular.module('oncokbApp')
    .controller('NavCtrl', function($scope, $location, $rootScope, $q, DatabaseConnector, $firebaseAuth, $firebaseObject, user, dialogs) {
        var tabs = {
            variant: 'Variant Annotation',
            genes: 'Genes',
            tools: 'Tools',
            feedback: 'Feedback',
            queues: 'Curation Queue',
            drugs: 'Drugs'
        };

        function setParams() {
            var filterTabs = [];
            filterTabs.push({key: 'genes', value: tabs.genes});
            filterTabs.push({key: 'queues', value: tabs.queues});
            filterTabs.push({key: 'drugs', value: tabs.drugs});
            if ($rootScope.me.admin) {
                var keys = ['variant', 'tools', 'feedback'];
                keys.forEach(function(e) {
                    filterTabs.push({key: e, value: tabs[e]});
                });
            }
            $scope.tabs = filterTabs;
        }
        $scope.setLocalStorage = function(key) {
            if (key !== 'gene') {
                delete window.localStorage.geneName;
            }
            window.localStorage.tab = key;
        }

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
        $firebaseAuth().$onAuthStateChanged(function(firebaseUser) {
            if (firebaseUser) {
                user.setRole(firebaseUser).then(function() {
                    $rootScope.isAuthorizedUser = true;
                    $rootScope.signedInUser = $rootScope.me;
                    setParams();
                    testInternal().then(function() {
                        if (window.localStorage.geneName) {
                            $location.url('/gene/' + window.localStorage.geneName);
                        } else if (window.localStorage.tab){
                            $location.url('/' + window.localStorage.tab);
                        } else {
                            $location.url('/genes');
                        }
                    });
                }, function(error) {
                    mainUtils.sendEmail('dev.oncokb@gmail.com', 'Failed to set user role.',
                        'Content: \n' + JSON.stringify(firebaseUser) + '\n\nError: \n' + JSON.stringify(error));
                });
            } else {
                console.log('not logged in yet');
            }
        });
        $scope.signIn = function() {
            user.login().then(function() {
                setParams();
                $location.url('/genes');
            }, function(error) {
                console.log('finish is called');
                loadingScreen.finish();
                if (!$rootScope.isAuthorizedUser) {
                    dialogs.notify('Warning', 'You do not have access to the system. Please contact the OncoKB team.');
                }
            });
        };

        $scope.signOut = function() {
            user.logout().then(function() {
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

        // This flag we use to show or hide the button in our HTML.
        // $scope.signedIn = false;

        $rootScope.$watch('isAuthorizedUser', function(n, o) {
            if (n !== o) {
                $scope.isAuthorizedUser = $rootScope.isAuthorizedUser;
            }
        });
    });
