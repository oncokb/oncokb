/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal Genome Nexus.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.oncokb.genomenexus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author Selcuk Onur Sumer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranscriptConsequence {
    @SerializedName("transcript_id")
    private String transcriptId;

    private String hgvsp;
    private String hgvsc;
    @SerializedName("variant_allele")
    private String variantAllele;
    private String codons;
    @SerializedName("protein_id")
    private String proteinId;
    @SerializedName("protein_start")
    private String proteinStart;
    @SerializedName("protein_end")
    private String proteinEnd;
    @SerializedName("gene_symbol")
    private String geneSymbol;
    @SerializedName("gene_id")
    private String geneId;
    @SerializedName("amino_acids")
    private String aminoAcids;
    @SerializedName("hgnc_id")
    private String hgncId;
    private String canonical;

    @SerializedName("refseq_transcript_ids")
    private List<String> refseqTranscriptIds;
    @SerializedName("consequence_terms")
    private List<String> consequenceTerms;

    // Following properties are intended to be set during enrichment

    private String hgvspShort;
    private String refSeq;
    private String codonChange;
    private Boolean isHotspot;
    private List<Hotspot> hotspots;
    private String consequence;

    public TranscriptConsequence() {
        this(null);
    }

    public TranscriptConsequence(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    @JsonProperty(value = "transcript_id", required = true)
    @ApiModelProperty(value = "Ensembl transcript id", required = true)
    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    @JsonProperty(value = "hgvsp", required = true)
    @ApiModelProperty(value = "HGVSp", required = false)
    public String getHgvsp() {
        return hgvsp;
    }

    public void setHgvsp(String hgvsp) {
        this.hgvsp = hgvsp;
    }

    @JsonProperty(value = "hgvsc", required = true)
    @ApiModelProperty(value = "HGVSc", required = false)
    public String getHgvsc() {
        return hgvsc;
    }

    public void setHgvsc(String hgvsc) {
        this.hgvsc = hgvsc;
    }

    @JsonProperty(value = "variant_allele", required = true)
    @ApiModelProperty(value = "Variant allele", required = false)
    public String getVariantAllele() {
        return variantAllele;
    }

    public void setVariantAllele(String variantAllele) {
        this.variantAllele = variantAllele;
    }

    @JsonProperty(value = "codons", required = true)
    @ApiModelProperty(value = "Codons", required = false)
    public String getCodons() {
        return codons;
    }

    public void setCodons(String codons) {
        this.codons = codons;
    }

    @JsonProperty(value = "protein_id", required = true)
    @ApiModelProperty(value = "Ensembl protein id", required = false)
    public String getProteinId() {
        return proteinId;
    }

    public void setProteinId(String proteinId) {
        this.proteinId = proteinId;
    }

    @JsonProperty(value = "protein_start", required = true)
    @ApiModelProperty(value = "Protein start position", required = false)
    public String getProteinStart() {
        return proteinStart;
    }

    public void setProteinStart(String proteinStart) {
        this.proteinStart = proteinStart;
    }

    @JsonProperty(value = "protein_end", required = true)
    @ApiModelProperty(value = "Protein end position", required = false)
    public String getProteinEnd() {
        return proteinEnd;
    }

    public void setProteinEnd(String proteinEnd) {
        this.proteinEnd = proteinEnd;
    }

    @JsonProperty(value = "gene_symbol", required = true)
    @ApiModelProperty(value = "Hugo gene symbol", required = false)
    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    @JsonProperty(value = "gene_id", required = true)
    @ApiModelProperty(value = "Ensembl gene id", required = false)
    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    @JsonProperty(value = "amino_acids", required = true)
    @ApiModelProperty(value = "Amino acids", required = false)
    public String getAminoAcids() {
        return aminoAcids;
    }

    public void setAminoAcids(String aminoAcids) {
        this.aminoAcids = aminoAcids;
    }

    @JsonProperty(value = "hgnc_id", required = true)
    @ApiModelProperty(value = "HGNC id", required = false)
    public String getHgncId() {
        return hgncId;
    }

    public void setHgncId(String hgncId) {
        this.hgncId = hgncId;
    }

    @JsonProperty(value = "canonical", required = true)
    @ApiModelProperty(value = "Canonical transcript indicator", required = false)
    public String getCanonical() {
        return canonical;
    }

    public void setCanonical(String canonical) {
        this.canonical = canonical;
    }

    @JsonProperty(value = "refseq_transcript_ids", required = true)
    @ApiModelProperty(value = "List of RefSeq transcript ids", required = false)
    public List<String> getRefseqTranscriptIds() {
        return refseqTranscriptIds;
    }

    public void setRefseqTranscriptIds(List<String> refseqTranscriptIds) {
        this.refseqTranscriptIds = refseqTranscriptIds;
    }

    @JsonProperty(value = "consequence_terms", required = true)
    @ApiModelProperty(value = "List of consequence terms", required = false)
    public List<String> getConsequenceTerms() {
        return consequenceTerms;
    }

    public void setConsequenceTerms(List<String> consequenceTerms) {
        this.consequenceTerms = consequenceTerms;
    }

    public String getHgvspShort() {
        return hgvspShort;
    }

    public void setHgvspShort(String hgvspShort) {
        this.hgvspShort = hgvspShort;
    }

    public String getRefSeq() {
        return refSeq;
    }

    public void setRefSeq(String refSeq) {
        this.refSeq = refSeq;
    }

    public String getCodonChange() {
        return codonChange;
    }

    public void setCodonChange(String codonChange) {
        this.codonChange = codonChange;
    }

    public Boolean getIsHotspot() {
        return isHotspot;
    }

    public void setIsHotspot(Boolean hotspot) {
        isHotspot = hotspot;
    }

    public List<Hotspot> getHotspots() {
        return hotspots;
    }

    public void setHotspots(List<Hotspot> hotspots) {
        this.hotspots = hotspots;
    }

    public String getConsequence() {
        return consequence;
    }

    public void setConsequence(String consequence) {
        this.consequence = consequence;
    }
}
