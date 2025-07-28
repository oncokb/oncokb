package org.mskcc.cbio.oncokb.model.health;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InMemoryCacheSizes {
    private Integer genesCacheSize;
    private Integer alterationsCacheSize;
    private Integer drugsCacheSize;
    private Integer cancerTypesCacheSize;
    private Integer geneEvidencesCacheSize;


    public InMemoryCacheSizes(Integer genesCacheSize, Integer alterationsCacheSize, Integer drugsCacheSize, Integer cancerTypesCacheSize, Integer geneEvidencesCacheSize) {
        this.genesCacheSize = genesCacheSize;
        this.alterationsCacheSize = alterationsCacheSize;
        this.drugsCacheSize = drugsCacheSize;
        this.cancerTypesCacheSize = cancerTypesCacheSize;
        this.geneEvidencesCacheSize = geneEvidencesCacheSize;
    }


    public Integer getGenesCacheSize() {
        return this.genesCacheSize;
    }

    public void setGenesCacheSize(Integer genesCacheSize) {
        this.genesCacheSize = genesCacheSize;
    }

    public Integer getAlterationsCacheSize() {
        return this.alterationsCacheSize;
    }

    public void setAlterationsCacheSize(Integer alterationsCacheSize) {
        this.alterationsCacheSize = alterationsCacheSize;
    }

    public Integer getDrugsCacheSize() {
        return this.drugsCacheSize;
    }

    public void setDrugsCacheSize(Integer drugsCacheSize) {
        this.drugsCacheSize = drugsCacheSize;
    }

    public Integer getCancerTypesCacheSize() {
        return this.cancerTypesCacheSize;
    }

    public void setCancerTypesCacheSize(Integer cancerTypesCacheSize) {
        this.cancerTypesCacheSize = cancerTypesCacheSize;
    }

    public Integer getGeneEvidencesCacheSize() {
        return this.geneEvidencesCacheSize;
    }

    public void setGeneEvidencesCacheSize(Integer geneEvidencesCacheSize) {
        this.geneEvidencesCacheSize = geneEvidencesCacheSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InMemoryCacheSizes that = (InMemoryCacheSizes) obj;
        return Objects.equals(genesCacheSize, that.genesCacheSize) &&
            Objects.equals(alterationsCacheSize, that.alterationsCacheSize) &&
            Objects.equals(drugsCacheSize, that.drugsCacheSize) &&
            Objects.equals(cancerTypesCacheSize, that.cancerTypesCacheSize) &&
            Objects.equals(geneEvidencesCacheSize, that.geneEvidencesCacheSize);
    }

    public List<String> getDifferentCacheSizes(InMemoryCacheSizes other) {
        List<String> differences = new ArrayList<>();
        
        if (other == null) {
            differences.add("Other cache object is null");
            return differences;
        }
    
        if (!Objects.equals(this.genesCacheSize, other.genesCacheSize)) {
            differences.add("Genes cache: Expected " + this.genesCacheSize + ", but is " + other.genesCacheSize);
        }
        if (!Objects.equals(this.alterationsCacheSize, other.alterationsCacheSize)) {
            differences.add("Alterations cache: Expected " + this.alterationsCacheSize + ", but is " + other.alterationsCacheSize);
        }
        if (!Objects.equals(this.drugsCacheSize, other.drugsCacheSize)) {
            differences.add("Drugs cache: Expected " + this.drugsCacheSize + ", but is " + other.drugsCacheSize);
        }
        if (!Objects.equals(this.cancerTypesCacheSize, other.cancerTypesCacheSize)) {
            differences.add("Cancer Types cache: Expected " + this.cancerTypesCacheSize + ", but is " + other.cancerTypesCacheSize);
        }
        if (!Objects.equals(this.geneEvidencesCacheSize, other.geneEvidencesCacheSize)) {
            differences.add("Gene-based Evidences cache: Expected " + this.geneEvidencesCacheSize + ", but is " + other.geneEvidencesCacheSize);
        }
    
        return differences;
    }

}
