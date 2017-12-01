package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

class VirtualFileStatusHolder extends PropertyHolderImpl<VirtualFileStatus> {
    VirtualFileStatusHolder() {
        super(VirtualFileStatus.class);
    }
}
