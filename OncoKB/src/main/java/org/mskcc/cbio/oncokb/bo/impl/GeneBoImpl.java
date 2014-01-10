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
    
    public Gene findGeneByHugoSymbol(String symbol) {
        return getDao().findGeneByHugoSymbol(symbol);
    }

    public Gene findGeneByEntrezGeneId(int entrezGeneId) {
        return getDao().findGeneByEntrezGeneId(entrezGeneId);
    }
    
}
