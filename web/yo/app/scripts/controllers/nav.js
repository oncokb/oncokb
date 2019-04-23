'use strict';

/**
 * @ngdoc function
 * @name oncokbApp.controller:NavCtrl
 * @description
 * # NavCtrl
 * Controller of the oncokbApp
 */
angular.module('oncokbApp')
    .controller('NavCtrl', function($scope, $location, $rootScope, $q, DatabaseConnector, firebaseConnector, $firebaseAuth, $firebaseObject, user, dialogs, mainUtils) {
        var tabs = {
            variant: 'Variant Annotation',
            genes: 'Genes',
            tools: 'Tools',
            feedback: 'Feedback',
            queues: 'Curation Queue',
            therapies: 'Therapies'
        };

        function setParams() {
            var filterTabs = [];
            filterTabs.push('genes');
            filterTabs.push('queues');
            filterTabs.push('therapies');
            if ($rootScope.me && $rootScope.me.admin) {
                filterTabs = _.union(filterTabs, ['variant', 'tools', 'feedback']);
            }

            if (!$rootScope.internal) {
                filterTabs = _.intersection(filterTabs, ['genes', 'queues', 'feedback', 'therapies', 'tools']);
            }

            $scope.tabs = filterTabs.map(function(tabKey) {
                return {key: tabKey, value: tabs[tabKey]}
            });
        }
        $scope.setLocalStorage = function(key) {
            if (key !== 'gene') {
                delete window.localStorage.geneName;
            }
            window.localStorage.tab = key;
        }
        $firebaseAuth().$onAuthStateChanged(function(firebaseUser) {
            if (firebaseUser) {
                user.setRole(firebaseUser).then(function() {
                    $rootScope.isAuthorizedUser = true;
                    $rootScope.signedInUser = $rootScope.me;
                    if(!$rootScope.drugList){
                        // Loading all drugs info
                        $firebaseObject(firebaseConnector.ref("Drugs/")).$bindTo($rootScope, "drugList").then(function () {
                        }, function (error) {
                            dialogs.error('Error', 'Failed to load drugs information. Please Contact developer and stop curation.');
                        });
                    }
                    setParams();
                    if (window.localStorage.geneName) {
                        $location.url('/gene/' + window.localStorage.geneName);
                    } else if (window.localStorage.tab) {
                        $location.url('/' + window.localStorage.tab);
                    } else{
                        $location.url('/genes');
                    }
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

        $scope.$on('internalStateChange', function() {
            setParams();
        });

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
