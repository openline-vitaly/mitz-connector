/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.localization.gateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.extension.Extension;
import com.olv.integrations.commons.api.IntegrationsResponse;
import com.olv.integrations.commons.api.supportinginformation.IdentifierIce;
import com.olv.integrations.commons.tests.CustomXPathEvaluation;
import com.olv.integrations.commons.tests.RelatesToWireMockTemplateTransformer;
import com.olv.integrations.localization.api.LocalizationSupportingInformation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.DocumentReference;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(Arquillian.class)
public class LocalizationMitzIntegrationsIT extends LocalizationAbstractIntegrationsIT {

    @Override
    protected Extension[] serverExtensions() {
        return new RelatesToWireMockTemplateTransformer[]{new RelatesToWireMockTemplateTransformer(true)};
    }

    @Test
    public void query_mitz() throws IOException {
        applyConfig(MITZ_PROPERTIES, MITZ_JSON, "localization-mitz-integrator-system.json");

        final LocalizationSupportingInformation suppInfo = LocalizationSupportingInformation.builder()
                .patient(new IdentifierIce("2.16.840.1.113883.2.4.6.3", "900203638"))
                .allAvailablePatientIdentifiers(
                        Arrays.asList(new IdentifierIce("2.16.840.1.113883.2.4.6.3", "900203638")))
                .build();

        final String xml = new String(
                getClass().getClassLoader()
                        .getResourceAsStream("systems/mitz/mitz_example_resp.xml")
                        .readAllBytes(),
                StandardCharsets.UTF_8
        );
        zero.stubFor(post(anyUrl()).willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/soap+xml; charset=UTF-8")
                        .withTransformers("response-template")
                        .withBody(xml)));

        final IntegrationsResponse<DocumentReference> queryResponse = iceLocalizationClient.getLocalizationIceResource()
                .query(suppInfo);

        // Define namespaces for XPath
        final Map<String, String> namespaces = Map.of(
                "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                "wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
                "soap", "http://www.w3.org/2003/05/soap-envelope",
                "saml2", "urn:oasis:names:tc:SAML:2.0:assertion",
                "xua", "urn:ihe:iti:xua",
                "hl7v3", "urn:hl7-org:v3",
                "xcpd", "urn:ihe:iti:xcpd:2009"
        );

