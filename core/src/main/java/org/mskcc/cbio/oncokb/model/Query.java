package org.mskcc.cbio.oncokb.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.annotation.*;
import org.mskcc.cbio.oncokb.util.AminoAcidConverterUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.QueryUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;
import static org.mskcc.cbio.oncokb.util.FusionUtils.FUSION_ALTERNATIVE_SEPARATOR;
import static org.mskcc.cbio.oncokb.util.FusionUtils.FUSION_SEPARATOR;


@ApiModel(description = "Enriched user annotation query")
public class Query implements java.io.Serializable {
    @ApiModelProperty(value = "(Nullable) The id passed in request for the user to distinguish the query.")
    private String id;

    @ApiModelProperty(value = "Reference genome build version. Defaulted to GRCh37")
    private ReferenceGenome referenceGenome = DEFAULT_REFERENCE_GENOME;

    @ApiModelProperty(value = "(Nullable) The gene symbol used in Human Genome Organisation")
    private String hugoSymbol;

    @ApiModelProperty(value = "(Nullable) Unique gene identifiers from NCBI. May be null if omitted from original query, otherwise filled in by OncoKB")
    private Integer entrezGeneId;

    @ApiModelProperty(value = "(Nullable) Alteration from original query or the resolved alteration from HGVS variant")
    private String alteration;

    @ApiModelProperty(value = "(Nullable) Alteration type", allowableValues = "MUTATION, COPY_NUMBER_ALTERATION, STRUCTURAL_VARIANT")
    private String alterationType;

    @ApiModelProperty(value = "(Nullable) Structural variant type")
    private StructuralVariantType svType;

    @ApiModelProperty(value = "(Nullable) Oncotree tumor type name, code, or main type.")
    private String tumorType;

    @ApiModelProperty(value = "(Nullable) Variant consequence term from Ensembl. See https://useast.ensembl.org/info/genome/variation/prediction/predicted_data.html")
    private String consequence;

    @ApiModelProperty(value = "(Nullable) Protein start position")
    private Integer proteinStart;

    @ApiModelProperty(value = "(Nullable) Protein end position")
    private Integer proteinEnd;

    @ApiModelProperty(value = "(Nullable) The hgvsg or genomic location from original query")
    private String hgvs;

    @ApiModelProperty(value = "(Nullable) Additional message for \"hgvs\" field. May indicate reason for failed hgvs annotation.")
    private String hgvsInfo;

    @ApiModelProperty(value = "(Nullable) The OncoKB canonical transcript. See https://www.oncokb.org/cancer-genes")
    private String canonicalTranscript;

    public Query() {
    }

    public Query copy() {
        Query newQuery = new Query();
        newQuery.setId(this.id);
        newQuery.setReferenceGenome(this.referenceGenome);
        newQuery.setHugoSymbol(this.hugoSymbol);
        newQuery.setEntrezGeneId(this.entrezGeneId);
        newQuery.setAlteration(this.alteration);
        newQuery.setAlterationType(this.alterationType);
        newQuery.setSvType(this.svType);
        newQuery.setTumorType(this.tumorType);
        newQuery.setConsequence(this.consequence);
        newQuery.setProteinStart(this.proteinStart);
        newQuery.setProteinEnd(this.proteinEnd);
        newQuery.setHgvs(this.hgvs);
        newQuery.setHgvsInfo(this.hgvsInfo);
        newQuery.setCanonicalTranscript(this.canonicalTranscript);
        return newQuery;
    }

    public Query(AnnotateMutationByProteinChangeQuery mutationQuery) {
        this.id = mutationQuery.getId();
        this.setTumorType(mutationQuery.getTumorType());

        if (mutationQuery.getGene() != null) {
            this.hugoSymbol = mutationQuery.getGene().getHugoSymbol();
            this.entrezGeneId = mutationQuery.getGene().getEntrezGeneId();
        }

        setAlteration(mutationQuery.getAlteration());
        this.consequence = mutationQuery.getConsequence();
        this.proteinStart = mutationQuery.getProteinStart();
        this.proteinEnd = mutationQuery.getProteinEnd();
        this.referenceGenome = mutationQuery.getReferenceGenome();
        if (this.referenceGenome == null) {
            this.referenceGenome = DEFAULT_REFERENCE_GENOME;
        }
    }

    public Query(Alteration alt, ReferenceGenome referenceGenome) {
        if (alt != null) {
            if (alt.getGene() != null) {
                this.hugoSymbol = alt.getGene().getHugoSymbol();
                this.entrezGeneId = alt.getGene().getEntrezGeneId();
            }
            setAlteration(alt.getAlteration());
            this.alterationType = alt.getAlterationType() == null ? "MUTATION" : alt.getAlterationType().name();
            this.consequence = alt.getConsequence() == null ? null : alt.getConsequence().getTerm();
            this.proteinStart = alt.getProteinStart();
            this.proteinEnd = alt.getProteinEnd();
            this.referenceGenome = referenceGenome;
            if (this.referenceGenome == null) {
                this.referenceGenome = DEFAULT_REFERENCE_GENOME;
            }
        }
    }

    public Query(String hugoSymbol, String alteration, String tumorType) {
        this.hugoSymbol = hugoSymbol;
        this.setAlteration(alteration);
        this.setTumorType(tumorType);
    }

