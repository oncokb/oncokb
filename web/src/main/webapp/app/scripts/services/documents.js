'use strict';

/**
 * @ngdoc service
 * @name oncokb.documents
 * @description
 * # documents
 * Service in the oncokb
 */
angular.module('oncokb')
  .service('documents', function documents() {
    // AngularJS will instantiate a singleton by calling "new" on this function
    var self = this;
    self.documents = [];
    self.documentsL = 0;

    function searchById(id) {
        for (var i = 0; i < self.documentsL; i++) {
            if(self.documents[i].id === id) {
                return self.documents[i];
            }
        }
        return false;
    }

    //Ideally should only return one file, but may have duplicates
    function searchByTitle(title) {
        var seletecd = [];
        for (var i = 0; i < self.documentsL; i++) {
            if(self.documents[i].title === title) {
                seletecd.push(self.documents[i]);
            }
        }
        return seletecd;
    }

    return {
        set: function(documents){
            if(angular.isArray(documents)) {
                self.documents = documents;
                self.documentsL = documents.length;
                console.log(self);
            }
        },
        get: function(params){

            //Only ID and Title accepted
            if(angular.isObject(params)) {
                if(params.hasOwnProperty('id')) {
                    return searchById(params.id);
                }else if(params.hasOwnProperty('title')){
                    return searchByTitle(params.title);
                }else {
                    return false;
                }
            }
            return self.documents;
        }
    }
  });
