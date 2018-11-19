/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import java.util.List;
import org.mskcc.cbio.oncokb.model.Drug;

/**
 * handling db requests for gene, gene_alias, and gene_label
 * @author jgao
 */
public interface DrugDao extends GenericDao<Drug, Integer> {
    Drug findDrugById(Integer id);
    /**
     *
     * @param drugName
     * @return
     */
    Drug findDrugByName(String drugName);

    /**
     *
     * @param synonym
     * @return
     */
    List<Drug> findDrugBySynonym(String synonym);

    /**
     *
     * @param ncitCode
     * @return
     */
    Drug findDrugByNcitCode(String ncitCode);

    @Override
    void save(Drug drug);
}
