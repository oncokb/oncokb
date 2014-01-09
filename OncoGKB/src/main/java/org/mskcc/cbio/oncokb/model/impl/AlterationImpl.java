package org.mskcc.cbio.oncokb.model.impl;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;


/**
 * 
 * @author jgao
 */
public class AlterationImpl  implements java.io.Serializable, Alteration {


     private Integer alterationId;
     private Gene gene;
     private String alteration;
     private String type;

    public AlterationImpl() {
    }

	
    public AlterationImpl(Gene gene, String alteration, String type) {
        this.gene = gene;
        this.alteration = alteration;
        this.type = type;
    }
   
    @Override
    public Integer getAlterationId() {
        return this.alterationId;
    }
    
    @Override
    public void setAlterationId(Integer alterationId) {
        this.alterationId = alterationId;
    }
    @Override
    public Gene getGene() {
        return this.gene;
    }
    
    @Override
    public void setGene(Gene gene) {
        this.gene = gene;
    }
    @Override
    public String getAlteration() {
        return this.alteration;
    }
    
    @Override
    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }
    @Override
    public String getType() {
        return this.type;
    }
    
    @Override
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
        final AlterationImpl other = (AlterationImpl) obj;
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


