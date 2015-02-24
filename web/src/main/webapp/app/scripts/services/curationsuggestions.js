'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.curationSuggestions
 * @description
 * # curationSuggestions
 * Service in the oncokbApp.
 */
angular.module('oncokb')
  .service('curationSuggestions', function curationSuggestions() {
    var self = this;
    self.suggestions = {};
    self.suggestionsA = [];
    self.suggestionsL = 0;

    function Suggestion(){
        this.gene = '';
        this.mutations = [];
    }

    function get(params) {
      if(angular.isObject(params)) {
        if(params.hasOwnProperty('gene')) {
          return self.suggestions[params.gene];
        }else {
          return false;
        }
      }
      return self.suggestionsA;
    }

    return {
      set: function(suggestions){
        if(angular.isArray(suggestions)) {
          suggestions.forEach(function(e){
            if(e.gene) {
              var _sug = new Suggestion();
              _sug.gene = e.gene.toString().trim();
              _sug.mutations = e.mutations.split(';').map(function(e){ return e.toString().trim()});
              self.suggestionsA.push(_sug);
              self.suggestions[e.gene] = _sug;
            }
          });
          self.suggestionsL = self.suggestionsA.length;
        }
      },
      get: get,
      getMutation: function(gene){
        if(angular.isString(gene)) {
          var _gene = get({'gene': gene});
          if(_gene){
            return _gene.mutations;
          }
          return undefined;
        }
        return false;
      }
    }
  });
