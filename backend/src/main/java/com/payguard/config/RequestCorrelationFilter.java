package com.payguard.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";
    private static final int MAX_LENGTH = 100;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String incoming = request.getHeader(HEADER);
        String correlationId = isValid(incoming)
                ? incoming.trim()
                : UUID.randomUUID().toString();

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private boolean isValid(String value) {
        return value != null
                && !value.isBlank()
                && value.length() <= MAX_LENGTH
                && value.matches("[A-Za-z0-9._:-]+");
    }
}
