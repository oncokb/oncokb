package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


/**
 * ApiListResp
 */
@javax.annotation.Generated(
    value = "class io.swagger.codegen.languages.SpringCodegen",
    date = "2016-10-14T18:47:53.991Z")

public class ApiObjectResp {
    private Object data;

    private Meta meta = null;

    public ApiObjectResp data(Object data) {
        this.data = data;
        return this;
    }

    /**
     * Get data
     *
     * @return data
     **/
    @ApiModelProperty(value = "")
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public ApiObjectResp meta(Meta meta) {
        this.meta = meta;
        return this;
    }

    /**
     * Get meta
     *
     * @return meta
     **/
    @ApiModelProperty(value = "")
    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
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
        ApiObjectResp apiListResp = (ApiObjectResp) o;
        return Objects.equals(this.data, apiListResp.data) &&
            Objects.equals(this.meta, apiListResp.meta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, meta);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ApiListResp {\n");

        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("    meta: ").append(toIndentedString(meta)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

