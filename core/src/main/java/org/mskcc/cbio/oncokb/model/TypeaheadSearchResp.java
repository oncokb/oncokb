package org.mskcc.cbio.oncokb.model;

import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;

import java.util.Objects;
import java.util.Set;

/**
 * Created by Hongxin on 6/7/17.
 */
public class TypeaheadSearchResp {
    private Gene gene;
    private Set<Alteration> variants;
    private Set<TumorType> tumorTypes;
    private Drug drug;
    private String oncogenicity;
    private String highestSensitiveLevel;
    private String highestResistanceLevel;
    private Boolean variantExist;
    private Boolean isVUS;
    private String annotation;
    private TypeaheadQueryType queryType;
    private String link;

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public Set<Alteration> getVariants() {
        return variants;
    }

    public void setVariants(Set<Alteration> variants) {
        this.variants = variants;
    }

    public Set<TumorType> getTumorTypes() {
        return tumorTypes;
    }

    public void setTumorTypes(Set<TumorType> tumorTypes) {
        this.tumorTypes = tumorTypes;
    }

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
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

    public TypeaheadQueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(TypeaheadQueryType queryType) {
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
        return Objects.equals(getGene(), that.getGene()) &&
            Objects.equals(getVariants(), that.getVariants()) &&
            Objects.equals(getTumorTypes(), that.getTumorTypes()) &&
            Objects.equals(getDrug(), that.getDrug()) &&
            Objects.equals(getOncogenicity(), that.getOncogenicity()) &&
            Objects.equals(getHighestSensitiveLevel(), that.getHighestSensitiveLevel()) &&
            Objects.equals(getHighestResistanceLevel(), that.getHighestResistanceLevel()) &&
            Objects.equals(getVariantExist(), that.getVariantExist()) &&
            Objects.equals(isVUS, that.isVUS) &&
            Objects.equals(getAnnotation(), that.getAnnotation()) &&
            Objects.equals(getQueryType(), that.getQueryType()) &&
            Objects.equals(getLink(), that.getLink());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGene(), getVariants(), getTumorTypes(), getDrug(), getOncogenicity(), getHighestSensitiveLevel(), getHighestResistanceLevel(), getVariantExist(), isVUS, getAnnotation(), getQueryType(), getLink());
    }
}
