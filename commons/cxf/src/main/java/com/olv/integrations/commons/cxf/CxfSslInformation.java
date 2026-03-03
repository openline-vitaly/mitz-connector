/*
 * Copyright (c) by Parsek d.o.o.
 * All Rights Reserved.
 */

package com.olv.integrations.commons.cxf;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CxfSslInformation {

    private boolean sslAuthEnabled;
    private String alias;
    private boolean cnCheckDisabled;
}
