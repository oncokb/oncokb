
package org.mskcc.cbio.oncokb.controller.advice;

import org.springframework.http.HttpStatus;

public class ApiHttpErrorException extends Exception {

    private HttpStatus httpStats;

    public HttpStatus getHttpStats() {
        return httpStats;
    }

    public ApiHttpErrorException(String message, HttpStatus httpStats) {
        super(message);
        this.httpStats = httpStats;
    }
}
