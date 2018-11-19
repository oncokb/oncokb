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
        name = "findDrugById",
        query = "select d from Drug d where d.id=?"
    ),
    @NamedQuery(
        name = "findDrugBySynonym",
        query = "select d from Drug d join d.synonyms s where s=?"
    ),
    @NamedQuery(
        name = "findDrugByNcitCode",
        query = "select d from Drug d where d.ncitCode=?"
    )
})

@Entity
@Table(name = "drug")
public class Drug implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, name = "ncit_code", nullable = false)
    private String ncitCode;

    @Column(length = 1000, name = "drug_name", nullable = false)
    private String drugName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "drug_synonym", joinColumns = @JoinColumn(name = "drug_id", nullable = false))
    @Column(length = 1000, name = "synonym")
    private Set<String> synonyms = new HashSet<String>(0);

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "drug_atccode", joinColumns = @JoinColumn(name = "drug_id", nullable = false))
    @Column(name = "atccode")
    private Set<String> atcCodes;

    @JsonIgnore
    @Column(length = 65535)
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

    public String getNcitCode() {
        return ncitCode;
    }

    public void setNcitCode(String ncitCode) {
        this.ncitCode = ncitCode;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Drug)) return false;

        Drug drug = (Drug) o;

        if (ncitCode != null ? !ncitCode.equals(drug.ncitCode) : drug.ncitCode != null) return false;
        return getDrugName().equals(drug.getDrugName());
    }

    @Override
    public int hashCode() {
        int result = ncitCode != null ? ncitCode.hashCode() : 0;
        result = 31 * result + getDrugName().hashCode();
        return result;
    }
}


