package org.mskcc.cbio.oncogkb.model.impl;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA


import org.mskcc.cbio.oncogkb.model.Gene;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author jgao
 */
public class GeneImpl  implements java.io.Serializable, Gene {

     private int entrezGeneId;
     private String hugoSymbol;
     private String name;
     private String summary;
     
     private Set<String> geneLabels = new HashSet<String>(0);
     private Set<String> geneAliases = new HashSet<String>(0);

    public GeneImpl() {
    }

	
    public GeneImpl(int entrezGeneId, String hugoSymbol, String name) {
        this.entrezGeneId = entrezGeneId;
        this.hugoSymbol = hugoSymbol;
        this.name = name;
    }
    public GeneImpl(int entrezGeneId, String hugoSymbol, String name, String summary, Set<String> geneLabels, Set<String> geneAliases) {
       this.entrezGeneId = entrezGeneId;
       this.hugoSymbol = hugoSymbol;
       this.name = name;
       this.summary = summary;
       this.geneLabels = geneLabels;
       this.geneAliases = geneAliases;
    }
   
    @Override
    public int getEntrezGeneId() {
        return this.entrezGeneId;
    }
    
    @Override
    public void setEntrezGeneId(int entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }
    @Override
    public String getHugoSymbol() {
        return this.hugoSymbol;
    }
    
    @Override
    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String getSummary() {
        return this.summary;
    }
    
    @Override
    public void setSummary(String summary) {
        this.summary = summary;
    }
    @Override
    public Set<String> getGeneLabels() {
        return this.geneLabels;
    }
    
    @Override
    public void setGeneLabels(Set<String> geneLabels) {
        this.geneLabels = geneLabels;
    }
    @Override
    public Set<String> getGeneAliases() {
        return this.geneAliases;
    }
    
    @Override
    public void setGeneAliases(Set<String> geneAliases) {
        this.geneAliases = geneAliases;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.entrezGeneId;
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
        final GeneImpl other = (GeneImpl) obj;
        if (this.entrezGeneId != other.entrezGeneId) {
            return false;
        }
        return true;
    }


}


