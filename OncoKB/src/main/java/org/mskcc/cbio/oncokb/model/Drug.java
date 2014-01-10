package org.mskcc.cbio.oncokb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

/**
 * 
 * @author jgao
 */
public class Drug implements java.io.Serializable {


     private Integer drugId;
     private String drugName;
     private String synonyms;
     private boolean fdaApproved;

    public Drug() {
    }

    public Drug(String drugName, boolean fdaApproved) {
        this.drugName = drugName;
        this.fdaApproved = fdaApproved;
    }
    public Drug(String drugName, String synonyms, boolean fdaApproved) {
       this.drugName = drugName;
       this.synonyms = synonyms;
       this.fdaApproved = fdaApproved;
    }
   
    
    public Integer getDrugId() {
        return this.drugId;
    }
    
    
    public void setDrugId(Integer drugId) {
        this.drugId = drugId;
    }
    
    public String getDrugName() {
        return this.drugName;
    }
    
    
    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }
    
    public String getSynonyms() {
        return this.synonyms;
    }
    
    
    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }
    
    public boolean isFdaApproved() {
        return this.fdaApproved;
    }
    
    
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
        final Drug other = (Drug) obj;
        if ((this.drugName == null) ? (other.drugName != null) : !this.drugName.equals(other.drugName)) {
            return false;
        }
        return true;
    }
    
    
}


