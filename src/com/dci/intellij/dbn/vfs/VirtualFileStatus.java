package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum VirtualFileStatus implements Property{
    LATEST(Group.CODE, true),
    MERGED(Group.CODE, false),
    OUTDATED(Group.CODE, false),

    MODIFIED,

    LOADING,
    SAVING,
    REFRESHING;

    private int index;
    private boolean implicit;
    private PropertyGroup group;

    VirtualFileStatus(PropertyGroup group, boolean implicit) {
        this.index = PropertyHolderImpl.idx(this);
        this.implicit = implicit;
        this.group = group;
    }

    VirtualFileStatus() {
        this(null, false);
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public PropertyGroup group() {
        return group;
    }

    @Override
    public boolean implicit() {
        return implicit;
    }

    public enum Group implements PropertyGroup{
        CODE
    }
}
