package org.mskcc.cbio.oncokb.model.oncotree;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.Objects;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-04-04T17:16:11.368Z")
public class Links extends ArrayList<Link> {


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Links links = (Links) o;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Links {\n");
        sb.append("  " + super.toString()).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
