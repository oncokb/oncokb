package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


/**
 * Meta
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-14T18:47:53.991Z")

public class Meta {
    private Integer code = null;

    private String errorMessage = null;

    private String errorType = null;

    public Meta code(Integer code) {
        this.code = code;
        return this;
    }

    /**
     * Get code
     *
     * @return code
     **/
    @ApiModelProperty(value = "")
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Meta errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * Get errorMessage
     *
     * @return errorMessage
     **/
    @ApiModelProperty(value = "")
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Meta errorType(String errorType) {
        this.errorType = errorType;
        return this;
    }

    /**
     * Get errorType
     *
     * @return errorType
     **/
    @ApiModelProperty(value = "")
    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Meta meta = (Meta) o;
        return Objects.equals(this.code, meta.code) &&
            Objects.equals(this.errorMessage, meta.errorMessage) &&
            Objects.equals(this.errorType, meta.errorType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, errorMessage, errorType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Meta {\n");

        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
        sb.append("    errorType: ").append(toIndentedString(errorType)).append("\n");
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

