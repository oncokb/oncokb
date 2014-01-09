package org.mskcc.cbio.oncokb.model.impl;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

import org.mskcc.cbio.oncokb.model.Drug;

/**
 * 
 * @author jgao
 */
public class DrugImpl  implements java.io.Serializable, Drug {


     private Integer drugId;
     private String drugName;
     private String synonyms;
     private boolean fdaApproved;

    public DrugImpl() {
    }

    public DrugImpl(String drugName, boolean fdaApproved) {
        this.drugName = drugName;
        this.fdaApproved = fdaApproved;
    }
    public DrugImpl(String drugName, String synonyms, boolean fdaApproved) {
       this.drugName = drugName;
       this.synonyms = synonyms;
       this.fdaApproved = fdaApproved;
    }
   
    @Override
    public Integer getDrugId() {
        return this.drugId;
    }
    
    @Override
    public void setDrugId(Integer drugId) {
        this.drugId = drugId;
    }
    @Override
    public String getDrugName() {
        return this.drugName;
    }
    
    @Override
    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }
    @Override
    public String getSynonyms() {
        return this.synonyms;
    }
    
    @Override
    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }
    @Override
    public boolean isFdaApproved() {
        return this.fdaApproved;
    }
    
    @Override
    public void setFdaApproved(boolean fdaApproved) {
        this.fdaApproved = fdaApproved;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.drugName != null ? this.drugName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DrugImpl other = (DrugImpl) obj;
        if ((this.drugName == null) ? (other.drugName != null) : !this.drugName.equals(other.drugName)) {
            return false;
        }
        return true;
    }
    
    
}


