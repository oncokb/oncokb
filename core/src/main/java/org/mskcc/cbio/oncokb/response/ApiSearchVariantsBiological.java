package org.mskcc.cbio.oncokb.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.model.BiologicalVariant;
import org.mskcc.cbio.oncokb.model.RespMeta;

import java.util.HashSet;
import java.util.Set;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-10T02:49:36.208Z")
public class ApiSearchVariantsBiological {

    private Set<BiologicalVariant> data = new HashSet<>();
    private RespMeta meta = new RespMeta();


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("data")
    public Set<BiologicalVariant> getData() {
        return data;
    }

    public void setData(Set<BiologicalVariant> data) {
        this.data = data;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("meta")
    public RespMeta getMeta() {
        return meta;
    }

    public void setMeta(RespMeta meta) {
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiSearchVariantsBiological that = (ApiSearchVariantsBiological) o;

        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        if (meta != null ? !meta.equals(that.meta) : that.meta != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (meta != null ? meta.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApiSearchVariantsBiological{" +
            "data=" + data +
            ", meta=" + meta +
            '}';
    }
}
