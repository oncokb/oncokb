'use strict';

describe('Directive: vusItem', function () {

  // load the directive's module
  beforeEach(module('oncokbApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<vus-item></vus-item>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the vusItem directive');
  }));
});
