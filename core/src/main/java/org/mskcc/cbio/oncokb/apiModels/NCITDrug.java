package org.mskcc.cbio.oncokb.apiModels;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Hongxin Zhang
 */


public class NCITDrug implements java.io.Serializable {

    private String ncitCode;
    private String drugName;
    private Set<String> synonyms = new HashSet<String>(0);
    private String description;

    public NCITDrug() {
    }

    public NCITDrug(String drugName) {
        this.drugName = drugName;
    }

    public String getNcitCode() {
        return ncitCode;
    }

    public void setNcitCode(String ncitCode) {
        this.ncitCode = ncitCode;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


