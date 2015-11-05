package org.mskcc.cbio.oncokb.model;

/**
 * Created by Hongxin on 11/5/15.
 */
public class StringResponse {
    private String response;
    public StringResponse(String s) {
        this.response = s;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
