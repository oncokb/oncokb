package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.ReferenceGenome;

public class CplAnnotationRequest implements java.io.Serializable{
    private String template;
    private String hugoSymbol;
    private String alteration;
    private String cancerType;
    private ReferenceGenome referenceGenome;

    public String getTemplate() {
        return this.template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getHugoSymbol() {
        return this.hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String getAlteration() {
        return this.alteration;
    }

    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }

    public String getCancerType() {
        return this.cancerType;
    }

    public void setCancerType(String cancerType) {
        this.cancerType = cancerType;
    }


    public ReferenceGenome getReferenceGenome() {
        return this.referenceGenome;
    }

    public void setReferenceGenome(ReferenceGenome referenceGenome) {
        this.referenceGenome = referenceGenome;
    }


}