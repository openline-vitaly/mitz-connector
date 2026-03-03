/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.localization.gateway.fixture;


import com.olv.integrations.localization.gateway.LocalizationAbstractIntegrationsIT;
import com.olv.integrations.localization.gateway.LocalizationMitzIntegrationsIT;
import java.io.File;
import java.util.List;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class DeploymentFactory {

    public WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(ZipImporter.class, "test.war")
                .importFrom(new File("../../../_deployables/vitaly-backend-integrations-localization-gateway.war"))
                .as(WebArchive.class);

        webArchive
                .addClasses(
                        LocalizationMitzIntegrationsIT.class,
                        LocalizationAbstractIntegrationsIT.class)
                .addPackage("com.olv.integrations.localization.gateway.fixture")
                .addAsResource("ice-localization-client.properties")
                .addAsResource("systems")
                .addAsWebInfResource("jboss-web.xml")
                .addAsWebInfResource("beans.xml")
                .addAsWebInfResource("ejb-jar.xml")
                .addAsWebInfResource("jboss-deployment-structure.xml");

        addTestUtils(webArchive);

        System.out.println(webArchive.toString(true));

        return webArchive;
    }

    private void addTestUtils(final WebArchive webArchive) {
        final File[] files = Maven.configureResolver()
                .workOffline(true)
                .loadPomFromFile("pom.xml")
                .resolve(
                        List.of(
                                "org.assertj:assertj-core",
                                "com.parsek.vitaly:vitaly-backend-integrations-localization-rest-client",
                                "com.parsek.vitaly:vitaly-backend-test-utils",
                                "com.parsek.vitaly:vitaly-backend-services-commons-client",
                                "com.parsek.vitaly:vitaly-backend-services-commons-rest",
                                "com.parsek.vitaly:vitaly-backend-integrations-commons-tests",
                                "com.parsek.vitaly:vitaly-patient-manager-service-ejb-client",
                                "com.parsek.vitaly:vitaly-patient-manager-service-ejb-api",
                                "com.parsek.vitaly:vitaly-backend-integrations-commons-rest-client",
                                "com.parsek.vitaly:vitaly-patient-manager-service-ejb-client",
                                "io.jsonwebtoken:jjwt-api",
                                "io.jsonwebtoken:jjwt-impl",
                                "com.parsek.vitaly:vitaly-patient-manager-service-ejb-api",
                                "com.parsek.vitaly:vitaly-integration-service-ejb-client",
                                "com.parsek.vitaly:vitaly-integration-service-ejb-api"
                        )
                )
                .withoutTransitivity().asFile();
        webArchive.addAsLibraries(files);

        final File[] additionalFiles = Maven.configureResolver()
                .workOffline(true)
                .loadPomFromFile("pom.xml")
                .resolve(
                        List.of(
                                "org.mockito:mockito-core",
                                "com.github.tomakehurst:wiremock"
                        )
                )
                .withTransitivity().asFile();
        webArchive.addAsLibraries(additionalFiles);
    }

}
