package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.ThreadLocalFlag;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Configuration<T extends ConfigurationEditorForm> extends ConfigurationUtil implements SearchableConfigurable, PersistentConfiguration {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    protected static ThreadLocalFlag IS_TRANSITORY = new ThreadLocalFlag(false);

    private static ThreadLocalFlag IS_RESETTING = new ThreadLocalFlag(false);
    private static ThreadLocal<List<SettingsChangeNotifier>> SETTINGS_CHANGE_NOTIFIERS = new ThreadLocal<List<SettingsChangeNotifier>>();
    private T configurationEditorForm;

    private boolean modified = false;
    private boolean transitory = IS_TRANSITORY.get();

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
        return getClass().getName();
    }

    public Runnable enableSearch(String option) {
        return null;
    }

    @Nullable
    public final T getSettingsEditor() {
        return configurationEditorForm;
    }

    @NotNull
    protected abstract T createConfigurationEditor();

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

    public static void registerChangeNotifier(SettingsChangeNotifier notifier) {
        List<SettingsChangeNotifier> notifiers = SETTINGS_CHANGE_NOTIFIERS.get();
        if (notifiers == null) {
            notifiers = new ArrayList<SettingsChangeNotifier>();
            SETTINGS_CHANGE_NOTIFIERS.set(notifiers);
        }
        notifiers.add(notifier);

    }

    public boolean isModified() {
        return modified;
    }

    public final boolean isTransitory() {
        return transitory;
    }

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
            notifyChanges();
        }
        onApply();
    }

    public void notifyChanges() {
        List<SettingsChangeNotifier> changeNotifiers = SETTINGS_CHANGE_NOTIFIERS.get();
        if (changeNotifiers != null) {
            SETTINGS_CHANGE_NOTIFIERS.set(null);
            for (SettingsChangeNotifier changeNotifier : changeNotifiers) {
                try {
                    changeNotifier.notifyChanges();
                } catch (Exception e){
                    if (!(e instanceof ProcessCanceledException)) {
                        LOGGER.error("Error notifying configuration changes", e);
                    }
                }
            }
        }
    }

    @Deprecated
    protected void onApply() {}

    public void reset() {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                try {
                    if (configurationEditorForm != null && !configurationEditorForm.isDisposed()) {
                        IS_RESETTING.set(true);
                        configurationEditorForm.resetFormChanges();
                    }
                } finally {
                    modified = false;
                    IS_RESETTING.set(false);
                }
            }
        }.start();
    }

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
