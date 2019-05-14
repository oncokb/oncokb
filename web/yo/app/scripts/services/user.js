'use strict';

/**
 * @ngdoc service
 * @name oncokb.user
 * @description
 * # user
 * Service in the oncokb.
 *
 * Google email account has two addresses: gmail.com and googlemail.com
 * gmail.com will be used as standard email format, googlemail address will be converted to gmail
 */
angular.module('oncokbApp')
    .service('user', function user($routeParams, $q, $firebaseAuth, $firebaseObject, $rootScope, mainUtils, firebaseConnector, dialogs) {
        // me is used only inside user.js to share user information
        var me = {
            admin: false,
            email: '',
            name: '',
            photoURL: '',
            key: '' // this is the key used in the Firebase Realtime Users table
        };
        var editableData = {};
        var allUsers = {};
        $rootScope.isAuthorizedUser = false;
        function login() {
            var defer = $q.defer();
            $firebaseAuth().$signInWithPopup("google").then(function(gResp) {
                me.name = gResp.user.displayName;
                me.email = gResp.user.email;
                me.photoURL = gResp.user.photoURL;
                me.key = gResp.user.email.replace(/\./g, '');
                // $rootScope.signedInUser is used to store user info who passed google authentication, but they might not be authorized to the curation platform.
                $rootScope.signedInUser = me;
                setRole(gResp.user).then(function() {
                    if (!allUsers[me.key]) {
                        defer.reject('You do not have access to login. Please contact the OncoKB team.');
                    } else {
                        if (!allUsers[me.key].email) {
                            updateUserInfo().then(function() {
                                // $rootScope.isAuthorizedUser is used to recognize authorized user of the curation platform.
                                $rootScope.isAuthorizedUser = true;
                                defer.resolve();
                            }, function(error) {
                                defer.reject('fail to initialize user info ' + error);
                            });
                        } else {
                            $rootScope.isAuthorizedUser = true;
                            defer.resolve();
                        }
                        // Loading all drugs info
                        $firebaseObject(firebaseConnector.ref("Drugs/")).$bindTo($rootScope, "drugList").then(function () {
                        }, function (error) {
                            dialogs.error('Error', 'Failed to load drugs information. Please Contact developer and stop curation.');
                        });
                    }
                }, function(error) {
                    defer.reject(error);
                });
            }).catch(function(error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function setRole(user) {
            if (!me.email) {
                me.email = user.email;
                me.name = user.displayName;
                me.photoURL = user.photoURL;
                me.key = user.email.replace(/\./g, '');
            }
            var defer = $q.defer();
            getAllUsers().then(function(allUsers) {
                if (!_.isUndefined(allUsers[me.key].admin)) {
                    me.admin = allUsers[me.key].admin;
                } else if (!_.isUndefined(allUsers[me.key].genes)) {
                    me.genes = allUsers[me.key].genes;
                }
                // $rootScope.me is used to store the user who passed both authtication and authorization process. It is used accross the whole project to access current user info.
                $rootScope.me = me;
                defer.resolve();
            } , function(error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function updateUserInfo() {
            var defer = $q.defer();
            var updatedUserInfo = {
                name: me.name,
                email: me.email,
                photoURL: me.photoURL
            };
            firebase.database().ref('Users/'+me.key).update(updatedUserInfo).then(function() {
                defer.resolve();
            }, function(error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function logout() {
            var defer = $q.defer();
            mainUtils.clearCollaboratorsByName($rootScope.me.name.toLowerCase());
            $firebaseAuth().$signOut().then(function() {
                $rootScope.isAuthorizedUser = false;
                $rootScope.signedInUser = {};
                defer.resolve();
            }, function(error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function setFileeditable(hugoSymbols) {
            var defer = $q.defer();
            getAllUsers().then(function(users) {
                _.each(hugoSymbols, function(hugoSymbol) {
                    if (users[me.key].admin === true || users[me.key].genes.write === 'all' || users[me.key].genes.write.indexOf(hugoSymbol) !== -1) {
                        editableData[hugoSymbol] = true;
                    } else {
                        editableData[hugoSymbol] = false;
                    }
                });
                defer.resolve(editableData);
            }, function(error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function isFileEditable(hugoSymbol) {
            if (editableData[hugoSymbol] === true) {
                return true;
            } else {
                return false;
            }
        }
        function getAllUsers() {
            var defer = $q.defer();
            if (_.isEmpty(allUsers)) {
                firebase.database().ref('Users').on('value', function(users) {
                    allUsers = users.val();
                    defer.resolve(allUsers);
                }, function(error) {
                    defer.reject(error);
                });
            } else {
                defer.resolve(allUsers);
            }
            return defer.promise;
        }
        return {
            login: login,
            logout: logout,
            isFileEditable: isFileEditable,
            setFileeditable: setFileeditable,
            setRole: setRole,
            getAllUsers: getAllUsers
        };
    });
