/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jgao
 */
public class AlterationDaoImpl extends GenericDaoImpl<Alteration, Integer> implements AlterationDao {
    @Override
    public List<Alteration> findAlterationsByGene(Gene gene) {
        return findByNamedQuery("findAlterationsByGene", gene);
    }

    @Override
    public Alteration findAlteration(Gene gene, AlterationType alterationType, ReferenceGenome referenceGenome, String alteration, Boolean isGermline) {
        boolean germline = Boolean.TRUE.equals(isGermline);
        List<Alteration> alterations = referenceGenome == null ?
            findByNamedQuery("findAlteration", gene, alteration, germline) :
            findByNamedQuery("findAlterationAndReferenceGenome", gene, alteration, referenceGenome, germline);
        return alterations.isEmpty() ? null : alterations.get(0);
    }

    @Override
    public Alteration findAlteration(Gene gene, AlterationType alterationType, ReferenceGenome referenceGenome, String alteration, String name, Boolean isGermline) {
        boolean germline = Boolean.TRUE.equals(isGermline);
        List<Alteration> alterations = referenceGenome == null ?
            findByNamedQuery("findAlterationByAlterationAndName", gene, alteration, name, germline) :
            findByNamedQuery("findAlterationByAlterationAndNameAndReferenceGenome", gene, alteration, name, referenceGenome, germline);
        return alterations.isEmpty() ? null : alterations.get(0);
    }

    @Override
    public List<Alteration> findMutationsByConsequenceAndPositionOnSamePosition(Gene gene, ReferenceGenome referenceGenome, VariantConsequence consequence, int start, int end) {
        List<Alteration> result = findByNamedQuery("findMutationsByConsequenceAndPositionOnSamePosition", gene, consequence, start, end);
        return filterAlterationsByReferenceGenome(result, referenceGenome);
    }

    private List<Alteration> filterAlterationsByReferenceGenome(List<Alteration> alterations, ReferenceGenome referenceGenome) {
        if (referenceGenome == null) {
            return alterations;
        } else {
            return alterations.stream().filter(alteration -> alteration.getReferenceGenomes().contains(referenceGenome)).collect(Collectors.toList());
        }
    }
}
