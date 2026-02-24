package org.mskcc.cbio.oncokb.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.mskcc.cbio.oncokb.util.LevelUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Entity
@Table(name = "tag")
@NamedQueries({
    @NamedQuery(
        name = "findTagsByEntrezGeneId",
        query = "select t from Tag t join t.gene g where g.entrezGeneId=?"
    ),
    @NamedQuery(
        name = "findTagsByHugoSymbolAndName",
        query = "select t from Tag t join t.gene g where g.hugoSymbol=? and t.name=?"
    )
})
public class Tag implements java.io.Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;    

    private String name;

    private String description;
 
    private Integer start;

    private Integer end;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "oncogenicity_tag", 
        joinColumns = {@JoinColumn(name="tag_id")},
        inverseJoinColumns = {@JoinColumn(name="oncogenicity_id")}
    )
    private Set<OncogenicityEntity> oncogenicities;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "mutation_type_tag", 
        joinColumns = {@JoinColumn(name="tag_id")},
        inverseJoinColumns = {@JoinColumn(name="mutation_type_id")}
    )
    private Set<MutationTypeEntity> mutationTypes;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "evidence_tag", 
        joinColumns = {@JoinColumn(name="tag_id")},
        inverseJoinColumns = {@JoinColumn(name="evidence_id")}
    )
    @JsonIgnoreProperties("tags")
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

     public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Set<OncogenicityEntity> getOncogenicities() {
        return oncogenicities;
    }

    public void setOncogenicities(Set<OncogenicityEntity> oncogenicities) {
        this.oncogenicities = oncogenicities;
    }

    public Set<MutationTypeEntity> getMutationTypes() {
        return mutationTypes;
    }

    public void setMutationTypes(Set<MutationTypeEntity> mutationTypes) {
        this.mutationTypes = mutationTypes;
    }

    public LevelInfo getHighestLevels() {
        LevelInfo levelInfo = new LevelInfo();
        for (Evidence evidence : this.evidences) {
            LevelOfEvidence levelOfEvidence = evidence.getLevelOfEvidence();
            if (LevelUtils.isSensitiveLevel(levelOfEvidence)) {
                levelInfo.highestSensitiveLevel = LevelUtils.getHighestLevel(levelInfo.highestSensitiveLevel, levelOfEvidence);
            } else if (LevelUtils.isResistanceLevel(levelOfEvidence)) {
                levelInfo.highestResistanceLevel = LevelUtils.getHighestLevel(levelInfo.highestResistanceLevel, levelOfEvidence);
            } else if (LevelUtils.isDiagnosticLevel(levelOfEvidence)) {
                levelInfo.highestDiagnosticLevel = LevelUtils.getHighestLevel(levelInfo.highestDiagnosticLevel, levelOfEvidence);
            } else if (LevelUtils.isPrognosticLevel(levelOfEvidence)) {
                levelInfo.highestPrognosticLevel = LevelUtils.getHighestLevel(levelInfo.highestPrognosticLevel, levelOfEvidence);
            } 
            
            if (evidence.getFdaLevel() != null) {
                levelInfo.highestFDALevel = LevelUtils.getHighestLevel(levelInfo.highestFDALevel, evidence.getFdaLevel());
            }
        }
        return levelInfo;
    }

    public static class LevelInfo {
        public LevelOfEvidence highestSensitiveLevel;
        public LevelOfEvidence highestResistanceLevel;
        public LevelOfEvidence highestDiagnosticLevel;
        public LevelOfEvidence highestPrognosticLevel;
        public LevelOfEvidence highestFDALevel;
    }
}
