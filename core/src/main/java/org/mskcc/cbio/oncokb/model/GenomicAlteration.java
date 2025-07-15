package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

// TODO: look into UUID column
@Entity
@Table(name = "genomic_alteration")
public class GenomicAlteration implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "entrez_gene_id")
    private Gene gene;

    @Column(name = "chromosome")
    private String chromosome;

    @Column(name = "ref")
    private String ref;

    @Column(name = "start")
    private Integer start;

    @Column(name = "end")
    private Integer end;

    @Column(name = "var")
    private String var;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "genomic_alteration_reference_genome", joinColumns = @JoinColumn(name = "genomic_alteration_id", nullable = false))
    @Column(length = 10, name = "reference_genome")
    @Enumerated(EnumType.STRING)
    private Set<ReferenceGenome> referenceGenomes = new HashSet<>(0);

    public GenomicAlteration() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Gene getGene() {
        return this.gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getRef() {
        return this.ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Integer getStart() {
        return this.start;
    }

    public void setStart(Integer proteinStart) {
        this.start = proteinStart;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer proteinEnd) {
        this.end = proteinEnd;
    }

    public String getVar() {
        return this.var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public Set<ReferenceGenome> getReferenceGenomes() {
        return referenceGenomes;
    }

    public void setReferenceGenomes(Set<ReferenceGenome> referenceGenomes) {
        this.referenceGenomes = referenceGenomes;
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId();
        } else {
            return Objects.hash(this.gene, this.chromosome, this.start, this.end, this.ref, this.var, this.referenceGenomes);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alteration)) return false;
        GenomicAlteration that = (GenomicAlteration) o;
        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        }
        return Objects.equals(getGene(), that.getGene()) &&
            Objects.equals(getChromosome(), that.getChromosome()) &&
            Objects.equals(getRef(), that.getRef()) &&
            Objects.equals(getStart(), that.getStart()) &&
            Objects.equals(getEnd(), that.getEnd()) &&
            Objects.equals(getVar(), that.getVar()) &&
            Objects.equals(getReferenceGenomes(), that.getReferenceGenomes());
    }

    @Override
    public String toString() {
        return this.getChromosome() + "," + this.getStart() + "," + this.getEnd() + "," + this.getRef() + "," + this.getVar();
    }

    @JsonIgnore
    public String getUniqueId() {
        List<String> content = new ArrayList<>();
        if (this.gene != null) {
            if (this.gene.getHugoSymbol() != null) {
                content.add(this.gene.getHugoSymbol());
            } else {
                content.add("");
            }
        }
        if (this.chromosome != null) {
            content.add(this.chromosome);
        } else {
            content.add("");
        }
        if (this.start != null) {
            content.add(Integer.toString(this.start));
        } else {
            content.add("");
        }
        if (this.end != null) {
            content.add(Integer.toString(this.end));
        } else {
            content.add("");
        }
        if (this.ref != null) {
            content.add(this.ref);
        } else {
            content.add("");
        }
        if (this.var != null) {
            content.add(this.var);
        } else {
            content.add("");
        }
        if (this.referenceGenomes != null) {
            content.add(this.referenceGenomes.stream().map(referenceGenome -> referenceGenome.name()).collect(Collectors.joining(",")));
        } else {
            content.add("");
        }

        return StringUtils.join(content.toArray(), "&");
    }
}
