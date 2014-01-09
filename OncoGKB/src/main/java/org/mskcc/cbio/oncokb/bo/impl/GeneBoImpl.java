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
public class GeneBoImpl implements GeneBo {

    GeneDao geneDao;

    public void setGeneDao(GeneDao geneDao) {
        this.geneDao = geneDao;
    }
    
    public Gene getGeneByHugoSymbol(String symbol) {
        return geneDao.getGeneByHugoSymbol(symbol);
    }

    public Gene getGeneByEntrezGeneId(int entrezGeneId) {
        return geneDao.getGeneByEntrezGeneId(entrezGeneId);
    }

    public void saveOrUpdate(Gene gene) {
        geneDao.saveOrUpdate(gene);
    }
    
}
