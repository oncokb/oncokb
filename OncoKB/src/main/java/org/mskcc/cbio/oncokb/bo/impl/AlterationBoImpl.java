/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.Alteration;

/**
 *
 * @author jgao
 */
public class AlterationBoImpl extends GenericBoImpl<Alteration, AlterationDao> implements AlterationBo {

    public List<Alteration> findAlterationsByGene(int entrezGeneId) {
        return getDao().findAlterationsByGene(entrezGeneId);
    }
    
    /**
     * 
     * @param entrezGeneId
     * @param alteration
     * @return 
     */
    public Alteration findAlteration(int entrezGeneId, String alteration) {
        return getDao().findAlteration(entrezGeneId, alteration);
    }
}
