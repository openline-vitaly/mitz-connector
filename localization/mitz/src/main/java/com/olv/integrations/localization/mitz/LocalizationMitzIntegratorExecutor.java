/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.localization.mitz;

import com.olv.integrations.commons.mitz.MitzIntegrationClientFactory;
import com.olv.integrations.commons.mitz.MitzIntegrationExecutor;
import com.olv.integrations.commons.utils.FhirMapperImpl;
import com.olv.integrations.commons.utils.IntegrationPatientUtils;
import com.olv.integrations.commons.utils.IntegrationUtils;
import com.parsek.config.api.ConfigReloadEvent;
import com.parsek.vitaly.integration.v3.Hl7v3Util;
import com.parsek.vitaly.security.server.authdata.AuthData;
import com.parsek.vitaly.util.VitalyConstants;
import com.parsek.vitaly.xua.user.XuaAuthDataJsonHolder;
import com.parsek.vitaly.xua.user.XuaSamlCallbackProcessor;
import ihe.iti.xcpd._2009.PatientLocationQueryResponseType;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class LocalizationMitzIntegratorExecutor extends MitzIntegrationExecutor<DocumentReference> {

    @jakarta.annotation.Resource(lookup = "java:jboss/ee/concurrency/executor/ice-localization-mitz")
    private ManagedExecutorService executor;

    @Inject
    public LocalizationMitzIntegratorExecutor(
            final LocalizationMitzIntegratorConfiguration configuration,
            final MitzIntegrationClientFactory clientFactory,
            final Instance<AuthData> authDataInstance, final IntegrationUtils integrationUtils,
            final FhirMapperImpl fhirMapper,
            final FhirPathR4 fhirPathR4, final XuaAuthDataJsonHolder xuaAuthDataJsonHolder,
            final XuaSamlCallbackProcessor xuaSamlCallbackProcessor,
            final Hl7v3Util hl7v3Util,
            final IntegrationPatientUtils integrationPatientUtils) {
        super(configuration.mitzSystem(), clientFactory, authDataInstance, integrationUtils, fhirMapper, fhirPathR4,
              xuaAuthDataJsonHolder,
              xuaSamlCallbackProcessor, hl7v3Util, integrationPatientUtils);
    }

    private void resetStateAfterConfigHotReload(
            @Observes final ConfigReloadEvent<LocalizationMitzIntegratorConfiguration> event) {
        if (!event.getReloadEvent().getEvents().isEmpty()) {
            resetAfterConfigChange(
                    event.getConfig().mitzSystem() == null ? null : event.getConfig().mitzSystem());
        }
    }

    @Override
    protected List<DocumentReference> mapPatientLocationQueryResponseType(
            final PatientLocationQueryResponseType patientLocationQueryResponseType) {
        if (patientLocationQueryResponseType == null
                || patientLocationQueryResponseType.getPatientLocationResponse() == null) {
            return List.of();
        }

        final List<DocumentReference> documentReferences = new ArrayList<>();

        patientLocationQueryResponseType.getPatientLocationResponse().forEach(response -> {
            try {
                final DocumentReference docRef = new DocumentReference();

                // Map HomeCommunityId to identifier
                Optional.ofNullable(response.getHomeCommunityId())
                        .ifPresent(homeCommunityId -> {
                            final Identifier identifier = new Identifier();
                            identifier.setSystem(VitalyConstants.HOME_COMMUNITY_ID);
                            identifier.setValue(homeCommunityId);
                            docRef.addIdentifier(identifier);
                        });

                // Map CorrespondingPatientId to subject
                Optional.ofNullable(response.getCorrespondingPatientId())
                        .ifPresent(correspondingPatientId -> {
                            final Reference subject = new Reference();
                            final Identifier patientIdentifier = new Identifier();

                            Optional.ofNullable(correspondingPatientId.getRoot())
                                    .ifPresent(patientIdentifier::setSystem);
                            Optional.ofNullable(correspondingPatientId.getExtension())
                                    .ifPresent(patientIdentifier::setValue);

                            subject.setIdentifier(patientIdentifier);
                            docRef.setSubject(subject);
                        });

                // Map RequestedPatientId to context.sourcePatientInfo
                Optional.ofNullable(response.getRequestedPatientId())
                        .ifPresent(requestedPatientId -> {
                            final DocumentReference.DocumentReferenceContextComponent context =
                                    docRef.hasContext() ? docRef.getContext()
                                            : new DocumentReference.DocumentReferenceContextComponent();

                            final Reference sourcePatientInfo = new Reference();
                            final Identifier patientIdentifier = new Identifier();

                            Optional.ofNullable(requestedPatientId.getRoot())
                                    .ifPresent(patientIdentifier::setSystem);
                            Optional.ofNullable(requestedPatientId.getExtension())
                                    .ifPresent(patientIdentifier::setValue);

                            sourcePatientInfo.setIdentifier(patientIdentifier);
                            context.setSourcePatientInfo(sourcePatientInfo);
                            docRef.setContext(context);
                        });

                // Map SourceId to custodian
                Optional.ofNullable(response.getSourceId())
                        .ifPresent(sourceId -> {
                            final Reference custodian = new Reference();
                            final Identifier custodianIdentifier = new Identifier();
                            custodianIdentifier.setValue(sourceId);
                            custodian.setIdentifier(custodianIdentifier);
                            docRef.setCustodian(custodian);
                        });

                // Map author-institution to author
                Optional.ofNullable(response.getAuthorInstitution())
                        .ifPresent(authorInstitution -> {
                            final Reference author = new Reference();
                            final Identifier authorIdentifier = new Identifier();

                            Optional.ofNullable(authorInstitution.getRoot())
                                    .ifPresent(authorIdentifier::setSystem);
                            Optional.ofNullable(authorInstitution.getExtension())
                                    .ifPresent(authorIdentifier::setValue);

                            author.setIdentifier(authorIdentifier);
                            docRef.addAuthor(author);
                        });

                documentReferences.add(docRef);
            } catch (Exception e) {
                log.error("Error mapping PatientLocationResponse to DocumentReference", e);
            }
        });

        return documentReferences;
    }

    @Override
    protected ManagedExecutorService executor() {
        return executor;
    }


}
