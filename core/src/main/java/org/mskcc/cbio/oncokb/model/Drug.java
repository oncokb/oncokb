package org.mskcc.cbio.oncokb.model;
// Generated Dec 19, 2013 1:33:26 AM by Hibernate Tools 3.2.1.GA

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


/**
 * @author jgao, Hongxin Zhang
 */

@NamedQueries({
    @NamedQuery(
        name = "findDrugByName",
        query = "select d from Drug d where d.drugName=?"
    ),
    @NamedQuery(
        name = "findDrugBySynonym",
        query = "select d from Drug d join d.synonyms s where s=?"
    ),
    @NamedQuery(
        name = "findDrugByAtcCode",
        query = "select d from Drug d join d.atcCodes a where a=?"
    )
})

@Entity
@Table(name = "drug")
public class Drug implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @JsonIgnore
    @Column(length = 40)
    private String uuid;

    @Column(name = "drug_name", nullable = false)
    private String drugName;

    @ElementCollection()
    @CollectionTable(name = "drug_synonym", joinColumns = @JoinColumn(name = "drug_id", nullable = false))
    private Set<String> synonyms = new HashSet<String>(0);

    @ElementCollection()
    @CollectionTable(name = "drug_atccode", joinColumns = @JoinColumn(name = "drug_id", nullable = false))
    @Column(name = "atccode")
    private Set<String> atcCodes;

    @JsonIgnore
    private String description;

    public Drug() {
    }

    public Drug(String drugName) {
        this.drugName = drugName;
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

    public String getDrugName() {
        return this.drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public Set<String> getSynonyms() {
        return this.synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.drugName != null ? this.drugName.hashCode() : 0);
        return hash;
    }

    public Set<String> getAtcCodes() {
        return atcCodes;
    }

    public void setAtcCodes(Set<String> atcCodes) {
        this.atcCodes = atcCodes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Drug other = (Drug) obj;
        if ((this.drugName == null) ? (other.drugName != null) : !this.drugName.equals(other.drugName)) {
            return false;
        }
        return true;
    }
}


