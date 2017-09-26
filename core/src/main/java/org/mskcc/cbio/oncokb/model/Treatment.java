package org.mskcc.cbio.oncokb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * @author jgao, Hongxin Zhang
 */

@Entity
@Table(name = "treatment")
public class Treatment implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @JsonIgnore
    @Column(length = 40)
    private String uuid;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "treatment_drug", joinColumns = {
        @JoinColumn(name = "treatment_id", nullable = false, updatable = false)},
        inverseJoinColumns = {@JoinColumn(name = "drug_id",
            nullable = false, updatable = false)})
    private Set<Drug> drugs = new HashSet<>(0);

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "treatment_approved_indications",
        joinColumns = @JoinColumn(name = "treatment_id", nullable = false))
    @Column(name = "approved_indications")
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


