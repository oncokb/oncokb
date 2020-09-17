/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.*;

import java.util.List;

/**
 * @author jgao
 */
public interface AlterationDao extends GenericDao<Alteration, Integer> {

    /**
     * Get set of alterations by entrez gene Id.
     *
     * @param gene
     * @return
     */
    List<Alteration> findAlterationsByGene(Gene gene);

    /**
     * @param gene
     * @param alterationType
     * @param alteration
     * @return
     */
    Alteration findAlteration(Gene gene, AlterationType alterationType, ReferenceGenome referenceGenome, String alteration);


    /**
     * @param gene
     * @param alterationType
     * @param alteration
     * @param name
     * @return
     */
    Alteration findAlteration(Gene gene, AlterationType alterationType, ReferenceGenome referenceGenome, String alteration, String name);

    /**
     * @param gene
     * @param consequence
     * @param start
     * @param end
     * @return
     */
    List<Alteration> findMutationsByConsequenceAndPosition(Gene gene, ReferenceGenome referenceGenome, VariantConsequence consequence, int start, int end);

    /**
     * @param gene
     * @param consequence
     * @param start
     * @param end
     * @return
     */
    List<Alteration> findMutationsByConsequenceAndPositionOnSamePosition(Gene gene, ReferenceGenome referenceGenome, VariantConsequence consequence, int start, int end);
}
