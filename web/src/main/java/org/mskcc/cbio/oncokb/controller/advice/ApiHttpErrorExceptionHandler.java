package org.mskcc.cbio.oncokb.controller.advice;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ApiHttpErrorExceptionHandler {
    @ExceptionHandler(ApiHttpErrorException.class)
    public final ResponseEntity<Object> handleException(ApiHttpErrorException ex, WebRequest request) {
        return ex.getResponseEntity();
    }
}
