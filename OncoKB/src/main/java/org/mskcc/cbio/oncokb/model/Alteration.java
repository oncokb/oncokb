package org.mskcc.cbio.oncokb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

/**
 * 
 * @author jgao
 */
public class Alteration implements java.io.Serializable {


     private Integer alterationId;
     private Gene gene;
     private String alteration;
     private String type;

    public Alteration() {
    }

	
    public Alteration(Gene gene, String alteration, String type) {
        this.gene = gene;
        this.alteration = alteration;
        this.type = type;
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
    
    public String getType() {
        return this.type;
    }
    
    
    public void setType(String type) {
        this.type = type;
    }

    
     @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.gene != null ? this.gene.hashCode() : 0);
        hash = 83 * hash + (this.alteration != null ? this.alteration.hashCode() : 0);
        hash = 83 * hash + (this.type != null ? this.type.hashCode() : 0);
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
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        return true;
    }
    
    
}


