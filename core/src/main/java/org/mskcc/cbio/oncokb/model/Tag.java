package org.mskcc.cbio.oncokb.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NamedQueries({
    @NamedQuery(
        name = "findTagsByEntrezGeneId",
        query = "select t from Tag t where t.gene.entrezGeneId = ?"
    ),
})
@Entity
@Table(name = "tag")
public class Tag implements java.io.Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;    

    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "evidence_tag", 
        joinColumns = {@JoinColumn(name="tag_id")},
        inverseJoinColumns = {@JoinColumn(name="evidence_id")}
    )
    private Set<Evidence> evidences = new HashSet<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "entrez_gene_id")
    private Gene gene;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Evidence> getEvidences() {
        return evidences;
    }

    public void setEvidences(Set<Evidence> evidences) {
        this.evidences = evidences;
    }

    public Gene getGene() {
        return this.gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }
}
