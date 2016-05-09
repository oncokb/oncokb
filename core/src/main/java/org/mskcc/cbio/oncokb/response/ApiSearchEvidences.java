package org.mskcc.cbio.oncokb.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.model.Evidence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class ApiSearchEvidences {

    private List<Evidence> data = new ArrayList<Evidence>();


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("data")
    public List<Evidence> getData() {
        return data;
    }

    public void setData(List<Evidence> data) {
        this.data = data;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiSearchEvidences response = (ApiSearchEvidences) o;
        return Objects.equals(data, response.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ApiSearchEvidences {\n");

        sb.append("  data: ").append(data).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
