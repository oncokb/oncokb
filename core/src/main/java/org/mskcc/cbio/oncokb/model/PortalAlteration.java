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
    
    private String cancertype;
    private Integer numberOfSamples;
    private Integer entrezID;
    private String proteinChange;
    private Integer startPosition;
    private Integer endPosition;
    private List<String> variants; 
    
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
    

    public String getCancertype() {
        return cancertype;
    }

    public void setCancertype(String cancertype) {
        this.cancertype = cancertype;
    }

    public Integer getNumberOfSamples() {
        return numberOfSamples;
    }

    public void setNumberOfSamples(Integer numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public Integer getEntrezID() {
        return entrezID;
    }

    public void setEntrezID(Integer entrezID) {
        this.entrezID = entrezID;
    }

    public String getProteinChange() {
        return proteinChange;
    }

    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
    }

    public List<String> getVariants() {
        return variants;
    }

    public void setVariants(List<String> variants) {
        this.variants = variants;
    }
    
    
    
}
