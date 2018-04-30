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
    .service('userFire', function user(config, $routeParams, $q, $firebaseAuth, $firebaseObject, $rootScope) {
        var me = {
            email: '',
            name: '',
            role: 1,
            genes: []
        };
        var editableData = {};
        function login() {
            var defer = $q.defer();
            $firebaseAuth().$signInWithPopup("google").then(function(gResp) {
                me.name = gResp.user.displayName;
                me.email = gResp.user.email;
                me.photoURL = gResp.user.photoURL;
                $rootScope.isSignedIn = true;
                setRole(gResp.user).then(function() {
                    defer.resolve();
                    $rootScope.me = me;
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
            var currentUser = $firebaseObject(firebase.database().ref('Users/' + user.uid));
            currentUser.$loaded().then(function(data) {
                if (!data.email) {
                    saveNewUser(user).then(function() {
                        defer.resolve();
                        $rootScope.me = me;
                    }, function(error) {
                        defer.reject(error);
                    });
                } else {
                    if (data.admin === true) {
                        me.role = 8;
                    } else {
                        me.role = 4;
                    }
                    defer.resolve();
                    $rootScope.me = me;
                }
            }).catch(function(error) {
                defer.reject(error);
            });
            return defer.promise;
        }
        function saveNewUser(user) {
            var defer = $q.defer();
            DriveOncokbInfo.getFirebasePermission().then(function(response) {
                var permission = response.data;
                var currentUser = $firebaseObject(firebase.database().ref('Users/' + user.uid));
                currentUser.email = user.email;
                currentUser.displayName = user.displayName;
                if (permission.admins.indexOf(user.email) !== -1) {
                    currentUser.admin = true;
                    me.role = 8;
                } else if (permission.commonUsers[user.email]) {
                    currentUser.genes = permission.commonUsers[user.email];
                    me.role = 4;
                }                                        
                currentUser.$save().then(function(res) {
                    defer.resolve();
                }, function(error) {
                    defer.reject(error);
                });    
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
            if (_.isEmpty(editableData) || hugoSymbols.length === 1) {
                firebase.database().ref('Users').on('value', function(users) {
                    var usersInfo = users.val();
                    for(var i = 0; i < _.keys(usersInfo).length; i++) {
                        var currentUser = usersInfo[_.keys(usersInfo)[i]];
                        if (currentUser.email === me.email) {
                            _.each(hugoSymbols, function(hugoSymbol) {
                                if (currentUser.admin || currentUser.genes.indexOf(hugoSymbol) !== -1) {
                                    editableData[hugoSymbol] = true;
                                } else {
                                    editableData[hugoSymbol] = false;
                                }
                            });
                            defer.resolve(editableData);
                        }
                    }
                }, function(error) {
                    defer.reject(error);
                });
            } else {
                defer.resolve(editableData);
            }
            return defer.promise;
        }
        function isFileEditable(hugoSymbol) {
            if (editableData[hugoSymbol] === true) {
                return true;
            } else {
                return false;
            }
        }

        return {
            login: login,
            logout: logout,
            isFileEditable: isFileEditable,
            setFileeditable: setFileeditable,
            setRole: setRole
        };
    });
