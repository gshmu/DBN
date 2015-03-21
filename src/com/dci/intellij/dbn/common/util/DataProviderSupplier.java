package com.dci.intellij.dbn.common.util;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.DataProvider;

public interface DataProviderSupplier {
    @Nullable
    DataProvider getDataProvider();
}
