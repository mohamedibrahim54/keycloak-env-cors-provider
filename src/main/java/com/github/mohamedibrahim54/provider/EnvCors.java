package com.github.mohamedibrahim54.provider;

import java.util.*;

import jakarta.ws.rs.core.Response.ResponseBuilder;

import org.jboss.logging.Logger;
import org.keycloak.http.HttpRequest;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.WebOriginsUtils;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.cors.Cors;

public class EnvCors implements Cors {

    private static final Logger logger = Logger.getLogger(EnvCors.class);
    private static final String CORS_ALLOW_ORIGINS_ENV = "CORS_ALLOW_ORIGINS";

    private final HttpRequest request;
    private final HttpResponse response;
    // This field is set in the builder method but not used elsewhere
    // It's kept for potential future use or compatibility with the Cors interface
    private ResponseBuilder builder;
    private Set<String> allowedOrigins;
    private Set<String> allowedMethods;
    private Set<String> exposedHeaders;

    private boolean preflight;
    private boolean auth;


    EnvCors(KeycloakSession session) {
        this.request = session.getContext().getHttpRequest();
        this.response = session.getContext().getHttpResponse();
    }

    private List<String> getAllowedOrigins() {
        String allowedOriginsEnv = System.getenv(CORS_ALLOW_ORIGINS_ENV);
        if (allowedOriginsEnv == null || allowedOriginsEnv.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> origins = new ArrayList<>();
        for (String origin : allowedOriginsEnv.split(",")) {
            String trimmed = origin.trim();
            if (!trimmed.isEmpty()) {
                origins.add(trimmed);
            }
        }
        return origins;
    }

    @Override
    public Cors builder(ResponseBuilder builder) {
        this.builder = builder;
        return this;
    }

    @Override
    public Cors preflight() {
        preflight = true;
        return this;
    }

    @Override
    public Cors auth() {
        auth = true;
        return this;
    }

    @Override
    public Cors allowAllOrigins() {
        allowedOrigins = Collections.singleton(ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD);
        return this;
    }

    @Override
    public Cors allowedOrigins(KeycloakSession session, ClientModel client) {
        if (client != null) {
            allowedOrigins = WebOriginsUtils.resolveValidWebOrigins(session, client);
        }
        return this;
    }

    @Override
    public Cors allowedOrigins(AccessToken token) {
        if (token != null) {
            allowedOrigins = token.getAllowedOrigins();
        }
        return this;
    }

    @Override
    public Cors allowedOrigins(String... allowedOrigins) {
        if (allowedOrigins != null && allowedOrigins.length > 0) {
            this.allowedOrigins = new HashSet<>(Arrays.asList(allowedOrigins));
        }
        return this;
    }

    @Override
    public Cors allowedMethods(String... allowedMethods) {
        this.allowedMethods = new HashSet<>(Arrays.asList(allowedMethods));
        return this;
    }

    @Override
    public Cors exposedHeaders(String... exposedHeaders) {
        if (this.exposedHeaders == null) {
            this.exposedHeaders = new HashSet<>();
        }

        this.exposedHeaders.addAll(Arrays.asList(exposedHeaders));

        return this;
    }

    @Override
    public void add() {
        if (request == null) {
            throw new IllegalStateException("request is not set");
        }

        if (response == null) {
            throw new IllegalStateException("response is not set");
        }

        String origin = request.getHttpHeaders().getRequestHeaders().getFirst(ORIGIN_HEADER);
        if (origin == null) {
            logger.trace("No Origin header, ignoring");
            return;
        }

        // Check if we have a wildcard and need to restrict to env origins
        if (allowedOrigins != null && allowedOrigins.contains(ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD)) {
            List<String> envAllowedOrigins = getAllowedOrigins();
            if (!envAllowedOrigins.isEmpty()) {
                // Replace wildcard with specific origins from environment
                allowedOrigins.remove(ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD);
                allowedOrigins.addAll(envAllowedOrigins);
                logger.debug("Replaced wildcard with origins from environment: " + allowedOrigins);
            }
        }

        // Skip origin check for preflight requests
        if (preflight) {
            // Continue processing for preflight requests
        } else if (allowedOrigins == null) {
            // No origins are allowed
            if (logger.isDebugEnabled()) {
                logger.debugv("Invalid CORS request: no allowed origins configured");
            }
            return;
        } else if (!allowedOrigins.contains(origin) && !allowedOrigins.contains(ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD)) {
            // Origin is not in the allowed list and wildcard is not enabled
            if (logger.isDebugEnabled()) {
                logger.debugv("Invalid CORS request: origin {0} not in allowed origins {1}", origin, allowedOrigins);
            }
            return;
        }

        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.toString(auth));

        if (preflight) {
            // Handle preflight request headers
            if (allowedMethods != null) {
                response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, CollectionUtil.join(allowedMethods));
            } else {
                response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, DEFAULT_ALLOW_METHODS);
            }

            // Set allowed headers
            if (auth) {
                response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, String.format("%s, %s", DEFAULT_ALLOW_HEADERS, AUTHORIZATION_HEADER));
            } else {
                response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, DEFAULT_ALLOW_HEADERS);
            }

            // Set max age
            response.setHeader(ACCESS_CONTROL_MAX_AGE, String.valueOf(DEFAULT_MAX_AGE));
        } else {
            // Handle actual request headers
            if (exposedHeaders != null) {
                response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, CollectionUtil.join(exposedHeaders));
            }
        }
    }

    @Override
    public void close() {
        // No resources to clean up
    }
}
