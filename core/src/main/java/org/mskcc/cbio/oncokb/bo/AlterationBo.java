/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.VariantConsequence;

/**
 *
 * @author jgao
 */
public interface AlterationBo extends GenericBo<Alteration> {

    /**
     * Get set of alterations by entrez gene Ids.
     * @param genes
     * @return
     */
    List<Alteration> findAlterationsByGene(Collection<Gene> genes);

    /**
     *
     * @param gene
     * @param alterationType
     * @param alteration
     * @return
     */
    Alteration findAlteration(Gene gene, AlterationType alterationType, String alteration);

    /**
     *
     * @param gene
     * @param consequence
     * @param start
     * @param end
     * @return
     */
    List<Alteration> findMutationsByConsequenceAndPosition(Gene gene, VariantConsequence consequence, int start, int end, Collection<Alteration> alterations);

    /**
     *
     * @param gene
     * @param consequence
     * @param start
     * @param end
     * @return
     */
    List<Alteration> findMutationsByConsequenceAndPositionOnSamePosition(Gene gene, VariantConsequence consequence, int start, int end, Collection<Alteration> alterations);

    /**
     *
     * @param alteration
     * @return
     */
    LinkedHashSet<Alteration> findRelevantAlterations(Alteration alteration, boolean includeAlternativeAllele);

    /**
     *
     * @param alteration
     * @return
     */
    LinkedHashSet<Alteration> findRelevantAlterations(Alteration alteration, Set<Alteration> alterations, boolean includeAlternativeAllele);
}
