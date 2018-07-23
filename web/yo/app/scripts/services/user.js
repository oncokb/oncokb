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
    .service('user', function user($routeParams, $q, $firebaseAuth, $firebaseObject, $rootScope) {
        var me = {
            admin: false,
            email: '',
            name: '',
            photoURL: ''
        };
        var editableData = {};
        var allUsers = {};
        function login() {
            var defer = $q.defer();
            $firebaseAuth().$signInWithPopup("google").then(function(gResp) {
                me.name = gResp.user.displayName;
                me.email = gResp.user.email;
                me.photoURL = gResp.user.photoURL;
                $rootScope.isSignedIn = true;
                $rootScope.signedInUser = me;
                setRole(gResp.user).then(function() {
                    if (allUsers[me.name.toLowerCase()].email && allUsers[me.name.toLowerCase()].email !== me.email) {
                        defer.reject('You do not have access to login. Please contact the OncoKB team.');
                    } else {
                        if (!allUsers[me.name.toLowerCase()].email) {
                            updateUserInfo().then(function() {
                                defer.resolve();
                            }, function(error) {
                                defer.reject('fail to initialize user info ' + error);
                            });
                        } else {
                            defer.resolve();
                        }                        
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
            }
            var defer = $q.defer();
            getAllUsers().then(function(allUsers) {
                if (!_.isUndefined(allUsers[me.name.toLowerCase()].admin)) {
                    me.admin = allUsers[me.name.toLowerCase()].admin;
                } else if (!_.isUndefined(allUsers[me.name.toLowerCase()].genes)) {
                    me.genes = allUsers[me.name.toLowerCase()].genes;
                }
                $rootScope.me = me;
                defer.resolve();
            });
            return defer.promise;
        }
        function updateUserInfo() {
            var defer = $q.defer();
            var myName = $rootScope.me.name.toLowerCase();
            var updatedUserInfo = {
                name: $rootScope.me.name,
                email: $rootScope.me.email,
                photoURL: $rootScope.me.photoURL
            };
            firebase.database().ref('Users/'+myName).update(updatedUserInfo).then(function() {
                defer.resolve();
            }, function(error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function logout() {
            var defer = $q.defer();
            $firebaseAuth().$signOut().then(function() {
                $rootScope.isSignedIn = false;
                defer.resolve();
            }, function(error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function setFileeditable(hugoSymbols) {
            var defer = $q.defer();
            getAllUsers().then(function(users) {
                var name = me.name.toLowerCase();
                _.each(hugoSymbols, function(hugoSymbol) {
                    if (users[name].admin === true || users[name].genes.write === 'all' || users[name].genes.write.indexOf(hugoSymbol) !== -1) {
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
                    console.log('Failed to load users information', error);
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
