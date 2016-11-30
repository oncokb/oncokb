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
    .service('users', function user(config) {
        var self = {};

        self.me = {};
        self.users = {};
        self.usersL = 0;
        self.usersU = []; // Without keys

        function User() {
            this.email = '';
            this.mskccEmail = '';
            this.name = '';
            this.role = 1;
            this.genes = [];
            this.phases = [];
        }

        /**
         * Get user by keywords
         * @param  {string} key Only email/role allowed
         * @param  {string|array} search Search criteria
         * @return {array} matched users
         */
        function getUsers(key, search) {
            if (angular.isString(key) && ['email', 'role'].indexOf(key) !== -1) {
                var matches = [];
                if (angular.isArray(search)) {
                    for (var i = 0; i < self.usersL; i++) {
                        var _val = self.usersU[i][key];
                        if (search.indexOf(_val) !== -1) {
                            matches.push(self.usersU[i]);
                        }
                    }
                } else if (key === 'email') {
                    if (self.users.hasOwnProperty(search)) {
                        matches.push(self.users[key]);
                    }
                } else {
                    for (var j = 0; j < self.usersL; j++) {
                        var __val = self.usersU[j][key];
                        if (search === __val) {
                            matches.push(self.usersU[j]);
                        }
                    }
                }
                return matches;
            } else if (angular.isUndefined(key)) {
                return self.usersU;
            }
            return false;
        }

        function setUsers(users) {
            var usersL = 0;

            if (angular.isArray(users)) {
                usersL = users.length;
                for (var i = 0; i < usersL; i++) {
                    if (users[i].hasOwnProperty('email')) {
                        var _user = new User();
                        for (var __key in users[i]) {
                            if (_user.hasOwnProperty(__key) && users[i][__key]) {
                                if (['genes', 'phases'].indexOf(__key) === -1) {
                                    _user[__key] = users[i][__key];
                                } else {
                                    _user[__key] = users[i][__key].split(',').map(function(d) {
                                        return d.toString().trim();
                                    });
                                }
                                if (__key === 'email') {
                                    _user.email = convertEmail(_user.email);
                                }
                            }
                        }

                        // if(_user['genes'].length !== _user['phases'].length) {
                        //     console.log(_user);
                        // }
                        self.users[_user.email] = angular.copy(_user);
                        self.usersU.push(_user);
                        _user = null;
                    }
                }
                self.usersL = self.usersU.length;
                return true;
            }
            return false;
        }

        function setMe(user) {
            if (angular.isString(user.email)) {
                var email = convertEmail(user.email);
                if (self.users.hasOwnProperty(email)) {
                    self.me = self.users[email];
                } else {
                    self.me = user;
                    self.me.role = config.userRoles.user;
                }
            }
        }

        function convertEmail(email) {
            email = email.toString().toLowerCase();
            if (/@googlemail/.test(email)) {
                email = email.replace(/@googlemail/, '@gmail');
            }
            return email;
        }

        return {
            resetMe: function() {
                self.me = {};
            },
            getMe: function() {
                return self.me;
            },
            setMe: setMe,
            setUsers: setUsers,
            getUsers: getUsers,
            resetUsers: function() {
                self.users = {};
                self.usersL = 0;
                self.usersU = [];
            }
        };
    });
