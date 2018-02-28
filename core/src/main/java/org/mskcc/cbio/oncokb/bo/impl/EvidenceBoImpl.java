/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import com.google.common.collect.Sets;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.mskcc.cbio.oncokb.model.oncotree.TumorType;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;

import java.util.*;

/**
 * @author jgao
 */
public class EvidenceBoImpl extends GenericBoImpl<Evidence, EvidenceDao> implements EvidenceBo {

    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        if (CacheUtils.isEnabled()) {
            Set<Alteration> alterationSet = new HashSet<>(alterations);
            for (Evidence evidence : EvidenceUtils.getAllEvidencesByAlterationsGenes(alterations)) {
                if (!Collections.disjoint(evidence.getAlterations(), alterationSet)) {
                    set.add(evidence);
                }
            }
        } else {
            for (Alteration alteration : alterations) {
                set.addAll(getDao().findEvidencesByAlteration(alteration));
            }
        }
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        Set<Alteration> altsSet = new HashSet<>(alterations);
        if (CacheUtils.isEnabled()) {
            for (Evidence evidence : EvidenceUtils.getAllEvidencesByAlterationsGenes(alterations)) {
                if (evidenceTypes.contains(evidence.getEvidenceType()) && !Collections.disjoint(evidence.getAlterations(), altsSet)) {
                    set.add(evidence);
                }
            }
        } else {
            for (Alteration alteration : alterations) {
                for (EvidenceType evidenceType : evidenceTypes) {
                    set.addAll(getDao().findEvidencesByAlteration(alteration, evidenceType));
                }
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

        if (CacheUtils.isEnabled()) {
            for (Evidence evidence : EvidenceUtils.getAllEvidencesByAlterationsGenes(alterations)) {
                if (Sets.intersection(evidence.getAlterations(), new HashSet(alterations)).size() > 0
                    && evidenceTypes.contains(evidence.getEvidenceType())
                    && levelOfEvidences.contains(evidence.getLevelOfEvidence())) {
                    set.add(evidence);
                }
            }
        } else {
            for (Alteration alteration : alterations) {
                for (EvidenceType evidenceType : evidenceTypes) {
                    for (LevelOfEvidence levelOfEvidence : levelOfEvidences) {
                        set.addAll(getDao().findEvidencesByAlterationAndLevels(alteration, evidenceType, levelOfEvidence));
                    }
                }
            }
        }
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes) {
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

        for (TumorType oncoTreeType : tumorTypes) {
            if (oncoTreeType.getCode() == null) {
                if (oncoTreeType.getMainType() != null) {
                    cancerTypes.add(oncoTreeType.getMainType().getName());
                }
            } else {
                subTypes.add(oncoTreeType.getCode());
                if (oncoTreeType.getMainType() != null) {
                    cancerTypesOfSubtypes.add(oncoTreeType.getMainType().getName());
                }
            }
        }

        if (subTypes.size() > 0) {
            List<String> tts = subTypes;
            if (CacheUtils.isEnabled()) {
                List<Evidence> evidences = findEvidencesByAlteration(alterations);
                for (Evidence evidence : evidences) {
                    if ((ets.contains(evidence.getEvidenceType())) && tts.contains(evidence.getSubtype())) {
                        set.add(evidence);
                    }
                }
            } else {
                set.addAll(getDao().findEvidencesByAlterationsAndSubtypesAndEvidenceTypes(alts, tts, ets));
            }
        }

        if (cancerTypesOfSubtypes.size() > 0) {
            List<String> tts = cancerTypesOfSubtypes;

            if (CacheUtils.isEnabled()) {
                List<Evidence> evidences = findEvidencesByAlteration(alterations);
                for (Evidence evidence : evidences) {
                    if ((ets.contains(evidence.getEvidenceType())) && tts.contains(evidence.getCancerType())
                        && evidence.getSubtype() == null) {
                        set.add(evidence);
                    }
                }
            } else {
                set.addAll(getDao().findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesNoSubtype(alts, tts, ets));
            }
        }

        if (cancerTypes.size() > 0) {
            List<String> tts = cancerTypes;
            if (CacheUtils.isEnabled()) {
                List<Evidence> evidences = findEvidencesByAlteration(alterations);
                for (Evidence evidence : evidences) {
                    if ((ets.contains(evidence.getEvidenceType())) && tts.contains(evidence.getCancerType())) {
                        set.add(evidence);
                    }
                }
            } else {
                set.addAll(getDao().findEvidencesByAlterationsAndCancerTypesAndEvidenceTypes(alts, tts, ets));
            }

        }
        return new ArrayList<>(set);
    }

    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes, Collection<LevelOfEvidence> levelOfEvidences) {
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

        for (TumorType oncoTreeType : tumorTypes) {
            if (oncoTreeType.getCode() == null) {
                if (oncoTreeType.getMainType() != null) {
                    cancerTypes.add(oncoTreeType.getMainType().getName());
                }
            } else {
                subTypes.add(oncoTreeType.getCode());
                if (oncoTreeType.getMainType() != null) {
                    cancerTypesOfSubtypes.add(oncoTreeType.getMainType().getName());
                }
            }
        }

        if (subTypes.size() > 0) {
            List<String> tts = subTypes;
            if (CacheUtils.isEnabled()) {
                List<Evidence> evidences = findEvidencesByAlteration(alterations);
                for (Evidence evidence : evidences) {
                    if ((ets.contains(evidence.getEvidenceType())) && tts.contains(evidence.getSubtype())
                        && les.contains(evidence.getLevelOfEvidence())) {
                        set.add(evidence);
                    }
                }
            } else {
                set.addAll(getDao().findEvidencesByAlterationsAndSubtypesAndEvidenceTypesAndLevelOfEvidence(alts, tts, ets, les));
            }
        }

        if (cancerTypesOfSubtypes.size() > 0) {
            List<String> tts = cancerTypesOfSubtypes;
            if (CacheUtils.isEnabled()) {
                List<Evidence> evidences = findEvidencesByAlteration(alterations);
                for (Evidence evidence : evidences) {
                    if ((ets.contains(evidence.getEvidenceType())) && tts.contains(evidence.getCancerType())
                        && evidence.getSubtype() == null
                        && les.contains(evidence.getLevelOfEvidence())) {
                        set.add(evidence);
                    }
                }
            } else {
                set.addAll(getDao().findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesAndLevelOfEvidenceNoSubtype(alts, tts, ets, les));
            }
        }

        if (cancerTypes.size() > 0) {
            List<String> tts = cancerTypes;
            if (CacheUtils.isEnabled()) {
                List<Evidence> evidences = findEvidencesByAlteration(alterations);
                for (Evidence evidence : evidences) {
                    if ((ets.contains(evidence.getEvidenceType())) && tts.contains(evidence.getCancerType())
                        && les.contains(evidence.getLevelOfEvidence())) {
                        set.add(evidence);
                    }
                }
            } else {
                set.addAll(getDao().findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesAndLevelOfEvidence(alts, tts, ets, les));
            }
        }

        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Collection<Gene> genes) {
        if (CacheUtils.isEnabled()) {
            Set<Evidence> set = new LinkedHashSet<Evidence>();
            for (Gene gene : genes) {
                set.addAll(CacheUtils.getEvidences(gene));
            }
            return new ArrayList<Evidence>(set);
        } else {
            return findEvidencesByGeneFromDB(genes);
        }
    }

