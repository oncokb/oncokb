'use strict';

/**
 * @ngdoc service
 * @name oncokb.DatabaseConnector
 * @description
 * # DatabaseConnector
 * Factory in the oncokb.
 */
angular.module('oncokbApp')
  .factory('DatabaseConnector', [
    '$timeout',
    'Gene',
    'Alteration',
    'TumorType',
    'Evidence',
    'SearchVariant',
    'GenerateDoc',
    'DriveOncokbInfo',
    'OncoTreeTumorTypes',
    'DriveAnnotation',
    'SendEmail',
    function (
      $timeout,
      Gene,
      Alteration,
      TumorType,
      Evidence,
      SearchVariant,
      GenerateDoc,
      DriveOncokbInfo,
      OncoTreeTumorTypes,
      DriveAnnotation,
      SendEmail) {

    var numOfLocks = {},
        data = {};

    //When running locally, set this to true, all servlet will read data from relative files.
    var dataFromFile = true;

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

    function getAllOncoTreeTumorTypes(callback, timestamp) {
      if(dataFromFile) {
        OncoTreeTumorTypes.getFromFile().success(function(data) {
          if (timestamp) {
            numOfLocks[timestamp]--;
          }
          callback(data);
        });
      }else {
        OncoTreeTumorTypes.getFromServer().success(function(data) {
          if (timestamp) {
            numOfLocks[timestamp]--;
          }
          callback(data);
        });
      }
    }

    function getOncokbInfo(callback, timestamp) {
      if(dataFromFile) {
        DriveOncokbInfo.getFromFile().success(function(data) {
          if (timestamp) {
            numOfLocks[timestamp]--;
          }
          callback(data);
        });
      }else {
        DriveOncokbInfo.getFromServer().success(function(data) {
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

    function searchVariant(params, success, fail) {
      if(dataFromFile) {
        SearchVariant.annotationFromFile(params).success(function(data) {
          success(data);
        });
      }else {
        SearchVariant
            .getAnnotation(params)
                .success(function(data) {
                    success(data);
                })
                .error(function(){
                    fail();
                });
      }
    }

    function generateGoogleDoc(params, success, fail) {
      console.log(params);
      if(dataFromFile) {
        success('');
      }else {
        GenerateDoc
            .getDoc(params)
                .success(function(data) {
                    success(data);
                })
                .error(function(){
                    fail();
                });
      }
    }
    
    function updateGene(geneString, success, fail) {
      if(dataFromFile) {
        success('');
      }else {
        DriveAnnotation
            .updateGene(geneString)
                .success(function(data) {
                    success(data);
                })
                .error(function(){
                    fail();
                });
      }
    }

    function createGoogleFolder(params, success, fail) {
      if(dataFromFile) {
        success('');
      }else {
        GenerateDoc
            .createFolder(params)
                .success(function(data) {
                    success(data);
                })
                .error(function(){
                    fail();
                });
      }
    }

    function sendEmail(params, success, fail) {
      if(dataFromFile) {
        success(true);
      }else {
        console.log(params);
        SendEmail
            .init(params)
                .success(function(data) {
                    success(data);
                })
                .error(function(){
                    fail();
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

        timeout(callback, timestamp);
      },
      'getAllEvidence': getAllEvidence,
      'searchAnnotation': searchVariant,
      'googleDoc': generateGoogleDoc,
      'createGoogleFolder': createGoogleFolder,
      'getOncokbInfo': getOncokbInfo,
      'getAllTumorType': getAllTumorType,
      'getAllOncoTreeTumorTypes': getAllOncoTreeTumorTypes,
      'updateGene': updateGene,
      'sendEmail': sendEmail
    };
  }]);
