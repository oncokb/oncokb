package org.mskcc.cbio.oncokb.model;

/**
 * Created by Hongxin on 6/7/17.
 */
public class TypeaheadSearchResp {
    private Gene gene;
    private Alteration variant;
    private String oncogenicity;
    private String highestSensitiveLevel;
    private String highestResistanceLevel;
    private Boolean variantExist;
    private Boolean isVUS;
    private String annotation;
    private String queryType;
    private String link;

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public Alteration getVariant() {
        return variant;
    }

    public void setVariant(Alteration variant) {
        this.variant = variant;
    }

    public String getOncogenicity() {
        return oncogenicity;
    }

    public void setOncogenicity(String oncogenicity) {
        this.oncogenicity = oncogenicity;
    }

    public String getHighestSensitiveLevel() {
        return highestSensitiveLevel;
    }

    public void setHighestSensitiveLevel(String highestSensitiveLevel) {
        this.highestSensitiveLevel = highestSensitiveLevel;
    }

    public String getHighestResistanceLevel() {
        return highestResistanceLevel;
    }

    public void setHighestResistanceLevel(String highestResistanceLevel) {
        this.highestResistanceLevel = highestResistanceLevel;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public Boolean getVariantExist() {
        return variantExist;
    }

    public void setVariantExist(Boolean variantExist) {
        this.variantExist = variantExist;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Boolean getVUS() {
        return isVUS;
    }

    public void setVUS(Boolean VUS) {
        isVUS = VUS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeaheadSearchResp)) return false;

        TypeaheadSearchResp that = (TypeaheadSearchResp) o;

        if (getGene() != null ? !getGene().equals(that.getGene()) : that.getGene() != null) return false;
        if (getVariant() != null ? !getVariant().equals(that.getVariant()) : that.getVariant() != null) return false;
        if (getOncogenicity() != null ? !getOncogenicity().equals(that.getOncogenicity()) : that.getOncogenicity() != null)
            return false;
        if (getHighestSensitiveLevel() != null ? !getHighestSensitiveLevel().equals(that.getHighestSensitiveLevel()) : that.getHighestSensitiveLevel() != null)
            return false;
        if (getHighestResistanceLevel() != null ? !getHighestResistanceLevel().equals(that.getHighestResistanceLevel()) : that.getHighestResistanceLevel() != null)
            return false;
        if (getVariantExist() != null ? !getVariantExist().equals(that.getVariantExist()) : that.getVariantExist() != null)
            return false;
        if (isVUS != null ? !isVUS.equals(that.isVUS) : that.isVUS != null) return false;
        if (getAnnotation() != null ? !getAnnotation().equals(that.getAnnotation()) : that.getAnnotation() != null)
            return false;
        if (getQueryType() != null ? !getQueryType().equals(that.getQueryType()) : that.getQueryType() != null)
            return false;
        return getLink() != null ? getLink().equals(that.getLink()) : that.getLink() == null;
    }

    @Override
    public int hashCode() {
        int result = getGene() != null ? getGene().hashCode() : 0;
        result = 31 * result + (getVariant() != null ? getVariant().hashCode() : 0);
        result = 31 * result + (getOncogenicity() != null ? getOncogenicity().hashCode() : 0);
        result = 31 * result + (getHighestSensitiveLevel() != null ? getHighestSensitiveLevel().hashCode() : 0);
        result = 31 * result + (getHighestResistanceLevel() != null ? getHighestResistanceLevel().hashCode() : 0);
        result = 31 * result + (getVariantExist() != null ? getVariantExist().hashCode() : 0);
        result = 31 * result + (isVUS != null ? isVUS.hashCode() : 0);
        result = 31 * result + (getAnnotation() != null ? getAnnotation().hashCode() : 0);
        result = 31 * result + (getQueryType() != null ? getQueryType().hashCode() : 0);
        result = 31 * result + (getLink() != null ? getLink().hashCode() : 0);
        return result;
    }
}
