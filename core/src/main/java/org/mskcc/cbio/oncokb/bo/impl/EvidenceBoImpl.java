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
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

/**
 * @author jgao
 */
public class EvidenceBoImpl extends GenericBoImpl<Evidence, EvidenceDao> implements EvidenceBo {

    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Alteration alteration : alterations) {
            set.addAll(getDao().findEvidencesByAlteration(alteration));
        }
        return new ArrayList<Evidence>(set);
    }

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
    public List<Evidence> findEvidencesByAlterationWithLevels(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<LevelOfEvidence> levelOfEvidences) {
        if (evidenceTypes == null) {
            return findEvidencesByAlteration(alterations, evidenceTypes);
        }
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Alteration alteration : alterations) {
            for (EvidenceType evidenceType : evidenceTypes) {
                for (LevelOfEvidence levelOfEvidence : levelOfEvidences) {
                    set.addAll(getDao().findEvidencesByAlterationAndLevels(alteration, evidenceType, levelOfEvidence));
                }
            }
        }
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<OncoTreeType> tumorTypes) {
        if (tumorTypes == null) {
            if (evidenceTypes == null) {
                return findEvidencesByAlteration(alterations);
            }
            return findEvidencesByAlteration(alterations, evidenceTypes);
        }
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        List<Alteration> alts = new ArrayList<>();
        List<String> cancerTypes = new ArrayList<>();
        List<String> subTypes = new ArrayList<>();
        List<String> cancerTypesOfSubtypes = new ArrayList<>();
        List<EvidenceType> ets = new ArrayList<>();
        alts.addAll(alterations);
        ets.addAll(evidenceTypes);

        for (OncoTreeType oncoTreeType : tumorTypes) {
            if (oncoTreeType.getSubtype() == null) {
                cancerTypes.add(oncoTreeType.getCancerType());
            } else {
                subTypes.add(oncoTreeType.getCode());
                cancerTypesOfSubtypes.add(oncoTreeType.getCancerType());
            }
        }

        if (subTypes.size() > 0) {
            List<String> tts = subTypes;
            set.addAll(getDao().findEvidencesByAlterationsAndSubtypesAndEvidenceTypes(alts, tts, ets));
        }

        if (cancerTypesOfSubtypes.size() > 0) {
            List<String> tts = cancerTypesOfSubtypes;
            set.addAll(getDao().findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesNoSubtype(alts, tts, ets));
        }

        if (cancerTypes.size() > 0) {
            List<String> tts = cancerTypes;
            set.addAll(getDao().findEvidencesByAlterationsAndCancerTypesAndEvidenceTypes(alts, tts, ets));
        }
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<OncoTreeType> tumorTypes, Collection<LevelOfEvidence> levelOfEvidences) {
        if (tumorTypes == null) {
            if (evidenceTypes == null) {
                return findEvidencesByAlteration(alterations);
            }
            return findEvidencesByAlteration(alterations, evidenceTypes);
        }
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        List<Alteration> alts = new ArrayList<>();
        List<String> cancerTypes = new ArrayList<>();
        List<String> subTypes = new ArrayList<>();
        List<String> cancerTypesOfSubtypes = new ArrayList<>();
        List<EvidenceType> ets = new ArrayList<>();
        List<LevelOfEvidence> les = new ArrayList<>();
        alts.addAll(alterations);
        ets.addAll(evidenceTypes);
        les.addAll(levelOfEvidences);

        for (OncoTreeType oncoTreeType : tumorTypes) {
            if (oncoTreeType.getSubtype() == null) {
                cancerTypes.add(oncoTreeType.getCancerType());
            } else {
                subTypes.add(oncoTreeType.getCode());
                cancerTypesOfSubtypes.add(oncoTreeType.getCancerType());
            }
        }

        if (subTypes.size() > 0) {
            List<String> tts = subTypes;
            set.addAll(getDao().findEvidencesByAlterationsAndSubtypesAndEvidenceTypesAndLevelOfEvidence(alts, tts, ets, les));
        }

        if (cancerTypesOfSubtypes.size() > 0) {
            List<String> tts = cancerTypesOfSubtypes;
            set.addAll(getDao().findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesAndLevelOfEvidenceNoSubtype(alts, tts, ets, les));
        }

        if (cancerTypes.size() > 0) {
            List<String> tts = cancerTypes;
            set.addAll(getDao().findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesAndLevelOfEvidence(alts, tts, ets, les));
        }

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
    public List<Evidence> findEvidencesByGene(Collection<Gene> genes, Collection<EvidenceType> evidenceTypes, Collection<OncoTreeType> tumorTypes) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Gene gene : genes) {
            for (EvidenceType evidenceType : evidenceTypes) {
                for (OncoTreeType oncoTreeType : tumorTypes) {
                    if (oncoTreeType.getSubtype() != null) {
                        set.addAll(getDao().findEvidencesByGeneAndSubtype(gene, evidenceType, oncoTreeType.getCode()));
                        set.addAll(getDao().findEvidencesByGeneAndCancerTypeNoSubtype(gene, evidenceType, oncoTreeType.getCancerType()));
                    } else if (oncoTreeType.getCancerType() != null) {
                        set.addAll(getDao().findEvidencesByGeneAndCancerType(gene, evidenceType, oncoTreeType.getCancerType()));
                    }
                }
            }
        }
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByIds(List<Integer> ids) {
        return getDao().findEvidencesByIds(ids);
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

    @Override
    public List<Object> findTumorTypesWithEvidencesForAlterations(List<Alteration> alterations) {
        return getDao().findTumorTypesWithEvidencesForAlterations(alterations);
    }

    @Override
    public List<Object> findCancerTypesWithEvidencesForAlterations(List<Alteration> alterations) {
        return getDao().findCancerTypesWithEvidencesForAlterations(alterations);
    }

    @Override
    public List<Object> findSubtypesWithEvidencesForAlterations(List<Alteration> alterations) {
        return getDao().findSubtypesWithEvidencesForAlterations(alterations);
    }

    @Override
    public List<String> findAllCancerTypes() {
        return getDao().findAllCancerTypes();
    }

    @Override
    public List<String> findAllSubtypes() {
        return getDao().findAllSubtypes();
    }

    @Override
    public List<Evidence> findEvidenceByUUIDs(List<String> uuids){
        return getDao().findEvidenceByUUIDs(uuids);
    }
}
