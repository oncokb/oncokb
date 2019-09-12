package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.mskcc.cbio.oncokb.serializer.SetGeneInGenesetConverter;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Hongxin Zhang on 2019-08-08.
 */

@NamedQueries({
    @NamedQuery(
        name = "findGenesetByName",
        query = "select g from Geneset g where g.name=?"
    ),
    @NamedQuery(
        name = "findGenesetByUUID",
        query = "select g from Geneset g where g.uuid=?"
    ),
})

@Entity
@Table(name = "geneset")
public class Geneset implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(length = 40, nullable = false)
    private String uuid;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "geneset_gene", joinColumns = {
        @JoinColumn(name = "geneset_id", updatable = false, nullable = false)
    }, inverseJoinColumns = {
        @JoinColumn(name = "entrez_gene_id", updatable = false, nullable = false)
    })
    @JsonSerialize(converter = SetGeneInGenesetConverter.class)
    private Set<Gene> genes;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Gene> getGenes() {
        return genes;
    }

    public void setGenes(Set<Gene> genes) {
        this.genes = genes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Geneset)) return false;
        Geneset geneset = (Geneset) o;
        return Objects.equals(getName(), geneset.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
