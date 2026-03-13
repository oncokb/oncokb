package org.mskcc.cbio.oncokb.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "oncogenicity")
public class OncogenicityEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @Column(name = "oncogenicity", length = 20)
    private String oncogenicity;

    @JsonIgnore
    @ManyToMany(mappedBy = "oncogenicities")
    private Set<Tag> tags;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Oncogenicity getOncogenicity() {
        return Oncogenicity.getByEffect(oncogenicity);
    }

    public void setOncogenicity(Oncogenicity oncogenicity) {
        this.oncogenicity = oncogenicity.getOncogenic();
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}
