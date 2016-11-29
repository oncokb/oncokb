'use strict';

/**
 * @ngdoc service
 * @name oncokb.deepMerge
 * @description
 * # deepMerge
 * Service in the oncokb.
 * This original function comes fromhttps://github.com/nrf110/deepmerge
 * Made changed for adjusting duplicates entries
 * ct1: cancer type of target; ct2: cancer type of source
 */

angular.module('oncokbApp')
    .service('DeepMerge', function DeepMerge() {
        function deepMerge(target, src, ct1, ct2) {
            var array = Array.isArray(src);
            var dst = array && [] || {};

            if (array) {
                target = target || [];
                dst = dst.concat(target);
                src.forEach(function(e) {
                    dst.push(e);
                });
            } else {
                if (target && typeof target === 'object') {
                    Object.keys(target).forEach(function(key) {
                        dst[key] = target[key];
                    });
                }
                Object.keys(src).forEach(function(key) {
                    if (typeof src[key] !== 'object' || !src[key]) {
                        if (!Array.isArray(dst[key])) {
                            var _tmp = dst[key];
                            _tmp = angular.isString(_tmp) ? _tmp.trim() : _tmp;
                            dst[key] = [{'value': _tmp, 'Cancer type': ct1}];
                        }
                        src[key] = angular.isString(src[key]) ? src[key].trim() : src[key];
                        dst[key].push({'value': src[key], 'Cancer type': ct2});
                    } else if (target[key]) {
                        dst[key] = deepMerge(target[key], src[key], ct1, ct2);
                    } else {
                        dst[key] = src[key];
                    }
                });
            }
            return dst;
        }

        return {
            init: deepMerge
        };
    });
