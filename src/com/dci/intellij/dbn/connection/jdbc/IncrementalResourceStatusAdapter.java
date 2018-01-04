package com.dci.intellij.dbn.connection.jdbc;

public abstract class IncrementalResourceStatusAdapter<T extends Resource> extends IncrementalStatusAdapter<T, ResourceStatus>{
    public IncrementalResourceStatusAdapter(ResourceStatus status, T resource) {
        super(status, resource);
    }

    @Override
    protected void statusChanged() {
        T resource = getResource();
        ResourceStatus status = getStatus();
        resource.statusChanged(status);
    }

    protected abstract boolean setInner(ResourceStatus status, boolean value);


}
