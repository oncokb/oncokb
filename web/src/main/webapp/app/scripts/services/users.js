'use strict';

/**
 * @ngdoc service
 * @name oncokb.user
 * @description
 * # user
 * Service in the oncokb.
 */
angular.module('oncokb')
  .service('users', function user() {
    var self = this;

    self.me = {};
    self.users = {};
    self.usersL = 0;
    self.usersU = []; //Without keys

    function User(){
        this.email = '';
        this.name = '';
        this.role = 1;
        this.genes = [];
    };

    /**
     * [getUsers description]
     * @param  {string} key     only email/role allowed
     * @param  {string / array} search
     * @return {array}          matched users
     */
    function getUsers(key, search) {
        if(angular.isString(key) && ['email', 'role'].indexOf(key) !== -1) {
            var matches = [];
            if(angular.isArray(search)) {
                for(var i = 0; i < self.usersL; i++) {
                    var _val = self.usersU[i][key];
                    if(search.indexOf(_val) !== -1) {
                        matches.push(self.usersU[i]);
                    }
                }
            }else {
                if(key === 'email') {
                    if(self.users.hasOwnProperty(search)) {
                        matches.push(self.users[key]);
                    }
                }else {
                    for(var j = 0; j < self.usersL; j++) {
                        var __val = self.usersU[j][key];
                        if(search === __val) {
                            matches.push(self.usersU[j]);
                        }
                    }
                }
            }
            return matches;
        }else if(angular.isUndefined(key)) {
            return self.usersU;
        }else {
            return false;
        }
    }

    function setUsers(users) {
        var usersL = 0;

        if(angular.isArray(users)) {
            usersL = users.length;
            for (var i = 0; i < usersL; i++) {
                if(users[i].hasOwnProperty('email')) {
                    var _user = new User();
                    for(var __key in users[i]) {
                        if(_user.hasOwnProperty(__key) && users[i][__key]) {
                            if(__key !== 'genes') {
                                _user[__key] = users[i][__key];
                            }else {
                                _user[__key] = users[i][__key].split(',').map(function(d){ return d.toString().trim();});
                            }
                        }
                    }
                    self.users[_user.email] = angular.copy(_user);
                    self.usersU.push(_user);
                    _user = null;
                }
            }
            self.usersL = self.usersU.length;
            return true;
        }else {
            return false;
        }
    }

    function setMe(email){
        if(angular.isString(email) && self.users.hasOwnProperty(email)) {
            self.me = self.users[email];
        }
    }

    return {
        getMe: function(){ return self.me;},
        setMe: setMe,
        setUsers: setUsers,
        getUsers: getUsers
    };
  });
