/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
import java.util.List;
import org.mskcc.cbio.oncokb.model.Drug;

/**
 * Gene business object (BO) interface and implementation, it's used to store
 * the project's business function, the real database operations (CRUD) works
 * should not involved in this class, instead it has a DAO (GeneDao) class to do it.
 * @author jgao
 */
public interface DrugBo extends GenericBo<Drug> {

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
    List<Drug> findDrugsBySynonym(String synonym);

    /**
     *
     * @param ncitCode
     * @return
     */
    Drug findDrugsByNcitCode(String ncitCode);

    /**
     *
     * @param drugNameOrSynonym
     * @return
     */
    List<Drug> guessDrugs(String drugNameOrSynonym);

    /**
     *
     * @param drugNameOrSynonym
     * @return
     */
    Drug guessUnambiguousDrug(String drugNameOrSynonym);
}
