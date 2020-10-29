/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import com.google.common.collect.Sets;
import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;

import java.util.*;

/**
 * @author jgao
 */
public class EvidenceBoImpl extends GenericBoImpl<Evidence, EvidenceDao> implements EvidenceBo {

    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        Set<Alteration> alterationSet = new HashSet<>(alterations);
        for (Evidence evidence : EvidenceUtils.getAllEvidencesByAlterationsGenes(alterations)) {
            if (!Collections.disjoint(evidence.getAlterations(), alterationSet)) {
                set.add(evidence);
            }
        }
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        Set<Alteration> altsSet = new HashSet<>(alterations);
        for (Evidence evidence : EvidenceUtils.getAllEvidencesByAlterationsGenes(alterations)) {
            if (evidenceTypes.contains(evidence.getEvidenceType()) && !Collections.disjoint(evidence.getAlterations(), altsSet)) {
                set.add(evidence);
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

        for (Evidence evidence : EvidenceUtils.getAllEvidencesByAlterationsGenes(alterations)) {
            if (Sets.intersection(evidence.getAlterations(), new HashSet(alterations)).size() > 0
                && evidenceTypes.contains(evidence.getEvidenceType())
                && levelOfEvidences.contains(evidence.getLevelOfEvidence())) {
                set.add(evidence);
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
            List<Evidence> evidences = findEvidencesByAlteration(alterations);
            for (Evidence evidence : evidences) {
                if ((ets.contains(evidence.getEvidenceType())) && tts.contains(evidence.getSubtype())) {
                    set.add(evidence);
                }
            }
        }

        if (cancerTypesOfSubtypes.size() > 0) {
            List<String> tts = cancerTypesOfSubtypes;


            List<Evidence> evidences = findEvidencesByAlteration(alterations);
            for (Evidence evidence : evidences) {
                if ((ets.contains(evidence.getEvidenceType())) && tts.contains(evidence.getCancerType())
                    && evidence.getSubtype() == null) {
                    set.add(evidence);
                }
            }
        }

        if (cancerTypes.size() > 0) {
            List<String> tts = cancerTypes;

            List<Evidence> evidences = findEvidencesByAlteration(alterations);
            for (Evidence evidence : evidences) {
                if ((ets.contains(evidence.getEvidenceType())) && tts.contains(evidence.getCancerType()) && StringUtils.isNullOrEmpty(evidence.getSubtype())) {
                    set.add(evidence);
                }
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
            if (StringUtils.isNullOrEmpty(oncoTreeType.getCode())) {
                if (oncoTreeType.getMainType() != null) {
                    cancerTypes.add(oncoTreeType.getMainType().getName());
                }
            } else {
                subTypes.add(oncoTreeType.getCode());
            }
        }

        if (subTypes.size() > 0) {
            List<String> tts = subTypes;

            List<Evidence> evidences = findEvidencesByAlteration(alterations);
            for (Evidence evidence : evidences) {
                if ((ets.contains(evidence.getEvidenceType())) && tts.contains(evidence.getSubtype())
                    && les.contains(evidence.getLevelOfEvidence())) {
                    set.add(evidence);
                }
            }
        }

        if (cancerTypes.size() > 0) {
            List<String> tts = cancerTypes;

            List<Evidence> evidences = findEvidencesByAlteration(alterations);
            for (Evidence evidence : evidences) {
                if ((ets.contains(evidence.getEvidenceType())) && tts.contains(evidence.getCancerType())
                    && les.contains(evidence.getLevelOfEvidence()) && StringUtils.isNullOrEmpty(evidence.getSubtype())) {
                    set.add(evidence);
                }
            }
        }

        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Collection<Gene> genes) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Gene gene : genes) {
            set.addAll(CacheUtils.getEvidences(gene));
        }
        return new ArrayList<Evidence>(set);
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
            for (Evidence evidence : CacheUtils.getEvidences(gene)) {
                if (evidenceTypes.contains(evidence.getEvidenceType())) {
                    set.add(evidence);
                }
            }
        }
        return new ArrayList<Evidence>(set);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Collection<Gene> genes, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes) {
        Set<Evidence> set = new LinkedHashSet<Evidence>();
        for (Gene gene : genes) {
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
        }
        return new ArrayList<>(set);
    }

    @Override
    public List<Evidence> findEvidencesByIds(List<Integer> ids) {
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
    }

    @Override
    public List<Drug> findDrugsByAlterations(Collection<Alteration> alterations) {
        List<Evidence> evidences = new ArrayList<Evidence>();

        Set<EvidenceType> evidenceTypes = new HashSet<>();
        evidenceTypes.add(EvidenceType.STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY);
        evidenceTypes.add(EvidenceType.INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY);
        evidences = findEvidencesByAlteration(alterations, evidenceTypes);


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
        return new ArrayList<>(CacheUtils.getEvidencesByUUIDs(new HashSet<>(uuids)));
    }
}
