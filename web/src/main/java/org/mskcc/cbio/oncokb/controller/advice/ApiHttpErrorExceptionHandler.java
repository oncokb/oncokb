package org.mskcc.cbio.oncokb.controller.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ApiHttpErrorExceptionHandler {
    @ExceptionHandler(ApiHttpErrorException.class)
    public final ResponseEntity<ApiHttpError> handleException(ApiHttpErrorException ex, WebRequest request) {
        String path = request.getDescription(false).replaceFirst("uri=", "");
        ApiHttpError error = new ApiHttpError(path, ex.getHttpStatus(), ex.getMessage());
        return new ResponseEntity<>(error, ex.getHttpStatus());
    }
}
