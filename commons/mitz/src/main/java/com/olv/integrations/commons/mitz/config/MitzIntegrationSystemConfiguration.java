/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.commons.mitz.config;

import com.olv.integrations.commons.config.IntegrationSystemConfiguration;
import com.olv.integrations.commons.config.authorization.IntegrationSystemSamlSecurityConfiguration;
import lombok.Data;

@Data
public class MitzIntegrationSystemConfiguration extends IntegrationSystemConfiguration<IntegrationSystemSamlSecurityConfiguration> {

    private String mitzUrl;
    private String xcpdAssigningAuthorityName;

}
