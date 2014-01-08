/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.dao;

import java.util.Collection;
import org.mskcc.cbio.oncogkb.model.Gene;
import org.mskcc.cbio.oncogkb.model.GeneAlias;
import org.mskcc.cbio.oncogkb.model.GeneLabel;

/**
 * handling db requests for gene, gene_alias, and gene_label
 * @author jgao
 */
public interface GeneDao {
    /**
     * Get a gene by hugo symbol
     * @param symbol
     * @return gene object or null
     */
    Gene getGeneByHugoSymbol(String symbol);
    
    /**
     * Get a gene by Entrez Gene Id.
     * @param entrezGeneId
     * @return gene object or null.
     */
    Gene getGeneByEntrezGeneId(int entrezGeneId);
    
    /**
     * Get GeneAlias 
     * @param entrezGeneId
     * @param alias
     * @return 
     */
    GeneAlias getGeneAlias(int entrezGeneId, String alias);
    
    /**
     * get GeneLabel
     * @param entrezGeneId
     * @param label
     * @return 
     */
    GeneLabel getGeneLabel(int entrezGeneId, String label);
    
    /**
     * Save a gene to db.
     * @param gene 
     */
    void saveGene(Gene gene);
    
    /**
     * Save genes to db.
     * @param genes 
     */
    void saveGenes(Collection<Gene> genes);
}
