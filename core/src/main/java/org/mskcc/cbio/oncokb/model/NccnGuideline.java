

package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mysql.jdbc.StringUtils;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author jgao
 */
@NamedQueries({
    @NamedQuery(
        name = "findNccnGuideline",
        query = "select n from NccnGuideline n where n.therapy=? and n.disease=? and n.version=? and n.pages=?"
    )
})

@Entity
@Table(name = "nccn_guideline")
public class NccnGuideline implements java.io.Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @JsonIgnore
    @Column(length = 40)
    private String uuid;
    private String therapy;
    private String disease;
    private String version;
    private String pages;
    private String category;

    @Column(length = 65535)
    private String description;

    @Column(name = "additional_info", length = 65535)
    private String additionalInfo;

    public NccnGuideline() {
    }

    public Integer getId() {
        return id;
    }

    public String getTherapy() {
        return therapy;
    }

    public void setTherapy(String therapy) {
        this.therapy = therapy;
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

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.id);
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
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        if (StringUtils.isNullOrEmpty(this.therapy) && StringUtils.isNullOrEmpty(this.disease) && StringUtils.isNullOrEmpty(this.version)
            && StringUtils.isNullOrEmpty(this.pages) && StringUtils.isNullOrEmpty(this.category) && StringUtils.isNullOrEmpty(this.description))
            return true;
        else return false;
    }


}
