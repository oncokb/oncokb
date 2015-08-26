'use strict';

describe('Service: stringUtils', function () {

  // load the service's module
  beforeEach(module('oncokbApp'));

  // instantiate service
  var stringUtils;
  beforeEach(inject(function (_stringUtils_) {
    stringUtils = _stringUtils_;
  }));

  it('should do something', function () {
    expect(!!stringUtils).toBe(true);
  });

});
