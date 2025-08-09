package com.devapps.just_team_backend.utils;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.IOException;
import java.util.regex.Pattern;

public class UrlSanitizationFilter implements Filter {

    private static final Pattern MALICIOUS_CHARACTERS = Pattern.compile("%0A|%0D");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpRequest.getRequestURI();

        // Sanitize the URL
        String sanitizedURI = MALICIOUS_CHARACTERS.matcher(requestURI).replaceAll("");

        // Wrap the request with the sanitized URI
        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
            @Override
            public String getRequestURI() {
                return sanitizedURI;
            }
        };

        chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
