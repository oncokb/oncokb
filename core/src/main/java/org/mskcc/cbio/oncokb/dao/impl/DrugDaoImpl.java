/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import org.mskcc.cbio.oncokb.dao.DrugDao;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.util.CacheUtils;

import java.util.List;

/**
 * handling db requests for gene, gene_alias, and gene_label
 *
 * @author jgao
 */
public class DrugDaoImpl extends GenericDaoImpl<Drug, Integer> implements DrugDao {
    @Override
    public Drug findDrugById(Integer id) {
        List<Drug> list = findByNamedQuery("findDrugById", id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * @param drugName
     * @return
     */
    public Drug findDrugByName(String drugName) {
        List<Drug> list = findByNamedQuery("findDrugByName", drugName);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Drug> findDrugBySynonym(String synonym) {
        return findByNamedQuery("findDrugBySynonym", synonym);
    }

    @Override
    public Drug findDrugByNcitCode(String ncitCode) {
        List<Drug> list = findByNamedQuery("findDrugByNcitCode", ncitCode);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void save(Drug drug) {
        super.save(drug);
        CacheUtils.addDrug(drug);
    }
}
