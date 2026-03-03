/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.commons.cxf;

import com.parsek.vitaly.xua.user.XuaOutInterceptor;
import jakarta.inject.Inject;
import jakarta.xml.ws.BindingProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.HTTPConduit;

@Slf4j
public class CxfClientProducer {

    private XuaOutInterceptor xuaUserOutInterceptor;
    private CxfSslClientAuthentication cxfSslClientAuthentication;

    @Inject
    public CxfClientProducer(final XuaOutInterceptor xuaUserOutInterceptor,
                             final CxfSslClientAuthentication cxfSslClientAuthentication) {
        this.xuaUserOutInterceptor = xuaUserOutInterceptor;
        this.cxfSslClientAuthentication = cxfSslClientAuthentication;
    }

    public void configureClient(final Object jaxWsClient,
                                 final String endpointAddress,
                                 final Integer socketTimeout,
                                 final Integer connectTimeout,
                                 final CxfLoggingInformation cxfLoggingInformation,
                                 final String xuaJndi,
                                 final CxfSslInformation cxfSslInformation) {
        final String jaxWsClientName = jaxWsClient.getClass().getSimpleName();

        log.debug("Configuring endpoint address on {}: {}", jaxWsClientName, endpointAddress);
        final BindingProvider bindingProvider = (BindingProvider) jaxWsClient;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

        final Client client = ClientProxy.getClient(jaxWsClient);

        if (cxfLoggingInformation.isLoggingEnabled()) {
            log.debug("Logging enabled for this system: {}", cxfLoggingInformation);
            client.getInInterceptors().add(new CxfLoggingInInterceptor(cxfLoggingInformation));
            client.getOutInterceptors().add(new CxfLoggingOutInterceptor(cxfLoggingInformation));

        }

        if (StringUtils.isNotEmpty(xuaJndi)) {
            log.debug("Configuring XUA on {}", jaxWsClientName);
            client.getOutInterceptors().add(xuaUserOutInterceptor);
        }

        client.getOutInterceptors().add(new AbstractSoapInterceptor(Phase.WRITE) {
            @Override
            public void handleMessage(final SoapMessage soapMessage) throws Fault {
                ((SoapHeader) soapMessage.getHeaders().get(0)).setMustUnderstand(true);
            }
        });


        if (cxfSslInformation.isSslAuthEnabled()) {
            cxfSslClientAuthentication.setupTLSClientAuth((HTTPConduit) client.getConduit(),
                                                          cxfSslInformation.isCnCheckDisabled(),
                                                          cxfSslInformation.getAlias());
        }

        if (cxfSslInformation.isCnCheckDisabled()) {
            cxfSslClientAuthentication.disableCnCheck((HTTPConduit) client.getConduit());
        }

        if (connectTimeout != null) {
            ((HTTPConduit) client.getConduit()).getClient().setConnectionTimeout(connectTimeout);
        }

        if (socketTimeout != null) {
            ((HTTPConduit) client.getConduit()).getClient().setReceiveTimeout(socketTimeout);
        }
    }
}
