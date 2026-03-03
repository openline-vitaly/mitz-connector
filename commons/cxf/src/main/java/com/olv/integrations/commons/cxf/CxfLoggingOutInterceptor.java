/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.commons.cxf;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;

@Slf4j
public class CxfLoggingOutInterceptor extends LoggingOutInterceptor {

    public CxfLoggingOutInterceptor(final CxfLoggingInformation cxfLoggingInformation) {
        super(new VitalyCxfPrettyLoggingFilter(cxfLoggingInformation));
        if (cxfLoggingInformation.isLogFullPayload()) {
            setLimit(-1);
        }
        setPrettyLogging(true);
        setLogBinary(cxfLoggingInformation.isLogBinary());
        setLogMultipart(cxfLoggingInformation.isLogBinary());
    }
}
