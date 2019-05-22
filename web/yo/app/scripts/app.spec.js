'use strict';
/**
 * @ngdoc overview
 * @name oncokb
 * @description
 * # oncokb
 *
 * Main module of the application.
 */
var OncoKB = {
    global: {},
    config: {},
    backingUp: false,
    gene: {
        "background": "AKT1 is a serine/threonine protein kinase that is a critical downstream effector in the PI3K (phosphoinositide 3-kinase) signaling pathway. Following activation of PI3K, cytosolic inactive AKT1 is recruited to the membrane and engages PIP3 (PtdIns3,4,5-P3), leading to phosphorylation and activation of AKT1 (PMID: 28431241). AKT1 can activate a number of downstream substrates, including GSK3, FOXO and mTORC1, which are critical for cellular survival, proliferation, and metabolism (PMID: 9843996, 7611497). Negative regulation of AKT1 occurs when PI3K signaling is terminated by PTEN phosphatase activity (PMID: 28431241). AKT1 is frequently activated in cancers, typically through activation of the PI3K pathway or by inactivation of PTEN (PMID: 28431241). Activating mutations in AKT1 (PMID: 17611497, 23134728, 20440266) and infrequent AKT1 gene amplification (PMID: 18767981) have been identified in human cancers, which allow for phosphoinositide-independent AKT1 activation. The ATP-competitive AKT1 inhibitor AZD5363 has demonstrated activity in patients with AKT1-mutant cancers (PMID: 28489509). Negative feedback mechanisms can mediate AKT-inhibitor resistance in human cancers with dysregulated AKT signaling (PMID: 29535262, 29339542).",
        "background_review": {
            "lastReviewed": "",
            "updateTime": 1524594735169,
            "updatedBy": "Moriah Nissan"
        },
        "background_uuid": "82d247bc-2178-4273-96c2-ce7b6ca9ea15",
        "dmp_refseq_id": "NM_001014431.1",
        "isoform_override": "ENST00000349310",
        "mutations": [{
            "mutation_effect": {
                "description": "The AKT1 E17K mutation is located within the protein's pleckstrin homology domain (PMID: 17611497, 20440266). This mutation alters the lipid binding specificity of AKT1, allowing P13K-independent constitutive membrane localization and deregulated activation of AKT1 (PMID: 17611497, 18256540, 9843996). In vitro, AKT1 E17K alone was unable to support growth-factor independent growth of BaF3 pro-B cells (PMID: 23134728). However, co-expression of AKT1 E17K with an activated form of MEK1 promoted factor-independent growth in vitro, and promoted massive growth of subcutaneously implanted tumors in mice; this suggests cooperation between AKT1 and MEK in oncogenic activation (PMID: 23134728). In addition, the E17K mutation partially disrupts inhibitory interactions between the pleckstrin homology domain (PHD) and the kinase domain (KD) (PMID: 23134728). Expression of the mutant in cells causes increased signaling to AKT substrates and cell transformation (PMID: 17611497, 23134728, 23741320). AKT1 E17K mutations have also been identified in the tissue overgrowth Proteus syndrome (PMID: 21793738).",
                "description_review": {
                    "updateTime": 1493325255069
                },
                "description_uuid": "83b8a796-0f4b-4555-96ae-809d2aa45d62",
                "effect": "Gain-of-function",
                "effect_review": {
                    "updateTime": 1493325255069
                },
                "effect_uuid": "d9043ef0-6106-4951-af64-f934d00041c7",
                "oncogenic": "Yes",
                "oncogenic_review": {
                    "updateTime": 1490821090000,
                    "updatedBy": "Debyani Chakravarty"
                },
                "oncogenic_uuid": "ef3e7a30-73e5-41b0-8b3e-f9f08b92c50a",
                "short": ""
            },
            "mutation_effect_comments": [{
                "content": "taken from 3A clinical summaries sheet",
                "date": "1473355244883",
                "email": "moriah.heller@gmail.com",
                "resolved": "false",
                "userName": "Moriah Nissan"
            }, {
                "content": "While there are no FDA-approved or NCCN-compendium listed treatments specifically for patients with breast cancer harboring the AKT1 E17K mutation, there is compelling clinical and preclinical data supporting the use of AKT-inhibitors in this patient population. In multiple phase I clinical studies patients with solid tumors harboring activating AKT1 mutations achieved clinical benefit following treatment with an orally available, ATP-competitive pan-AKT inhibitor and independent preclinical studies have biologically characterized this mutant to be associated with sensitivity to AKT-targeted inhibitors, as evidenced by target and consequent pathway inhibition upon drug treatment.",
                "date": "1490821094497",
                "email": "debyani.c@gmail.com",
                "resolved": "false",
                "userName": "Debyani Chakravarty"
            }],
            "mutation_effect_uuid": "91d73a66-0a4b-472e-8b6a-95e87e4e5520",
            "name": "E17K",
            "name_review": {
                "updateTime": 1493325255069
            },
            "name_uuid": "606c509a-c9f4-4541-8b93-58491e6ba8eb",
            "tumors": [{
                "TIs": [{
                    "name": "Standard implications for sensitivity to therapy",
                    "name_uuid": "a441a693-f5be-4abb-b634-d36529b4862d",
                    "type": "SS"
                }, {
                    "name": "Standard implications for resistance to therapy",
                    "name_uuid": "05e34064-376f-4745-bf17-ca3e01f24ed1",
                    "type": "SR"
                }, {
                    "name": "Investigational implications for sensitivity to therapy",
                    "name_uuid": "b7c07f98-47fa-4a80-b72b-d496c97929d8",
                    "treatments": [{
                        "description": "AZD5363 is an orally available, ATP-competitive pan-AKT inhibitor that targets the PI3K/AKT/mTOR signaling pathway (PMID: 23394218). In a Phase I basket study of AZD5363 in AKT E17K-mutated cancers, AZD5363 treatment induced target lesion regression in fifteen of twenty evaluable patients with breast cancer, including four RECIST partial responses, with a median progression-free survival of 5.5 months (PMID: 28489509). In a second Phase I study of 41 patients with solid tumors in Japan, two patients with the AKT E17K mutation, one having breast cancer and the other having ovarian cancer, had partial responses to AZD5363 (PMID: 26351323). In vitro studies of breast cancer explants harboring the AKT E17K mutation have shown that AZD5363 inhibits tumor growth and reduces signaling downstream of AKT, including reduced phosphorylation of PRAS40 and S6 (PMID: 22294718).",
                        "description_review": {
                            "updateTime": 1517434586075,
                            "updatedBy": "Moriah Nissan"
                        },
                        "description_uuid": "4aedd549-8199-4790-9532-35bcf94e7302",
                        "indication": "",
                        "indication_review": {
                            "updateTime": 1493325255069
                        },
                        "indication_uuid": "95694ac1-4d1a-443b-af3a-2f5912137955",
                        "level": "3A",
                        "level_review": {
                            "updateTime": 1493325255069
                        },
                        "level_uuid": "49fd8572-55aa-4b12-afe4-6672e4ac9399",
                        "name": "AZD5363",
                        "name_comments": [{
                            "content": "Add the PMID for level 3 evidence: 26351323; Hyman et al, Abstract#B109, Nov 7 2015 AACR-NCI-EORTC International Conference on Molecular Targets and Cancer Therapeutic",
                            "date": "1445367262725",
                            "email": "s.m.phillips2@gmail.com",
                            "resolved": "true",
                            "userName": "Sarah Phillips"
                        }, {
                            "content": "Hyman et al, Abstract#B109, Nov 7 2015 AACR-NCI-EORTC International Conference on Molecular Targets and Cancer Therapeutic",
                            "date": "1458854439398",
                            "email": "s.m.phillips2@gmail.com",
                            "resolved": "true",
                            "userName": "Sarah Phillips"
                        }],
                        "name_review": {
                            "updateTime": 1517434586075
                        },
                        "name_uuid": "9d49b289-fb22-4736-aab2-74b201ce68f5",
                        "propagation": "3B",
                        "propagation_uuid": "7e83f7c5-d359-45aa-b5f2-b4757a881107",
                        "short": ""
                    }],
                    "type": "IS"
                }, {
                    "name": "Investigational implications for resistance to therapy",
                    "name_uuid": "5d225e58-dcc4-4b68-b18b-5c2849ff9395",
                    "type": "IR"
                }],
                "cancerTypes": [{
                    "code": "",
                    "mainType": "Breast Cancer",
                    "subtype": ""
                }],
                "cancerTypes_uuid": "a6ef94e4-f8c9-489e-b1d9-5a7b1821698e",
                "diagnostic": {
                    "description": "",
                    "description_review": {
                        "updateTime": 1512692798251
                    },
                    "description_uuid": "bb8b36d4-40a3-4d40-819d-6e1f76e005da",
                    "level": "",
                    "level_uuid": "2fbb9ec4-6869-4a9f-a67a-1df6b4a5d3f6",
                    "short": ""
                },
                "diagnostic_uuid": "80b401af-d302-4437-9dad-4e72be534662",
                "prognostic": {
                    "description": "",
                    "description_review": {
                        "updateTime": 1512692797994
                    },
                    "description_uuid": "4f106a5f-9909-4381-bb9e-e193ecd8327f",
                    "level": "",
                    "level_uuid": "c4bcd35c-ed2c-478f-8d9e-234c0b07321c",
                    "short": ""
                },
                "prognostic_uuid": "33741aa9-014d-4582-9268-452db26b96a3",
                "summary": "There is promising clinical data in patients with AKT1 E17K mutant ER+ ductal breast cancer treated with the pan-AKT targeted inhibitor AZD5363.",
                "summary_review": {
                    "lastReviewed": "There is promising clinical data in patients with AKT1 E17K mutant ER+ ductal breast cancer treated with the pan-AKT targeted inhibitor AZD5363.",
                    "updateTime": 1508179719974
                },
                "summary_uuid": "66667275-ca5c-4357-80e5-790747fc6c1b"
            }, {
                "TIs": [{
                    "name": "Standard implications for sensitivity to therapy",
                    "name_uuid": "75f2ba31-595e-40ff-a54a-9246e85201ff",
                    "type": "SS"
                }, {
                    "name": "Standard implications for resistance to therapy",
                    "name_uuid": "dec0092d-b031-49de-ad16-c88c6f575fca",
                    "type": "SR"
                }, {
                    "name": "Investigational implications for sensitivity to therapy",
                    "name_uuid": "af8079eb-fc14-41bd-93e3-5419f8425ce7",
                    "treatments": [{
                        "description": "AZD5363 is an orally available, ATP-competitive pan-AKT inhibitor that targets the PI3K/AKT/mTOR signaling pathway (PMID: 23394218). In a Phase I basket study of AZD5363 in AKT E17K-mutated cancers, AZD5363 treatment induced partial responses in two patients with endometrial cancer and one patient with cervical cancer, with a median progression-free survival of 6.6 months (PMID: 28489509). In a second Phase I study of 41 patients with solid tumors in Japan, two patients with the AKT E17K mutation, one having breast cancer and the other having ovarian cancer, had partial responses to AZD5363 (PMID: 26351323). In vitro studies of breast cancer explants harboring the AKT E17K mutation have shown that AZD5363 inhibits tumor growth and reduces signaling downstream of AKT, including reduced phosphorylation of PRAS40 and S6 (PMID: 22294718).",
                        "description_review": {
                            "updateTime": 1517434686850,
                            "updatedBy": "Moriah Nissan"
                        },
                        "description_uuid": "c5f3a0f0-8581-4069-992b-63893c2e5c79",
                        "indication": "",
                        "indication_review": {
                            "updateTime": 1493325255069
                        },
                        "indication_uuid": "d55bee63-62db-4464-8fd6-ebbffccbca5e",
                        "level": "3A",
                        "level_review": {
                            "updateTime": 1493325255069
                        },
                        "level_uuid": "2700403e-d188-41c5-95b1-8635ff6a6904",
                        "name": "AZD5363",
                        "name_comments": [{
                            "content": "Hyman et al, Abstract#B109, Nov 7 2015 AACR-NCI-EORTC International Conference on Molecular Targets and Cancer Therapeutic",
                            "date": "1458854483280",
                            "email": "s.m.phillips2@gmail.com",
                            "resolved": "false",
                            "userName": "Sarah Phillips"
                        }, {
                            "content": "For Level 3A: 26351323; Hyman et al, Abstract#B109, Nov 7 2015 AACR-NCI-EORTC International Conference on Molecular Targets and Cancer Therapeutic",
                            "date": "1459273152271",
                            "email": "s.m.phillips2@gmail.com",
                            "resolved": "false",
                            "userName": "Sarah Phillips"
                        }],
                        "name_review": {
                            "updateTime": 1517434686850
                        },
                        "name_uuid": "4a98d8b8-29bd-4d6f-8ec1-e1eedf125c80",
                        "propagation": "3B",
                        "propagation_uuid": "05a56a07-d1e5-4778-b4d9-5679dbe294f2",
                        "short": ""
                    }],
                    "type": "IS"
                }, {
                    "name": "Investigational implications for resistance to therapy",
                    "name_uuid": "43d443b5-24b4-469b-987e-14016c69aed2",
                    "type": "IR"
                }],
                "cancerTypes": [{
                    "code": "",
                    "mainType": "Endometrial Cancer",
                    "subtype": ""
                }, {
                    "code": "",
                    "mainType": "Ovarian Cancer",
                    "subtype": ""
                }],
                "cancerTypes_uuid": "9ada42fa-fa40-455b-a2c2-508a2492e5fa",
                "diagnostic": {
                    "description": "",
                    "description_uuid": "74e1d8a8-f4e8-4353-884b-ad721ce5881d",
                    "level": "",
                    "level_uuid": "4dfb8f9b-fc5b-4979-a19f-8d92c6176f00",
                    "short": ""
                },
                "diagnostic_uuid": "97e41c59-054f-4f42-94b2-01b58f3b7d6c",
                "prognostic": {
                    "description": "",
                    "description_review": {
                        "updateTime": 1493325255069
                    },
                    "description_uuid": "2be3d33e-daae-4b66-99bd-0c747cb77733",
                    "level": "",
                    "level_uuid": "b7ff9248-3bd1-4c1a-b55f-29e4cbd3f54c",
                    "short": ""
                },
                "prognostic_uuid": "1924db33-3e49-4df9-a5ca-a5bdd9088f5e",
                "summary": "There is promising clinical data in patients with AKT1 E17K mutant gynecological cancer treated with the pan-AKT targeted inhibitor AZD5363.",
                "summary_review": {
                    "updateTime": 1508177294799,
                    "updatedBy": "Debyani Chakravarty"
                },
                "summary_uuid": "84b36b75-e817-47f0-bce3-c4d7f108a49b"
            }]
        }],
        "name": "AKT1",
        "name_comments": [{
            "content": "Hi Andy, In your cleanup please make sure that PMIDs: 9843996, 23134728 are curated. Thanks so much!",
            "date": "1425671517595",
            "email": "debyani.c@gmail.com",
            "resolved": "true",
            "userName": "Debyani Chakravarty"
        }],
        "summary": "AKT1, an intracellular kinase, is mutated at low frequencies in a diverse range of cancers.",
        "summary_comments": [{
            "content": "AKT1 encodes an intracellular kinase which is a critical downstream effector in the oncogenic PI3K signaling pathway. Mutation of the AKT1 gene are observed at low frequencies (<5%) in a range of tumors including breast, lung and colorectal.",
            "date": "1460659497528",
            "email": "s.m.phillips2@gmail.com",
            "resolved": "true",
            "userName": "Sarah Phillips"
        }, {
            "content": "AKT1 encodes a serine/threonine kinase involved in metabolism, proliferation, cell survival, growth and angiogenesis. Mutations of AKT1 gene are found in breast, lung and colorectal cancers, among others.",
            "date": "1460659543592",
            "email": "s.m.phillips2@gmail.com",
            "resolved": "true",
            "userName": "Sarah Phillips"
        }],
        "summary_review": {
            "lastReviewed": "",
            "updateTime": 1493325255069,
            "updatedBy": 'Jiaojiao wang'
        },
        "summary_uuid": "7c2782d9-fbde-417b-817f-b16448b931f5",
        "type": {
            "ocg": "Oncogene",
            "ocg_review": {
                "updateTime": 1493325255069
            },
            "ocg_uuid": "55c0ed9b-477e-469b-ae8a-9a14572ae8ac",
            "tsg": "",
            "tsg_review": {
                "updateTime": 1493325255069
            },
            "tsg_uuid": "ad6e2b65-f03c-4385-b5fa-e162ce26f2f6"
        },
        "type_uuid": "a3c1500e-46fb-4834-ae06-eadc6f6ca341"
    }
};
OncoKB.config = {
    "apiLink": "",
    "curationLink": "",
    "privateApiLink": "",
    "publicApiLink": "",
    "testing": true,
    "production": false,
    "firebaseConfig": {
        "apiKey": "",
        "authDomain": "",
        "databaseURL": "",
        "projectId": "",
        "storageBucket": "",
        "messagingSenderId": ""
    }
};
var oncokbApp = angular.module('oncokbApp', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'ui.bootstrap',
    'localytics.directives',
    'dialogs.main',
    'dialogs.default-translations',
    'RecursionHelper',
    'xml',
    'contenteditable',
    'datatables',
    'datatables.bootstrap',
    'ui.sortable',
    'firebase'
])
    .value('OncoKB', OncoKB)
    // This is used for typeahead
    .constant('SecretEmptyKey', '[$empty$]')
    .constant('loadingScreen', window.loadingScreen)
    .constant('S', window.S)
    .constant('_', window._)
    .constant('Levenshtein', window.Levenshtein)
    .constant('PDF', window.jsPDF)
    .constant('UUIDjs', window.UUIDjs)
    .config(function ($provide, $locationProvider, $routeProvider, $sceProvider, dialogsProvider, $animateProvider, x2jsProvider) {
    });