    @Override
    public List<Evidence> findEvidencesByGeneFromDB(Collection<Gene> genes) {
        Set<Evidence> set = new LinkedHashSet<>();
        for (Gene gene : genes) {
            set.addAll(getDao().findEvidencesByGene(gene));
        }
        return new ArrayList<>(set);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Collection<Gene> genes, Collection<EvidenceType> evidenceTypes) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Gene gene : genes) {
            if (CacheUtils.isEnabled()) {
                for (Evidence evidence : CacheUtils.getEvidences(gene)) {
                    if (evidenceTypes.contains(evidence.getEvidenceType())) {
                        set.add(evidence);
                    }
                }
            } else {
                for (EvidenceType evidenceType : evidenceTypes) {
                    set.addAll(getDao().findEvidencesByGene(gene, evidenceType));
                }
            }
        }
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Collection<Gene> genes, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Gene gene : genes) {
            if (CacheUtils.isEnabled()) {
                for (Evidence evidence : CacheUtils.getEvidences(gene)) {
                    if (evidenceTypes.contains(evidence.getEvidenceType())) {
                        for (TumorType oncoTreeType : tumorTypes) {
                            if (oncoTreeType.getCode() != null) {
                                if (oncoTreeType.getCode().equals(evidence.getSubtype())) {
                                    set.add(evidence);
                                }
                                if (oncoTreeType.getMainType() != null && oncoTreeType.getMainType().getName().equals(evidence.getCancerType()) && evidence.getSubtype() == null) {
                                    set.add(evidence);
                                }
                            } else if (oncoTreeType.getMainType() != null) {
                                if (oncoTreeType.getMainType().getName().equals(evidence.getCancerType()) && evidence.getSubtype() == null) {
                                    set.add(evidence);
                                }
                            }
                        }
                    }
                }
            } else {
                for (EvidenceType evidenceType : evidenceTypes) {
                    for (TumorType oncoTreeType : tumorTypes) {
                        if (oncoTreeType.getCode() != null) {
                            set.addAll(getDao().findEvidencesByGeneAndSubtype(gene, evidenceType, oncoTreeType.getCode()));
                            if (oncoTreeType.getMainType() != null) {
                                set.addAll(getDao().findEvidencesByGeneAndCancerTypeNoSubtype(gene, evidenceType, oncoTreeType.getMainType().getName()));
                            }
                        } else if (oncoTreeType.getMainType() != null) {
                            set.addAll(getDao().findEvidencesByGeneAndCancerType(gene, evidenceType, oncoTreeType.getMainType().getName()));
                        }
                    }
                }
            }
        }
        return new ArrayList<>(set);
    }

    @Override
    public List<Evidence> findEvidencesByIds(List<Integer> ids) {
        if (CacheUtils.isEnabled()) {
            List<Evidence> evidences = new ArrayList<>();
            for (Evidence evidence : CacheUtils.getAllEvidences()) {
                if (ids.contains(evidence.getId())) {
                    evidences.add(evidence);
                }
                if (evidences.size() == ids.size()) {
                    break;
                }
            }
            return evidences;
        } else {
            return getDao().findEvidencesByIds(ids);
        }
    }

    @Override
    public List<Drug> findDrugsByAlterations(Collection<Alteration> alterations) {
        List<Evidence> evidences = new ArrayList<Evidence>();

        if (CacheUtils.isEnabled()) {
            Set<EvidenceType> evidenceTypes = new HashSet<>();
            evidenceTypes.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
            evidenceTypes.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
            evidences = findEvidencesByAlteration(alterations, evidenceTypes);
        } else {
            for (Alteration alteration : alterations) {
                evidences.addAll(getDao().findEvidencesByAlteration(alteration, EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY));
                evidences.addAll(getDao().findEvidencesByAlteration(alteration, EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY));
            }
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
    public List<Evidence> findEvidenceByUUIDs(List<String> uuids) {
        if (CacheUtils.isEnabled()) {
            return new ArrayList<>(CacheUtils.getEvidencesByUUIDs(new HashSet<>(uuids)));
        } else {
            return getDao().findEvidenceByUUIDs(uuids);
        }
    }
}
