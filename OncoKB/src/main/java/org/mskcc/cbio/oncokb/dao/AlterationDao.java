/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 *
 * @author jgao
 */
public interface AlterationDao extends GenericDao<Alteration, Integer> {
    
    /**
     * Get set of alterations by entrez gene Id.
     * @param gene
     * @return 
     */
    List<Alteration> findAlterationsByGene(Gene gene);
    
    /**
     * 
     * @param gene
     * @param alteration_type
     * @param alteration
     * @return 
     */
    Alteration findAlteration(Gene gene, AlterationType alterationType, String alteration);
}
