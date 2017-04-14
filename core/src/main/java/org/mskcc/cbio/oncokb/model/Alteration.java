package org.mskcc.cbio.oncokb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.VariantConsequenceUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author jgao
 */
public class Alteration implements java.io.Serializable {

    @JsonIgnore
    private Integer id;
    @JsonIgnore
    private String uuid;
    private Gene gene;
    @JsonIgnore
    private AlterationType alterationType;
    private VariantConsequence consequence;

    private String alteration;
    private String name;
    private String refResidues;
    private Integer proteinStart;
    private Integer proteinEnd;
    private String variantResidues;
    @JsonIgnore
    private Set<PortalAlteration> portalAlterations = new HashSet<PortalAlteration>(0);

    public Set<PortalAlteration> getPortalAlterations() {
        return portalAlterations;
    }

    public void setPortalAlterations(Set<PortalAlteration> portalAlterations) {
        this.portalAlterations = portalAlterations;
    }

    public Alteration() {
    }

    public Alteration(Query query) {
        if (query.getEntrezGeneId() != null) {
            this.gene = GeneUtils.getGeneByEntrezId(query.getEntrezGeneId());
        }
        if (this.gene == null && query.getHugoSymbol() != null) {
            this.gene = GeneUtils.getGeneByHugoSymbol(query.getHugoSymbol());
        }
        this.alteration = query.getAlteration();
        this.proteinEnd = query.getProteinEnd();
        this.proteinStart = query.getProteinStart();
        if (query.getConsequence() != null) {
            this.consequence = VariantConsequenceUtils.findVariantConsequenceByTerm(query.getConsequence());
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlterationType getAlterationType() {
        return this.alterationType;
    }


    public void setAlterationType(AlterationType alterationType) {
        this.alterationType = alterationType;
    }

    public VariantConsequence getConsequence() {
        return consequence;
    }

    public void setConsequence(VariantConsequence consequence) {
        this.consequence = consequence;
    }

    public String getRefResidues() {
        return refResidues;
    }

    public void setRefResidues(String refResidues) {
        this.refResidues = refResidues;
    }

    public Integer getProteinStart() {
        return proteinStart;
    }

    public void setProteinStart(Integer proteinStart) {
        this.proteinStart = proteinStart;
    }

    public Integer getProteinEnd() {
        return proteinEnd;
    }

    public void setProteinEnd(Integer proteinEnd) {
        this.proteinEnd = proteinEnd;
    }

    public String getVariantResidues() {
        return variantResidues;
    }

    public void setVariantResidues(String variantResidues) {
        this.variantResidues = variantResidues;
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

    @Override
    public String toString() {
        return gene + " " + alteration;
    }
}


