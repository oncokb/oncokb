package org.mskcc.cbio.oncokb.model;



import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.serializer.SetGenesetInGeneConverter;

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

    @Column(name = "hugo_symbol", length = 50, unique = true)
    private String hugoSymbol;

    @Column(name = "tsg")
    @ApiModelProperty(value = "tumorSuppressorGene")
    private Boolean TSG;
    private Boolean oncogene;

    @Column(name = "grch37_isoform", length = 100)
    private String grch37Isoform;

    @Column(name = "grch37_ref_seq", length = 100)
    private String grch37RefSeq;

    @Column(name = "grch38_isoform", length = 100)
    private String grch38Isoform;

    @Column(name = "grch38_ref_seq", length = 100)
    private String grch38RefSeq;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gene_alias", joinColumns = @JoinColumn(name = "entrez_gene_id", nullable = false))
    @Column(name = "alias")
    private Set<String> geneAliases = new HashSet<String>(0);

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "genes")
    @JsonSerialize(converter = SetGenesetInGeneConverter.class)
    private Set<Geneset> genesets = new HashSet<>();

    public Gene() {
    }


    public Gene(int entrezGeneId, String hugoSymbol) {
        this.entrezGeneId = entrezGeneId;
        this.hugoSymbol = hugoSymbol;
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

    public String getGrch37Isoform() {
        return grch37Isoform;
    }

    public void setGrch37Isoform(String grch37Isoform) {
        this.grch37Isoform = grch37Isoform;
    }

    public String getGrch37RefSeq() {
        return grch37RefSeq;
    }

    public void setGrch37RefSeq(String grch37RefSeq) {
        this.grch37RefSeq = grch37RefSeq;
    }

    public String getGrch38Isoform() {
        return grch38Isoform;
    }

    public void setGrch38Isoform(String grch38Isoform) {
        this.grch38Isoform = grch38Isoform;
    }

    public String getGrch38RefSeq() {
        return grch38RefSeq;
    }

    public void setGrch38RefSeq(String grch38RefSeq) {
        this.grch38RefSeq = grch38RefSeq;
    }

    public Set<Geneset> getGenesets() {
        return genesets;
    }

    public void setGenesets(Set<Geneset> genesets) {
        this.genesets = genesets;
    }

    @Override
    public int hashCode() {
        int result = getEntrezGeneId() != null ? getEntrezGeneId().hashCode() : 0;
        result = 31 * result + (getHugoSymbol() != null ? getHugoSymbol().hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gene)) return false;

        Gene gene = (Gene) o;

        return getEntrezGeneId() != null ? getEntrezGeneId().equals(gene.getEntrezGeneId()) : gene.getEntrezGeneId() == null;
    }

    @Override
    public String toString() {
        return hugoSymbol;
    }

}

