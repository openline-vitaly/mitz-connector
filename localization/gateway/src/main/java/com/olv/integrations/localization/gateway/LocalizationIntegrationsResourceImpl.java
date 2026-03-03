/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.localization.gateway;

import com.olv.integrations.commons.api.IntegrationsResponse;
import com.olv.integrations.localization.api.LocalizationSupportingInformation;
import com.olv.integrations.localization.gateway.impl.LocalizationIntegrationsGateway;
import com.olv.integrations.localization.rest.api.LocalizationIntegrationsResource;
import java.io.IOException;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.DocumentReference;


@Slf4j
@NoArgsConstructor
@Path("/")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class LocalizationIntegrationsResourceImpl implements LocalizationIntegrationsResource {

    private LocalizationIntegrationsGateway localizationIntegrationsGateway;

    @Inject
    public LocalizationIntegrationsResourceImpl(final LocalizationIntegrationsGateway localizationIntegrationsGateway) {
        this.localizationIntegrationsGateway = localizationIntegrationsGateway;
    }

    @Override
    public IntegrationsResponse<DocumentReference> query(final LocalizationSupportingInformation integrationsSupportingInformation) {
        return localizationIntegrationsGateway.query(integrationsSupportingInformation);
    }

    @Override
    public IntegrationsResponse<DocumentReference> create(final LocalizationSupportingInformation integrationsSupportingInformation) {
        return localizationIntegrationsGateway.send(integrationsSupportingInformation);
    }

    @Override
    public IntegrationsResponse<DocumentReference> read(final LocalizationSupportingInformation integrationsSupportingInformation) {
        return localizationIntegrationsGateway.read(integrationsSupportingInformation);
    }

    @Override
    public IntegrationsResponse<DocumentReference> update(final LocalizationSupportingInformation integrationsSupportingInformation) {
        return null;
    }

    @Override
    public IntegrationsResponse<DocumentReference> delete(final LocalizationSupportingInformation integrationsSupportingInformation) {
        return null;
    }
}
