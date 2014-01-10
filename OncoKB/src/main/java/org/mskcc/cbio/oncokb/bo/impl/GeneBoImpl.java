/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.dao.GeneDao;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 *
 * @author jgao
 */
public class GeneBoImpl extends GenericBoImpl<Gene, GeneDao> implements GeneBo {
    
    public Gene getGeneByHugoSymbol(String symbol) {
        return getDao().getGeneByHugoSymbol(symbol);
    }

    public Gene getGeneByEntrezGeneId(int entrezGeneId) {
        return getDao().getGeneByEntrezGeneId(entrezGeneId);
    }
    
}
