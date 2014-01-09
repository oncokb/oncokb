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
public interface AlterationDao {
    
    /**
     * Get set of alterations by entrez gene Id.
     * @param entrezGeneId
     * @return 
     */
    List<Alteration> getAlterations(long entrezGeneId);
    
    /**
     * Save alteration to db.
     * @param alteration 
     */
    void saveOrUpdate(Alteration alteration);
}
