/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public interface Drug {

    Integer getDrugId();

    String getDrugName();

    String getSynonyms();

    boolean isFdaApproved();

    void setDrugId(Integer drugId);

    void setDrugName(String drugName);

    void setFdaApproved(boolean fdaApproved);

    void setSynonyms(String synonyms);
    
}
