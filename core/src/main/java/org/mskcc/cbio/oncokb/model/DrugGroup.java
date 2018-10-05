package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Set;


/**
 * @author Hongxin Zhang
 */

@NamedQueries({
    @NamedQuery(
        name = "findGroupByName",
        query = "select dg from DrugGroup dg where dg.groupName=?"
    ),
    @NamedQuery(
        name = "findGroupsByDrug",
        query = "select dg from DrugGroup dg join dg.drugs d where d=?"
    )
})

@Entity
@Table(name = "drug_group")
public class DrugGroup implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @Column(length = 1000, name = "group_name", nullable = false)
    private String groupName;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "drug_group_drug", joinColumns = {
        @JoinColumn(name = "drug_group_id", updatable = false, nullable = false)
    }, inverseJoinColumns = {
        @JoinColumn(name = "drug_id", updatable = false, nullable = false)
    })
    private Set<Drug> drugs;

    @JsonIgnore
    @Column(length = 65535)
    private String description;

    public DrugGroup() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Set<Drug> getDrugs() {
        return drugs;
    }

    public void setDrugs(Set<Drug> drugs) {
        this.drugs = drugs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