    public Query(String id, ReferenceGenome referenceGenome, Integer entrezGeneId, String hugoSymbol,
                 String alteration, String alterationType, StructuralVariantType svType,
                 String tumorType, String consequence, Integer proteinStart, Integer proteinEnd, String hgvs) {
        this.id = id;
        this.referenceGenome = referenceGenome == null ? DEFAULT_REFERENCE_GENOME : referenceGenome;
        if (hugoSymbol != null && !hugoSymbol.isEmpty()) {
            this.hugoSymbol = hugoSymbol;
        }
        this.entrezGeneId = entrezGeneId;
        this.setAlteration(alteration);
        this.alterationType = alterationType;
        this.svType = svType;
        this.setTumorType(tumorType);
        this.consequence = consequence;
        this.proteinStart = proteinStart;
        this.proteinEnd = proteinEnd;
        this.setHgvs(hgvs);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ReferenceGenome getReferenceGenome() {
        return referenceGenome;
    }

    public void setReferenceGenome(ReferenceGenome referenceGenome) {
        this.referenceGenome = referenceGenome;
    }

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(String alteration) {
        if (alteration != null) {
            alteration = alteration.replace("p.", "");
        }
        this.alteration = AminoAcidConverterUtils.resolveHgvspShortFromHgvsp(alteration);;
    }

    public String getAlterationType() {
        return alterationType;
    }

    public void setAlterationType(String alterationType) {
        this.alterationType = alterationType;
    }

    public StructuralVariantType getSvType() {
        return svType;
    }

    public void setSvType(StructuralVariantType svType) {
        this.svType = svType;
    }

    public String getTumorType() {
        return tumorType;
    }

    public void setTumorType(String tumorType) {
        if (tumorType != null) {
            tumorType = tumorType.trim();
        }
        this.tumorType = tumorType;
    }

    public String getConsequence() {
        return consequence;
    }

    public void setConsequence(String consequence) {
        this.consequence = consequence;
    }

    public Integer getProteinStart() {
        return proteinStart;
    }

    public void setProteinStart(Integer proteinStart) {
        this.proteinStart = proteinStart;
    }

    public Integer getProteinEnd() {
        return proteinEnd;
    }

    public void setProteinEnd(Integer proteinEnd) {
        this.proteinEnd = proteinEnd;
    }

    public String getHgvs() {
        return hgvs;
    }

    public void setHgvs(String hgvs) {
        this.hgvs = hgvs;
    }

    public String getHgvsInfo() {
        return this.hgvsInfo;
    }

    public void setHgvsInfo(String hgvsInfo) {
        this.hgvsInfo = hgvsInfo;
    }

    public String getCanonicalTranscript() {
        return this.canonicalTranscript;
    }

    public void setCanonicalTranscript(String canonicalTranscript) {
        this.canonicalTranscript = canonicalTranscript;
    }


    public void enrich() {
        if (this.getEntrezGeneId() == null && this.getHugoSymbol() == null
            && this.getAlteration() != null && !this.getAlteration().isEmpty()) {
            this.setEntrezGeneId(-2);
        }

        // For structural variant, if the entrezGeneId is specified which means this is probably a intragenic event. In this case, the hugoSymbol should be ignore.
        if (this.getAlterationType() != null) {
            AlterationType alterationType = AlterationType.getByName(this.getAlterationType());
            if (alterationType != null && (alterationType.equals(AlterationType.FUSION) ||
                alterationType.equals(AlterationType.STRUCTURAL_VARIANT)) &&
                this.getEntrezGeneId() != null) {
                Gene entrezGeneIdGene = GeneUtils.getGeneByEntrezId(this.getEntrezGeneId());
                this.setHugoSymbol(entrezGeneIdGene.getHugoSymbol());
            }
            if (this.getAlteration() != null &&
                !this.getAlteration().toLowerCase().contains("fusion") &&
                (!this.getAlteration().toLowerCase().contains(FUSION_SEPARATOR) && this.getAlteration().toLowerCase().contains(FUSION_ALTERNATIVE_SEPARATOR)) &&
                (alterationType.equals(AlterationType.FUSION) || (this.consequence != null && this.consequence.toLowerCase().equals("fusion")))
            ) {
                this.setAlteration(this.getAlteration() + " Fusion");
            }
        }

        // Set the alteration to empty string in order to get relevant variants.
        if (this.getAlteration() == null) {
            this.setAlteration("");
        }

        this.setAlteration(QueryUtils.getAlterationName(this));
    }

    @JsonIgnore
    public String getQueryId() {

        List<String> content = new ArrayList<>();
        if (this.entrezGeneId != null) {
            content.add(Integer.toString(this.entrezGeneId));
        } else {
            if (this.hugoSymbol != null) {
                content.add(this.hugoSymbol);
            } else {
                content.add("");
            }
        }
        if (this.alteration != null) {
            content.add(this.alteration);
        } else {
            content.add("");
        }
        if (this.alterationType != null) {
            content.add(this.alterationType);
        } else {
            content.add("");
        }
        if (this.svType != null) {
            content.add(this.svType.name());
        } else {
            content.add("");
        }
        if (this.tumorType != null) {
            content.add(this.tumorType);
        } else {
            content.add("");
        }
        if (consequence != null) {
            content.add(this.consequence);
        } else {
            content.add("");
        }
        if (this.proteinStart != null) {
            content.add(Integer.toString(this.proteinStart));
        } else {
            content.add("");
        }

        if (this.proteinEnd != null) {
            content.add(Integer.toString(this.proteinEnd));
        } else {
            content.add("");
        }

        if (this.hgvs != null) {
            content.add(this.hgvs);
        } else {
            content.add("");
        }

        return StringUtils.join(content.toArray(), "&");
    }
}


