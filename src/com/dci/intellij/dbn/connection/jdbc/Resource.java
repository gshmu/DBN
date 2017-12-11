package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.PropertyHolder;

public interface Resource extends PropertyHolder<ResourceStatus> {
    ResourceType getResourceType();

    void statusChanged(ResourceStatus status);
}
