package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.property.PropertyHolder;

public interface Resource extends PropertyHolder<ResourceStatus> {
    ResourceType getResourceType();
    String getResourceId();

    void statusChanged(ResourceStatus status);


    static Resource wrapper(Resource resource) {
        return new Wrapper<>(resource);
    }

    class Wrapper<T extends Resource> implements Resource{
        private final T inner;

        public Wrapper(T inner) {
            this.inner = inner;
        }

        @Override
        public String getResourceId() {
            return inner.getResourceId();
        }

        @Override
        public boolean set(ResourceStatus status, boolean value) {
            return inner.set(status, value);
        }

        @Override
        public boolean is(ResourceStatus status) {
            return inner.is(status);
        }

        @Override
        public ResourceType getResourceType() {
            return inner.getResourceType();
        }

        @Override
        public void statusChanged(ResourceStatus status) {
            inner.statusChanged(status);
        }
    }
}
