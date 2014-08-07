/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.dao.DrugDao;
import org.mskcc.cbio.oncokb.model.Drug;

/**
 *
 * @author jgao
 */
public class DrugBoImpl extends GenericBoImpl<Drug, DrugDao> implements DrugBo {
    @Override
    public Drug findDrugByName(String drugName) {
        return getDao().findDrugByName(drugName);
    }
    
    @Override
    public List<Drug> findDrugsByName(Collection<String> drugNames) {
        List<Drug> drugs = new ArrayList<Drug>();
        for (String drugName : drugNames) {
            Drug drug = getDao().findDrugByName(drugName);
            if (drug != null) {
                drugs.add(drug);
            }
        }
        return drugs;
    }
}
