/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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
 * @author Benjamin Gross
 * @author Selcuk Onur Sumer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariantAnnotation {
    private String variant;        // used as an id for mongodb record
    private String annotationJSON; // raw annotation JSON

    @SerializedName("id")
    private String variantId;     // variant id
    @SerializedName("assembly_name")
    private String assemblyName;  // NCBI build number
    @SerializedName("seq_region_name")
    private String seqRegionName; // chromosome
    private String start;         // start position
    private String end;           // end position
    @SerializedName("allele_string")
    private String alleleString;  // reference allele & variant allele
    private String strand;
    @SerializedName("most_severe_consequence")
    private String mostSevereConsequence;
    @SerializedName("transcript_consequences")
    private List<TranscriptConsequence> transcriptConsequences;

    public VariantAnnotation() {
        this(null, null);
    }

    public VariantAnnotation(String variant) {
        this(variant, null);
    }

    public VariantAnnotation(String variant, String annotationJSON) {
        this.variant = variant;
        this.annotationJSON = annotationJSON;
    }

    @JsonProperty(required = true)
    @ApiModelProperty(value = "Variant key", required = true)
    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    @JsonProperty(required = true)
    @ApiModelProperty(value = "Annotation data as JSON string", required = true)
    public String getAnnotationJSON() {
        return annotationJSON;
    }

    public void setAnnotationJSON(String annotationJSON) {
        this.annotationJSON = annotationJSON;
    }

    @JsonProperty(value = "id", required = true)
    @ApiModelProperty(value = "Variant id", required = true)
    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    @JsonProperty(value = "assembly_name", required = true)
    @ApiModelProperty(value = "NCBI build number", required = false)
    public String getAssemblyName() {
        return assemblyName;
    }

    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }

    @JsonProperty(value = "seq_region_name", required = true)
    @ApiModelProperty(value = "Chromosome", required = false)
    public String getSeqRegionName() {
        return seqRegionName;
    }

    public void setSeqRegionName(String seqRegionName) {
        this.seqRegionName = seqRegionName;
    }

    @JsonProperty(value = "start", required = true)
    @ApiModelProperty(value = "Start position", required = false)
    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    @JsonProperty(value = "end", required = true)
    @ApiModelProperty(value = "End position", required = false)
    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    @JsonProperty(value = "allele_string", required = true)
    @ApiModelProperty(value = "Allele string (e.g: A/T)", required = false)
    public String getAlleleString() {
        return alleleString;
    }

    public void setAlleleString(String alleleString) {
        this.alleleString = alleleString;
    }

    @JsonProperty(value = "strand", required = true)
    @ApiModelProperty(value = "Strand (negative or positive)", required = false)
    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    @JsonProperty(value = "most_severe_consequence", required = true)
    @ApiModelProperty(value = "Most severe consequence", required = false)
    public String getMostSevereConsequence() {
        return mostSevereConsequence;
    }

    public void setMostSevereConsequence(String mostSevereConsequence) {
        this.mostSevereConsequence = mostSevereConsequence;
    }

    @JsonProperty(value = "transcript_consequences", required = true)
    @ApiModelProperty(value = "List of transcripts", required = false)
    public List<TranscriptConsequence> getTranscriptConsequences() {
        return transcriptConsequences;
    }

    public void setTranscriptConsequences(List<TranscriptConsequence> transcriptConsequences) {
        this.transcriptConsequences = transcriptConsequences;
    }

    @Override
    public String toString() {
        return annotationJSON;
    }
}
