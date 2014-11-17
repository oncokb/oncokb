'use strict';

/**
 * @ngdoc service
 * @name webappApp.DatabaseConnector
 * @description
 * # DatabaseConnector
 * Factory in the webappApp.
 */
angular.module('webappApp')
  .factory('DatabaseConnector', [
    '$timeout',
    'Gene',
    'Alteration',
    'TumorType',
    'Evidence',
    'SearchVariant',
    'GenerateDoc',
    function (
      $timeout,
      Gene,
      Alteration,
      TumorType,
      Evidence,
      SearchVariant,
      GenerateDoc) {

    var numOfLocks = {},
        data = {};

    //When running locally, set this to true, all servlet will read data from relative files.
    var dataFromFile = false;

    function getAllGene(callback, timestamp) {
      if(dataFromFile) {
        Gene.getFromFile().success(function(data) {
          if (timestamp) {
            numOfLocks[timestamp]--;
          }
          callback(data);
        });
      }else {
        Gene.getFromServer().success(function(data) {
          if (timestamp) {
            numOfLocks[timestamp]--;
          }
          callback(data);
        });
      }
    }

    function getAllAlteration(callback, timestamp) {
      if(dataFromFile) {
        Alteration.getFromFile().success(function(data) {
          if (timestamp) {
            numOfLocks[timestamp]--;
          }
          callback(data);
        });
      }else {
        Alteration.getFromServer().success(function(data) {
          if (timestamp) {
            numOfLocks[timestamp]--;
          }
          callback(data);
        });
      }
    }

    function getAllTumorType(callback, timestamp) {
      if(dataFromFile) {
        TumorType.getFromFile().success(function(data) {
          if (timestamp) {
            numOfLocks[timestamp]--;
          }
          callback(data);
        });
      }else {
        TumorType.getFromServer().success(function(data) {
          if (timestamp) {
            numOfLocks[timestamp]--;
          }
          callback(data);
        });
      }
    }

    function getAllEvidence(callback, timestamp) {
      if(dataFromFile) {
        Evidence.getFromFile().success(function(data) {
          if (timestamp) {
            numOfLocks[timestamp]--;
          }
          callback(data);
        });
      }else {
        Evidence.getFromServer().success(function(data) {
          if (timestamp) {
            numOfLocks[timestamp]--;
          }
          callback(data);
        });
      }
    }

    function searchVariant(callback, params) {
      if(dataFromFile) {
        SearchVariant.annotationFromFile(params).success(function(data) {
          callback(data);
        });
      }else {
        SearchVariant.getAnnotation(params).success(function(data) {
          callback(data);
        });
      }
    }

    function generateGoogleDoc(callback, params) {
      if(dataFromFile) {
        callback('');
      }else {
        GenerateDoc.getDoc(params).success(function(data) {
          callback(data);
        });
      }
    }

    function timeout(callback, timestamp) {
      $timeout(function(){
        if(numOfLocks[timestamp] === 0) {
          callback(data[timestamp]);
        }else{
          timeout(callback, timestamp);
        }
      }, 100);
    }
    // Public API here
    return {
      'getGeneAlterationTumortype': function(callback) {
        var timestamp = new Date().getTime().toString();
        
        numOfLocks[timestamp] = 3;
        data[timestamp] = {};

        getAllGene(function(d){
          data[timestamp].genes = d;
        }, timestamp);
        getAllAlteration(function(d){
          data[timestamp].alterations = d;
        }, timestamp);
        getAllTumorType(function(d){
          data[timestamp].tumorTypes = d;
        }, timestamp);

        timeout(callback, timestamp)
      },
      'getAllEvidence': getAllEvidence,
      'searchAnnotation': searchVariant,
      'googleDoc': generateGoogleDoc
    };
  }]);
