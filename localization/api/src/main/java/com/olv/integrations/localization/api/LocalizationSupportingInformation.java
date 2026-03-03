/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.localization.api;

import com.olv.integrations.commons.api.IntegrationsSupportingInformation;
import com.olv.integrations.commons.api.cache.IntegratorCacheConfig;
import com.olv.integrations.documents.api.DocumentsSupportingInformation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuperBuilder(toBuilder = true)
@Jacksonized
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LocalizationSupportingInformation extends DocumentsSupportingInformation {


    @Override
    public boolean cacheKeyEqualsShallow(final IntegrationsSupportingInformation other) {
        throw new UnsupportedOperationException(
                "ImagingStudySupportingInformation does not support cacheKeyEqualsShallow method");
    }

    @Override
    public boolean isCacheable(final IntegratorCacheConfig cacheConfig) {
        throw new UnsupportedOperationException(
                "ImagingStudySupportingInformation does not support isCacheable method");
    }
}
