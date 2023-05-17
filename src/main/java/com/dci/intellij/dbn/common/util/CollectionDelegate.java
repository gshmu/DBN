package com.dci.intellij.dbn.common.util;

import lombok.experimental.Delegate;

import java.util.Collection;

public abstract class CollectionDelegate<T> implements Collection<T> {
    @Delegate
    protected abstract Collection<T> getSource();
}
