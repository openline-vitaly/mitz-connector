# MITZ Connector — Reference Implementation

> **Note:** This is a reference implementation only. It does **not** compile or run out of the box due to missing internal dependencies (Vitaly backend platform libraries, Parsek internal frameworks, etc.). It is provided as a structural and architectural reference for connecting to the MITZ (NL national consent registry) open query interface.

## What is MITZ?

MITZ is the Dutch national consent registry. It allows healthcare providers to perform a **Patient Location Query (PLQ)** based on IHE XCPD to discover which healthcare organisations hold records for a given patient, and what data categories (gegevenscategorieën / GTZ codes) the patient has consented to share.


## Project Structure

```
mitz-connector/
├── commons/
│   ├── cxf/            # Apache CXF client infrastructure (SOAP, logging, SSL)
│   └── mitz/           # MITZ-specific client factory and executor (core integration logic)
└── localization/
    ├── api/            # Localization integrator API (LocalizationIntegrator, SupportingInformation)
    ├── mitz/           # MITZ localization integrator implementation
    └── gateway/        # WildFly/Jakarta EE WAR gateway exposing the localization REST endpoint
```

## How It Works

### Protocol

Communication with MITZ uses **IHE XCPD (Cross-Community Patient Discovery)** over SOAP/WS-Addressing. The operation invoked is `PatientLocationQuery`.

1. A `PatientLocationQueryRequestType` is constructed with the patient's BSN (Dutch citizen service number) as the requested patient identifier (assigning authority OID: `2.16.840.1.113883.2.4.6.3`).
2. The request is sent via a JAX-WS CXF client (`RespondingGatewayPLQPortType`) generated from the IHE XCPD WSDL.
3. The response (`PatientLocationQueryResponseType`) contains a list of `PatientLocationResponse` entries, each describing:
   - **HomeCommunityId** — OID of the healthcare community holding patient data
   - **CorrespondingPatientId / RequestedPatientId** — patient identifier echo
   - **SourceId** — OID of the specific source system
   - **event-code** — GTZ data categories (e.g. `GGC002`, `GGC012`, `GGC013`)
   - **author-institution** — UZI register identifier of the care organisation (assigning authority: CIBG, OID `2.16.528.1.1007.3.3`)

### Authentication

Requests are authenticated with an **XUA SAML assertion** carrying the following claims (configurable per system):

| Claim namespace | Description |
|---|---|
| `urn:oasis:names:tc:xacml:2.0:subject:role` | Role code (e.g. GP = `01.015` in `RoleCodeNL`) |
| `urn:ihe:iti:xua:2017:subject:provider-identifier` | UZI personal number of the requesting healthcare professional |
| `urn:nl:otv:names:tc:1.0:subject:mandated` | Mandated practitioner UZI number |
| `urn:nl:otv:names:tc:1.0:subject:provider-institution` | UZI organisation number (URA) |
| `urn:nl:otv:names:tc:1.0:subject:consulting-healthcare-facility-type-code` | Facility type code |
| `urn:oasis:names:tc:xspa:1.0:subject:purposeofuse` | Purpose of use (e.g. `TREAT`) |

The SAML token is obtained via a Parsek-internal XUA STS client (`XuaTokenSource`) configured through a JNDI reference.

### Key Classes

| Class | Location | Purpose |
|---|---|---|
| `MitzIntegrationClientFactory` | `commons/mitz` | Builds and caches the JAX-WS CXF client for the XCPD PLQ endpoint |
| `MitzIntegrationExecutor` | `commons/mitz` | Abstract executor: runs preflight checks, prepares XUA context, calls MITZ, maps response to FHIR R4 resources |
| `MitzIntegrationSystemConfiguration` | `commons/mitz/config` | Configuration POJO: MITZ URL, XCPD assigning authority name, timeouts, SSL, logging |
| `LocalizationMitzIntegrator` | `localization/mitz` | CDI integrator bean wiring configuration, executor and client factory for localization use case |
| `LocalizationMitzIntegratorExecutor` | `localization/mitz` | Concrete executor extending `MitzIntegrationExecutor`, maps PLQ response to FHIR `DocumentReference` resources |
| `LocalizationMitzIntegratorConfigurationFactory` | `localization/mitz` | Loads and produces `MitzIntegrationSystemConfiguration` from JSON at runtime |
| `LocalizationIntegrationsGateway` | `localization/gateway` | CDI bean orchestrating query/send/read operations across configured integrators |
| `LocalizationIntegrationsResourceImpl` | `localization/gateway` | Jakarta REST endpoint exposing `query`, `create`, `read`, `update`, `delete` at `/api/` |

## System Configuration Example

System configuration is provided as JSON (loaded at runtime via the Parsek configuration framework):

```json
{
  "id": "mitz",
  "patient_aa_query": "2.16.840.1.113883.2.4.6.3",
  "logging": true,
  "ssl_client_authentication": false,
  "common_name_check_disabled": true,
  "connect_timeout": 3000,
  "socket_timeout": 5000,
  "mitzUrl": "https://<mitz-host>/mitz",
  "authorization": {
    "jndi": "java:global/ParsekStsTokenSource/ParsekXuaStsTokenSource!com.parsek.vitaly.xua.user.XuaTokenSource",
    "logging": true,
    "org_based_token": true,
    "assertion_issuer": "xds-bridge-xua-proxy",
    "token_claims": [
      {
        "namespace": "urn:oasis:names:tc:xacml:2.0:subject:role",
        "type": "role",
        "value": [{ "code": "01.015", "system": "2.16.840.1.113883.2.4.15.111", "systemName": "RoleCodeNL", "display": "Huisarts" }]
      }
    ]
  }
}
```

## Missing Dependencies

This project depends on internal Vitaly/Parsek platform libraries that are not publicly available:

- `com.parsek.vitaly:vitaly-backend` — parent POM and shared BOM
- `com.parsek.vitaly:vitaly-backend-commons` — shared utilities
- `com.parsek.vitaly:vitaly-backend-integration-cache` — integration cache
- `com.parsek.vitaly:parsek-config` — configuration framework
- `com.parsek.vitaly:vitaly-auditing-service-ejb-*` — auditing
- `com.parsek.vitaly.xua.*` — XUA/SAML token handling
- `com.parsek.vitaly.integration.v3.Hl7v3Util` — HL7v3 utilities
- IHE XCPD generated WSDL stubs (`ihe.iti.xcpd._2009.*`)

## Runtime Environment

The gateway WAR is designed to deploy on **WildFly / JBoss EAP** with:
- Jakarta EE 10 (CDI, JAX-RS, EJB)
- FHIR R4 (HAPI FHIR)
- Apache CXF for SOAP/WS-Addressing
