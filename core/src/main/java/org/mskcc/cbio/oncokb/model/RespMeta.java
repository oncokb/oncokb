package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Hongxin on 5/8/16.
 */
public class RespMeta {
    private String error_type;
    private String OAuthException;
    private Integer code;
    private String error_message;

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("error_type")
    public String getError_type() {
        return error_type;
    }

    public void setError_type(String error_type) {
        this.error_type = error_type;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("oauth_exception")
    public String getOAuthException() {
        return OAuthException;
    }

    public void setOAuthException(String OAuthException) {
        this.OAuthException = OAuthException;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("code")
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("error_message")
    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RespMeta respMeta = (RespMeta) o;

        if (error_type != null ? !error_type.equals(respMeta.error_type) : respMeta.error_type != null) return false;
        if (OAuthException != null ? !OAuthException.equals(respMeta.OAuthException) : respMeta.OAuthException != null)
            return false;
        if (!code.equals(respMeta.code)) return false;
        return error_message != null ? error_message.equals(respMeta.error_message) : respMeta.error_message == null;

    }

    @Override
    public int hashCode() {
        int result = error_type != null ? error_type.hashCode() : 0;
        result = 31 * result + (OAuthException != null ? OAuthException.hashCode() : 0);
        result = 31 * result + code.hashCode();
        result = 31 * result + (error_message != null ? error_message.hashCode() : 0);
        return result;
    }
}
