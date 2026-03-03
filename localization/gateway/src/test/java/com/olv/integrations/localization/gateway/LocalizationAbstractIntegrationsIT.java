/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.localization.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.olv.integrations.commons.tests.AbstractIceIT;
import com.olv.integrations.localization.mitz.LocalizationMitzIntegratorConfiguration;
import com.olv.integrations.localization.rest.client.IceLocalizationClient;
import com.parsek.vitaly.testutils.KeycloakLogin;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class LocalizationAbstractIntegrationsIT extends AbstractIceIT {

    protected final String MITZ_PROPERTIES = "localization-mitz-integrator.properties";
    protected final String MITZ_JSON = "localization-mitz-integrator-system.json";

    @Inject
    public Instance<LocalizationMitzIntegratorConfiguration> mitzIntegratorConfigurations;

    @Override
    protected void reloadConfig() {
        mitzIntegratorConfigurations.get().reload();
    }

    protected final WireMockServer zero = new WireMockServer(
            WireMockConfiguration.options()
                    .port(9280)
                    .jettyHeaderBufferSize(32768)
                    .extensions(serverExtensions()));

    @Inject
    protected IceLocalizationClient iceLocalizationClient;

    @Inject
    private KeycloakLogin keycloakLogin;

    @Override
    public KeycloakLogin keycloakLogin() {
        return keycloakLogin;
    }

    @Override
    protected List<WireMockServer> wireMockServers() {
        return Arrays.asList(zero);
    }

    protected com.github.tomakehurst.wiremock.extension.Extension[] serverExtensions() {
        return new com.github.tomakehurst.wiremock.extension.Extension[0];
    }

    @Override
    public Set<String> configurationFiles() {
        final Set<String> iceLocalizationFiles = new HashSet<>();
        iceLocalizationFiles.add(MITZ_PROPERTIES);
        iceLocalizationFiles.add(MITZ_JSON);
        return iceLocalizationFiles;
    }

}
