package org.mskcc.cbio.oncokb.controller.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

/**
 * Define your global exception handlers
 */

@ControllerAdvice
public class ExceptionTranslator {
    @ExceptionHandler(ApiHttpErrorException.class)
    public final ResponseEntity<ApiHttpError> handleException(ApiHttpErrorException ex, WebRequest request) {
        String path = request.getDescription(false).replaceFirst("uri=", "");
        ApiHttpError error = new ApiHttpError(path, ex.getHttpStatus(), ex.getMessage());
        return new ResponseEntity<>(error, ex.getHttpStatus());
    }

    @ExceptionHandler(org.oncokb.oncokb_transcript.ApiException.class)
    public final ResponseEntity<ApiHttpError> handleTranscriptApiException(org.oncokb.oncokb_transcript.ApiException ex, WebRequest request) {
        String path = request.getDescription(false).replaceFirst("uri=", "");
        HttpStatus httpStatus = getHttpStatusFromException(ex.getCode());
        ApiHttpError error = new ApiHttpError(path, httpStatus, ex.getMessage());
        return new ResponseEntity<>(error, httpStatus);
    }

    @ExceptionHandler(org.genome_nexus.ApiException.class)
    public final ResponseEntity<ApiHttpError> handleTranscriptApiException(org.genome_nexus.ApiException ex, WebRequest request) {
        String path = request.getDescription(false).replaceFirst("uri=", "");
        HttpStatus httpStatus = getHttpStatusFromException(ex.getCode());
        String message = ex.getMessage();
        if (StringUtils.isEmpty(message)) {
            message = "Error annotating variant(s) using Genome Nexus";
        }
        ApiHttpError error = new ApiHttpError(path, httpStatus, message);
        return new ResponseEntity<>(error, httpStatus);
    }

    private HttpStatus getHttpStatusFromException(int statusCode) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        try {
            httpStatus = HttpStatus.valueOf(statusCode);
        } catch (IllegalArgumentException e) {
            // Some exception may not provide a status code. In these cases
            // we will use the default Internal Server Error code
        }
        return httpStatus;
    }
}
