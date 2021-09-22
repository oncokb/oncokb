/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author jiaojiao, Hongxin Zhang
 */

@NamedQueries({
    @NamedQuery(
        name = "findPortalAlterationCountByGene",
        query = "select pa.cancerType, count(distinct pa.sampleId) as sampleCount from PortalAlteration pa, Alteration a join a.portalAlterations ap where pa.id = ap.id and pa.gene=? group by pa.cancerType"
    ),
    @NamedQuery(
        name = "findPortalAlterationCount",
        query = "select cancerType, count(distinct sampleId) as sampleCount from PortalAlteration pa group by cancerType"
    ),
    @NamedQuery(
        name = "findMutationMapperData",
        query = "select distinct pa from PortalAlteration pa, Alteration a join a.portalAlterations ap where pa.id = ap.id and pa.gene=?"
    )
})

@Entity
@Table(name = "portal_alteration")
public class PortalAlteration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @Column(name = "cancer_type", length = 300, nullable = false)
    private String cancerType;

    @Column(name = "cancer_study", length = 300, nullable = false)
    private String cancerStudy;

    @Column(name = "sample_id")
    private String sampleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "entrez_gene_id")
    private Gene gene;

    @Column(name = "protein_change")
    private String proteinChange;

    @Column(name = "protein_start")
    private Integer proteinStartPosition;

    @Column(name = "protein_end")
    private Integer proteinEndPosition;

    @Column(name = "alteration_type", length = 300)
    private String alterationType;

    public PortalAlteration() {
    }

    public PortalAlteration(String cancerType, String cancerStudy, String sampleId, Gene gene, String proteinChange, Integer proteinStartPosition, Integer proteinEndPosition, String alterationType) {
        this.cancerType = cancerType;
        this.cancerStudy = cancerStudy;
        this.sampleId = sampleId;
        this.gene = gene;
        this.proteinChange = proteinChange;
        this.proteinStartPosition = proteinStartPosition;
        this.proteinEndPosition = proteinEndPosition;
        this.alterationType = alterationType;
    }

    public PortalAlteration(PortalAlteration pa) {
        cancerType = pa.cancerType;
        cancerStudy = pa.cancerStudy;
        sampleId = pa.sampleId;
        gene = pa.gene;
        proteinChange = pa.proteinChange;
        proteinStartPosition = pa.proteinStartPosition;
        proteinEndPosition = pa.proteinEndPosition;
        alterationType = pa.alterationType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCancerType() {
        return cancerType;
    }

    public void setCancerType(String cancerType) {
        this.cancerType = cancerType;
    }

    public String getCancerStudy() {
        return cancerStudy;
    }

    public void setCancerStudy(String cancerStudy) {
        this.cancerStudy = cancerStudy;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public String getProteinChange() {
        return proteinChange;
    }

    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
    }

    public Integer getProteinStartPosition() {
        return proteinStartPosition;
    }

    public void setProteinStartPosition(Integer proteinStartPosition) {
        this.proteinStartPosition = proteinStartPosition;
    }

    public Integer getProteinEndPosition() {
        return proteinEndPosition;
    }

    public void setProteinEndPosition(Integer proteinEndPosition) {
        this.proteinEndPosition = proteinEndPosition;
    }

    public String getAlterationType() {
        return alterationType;
    }

    public void setAlterationType(String alterationType) {
        this.alterationType = alterationType;
    }

}
