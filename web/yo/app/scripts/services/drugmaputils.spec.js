describe('drugMapUtils', function(){
    beforeEach(module('oncokbApp'));
    var drugMapUtilsFactory = null;
    beforeEach(inject(function(drugMapUtils) {
        drugMapUtilsFactory = drugMapUtils;
    }));
    var drugListExample = {
        "001e534f-3e63-432f-90a6-d1af1759e4e2" : {
            "description" : "",
            "drugName" : "Encorafenib",
            "ncitCode" : "C98283",
            "ncitName" : "Encorafenib",
            "synonyms" : [ "LGX-818", "LGX818", "LGX 818", "ENCORAFENIB", "Braftovi" ],
            "uuid" : "001e534f-3e63-432f-90a6-d1af1759e4e2"
        },
        "0746dd92-8f4d-4bf7-9161-57a223cb67d5" : {
            "description" : "",
            "drugName" : "Pembrolizumab",
            "ncitCode" : "C106432",
            "ncitName" : "Pembrolizumab",
            "synonyms" : [ "MK-3475", "PEMBROLIZUMAB", "Keytruda", "Lambrolizumab", "SCH 900475", "Immunoglobulin G4, Anti-(Human Programmed Cell Death 1); Humanized Mouse Monoclonal (228-L-proline(H10-S&gt;P))gamma 4 Heavy Chain (134-218&apos;)-disulfide with Humanized Mouse Monoclonal Kappa Light Chain Dimer (226-226&apos;&apos;:229-229&apos;&apos;)-bisdisulfide" ],
            "uuid" : "0746dd92-8f4d-4bf7-9161-57a223cb67d5"
        },
        "0ade496c-77e6-45cd-8e96-7c7d87b4e5de" : {
            "description" : "",
            "drugName" : "MDM2 Antagonist RO5045337",
            "ncitCode" : "C91724",
            "ncitName" : "MDM2 Antagonist RO5045337",
            "synonyms" : [ "R7112", "RO-5045337", "RO5045337" ],
            "uuid" : "0ade496c-77e6-45cd-8e96-7c7d87b4e5de"
        },
        "0f991d49-4cf2-4975-b52f-d7d037aa7f11" : {
            "description" : "",
            "drugName" : "Nilotinib",
            "ncitCode" : "C48375",
            "ncitName" : "Nilotinib",
            "synonyms" : [ "AMN 107 Base Form", "nilotinib", "4-Methyl-3-((4-(3-pyridinyl)-2-pyrimidinyl)amino)-N-(5-(4-methyl-1H-imidazol-1-yl)-3-(trifluoromethyl)phenyl)benzamide", "NILOTINIB" ],
            "uuid" : "0f991d49-4cf2-4975-b52f-d7d037aa7f11"
        },
        "122b8921-3cd6-4b57-bdc8-5d39cc5465a1" : {
            "description" : "",
            "drugName" : "Ponatinib",
            "ncitCode" : "C95777",
            "ncitName" : "Ponatinib",
            "synonyms" : [ "PONATINIB", "AP-24534", "AP24534", "Benzamide, 3-(2-Imidazo(1,2-B)Pyridazin-3-Ylethynyl)-4-Methyl-N-(4-((4-Methyl-1- Piperazinyl)Methyl)-3-(Trifluoromethyl)Phenyl)" ],
            "uuid" : "122b8921-3cd6-4b57-bdc8-5d39cc5465a1"
        }
    };
    it('therapyStrToArr should change therapyName from String to Array', function () {
        expect(drugMapUtilsFactory.therapyStrToArr()).toEqual([]);
        expect(drugMapUtilsFactory.therapyStrToArr('Encorafenib + Pembrolizumab, Encorafenib')).toEqual([['Encorafenib', 'Pembrolizumab'], ['Encorafenib']]);
        expect(drugMapUtilsFactory.therapyStrToArr('Encorafenib + Pembrolizumab, MDM2 Antagonist RO5045337, MDM2 Antagonist RO5045337 + Nilotinib, Ponatinib')).toEqual([['Encorafenib', 'Pembrolizumab'], ['MDM2 Antagonist RO5045337'], ['MDM2 Antagonist RO5045337', 'Nilotinib'], ['Ponatinib']]);
    });
    it('drugUuidtoName should return the drugName combination based on the drugUuid combination', function () {
        expect(drugMapUtilsFactory.drugUuidtoName(null, drugListExample)).toEqual();
        expect(drugMapUtilsFactory.drugUuidtoName('0ade496c-77e6-45cd-8e96-7c7d87b4e5de + 001e534f-3e63-432f-90a6-d1af1759e4e2, 122b8921-3cd6-4b57-bdc8-5d39cc5465a1, 0f991d49-4cf2-4975-b52f-d7d037aa7f11 + 0746dd92-8f4d-4bf7-9161-57a223cb67d5', drugListExample)).toEqual('MDM2 Antagonist RO5045337 + Encorafenib, Ponatinib, Nilotinib + Pembrolizumab');
    });
    it('checkDifferenceBetweenTherapies should return sameDrugs, extraDrugs in the first array, extraDrugs in the second array', function () {
        var difference = {
            'sameDrugs': ['Encorafenib'],
            'extraDrugsInOld': ['Pembrolizumab', 'MDM2 Antagonist RO5045337'],
            'extraDrugsInNew': ['Nilotinib']
        };
        expect(drugMapUtilsFactory.checkDifferenceBetweenTherapies('Encorafenib, Pembrolizumab + MDM2 Antagonist RO5045337', 'Encorafenib + Nilotinib')).toEqual(difference);
        difference = {
            'sameDrugs': ['Encorafenib', 'Pembrolizumab'],
            'extraDrugsInOld': [],
            'extraDrugsInNew': []
        };
        expect(drugMapUtilsFactory.checkDifferenceBetweenTherapies('Encorafenib, Pembrolizumab', 'Encorafenib, Pembrolizumab')).toEqual(difference);
        difference = {
            'sameDrugs': ['Encorafenib'],
            'extraDrugsInOld': ['Pembrolizumab'],
            'extraDrugsInNew': []
        }
        expect(drugMapUtilsFactory.checkDifferenceBetweenTherapies('Encorafenib, Pembrolizumab', 'Encorafenib')).toEqual(difference);
        expect(drugMapUtilsFactory.checkDifferenceBetweenTherapies()).toEqual({});
        difference = {
            'sameDrugs': [],
            'extraDrugsInOld': ['Encorafenib', 'Pembrolizumab'],
            'extraDrugsInNew': []
        };
        expect(drugMapUtilsFactory.checkDifferenceBetweenTherapies('Encorafenib, Pembrolizumab')).toEqual(difference);
        difference = {
            'sameDrugs': [],
            'extraDrugsInOld': [],
            'extraDrugsInNew': ['Encorafenib', 'Pembrolizumab'],
        };
        expect(drugMapUtilsFactory.checkDifferenceBetweenTherapies(null, 'Encorafenib, Pembrolizumab')).toEqual(difference);
    });
    it('drugUuidtoDrug should return drugs objects based on the drugUuid combination', function () {
        var result = [
            [{
                    "description": "",
                    "drugName": "MDM2 Antagonist RO5045337",
                    "ncitCode": "C91724",
                    "ncitName": "MDM2 Antagonist RO5045337",
                    "synonyms": ["R7112", "RO-5045337", "RO5045337"],
                    "uuid": "0ade496c-77e6-45cd-8e96-7c7d87b4e5de"
                }, {
                    "description": "",
                    "drugName": "Encorafenib",
                    "ncitCode": "C98283",
                    "ncitName": "Encorafenib",
                    "synonyms": ["LGX-818", "LGX818", "LGX 818", "ENCORAFENIB", "Braftovi"],
                    "uuid": "001e534f-3e63-432f-90a6-d1af1759e4e2"
                }], [{
                    "description": "",
                    "drugName": "Ponatinib",
                    "ncitCode": "C95777",
                    "ncitName": "Ponatinib",
                    "synonyms": ["PONATINIB", "AP-24534", "AP24534", "Benzamide, 3-(2-Imidazo(1,2-B)Pyridazin-3-Ylethynyl)-4-Methyl-N-(4-((4-Methyl-1- Piperazinyl)Methyl)-3-(Trifluoromethyl)Phenyl)"],
                    "uuid": "122b8921-3cd6-4b57-bdc8-5d39cc5465a1"
                }]];
        expect(drugMapUtilsFactory.drugUuidtoDrug(null, drugListExample)).toEqual({});
        expect(drugMapUtilsFactory.drugUuidtoDrug('0ade496c-77e6-45cd-8e96-7c7d87b4e5de + 001e534f-3e63-432f-90a6-d1af1759e4e2, 122b8921-3cd6-4b57-bdc8-5d39cc5465a1', drugListExample)).toEqual(result);
    });

});
