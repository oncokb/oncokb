package org.mskcc.cbio.oncokb.model;


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
        query = "select d from Drug d where d.drugName=? and d.type = 'DRUG'"
    ),
    @NamedQuery(
        name = "findDrugById",
        query = "select d from Drug d where d.id=?"
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, name = "ncit_code")
    private String ncitCode;

    @Column(length = 1000, name = "drug_name", nullable = false)
    private String drugName;

    @Column(length = 20, name = "type")
    @Enumerated(EnumType.STRING)
    private DrugTableItemType type = DrugTableItemType.DRUG;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "drug_synonym", joinColumns = @JoinColumn(name = "drug_id", nullable = false))
    @Column(length = 1000, name = "synonym")
    private Set<String> synonyms = new HashSet<String>(0);

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "drug_atccode", joinColumns = @JoinColumn(name = "drug_id", nullable = false))
    @Column(name = "atccode")
    private Set<String> atcCodes;

    @ManyToMany(cascade={CascadeType.ALL})
    @JoinTable(name="drug_family",
        joinColumns={@JoinColumn(name="drug_id")},
        inverseJoinColumns={@JoinColumn(name="drug_family_id")})
    private Set<Drug> drugFamlilies = new HashSet<>();

    @ManyToMany(mappedBy="drugFamlilies")
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

   public Set<String> getAtcCodes() {
        return atcCodes;
    }

    public void setAtcCodes(Set<String> atcCodes) {
        this.atcCodes = atcCodes;
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


