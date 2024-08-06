
package org.mskcc.cbio.oncokb.controller.advice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiHttpErrorException extends Exception {

    private ResponseEntity<Object> responseEntity;

    public ResponseEntity<Object> getResponseEntity() {
        return responseEntity;
    }

    public ApiHttpErrorException(ResponseEntity<Object> responseEntity) {
        super(responseEntity.getBody().toString());
        this.responseEntity = responseEntity;
    }

    public ApiHttpErrorException(String message, HttpStatus httpStats) {
        this(new ResponseEntity<>(message, httpStats));
    }

}
