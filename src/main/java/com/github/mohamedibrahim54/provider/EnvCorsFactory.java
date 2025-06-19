package com.github.mohamedibrahim54.provider;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.cors.CorsFactory;

@AutoService(CorsFactory.class)
public class EnvCorsFactory implements CorsFactory {

    private static final String PROVIDER_ID = "env-cors";

    @Override
    public Cors create(KeycloakSession session) {
        return new EnvCors(session);
    }

    @Override
    public void init(Config.Scope config) {
        // No initialization needed
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // No post-initialization needed
    }

    @Override
    public void close() {
        // No resources to clean up
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
