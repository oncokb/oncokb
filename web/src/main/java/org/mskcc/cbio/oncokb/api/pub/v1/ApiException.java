package org.mskcc.cbio.oncokb.api.pub.v1;

@javax.annotation.Generated(
    value = "class io.swagger.codegen.languages.SpringCodegen",
    date = "2016-10-14T18:47:53.991Z")

public class ApiException extends Exception {
    private int code;

    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}
