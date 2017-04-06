/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
import java.util.List;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 * Gene business object (BO) interface and implementation, it's used to store
 * the project's business function, the real database operations (CRUD) works
 * should not involved in this class, instead it has a DAO (GeneDao) class to do it.
 * @author jgao
 */
public interface GeneBo extends GenericBo<Gene> {
    /**
     * Gene genes by hugo symbol
     * @param symbols
     * @return a list of genes
     */
    List<Gene> findGenesByHugoSymbol(Collection<String> symbols);

    /**
     *
     * @param symbol
     * @return
     */
    Gene findGeneByHugoSymbol(String symbol);

    /**
     * Get genes by Entrez Gene Id.
     * @param entrezGeneIds
     * @return a list of genes
     */
    List<Gene> findGenesByEntrezGeneId(Collection<Integer> entrezGeneIds);

    /**
     *
     * @param entrezGeneId
     * @return
     */
    Gene findGeneByEntrezGeneId(int entrezGeneId);

    /**
     * Get gene by gene alias
     * @param geneAlias
     * @return
     */
    Gene findGeneByAlias(String geneAlias);
}
