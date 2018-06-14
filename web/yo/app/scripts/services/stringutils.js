'use strict';

/**
 * @ngdoc service
 * @name oncokbApp.stringUtils
 * @description
 * # stringUtils
 * Factory in the oncokbApp.
 */
angular.module('oncokbApp')
    .factory('stringUtils', function(_, OncoKB, S, UUIDjs) {
        /* eslint camelcase: ["error", {properties: "never"}]*/
        function findMutationEffect(query) {
            var mapping = {
                'Loss-of-function, dominant negative, or gain-of-function': {
                    after: 'Likely Loss-of-function'
                },
                'Activating Likely, mild effect': {
                    after: 'Likely Gain-of-function'
                },
                'Activating moderate': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Probably mild effect': {
                    after: 'Likely Gain-of-function'
                },
                'Activating LIkely': {
                    after: 'Likely Neutral'
                },
                'likely passenger mutation': {
                    after: 'Likely Neutral'
                },
                'passenger mutation': {
                    after: 'Neutral'
                },
                'possibly activating': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Activation potential appears to be much weaker than E545K variant.': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Weak activation': {
                    after: 'Likely Gain-of-function'
                },
                'Functional studies needed.': {
                    after: 'Unknown'
                },
                'Likely to be activating': {
                    after: 'Likely Gain-of-function'
                },
                'Needs functional characterization.': {
                    after: 'Unknown'
                },
                'Possibly activating, although the evidence is not quite conclusive.': {
                    after: 'Unknown'
                },
                'Weak activity': {
                    after: 'Likely Gain-of-function'
                },
                'Weakly activating': {
                    after: 'Likely Gain-of-function'
                },
                'Gain-of-function': {
                    after: 'Gain-of-function'
                },
                'Likely Gain-of-function': {
                    after: 'Likely Gain-of-function'
                },
                'Loss-of-functionn': {
                    after: 'Loss-of-function'
                },
                'Loss-of-function': {
                    after: 'Loss-of-function'
                },
                'Likely Loss-of-function': {
                    after: 'Likely Loss-of-function'
                },
                'Switch-of-function': {
                    after: 'Switch-of-function'
                },
                'Likely Switch-of-function': {
                    after: 'Likely Switch-of-function'
                },
                'Neutral': {
                    after: 'Neutral'
                },
                'Likely Neutral': {
                    after: 'Likely Neutral'
                },
                'Conflicting reports': {
                    after: 'Unknown'
                },
                'Abnormal splicing': {
                    after: 'Likely Loss-of-function'
                },
                'acquired resistance to dasatinib': {
                    after: 'Likely Gain-of-function'
                },
                'Activating': {
                    after: 'Gain-of-function'
                },
                'Activating Activates PI3K pathway': {
                    after: 'Gain-of-function'
                },
                'Activating Activates transcription through fusion protein gain of function': {
                    after: 'Gain-of-function'
                },
                'Activating Activation of partner ERBB family members': {
                    after: 'Gain-of-function'
                },
                'Activating AKT3 through amplification is seen in multiple tumors such as ovarian, breast, melanoma and gliomas.': {
                    after: 'Gain-of-function'
                },
                'Activating Amplification of CCND2 is expected to result in aberrant activation of CDK4/6 to promote cell proliferation.': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Amplification of CCND3 is expected to result in increased tumor cell proliferation.': {
                    after: 'Likely Gain-of-function'
                },
                'Activating BCOR-CCNB3 fusion activates Wnt and Hedgehog signaling pathways': {
                    after: 'Likely Switch-of-function'
                },
                'Activating BCOR-RARA fusion leads to transcriptional activation': {
                    after: 'Likely Switch-of-function'
                },
                'Activating Confers resistance to crizotinib': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Confers resistance to gefitinib, erlotinib': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Downstream activation of SRC activity, caused by upstream mutation within signaling cascade': {
                    after: 'Gain-of-function'
                },
                'Activating Expression and phosphorylation of JUN is activated during amplification.': {
                    after: 'Gain-of-function'
                },
                'Activating forms heterodimers with ERBB family members': {
                    after: 'Gain-of-function'
                },
                'Activating Further functional characterization is required.': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Fusion of PVT1 to MYC likely increases the expression of the MYC gene': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Fusion transcript leading to overexpression': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Gain-of-function mutation of GNAS alternate splice form': {
                    after: 'Gain-of-function'
                },
                'Activating gate keeper mutation': {
                    after: 'Likely Gain-of-function'
                },
                'Activating In combination with the JAK2 V617F mutation.': {
                    after: 'Likely Gain-of-function'
                },
                'activating in vitro': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Increased expression is predicted to lead to increased function.': {
                    after: 'Likely Gain-of-function'
                },
                'Activating increased formation of ERBB2 dimers': {
                    after: 'Gain-of-function'
                },
                'Activating increased formation of mutant ERBB2 dimers': {
                    after: 'Gain-of-function'
                },
                'Activating increased kinase activity': {
                    after: 'Gain-of-function'
                },
                'Activating increases mutant ERBB2-ERBB3 heterodimer formation': {
                    after: 'Gain-of-function'
                },
                'Activating increases mutant ERBB2-ERBB3 heterodimers': {
                    after: 'Gain-of-function'
                },
                'Activating It shows higher lipid kinase activity.': {
                    after: 'Gain-of-function'
                },
                'Activating MED12 exon 2 mutations dysregulate extracellular matrix organization and may elicit aberrant estrogen signaling': {
                    after: 'Likely Gain-of-function'
                },
                'Activating MED12 knockdown activates TGF-beta signaling': {
                    after: 'Likely Gain-of-function'
                },
                'Activating mild effect': {
                    after: 'Likely Gain-of-function'
                },
                'Activating modest activation': {
                    after: 'Likely Gain-of-function'
                },
                'Activating modest transforming ability': {
                    after: 'Likely Gain-of-function'
                },
                'Activating NUSAP activation in response to RB1 gene loss': {
                    after: 'Likely Loss-of-function'
                },
                'Activating Often found in context of KRAS mutations.': {
                    after: 'Gain-of-function'
                },
                'Activating or inactivating, probably tissue- and context-dependent': {
                    after: 'Unknown'
                },
                'Activating Overexpression, potentially leading to increased kinase activity even without activation.': {
                    after: 'Gain-of-function'
                },
                'Activating predicted to result in higher expression of AURKB': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Questionable sensitivity to imatinib': {
                    after: 'Likely Gain-of-function'
                },
                'Activating Requires a second RAS alteration': {
                    after: 'Likely Gain-of-function'
                },
                'Activating results in nuclear localization of cyclin D1 protein': {
                    after: 'Gain-of-function'
                },
                'Activating Sensitive to EGFR TKIs erlotinib or gefitinib': {
                    after: 'Gain-of-function'
                },
                'Activating the ZC3H7B-BCOR chimeric gene alters epigenetic machinery leading to oncogenesis': {
                    after: 'Likely Switch-of-function'
                },
                'Activating This mutation is expected to stabilize a truncated Cyclin D3 protein, promoting cell proliferation.': {
                    after: 'Likely Gain-of-function'
                },
                'Activating This mutation is expected to stabilize Cyclin D3 protein levels, promoting cell proliferation.': {
                    after: 'Likely Gain-of-function'
                },
                'Activating This mutation mediates tumor resistance to first generation EGFR TKIs erlotinib and gefitinib': {
                    after: 'Gain-of-function'
                },
                'Activating This mutation stabilizes Cyclin D3 protein levels, promoting cell proliferation.': {
                    after: 'Gain-of-function'
                },
                'Activating This seems to be a very rare alteration.': {
                    after: 'Gain-of-function'
                },
                'Activating Weakly': {
                    after: 'Likely Gain-of-function'
                },
                'activity similar to wildtype': {
                    after: 'Neutral'
                },
                'Affects the proofreading function of POLE; however, there is no known effect on the protein\'s polymerase function.': {
                    after: 'Unknown'
                },
                'C121S is a MEK1 and BRAF inhibitor resistance allele': {
                    after: 'Likely Gain-of-function'
                },
                'Change of function; alters the protein\'s DNA binfding abilities': {
                    after: 'Switch-of-function'
                },
                'Clear evidence is lacking.': {
                    after: 'Unknown'
                },
                'Computer modeling suggests that this mutation may prevent RHOA interaction with GEF proteins, in this way possibly decreasing basal RHOA activation': {
                    after: 'Unknown'
                },
                'Confers resistance to crizotinib': {
                    after: 'Likely Gain-of-function'
                },
                'conflicting data': {
                    after: 'Unknown'
                },
                'Conflicting data exists for the effect of this mutation (PMID: 23822953, 22328973)': {
                    after: 'Unknown'
                },
                'Conflicting evidence': {
                    after: 'Unknown'
                },
                'Crizotinib': {
                    after: 'Likely Gain-of-function'
                },
                'crizotinib and alectinib resistance': {
                    after: 'Likely Gain-of-function'
                },
                'Crizotinib and ceritinib resistance': {
                    after: 'Likely Gain-of-function'
                },
                'Data suggests this mutation is not oncogenic.': {
                    after: 'Likely Neutral'
                },
                'deleterious or neutral': {
                    after: 'Loss-of-function'
                },
                'Drug resistance': {
                    after: 'Unknown'
                },
                'Evidence for inactivation': {
                    after: 'Unknown'
                },
                'Evidence for pathogenicity': {
                    after: 'Likely Gain-of-function'
                },
                'Functional role unclear': {
                    after: 'Unknown'
                },
                'Functional significance of mutation unknown': {
                    after: 'Unknown'
                },
                'Functional significance unknown': {
                    after: 'Unknown'
                },
                'Further functional characterization is required.': {
                    after: 'Likely Gain-of-function'
                },
                'Gatekeeper mutation in ATP binding pocket': {
                    after: 'Likely Gain-of-function'
                },
                'Germline susceptibility variant': {
                    after: 'Unknown'
                },
                'Hypothesized from structural studies to interfere with regulatory subunit p85 interaction, leading to p110alpha hyperactivation': {
                    after: 'Likely Gain-of-function'
                },
                'Hypothesized to be activating but no functional data available': {
                    after: 'Unknown'
                },
                'Imatinib resistance': {
                    after: 'Likely Gain-of-function'
                },
                'Inactivating': {
                    after: 'Loss-of-function'
                },
                'Inactivating Acts as a tumor suppressor gene': {
                    after: 'Likely Loss-of-function'
                },
                'Inactivating As suggested by predictions.': {
                    after: 'Likely Loss-of-function'
                },
                'Inactivating As suggested by studies of other missense mutations in the same locus.': {
                    after: 'Likely Loss-of-function'
                },
                'Inactivating decreased activity compared to wildtype': {
                    after: 'Unknown'
                },
                'Inactivating decreased kinase activity': {
                    after: 'Likely Loss-of-function'
                },
                'Inactivating Epigenetic silencing of gene expression': {
                    after: 'Loss-of-function'
                },
                'Inactivating Frameshift alteration associated with loss of expression.': {
                    after: 'Loss-of-function'
                },
                'Inactivating frameshift, nonsense, and splice site mutations': {
                    after: 'Loss-of-function'
                },
                'Inactivating Genetic or epigenetic deactivation of PRDM1': {
                    after: 'Loss-of-function'
                },
                'Inactivating Hypermethylation of CDKN2A promoter': {
                    after: 'Loss-of-function'
                },
                'Inactivating Inhibition of nuclear translocation': {
                    after: 'Loss-of-function'
                },
                'Inactivating kinase dead, autophosphorylation null': {
                    after: 'Loss-of-function'
                },
                'Inactivating kinase-dead and autophosphorylation-null': {
                    after: 'Loss-of-function'
                },
                'Inactivating kinase-dead and autophosphorylation-null mutants': {
                    after: 'Loss-of-function'
                },
                'Inactivating Likely deleterious': {
                    after: 'Loss-of-function'
                },
                'Inactivating Likely happloinsufficient': {
                    after: 'Loss-of-function'
                },
                'Inactivating Likely pathogenic': {
                    after: 'Likely Loss-of-function'
                },
                'Inactivating Loss of expression at gene or protein level.': {
                    after: 'Loss-of-function'
                },
                'Inactivating loss of functional beta2-microglobulin protein expression': {
                    after: 'Loss-of-function'
                },
                'Inactivating Loss of gene or protein expression.': {
                    after: 'Loss-of-function'
                },
                'Inactivating Loss of SMARCA4\'s crucial domains; ATPase domain and/or bromodomain': {
                    after: 'Loss-of-function'
                },
                'Inactivating Mutants either lose DNA-binding activity or act as a dominant negative.': {
                    after: 'Loss-of-function'
                },
                'Inactivating Mutations of KEAP1 C23Y impairs ubiquitinylation of NRF2': {
                    after: 'Loss-of-function'
                },
                'Inactivating Pathogenic': {
                    after: 'Loss-of-function'
                },
                'Inactivating Point mutation giving rise to dominant negative version of the receptor': {
                    after: 'Loss-of-function'
                },
                'Inactivating Premature STOP codon within the ATPase domain': {
                    after: 'Loss-of-function'
                },
                'Inactivating Probably happloinsufficient': {
                    after: 'Loss-of-function'
                },
                'Inactivating Results in ATPase-dead SMARCA4': {
                    after: 'Loss-of-function'
                },
                'Inactivating RUNX1-EVI1 acts as a dominant negative': {
                    after: 'Loss-of-function'
                },
                'Inactivating Silencing of KEAP1 expression via promoter methylation can activate NRF2': {
                    after: 'Loss-of-function'
                },
                'Inactivating The mutational effect is context dependent.': {
                    after: 'Loss-of-function'
                },
                'Inactivating These mutations impair KEAP1-dependent regulation of NRF2.': {
                    after: 'Loss-of-function'
                },
                'Inactivating This mutation results in altered splicing of SF3B1 target transcripts.': {
                    after: 'Loss-of-function'
                },
                'Inactivating Truncated protein product.': {
                    after: 'Loss-of-function'
                },
                'Inactivating Truncating mutations disrupting functional protein domains.': {
                    after: 'Loss-of-function'
                },
                'Inactivating Truncating mutations impair KEAP1-dependent regulation of NRF2.': {
                    after: 'Loss-of-function'
                },
                'Inactivating truncating mutations in the BCOR tumor suppressor gene lead to oncogenesis': {
                    after: 'Loss-of-function'
                },
                'Increased NRF2 activity activates expression of oxidative stress response machinery': {
                    after: 'Gain-of-function'
                },
                'increases Gli protein expression and downstream transcription targets': {
                    after: 'Gain-of-function'
                },
                'increases Gli protein expression and transcription of downstream targets': {
                    after: 'Gain-of-function'
                },
                'Interferes with binding to substrates, including Aura B': {
                    after: 'Loss-of-function'
                },
                'intermediate': {
                    after: 'Likely Loss-of-function'
                },
                'intermediate functional effect': {
                    after: 'Likely Neutral'
                },
                'It is unknown if this mutation is activating': {
                    after: 'Unknown'
                },
                'kinase inactivating but may activate pathway via dimerization with other RAF isoforms': {
                    after: 'Likely Gain-of-function'
                },
                'kinase inactivating but may activate via dimerization with other RAF isoforms': {
                    after: 'Likely Gain-of-function'
                },
                'Likely': {
                    after: 'Likely Gain-of-function'
                },
                'Likely Activating': {
                    after: 'Likely Gain-of-function'
                },
                'Likely activating': {
                    after: 'Likely Gain-of-function'
                },
                'likely activating': {
                    after: 'Likely Gain-of-function'
                },
                'Likely activating, but has not been tested': {
                    after: 'Likely Gain-of-function'
                },
                'Likely but it has not been characterized yet.': {
                    after: 'Likely Gain-of-function'
                },
                'Likely deleterious': {
                    after: 'Likely Loss-of-function'
                },
                'likely inactivating': {
                    after: 'Likely Loss-of-function'
                },
                'Likely inactivating': {
                    after: 'Likely Loss-of-function'
                },
                'Likely Inactivating': {
                    after: 'Likely Loss-of-function'
                },
                'Likely Inactivating, although not all missense mutations have been functionally tested': {
                    after: 'Likely Loss-of-function'
                },
                'Likely inactivating, probable slightly different effects depending on the exact location': {
                    after: 'Likely Loss-of-function'
                },
                'Likely inactivating, proposed dominant-negative': {
                    after: 'Likely Loss-of-function'
                },
                'Likely inactivating.': {
                    after: 'Likely Loss-of-function'
                },
                'Activating Likely': {
                    after: 'Likely Gain-of-function'
                },
                'Likely neutral': {
                    after: 'Likely Neutral'
                },
                'Further functional characterization is required': {
                    after: 'Unknown'
                },
                'Not functionally characterized': {
                    after: 'Unknown'
                },
                'Confers resistance to AKT1 inhibitors': {
                    after: 'Unknown'
                },
                'Likely not pathogenic': {
                    after: 'Likely Neutral'
                },
                'Likely pathogenic': {
                    after: 'Likely Loss-of-function'
                },
                'Likely.': {
                    after: 'Likely Gain-of-function'
                },
                'Loss-of-function, dominant negative, or Gain-of-function': {
                    after: 'Unknown'
                },
                'low functional effect': {
                    after: 'Likely Neutral'
                },
                'May cause altered DNA binding of the PBAF complex': {
                    after: 'Unknown'
                },
                'Missense': {
                    after: 'Unknown'
                },
                'MYC-nick is a proteolytically cleaved MYC protein lacking a DNA-binding domain.': {
                    after: 'Likely Loss-of-function'
                },
                'neutral': {
                    after: 'Neutral'
                },
                'neutral or uncertain': {
                    after: 'Likely Neutral'
                },
                'neutral/intermediate': {
                    after: 'Likely Neutral'
                },
                'no effect': {
                    after: 'Neutral'
                },
                'no effect on pathway activation': {
                    after: 'Neutral'
                },
                'no function': {
                    after: 'Likely Neutral'
                },
                'No functional effect': {
                    after: 'Neutral'
                },
                'no functional effect': {
                    after: 'Neutral'
                },
                'Not Activating': {
                    after: 'Neutral'
                },
                'Not been functionally characterized.': {
                    after: 'Unknown'
                },
                'not functionally tested': {
                    after: 'Unknown'
                },
                'not functionally validated': {
                    after: 'Unknown'
                },
                'Not pathogenic': {
                    after: 'Likely Neutral'
                },
                'Oncogenic activity has not been tested': {
                    after: 'Unknown'
                },
                'Other': {
                    after: 'Neutral'
                },
                'Over-represented due to homology with a pseudo-gene': {
                    after: 'Unknown'
                },
                'possible inactivating': {
                    after: 'Likely Loss-of-function'
                },
                'Possibly Activating': {
                    after: 'Likely Gain-of-function'
                },
                'Possibly inactivating': {
                    after: 'Likely Loss-of-function'
                },
                'Potentially activating': {
                    after: 'Likely Gain-of-function'
                },
                'Predicted to be activating': {
                    after: 'Likely Gain-of-function'
                },
                'Predicted to be inactivating': {
                    after: 'Likely Loss-of-function'
                },
                'Predicted to be inactivating based on mutation type.': {
                    after: 'Likely Loss-of-function'
                },
                'Probably activating': {
                    after: 'Unknown'
                },
                'Putative change of function, but not confirmed': {
                    after: 'Likely Gain-of-function'
                },
                'reported to disrupt the interaction between pVHL and HIFs': {
                    after: 'Likely Loss-of-function'
                },
                'similar activity to wild-type': {
                    after: 'Likely Neutral'
                },
                'Similar functional effect as expression of wildtype ERBB2': {
                    after: 'Likely Neutral'
                },
                'Similar kinase activity to wildtype BRAF but increased activation of c-RAF and downstream pERK': {
                    after: 'Likely Gain-of-function'
                },
                'similar to wildtype': {
                    after: 'Likely Neutral'
                },
                'Similar to wildtype': {
                    after: 'Unknown'
                },
                'Similar to wildtype BRAF activity': {
                    after: 'Likely Neutral'
                },
                'similar to wtERBB2': {
                    after: 'Likely Neutral'
                },
                'similar to wtHER3': {
                    after: 'Likely Neutral'
                },
                'slightly activating': {
                    after: 'Likely Gain-of-function'
                },
                'slightly to moderately activating': {
                    after: 'Likely Gain-of-function'
                },
                'Studies done in yeast; carcinoma cell lines indicate mutant is hypomorphic': {
                    after: 'Loss-of-function'
                },
                'The activity of the mutant was not tested': {
                    after: 'Unknown'
                },
                'The activity of the mutant was not tested, gatekeeper mutation': {
                    after: 'Likely Gain-of-function'
                },
                'The activity of this mutant has not been tested': {
                    after: 'Unknown'
                },
                'The activity of this mutant was not tested': {
                    after: 'Unknown'
                },
                'The activity of this mutation was not tested': {
                    after: 'Unknown'
                },
                'The biological effect of this mutation is unknown': {
                    after: 'Unknown'
                },
                'The biological effect of this mutation is unknown.': {
                    after: 'Unknown'
                },
                'The biological effect of this mutation requires further characterization.': {
                    after: 'Unknown'
                },
                'The biological function of this mutation is unknown': {
                    after: 'Unknown'
                },
                'The effect of this mutation is unknown.': {
                    after: 'Unknown'
                },
                'The effect of this polymorphism is unknown.': {
                    after: 'Unknown'
                },
                'The MED12 L1224F mutation has an unclear effect on MED12 activity': {
                    after: 'Unknown'
                },
                'The mutation allows MYOD to act in an activating fashion on MYC target genes': {
                    after: 'Likely Gain-of-function'
                },
                'This fusion has not been functionally tested': {
                    after: 'Unknown'
                },
                'This fusion is likely activating.': {
                    after: 'Likely Gain-of-function'
                },
                'This fusion is not well characterized.': {
                    after: 'Unknown'
                },
                'This mutant demonstrated activity similar to wildtype': {
                    after: 'Likely Neutral'
                },
                'This mutant may have different effects in different tumor types.': {
                    after: 'Likely Switch-of-function'
                },
                'This mutation confers drug resistance': {
                    after: 'Unknown'
                },
                'This mutation does not affect TSC1 function': {
                    after: 'Neutral'
                },
                'This mutation does not lead to transformation.': {
                    after: 'Neutral'
                },
                'This mutation has not been functionally characterized.': {
                    after: 'Unknown'
                },
                'This mutation is likely Activating': {
                    after: 'Likely Gain-of-function'
                },
                'This mutation is likely activating, although functional studies testing this have not been performed (PMID: 9227342).': {
                    after: 'Likely Gain-of-function'
                },
                'This mutation is likely activating.': {
                    after: 'Likely Gain-of-function'
                },
                'This mutation is likely inactivating.': {
                    after: 'Likely Loss-of-function'
                },
                'This mutation is possibly activating': {
                    after: 'Likely Gain-of-function'
                },
                'This mutation is possibly functionally silent.': {
                    after: 'Likely Switch-of-function'
                },
                'This mutation may be activating': {
                    after: 'Likely Gain-of-function'
                },
                'This mutation may confer resistance to erlotinib and gefitinib': {
                    after: 'Likely Switch-of-function'
                },
                'This mutation may have similar activity to wildtype EGFR': {
                    after: 'Likely Neutral'
                },
                'This mutation results in altered splicing of SF3B1 target transcripts.': {
                    after: 'Switch-of-function'
                },
                'This variant has not been functionally tested.': {
                    after: 'Unknown'
                },
                'This variant is hypothesized to adversely affect Chek2 substrate binding.': {
                    after: 'Unknown'
                },
                'uncertain': {
                    after: 'Unknown'
                },
                'Uncertain': {
                    after: 'Unknown'
                },
                'unclear': {
                    after: 'Unknown'
                },
                'Unclear if mutation is activating.': {
                    after: 'Unknown'
                },
                'Unknown': {
                    after: 'Unknown'
                },
                'unknown': {
                    after: 'Unknown'
                },
                'Unknown function': {
                    after: 'Unknown'
                },
                'unknown function, possible polymorphism': {
                    after: 'Unknown'
                },
                'unknown if oncogenic': {
                    after: 'Unknown'
                },
                'Unknown, predicted by structural studies to be inactivating': {
                    after: 'Unknown'
                },
                'unknown; likely inactivating': {
                    after: 'Unknown'
                },
                'Unknown.': {
                    after: 'Unknown'
                },
                'Unknown. This mutation is shown to be associated with microsatellite instability (MSI).': {
                    after: 'Likely Loss-of-function'
                },
                'Unlikely pathogenic': {
                    after: 'Likely Neutral'
                },
                'Very likely but it has not been characterized yet.': {
                    after: 'Likely Gain-of-function'
                },
                'weakly transforming': {
                    after: 'Likely Gain-of-function'
                }
            };

            if (mapping.hasOwnProperty(query)) {
                return mapping[query].after;
            }
            // TODO: deal with none mapping mutation effect, return the original mutation effect for now
            console.log('\tDid not find mappings: ', query);
            return query;
        }

        function getVUSData(vus, excludeComments) {
            var vusData = vus;
            excludeComments = _.isBoolean(excludeComments) ? excludeComments : false;
            _.each(vusData, function(vusItem) {
                if (excludeComments) {
                    delete vusItem.name_comments;
                }
            });
            return vusData;
        }

        // get history data
        function getHistoryData(history) {
            var result = {};
            if (history && _.isArray(history.keys())) {
                _.each(history.keys(), function(key) {
                    if (['api'].indexOf(key) !== -1) {
                        result[key] = Array.from(history.get(key));
                    } else {
                        result[key] = history.get(key);
                    }
                });
            }
            return result;
        }
        function processData(data, keys, excludeComments, onlyReviewedContent) {
            _.each(keys, function(key) {
                if (excludeComments) {
                    delete data[key+'_comments'];
                }
                if (onlyReviewedContent && data[key+'_review'] && !_.isUndefined(data[key+'_review'].lastReviewed)) {
                    data[key] = data[key+'_review'].lastReviewed;
                }
                delete data[key+'_review'];
            });
        }
        function shouldExclude(onlyReviewedContent, reviewObj) {
            return reviewObj && (onlyReviewedContent && reviewObj.added == true || !onlyReviewedContent && reviewObj.removed == true);
        }
        function getGeneData(geneData, excludeComments, onlyReviewedContent) {
            var gene = _.clone(geneData);
            excludeComments = _.isBoolean(excludeComments) ? excludeComments : false;
            onlyReviewedContent = _.isBoolean(onlyReviewedContent) ? onlyReviewedContent : false;
            processData(gene, ['summary', 'background'], excludeComments, onlyReviewedContent);
            processData(gene.type, ['tsg', 'ocg'], excludeComments, onlyReviewedContent);
            _.each(gene.mutations, function(mutation, mutationIndex) {
                if (shouldExclude(onlyReviewedContent, mutation.name_review)) {
                    gene.mutations.splice(mutationIndex, 1);
                    return true;
                }
                processData(mutation, ['name'], excludeComments, onlyReviewedContent);
                processData(mutation.mutation_effect, ['oncogenic', 'effect', 'description'], excludeComments, onlyReviewedContent);
                _.each(mutation.tumors, function(tumor, tumorIndex) {
                    if (shouldExclude(onlyReviewedContent, tumor.cancerTypes_review)) {
                        mutation.tumors.splice(tumorIndex, 1);
                        return true;
                    }
                    // process tumor cancerTypes
                    processData(tumor, ['summary'], excludeComments, onlyReviewedContent);
                    processData(tumor.diagnostic, ['level', 'description'], excludeComments, onlyReviewedContent);
                    processData(tumor.prognostic, ['level', 'description'], excludeComments, onlyReviewedContent);
                    _.each(tumor.TIs, function(ti) {
                        processData(ti, ['description'], excludeComments, onlyReviewedContent);
                        _.each(ti.treatments, function(treatment, treatmentIndex) {
                            if (shouldExclude(onlyReviewedContent, treatment.name_review)) {
                                ti.treatments.splice(treatmentIndex, 1);
                                return true;
                            }
                            processData(treatment, ['name', 'level', 'propagation', 'indication', 'description'], excludeComments, onlyReviewedContent);
                        });
                    });
                });
            });
            return gene;
        }

        function isUndefinedOrEmpty(str) {
            if (_.isUndefined(str)) {
                return true;
            }
            return str.toString().trim() === '';
        }

        function stringObject(object) {
            var result = [];
            for (var key in object) {
                if (object.hasOwnProperty(key)) {
                    result.push(key + ': ' + object[key]);
                }
            }
            return result.join('\t');
        }

        function mostRecentItem(reviewObjs, include) {
            var mostRecent = -1;
            for (var i = 0; i < reviewObjs.length; i++) {
                if (!include) {
                    // This is designed to handle the reviewObj with systematically set updatetime
                    // when 'include' equals true, it will use all reviewObj in the list
                    // otherwise, we will only use the reviewObj with updatedBy info.
                    if (!reviewObjs[i] || !reviewObjs[i].updatedBy) continue;
                }
                var currentItemTime;
                if (reviewObjs[i] && reviewObjs[i].updateTime) {
                    currentItemTime = new Date(reviewObjs[i].updateTime);
                }
                // we only continue to check if current item time is valid
                if (currentItemTime instanceof Date && !isNaN(currentItemTime.getTime())) {
                    if (mostRecent < 0) {
                        mostRecent = i;
                    } else {
                        // reset mostRect time when current item time is closer
                        var mostRecentTime = new Date(reviewObjs[mostRecent].updateTime);
                        if(mostRecentTime < currentItemTime) {
                            mostRecent = i;
                        }
                    }
                }
            }
            if (mostRecent < 0) {
                return 0;
            }
            return mostRecent;
        }

        // Public API here
        return {
            trimMutationName: function(mutation) {
                if (typeof mutation === 'string') {
                    if (mutation.indexOf('p.') === 0) {
                        mutation = mutation.substring(2);
                    }
                }
                return mutation;
            },
            findMutationEffect: findMutationEffect,
            getCurrentTimeForEmailCase: function() {
                var date = new Date();
                return date.getFullYear() + '-' + (date.getMonth() + 1) +
                    '-' + date.getDate() + ' ' + date.getHours() + ':' +
                    date.getMinutes() + ':' + date.getSeconds() +
                    '     ' + date.getTime();
            },
            getCaseNumber: function() {
                var date = new Date();
                return date.getTime();
            },
            getGeneData: getGeneData,
            getVUSData: getVUSData,
            isUndefinedOrEmpty: isUndefinedOrEmpty,
            stringObject: stringObject,
            getTextString: OncoKB.utils.getString,
            mostRecentItem: mostRecentItem,
            getHistoryData: getHistoryData
        };
    });
