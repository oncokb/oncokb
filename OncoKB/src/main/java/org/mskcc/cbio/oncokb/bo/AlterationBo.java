/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;

/**
 *
 * @author jgao
 */
public interface AlterationBo extends GenericBo<Alteration> {
    
    /**
     * Get set of alterations by entrez gene Id.
     * @param entrezGeneId
     * @return 
     */
    List<Alteration> getAlterationsByGene(int entrezGeneId);
}
