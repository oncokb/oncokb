package org.mskcc.cbio.oncokb.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "mutation_type")
public class MutationTypeEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @Column(name = "mutation_type", length = 10)
    private String mutationType;

    @JsonIgnore
    @ManyToMany(mappedBy = "mutationTypes")
    private Set<Tag> tags;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MutationType getMutationType() {
        return MutationType.fromString(mutationType);
    }

    public void setMutationType(MutationType mutationType) {
        this.mutationType = mutationType.getMutationType();
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}
