/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.GeneDao;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 * handling db requests for gene, gene_alias, and gene_label
 * @author jgao
 */
public class GeneDaoImpl extends GenericDaoImpl<Gene, Integer> implements GeneDao {
    
    /**
     * Get a gene by hugo symbol
     * @param symbol
     * @return gene object or null
     */
    public Gene findGeneByHugoSymbol(String symbol) {
        List<Gene> list = findByNamedQuery("findGeneByHugoSymbol", symbol);
        return list.isEmpty() ? null : list.get(0);
    }
    
    /**
     * Get a gene by Entrez Gene Id.
     * @param entrezGeneId
     * @return gene object or null.
     */
    public Gene findGeneByEntrezGeneId(int entrezGeneId) {
        return findById(entrezGeneId);
    }
}
