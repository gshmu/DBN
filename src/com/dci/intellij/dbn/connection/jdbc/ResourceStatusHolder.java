package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

class ResourceStatusHolder extends PropertyHolderImpl<ResourceStatus> {
    @Override
    protected ResourceStatus[] properties() {
        return ResourceStatus.values();
    }
}
