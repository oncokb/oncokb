package org.mskcc.cbio.oncokb.model;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Gene info without summary and status. Designed for public website.
 * @author Hongxin
 */
public class ShortGene implements Serializable {

    private int entrezGeneId;
    private String hugoSymbol;
    private String name;

    private Set<String> geneLabels = new HashSet<String>(0);
    private Set<String> geneAliases = new HashSet<String>(0);

    public ShortGene() {
    }


    public ShortGene(int entrezGeneId, String hugoSymbol, String name) {
        this.entrezGeneId = entrezGeneId;
        this.hugoSymbol = hugoSymbol;
        this.name = name;
    }

    public ShortGene(int entrezGeneId, String hugoSymbol, String name, String summary, Set<String> geneLabels, Set<String> geneAliases) {
        this.entrezGeneId = entrezGeneId;
        this.hugoSymbol = hugoSymbol;
        this.name = name;
        this.geneLabels = geneLabels;
        this.geneAliases = geneAliases;
    }


    public int getEntrezGeneId() {
        return this.entrezGeneId;
    }

    public void setEntrezGeneId(int entrezGeneId) {
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

    public Set<String> getGeneLabels() {
        return this.geneLabels;
    }

    public void setGeneLabels(Set<String> geneLabels) {
        this.geneLabels = geneLabels;
    }

    public Set<String> getGeneAliases() {
        return this.geneAliases;
    }


    public void setGeneAliases(Set<String> geneAliases) {
        this.geneAliases = geneAliases;
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
        final ShortGene other = (ShortGene) obj;
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


