package org.mskcc.cbio.oncokb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * @author jgao
 */
public class Treatment implements java.io.Serializable {

    @JsonIgnore
    private Integer id;
    @JsonIgnore
    private String uuid;
    private Set<Drug> drugs = new HashSet<Drug>(0);
    private Set<String> approvedIndications = new HashSet<String>(0);

    public Treatment() {
    }

    public Integer getId() {
        return id;
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

    public Set<Drug> getDrugs() {
        return drugs;
    }

    public void setDrugs(Set<Drug> drugs) {
        this.drugs = drugs;
    }

    public Set<String> getApprovedIndications() {
        return approvedIndications;
    }

    public void setApprovedIndications(Set<String> approvedIndications) {
        this.approvedIndications = approvedIndications;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.drugs);
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
        final Treatment other = (Treatment) obj;
        if (!Objects.equals(this.drugs, other.drugs)) {
            return false;
        }
        return true;
    }


}


