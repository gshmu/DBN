package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.property.Property;

public enum ExecutionStatus implements Property {
    QUEUED,
    PROMPTED,
    EXECUTING,
    CANCELLED;
}
