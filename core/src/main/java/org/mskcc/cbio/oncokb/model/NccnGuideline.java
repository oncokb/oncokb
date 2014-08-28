

package org.mskcc.cbio.oncokb.model;

import java.util.Objects;

/**
 *
 * @author jgao
 */
public class NccnGuideline implements java.io.Serializable {
    private Integer nccnGuidelineId;
    private String disease;
    private String version;
    private String pages;
    private String category;
    private String description;

    public NccnGuideline() {
    }

    public Integer getNccnGuidelineId() {
        return nccnGuidelineId;
    }

    public void setNccnGuidelineId(Integer nccnGuidelineId) {
        this.nccnGuidelineId = nccnGuidelineId;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.disease);
        hash = 31 * hash + Objects.hashCode(this.version);
        hash = 31 * hash + Objects.hashCode(this.pages);
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
        final NccnGuideline other = (NccnGuideline) obj;
        if (!Objects.equals(this.disease, other.disease)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.pages, other.pages)) {
            return false;
        }
        return true;
    }
    
    
}
