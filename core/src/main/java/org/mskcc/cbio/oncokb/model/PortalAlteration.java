/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author jiaojiao
 */
public class PortalAlteration {
    @JsonIgnore
    private Integer id;
    private String cancerType;
    private String cancerStudy;
    private String sampleId;
    private Gene gene;
    private String proteinChange;
    private Integer proteinStartPosition;
    private Integer proteinEndPosition;
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
