/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.model.*;

/**
 *
 * @author jgao
 */
public class EvidenceBoImpl  extends GenericBoImpl<Evidence, EvidenceDao> implements EvidenceBo {
    
    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Alteration alteration : alterations) {
            set.addAll(getDao().findEvidencesByAlteration(alteration));
        }
        return new ArrayList<Evidence>(set);    }
    
    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Alteration alteration : alterations) {
            for (EvidenceType evidenceType : evidenceTypes) {
                set.addAll(getDao().findEvidencesByAlteration(alteration, evidenceType));
            }
        }
        return new ArrayList<Evidence>(set);
    }
    
    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes) {
        if(tumorTypes == null) {
            if (evidenceTypes == null) {
                return findEvidencesByAlteration(alterations);
            }
            return findEvidencesByAlteration(alterations, evidenceTypes);
        }
        Set<Evidence> set = new LinkedHashSet<Evidence>();
//        for (Alteration alteration : alterations) {
//            for (TumorType tumorType : tumorTypes) {
//                if(evidenceTypes == null) {
//                    set.addAll(getDao().findEvidencesByAlterationAndTumorType(alteration, tumorType));
//                }else{
//                    for (EvidenceType evidenceType : evidenceTypes) {
//                        set.addAll(getDao().findEvidencesByAlteration(alteration, evidenceType, tumorType));
//                    }
//                }
//            }
//        }
        List<Alteration> alts = new ArrayList<>();
        List<TumorType> tts = new ArrayList<>();
        List<EvidenceType> ets = new ArrayList<>();
        alts.addAll(alterations);
        tts.addAll(tumorTypes);
        ets.addAll(evidenceTypes);
        set.addAll(getDao().findEvidencesByAlterationsAndTumorTypesAndEvidenceTypes(alts, tts, ets));
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes, Collection<LevelOfEvidence> levelOfEvidences) {
        if(tumorTypes == null) {
            if (evidenceTypes == null) {
                return findEvidencesByAlteration(alterations);
            }
            return findEvidencesByAlteration(alterations, evidenceTypes);
        }
        Set<Evidence> set = new LinkedHashSet<Evidence>();
//        for (Alteration alteration : alterations) {
//            for (TumorType tumorType : tumorTypes) {
//                if(evidenceTypes == null) {
//                    set.addAll(getDao().findEvidencesByAlterationAndTumorType(alteration, tumorType));
//                }else{
//                    for (EvidenceType evidenceType : evidenceTypes) {
//                        set.addAll(getDao().findEvidencesByAlteration(alteration, evidenceType, tumorType));
//                    }
//                }
//            }
//        }
        List<Alteration> alts = new ArrayList<>();
        List<TumorType> tts = new ArrayList<>();
        List<EvidenceType> ets = new ArrayList<>();
        List<LevelOfEvidence> les = new ArrayList<>();
        alts.addAll(alterations);
        tts.addAll(tumorTypes);
        ets.addAll(evidenceTypes);
        les.addAll(levelOfEvidences);
        set.addAll(getDao().findEvidencesByAlterationsAndTumorTypesAndEvidenceTypesAndLevelOfEvidence(alts, tts, ets, les));
        return new ArrayList<Evidence>(set);
    }
    
    @Override
    public List<Evidence> findEvidencesByGene(Collection<Gene> genes) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Gene gene : genes) {
            set.addAll(getDao().findEvidencesByGene(gene));
        }
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Collection<Gene> genes, Collection<EvidenceType> evidenceTypes) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Gene gene : genes) {
            for (EvidenceType evidenceType : evidenceTypes) {
                set.addAll(getDao().findEvidencesByGene(gene, evidenceType));
            }
        }
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Collection<Gene> genes, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Gene gene : genes) {
            for (EvidenceType evidenceType : evidenceTypes) {
                for (TumorType tumorType : tumorTypes) {
                    set.addAll(getDao().findEvidencesByGene(gene, evidenceType, tumorType));
                }
            }
        }
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Drug> findDrugsByAlterations(Collection<Alteration> alterations) {
        List<Evidence> evidences = new ArrayList<Evidence>();
        for (Alteration alteration : alterations) {
            evidences.addAll(getDao().findEvidencesByAlteration(alteration, EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY));
            evidences.addAll(getDao().findEvidencesByAlteration(alteration, EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY));
        }
        Set<Drug> set = new HashSet<Drug>();
        for (Evidence ev : evidences) {
            for (Treatment t : ev.getTreatments()) {
                set.addAll(t.getDrugs());
            }
        }
        return new ArrayList<Drug>(set);
    }
}
