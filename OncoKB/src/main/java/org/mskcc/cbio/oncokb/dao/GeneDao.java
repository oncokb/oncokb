/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.Gene;

/**
 * handling db requests for gene, gene_alias, and gene_label
 * @author jgao
 */
public interface GeneDao extends GenericDao<Gene, Integer> {
    /**
     * Get a gene by hugo symbol
     * @param symbol
     * @return gene object or null
     */
    Gene findGeneByHugoSymbol(String symbol);
    
    /**
     * Get a gene by Entrez Gene Id.
     * @param entrezGeneId
     * @return gene object or null.
     */
    Gene findGeneByEntrezGeneId(int entrezGeneId);
    
}
