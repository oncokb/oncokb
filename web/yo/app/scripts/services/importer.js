'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.importer
 * @description
 * # importer
 * Service in the oncokbApp.
 */
angular.module('oncokbApp')
    .service('importer', function importer($timeout, documents, S, storage, OncoKB, $q, _) {
        var self = this;
        self.docs = [];
        self.docsL = 0;
        self.newFolder = '';
        self.parentFolder = OncoKB.config.backupFolderId; //backup folder under knowledgebase
        self.status = {
            backupIndex: -1,
            migrateIndex: -1
        };

        function backup(callback) {
            if (self.parentFolder) {
                if (!angular.isFunction(callback)) {
                    callback = undefined;
                }

                createFolder().then(function (result) {
                    if (result && result.error) {
                        console.error('Create folder failed.', result);
                        if (callback) {
                            callback();
                        }
                    } else {
                        // var docs = documents.get({title: 'BRAF'});
                        self.docs = documents.get();
                        self.docsL = self.docs.length;
                        copyData(0, callback);
                    }
                });
            } else {
                console.log('Backup folder ID needed.');
            }
        }

        function createFolder() {
            var deferred = $q.defer();

            storage.requireAuth(true).then(function (result) {
                if (result && !result.error) {
                    storage.createFolder(self.parentFolder).then(function (result) {
                        if (result.id) {
                            self.newFolder = result.id;
                            deferred.resolve(result);
                        } else {
                            deferred.reject(result);
                        }
                    });
                }
            });

            return deferred.promise;
        }

        function copyData(index, callback) {
            if (index < self.docs.length) {
                var doc = self.docs[index];
                copyFileData(self.newFolder, doc.id, doc.title, index).then(function () {
                    copyData(++index, callback);
                });
            } else {
                if (callback) {
                    callback();
                }
                console.log('finished');
            }
        }

        function migrate() {
            var destinationFolderId = '';
            var deferred = $q.defer();

            destinationFolderId = '0BzBfo69g8fP6RmczanJFSVlNSjA';

            if (destinationFolderId) {
                self.newFolder = destinationFolderId;
                self.docs = documents.get();
                self.docsL = self.docs.length;

                //copy foler permissions
                //assignPermission(OncoKB.config.folderId, self.newFolder)
                //    .then(function(result){
                //      if(result && result.error){
                //        console.log('Error when assigning folder permissions', result);
                //      }else{
                migrateOneFileProcess(0, deferred);
                //}
                //});
            } else {
                createFolder().then(function (result) {
                    if (result && result.error) {
                        console.log('Unable create folder.', result);
                    } else {
                        self.docs = documents.get();
                        self.docsL = self.docs.length;

                        //copy foler permissions
                        assignPermission(OncoKB.config.folderId, self.newFolder)
                            .then(function (result) {
                                if (result && result.error) {
                                    console.log('Error when assigning folder permissions', result);
                                } else {
                                    migrateOneFileProcess(0, deferred);
                                }
                            });
                    }
                });
            }

            return deferred.promise;
        }

        function migrateOneFileProcess(docIndex, deferred) {
            if (docIndex < self.docsL) {
                var doc = self.docs[docIndex];
                copyFileData(self.newFolder, doc.id, doc.title, docIndex)
                    .then(function (result) {
                        if (result && result.error) {
                            console.log(result);
                            migrateOneFileProcess(++docIndex, deferred);
                        } else {
                            assignPermission(doc.id, result.id)
                                .then(function (result) {
                                    if (result && result.error) {
                                        console.log('error', result);
                                    } else {
                                        migrateOneFileProcess(++docIndex, deferred);
                                    }
                                });
                        }
                    });
            } else {
                console.log('Migrating finished.');
                deferred.resolve();
            }
        }

        function assignPermission(oldDocId, newDocId) {
            var deferred = $q.defer();
            console.log('\t Giving permissions');
            storage.getPermission(oldDocId).then(function (result) {
                if (result && result.error) {
                    deferred.reject(result);
                } else {
                    assignIndividualPermission(newDocId, result.items, 0, deferred);
                    //var promises = [];
                    //promises = result.items.forEach(function(permission, i){
                    //  if(permission.id && permission.emailAddress && permission.role && permission.type && permission.role !== 'owner'){
                    //    promises.push();
                    //  }
                    //});
                    //$q.all(promises).then(function(){
                    //  deferred.resolve();
                    //});
                }
            });
            return deferred.promise;
        }

        function assignIndividualPermission(newDocId, items, itemIndex, deferred) {
            if (itemIndex < items.length) {
                var permission = items[itemIndex];
                console.log('\t\t\tp-', itemIndex + 1);
                if (permission.id && permission.emailAddress && permission.role && permission.type && permission.role !== 'owner') {
                    storage.insertPermission(newDocId, permission.emailAddress, permission.type, permission.role).then(function () {
                        //$timeout(function () {
                        assignIndividualPermission(newDocId, items, ++itemIndex, deferred);
                        //}, 100);
                    });
                } else {
                    console.log('\t\t\tskip-', permission);
                    //$timeout(function () {
                    assignIndividualPermission(newDocId, items, ++itemIndex, deferred);
                    //}, 100);
                }
            } else {
                deferred.resolve();
                console.log('\t\tAll permissions are assigned.');
            }
        }

        //Return all info
        function getVUSFullData(vus) {
            var vusData = [];
            if (vus) {
                vus.asArray().forEach(function (vusItem) {
                    var datum = {};
                    datum.name = vusItem.name.getText();
                    datum.time = [];
                    vusItem.time.asArray().forEach(function (time) {
                        var _time = {};
                        _time.value = time.value.getText();
                        _time.by = {};
                        _time.by.name = time.by.name.getText();
                        _time.by.email = time.by.email.getText();
                        datum.time.push(_time);
                    });
                    vusData.push(datum);
                });
            }
            return vusData;
        }

        //Only return last edit info
        function getVUSData(vus) {
            var vusData = [];
            if (vus) {
                vus.asArray().forEach(function (vusItem) {
                    var datum = {};
                    datum.name = vusItem.name.getText();
                    if(vusItem.time.length > 0) {
                        datum.lastEdit = vusItem.time.get(vusItem.time.length - 1).value.getText();
                    }
                    vusData.push(datum);
                });
            }
            return vusData;
        }

        function findMutationEffect(query) {
            var mapping = {
                "Conflicting reports": {
                    "after": "Unknown"
                },
                "Abnormal splicing": {
                    "after": "Likely Loss-of-function"
                },
                "acquired resistance to dasatinib": {
                    "after": "Likely Gain-of-function"
                },
                "Activating": {
                    "after": "Gain-of-function"
                },
                "Activating Activates PI3K pathway": {
                    "after": "Gain-of-function"
                },
                "Activating Activates transcription through fusion protein gain of function": {
                    "after": "Gain-of-function"
                },
                "Activating Activation of partner ERBB family members": {
                    "after": "Gain-of-function"
                },
                "Activating AKT3 through amplification is seen in multiple tumors such as ovarian, breast, melanoma and gliomas.": {
                    "after": "Gain-of-function"
                },
                "Activating Amplification of CCND2 is expected to result in aberrant activation of CDK4/6 to promote cell proliferation.": {
                    "after": "Likely Gain-of-function"
                },
                "Activating Amplification of CCND3 is expected to result in increased tumor cell proliferation.": {
                    "after": "Likely Gain-of-function"
                },
                "Activating BCOR-CCNB3 fusion activates Wnt and Hedgehog signaling pathways": {
                    "after": "Likely Switch-of-function"
                },
                "Activating BCOR-RARA fusion leads to transcriptional activation": {
                    "after": "Likely Switch-of-function"
                },
                "Activating Confers resistance to crizotinib": {
                    "after": "Likely Gain-of-function"
                },
                "Activating Confers resistance to gefitinib, erlotinib": {
                    "after": "Likely Gain-of-function"
                },
                "Activating Downstream activation of SRC activity, caused by upstream mutation within signaling cascade": {
                    "after": "Gain-of-function"
                },
                "Activating Expression and phosphorylation of JUN is activated during amplification.": {
                    "after": "Gain-of-function"
                },
                "Activating forms heterodimers with ERBB family members": {
                    "after": "Gain-of-function"
                },
                "Activating Further functional characterization is required.": {
                    "after": "Likely Gain-of-function"
                },
                "Activating Fusion of PVT1 to MYC likely increases the expression of the MYC gene": {
                    "after": "Likely Gain-of-function"
                },
                "Activating Fusion transcript leading to overexpression": {
                    "after": "Likely Gain-of-function"
                },
                "Activating Gain-of-function mutation of GNAS alternate splice form": {
                    "after": "Gain-of-function"
                },
                "Activating gate keeper mutation": {
                    "after": "Likely Gain-of-function"
                },
                "Activating In combination with the JAK2 V617F mutation.": {
                    "after": "Likely Gain-of-function"
                },
                "activating in vitro": {
                    "after": "Likely Gain-of-function"
                },
                "Activating Increased expression is predicted to lead to increased function.": {
                    "after": "Likely Gain-of-function"
                },
                "Activating increased formation of ERBB2 dimers": {
                    "after": "Gain-of-function"
                },
                "Activating increased formation of mutant ERBB2 dimers": {
                    "after": "Gain-of-function"
                },
                "Activating increased kinase activity": {
                    "after": "Gain-of-function"
                },
                "Activating increases mutant ERBB2-ERBB3 heterodimer formation": {
                    "after": "Gain-of-function"
                },
                "Activating increases mutant ERBB2-ERBB3 heterodimers": {
                    "after": "Gain-of-function"
                },
                "Activating It shows higher lipid kinase activity.": {
                    "after": "Gain-of-function"
                },
                "Activating MED12 exon 2 mutations dysregulate extracellular matrix organization and may elicit aberrant estrogen signaling": {
                    "after": "Likely Gain-of-function"
                },
                "Activating MED12 knockdown activates TGF-beta signaling": {
                    "after": "Likely Gain-of-function"
                },
                "Activating mild effect": {
                    "after": "Likely Gain-of-function"
                },
                "Activating modest activation": {
                    "after": "Likely Gain-of-function"
                },
                "Activating modest transforming ability": {
                    "after": "Likely Gain-of-function"
                },
                "Activating NUSAP activation in response to RB1 gene loss": {
                    "after": "Likely Loss-of-function"
                },
                "Activating Often found in context of KRAS mutations.": {
                    "after": "Gain-of-function"
                },
                "Activating or inactivating, probably tissue- and context-dependent": {
                    "after": "Unknown"
                },
                "Activating Overexpression, potentially leading to increased kinase activity even without activation.": {
                    "after": "Gain-of-function"
                },
                "Activating predicted to result in higher expression of AURKB": {
                    "after": "Likely Gain-of-function"
                },
                "Activating Questionable sensitivity to imatinib": {
                    "after": "Likely Gain-of-function"
                },
                "Activating Requires a second RAS alteration": {
                    "after": "Likely Gain-of-function"
                },
                "Activating results in nuclear localization of cyclin D1 protein": {
                    "after": "Gain-of-function"
                },
                "Activating Sensitive to EGFR TKIs erlotinib or gefitinib": {
                    "after": "Gain-of-function"
                },
                "Activating the ZC3H7B-BCOR chimeric gene alters epigenetic machinery leading to oncogenesis": {
                    "after": "Likely Switch-of-function"
                },
                "Activating This mutation is expected to stabilize a truncated Cyclin D3 protein, promoting cell proliferation.": {
                    "after": "Likely Gain-of-function"
                },
                "Activating This mutation is expected to stabilize Cyclin D3 protein levels, promoting cell proliferation.": {
                    "after": "Likely Gain-of-function"
                },
                "Activating This mutation mediates tumor resistance to first generation EGFR TKIs erlotinib and gefitinib": {
                    "after": "Gain-of-function"
                },
                "Activating This mutation stabilizes Cyclin D3 protein levels, promoting cell proliferation.": {
                    "after": "Gain-of-function"
                },
                "Activating This seems to be a very rare alteration.": {
                    "after": "Gain-of-function"
                },
                "Activating Weakly": {
                    "after": "Likely Gain-of-function"
                },
                "activity similar to wildtype": {
                    "after": "Neutral"
                },
                "Affects the proofreading function of POLE; however, there is no known effect on the protein's polymerase function.": {
                    "after": "Unknown"
                },
                "C121S is a MEK1 and BRAF inhibitor resistance allele": {
                    "after": "Likely Gain-of-function"
                },
                "Change of function; alters the protein's DNA binfding abilities": {
                    "after": "Switch-of-function"
                },
                "Clear evidence is lacking.": {
                    "after": "Unknown"
                },
                "Computer modeling suggests that this mutation may prevent RHOA interaction with GEF proteins, in this way possibly decreasing basal RHOA activation": {
                    "after": "Unknown"
                },
                "Confers resistance to crizotinib": {
                    "after": "Likely Gain-of-function"
                },
                "conflicting data": {
                    "after": "Unknown"
                },
                "Conflicting data exists for the effect of this mutation (PMID: 23822953, 22328973)": {
                    "after": "Unknown"
                },
                "Conflicting evidence": {
                    "after": "Unknown"
                },
                "Crizotinib": {
                    "after": "Likely Gain-of-function"
                },
                "crizotinib and alectinib resistance": {
                    "after": "Likely Gain-of-function"
                },
                "Crizotinib and ceritinib resistance": {
                    "after": "Likely Gain-of-function"
                },
                "Data suggests this mutation is not oncogenic.": {
                    "after": "Likely Neutral"
                },
                "deleterious or neutral": {
                    "after": "Loss-of-function"
                },
                "Drug resistance": {
                    "after": "Unknown"
                },
                "Evidence for inactivation": {
                    "after": "Unknown"
                },
                "Evidence for pathogenicity": {
                    "after": "Likely Gain-of-function"
                },
                "Functional role unclear": {
                    "after": "Unknown"
                },
                "Functional significance of mutation unknown": {
                    "after": "Unknown"
                },
                "Functional significance unknown": {
                    "after": "Unknown"
                },
                "Further functional characterization is required.": {
                    "after": "Likely Gain-of-function"
                },
                "Gatekeeper mutation in ATP binding pocket": {
                    "after": "Likely Gain-of-function"
                },
                "Germline susceptibility variant": {
                    "after": "Unknown"
                },
                "Hypothesized from structural studies to interfere with regulatory subunit p85 interaction, leading to p110alpha hyperactivation": {
                    "after": "Likely Gain-of-function"
                },
                "Hypothesized to be activating but no functional data available": {
                    "after": "Unknown"
                },
                "Imatinib resistance": {
                    "after": "Likely Gain-of-function"
                },
                "Inactivating": {
                    "after": "Loss-of-function"
                },
                "Inactivating Acts as a tumor suppressor gene": {
                    "after": "Likely Loss-of-function"
                },
                "Inactivating As suggested by predictions.": {
                    "after": "Likely Loss-of-function"
                },
                "Inactivating As suggested by studies of other missense mutations in the same locus.": {
                    "after": "Likely Loss-of-function"
                },
                "Inactivating decreased activity compared to wildtype": {
                    "after": "Unknown"
                },
                "Inactivating decreased kinase activity": {
                    "after": "Likely Loss-of-function"
                },
                "Inactivating Epigenetic silencing of gene expression": {
                    "after": "Loss-of-function"
                },
                "Inactivating Frameshift alteration associated with loss of expression.": {
                    "after": "Loss-of-function"
                },
                "Inactivating frameshift, nonsense, and splice site mutations": {
                    "after": "Loss-of-function"
                },
                "Inactivating Genetic or epigenetic deactivation of PRDM1": {
                    "after": "Loss-of-function"
                },
                "Inactivating Hypermethylation of CDKN2A promoter": {
                    "after": "Loss-of-function"
                },
                "Inactivating Inhibition of nuclear translocation": {
                    "after": "Loss-of-function"
                },
                "Inactivating kinase dead, autophosphorylation null": {
                    "after": "Loss-of-function"
                },
                "Inactivating kinase-dead and autophosphorylation-null": {
                    "after": "Loss-of-function"
                },
                "Inactivating kinase-dead and autophosphorylation-null mutants": {
                    "after": "Loss-of-function"
                },
                "Inactivating Likely deleterious": {
                    "after": "Loss-of-function"
                },
                "Inactivating Likely happloinsufficient": {
                    "after": "Loss-of-function"
                },
                "Inactivating Likely pathogenic": {
                    "after": "Likely Loss-of-function"
                },
                "Inactivating Loss of expression at gene or protein level.": {
                    "after": "Loss-of-function"
                },
                "Inactivating loss of functional beta2-microglobulin protein expression": {
                    "after": "Loss-of-function"
                },
                "Inactivating Loss of gene or protein expression.": {
                    "after": "Loss-of-function"
                },
                "Inactivating Loss of SMARCA4's crucial domains; ATPase domain and/or bromodomain": {
                    "after": "Loss-of-function"
                },
                "Inactivating Mutants either lose DNA-binding activity or act as a dominant negative.": {
                    "after": "Loss-of-function"
                },
                "Inactivating Mutations of KEAP1 C23Y impairs ubiquitinylation of NRF2": {
                    "after": "Loss-of-function"
                },
                "Inactivating Pathogenic": {
                    "after": "Loss-of-function"
                },
                "Inactivating Point mutation giving rise to dominant negative version of the receptor": {
                    "after": "Loss-of-function"
                },
                "Inactivating Premature STOP codon within the ATPase domain": {
                    "after": "Loss-of-function"
                },
                "Inactivating Probably happloinsufficient": {
                    "after": "Loss-of-function"
                },
                "Inactivating Results in ATPase-dead SMARCA4": {
                    "after": "Loss-of-function"
                },
                "Inactivating RUNX1-EVI1 acts as a dominant negative": {
                    "after": "Loss-of-function"
                },
                "Inactivating Silencing of KEAP1 expression via promoter methylation can activate NRF2": {
                    "after": "Loss-of-function"
                },
                "Inactivating The mutational effect is context dependent.": {
                    "after": "Loss-of-function"
                },
                "Inactivating These mutations impair KEAP1-dependent regulation of NRF2.": {
                    "after": "Loss-of-function"
                },
                "Inactivating This mutation results in altered splicing of SF3B1 target transcripts.": {
                    "after": "Loss-of-function"
                },
                "Inactivating Truncated protein product.": {
                    "after": "Loss-of-function"
                },
                "Inactivating Truncating mutations disrupting functional protein domains.": {
                    "after": "Loss-of-function"
                },
                "Inactivating Truncating mutations impair KEAP1-dependent regulation of NRF2.": {
                    "after": "Loss-of-function"
                },
                "Inactivating truncating mutations in the BCOR tumor suppressor gene lead to oncogenesis": {
                    "after": "Loss-of-function"
                },
                "Increased NRF2 activity activates expression of oxidative stress response machinery": {
                    "after": "Gain-of-function"
                },
                "increases Gli protein expression and downstream transcription targets": {
                    "after": "Gain-of-function"
                },
                "increases Gli protein expression and transcription of downstream targets": {
                    "after": "Gain-of-function"
                },
                "Interferes with binding to substrates, including Aura B": {
                    "after": "Loss-of-function"
                },
                "intermediate": {
                    "after": "Likely Loss-of-function"
                },
                "intermediate functional effect": {
                    "after": "Likely Neutral"
                },
                "It is unknown if this mutation is activating": {
                    "after": "Unknown"
                },
                "kinase inactivating but may activate pathway via dimerization with other RAF isoforms": {
                    "after": "Likely Gain-of-function"
                },
                "kinase inactivating but may activate via dimerization with other RAF isoforms": {
                    "after": "Likely Gain-of-function"
                },
                "Likely": {
                    "after": "Likely Gain-of-function"
                },
                "Likely Activating": {
                    "after": "Likely Gain-of-function"
                },
                "Likely activating": {
                    "after": "Likely Gain-of-function"
                },
                "likely activating": {
                    "after": "Likely Gain-of-function"
                },
                "Likely activating, but has not been tested": {
                    "after": "Likely Gain-of-function"
                },
                "Likely but it has not been characterized yet.": {
                    "after": "Likely Gain-of-function"
                },
                "Likely deleterious": {
                    "after": "Likely Loss-of-function"
                },
                "likely inactivating": {
                    "after": "Likely Loss-of-function"
                },
                "Likely inactivating": {
                    "after": "Likely Loss-of-function"
                },
                "Likely Inactivating": {
                    "after": "Likely Loss-of-function"
                },
                "Likely Inactivating, although not all missense mutations have been functionally tested": {
                    "after": "Likely Loss-of-function"
                },
                "Likely inactivating, probable slightly different effects depending on the exact location": {
                    "after": "Likely Loss-of-function"
                },
                "Likely inactivating, proposed dominant-negative": {
                    "after": "Likely Loss-of-function"
                },
                "Likely inactivating.": {
                    "after": "Likely Loss-of-function"
                },
                "Activating Likely": {
                    "after": "Likely Gain-of-function"
                },
                "Likely Neutral": {
                    "after": "Likely Neutral"
                },
                "Likely neutral": {
                    "after": "Likely Neutral"
                },
                "Further functional characterization is required" : {
                    "after": "Unknown"
                },
                "Not functionally characterized": {
                    "after": "Unknown"
                },
                "Confers resistance to AKT1 inhibitors": {
                    "after": "Unknown"  
                },
                "Likely not pathogenic": {
                    "after": "Likely Neutral"
                },
                "Likely pathogenic": {
                    "after": "Likely Loss-of-function"
                },
                "Likely.": {
                    "after": "Likely Gain-of-function"
                },
                "Loss-of-function, dominant negative, or Gain-of-function": {
                    "after": "Unknown"
                },
                "low functional effect": {
                    "after": "Likely Neutral"
                },
                "May cause altered DNA binding of the PBAF complex": {
                    "after": "Unknown"
                },
                "Missense": {
                    "after": "Unknown"
                },
                "MYC-nick is a proteolytically cleaved MYC protein lacking a DNA-binding domain.": {
                    "after": "Likely Loss-of-function"
                },
                "neutral": {
                    "after": "Neutral"
                },
                "Neutral": {
                    "after": "Neutral"
                },
                "neutral or uncertain": {
                    "after": "Likely Neutral"
                },
                "neutral/intermediate": {
                    "after": "Likely Neutral"
                },
                "no effect": {
                    "after": "Neutral"
                },
                "no effect on pathway activation": {
                    "after": "Neutral"
                },
                "no function": {
                    "after": "Likely Neutral"
                },
                "No functional effect": {
                    "after": "Neutral"
                },
                "no functional effect": {
                    "after": "Neutral"
                },
                "Not Activating": {
                    "after": "Neutral"
                },
                "Not been functionally characterized.": {
                    "after": "Unknown"
                },
                "not functionally tested": {
                    "after": "Unknown"
                },
                "not functionally validated": {
                    "after": "Unknown"
                },
                "Not pathogenic": {
                    "after": "Likely Neutral"
                },
                "Oncogenic activity has not been tested": {
                    "after": "Unknown"
                },
                "Other": {
                    "after": "Neutral"
                },
                "Over-represented due to homology with a pseudo-gene": {
                    "after": "Unknown"
                },
                "possible inactivating": {
                    "after": "Likely Loss-of-function"
                },
                "Possibly Activating": {
                    "after": "Likely Gain-of-function"
                },
                "Possibly inactivating": {
                    "after": "Likely Loss-of-function"
                },
                "Potentially activating": {
                    "after": "Likely Gain-of-function"
                },
                "Predicted to be activating": {
                    "after": "Likely Gain-of-function"
                },
                "Predicted to be inactivating": {
                    "after": "Likely Loss-of-function"
                },
                "Predicted to be inactivating based on mutation type.": {
                    "after": "Likely Loss-of-function"
                },
                "Probably activating": {
                    "after": "Unknown"
                },
                "Putative change of function, but not confirmed": {
                    "after": "Likely Gain-of-function"
                },
                "reported to disrupt the interaction between pVHL and HIFs": {
                    "after": "Likely Loss-of-function"
                },
                "similar activity to wild-type": {
                    "after": "Likely Neutral"
                },
                "Similar functional effect as expression of wildtype ERBB2": {
                    "after": "Likely Neutral"
                },
                "Similar kinase activity to wildtype BRAF but increased activation of c-RAF and downstream pERK": {
                    "after": "Likely Gain-of-function"
                },
                "similar to wildtype": {
                    "after": "Likely Neutral"
                },
                "Similar to wildtype": {
                    "after": "Unknown"
                },
                "Similar to wildtype BRAF activity": {
                    "after": "Likely Neutral"
                },
                "similar to wtERBB2": {
                    "after": "Likely Neutral"
                },
                "similar to wtHER3": {
                    "after": "Likely Neutral"
                },
                "slightly activating": {
                    "after": "Likely Gain-of-function"
                },
                "slightly to moderately activating": {
                    "after": "Likely Gain-of-function"
                },
                "Studies done in yeast; carcinoma cell lines indicate mutant is hypomorphic": {
                    "after": "Loss-of-function"
                },
                "The activity of the mutant was not tested": {
                    "after": "Unknown"
                },
                "The activity of the mutant was not tested, gatekeeper mutation": {
                    "after": "Likely Gain-of-function"
                },
                "The activity of this mutant has not been tested": {
                    "after": "Unknown"
                },
                "The activity of this mutant was not tested": {
                    "after": "Unknown"
                },
                "The activity of this mutation was not tested": {
                    "after": "Unknown"
                },
                "The biological effect of this mutation is unknown": {
                    "after": "Unknown"
                },
                "The biological effect of this mutation is unknown.": {
                    "after": "Unknown"
                },
                "The biological effect of this mutation requires further characterization.": {
                    "after": "Unknown"
                },
                "The biological function of this mutation is unknown": {
                    "after": "Unknown"
                },
                "The effect of this mutation is unknown.": {
                    "after": "Unknown"
                },
                "The effect of this polymorphism is unknown.": {
                    "after": "Unknown"
                },
                "The MED12 L1224F mutation has an unclear effect on MED12 activity": {
                    "after": "Unknown"
                },
                "The mutation allows MYOD to act in an activating fashion on MYC target genes": {
                    "after": "Likely Gain-of-function"
                },
                "This fusion has not been functionally tested": {
                    "after": "Unknown"
                },
                "This fusion is likely activating.": {
                    "after": "Likely Gain-of-function"
                },
                "This fusion is not well characterized.": {
                    "after": "Unknown"
                },
                "This mutant demonstrated activity similar to wildtype": {
                    "after": "Likely Neutral"
                },
                "This mutant may have different effects in different tumor types.": {
                    "after": "Likely Switch-of-function"
                },
                "This mutation confers drug resistance": {
                    "after": "Unknown"
                },
                "This mutation does not affect TSC1 function": {
                    "after": "Neutral"
                },
                "This mutation does not lead to transformation.": {
                    "after": "Neutral"
                },
                "This mutation has not been functionally characterized.": {
                    "after": "Unknown"
                },
                "This mutation is likely Activating": {
                    "after": "Likely Gain-of-function"
                },
                "This mutation is likely activating, although functional studies testing this have not been performed (PMID: 9227342).": {
                    "after": "Likely Gain-of-function"
                },
                "This mutation is likely activating.": {
                    "after": "Likely Gain-of-function"
                },
                "This mutation is likely inactivating.": {
                    "after": "Likely Loss-of-function"
                },
                "This mutation is possibly activating": {
                    "after": "Likely Gain-of-function"
                },
                "This mutation is possibly functionally silent.": {
                    "after": "Likely Switch-of-function"
                },
                "This mutation may be activating": {
                    "after": "Likely Gain-of-function"
                },
                "This mutation may confer resistance to erlotinib and gefitinib": {
                    "after": "Likely Switch-of-function"
                },
                "This mutation may have similar activity to wildtype EGFR": {
                    "after": "Likely Neutral"
                },
                "This mutation results in altered splicing of SF3B1 target transcripts.": {
                    "after": "Switch-of-function"
                },
                "This variant has not been functionally tested.": {
                    "after": "Unknown"
                },
                "This variant is hypothesized to adversely affect Chek2 substrate binding.": {
                    "after": "Unknown"
                },
                "uncertain": {
                    "after": "Unknown"
                },
                "Uncertain": {
                    "after": "Unknown"
                },
                "unclear": {
                    "after": "Unknown"
                },
                "Unclear if mutation is activating.": {
                    "after": "Unknown"
                },
                "Unknown": {
                    "after": "Unknown"
                },
                "unknown": {
                    "after": "Unknown"
                },
                "Unknown function": {
                    "after": "Unknown"
                },
                "unknown function, possible polymorphism": {
                    "after": "Unknown"
                },
                "unknown if oncogenic": {
                    "after": "Unknown"
                },
                "Unknown, predicted by structural studies to be inactivating": {
                    "after": "Unknown"
                },
                "unknown; likely inactivating": {
                    "after": "Unknown"
                },
                "Unknown.": {
                    "after": "Unknown"
                },
                "Unknown. This mutation is shown to be associated with microsatellite instability (MSI).": {
                    "after": "Likely Loss-of-function"
                },
                "Unlikely pathogenic": {
                    "after": "Likely Neutral"
                },
                "Very likely but it has not been characterized yet.": {
                    "after": "Likely Gain-of-function"
                },
                "weakly transforming": {
                    "after": "Likely Gain-of-function"
                }
            };
            
            if(mapping.hasOwnProperty(query)) {
                return mapping[query].after;
            } else {
                //TODO: dealwith none mapping mutation effect, return the original mutation effect for now
                console.log('\tDid not find mappings.');
                return query;
            }
        }
        
        function getGeneData(realtime, excludeObsolete) {
            /* jshint -W106 */
            var gene = {};
            var geneData = realtime;

            gene = combineData(gene, geneData, ['name', 'status', 'summary', 'background'], excludeObsolete);
            gene.mutations = [];
            gene.curators = [];

            geneData.curators.asArray().forEach(function (e) {
                var _curator = {};
                _curator = combineData(_curator, e, ['name', 'email']);
                gene.curators.push(_curator);
            });

            geneData.mutations.asArray().forEach(function (e) {
                if (!(excludeObsolete !== undefined && excludeObsolete && e.name_eStatus && e.name_eStatus.has('obsolete') && e.name_eStatus.get('obsolete') === 'true')){
                    var _mutation = {};
                    _mutation.tumors = [];
                    _mutation.effect = {};
                    _mutation = combineData(_mutation, e, ['name', 'summary'], excludeObsolete);
                    //This is a weird way to do, but due to time constraint, this has to be implemented in this way.
                    //I assigned shortSummary estatus for oncogenic and oncogenic estatus to mutation effect, 
                    // so there is no need to check excludeObsolete since I did outside of combinedata.
                    if (!(excludeObsolete !== undefined && excludeObsolete && e.shortSummary_eStatus && e.shortSummary_eStatus.has('obsolete') && e.shortSummary_eStatus.get('obsolete') === 'true')) {
                        _mutation = combineData(_mutation, e, ['shortSummary', 'oncogenic'], false);
                    }
                    if (!(excludeObsolete !== undefined && excludeObsolete && e.oncogenic_eStatus && e.oncogenic_eStatus.has('obsolete') && e.oncogenic_eStatus.get('obsolete') === 'true')) {
                        _mutation = combineData(_mutation, e, ['description', 'short'], excludeObsolete);
                        _mutation.effect = combineData(_mutation.effect, e.effect, ['value', 'addOn'], false);
    
                        if(_mutation.effect && _mutation.effect.value) {
                            var effect = _mutation.effect.value;

                            if(_mutation.effect.value.toLowerCase() === 'other') {
                                if(_mutation.effect.addOn) {
                                    effect = _mutation.effect.addOn;
                                }else {
                                    effect = 'Other';
                                }
                            }else {
                                if(_mutation.effect.addOn) {
                                    if(_mutation.effect.addOn.toLowerCase().indexOf(_mutation.effect.value.toLowerCase()) !== -1) {
                                        effect = _mutation.effect.addOn;
                                    }else {
                                        effect += ' ' + _mutation.effect.addOn;
                                    }
                                }
                            }

                            var message = '\t\t' + _mutation.name + '\tThe original mutation effect is ' + effect;
                            _mutation.effect.value = findMutationEffect(effect);
                            message += '\tconverting to: ' + _mutation.effect.value;
                            // console.log(message);
                            _mutation.effect.addOn = '';
                        }
                        
                        if (e.effect_comments) {
                            _mutation.effect_comments = getComments(e.effect_comments);
                        }
                    }
    
                    e.tumors.asArray().forEach(function (e1) {
                        if (!(excludeObsolete !== undefined && excludeObsolete && e1.name_eStatus && e1.name_eStatus.has('obsolete') && e1.name_eStatus.get('obsolete') === 'true')) {
                            var __tumor = {};
                            var selectedAttrs = ['name', 'summary'];

                            if (!(excludeObsolete !== undefined && excludeObsolete && e1.prevalence_eStatus && e1.prevalence_eStatus.has('obsolete') && e1.prevalence_eStatus.get('obsolete') === 'true')) {
                                selectedAttrs.push('prevalence', 'shortPrevalence');
                            }

                            if (!(excludeObsolete !== undefined && excludeObsolete && e1.progImp_eStatus && e1.progImp_eStatus.has('obsolete') && e1.progImp_eStatus.get('obsolete') === 'true')) {
                                selectedAttrs.push('progImp', 'shortProgImp');
                            }
                            __tumor = combineData(__tumor, e1, selectedAttrs, excludeObsolete);

                            // __tumor.cancerTypes =  __tumor.name.split(',').map(function(item) {
                            //     return {
                            //         cancerType: item.toString().trim()
                            //     };
                            // });
                            __tumor.cancerTypes = [];
                            __tumor.trials = [];
                            __tumor.TI = [];
                            __tumor.nccn = {};
                            __tumor.interactAlts = {};

                            if (!(excludeObsolete !== undefined && excludeObsolete && e1.nccn_eStatus && e1.nccn_eStatus.has('obsolete') && e1.nccn_eStatus.get('obsolete') === 'true')) {
                                __tumor.nccn = combineData(__tumor.nccn, e1.nccn, ['therapy', 'disease', 'version', 'pages', 'category', 'description', 'short'], excludeObsolete);
                            }

                            if (!(excludeObsolete !== undefined && excludeObsolete && e1.trials_eStatus && e1.trials_eStatus.has('obsolete') && e1.trials_eStatus.get('obsolete') === 'true')) {
                                e1.trials.asArray().forEach(function(trial) {
                                    __tumor.trials.push(trial);
                                });

                                if (e1.trials_comments) {
                                    __tumor.trials_comments = getComments(e1.trials_comments);
                                }
                            }
                            
                            e1.TI.asArray().forEach(function(e2) {
                                if (!(excludeObsolete !== undefined && excludeObsolete && e2.name_eStatus && e2.name_eStatus.has('obsolete') && e2.name_eStatus.get('obsolete') === 'true')) {
                                    var ti = {};

                                    ti = combineData(ti, e2, ['name', 'description', 'short'], excludeObsolete);
                                    ti.status = getString(e2.types.get('status'));
                                    ti.type = getString(e2.types.get('type'));
                                    ti.treatments = [];

                                    e2.treatments.asArray().forEach(function(e3) {
                                        var treatment = {};
                                        if (excludeObsolete !== undefined && excludeObsolete && e3.name_eStatus && e3.name_eStatus.has('obsolete') && e3.name_eStatus.get('obsolete') === 'true') {
                                            return;
                                        }
                                        treatment = combineData(treatment, e3, ['name', 'type', 'level', 'indication', 'description', 'short'], excludeObsolete);
                                        ti.treatments.push(treatment);
                                    });
                                    __tumor.TI.push(ti);
                                }
                            });

                            e1.cancerTypes.asArray().forEach(function(e2) {
                                var ct = {};
                                ct = combineData(ct, e2, ['cancerType', 'subtype', 'oncoTreeCode', 'operation'], excludeObsolete);
                                __tumor.cancerTypes.push(ct);
                            });

                            if (!(excludeObsolete !== undefined && excludeObsolete && e1.nccn_eStatus && e1.nccn_eStatus.has('obsolete') && e1.nccn_eStatus.get('obsolete') === 'true')) {
                                __tumor.nccn = combineData(__tumor.nccn, e1.nccn, ['therapy', 'disease', 'version', 'pages', 'category', 'description', 'short'], excludeObsolete);
                            }

                            __tumor.interactAlts = combineData(__tumor.interactAlts, e1.interactAlts, ['alterations', 'description'], excludeObsolete);
                            _mutation.tumors.push(__tumor);
                        }
                    });
    
                    gene.mutations.push(_mutation);
                }
            });
            return gene;
            /* jshint +W106 */
        }

        function createVUSItem(vusItem, vusModel, model) {
            var vus = model.create(OncoKB.VUSItem);

            vus.name.setText(vusItem.name);
            _.each(vusItem.time, function (time) {
                var timeStamp = model.create(OncoKB.TimeStampWithCurator);
                timeStamp.value.setText(time.value);
                timeStamp.by.name.setText(time.by.name);
                timeStamp.by.email.setText(time.by.email);
                vus.time.push(timeStamp);
            });
            vusModel.push(vus);
        }

        function copyFileData(folderId, fileId, fileTitle, docIndex) {
            var deferred = $q.defer();
            storage.requireAuth(true).then(function () {
                storage.createDocument(fileTitle, folderId).then(function (file) {
                    console.log('Created file ', fileTitle, docIndex + 1);
                    storage.getRealtimeDocument(fileId).then(function (realtime) {
                        if (realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        } else {
                            console.log('\t Copying');
                            var gene = realtime.getModel().getRoot().get('gene');
                            var vus = realtime.getModel().getRoot().get('vus');
                            if (gene) {
                                var geneData = getGeneData(gene);
                                var vusData = getVUSFullData(vus);
                                storage.getRealtimeDocument(file.id).then(function (newRealtime) {
                                    var model = createModel(newRealtime.getModel());
                                    var geneModel = model.getRoot().get('gene');
                                    var vusModel = model.getRoot().get('vus');

                                    model.beginCompoundOperation();
                                    for (var key in geneData) {
                                        if (geneModel[key]) {
                                            geneModel = setValue(model, geneModel, geneData[key], key);
                                        }
                                    }

                                    if (vusData) {
                                        _.each(vusData, function (vusItem) {
                                            createVUSItem(vusItem, vusModel, newRealtime.getModel());
                                        });
                                    }
                                    model.endCompoundOperation();
                                    console.log('\t Done.');
                                    $timeout(function () {
                                        deferred.resolve(file);
                                    }, 500, false);
                                });
                            } else {
                                console.log('\t\tNo gene model.');
                                $timeout(function () {
                                    deferred.resolve(file);
                                }, 500, false);
                            }
                        }
                    });
                });
            });
            return deferred.promise;
        }

        function setValue(rootModel, model, value, key) {
            if (angular.isString(value)) {
                if (model[key] && model[key].type) {
                    model[key].setText(value);
                } else {
                    console.log('Unknown key', key);
                }
            } else if (angular.isArray(value)) {
                value.forEach(function (e) {
                    var _datum;
                    switch (key) {
                        case 'curators':
                            _datum = rootModel.create('Curator');
                            break;
                        case 'mutations':
                            _datum = rootModel.create('Mutation');
                            break;
                        case 'tumors':
                            _datum = rootModel.create('Tumor');
                            break;
                        case 'TI':
                            _datum = rootModel.create('TI');
                            break;
                        case 'treatments':
                            _datum = rootModel.create('Treatment');
                            break;
                        case 'cancerTypes':
                            _datum = rootModel.create('CancerType');
                            break;
                        case 'trials':
                            _datum = e;
                            break;
                        default:
                            break;
                    }

                    if (key.indexOf('_comments') !== -1) {
                        _datum = rootModel.create('Comment');
                    }

                    if (key === 'TI') {
                        _datum.types.set('status', e.status);
                        _datum.types.set('type', e.type);
                        delete e.status;
                        delete e.type;
                    }
                    if (key !== 'trials') {
                        for (var _key in e) {
                            _datum = setValue(rootModel, _datum, e[_key], _key);
                        }
                    }
                    model[key].push(_datum);
                });
            } else if (angular.isObject(value)) {
                var _datum;
                switch (key) {
                    case 'nccn':
                        _datum = rootModel.create('NCCN');
                        break;
                    case 'effect':
                        _datum = rootModel.create('ME');
                        break;
                    case 'interactAlts':
                        _datum = rootModel.create('InteractAlts');
                        break;
                }

                if (key.indexOf('_eStatus') !== -1) {
                    for (var _key in value) {
                        model[key].set(_key, value[_key]);
                    }
                } else if (key.indexOf('_timeStamp') !== -1) {
                    for (var _key in value) {
                        _datum = rootModel.create('TimeStamp');
                        _datum.value.setText(value[_key].value || '');
                        _datum.by.setText(value[_key].by || '');
                        model[key].set(_key, _datum);
                    }
                } else {
                    for (var _key in value) {
                        _datum = setValue(rootModel, _datum, value[_key], _key);
                    }
                    model[key] = _datum;
                }
            } else {
                console.log('Error value type.');
            }
            return model;
        }

        function createModel(model) {
            model = createGeneModel(model);
            model = createVUSModel(model);
            return model;
        }

        function createGeneModel(model) {
            if (!model.getRoot().get('gene')) {
                var gene = model.create('Gene');
                model.getRoot().set('gene', gene);
            }
            return model;
        }

        function createVUSModel(model) {
            if (!model.getRoot().get('vus')) {
                var vus = model.createList();
                model.getRoot().set('vus', vus);
            }
            return model;
        }

        function combineData(object, model, keys, excludeObsolete) {
            keys.forEach(function(e) {
                if (!(excludeObsolete !== undefined && excludeObsolete && model[e + '_eStatus'] && model[e + '_eStatus'].has('obsolete') && model[e + '_eStatus'].get('obsolete') === 'true')) {
                    if (model[e]) {
                        object[e] = getString(model[e].getText());
                        if (model[e + '_comments']) {
                            object[e + '_comments'] = getComments(model[e + '_comments']);
                        }
                        if (model[e + '_eStatus']) {
                            object[e + '_eStatus'] = getEvidenceStatus(model[e + '_eStatus']);
                        }
                        if (model[e + '_timeStamp']) {
                            object[e + '_timeStamp'] = getTimeStamp(model[e + '_timeStamp']);
                        }
                    }
                }
            });
            return object;
        }

        function getString(string) {
            var tmp = window.document.createElement('DIV');
            tmp.innerHTML = string;
            var _string = tmp.textContent || tmp.innerText || S(string).stripTags().s;
            string = S(_string).collapseWhitespace().s;
            return string;
        }

        function getComments(model) {
            var comments = [];
            var commentKeys = Object.keys(OncoKB.curateInfo.Comment);
            var comment = {};

            commentKeys.forEach(function (e) {
                comment[e] = '';
            });

            model.asArray().forEach(function (e) {
                var _comment = angular.copy(comment);
                for (var key in _comment) {
                    if (e[key]) {
                        _comment[key] = e[key].getText();
                    }
                }
                comments.push(_comment);
            });
            return comments;
        }

        function getEvidenceStatus(model) {
            var keys = model.keys();
            var status = {};

            keys.forEach(function (e) {
                status[e] = model.get(e);
            });
            return status;
        }

        function getTimeStamp(model) {
            var keys = model.keys();
            var status = {};

            keys.forEach(function (e) {
                status[e] = {
                    value: model.get(e).value.text,
                    by: model.get(e).by.text
                };
            });
            return status;
        }

        return {
            backup: backup,
            migrate: migrate,
            getGeneData: getGeneData,
            getVUSData: getVUSData,
            getVUSFullData: getVUSFullData
        };
    });
