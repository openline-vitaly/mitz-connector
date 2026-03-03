/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.localization.gateway.fixture;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

@ArquillianSuiteDeployment
public class SuiteDeploymentIT {

    @Deployment(name = "baseDeploy", order = 1)
    public static WebArchive createSuiteDeployment() {
        return new DeploymentFactory().createDeployment();
    }

}
