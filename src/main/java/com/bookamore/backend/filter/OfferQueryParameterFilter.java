package com.bookamore.backend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OfferQueryParameterFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !"GET".equals(request.getMethod())
                || !("/api/v1/offers".equals(path) || "/api/v1/offers/with-book".equals(path))
                || !(request.getParameterMap().containsKey("genres")
                     || request.getParameterMap().containsKey("sortBy")
                     || request.getParameterMap().containsKey("sortDir"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Map<String, String[]> parameters = new LinkedHashMap<>(request.getParameterMap());
        String[] genres = parameters.remove("genres");
        if (genres != null) {
            parameters.putIfAbsent("genre", genres);
        }
        if (!parameters.containsKey("sort")
                && (parameters.containsKey("sortBy") || parameters.containsKey("sortDir"))) {
            String sortBy = parameterOrDefault(request, "sortBy", "createdDate");
            String sortDir = parameterOrDefault(request, "sortDir", "desc");
            parameters.put("sort", new String[]{sortBy + "," + sortDir});
        }
        parameters.remove("sortBy");
        parameters.remove("sortDir");
        Map<String, String[]> normalizedParameters = Collections.unmodifiableMap(parameters);

        filterChain.doFilter(new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                String[] values = getParameterValues(name);
                return values == null || values.length == 0 ? null : values[0];
            }

            @Override
            public String[] getParameterValues(String name) {
                return normalizedParameters.get(name);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return Collections.enumeration(normalizedParameters.keySet());
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return normalizedParameters;
            }
        }, response);
    }

    private String parameterOrDefault(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return value == null || value.isEmpty() ? defaultValue : value;
    }
}
