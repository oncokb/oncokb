/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import com.sun.org.apache.bcel.internal.generic.ALOAD;
import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.*;

import java.util.ArrayList;
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
    public Alteration findAlteration(Gene gene, AlterationType alterationType, ReferenceGenome referenceGenome, String alteration) {
        List<Alteration> alterations = referenceGenome == null ?
            findByNamedQuery("findAlteration", gene, alteration) :
            findByNamedQuery("findAlterationAndReferenceGenome", gene, alteration, referenceGenome);
        return alterations.isEmpty() ? null : alterations.get(0);
    }

    @Override
    public Alteration findAlteration(Gene gene, AlterationType alterationType, ReferenceGenome referenceGenome, String alteration, String name) {

        List<Alteration> alterations = referenceGenome == null ?
            findByNamedQuery("findAlterationByAlterationAndName", gene, alteration, name) :
            findByNamedQuery("findAlterationByAlterationAndNameAndReferenceGenome", gene, alteration, name, referenceGenome);
        return alterations.isEmpty() ? null : alterations.get(0);
    }

    @Override
    public List<Alteration> findMutationsByConsequenceAndPosition(Gene gene, ReferenceGenome referenceGenome, VariantConsequence consequence, int start, int end) {
        List<Alteration> result = new ArrayList<>();
        if (start <= AlterationPositionBoundary.START.getValue() || end >= AlterationPositionBoundary.END.getValue()) {
            result = findByNamedQuery("findMutationsByConsequenceAndPosition", gene, consequence, start, end);
        } else {
            result = findByNamedQuery("findMutationsByConsequenceAndPosition", gene, consequence, end, start);
        }
        return filterAlterationsByReferenceGenome(result, referenceGenome);
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
