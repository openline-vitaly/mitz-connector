/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.localization.mitz;

import static org.aeonbits.owner.Config.HotReload;
import static org.aeonbits.owner.Config.HotReloadType;
import static org.aeonbits.owner.Config.LoadPolicy;
import static org.aeonbits.owner.Config.LoadType;
import static org.aeonbits.owner.Config.Sources;

import com.olv.integrations.commons.mitz.config.MitzIntegrationSystemConfiguration;
import com.parsek.config.api.JsonConfigConverter;
import org.aeonbits.owner.Reloadable;


@LoadPolicy(LoadType.MERGE)
@Sources({
        "system:properties",
        "system:env",
        "file:${parsek.config.dir}/localization-mitz-integrator.properties",
        "classpath:/localization-mitz-integrator.properties"})
@HotReload(type = HotReloadType.ASYNC)
public interface LocalizationMitzIntegratorConfiguration extends Reloadable {

    @Key("disabled")
    @DefaultValue("false")
    boolean disabled();

    @Key("mitzSystem")
    @ConverterClass(JsonConfigConverter.class)
    MitzIntegrationSystemConfiguration mitzSystem();
}
