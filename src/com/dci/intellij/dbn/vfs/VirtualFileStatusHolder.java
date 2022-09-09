package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.property.PropertyHolderBase;

public class VirtualFileStatusHolder extends PropertyHolderBase.IntStore<VirtualFileStatus> {

    @Override
    protected VirtualFileStatus[] properties() {
        return VirtualFileStatus.VALUES;
    }
}
