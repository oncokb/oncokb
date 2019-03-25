package org.mskcc.cbio.oncokb.apiModels.annotation;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class GeneQueryPair {
    private Integer entrezGeneId;
    private String hugoSymbol;

    public GeneQueryPair(Integer entrezGeneId, String hugoSymbol) {
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
}
