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
    private Integer numberOfSamples;
    private Gene gene;
    private String proteinChange;
    private Integer startPosition;
    private Integer endPosition;
    private Set<Alteration> oncoKBAlterations;
    private String alterationType;

    public PortalAlteration(String cancerType, String cancerStudy, Integer numberOfSamples, Gene gene, String proteinChange, Integer startPosition, Integer endPosition,Set<Alteration> oncoKBAlterations,String alterationType) {
        this.cancerType = cancerType;
        this.cancerStudy = cancerStudy;
        this.numberOfSamples = numberOfSamples;
        this.gene = gene;
        this.proteinChange = proteinChange;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.oncoKBAlterations = oncoKBAlterations;
        this.alterationType = alterationType;
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

    public String getAlterationType() {
        return alterationType;
    }

    public void setAlterationType(String alterationType) {
        this.alterationType = alterationType;
    }
    public Set<Alteration> getOncoKBAlterations() {
        return oncoKBAlterations;
    }

    public void setOncoKBAlterations(Set<Alteration> oncoKBAlterations) {
        this.oncoKBAlterations = oncoKBAlterations;
    }
    
    
}
