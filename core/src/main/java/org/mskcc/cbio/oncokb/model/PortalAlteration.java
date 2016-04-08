/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;

import java.util.List;

/**
 *
 * @author jiaojiao
 */
public class PortalAlteration {
    private String cancerType;
    private String cancerStudy;
    private Integer numberOfSamples;
    private Gene gene;
    private String proteinChange;
    private Integer startPosition;
    private Integer endPosition;
    private List<Alteration> oncoKBAlterations;
    private String alterationType;

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

    public Integer getNumberOfSamples() {
        return numberOfSamples;
    }

    public void setNumberOfSamples(Integer numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
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

    public Integer getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Integer startPosition) {
        this.startPosition = startPosition;
    }

    public Integer getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Integer endPosition) {
        this.endPosition = endPosition;
    }

    public List<Alteration> getOncoKBAlterations() {
        return oncoKBAlterations;
    }

    public void setOncoKBAlterations(List<Alteration> oncoKBAlterations) {
        this.oncoKBAlterations = oncoKBAlterations;
    }

    public String getAlterationType() {
        return alterationType;
    }

    public void setAlterationType(String alterationType) {
        this.alterationType = alterationType;
    }
    
    
    
}
