/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;

/**
 *
 * @author jgao
 */
public interface AlterationDao extends GenericDao<Alteration, Integer> {
    
    /**
     * Get set of alterations by entrez gene Id.
     * @param entrezGeneId
     * @return 
     */
    List<Alteration> findAlterationsByGene(int entrezGeneId);
    
    /**
     * 
     * @param entrezGeneId
     * @param alteration
     * @return 
     */
    Alteration findAlteration(int entrezGeneId, String alteration);
}
