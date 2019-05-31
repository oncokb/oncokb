describe('firebasePathUtils', function(){
    beforeEach(module('oncokbApp'));
    var firebasePathUtilsFactory = null;
    beforeEach(inject(function(firebasePathUtils) {
        firebasePathUtilsFactory = firebasePathUtils;
    }));
    it('getTherapyMapPath should return drug + \'/\' + geneName + \'/\' + mutationUuid + \'/cancerTypes/\' + cancerTypeUuid + \'/\' + therapyUuid', function () {
        expect(firebasePathUtilsFactory.getTherapyMapPath('drug', 'geneName', 'mutationUuid', 'cancerTypeUuid', 'therapyUuid')).toEqual('drug/geneName/mutationUuid/cancerTypes/cancerTypeUuid/therapyUuid');
    });
    it('getMutationNameMapPath should return drug + \'/\' + geneName + \'/\' + mutationUuid + \'/mutationName\'', function () {
        expect(firebasePathUtilsFactory.getMutationNameMapPath('drug', 'geneName', 'mutationUuid')).toEqual('drug/geneName/mutationUuid/mutationName');
    });
    it('getTherapyNameMapPath should return drug + \'/\' + geneName + \'/\' + mutationUuid + \'/cancerTypes/\' + cancerTypeUuid + \'/\' + therapyUuid + \'/name\'', function () {
        expect(firebasePathUtilsFactory.getTherapyNameMapPath('drug', 'geneName', 'mutationUuid', 'cancerTypeUuid', 'therapyUuid')).toEqual('drug/geneName/mutationUuid/cancerTypes/cancerTypeUuid/therapyUuid/name');
    });
    it('getCancerTypesMapPath should return \'Map/\' + drug + \'/\' + geneName + \'/\' + mutationUuid + \'/cancerTypes\'', function () {
        expect(firebasePathUtilsFactory.getCancerTypesMapPath('drug', 'geneName', 'mutationUuid')).toEqual('Map/drug/geneName/mutationUuid/cancerTypes');
    });
});
