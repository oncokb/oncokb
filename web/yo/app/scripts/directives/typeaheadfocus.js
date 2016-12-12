'use strict';

/**
 * @ngdoc directive
 * @name oncokbApp.directive:typeaheadFocus
 * @description
 * # typeaheadFocus
 * Original comes from http://plnkr.co/edit/ZtuoTVgPLuMWDT2ejULW?p=preview
 *
 * focusMe and emptyTypeahead originally come from http://plnkr.co/edit/Qrnat8yTvISuM1qHHDlA?p=preview
 */
angular.module('oncokbApp')
// Did not use this directive, it needs angular 1.3.* support
    .directive('typeaheadFocus', function() {
        return {
            require: 'ngModel',
            link: function(scope, element, attr, ngModel) {
                function triggerPopup() {
                    var viewValue = ngModel.$viewValue;
                    // var modelValue = ngModel.$modelValue;

                    console.log(ngModel);

                    // restore to null value so that the typeahead can detect a change
                    if (ngModel.$viewValue === ' ') {
                        ngModel.$setViewValue('');
                    }

                    // force trigger the popup
                    ngModel.$setViewValue(' ');

                    // set the actual value in case there was already a value in the input
                    ngModel.$setViewValue(viewValue || ' ');
                }

                function emptyOrMatch(actual, expected) {
                    if (expected === ' ') {
                        return true;
                    }
                    return actual.indexOf(expected) > -1;
                }

                ngModel.$parsers.push(function(inputValue) {
                    // dont put empty space to model
                    console.info(inputValue);
                    if (inputValue === ' ') {
                        return '';
                    }
                    return inputValue;
                });

                // trigger the popup on 'click' because 'focus'
                // is also triggered after the item selection
                element.bind('click', triggerPopup);

                // compare function that treats the empty space as a match
                scope.emptyOrMatch = emptyOrMatch;
            }
        };
    })
    .directive('focusMe', function($timeout, $parse) {
        return {
            // scope: true,   // optionally create a child scope
            link: function(scope, element, attrs) {
                var model = $parse(attrs.focusMe);
                scope.$watch(model, function(value) {
                    if (value === true) {
                        $timeout(function() {
                            element[0].focus();
                        });
                    }
                });
            }
        };
    })
    .directive('emptyTypeahead', function(SecretEmptyKey) {
        return {
            require: 'ngModel',
            link: function(scope, element, attrs, modelCtrl) {
                // this parser run before typeahead's parser
                modelCtrl.$parsers.unshift(function(inputValue) {
                    var value = (inputValue ? inputValue : SecretEmptyKey); // replace empty string with secretEmptyKey to bypass typeahead-min-length check
                    modelCtrl.$viewValue = value; // this $viewValue must match the inputValue pass to typehead directive
                    return value;
                });

                // this parser run after typeahead's parser
                modelCtrl.$parsers.push(function(inputValue) {
                    return inputValue === SecretEmptyKey ? '' : inputValue; // set the secretEmptyKey back to empty string
                });
            }
        };
    });
