/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.commons.mitz;

import com.olv.integrations.commons.config.authorization.IntegrationSystemSamlSecurityConfiguration;
import com.olv.integrations.commons.cxf.CxfClientProducer;
import com.olv.integrations.commons.cxf.CxfLoggingInformation;
import com.olv.integrations.commons.cxf.CxfSslInformation;
import com.olv.integrations.commons.mitz.config.MitzIntegrationSystemConfiguration;
import ihe.iti.xcpd._2009.RespondingGatewayPLQPortType;
import ihe.iti.xcpd._2009.XCPD;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

/**
 * Produces instances of JAX-WS clients for XDS.b registry and repository.
 */

@Slf4j
@NoArgsConstructor
@ApplicationScoped
public class MitzIntegrationClientFactory {

    private final XCPD xcpdService = new XCPD();
    private CxfClientProducer cxfClientProducer;
    private RespondingGatewayPLQPortType mitzClient;

    @Inject
    public MitzIntegrationClientFactory(final CxfClientProducer cxfClientProducer) {
        this.cxfClientProducer = cxfClientProducer;
    }

    public void reset() {
        log.info("Resetting MitzIntegrationClientFactory client.");
        mitzClient = null;
    }

    public RespondingGatewayPLQPortType getMitzClient(final MitzIntegrationSystemConfiguration configuration) {
        if (mitzClient != null) {
            return mitzClient;
        }
        log.debug("Mitz client doesn't exist yet. Constructing Mitz client based on {}",
                  configuration);

        final RespondingGatewayPLQPortType client = xcpdService
                .getRespondingGatewayPLQPortSoap(new WSAddressingFeature());

        configureClient(client, configuration, configuration.getMitzUrl());

        mitzClient = client;

        return client;
    }

    private void configureClient(final Object jaxWsClient,
                                 final MitzIntegrationSystemConfiguration configuration,
                                 final String url) {
        final IntegrationSystemSamlSecurityConfiguration authorization = configuration.getAuthorization();
        cxfClientProducer.configureClient(jaxWsClient,
                                          url,
                                          configuration.getSocketTimeout(),
                                          configuration.getConnectTimeout(),
                                          CxfLoggingInformation.builder()
                                                  .loggingEnabled(configuration.isLogging())
                                                  .build(),
                                          authorization == null ? null : authorization.getJndi(),
                                          CxfSslInformation.builder()
                                                  .sslAuthEnabled(configuration.isSslClientAuth())
                                                  .alias(configuration.getSslClientAuthKeyAlias())
                                                  .cnCheckDisabled(configuration.isCommonNameCheckDisabled())
                                                  .build());
    }
}
