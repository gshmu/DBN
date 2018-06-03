package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public class VirtualFileStatusHolder extends PropertyHolderImpl<VirtualFileStatus> {
    @Override
    protected VirtualFileStatus[] getProperties() {
        return VirtualFileStatus.values();
    }
}
