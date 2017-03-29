package org.mskcc.cbio.oncokb.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.mskcc.cbio.oncokb.model.MainNumber;
import org.mskcc.cbio.oncokb.model.RespMeta;

import java.util.Objects;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class ApiNumbersMain {

    private MainNumber data = null;
    private RespMeta meta = null;


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("data")
    public MainNumber getData() {
        return data;
    }

    public void setData(MainNumber data) {
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiNumbersMain that = (ApiNumbersMain) o;

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
        return "ApiNumbersMain{" +
            "data=" + data +
            ", meta=" + meta +
            '}';
    }
}
