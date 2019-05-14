describe('Mainutils', function() {
    beforeEach(module('oncokbApp'));
    var mainUtilsFactory = null;
    beforeEach(inject(function(mainUtils) {
        mainUtilsFactory = mainUtils;
    }));
    it('trimMutationName should trim p. from mutation name', function() {
        expect(mainUtilsFactory.trimMutationName('p.120V')).toEqual('120V');
    });
    it('They should be in the developers list', function() {
        expect(mainUtilsFactory.developerCheck('jianjiong gao')).toEqual(true);
        expect(mainUtilsFactory.developerCheck('hongxin zhang')).toEqual(true);
        expect(mainUtilsFactory.developerCheck('jing su')).toEqual(true);
        expect(mainUtilsFactory.developerCheck('jiaojiao wang')).toEqual(true);
    });
    it('Current time stamp should not be listed as expired', function() {
        expect(mainUtilsFactory.isExpiredCuration(new Date().getTime())).toEqual(false);
    });
    it('The cancer type names returned is wrong', function() {
        expect(mainUtilsFactory.getCancerTypesName()).toEqual(null);
        var cancerTypes1 = [{
            mainType: 'All Tumors',
            subtype: '',
            code: ''
        }];
        expect(mainUtilsFactory.getCancerTypesName(cancerTypes1)).toEqual('All Tumors');
        var cancerTypes2 = [{
            mainType: 'All Tumors',
            subtype: 'SubType',
            code: ''
        }];
        expect(mainUtilsFactory.getCancerTypesName(cancerTypes2)).toEqual('SubType');
    });
    it('getFullCancerTypesName should return a sorted list of information', function () {
        var cancerTypesExample = [
            {mainType: 'mainType', subtype: '', code: ''},
            {mainType: '', subtype: 'subType', code: ''},
            {mainType: '', subtype: '', code: 'code'},
            {mainType: 'a', subtype: 'b', code: 'c'},
            {mainType: 'b', subtype:'', code:'d'}
        ];
        expect(mainUtilsFactory.getFullCancerTypesName()).toEqual(null);
        expect(mainUtilsFactory.getFullCancerTypesName(cancerTypesExample)).toEqual('--subType, -code-, a-c-b, b-d-, mainType--');
    });
    var cancerTypesMetaExample = [
        {mainType: {name: 'mainA', code: 'main1'}, subtype: {name: 'subA', code: 'sub1'}},
        {mainType: '', subtype: {name: 'subB', code: 'sub2'}},
        {mainType: {name: 'mainC', code: 'main3'}, subtype: ''},
        {mainType: '', subtype: {name: '', code: 'sub4'}},
        {mainType: '', subtype: {name: 'subE'}}];
    it('getNewCancerTypesName should return a sorted list of names', function () {
        expect(mainUtilsFactory.getNewCancerTypesName()).toEqual(null);
        expect(mainUtilsFactory.getNewCancerTypesName(cancerTypesMetaExample)).toEqual('mainC, subA, subB, subE');
    });
    it('getFullCancerTypesName should return a sorted list of information', function () {
        expect(mainUtilsFactory.getFullCancerTypesNames()).toEqual(null);
        expect(mainUtilsFactory.getFullCancerTypesNames(cancerTypesMetaExample)).toEqual('--subE, -sub2-subB, -sub4-, mainA-sub1-subA, mainC--');
    });
    it('hasDuplicatedCancerTypes should return true if there are duplicated cancerTypes', function () {
        var duplicatedCancerTypesMetaExample =[
            {mainType: {name: 'mainA', code: 'main1'}, subtype: {name: 'subA', code: 'sub1'}},
            {mainType: '', subtype: {name: 'subB', code: 'sub2'}},
            {mainType: '', subtype: {name: 'subB', code: 'sub2'}},
            {mainType: {name: 'mainC', code: 'main3'}}
        ];
        expect(mainUtilsFactory.hasDuplicateCancerTypes(duplicatedCancerTypesMetaExample)).toEqual(true);
        expect(mainUtilsFactory.hasDuplicateCancerTypes(cancerTypesMetaExample)).toEqual(false);
        expect(mainUtilsFactory.hasDuplicateCancerTypes()).toEqual(false);
    });
    it('containMainType should return true if it contatins', function () {
        expect(mainUtilsFactory.containMainType(cancerTypesMetaExample)).toEqual(null);
    });

    it('Reviewed data is not processed in the designed way', function() {
        var originalData = {
            summary_comments: [ {
                "content" : "Needs additional curation",
                "date" : "1445975597693",
                "email" : "s.m.phillips2@gmail.com",
                "resolved" : "false",
                "userName" : "Sarah Phillips"
              }],
              summary: 'This is the gene summay content',
              summary_review: {
                lastReviewed: 'This is the previsoud gene summary content',
                updateTime: 1531828464480,
                updatedBy: 'Jiaojiao wang'
              }
        };
        var keys = ['summary'];
        var noCommentsResult = {
            summary: 'This is the gene summay content',
            summary_review: {
                lastReviewed: 'This is the previsoud gene summary content',
                updateTime: 1531828464480,
                updatedBy: 'Jiaojiao wang'
            }
        };
        // this should be the json data that will be passed into database
        var resultForDB = {
            summary: 'This is the previsoud gene summary content',
            summary_review: {
                lastReviewed: 'This is the previsoud gene summary content',
                updateTime: 1531828464480,
                updatedBy: 'Jiaojiao wang'
            }
        };
        var data = angular.copy(originalData);
        mainUtilsFactory.processData(data, keys, true);
        expect(data).toEqual(noCommentsResult);
        var data = angular.copy(originalData);
        mainUtilsFactory.processData(data, keys, true, true)
        expect(data).toEqual(resultForDB);
    });
    it('Expired curation check is wrong', function() {
        var oneMonthAgo = new Date();
        oneMonthAgo.setMonth(oneMonthAgo.getMonth()-1);
        expect(mainUtilsFactory.isExpiredCuration(oneMonthAgo)).toEqual(true);

        var twoDaysAgo = new Date();
        twoDaysAgo.setDate(twoDaysAgo.getDate()-2);
        expect(mainUtilsFactory.isExpiredCuration(twoDaysAgo)).toEqual(true);

        var now = new Date();
        expect(mainUtilsFactory.isExpiredCuration(now)).toEqual(false);
    });
    it('Fail to return VUS in the right format', function() {
        var vusFromFirebase = {
            "-LH_Yd8HOp81juUJroAG" : {
              "name" : "T104P",
              "time" : {
                "by" : {
                  "email" : "kpgala15@gmail.com",
                  "name" : "Kinisha Gala"
                },
                "value" : 1514944647037
              }
            },
            "-LH_YdEb20N4XeoIEFnu" : {
              "name" : "Y326H",
              "name_comments" : [ {
                "content" : "PMID: 23009571",
                "date" : "1515703300669",
                "email" : "moriah.heller@gmail.com",
                "resolved" : "false",
                "userName" : "Moriah Nissan"
              } ],
              "time" : {
                "by" : {
                  "email" : "moriah.heller@gmail.com",
                  "name" : "Moriah Nissan"
                },
                "value" : 1515703295003
              }
            }
          };
        var noCommentsResult = [{
            "name" : "T104P",
            "time" : {
              "by" : {
                "email" : "kpgala15@gmail.com",
                "name" : "Kinisha Gala"
              },
              "value" : 1514944647037
            }
          }, {
            "name" : "Y326H",
            "time" : {
              "by" : {
                "email" : "moriah.heller@gmail.com",
                "name" : "Moriah Nissan"
              },
              "value" : 1515703295003
            }
        }];
        var withCommentsReuslt = [{
            "name" : "T104P",
            "time" : {
              "by" : {
                "email" : "kpgala15@gmail.com",
                "name" : "Kinisha Gala"
              },
              "value" : 1514944647037
            }
          }, {
            "name" : "Y326H",
            "name_comments" : [ {
              "content" : "PMID: 23009571",
              "date" : "1515703300669",
              "email" : "moriah.heller@gmail.com",
              "resolved" : "false",
              "userName" : "Moriah Nissan"
            } ],
            "time" : {
              "by" : {
                "email" : "moriah.heller@gmail.com",
                "name" : "Moriah Nissan"
              },
              "value" : 1515703295003
            }
        }];
        expect(mainUtilsFactory.getVUSData(vusFromFirebase, true)).toEqual(noCommentsResult);
        expect(mainUtilsFactory.getVUSData(vusFromFirebase, false)).toEqual(withCommentsReuslt);
        expect(mainUtilsFactory.getVUSData(vusFromFirebase)).toEqual(withCommentsReuslt);
    });
    it('Sections are not excluded correctly', function() {
        var newlyAddedReviewObj = {
            added: true,
            updateTime: 1515703295003,
            updatedBy: 'Jiaojiao wang'
        };
        var newlyRemovedReviewObj = {
            removed: true,
            updateTime: 1515703295003,
            updatedBy: 'Jiaojiao wang'
        };
        expect(mainUtilsFactory.shouldExclude(true, newlyAddedReviewObj)).toEqual(true);
        expect(mainUtilsFactory.shouldExclude(true, newlyRemovedReviewObj)).toEqual(false);
        expect(mainUtilsFactory.shouldExclude(false, newlyAddedReviewObj)).toEqual(false);
        expect(mainUtilsFactory.shouldExclude(false, newlyRemovedReviewObj)).toEqual(true);
    });
    it('Get the most rectently updated item is not working', function() {
        var now = new Date();

        var yesterday = new Date();
        yesterday.setDate(new Date().getDate()-1);

        var twoDaysAgo = new Date();
        twoDaysAgo.setDate(new Date().getDate()-2);

        var reviewObjs = [{
            updatedBy: 'cBioPortal',
            updateTime: yesterday.getTime(),
            lastReviewed: 'Previous effect value'
        }, {
            updatedBy: 'Jiaojiao wang',
            updateTime: now.getTime(),
            lastReviewed: 'Previous oncogenic value'
        }, {
            updatedBy: 'Jiaojiao wang',
            updateTime: twoDaysAgo.getTime(),
            lastReviewed: 'Previous description content'
        }];
        expect(mainUtilsFactory.mostRecentItem(reviewObjs)).toEqual(1);
    });
});