angular.module('oncokbApp').run(
    ['$window', '$timeout', '$rootScope', '$location', 'loadingScreen', 'DatabaseConnector', 'dialogs', 'mainUtils', 'user', 'loadFiles',
        function ($window, $timeout, $rootScope, $location, loadingScreen, DatabaseConnector, dialogs, mainUtils, user, loadFiles) {
            $rootScope.errors = [];
            $rootScope.internal = true;
            $rootScope.meta = {
                levelsDesc: {
                    '0': 'FDA-approved drug in this indication irrespective of gene/variant biomarker',
                    '1': 'FDA-recognized biomarker predictive of response to an FDA-approved drug in this indication',
                    '2A': 'Standard of care biomarker predictive of response to an FDA-approved drug in this indication',
                    '2B': 'Standard of care biomarker predictive of response to an FDA-approved drug in another indication but not standard of care for this indication',
                    '3A': 'Compelling clinical evidence supports the biomarker as being predictive of response to a drug in this indication but neither biomarker and drug are standard of care',
                    '3B': 'Compelling clinical evidence supports the biomarker as being predictive of response to a drug in another indication but neither biomarker and drug are standard of care',
                    '4': 'Compelling biological evidence supports the biomarker as being predictive of response to a drug but neither biomarker and drug are standard of care',
                    'R1': 'Standard of care biomarker predictive of resistance to an FDA-approved drug in this indication',
                    'R2': 'Not NCCN compendium-listed biomarker, but clinical evidence linking this biomarker to drug resistance',
                    'R3': 'Not NCCN compendium-listed biomarker, but preclinical evidence potentially linking this biomarker to drug resistance'
                },
                levelsDescHtml: {
                    '0': '<span>FDA-approved drug in this indication irrespective of gene/variant biomarker</span>',
                    '1': '<span><b>FDA-recognized</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
                    '2A': '<span><b>Standard of care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
                    '2B': '<span><b>Standard of care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in another indication</b> but not standard of care for this indication</span>',
                    '3A': '<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in this indication</b> but neither biomarker and drug are standard of care</span>',
                    '3B': '<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in another indication</b> but neither biomarker and drug are standard of care</span>',
                    '4': '<span><b>Compelling biological evidence</b> supports the biomarker as being predictive of response to a drug but neither biomarker and drug are standard of care</span>',
                    'R1': '<span><b>Standard of care</b> biomarker predictive of <b>resistance</b> to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
                    'R2': '<span>Not NCCN compendium-listed biomarker, but clinical evidence linking this biomarker to drug resistance</span>',
                    'R3': '<span>Not NCCN compendium-listed biomarker, but preclinical evidence potentially linking this biomarker to drug resistance</span>'
                },
                colorsByLevel: {
                    Level_1: '#33A02C',
                    Level_2A: '#1F78B4',
                    Level_2B: '#80B1D3',
                    Level_3A: '#984EA3',
                    Level_3B: '#BE98CE',
                    Level_4: '#424242',
                    Level_R1: '#EE3424',
                    Level_R2: '#F79A92',
                    Level_R3: '#FCD6D3'
                }
            };

            $rootScope.addError = function (error) {
                $rootScope.errors.push(error);
            };
            $rootScope.me = {
                admin: true,
                name: 'Jiaojiao wang',
                email: 'jiaojiaowanghere@gmail.com'
            };
        }]);

/**
 * Bootstrap the app
 */
(function (_, angular, $) {
    /**
     * Get OncoKB configurations
     */
    function fetchData() {
        firebase.initializeApp(OncoKB.config.firebaseConfig);
        bootstrapApplication();
    }

    /**
     * Bootstrap Angular application
     */
    function bootstrapApplication() {
        angular.element(document).ready(function () {
            angular.bootstrap(document, ['oncokbApp']);
        });
    }

    fetchData();
})(window._, window.angular, window.jQuery);
