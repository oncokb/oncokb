package org.mskcc.cbio.oncokb.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Order(1)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_REQUEST_ID_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_REQUEST_ID_KEY, requestId);

        try {
            if (LOGGER.isDebugEnabled()) {
                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
                ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

                filterChain.doFilter(wrappedRequest, wrappedResponse);

                logRequest(wrappedRequest);
                logResponse(wrappedResponse);
            } else {
                filterChain.doFilter(request, response);
            }
        } finally {
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) throws IOException {
        StringBuilder requestLog = new StringBuilder();
        requestLog.append("Request: ")
                .append(request.getMethod())
                .append(" ")
                .append(request.getRequestURI());

        byte[] requestBody = request.getContentAsByteArray();
        if (requestBody.length > 0) {
            String body = new String(requestBody, StandardCharsets.UTF_8);
            requestLog.append("\nBody: ").append(body);
        }

        LOGGER.debug(requestLog.toString());
    }

    private void logResponse(ContentCachingResponseWrapper response) throws IOException {
        StringBuilder responseLog = new StringBuilder();
        responseLog.append("Response: ")
                .append(response.getStatusCode());

        byte[] responseBody = response.getContentAsByteArray();
        if (responseBody.length > 0) {
            String body = new String(responseBody, StandardCharsets.UTF_8);
            responseLog.append("\nBody: ").append(body);
        }

        LOGGER.debug(responseLog.toString());
        response.copyBodyToResponse();
    }
}

