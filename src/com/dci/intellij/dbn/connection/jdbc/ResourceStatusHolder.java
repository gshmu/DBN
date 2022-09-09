package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.PropertyHolderBase;

class ResourceStatusHolder extends PropertyHolderBase.IntStore<ResourceStatus> {

    @Override
    protected ResourceStatus[] properties() {
        return ResourceStatus.VALUES;
    }
}
