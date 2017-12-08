package org.mskcc.cbio.oncokb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA


import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jgao, Hongxin ZHang
 */

@NamedQueries({
    @NamedQuery(
        name = "findGeneByHugoSymbol",
        query = "select g from Gene g where g.hugoSymbol=?"
    ),
    @NamedQuery(
        name = "findGenesByAlias",
        query = "select g from Gene g join g.geneAliases ga where ga=?"
    )
})

@Entity
@Table(name = "gene")
public class Gene implements Serializable {

    @Id
    @Column(name = "entrez_gene_id")
    private Integer entrezGeneId;

    @Column(name = "hugo_symbol", length = 50, nullable = true, unique = true)
    private String hugoSymbol;

    @Column(length = 500)
    private String name;

    @Column()
    @ApiModelProperty(value = "tumorSuppressorGene")
    private Boolean TSG;
    private Boolean oncogene;

    @Column(length = 100)
    private String curatedIsoform;

    @Column(length = 100)
    private String curatedRefSeq;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gene_alias", joinColumns = @JoinColumn(name = "entrez_gene_id", nullable = false))
    @Column(name = "alias")
    private Set<String> geneAliases = new HashSet<String>(0);

    public Gene() {
    }


    public Gene(int entrezGeneId, String hugoSymbol, String name) {
        this.entrezGeneId = entrezGeneId;
        this.hugoSymbol = hugoSymbol;
        this.name = name;
    }

    public Gene(int entrezGeneId, String hugoSymbol, String name, String summary, Set<String> geneLabels, Set<String> geneAliases) {
        this.entrezGeneId = entrezGeneId;
        this.hugoSymbol = hugoSymbol;
        this.name = name;
        this.geneAliases = geneAliases;
    }


    public Integer getEntrezGeneId() {
        return this.entrezGeneId;
    }


    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHugoSymbol() {
        return this.hugoSymbol;
    }


    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getGeneAliases() {
        return this.geneAliases;
    }


    public void setGeneAliases(Set<String> geneAliases) {
        this.geneAliases = geneAliases;
    }

    public Boolean getTSG() {
        return TSG;
    }

    public void setTSG(Boolean TSG) {
        this.TSG = TSG;
    }

    public Boolean getOncogene() {
        return oncogene;
    }

    public void setOncogene(Boolean oncogene) {
        this.oncogene = oncogene;
    }

    public String getCuratedIsoform() {
        return curatedIsoform;
    }

    public void setCuratedIsoform(String curatedIsoform) {
        this.curatedIsoform = curatedIsoform;
    }

    public String getCuratedRefSeq() {
        return curatedRefSeq;
    }

    public void setCuratedRefSeq(String curatedRefSeq) {
        this.curatedRefSeq = curatedRefSeq;
    }

    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.entrezGeneId;
        return hash;
    }


    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Gene other = (Gene) obj;
        if (this.entrezGeneId != other.entrezGeneId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return hugoSymbol;
    }

}

