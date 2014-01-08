/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.bo.impl;

import java.util.Collection;
import org.mskcc.cbio.oncogkb.bo.GeneBo;
import org.mskcc.cbio.oncogkb.dao.GeneDao;
import org.mskcc.cbio.oncogkb.model.Gene;
import org.mskcc.cbio.oncogkb.model.GeneAlias;
import org.mskcc.cbio.oncogkb.model.GeneLabel;

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

    public GeneAlias getGeneAlias(int entrezGeneId, String alias) {
        return geneDao.getGeneAlias(entrezGeneId, alias);
    }

    public GeneLabel getGeneLabel(int entrezGeneId, String label) {
        return geneDao.getGeneLabel(entrezGeneId, label);
    }

    public void saveGene(Gene gene) {
        geneDao.saveGene(gene);
    }

    public void saveGenes(Collection<Gene> genes) {
        geneDao.saveGenes(genes);
    }
    
}
