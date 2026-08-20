
package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.model.InheritanceMechanism;

import java.io.Serializable;
import java.util.List;

@ApiModel(description = "Query for retrieving the germline genomic indicators associated with a specific gene and variant")
public class GenomicIndicatorQuery implements Serializable {
    @ApiModelProperty(value = "The gene symbol used in Human Genome Organisation.")
    private String hugoSymbol;

    @ApiModelProperty(value = "Variant name")
    private String variant;

    @ApiModelProperty(value = "List of inheritance mechanisms used to filter the returned genomic indicators. The special value CARRIER is matched against the genomic indicator name rather than the inheritance mechanism. Example: AUTOSOMAL_DOMINANT,CARRIER")
    private List<InheritanceMechanism> inheritanceMechanisms;

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public List<InheritanceMechanism> getInheritanceMechanisms() {
        return inheritanceMechanisms;
    }

    public void setInheritanceMechanisms(List<InheritanceMechanism> inheritanceMechanisms) {
        this.inheritanceMechanisms = inheritanceMechanisms;
    }
}
