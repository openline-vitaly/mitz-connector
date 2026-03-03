/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.localization.mitz;

import com.parsek.config.api.ConfigReloadEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.aeonbits.owner.ConfigFactory;

public class LocalizationMitzIntegratorConfigurationFactory {

    @Inject
    private Event<ConfigReloadEvent<LocalizationMitzIntegratorConfiguration>> configEvent;


    @Produces
    @ApplicationScoped
    public LocalizationMitzIntegratorConfiguration getConfig() {
        LocalizationMitzIntegratorConfiguration config = ConfigFactory.create(LocalizationMitzIntegratorConfiguration.class);
        config.addReloadListener(event -> configEvent.fire(new ConfigReloadEvent<>(config, event)));
        return config;
    }
}
