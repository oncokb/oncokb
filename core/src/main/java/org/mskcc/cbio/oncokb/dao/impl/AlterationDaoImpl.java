/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.*;

import java.util.List;

/**
 * @author jgao
 */
public class AlterationDaoImpl extends GenericDaoImpl<Alteration, Integer> implements AlterationDao {
    @Override
    public List<Alteration> findAlterationsByGene(Gene gene) {
        return findByNamedQuery("findAlterationsByGene", gene);
    }

    @Override
    public Alteration findAlteration(Gene gene, AlterationType alterationType, String alteration) {
        List<Alteration> alterations = findByNamedQuery("findAlteration", gene, alteration);
        return alterations.isEmpty() ? null : alterations.get(0);
    }

    @Override
    public Alteration findAlteration(Gene gene, AlterationType alterationType, String alteration, String name) {
        List<Alteration> alterations = findByNamedQuery("findAlterationByAlterationAndName", gene, alteration, name);
        return alterations.isEmpty() ? null : alterations.get(0);
    }

    @Override
    public List<Alteration> findMutationsByConsequenceAndPosition(Gene gene, VariantConsequence consequence, int start, int end) {
        if (start <= AlterationPositionBoundary.START.getValue() || end >= AlterationPositionBoundary.END.getValue()) {
            return findByNamedQuery("findMutationsByConsequenceAndPosition", gene, consequence, start, end);
        } else {
            return findByNamedQuery("findMutationsByConsequenceAndPosition", gene, consequence, end, start);
        }
    }

    @Override
    public List<Alteration> findMutationsByConsequenceAndPositionOnSamePosition(Gene gene, VariantConsequence consequence, int start, int end) {
        return findByNamedQuery("findMutationsByConsequenceAndPositionOnSamePosition", gene, consequence, start, end);
    }
}
