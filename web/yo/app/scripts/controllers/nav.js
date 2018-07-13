'use strict';

/**
 * @ngdoc function
 * @name oncokbApp.controller:NavCtrl
 * @description
 * # NavCtrl
 * Controller of the oncokbApp
 */
angular.module('oncokbApp')
    .controller('NavCtrl', function($scope, $location, $rootScope, $q, DatabaseConnector, $firebaseAuth, $firebaseObject, user) {
        var tabs = {
            variant: 'Variant Annotation',
            genes: 'Genes',
            tools: 'Tools',
            feedback: 'Feedback',
            queues: 'Curation Queue'
        };

        function setParams() {
            var filterTabs = [];
            filterTabs.push({key: 'genes', value: tabs.genes});
            filterTabs.push({key: 'queues', value: tabs.queues});
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
                $rootScope.isSignedIn = true;
                user.setRole(firebaseUser).then(function() {
                    $scope.user = $rootScope.me;
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
                });
            } else {
                console.log('not logged in yet');
            }                
        });
        $scope.signIn = function() {
            user.login().then(function() {
                $scope.user = $rootScope.me;
                setParams();
                $location.url('/genes');
            }, function(error) {
                console.log('failed to login', error);
                console.log('finish is called');
                loadingScreen.finish();
            });
        };
        
        $scope.signOut = function() {
            user.logout().then(function() {
                $location.path('/');
                $scope.tabs = [];
            });
        };

        // This flag we use to show or hide the button in our HTML.
        // $scope.signedIn = false;

        $rootScope.$watch('isSignedIn', function(n, o) {
            if (n !== o) {
                $scope.isSignedIn = $rootScope.isSignedIn;
            }
        });
    });
