package org.mskcc.cbio.oncokb.apiModels.annotation;

import java.util.Objects;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class QueryGene implements java.io.Serializable {
    private Integer entrezGeneId;
    private String hugoSymbol;

    public QueryGene() {
    }

    public QueryGene(Integer entrezGeneId, String hugoSymbol) {
        this.entrezGeneId = entrezGeneId;
        this.hugoSymbol = hugoSymbol;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntrezGeneId(), getHugoSymbol());
    }
}
