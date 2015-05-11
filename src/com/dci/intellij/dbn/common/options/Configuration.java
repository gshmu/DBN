package com.dci.intellij.dbn.common.options;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

public abstract class Configuration<T extends ConfigurationEditorForm> extends ConfigurationUtil implements SearchableConfigurable, PersistentConfiguration {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    public static ThreadLocalFlag IS_RESETTING = new ThreadLocalFlag(false);
    public static ThreadLocal<List<SettingsChangeNotifier>> SETTINGS_CHANGE_NOTIFIERS = new ThreadLocal<List<SettingsChangeNotifier>>();
    private T configurationEditorForm;
    private boolean isModified = false;

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
            isModified = modified;
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
        return isModified;
    }

    public void apply() throws ConfigurationException {
        if (configurationEditorForm != null && !configurationEditorForm.isDisposed()) {
            configurationEditorForm.applyFormChanges();
        }
        isModified = false;

        Configuration<T> settings = getOriginalSettings();
        if (this instanceof TopLevelConfig && settings != this) {
            Element settingsElement = new Element("settings");
            writeConfiguration(settingsElement);
            settings.readConfiguration(settingsElement);
        }

        if (this instanceof TopLevelConfig) {
           // Notify only when all changes are set
            notifyChanges();
        }
        onApply();
    }

    protected void notifyChanges() {
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

    protected Configuration<T> getOriginalSettings() {
        return null;
    }

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
                    isModified = false;
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
