package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.actionSystem.DataProvider;
import org.jetbrains.annotations.Nullable;

public interface DataProviderSupplier {
    @Nullable
    DataProvider getDataProvider();
}
