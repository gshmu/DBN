package com.dci.intellij.dbn.options;

import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import org.jetbrains.annotations.NotNull;

public interface TopLevelConfig<P extends BasicConfiguration, E extends ConfigurationEditorForm>  {
    ConfigId getConfigId();

    @NotNull
    Configuration<P, E> getOriginalSettings();
}
