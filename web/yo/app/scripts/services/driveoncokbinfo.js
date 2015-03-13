'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.driveOncokbInfo
 * @description
 * # driveOncokbInfo
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
  .service('driveOncokbInfo', function driveOncokbInfo() {

    var self = this;
    self.genes = {};
    self.genesA = [];
    self.genesL = 0;

    function Gene(){
      this.gene = '';
      this.pubMedLinks = [];
      this.mutations = [];
    }

    function get(params, key) {
      if(angular.isUndefined(key)) {
        return self.genesA;
      }else{
        if(angular.isObject(params)) {
          if(params.hasOwnProperty('gene')) {
            var _gene = params.gene.toString().trim();
            if(self.genes.hasOwnProperty(_gene)) {
              var __obj = {
                'gene': _gene
              };
              __obj[key] = self.genes[_gene][key];
              return __obj;
            }else{
              return {};
            }
          }else {
            return false;
          }
        }

        var desiredA = [];
        for(var gene in self.genes) {
          var _obj = {
            'gene': gene.gene
          };
          _obj[key] = gene[key];
          desiredA.push(_obj);
        }
        return desiredA;
      }
    }

    function set(data, dataKey, geneKey) {
      if(!angular.isUndefined(dataKey) &&!angular.isUndefined(geneKey) && angular.isArray(data)){
        data.forEach(function(e){
          if(e.gene) {
            var _gene = e.gene.toString().trim();
            if(!self.genes.hasOwnProperty(_gene)){
              self.genes[_gene] = new Gene();
            }

            if(angular.isString(e[dataKey])) {
              self.genes[_gene][geneKey] = e[dataKey].split(';').map(function(e1){ return e1.toString().trim();});
            }else {
              self.genes[_gene][geneKey] = [];
            }
          }
        });
        updateGenesArray();
      }
    }

    function updateGenesArray() {
      self.genesA.length = 0;

      for(var key in self.genes) {
        self.genesA.push(self.genes[key]);
      }
      self.genesL = self.genesA.length;
    }
    return {
      setSuggestions: function(suggestions){
        set(suggestions, 'mutations', 'mutations');
      },
      setPubMed: function(pubMed){
        set(pubMed, 'links', 'pubMedLinks');
      },
      get: get,
      getSuggestions: function(params){
        return get(params, 'mutations');
      },
      getPubMed: function(params){
        var pubMed = get(params, 'pubMedLinks');
        if(pubMed){
          return pubMed.pubMedLinks;
        }
        return undefined;
      },
      getMutation: function(gene){
        if(angular.isString(gene)) {
          var _gene = get({'gene': gene}, 'mutations');
          if(_gene){
            return _gene.mutations;
          }
          return undefined;
        }
        return false;
      }
    };
  });
