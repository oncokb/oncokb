
package org.mskcc.cbio.oncokb.controller.advice;

import org.springframework.http.HttpStatus;

public class ApiHttpErrorException extends Exception {

    private HttpStatus httpStatus;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ApiHttpErrorException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
