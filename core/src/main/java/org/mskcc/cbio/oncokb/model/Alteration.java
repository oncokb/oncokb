package org.mskcc.cbio.oncokb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

import java.util.HashSet;
import java.util.Set;


/**
 * 
 * @author jgao
 */
public class Alteration implements java.io.Serializable {

     private Integer alterationId;
     private Gene gene;
     private String alteration;
     private AlterationType alterationType;

    public Alteration() {
    }

	
    public Alteration(Gene gene, String alteration, AlterationType alterationType) {
        this.gene = gene;
        this.alteration = alteration;
        this.alterationType = alterationType;
    }
   
    
    public Integer getAlterationId() {
        return this.alterationId;
    }
    
    
    public void setAlterationId(Integer alterationId) {
        this.alterationId = alterationId;
    }
    
    public Gene getGene() {
        return this.gene;
    }
    
    
    public void setGene(Gene gene) {
        this.gene = gene;
    }
    
    public String getAlteration() {
        return this.alteration;
    }
    
    
    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }
    
    public AlterationType getAlterationType() {
        return this.alterationType;
    }
    
    
    public void setAlterationType(AlterationType alterationType) {
        this.alterationType = alterationType;
    }

    
     @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.gene != null ? this.gene.hashCode() : 0);
        hash = 83 * hash + (this.alteration != null ? this.alteration.hashCode() : 0);
        hash = 83 * hash + (this.alterationType != null ? this.alterationType.hashCode() : 0);
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
        final Alteration other = (Alteration) obj;
        if (this.gene != other.gene && (this.gene == null || !this.gene.equals(other.gene))) {
            return false;
        }
        if ((this.alteration == null) ? (other.alteration != null) : !this.alteration.equals(other.alteration)) {
            return false;
        }
        if ((this.alterationType == null) ? (other.alterationType != null) : !this.alterationType.equals(other.alterationType)) {
            return false;
        }
        return true;
    }
    
    
}


