/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.commons.mitz;

import com.olv.integrations.commons.api.FatalIntegrationException;
import com.olv.integrations.commons.api.IntegrationsResponse;
import com.olv.integrations.commons.api.IntegrationsSupportingInformation;
import com.olv.integrations.commons.api.supportinginformation.IdentifierIce;
import com.olv.integrations.commons.config.IntegrationAction;
import com.olv.integrations.commons.config.authorization.IntegrationSystemSamlSecurityConfiguration;
import com.olv.integrations.commons.mitz.config.MitzIntegrationSystemConfiguration;
import com.olv.integrations.commons.utils.FhirMapperImpl;
import com.olv.integrations.commons.utils.IntegrationPatientUtils;
import com.olv.integrations.commons.utils.IntegrationUtils;
import com.parsek.vitaly.integration.v3.Hl7v3Util;
import com.parsek.vitaly.security.server.authdata.AuthData;
import com.parsek.vitaly.xua.user.XuaAuthDataJsonHolder;
import com.parsek.vitaly.xua.user.XuaSamlCallbackProcessor;
import ihe.iti.xcpd._2009.PatientLocationQueryRequestType;
import ihe.iti.xcpd._2009.PatientLocationQueryResponseType;
import ihe.iti.xcpd._2009.RespondingGatewayPLQPortType;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.inject.Instance;
import java.util.List;
import java.util.StringJoiner;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.hapi.fluentpath.FhirPathR4;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public abstract class MitzIntegrationExecutor<T extends Resource> {

    protected MitzIntegrationSystemConfiguration system; // mitz system is a singular, central one


    protected MitzIntegrationClientFactory clientFactory;
    protected Instance<AuthData> authDataInstance;
    protected IntegrationUtils integrationUtils;
    protected FhirMapperImpl fhirMapper;
    protected FhirPathR4 fhirPathR4;
    protected XuaAuthDataJsonHolder xuaAuthDataJsonHolder;
    protected XuaSamlCallbackProcessor xuaSamlCallbackProcessor;
    protected Hl7v3Util hl7v3Util;
    protected IntegrationPatientUtils integrationPatientUtils;

    /**
     * Since Executor is a @Resource and this class is abstract, it has to be passed through with this abstract method
     */
    protected abstract ManagedExecutorService executor();
    protected abstract List<T> mapPatientLocationQueryResponseType(final PatientLocationQueryResponseType patientLocationQueryResponseType);

    protected void resetAfterConfigChange(final MitzIntegrationSystemConfiguration system) {
        clientFactory.reset();
        this.system = system;
    }

    public IntegrationsResponse<T> query(final IntegrationsSupportingInformation integrationsSupportingInformation) {
        if (system == null) {
            log.warn("No system defined on this integrator.");
            return null;
        }

        return (IntegrationsResponse<T>) initiateQueryRequests(integrationsSupportingInformation);
    }

    private IntegrationsResponse<? extends Resource> initiateQueryRequests(
            final IntegrationsSupportingInformation info) {
        final AuthData authData = authDataInstance.get();
        return querySystem(system, info, authData);
    }

    @SuppressWarnings("unchecked")
    private IntegrationsResponse<? extends Resource> querySystem(final MitzIntegrationSystemConfiguration system,
                                                                 final IntegrationsSupportingInformation info,
                                                                 final AuthData authData) {

        try {
            final IntegrationsResponse<? extends Resource> preflightResponse =
                    integrationUtils.systemPreflightCheck(system, authData.getOrganizationId(),
                                                          authData.getTenant(), info,
                                                          IntegrationAction.QUERY);

            if (preflightResponse != null) {
                return preflightResponse;
            }

            prepareRequestScopedInformation(info, system, authData);

            final List<T> foundResources = queryExternalSystem(system, info);

            for (final T t : foundResources) {
                fhirMapper.map(List.of(t),
                               (res) -> system.getInboundMappingDetails(res,
                                                                        authDataInstance.get().getOrganizationId(),
                                                                        fhirPathR4),
                               info);
            }

            return IntegrationsResponse.createSuccess(foundResources, system.getId());

        } catch (final Exception e) {
            log.error("Error occurred trying to invoke third party system {}", system.getId(), e);
            return IntegrationsResponse.createError(system.getId(), e.getMessage());
        }
    }

    protected List<T> queryExternalSystem(final MitzIntegrationSystemConfiguration system,
                                          final IntegrationsSupportingInformation supportingInfo) {
        final RespondingGatewayPLQPortType client = clientFactory.getMitzClient(system);

        final IdentifierIce patientIdentifierToQueryBy = integrationPatientUtils.getPatientIdentifierToQueryBy(
                supportingInfo.getPatient(),
                supportingInfo.getAllAvailablePatientIdentifiers(),
                system.getPatientAaQuery());

        try {
            final PatientLocationQueryRequestType patientLocationQueryRequestType = new PatientLocationQueryRequestType();
            patientLocationQueryRequestType.setRequestedPatientId(
                    hl7v3Util.createII(patientIdentifierToQueryBy.getValue(),
                                       patientIdentifierToQueryBy.getSystem(),
                                       system.getXcpdAssigningAuthorityName()));
            final PatientLocationQueryResponseType patientLocationQueryResponseType = client.respondingGatewayPatientLocationQuery(
                    patientLocationQueryRequestType);

            return mapPatientLocationQueryResponseType(patientLocationQueryResponseType);

        } catch (final Exception e) {
            log.error("Error:", e);
            throw new FatalIntegrationException(getErrorMessage(e));
        }
    }

    protected void prepareRequestScopedInformation(final IntegrationsSupportingInformation intDate,
                                                   final MitzIntegrationSystemConfiguration system,
                                                   final AuthData authData) {
        final IntegrationSystemSamlSecurityConfiguration authorization = system.getAuthorization();
        if (authorization == null) {
            return;
        }
        xuaAuthDataJsonHolder.setAuthData(authData);
        xuaAuthDataJsonHolder.setIntegrationsData(intDate);
        xuaAuthDataJsonHolder.setSystemSecurityConfiguration(authorization);
        xuaSamlCallbackProcessor.setXuaJndiName(authorization.getJndi());
    }

    private String getErrorMessage(final Exception exception) {
        final StringJoiner stringBuilder = new StringJoiner(" // ");
        stringBuilder.add(exception.getMessage());
        if (exception.getCause() != null) {
            stringBuilder.add(exception.getCause().getMessage());
        }
        return stringBuilder.toString();
    }
}
