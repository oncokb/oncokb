'use strict';

describe('Directive: driveRealtimeString', function () {

  // load the directive's module
  beforeEach(module('oncokbApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<drive-realtime-string></drive-realtime-string>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the driveRealtimeString directive');
  }));
});
