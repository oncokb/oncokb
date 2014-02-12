/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;

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
     * @param alteration
     * @return 
     */
    Alteration findAlteration(Gene gene, String alteration);
}
