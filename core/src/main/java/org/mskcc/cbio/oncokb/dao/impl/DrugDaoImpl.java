/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.DrugDao;
import org.mskcc.cbio.oncokb.model.Drug;

/**
 * handling db requests for gene, gene_alias, and gene_label
 * @author jgao
 */
public class DrugDaoImpl extends GenericDaoImpl<Drug, Integer> implements DrugDao {
    
    /**
     * 
     * @param drugName
     * @return 
     */
    public Drug findDrugByName(String drugName) {
        List<Drug> list = findByNamedQuery("findDrugByName", drugName);
        return list.isEmpty() ? null : list.get(0);
    }
}
