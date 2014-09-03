package com.dci.intellij.dbn.common.options;

import javax.swing.Icon;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.Disposer;

public abstract class Configuration<T extends ConfigurationEditorForm> extends ConfigurationUtil implements SearchableConfigurable, PersistentConfiguration {
    private T configurationEditorForm;
    private boolean isModified;

    public String getHelpTopic() {
        return null;
    }

    @Nls
    public String getDisplayName() {
        return null;
    }

    public Icon getIcon() {
        return null;
    }

    @NotNull
    public String getId() {
        return null;
    }

    public Runnable enableSearch(String option) {
        return null;
    }

    @Nullable
    public final T getSettingsEditor() {
        return configurationEditorForm;
    }

    protected abstract T createConfigurationEditor();

    public JComponent createComponent() {
        configurationEditorForm = createConfigurationEditor();
        return configurationEditorForm == null ? null : configurationEditorForm.getComponent();
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

    public boolean isModified() {
        return isModified;
    }

    public void apply() throws ConfigurationException {
        if (configurationEditorForm != null && !configurationEditorForm.isDisposed()) configurationEditorForm.applyChanges();
        isModified = false;
    }

    public void reset() {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                if (configurationEditorForm != null && !configurationEditorForm.isDisposed()) configurationEditorForm.resetChanges();
                isModified = false;
            }
        }.start();
    }

    public void disposeUIResources() {
        if (configurationEditorForm != null) {
            Disposer.dispose(configurationEditorForm);
            configurationEditorForm = null;
        }
    }

    public String getConfigElementName() {
        //throw new UnsupportedOperationException("Element name not defined for this configuration type.");
        return null;
    }
}
