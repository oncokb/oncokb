package org.mskcc.cbio.oncokb.model;

import java.util.ArrayList;
import java.util.List;

public class GermlineIndicatorQueryResp extends IndicatorQueryRespBase {
    private List<org.mskcc.cbio.oncokb.apiModels.GenomicIndicator> genomicIndicators = new ArrayList<>();
    private String penetrance = "";
    private String pathogenic = "";
    private String clinVarId = "";

    public GermlineIndicatorQueryResp() {
        super();
    }

    @Override
    public GermlineIndicatorQueryResp copy() {
        GermlineIndicatorQueryResp newResp = new GermlineIndicatorQueryResp();
        newResp.setQuery(this.getQuery() == null ? null : this.getQuery().copy());
        newResp.setGeneExist(this.getGeneExist());
        newResp.setVariantExist(this.getVariantExist());
        newResp.setAlleleExist(this.getAlleleExist());
        newResp.setMutationEffect(this.getMutationEffect());
        newResp.setHighestSensitiveLevel(this.getHighestSensitiveLevel());
        newResp.setHighestResistanceLevel(this.getHighestResistanceLevel());
        newResp.setHighestDiagnosticImplicationLevel(this.getHighestDiagnosticImplicationLevel());
        newResp.setHighestPrognosticImplicationLevel(this.getHighestPrognosticImplicationLevel());
        newResp.setHighestFdaLevel(this.getHighestFdaLevel());
        newResp.setOtherSignificantSensitiveLevels(new java.util.ArrayList<>(this.getOtherSignificantSensitiveLevels()));
        newResp.setOtherSignificantResistanceLevels(new java.util.ArrayList<>(this.getOtherSignificantResistanceLevels()));
        newResp.setVUS(this.getVUS());
        newResp.setExon(this.getExon());
        newResp.setGeneSummary(this.getGeneSummary());
        newResp.setVariantSummary(this.getVariantSummary());
        newResp.setTumorTypeSummary(this.getTumorTypeSummary());
        newResp.setPrognosticSummary(this.getPrognosticSummary());
        newResp.setDiagnosticSummary(this.getDiagnosticSummary());
        newResp.setDiagnosticImplications(new java.util.ArrayList<>(this.getDiagnosticImplications()));
        newResp.setPrognosticImplications(new java.util.ArrayList<>(this.getPrognosticImplications()));
        newResp.setTreatments(new java.util.ArrayList<>(this.getTreatments()));
        newResp.setDataVersion(this.getDataVersion());
        newResp.setLastUpdate(this.getLastUpdate());
        newResp.setGenomicIndicators(new ArrayList<>(this.genomicIndicators));
        newResp.setPenetrance(this.penetrance);
        newResp.setPathogenic(this.pathogenic);
        newResp.setClinVarId(this.clinVarId);
        return newResp;
    }

    public List<org.mskcc.cbio.oncokb.apiModels.GenomicIndicator> getGenomicIndicators() {
        return genomicIndicators;
    }

    public void setGenomicIndicators(List<org.mskcc.cbio.oncokb.apiModels.GenomicIndicator> genomicIndicators) {
        this.genomicIndicators = genomicIndicators;
    }

    public String getPenetrance() {
        return penetrance;
    }

    public void setPenetrance(String penetrance) {
        this.penetrance = penetrance;
    }

    public String getPathogenic() {
        return pathogenic;
    }

    public void setPathogenic(String pathogenic) {
        this.pathogenic = pathogenic;
    }

    public String getClinVarId() {
        return clinVarId;
    }

    public void setClinVarId(String clinVarId) {
        this.clinVarId = clinVarId;
    }
}
