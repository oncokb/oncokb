
package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.model.InheritanceMechanism;

import java.io.Serializable;

@ApiModel(description = "clinical syndromes associated with specific germline variants/alleles that describe the clinical consequences associated with that variant")
public class GenomicIndicator implements Serializable {
    @ApiModelProperty(value = "Name of the clinical syndrome")
    private String name;

    @ApiModelProperty(value = "Description of the genomic indicator")
    private String description;
    
    @ApiModelProperty(value = "Describes how a genetic trait/allele may be passed to offspring")
    private InheritanceMechanism inheritanceMechanism;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InheritanceMechanism getInheritanceMechanism() {
        return inheritanceMechanism;
    }

    public void setInheritanceMechanism(InheritanceMechanism inheritanceMechanism) {
        this.inheritanceMechanism = inheritanceMechanism;
    }
}
