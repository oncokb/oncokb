package org.mskcc.cbio.oncokb.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * @author jgao, Hongxin Zhang
 */

@NamedQueries({
    @NamedQuery(
        name = "findDrugByName",
        query = "select d from Drug d where d.drugName=? and d.type = 'DRUG'"
    ),
    @NamedQuery(
        name = "findDrugById",
        query = "select d from Drug d where d.id=?"
    ),
    @NamedQuery(
        name = "findDrugByUuid",
        query = "select d from Drug d where d.uuid=?"
    ),
    @NamedQuery(
        name = "findDrugBySynonym",
        query = "select d from Drug d join d.synonyms s where s=? and d.type = 'DRUG'"
    ),
    @NamedQuery(
        name = "findDrugByNcitCode",
        query = "select d from Drug d where d.ncitCode=? and d.type = 'DRUG'"
    )
})

@Entity
@Table(name = "drug")
public class Drug implements java.io.Serializable {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, name = "ncit_code")
    private String ncitCode;

    @Column(length = 1000, name = "drug_name", nullable = false)
    private String drugName;

    @JsonIgnore
    @Column(length = 20, name = "type")
    @Enumerated(EnumType.STRING)
    private DrugTableItemType type = DrugTableItemType.DRUG;

    @JsonIgnore
    @Column(length = 40)
    private String uuid;

    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "drug_synonym", joinColumns = @JoinColumn(name = "drug_id", nullable = false))
    @Column(length = 1000, name = "synonym")
    private Set<String> synonyms = new HashSet<String>(0);

    @JsonIgnore
    @ManyToMany(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinTable(name="drug_family",
        joinColumns={@JoinColumn(name="drug_id")},
        inverseJoinColumns={@JoinColumn(name="drug_family_id")})
    private Set<Drug> drugFamlilies = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy="drugFamlilies", fetch = FetchType.EAGER)
    private Set<Drug> drugs = new HashSet<>();

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

    public DrugTableItemType getType() {
        return type;
    }

    public void setType(DrugTableItemType type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    @JsonIgnore
    public Set<Drug> getDrugFamlilies() {
        return drugFamlilies;
    }

    public void setDrugFamlilies(Set<Drug> drugFamlilies) {
        this.drugFamlilies = drugFamlilies;
    }

    public Set<Drug> getDrugs() {
        return drugs;
    }

    public void setDrugs(Set<Drug> drugs) {
        this.drugs = drugs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Drug)) return false;
        Drug drug = (Drug) o;
        return Objects.equals(getNcitCode(), drug.getNcitCode()) &&
            Objects.equals(getDrugName(), drug.getDrugName()) &&
            getType() == drug.getType() &&
            Objects.equals(getUuid(), drug.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNcitCode(), getDrugName(), getType(), getUuid());
    }
}


