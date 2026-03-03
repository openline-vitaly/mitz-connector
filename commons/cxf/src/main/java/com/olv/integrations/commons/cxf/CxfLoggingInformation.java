/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.commons.cxf;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CxfLoggingInformation {

    private boolean loggingEnabled;
    private boolean logFullPayload;
    private String logToFile;
    private boolean logBinary;
}
