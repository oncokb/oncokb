package org.mskcc.cbio.oncokb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CancerHotspot implements Serializable {

    private String hugoSymbol;
    private String residue;
    private String name;
    private String type;
    private Integer proteinStart;
    private Integer proteinEnd;
    private Integer tumorCount;
    private Set<String> pmids = new LinkedHashSet<>();
    private List<Alteration> curatedAlterations = new ArrayList<>();

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String getResidue() {
        return residue;
    }

    public void setResidue(String residue) {
        this.residue = residue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Integer getTumorCount() {
        return tumorCount;
    }

    public void setTumorCount(Integer tumorCount) {
        this.tumorCount = tumorCount;
    }

    public Set<String> getPmids() {
        return pmids;
    }

    public void setPmids(Set<String> pmids) {
        this.pmids = pmids;
    }

    public List<Alteration> getCuratedAlterations() {
        return curatedAlterations;
    }

    public void setCuratedAlterations(List<Alteration> curatedAlterations) {
        this.curatedAlterations = curatedAlterations;
    }
}
