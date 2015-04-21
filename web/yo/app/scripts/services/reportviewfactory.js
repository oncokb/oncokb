'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.reportViewFactory
 * @description
 * # reportViewFactory
 * Factory in the oncokbApp.
 */
angular.module('oncokbApp')
    .factory('reportViewFactory', function (FindRegex) {
      function getData(params){
        var _parmas = angular.copy(params);
        _parmas.overallInterpretation = processOverallInterpretation(_parmas.overallInterpretation);
        _parmas = constructData(_parmas);
        return _parmas;
      }

      function processOverallInterpretation(str) {
        var content = str.split(/[\n\r]/g);
        for(var i=0; i< content.length; i++) {
          if(i%2 === 0) {
            content[i]='<b>' + content[i] + '</b>';
          }
        }
        str = content.join('<br/>');
        return str;
      }

      function constructData(data) {
        for(var key in data) {
          var datum = data[key];
          if(angular.isArray(datum)) {
            datum = constructData(datum);
          }else if(isObject(datum) && !bottomObject(datum)) {
            datum = constructData(datum);
          }else if(isObject(datum) && bottomObject(datum)) {
            datum = objToArray(datum);
          }else {
            datum = FindRegex.get(datum);
          }
          data[key] = datum;
        }


        return data;
      }

      function isObject(obj) {
        return angular.isObject(obj) && !angular.isArray(obj);
      }

      function bottomObject(obj) {
        var flag = true;
        if(obj && typeof obj === 'object') {
          for(var key in obj) {
            if(typeof obj[key] !== 'string' && typeof obj[key] !== 'number') {
              flag = false;
              break;
            }
          }
        }else {
          flag = false;
        }
        return flag;
      }

      function objToArray(obj) {
        var delayAttrs = ['description'];
        var priorAttrs = ['trial','nccn_special','recommendation category 1 / 2A / 2 / 2A / 2A'];

        if (!angular.isObject(obj)) {
          return obj;
        }

        var keys = Object.keys(obj).filter(function(item) {
          return item !== '$$hashKey';
        }).sort(function(a,b) {
          var delayIndexOfA = delayAttrs.indexOf(a),
              delayIndexOfB = delayAttrs.indexOf(b),
              priorIndexOfA = priorAttrs.indexOf(a),
              priorIndexOfB = priorAttrs.indexOf(b);

          if(priorIndexOfA !== -1 && priorIndexOfB !== -1) {
            if(priorIndexOfA <= priorIndexOfB) {
              return -1;
            }else {
              return 1;
            }
          }else if(priorIndexOfA !== -1) {
            return -1;
          }else if(priorIndexOfB !== -1) {
            return 1;
          }else {
            if(delayIndexOfA !== -1 && delayIndexOfB !== -1) {
              if(delayIndexOfA <= delayIndexOfB) {
                return 1;
              }else {
                return -1;
              }
            }else if(delayIndexOfA !== -1) {
              return 1;
            }else if(delayIndexOfB !== -1) {
              return -1;
            }else {
              if(a < b) {
                return -1;
              }else {
                return 1;
              }
            }
          }
        });

        var returnArray = keys.map(function (key) {
          var _obj = {};

          _obj.key = key;
          _obj.value = FindRegex.get(obj[key]).toString();
          return _obj;
        });

        return returnArray;
      }

      // Public API here
      return {
        getData: getData
      };
    });
