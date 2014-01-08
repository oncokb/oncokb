/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.dao.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.mskcc.cbio.oncogkb.dao.GeneDao;
import org.mskcc.cbio.oncogkb.model.Gene;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * handling db requests for gene, gene_alias, and gene_label
 * @author jgao
 */
public class GeneDaoImpl extends HibernateDaoSupport implements GeneDao {
    
    /**
     * Get a gene by hugo symbol
     * @param symbol
     * @return gene object or null
     */
    public Gene getGeneByHugoSymbol(String symbol) {
        List list = getHibernateTemplate().find("from Gene as gene where gene.hugoSymbol=?", symbol);
        
        if (list==null || list.isEmpty()) {
            return null;
        }
        
        return Gene.class.cast(list.get(0));
    }
    
    /**
     * Get a gene by Entrez Gene Id.
     * @param entrezGeneId
     * @return gene object or null.
     */
    public Gene getGeneByEntrezGeneId(int entrezGeneId) {
        List list = getHibernateTemplate().find("from Gene as gene where gene.entrezGeneId=?",entrezGeneId);
        
        if (list==null || list.isEmpty()) {
            return null;
        }
        
        return Gene.class.cast(list.get(0));
    }
    
    /**
     * Save a gene to db.
     * @param gene 
     */
    public void saveGene(Gene gene) {
        saveGenes(Collections.singletonList(gene));
    }
    
    /**
     * Save genes to db.
     * @param genes 
     */
    public void saveGenes(Collection<Gene> genes) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        
        for (Gene gene : genes) {
            hibernateTemplate.saveOrUpdate(gene);
        }
        
    }
}
