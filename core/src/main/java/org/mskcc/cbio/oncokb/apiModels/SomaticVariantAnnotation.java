package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.SomaticIndicatorQueryResp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hongxin Zhang on 2019-07-18.
 */
public class SomaticVariantAnnotation extends SomaticIndicatorQueryResp {
    private String background;

    private Alteration alteration;

    private Boolean VUE = false;
    private List<VariantAnnotationTumorType> tumorTypes = new ArrayList<>();

    public SomaticVariantAnnotation() {
    }

    public SomaticVariantAnnotation(SomaticIndicatorQueryResp indicatorQueryResp) {
        this.setQuery(indicatorQueryResp.getQuery());
        this.setGeneExist(indicatorQueryResp.getGeneExist());
        this.setVariantExist(indicatorQueryResp.getVariantExist());
        this.setAlleleExist(indicatorQueryResp.getAlleleExist());
        this.setOncogenic(indicatorQueryResp.getOncogenic());
        this.setMutationEffect(indicatorQueryResp.getMutationEffect());
        this.setHighestSensitiveLevel(indicatorQueryResp.getHighestSensitiveLevel());
        this.setHighestResistanceLevel(indicatorQueryResp.getHighestResistanceLevel());
        this.setHighestDiagnosticImplicationLevel(indicatorQueryResp.getHighestDiagnosticImplicationLevel());
        this.setHighestPrognosticImplicationLevel(indicatorQueryResp.getHighestPrognosticImplicationLevel());
        this.setHighestFdaLevel(indicatorQueryResp.getHighestFdaLevel());
        this.setOtherSignificantSensitiveLevels(indicatorQueryResp.getOtherSignificantSensitiveLevels());
        this.setOtherSignificantResistanceLevels(indicatorQueryResp.getOtherSignificantResistanceLevels());
        this.setVUS(indicatorQueryResp.getVUS());
        this.setHotspot(indicatorQueryResp.getHotspot());
        this.setGeneSummary(indicatorQueryResp.getGeneSummary());
        this.setVariantSummary(indicatorQueryResp.getVariantSummary());
        this.setTumorTypeSummary(indicatorQueryResp.getTumorTypeSummary());
        this.setDiagnosticSummary(indicatorQueryResp.getDiagnosticSummary());
        this.setPrognosticSummary(indicatorQueryResp.getPrognosticSummary());
        this.setDiagnosticImplications(indicatorQueryResp.getDiagnosticImplications());
        this.setPrognosticImplications(indicatorQueryResp.getPrognosticImplications());
        this.setTreatments(indicatorQueryResp.getTreatments());
        this.setDataVersion(indicatorQueryResp.getDataVersion());
        this.setLastUpdate(indicatorQueryResp.getLastUpdate());
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public Boolean getVUE() {
        return VUE;
    }

    public void setVUE(Boolean VUE) {
        this.VUE = VUE;
    }

    public List<VariantAnnotationTumorType> getTumorTypes() {
        return tumorTypes;
    }

    public void setTumorTypes(List<VariantAnnotationTumorType> tumorTypes) {
        this.tumorTypes = tumorTypes;
    }


    public Alteration getAlteration() {
        return this.alteration;
    }

    public void setAlteration(Alteration alteration) {
        this.alteration = alteration;
    }
}
