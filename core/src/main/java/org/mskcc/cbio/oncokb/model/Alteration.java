package org.mskcc.cbio.oncokb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jgao
 */

@NamedQueries({
    @NamedQuery(
        name = "findAlterationsByGene",
        query = "select a from Alteration a where a.gene=?"
    ),
    @NamedQuery(
        name = "findAlteration",
        query = "select a from Alteration a where a.gene=? and a.alteration=?"
    ),
    @NamedQuery(
        name = "findMutationsByConsequence",
        query = "select a from Alteration a where a.gene=? and a.consequence=?"
    ),
    @NamedQuery(
        name = "findMutationsByConsequenceAndPosition",
        query = "select a from Alteration a where a.gene=? and a.consequence=? and a.proteinStart<=? and a.proteinEnd>=?"
    )
})

@Entity
@Table(name = "alteration")
public class Alteration implements java.io.Serializable {

    @Id
    @JsonIgnore
    private Integer id;

    @Column(length = 40)
    @JsonIgnore
    private String uuid;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "entrez_gene_id")
    private Gene gene;

    @Column(name = "alteration_type")
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private AlterationType alterationType;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "consequence")
    private VariantConsequence consequence;

    private String alteration;

    @Column(length = 300, nullable = false)
    private String name;

    @Column(name = "ref_residues")
    private String refResidues;

    @Column(name = "protein_start")
    private Integer proteinStart;

    @Column(name = "protein_end")
    private Integer proteinEnd;

    @Column(name = "variant_residues")
    private String variantResidues;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "portalAlt_oncoKBAlt", joinColumns = {
        @JoinColumn(name = "alteration_id", nullable = false, updatable = false)},
        inverseJoinColumns = {@JoinColumn(name = "portalAlteration_id",
            nullable = false, updatable = false)})
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

    @JsonIgnore
    public String getUniqueId() {
        List<String> content = new ArrayList<>();
        if (this.gene != null) {
            if (this.gene.getHugoSymbol() != null) {
                content.add(this.gene.getHugoSymbol());
            } else {
                content.add("");
            }
        }
        if (this.alteration != null) {
            content.add(this.alteration);
        } else {
            content.add("");
        }
        if (this.name != null) {
            content.add(this.name);
        } else {
            content.add("");
        }
        if (this.alterationType != null) {
            content.add(this.alterationType.name());
        } else {
            content.add("");
        }
        if (this.consequence != null) {
            content.add(this.consequence.getTerm());
        } else {
            content.add("");
        }
        if (this.proteinStart != null) {
            content.add(Integer.toString(this.proteinStart));
        } else {
            content.add("");
        }
        if (this.proteinEnd != null) {
            content.add(Integer.toString(this.proteinEnd));
        } else {
            content.add("");
        }
        if (this.refResidues != null) {
            content.add(this.refResidues);
        } else {
            content.add("");
        }
        if (this.variantResidues != null) {
            content.add(this.variantResidues);
        } else {
            content.add("");
        }

        return StringUtils.join(content.toArray(), "&");
    }
}


