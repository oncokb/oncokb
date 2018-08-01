/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.VariantConsequence;

import java.util.List;

/**
 *
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
        return findByNamedQuery("findMutationsByConsequenceAndPosition", gene, consequence, start, end);
    }

    @Override
    public List<Alteration> findMutationsByConsequenceAndPositionOnSamePosition(Gene gene, VariantConsequence consequence, int start, int end) {
        return findByNamedQuery("findMutationsByConsequenceAndPositionOnSamePosition", gene, consequence, start, end);
    }
}
