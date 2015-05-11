'use strict';

/**
 * @ngdoc service
 * @name oncokb.FindRegex
 * @description
 * # FindRegex
 * Factory in the oncokb.
 */
angular.module('oncokbApp')
  .factory('FindRegex', function (_, S) {
    var allRegex = {
        pmid: {
            regex: /PMID:?\s*([0-9]+,?\s*)+/ig,
            link: 'http://www.ncbi.nlm.nih.gov/pubmed/'
        },
        nct: {
            regex: /NCT:?[0-9]+/ig,
            link: 'http://clinicaltrials.gov/show/'
        }
    };
    function find(str, type) {
        if(typeof str === 'string' && str !== '') {
            var regex = [allRegex.pmid.regex, allRegex.nct.regex],
                links = [allRegex.pmid.link, allRegex.nct.link];
            for (var j = 0, regexL = regex.length; j < regexL; j++) {
                var result = str.match(regex[j]);

                if(result) {
                    /*jshint -W083 */
                    var uniqueResult = result.filter(function(elem, pos) {
                        return result.indexOf(elem) === pos;
                    });
                    /*jshint +W083 */
                    for(var i = 0, resultL = uniqueResult.length; i < resultL; i++) {
                        var _datum = uniqueResult[i];

                        switch(j) {
                            case 0:
                                var _number = '';
                                if(_datum.indexOf(':') !== -1){
                                    _number = _datum.split(':')[1].trim();
                                }else{
                                    if(isNaN(_datum)){
                                        var tmpResult = _datum.match(/([0-9]+)/m);
                                        if(tmpResult && tmpResult[1] && !isNaN(tmpResult[1]))
                                            _number = tmpResult[1];
                                    }else{
                                        _number = _number.trim();
                                    }
                                }
                                _number = _number.replace(/\s+/g, '');
                                str = str.replace(new RegExp(_datum, 'g'), createTag(type, links[j] + _number, _datum));
                                break;
                            default:
                                if(_datum.indexOf(':') !== -1){
                                    _datum = S(_datum).strip(':').s;
                                }
                                str = str.replace(_datum, createTag(type, links[j] + _datum, _datum));
                                break;
                        }

                    }
                }
            }
        }
        return str;
    }

    function result(str) {
        var uniqueResultA = [];
        if(typeof str === 'string' && str !== '') {
            var regex = [allRegex.pmid.regex, allRegex.nct.regex];
            for (var j = 0, regexL = regex.length; j < regexL; j++) {
                var resultMatch = str.match(regex[j]);

                if(resultMatch) {
                    /*jshint -W083 */
                    var _uniqueResult = resultMatch.filter(function(elem, pos) {
                        return resultMatch.indexOf(elem) === pos;
                    });
                    for(var i = 0, resultL = _uniqueResult.length; i < resultL; i++) {
                        var _datum = _uniqueResult[i];
                        var _number = 0;
                        switch(j) {
                            //pubmed PMID
                            case 0:
                                if(_datum.indexOf(':') !== -1){
                                    _number = _datum.split(':')[1].trim();
                                }else{
                                    _number = _datum;
                                }
                                _number = _number.split(',');
                                _number.forEach(function(e){
                                    if(e) {
                                        if(isNaN(e)){
                                            var tmpResult = e.match(/([0-9]+)/m);
                                            if(tmpResult && tmpResult[1] && !isNaN(tmpResult[1]))
                                            e = tmpResult[1];
                                        }else{
                                            e = e.trim();
                                        }

                                        uniqueResultA.push({type: 'pmid', id: e});
                                    }
                                });
                                break;
                            //clinical trial NCT
                            case 1:
                                if(_datum.indexOf(':') !== -1){
                                    _datum = S(_datum).strip(':').s;
                                }
                                uniqueResultA.push({type: 'nct', id: _datum});
                                break;
                            default:
                                break;
                        }

                    }
                    /*jshint +W083 */
                }
            }
        }

        return _.uniq(uniqueResultA, 'id');
    }

    function createTag(type, link, content){
        var str = '';
        switch (type) {
            case 'link':
                str = '<a class="withUnderScore" target="_blank" href="'+ link +'">' + content + '</a>';
                break;
            case 'iframe':
                str = '<div contenteditable="false" tooltip-html-unsafe="<iframe src=\'' + link + '\'></iframe>">' + content + '</div>';
                break;
            default:
                break;
        }
        return str;
    }
    // Public API here
    return {
      get: function(str){ return find(str, 'link');},
      iframe: function(str){ return find(str, 'iframe');},
      result: function(str){ return result(str);}
    };
  });
