'use strict';
angular.module('oncokbApp').filter('getIndexByObjectNameInArray', function() {
    return function(array, name, attr) {
        for (var i = 0, arrayL = array.length; i < arrayL; i++) {
            var _datum;
            if (typeof attr !== 'undefined' && attr) {
                _datum = array[i][attr];
            } else {
                _datum = array[i];
            }
            if (_datum.toUpperCase() === name.toUpperCase()) {
                return i;
            }
        }
        return null;
    };
})
    .filter('sortObject', function() {
        var delayAttrs = ['description'];
        var prioAttrs = ['trial', 'cancer_type', 'version'];

        return function(obj, addKey) {
            if (!(obj instanceof Object)) {
                return obj;
            }

            if (addKey === false) {
                return Object.values(obj);
            }
            var keys = Object.keys(obj).filter(function(item) {
                return item !== '$$hashKey';
            }).sort(function(a, b) {
                if (delayAttrs.indexOf(a) !== -1) {
                    return 1;
                } else if (delayAttrs.indexOf(b) !== -1) {
                    return -1;
                } else if (prioAttrs.indexOf(a) !== -1) {
                    return -1;
                } else if (prioAttrs.indexOf(b) !== -1) {
                    return 1;
                } else if (a < b) {
                    return -1;
                }
                return 1;
            });
            return keys.map(function(key) {
                if (typeof obj[key] !== 'object') {
                    var _obj = {};
                    Object.defineProperty(_obj, '$value', {
                        enumerable: false,
                        value: obj[key]
                    });
                    obj[key] = _obj;
                }
                return Object.defineProperty(obj[key], '$key', {
                    enumerable: false,
                    value: key
                });
            });
        };
    })
    .filter('sortPubs', function() {
        return function(array) {
            if (angular.isArray(array)) {
                array.sort(function(a, b) {
                    var strA = '';
                    var strB = '';
                    var numA = -1;
                    var numB = -1;

                    strA = a.type.trim();
                    strB = b.type.trim();
                    numA = a.id.trim();
                    numB = b.id.trim();

                    if (strA !== strB) {
                        return strA < strB;
                    }
                    return numA < numB;
                });
            }
            return array;
        };
    })
    .filter('typeaheadFilter', function(Levenshtein) {
        return function(input, query) {
            var result = [];

            query = query.toString().toLowerCase();
            angular.forEach(input, function(object) {
                if (object.toLowerCase().indexOf(query.toLowerCase()) !== -1) {
                    result.push(object);
                }
            });
            result.sort(function(a, b) {
                a = a.toString().toLowerCase();
                b = b.toString().toLowerCase();
                if (a.indexOf(query) === 0) {
                    return -1;
                } else if (b.indexOf(query) === 0) {
                    return 1;
                }
                var matchA = new Levenshtein(a, query);
                var matchB = new Levenshtein(b, query);
                return matchA.distance < matchB.distance;
            });
            return result;
        };
    })
    .filter('cut', function() {
        return function(value, wordwise, max, tail) {
            var _tail = tail || '...';
            if (!value) {
                return '';
            }

            max = parseInt(max, 10);
            if (!max) {
                return value;
            }
            if (value.length <= max) {
                return value;
            }

            value = value.substr(0, max - _tail.length);
            if (wordwise) {
                var lastspace = value.lastIndexOf(' ');
                if (lastspace !== -1) {
                    value = value.substr(0, lastspace);
                }
            }

            return value + _tail;
        };
    });
