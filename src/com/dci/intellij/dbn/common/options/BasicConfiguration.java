package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.options.ConfigurationException;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class BasicConfiguration<P extends Configuration, E extends ConfigurationEditorForm>
        extends SettingsSupport
        implements Configuration<P, E> {

    private transient E configurationEditorForm;

    private boolean modified = false;
    private boolean transitory = IS_TRANSITORY.get();
    private WeakRef<P> parent;

    public BasicConfiguration(P parent) {
        this.parent = WeakRef.from(parent);
    }

    public P getParent() {
        return WeakRef.get(this.parent);
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    @Nls
    public String getDisplayName() {
        return null;
    }

    public Icon getIcon() {
        return null;
    }

    @Override
    @NotNull
    public String getId() {
        return getClass().getName();
    }

    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nullable
    public final E getSettingsEditor() {
        return configurationEditorForm;
    }

    @NotNull
    public final E ensureSettingsEditor() {
        return Failsafe.get(configurationEditorForm);
    }


    @Override
    @NotNull
    public JComponent createComponent() {
        configurationEditorForm = createConfigurationEditor();
        return configurationEditorForm.getComponent();
    }

    public void setModified(boolean modified) {
        if (!isResetting()) {
            this.modified = modified;
        }
    }

    private static Boolean isResetting() {
        return IS_RESETTING.get();
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    public final boolean isTransitory() {
        return transitory;
    }

    @Override
    public void apply() throws ConfigurationException {
        if (configurationEditorForm != null && !configurationEditorForm.isDisposed()) {
            configurationEditorForm.applyFormChanges();
        }
        modified = false;

        if (this instanceof TopLevelConfig) {
            TopLevelConfig topLevelConfig = (TopLevelConfig) this;
            Configuration originalSettings = topLevelConfig.getOriginalSettings();
            if (originalSettings != this ) {
                Element settingsElement = new Element("settings");
                writeConfiguration(settingsElement);
                originalSettings.readConfiguration(settingsElement);
            }

            // Notify only when all changes are set
            Configuration.notifyChanges();
        }
        onApply();
    }

    @Deprecated
    protected void onApply() {}

    @Override
    public void reset() {
        Dispatch.invoke(() -> {
            try {
                IS_RESETTING.set(true);
                Failsafe.get(configurationEditorForm).resetFormChanges();
            } finally {
                modified = false;
                IS_RESETTING.set(false);
            }
        });
    }

    @Override
    public void disposeUIResources() {
        DisposerUtil.dispose(configurationEditorForm);
        configurationEditorForm = null;
    }

    public String getConfigElementName() {
        //throw new UnsupportedOperationException("Element name not defined for this configuration type.");
        return null;
    }

    protected static String nvl(String value) {
        return value == null ? "" : value;
    }


}
