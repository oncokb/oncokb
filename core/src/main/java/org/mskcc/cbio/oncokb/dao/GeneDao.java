/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.Gene;

import java.util.List;

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

    /**
     *  Get genes by alias
     * @param geneAlias
     * @return
     */
    Gene findGeneByAlias(String geneAlias);
}
