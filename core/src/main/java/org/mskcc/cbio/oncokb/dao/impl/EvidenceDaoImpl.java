/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.Query;
import org.mskcc.cbio.oncokb.dao.EvidenceDao;
import org.mskcc.cbio.oncokb.model.*;

/**
 *
 * @author jgao
 */
public class EvidenceDaoImpl
            extends GenericDaoImpl<Evidence, Integer>
            implements EvidenceDao {
    @Override
    public List<Evidence> findEvidencesByAlteration(Alteration alteration) {
        return findByNamedQuery("findEvidencesByAlteration", alteration.getAlterationId());
    }

    @Override
    public List<Evidence> findEvidencesByAlterationAndTumorType(Alteration alteration, TumorType tumorType) {
        return findByNamedQuery("findEvidencesByAlterationAndTumorType", alteration.getAlterationId(), tumorType);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationsAndTumorTypesAndEvidenceTypes(List<Alteration> alterations, List<TumorType> tumorTypes, List<EvidenceType> evidenceTypes) {
        List<Integer> alterationIds = new ArrayList<>();
        for(Alteration alteration : alterations) {
            alterationIds.add(alteration.getAlterationId());
        }

        String[] params = {"alts", "tts", "ets"};
        List[] values = {alterationIds, tumorTypes, evidenceTypes};

        return findByNamedQueryAndNamedParam("findEvidencesByAlterationsAndTumorTypesAndEvidenceTypes", params, values);
    }

    @Override
    public List<Evidence> findEvidencesByAlterationsAndTumorTypesAndEvidenceTypesAndLevelOfEvidence(List<Alteration> alterations, List<TumorType> tumorTypes, List<EvidenceType> evidenceTypes, List<LevelOfEvidence> levelOfEvidences) {
        List<Integer> alterationIds = new ArrayList<>();
        for(Alteration alteration : alterations) {
            alterationIds.add(alteration.getAlterationId());
        }

        String[] params = {"alts", "tts", "ets", "les"};
        List[] values = {alterationIds, tumorTypes, evidenceTypes, levelOfEvidences};

        return findByNamedQueryAndNamedParam("findEvidencesByAlterationsAndTumorTypesAndEvidenceTypesAndLevelOfEvidence", params, values);
    }
    
    @Override
    public List<Evidence> findEvidencesByAlteration(Alteration alteration, EvidenceType evidenceType) {
        if (evidenceType==null) return findEvidencesByAlteration(alteration);
        return findByNamedQuery("findEvidencesByAlterationAndEvidenceType", alteration.getAlterationId(), evidenceType);
    }
    
    @Override
    public List<Evidence> findEvidencesByAlteration(Alteration alteration, EvidenceType evidenceType, TumorType tumorType) {
        if (tumorType==null) return findEvidencesByAlteration(alteration, evidenceType);
        return findByNamedQuery("findEvidencesByAlterationAndEvidenceTypeAndTumorType", alteration.getAlterationId(), evidenceType, tumorType);
    }


    @Override
    public List<Evidence> findEvidencesByGene(Gene gene) {
        return findByNamedQuery("findEvidencesByGene", gene);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Gene gene, EvidenceType evidenceType) {
        if (evidenceType==null) return findEvidencesByGene(gene);
        return findByNamedQuery("findEvidencesByGeneAndEvidenceType", gene, evidenceType);
    }

    @Override
    public List<Evidence> findEvidencesByGene(Gene gene, EvidenceType evidenceType, TumorType tumorType) {
        if (tumorType==null) return findEvidencesByGene(gene, evidenceType);
        return findByNamedQuery("findEvidencesByGeneAndEvidenceTypeAndTumorType", gene, evidenceType);
    }
}
