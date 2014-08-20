package org.mskcc.cbio.oncokb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

import java.util.HashSet;
import java.util.Set;


/**
 * 
 * @author jgao
 */
public class Drug implements java.io.Serializable {


     private Integer drugId;
     private String drugName;
     private Set<String> synonyms = new HashSet<String>(0);
     private Boolean fdaApproved;
     private Set<String> atcCodes;
     private String description;

    public Drug() {
    }

    public Drug(String drugName) {
        this.drugName = drugName;
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
    
    public Set<String> getSynonyms() {
        return this.synonyms;
    }
    
    
    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }
    
    public Boolean isFdaApproved() {
        return this.fdaApproved;
    }
    
    
    public void setFdaApproved(Boolean fdaApproved) {
        this.fdaApproved = fdaApproved;
    }

    
     @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.drugName != null ? this.drugName.hashCode() : 0);
        return hash;
    }

    public Set<String> getAtcCodes() {
        return atcCodes;
    }

    public void setAtcCodes(Set<String> atcCodes) {
        this.atcCodes = atcCodes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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


