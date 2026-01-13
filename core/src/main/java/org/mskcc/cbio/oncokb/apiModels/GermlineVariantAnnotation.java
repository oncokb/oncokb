package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.GermlineIndicatorQueryResp;

import java.util.ArrayList;
import java.util.List;

public class GermlineVariantAnnotation extends GermlineIndicatorQueryResp {
    private List<VariantAnnotationTumorType> tumorTypes = new ArrayList<>();

    public GermlineVariantAnnotation() {
        super();
    }

    public GermlineVariantAnnotation(GermlineIndicatorQueryResp indicatorQueryResp) {
        this.setQuery(indicatorQueryResp.getQuery());
        this.setGeneExist(indicatorQueryResp.getGeneExist());
        this.setVariantExist(indicatorQueryResp.getVariantExist());
        this.setAlleleExist(indicatorQueryResp.getAlleleExist());
        this.setMutationEffect(indicatorQueryResp.getMutationEffect());
        this.setHighestSensitiveLevel(indicatorQueryResp.getHighestSensitiveLevel());
        this.setHighestResistanceLevel(indicatorQueryResp.getHighestResistanceLevel());
        this.setHighestDiagnosticImplicationLevel(indicatorQueryResp.getHighestDiagnosticImplicationLevel());
        this.setHighestPrognosticImplicationLevel(indicatorQueryResp.getHighestPrognosticImplicationLevel());
        this.setHighestFdaLevel(indicatorQueryResp.getHighestFdaLevel());
        this.setVUS(indicatorQueryResp.getVUS());
        this.setExon(indicatorQueryResp.getExon());
        this.setGeneSummary(indicatorQueryResp.getGeneSummary());
        this.setVariantSummary(indicatorQueryResp.getVariantSummary());
        this.setTumorTypeSummary(indicatorQueryResp.getTumorTypeSummary());
        this.setPrognosticSummary(indicatorQueryResp.getPrognosticSummary());
        this.setDiagnosticSummary(indicatorQueryResp.getDiagnosticSummary());
        this.setDiagnosticImplications(indicatorQueryResp.getDiagnosticImplications());
        this.setPrognosticImplications(indicatorQueryResp.getPrognosticImplications());
        this.setTreatments(indicatorQueryResp.getTreatments());
        this.setDataVersion(indicatorQueryResp.getDataVersion());
        this.setLastUpdate(indicatorQueryResp.getLastUpdate());
        this.setGenomicIndicators(indicatorQueryResp.getGenomicIndicators());
        this.setPenetrance(indicatorQueryResp.getPenetrance());
        this.setPathogenic(indicatorQueryResp.getPathogenic());
        this.setClinVarId(indicatorQueryResp.getClinVarId());
    }

    public List<VariantAnnotationTumorType> getTumorTypes() {
        return tumorTypes;
    }

    public void setTumorTypes(List<VariantAnnotationTumorType> tumorTypes) {
        this.tumorTypes = tumorTypes;
    }
}
