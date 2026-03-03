/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.localization.mitz;

import com.olv.integrations.commons.api.IntegrationsResponse;
import com.olv.integrations.commons.config.IntegrationAction;
import com.olv.integrations.documents.api.DocumentsSupportingInformation.XdsAction;
import com.olv.integrations.localization.api.LocalizationIntegrator;
import com.olv.integrations.localization.api.LocalizationSupportingInformation;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@Stateless
public class LocalizationMitzIntegrator implements LocalizationIntegrator {

    private final LocalizationMitzIntegratorExecutor mitzIntegratorExecutor;
    private final LocalizationMitzIntegratorConfiguration configuration;

    @Inject
    public LocalizationMitzIntegrator(final LocalizationMitzIntegratorExecutor mitzIntegratorExecutor,
                                      final LocalizationMitzIntegratorConfiguration configuration) {
        this.mitzIntegratorExecutor = mitzIntegratorExecutor;
        this.configuration = configuration;
    }

    @Override
    public boolean canHandle(final LocalizationSupportingInformation integrationsSupportingInformation,
                             final IntegrationAction integrationAction) {
        if (configuration.disabled() || configuration.mitzSystem() == null) {
            log.info("Configuration {} is disabled.", getClass().getName());
            return false;
        }

        if (!requestedSystemExists(integrationsSupportingInformation,
                                   Collections.singletonList(configuration.mitzSystem()))) {
            log.info("Requesting specific systems ({}) none of which exist in config of this integrator {}",
                     integrationsSupportingInformation.getRequestingSystems(),
                     getName());
            return false;
        }

        if (!configuration.mitzSystem().getEnabledActions().contains(integrationAction)) {
            log.info("Mitz system can not handle integrationAction {}", integrationAction);
            return false;
        }
        return integrationsSupportingInformation.getResourceType() == null
                || integrationsSupportingInformation.getResourceType() == ResourceType.DocumentReference;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public IntegrationsResponse<DocumentReference> query(
            final LocalizationSupportingInformation integrationsSupportingInformation) {
        // just to make sure all is well with generic implementation
        integrationsSupportingInformation.setResourceType(ResourceType.DocumentReference);
        return mitzIntegratorExecutor.query(integrationsSupportingInformation);
    }

    @Override
    public IntegrationsResponse<DocumentReference> create(
            final LocalizationSupportingInformation integrationsSupportingInformation) {
        return null;
    }

    @Override
    public IntegrationsResponse<DocumentReference> read(
            final LocalizationSupportingInformation integrationsSupportingInformation) {
        return null;
    }

    @Override
    public IntegrationsResponse<DocumentReference> upload(
            final LocalizationSupportingInformation integrationsSupportingInformation) {
        return null;
    }

    @Override
    public IntegrationsResponse<DocumentReference> download(
            final LocalizationSupportingInformation integrationsSupportingInformation) {
        return null;
    }

    @Override
    public IntegrationsResponse<DocumentReference> update(
            final LocalizationSupportingInformation integrationsSupportingInformation) {
        return null;
    }

    @Override
    public IntegrationsResponse<DocumentReference> delete(
            final LocalizationSupportingInformation integrationsSupportingInformation) {
        return null;
    }
}