        // Assert security header attributes
        zero.verify(postRequestedFor(anyUrl())
                // Assert Role attribute
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:oasis:names:tc:xacml:2.0:subject:role']/saml2:AttributeValue/hl7v3:Role[@code='01.015']",
                        namespaces,
                        null
                ))
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:oasis:names:tc:xacml:2.0:subject:role']/saml2:AttributeValue/hl7v3:Role[@codeSystem='2.16.840.1.113883.2.4.15.111']",
                        namespaces,
                        null
                ))
                // Assert provider-identifier attribute
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:ihe:iti:xua:2017:subject:provider-identifier']/saml2:AttributeValue/hl7v3:id[@root='2.16.528.1.1007.3.1']",
                        namespaces,
                        null
                ))
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:ihe:iti:xua:2017:subject:provider-identifier']/saml2:AttributeValue/hl7v3:id[@extension='123456782']",
                        namespaces,
                        null
                ))
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:ihe:iti:xua:2017:subject:provider-identifier']/saml2:AttributeValue/hl7v3:id[@assigningAuthorityName='CIBG']",
                        namespaces,
                        null
                ))
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:ihe:iti:xua:2017:subject:provider-identifier']/saml2:AttributeValue/hl7v3:id[@displayable='true']",
                        namespaces,
                        null
                ))
                // Assert mandated attribute
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:nl:otv:names:tc:1.0:subject:mandated']/saml2:AttributeValue/hl7v3:id[@root='2.16.528.1.1007.3.1']",
                        namespaces,
                        null
                ))
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:nl:otv:names:tc:1.0:subject:mandated']/saml2:AttributeValue/hl7v3:id[@extension='123456789']",
                        namespaces,
                        null
                ))
                // Assert provider-institution attribute
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:nl:otv:names:tc:1.0:subject:provider-institution']/saml2:AttributeValue/hl7v3:InstanceIdentifier[@root='2.16.528.1.1007.3.3']",
                        namespaces,
                        null
                ))
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:nl:otv:names:tc:1.0:subject:provider-institution']/saml2:AttributeValue/hl7v3:InstanceIdentifier[@extension='00000659']",
                        namespaces,
                        null
                ))
                // Assert consulting-healthcare-facility-type-code attribute
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:nl:otv:names:tc:1.0:subject:consulting-healthcare-facility-type-code']/saml2:AttributeValue/hl7v3:InstanceIdentifier[@code='Z3']",
                        namespaces,
                        null
                ))
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:nl:otv:names:tc:1.0:subject:consulting-healthcare-facility-type-code']/saml2:AttributeValue/hl7v3:InstanceIdentifier[@codeSystem='2.16.840.1.113883.2.4.15.1060']",
                        namespaces,
                        null
                ))
                // Assert purposeofuse attribute
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:oasis:names:tc:xspa:1.0:subject:purposeofuse']/saml2:AttributeValue/hl7v3:CodedValue[@code='TREAT']",
                        namespaces,
                        null
                ))
                .withRequestBody(new CustomXPathEvaluation(
                        "//saml2:Attribute[@Name='urn:oasis:names:tc:xspa:1.0:subject:purposeofuse']/saml2:AttributeValue/hl7v3:CodedValue[@codeSystem='2.16.840.1.113883.1.11.20448']",
                        namespaces,
                        null
                ))
                // Assert PatientLocationQueryRequest in body
                .withRequestBody(new CustomXPathEvaluation(
                        "//xcpd:PatientLocationQueryRequest/xcpd:RequestedPatientId[@root='2.16.840.1.113883.2.4.6.3']",
                        namespaces,
                        null
                ))
                .withRequestBody(new CustomXPathEvaluation(
                        "//xcpd:PatientLocationQueryRequest/xcpd:RequestedPatientId[@extension='900203638']",
                        namespaces,
                        null
                ))
                .withoutHeader("Authorization"));

        // Assert response
        assertEquals(7, queryResponse.getPayloads().size());

        // Verify first response mapping (index 0)
        final DocumentReference firstDoc = queryResponse.getPayloads().get(0);

        // Assert HomeCommunityId mapping to identifier
        assertEquals("urn:oid:2.16.840.1.113883.2.4.6.6.1",
                firstDoc.getIdentifier().stream()
                        .filter(id -> id.getSystem().equals("homeCommunityId"))
                        .map(org.hl7.fhir.r4.model.Identifier::getValue)
                        .findFirst()
                        .orElse(null));

        // Assert CorrespondingPatientId mapping to subject
        assertEquals("2.16.840.1.113883.2.4.6.3", firstDoc.getSubject().getIdentifier().getSystem());
        assertEquals("900203638", firstDoc.getSubject().getIdentifier().getValue());

        // Assert RequestedPatientId mapping to context.sourcePatientInfo
        assertEquals("2.16.840.1.113883.2.4.6.3",
                firstDoc.getContext().getSourcePatientInfo().getIdentifier().getSystem());
        assertEquals("900203638",
                firstDoc.getContext().getSourcePatientInfo().getIdentifier().getValue());

        // Assert SourceId mapping to custodian
        assertEquals("urn:oid:2.16.840.1.113883.2.4.6.6.90000017",
                firstDoc.getCustodian().getIdentifier().getValue());

        // Assert author-institution mapping to author
        assertEquals(1, firstDoc.getAuthor().size());
        assertEquals("2.16.528.1.1007.3.3", firstDoc.getAuthor().get(0).getIdentifier().getSystem());
        assertEquals("90000697", firstDoc.getAuthor().get(0).getIdentifier().getValue());

        // Verify second response mapping (index 1) - different HomeCommunityId and author
        final DocumentReference secondDoc = queryResponse.getPayloads().get(1);

        assertEquals("urn:oid:2.16.840.1.113883.2.4.3.164.4",
                secondDoc.getIdentifier().stream()
                        .filter(id -> id.getSystem().equals("homeCommunityId"))
                        .map(org.hl7.fhir.r4.model.Identifier::getValue)
                        .findFirst()
                        .orElse(null));
        assertEquals("urn:oid:2.16.840.1.113883.2.4.3.164.4.1234",
                secondDoc.getCustodian().getIdentifier().getValue());
        assertEquals("00005098", secondDoc.getAuthor().get(0).getIdentifier().getValue());

        // Verify fourth response mapping (index 3) - another unique HomeCommunityId
        final DocumentReference fourthDoc = queryResponse.getPayloads().get(3);

        assertEquals("urn:oid:2.16.840.1.113883.2.4.3.2.1.7392.1.25.1",
                fourthDoc.getIdentifier().stream()
                        .filter(id -> id.getSystem().equals("homeCommunityId"))
                        .map(org.hl7.fhir.r4.model.Identifier::getValue)
                        .findFirst()
                        .orElse(null));
        assertEquals("urn:oid:2.16.840.1.113883.2.4.3.2.1.7392.1.25.1.1.4.3",
                fourthDoc.getCustodian().getIdentifier().getValue());
        assertEquals("00014332", fourthDoc.getAuthor().get(0).getIdentifier().getValue());

        // Verify last response mapping (index 6) - Epic system
        final DocumentReference lastDoc = queryResponse.getPayloads().get(6);

        assertEquals("urn:oid:1.2.840.114350.1.13.222.3.7.3.688884.100",
                lastDoc.getIdentifier().stream()
                        .filter(id -> id.getSystem().equals("homeCommunityId"))
                        .map(org.hl7.fhir.r4.model.Identifier::getValue)
                        .findFirst()
                        .orElse(null));
        assertEquals("urn:oid:1.2.840.114350.1.13.222.3.7.3.688884.100",
                lastDoc.getCustodian().getIdentifier().getValue());
        assertEquals("00065543", lastDoc.getAuthor().get(0).getIdentifier().getValue());

        // Verify all documents have consistent patient identifiers
        queryResponse.getPayloads().forEach(doc -> {
            assertEquals("2.16.840.1.113883.2.4.6.3", doc.getSubject().getIdentifier().getSystem());
            assertEquals("900203638", doc.getSubject().getIdentifier().getValue());
        });
    }

}
