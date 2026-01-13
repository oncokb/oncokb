
package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.model.InheritanceMechanism;

import java.io.Serializable;

@ApiModel(description = "")
public class GenomicIndicator implements Serializable {
    @ApiModelProperty(value = "")
    private String name;

    @ApiModelProperty(value = "")
    private String description;
    
    @ApiModelProperty(value = "")
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
