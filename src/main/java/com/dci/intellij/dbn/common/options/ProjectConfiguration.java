package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.project.ProjectSupplier;

public interface ProjectConfiguration<P extends ProjectConfiguration, E extends ConfigurationEditorForm>
        extends Configuration<P, E>, ProjectSupplier {
}
