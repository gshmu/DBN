package com.dci.intellij.dbn.options;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;

public interface TopLevelConfig<T extends ConfigurationEditorForm>  {
    ConfigId getConfigId();

    @NotNull
    Configuration<T> getOriginalSettings();
}
