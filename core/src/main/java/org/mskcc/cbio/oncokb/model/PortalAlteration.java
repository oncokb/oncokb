/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;

import java.util.Set;

/**
 *
 * @author jiaojiao
 */
public class PortalAlteration {

    private Integer portalAlterationID;
    private String cancerType;
    private String cancerStudy;
    private String sampleId;
    private Gene gene;
    private String proteinChange;
    private Integer proteinStartPosition;
    private Integer proteinEndPosition;
    private Set<Alteration> oncoKBAlterations;
    private String alterationType;

    public PortalAlteration() {
    }

    public PortalAlteration(String cancerType, String cancerStudy, String sampleId, Gene gene, String proteinChange, Integer proteinStartPosition, Integer proteinEndPosition, Set<Alteration> oncoKBAlterations, String alterationType) {
        this.cancerType = cancerType;
        this.cancerStudy = cancerStudy;
        this.sampleId = sampleId;
        this.gene = gene;
        this.proteinChange = proteinChange;
        this.proteinStartPosition = proteinStartPosition;
        this.proteinEndPosition = proteinEndPosition;
        this.oncoKBAlterations = oncoKBAlterations;
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
        oncoKBAlterations = pa.oncoKBAlterations;
        alterationType = pa.alterationType;
    }
    public Integer getPortalAlterationID() {
        return portalAlterationID;
    }

    public void setPortalAlterationID(Integer portalAlterationID) {
        this.portalAlterationID = portalAlterationID;
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

    public Set<Alteration> getOncoKBAlterations() {
        return oncoKBAlterations;
    }

    public void setOncoKBAlterations(Set<Alteration> oncoKBAlterations) {
        this.oncoKBAlterations = oncoKBAlterations;
    }

    public String getAlterationType() {
        return alterationType;
    }

    public void setAlterationType(String alterationType) {
        this.alterationType = alterationType;
    }

}
