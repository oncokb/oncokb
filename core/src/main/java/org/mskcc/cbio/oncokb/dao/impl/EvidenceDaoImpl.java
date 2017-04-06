/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jgao
 */
public class EvidenceDaoImpl
    extends GenericDaoImpl<Evidence, Integer>
    implements EvidenceDao {
    @Override
    public List<Evidence> findEvidencesByAlteration(Alteration alteration) {
        return findByNamedQuery("findEvidencesByAlteration", alteration.getId());
    }

    @Override
    public List<Evidence> findEvidencesByAlterationAndTumorType(Alteration alteration, String tumorType) {
        String[] params = {"alt", "tt"};
        String[] values = {Integer.toString(alteration.getId()), tumorType};
        return findByNamedQuery("findEvidencesByAlterationAndTumorType", params, values);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationAndCancerType(Alteration alteration, String cancerType) {
        return findByNamedQuery("findEvidencesByAlterationAndCancerType", alteration.getId(), cancerType);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationAndCancerTypeNoSubtype(Alteration alteration, String cancerType) {
        return findByNamedQuery("findEvidencesByAlterationAndCancerTypeNoSubtype", alteration.getId(), cancerType);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationAndSubtype(Alteration alteration, String subtype) {
        return findByNamedQuery("findEvidencesByAlterationAndSubtype", alteration.getId(), subtype);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationsAndTumorTypesAndEvidenceTypes(List<Alteration> alterations, List<String> tumorTypes, List<EvidenceType> evidenceTypes) {
        List<Integer> alterationIds = new ArrayList<>();
        for (Alteration alteration : alterations) {
            alterationIds.add(alteration.getId());
        }

        String[] params = {"alts", "tts", "ets"};
        List[] values = {alterationIds, tumorTypes, evidenceTypes};

        return findByNamedQueryAndNamedParam("findEvidencesByAlterationsAndTumorTypesAndEvidenceTypes", params, values);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationsAndCancerTypesAndEvidenceTypes(List<Alteration> alterations, List<String> cancerTypes, List<EvidenceType> evidenceTypes) {
        List<Integer> alterationIds = new ArrayList<>();
        for (Alteration alteration : alterations) {
            alterationIds.add(alteration.getId());
        }

        String[] params = {"alts", "tts", "ets"};
        List[] values = {alterationIds, cancerTypes, evidenceTypes};

        return findByNamedQueryAndNamedParam("findEvidencesByAlterationsAndCancerTypesAndEvidenceTypes", params, values);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesNoSubtype(List<Alteration> alterations, List<String> cancerTypes, List<EvidenceType> evidenceTypes) {
        List<Integer> alterationIds = new ArrayList<>();
        for (Alteration alteration : alterations) {
            alterationIds.add(alteration.getId());
        }

        String[] params = {"alts", "tts", "ets"};
        List[] values = {alterationIds, cancerTypes, evidenceTypes};

        return findByNamedQueryAndNamedParam("findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesNoSubtype", params, values);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationsAndSubtypesAndEvidenceTypes(List<Alteration> alterations, List<String> subtypes, List<EvidenceType> evidenceTypes) {
        List<Integer> alterationIds = new ArrayList<>();
        for (Alteration alteration : alterations) {
            alterationIds.add(alteration.getId());
        }

        String[] params = {"alts", "tts", "ets"};
        List[] values = {alterationIds, subtypes, evidenceTypes};

        return findByNamedQueryAndNamedParam("findEvidencesByAlterationsAndSubtypesAndEvidenceTypes", params, values);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationsAndTumorTypesAndEvidenceTypesAndLevelOfEvidence(List<Alteration> alterations, List<String> tumorTypes, List<EvidenceType> evidenceTypes, List<LevelOfEvidence> levelOfEvidences) {
        List<Integer> alterationIds = new ArrayList<>();
        for (Alteration alteration : alterations) {
            alterationIds.add(alteration.getId());
        }

        String[] params = {"alts", "tts", "ets", "les"};
        List[] values = {alterationIds, tumorTypes, evidenceTypes, levelOfEvidences};

        return findByNamedQueryAndNamedParam("findEvidencesByAlterationsAndTumorTypesAndEvidenceTypesAndLevelOfEvidence", params, values);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesAndLevelOfEvidence(List<Alteration> alterations, List<String> cancerTypes, List<EvidenceType> evidenceTypes, List<LevelOfEvidence> levelOfEvidences) {
        List<Integer> alterationIds = new ArrayList<>();
        for (Alteration alteration : alterations) {
            alterationIds.add(alteration.getId());
        }

        String[] params = {"alts", "tts", "ets", "les"};
        List[] values = {alterationIds, cancerTypes, evidenceTypes, levelOfEvidences};

        return findByNamedQueryAndNamedParam("findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesAndLevelOfEvidence", params, values);

    }

    @Override
    public List<Evidence> findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesAndLevelOfEvidenceNoSubtype(List<Alteration> alterations, List<String> cancerTypes, List<EvidenceType> evidenceTypes, List<LevelOfEvidence> levelOfEvidences) {
        List<Integer> alterationIds = new ArrayList<>();
        for (Alteration alteration : alterations) {
            alterationIds.add(alteration.getId());
        }

        String[] params = {"alts", "tts", "ets", "les"};
        List[] values = {alterationIds, cancerTypes, evidenceTypes, levelOfEvidences};

        return findByNamedQueryAndNamedParam("findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesAndLevelOfEvidenceNoSubtype", params, values);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationsAndSubtypesAndEvidenceTypesAndLevelOfEvidence(List<Alteration> alterations, List<String> subtypes, List<EvidenceType> evidenceTypes, List<LevelOfEvidence> levelOfEvidences) {
        List<Integer> alterationIds = new ArrayList<>();
        for (Alteration alteration : alterations) {
            alterationIds.add(alteration.getId());
        }

        String[] params = {"alts", "tts", "ets", "les"};
        List[] values = {alterationIds, subtypes, evidenceTypes, levelOfEvidences};

        return findByNamedQueryAndNamedParam("findEvidencesByAlterationsAndSubtypesAndEvidenceTypesAndLevelOfEvidence", params, values);

    }

    @Override
    public List<Evidence> findEvidencesByAlteration(Alteration alteration, EvidenceType evidenceType) {
        if (evidenceType == null) return findEvidencesByAlteration(alteration);
        return findByNamedQuery("findEvidencesByAlterationAndEvidenceType", alteration.getId(), evidenceType);
    }


    @Override
    public List<Evidence> findEvidencesByAlterationAndLevels(Alteration alteration, EvidenceType evidenceType, LevelOfEvidence levelOfEvidence) {
        if (evidenceType == null) return findEvidencesByAlteration(alteration);
        if (levelOfEvidence == null) return findEvidencesByAlteration(alteration, evidenceType);
        return findByNamedQuery("findEvidencesByAlterationAndEvidenceTypeAndLevels", alteration.getId(), evidenceType, levelOfEvidence);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationAndTumorType(Alteration alteration, EvidenceType evidenceType, String tumorType) {
        String[] params = {"alt", "tt"};
        String[] values = {Integer.toString(alteration.getId()), tumorType};
        return findByNamedQuery("findEvidencesByAlterationAndEvidenceTypeAndTumorType", params, values);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationAndCancerType(Alteration alteration, EvidenceType evidenceType, String cancerType) {
        if (cancerType == null) return findEvidencesByAlteration(alteration, evidenceType);
        return findByNamedQuery("findEvidencesByAlterationAndEvidenceTypeAndCancerType", alteration.getId(), evidenceType, cancerType);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationAndCancerTypeNoSubtype(Alteration alteration, EvidenceType evidenceType, String cancerType) {
        if (cancerType == null) return findEvidencesByAlteration(alteration, evidenceType);
        return findByNamedQuery("findEvidencesByAlterationAndEvidenceTypeAndCancerTypeNoSubtype", alteration.getId(), evidenceType, cancerType);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationAndSubtype(Alteration alteration, EvidenceType evidenceType, String subtype) {
        if (subtype == null) return findEvidencesByAlteration(alteration, evidenceType);
        return findByNamedQuery("findEvidencesByAlterationAndEvidenceTypeAndSubtype", alteration.getId(), evidenceType, subtype);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Gene gene) {
        return findByNamedQuery("findEvidencesByGene", gene);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Gene gene, EvidenceType evidenceType) {
        if (evidenceType == null) return findEvidencesByGene(gene);
        return findByNamedQuery("findEvidencesByGeneAndEvidenceType", gene, evidenceType);
    }

    @Override
    public List<Evidence> findEvidencesByGeneAndTumorType(Gene gene, EvidenceType evidenceType, String tumorType) {
        if (tumorType == null) return findEvidencesByGene(gene, evidenceType);
        String[] params = {"gene", "et", "tt"};
        Object[] values = {gene, evidenceType, tumorType};
        return findByNamedQuery("findEvidencesByGeneAndEvidenceTypeAndTumorType", params, values);
    }

    @Override
    public List<Evidence> findEvidencesByGeneAndCancerType(Gene gene, EvidenceType evidenceType, String cancerType) {
        if (cancerType == null) return findEvidencesByGene(gene, evidenceType);
        return findByNamedQuery("findEvidencesByGeneAndEvidenceTypeAndCancerType", gene, evidenceType, cancerType);
    }

    @Override
    public List<Evidence> findEvidencesByGeneAndCancerTypeNoSubtype(Gene gene, EvidenceType evidenceType, String cancerType) {
        if (cancerType == null) return findEvidencesByGene(gene, evidenceType);
        return findByNamedQuery("findEvidencesByGeneAndEvidenceTypeAndCancerTypeNoSubtype", gene, evidenceType, cancerType);
    }

    @Override
    public List<Evidence> findEvidencesByGeneAndSubtype(Gene gene, EvidenceType evidenceType, String subtype) {
        if (subtype == null) return findEvidencesByGene(gene, evidenceType);
        return findByNamedQuery("findEvidencesByGeneAndEvidenceTypeAndSubtype", gene, evidenceType, subtype);
    }

    @Override
    public List<Evidence> findEvidencesByIds(List<Integer> ids) {
        if (ids == null) return new ArrayList<>();
        String[] params = {"ids"};
        List[] values = {ids};
        return getHibernateTemplate().findByNamedQueryAndNamedParam("findEvidencesByIds", params, values);
    }

    @Override
    public List<Object> findTumorTypesWithEvidencesForAlterations(List<Alteration> alterations) {
        List<Integer> alts = new ArrayList<>();
        for (Alteration alteration : alterations) {
            alts.add(alteration.getId());
        }
        String[] params = {"alts"};
        List[] values = {alts};
        return getHibernateTemplate().findByNamedQueryAndNamedParam("findTumorTypesWithEvidencesForAlterations", params, values);
    }

    @Override
    public List<Object> findCancerTypesWithEvidencesForAlterations(List<Alteration> alterations) {
        List<Integer> alts = new ArrayList<>();
        for (Alteration alteration : alterations) {
            alts.add(alteration.getId());
        }
        String[] params = {"alts"};
        List[] values = {alts};
        return getHibernateTemplate().findByNamedQueryAndNamedParam("findCancerTypesWithEvidencesForAlterations", params, values);
    }

    @Override
    public List<Object> findSubtypesWithEvidencesForAlterations(List<Alteration> alterations) {
        List<Integer> alts = new ArrayList<>();
        for (Alteration alteration : alterations) {
            alts.add(alteration.getId());
        }
        String[] params = {"alts"};
        List[] values = {alts};
        return getHibernateTemplate().findByNamedQueryAndNamedParam("findSubtypesWithEvidencesForAlterations", params, values);
    }

    @Override
    public List<String> findAllCancerTypes() {
        return getHibernateTemplate().findByNamedQuery("findAllCancerTypes");
    }

    @Override
    public List<String> findAllSubtypes() {
        return getHibernateTemplate().findByNamedQuery("findAllSubtypes");
    }

    @Override
    public List<Evidence> findEvidenceByUUIDs(List<String> uuids) {
        String[] params = {"uuids"};
        List[] values = {uuids};
        return getHibernateTemplate().findByNamedQueryAndNamedParam("findEvidenceByUUIDs", params, values);
    }
}
