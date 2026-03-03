/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.localization.gateway.impl;

import com.olv.integrations.commons.ejb.Integrator;
import com.olv.integrations.commons.gateway.IntegrationsGateway;
import com.olv.integrations.localization.api.LocalizationIntegrator;
import com.olv.integrations.localization.api.LocalizationSupportingInformation;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.DocumentReference;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LocalizationIntegrationsGateway extends IntegrationsGateway<DocumentReference, LocalizationSupportingInformation> {

    private Instance<LocalizationIntegrator> integrators;
    @jakarta.annotation.Resource(lookup = "java:jboss/ee/concurrency/executor/ice-gateway")
    private ManagedExecutorService executorService;

    @Inject
    public LocalizationIntegrationsGateway(final Instance<LocalizationIntegrator> integrators,
                                           final RequestContextController requestContextController) {
        super(requestContextController);
        this.integrators = integrators;
    }


    @Override
    protected Instance<? extends Integrator<DocumentReference, LocalizationSupportingInformation>> integrators() {
        return integrators;
    }

    @Override
    protected ManagedExecutorService executorService() {
        return executorService;
    }
}
