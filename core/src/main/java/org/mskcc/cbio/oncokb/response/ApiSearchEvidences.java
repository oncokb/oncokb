package org.mskcc.cbio.oncokb.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.RespMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class ApiSearchEvidences {

    private Set<Evidence> data = new HashSet<Evidence>();
    private RespMeta meta = null;


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("data")
    public Set<Evidence> getData() {
        return data;
    }

    public void setData(Set<Evidence> data) {
        this.data = data;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("meta")
    public RespMeta getRespMeta() {
        return meta;
    }

    public void setRespMeta(RespMeta meta) {
        this.meta = meta;
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
