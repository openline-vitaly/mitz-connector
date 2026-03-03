/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.commons.cxf;

import com.parsek.vitaly.VitalyKeyStoreUtils;
import jakarta.inject.Inject;
import javax.net.ssl.KeyManager;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;


public class CxfSslClientAuthentication {

    private VitalyKeyStoreUtils keyStoreUtils;

    @Inject
    public CxfSslClientAuthentication(final VitalyKeyStoreUtils keyStoreUtils) {
        this.keyStoreUtils = keyStoreUtils;
    }

    /**
     * Sets up client authentication and optionally also disables CN check.
     */
    public void setupTLSClientAuth(final HTTPConduit conduit,
                                   final boolean disableCnCheck) {
        final KeyManager[] keyManagers = keyStoreUtils.getKeyManagers(null);

        setupTLSClientAuth(conduit, disableCnCheck, keyManagers);
    }

    public void setupTLSClientAuth(final HTTPConduit conduit,
                                   final boolean disableCnCheck,
                                   final String alias) {
        final KeyManager[] keyManagers = keyStoreUtils.getKeyManagers(alias);
        setupTLSClientAuth(conduit, disableCnCheck, keyManagers);
    }

    public void setupTLSClientAuth(final HTTPConduit conduit,
                                   final boolean disableCnCheck,
                                   final KeyManager[] keyManagers) {
        final TLSClientParameters tlsClientParameters = getTlsClientParameters(conduit);

        tlsClientParameters.setUseHttpsURLConnectionDefaultSslSocketFactory(false);


        tlsClientParameters.setKeyManagers(keyManagers);

        final HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setAutoRedirect(true);
        conduit.setClient(httpClientPolicy);

        if (disableCnCheck) {
            disableCnCheck(conduit);
        }
    }

    /**
     * Only disables CN check
     */
    public void disableCnCheck(final HTTPConduit conduit) {
        final TLSClientParameters tlsClientParameters = getTlsClientParameters(conduit);

        tlsClientParameters.setDisableCNCheck(true);
    }

    /**
     * Returns existing TLS client parameters from the HTTPConduit or creates new ones if existing don't exist.
     */
    private TLSClientParameters getTlsClientParameters(final HTTPConduit conduit) {
        final TLSClientParameters existingParameters = conduit.getTlsClientParameters();
        if (existingParameters == null) {
            final TLSClientParameters params = new TLSClientParameters();
            conduit.setTlsClientParameters(params);
            return params;
        }
        return existingParameters;
    }
}
