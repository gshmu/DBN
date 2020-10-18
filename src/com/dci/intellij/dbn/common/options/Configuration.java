package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.project.ProjectSupplier;
import com.dci.intellij.dbn.common.util.ThreadLocalFlag;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public interface Configuration<P extends Configuration, E extends ConfigurationEditorForm>
        extends SearchableConfigurable, PersistentConfiguration {

    Logger LOGGER = LoggerFactory.createLogger();

    ThreadLocalFlag IS_TRANSITORY = new ThreadLocalFlag(false);
    ThreadLocalFlag IS_RESETTING = new ThreadLocalFlag(false);

    ThreadLocal<List<SettingsChangeNotifier>> SETTINGS_CHANGE_NOTIFIERS = new ThreadLocal<>();

    P getParent();

    String getConfigElementName();

    @NotNull
    default E createConfigurationEditor() {
        throw new UnsupportedOperationException();
    };

    E getSettingsEditor();

    E ensureSettingsEditor();

    static void registerChangeNotifier(SettingsChangeNotifier notifier) {
        List<SettingsChangeNotifier> notifiers = SETTINGS_CHANGE_NOTIFIERS.get();
        if (notifiers == null) {
            notifiers = new ArrayList<>();
            SETTINGS_CHANGE_NOTIFIERS.set(notifiers);
        }
        notifiers.add(notifier);
    }

    static void notifyChanges() {
        List<SettingsChangeNotifier> changeNotifiers = SETTINGS_CHANGE_NOTIFIERS.get();
        if (changeNotifiers != null) {
            SETTINGS_CHANGE_NOTIFIERS.set(null);
            for (SettingsChangeNotifier changeNotifier : changeNotifiers) {
                try {
                    changeNotifier.notifyChanges();
                } catch (ProcessCanceledException ignore){
                } catch (Exception e){
                    LOGGER.error("Error notifying configuration changes", e);
                }
            }
        }
    }

    /*****************************************************************
     *                         DOM utilities                         *
     ****************************************************************/
    default void writeConfiguration(Element element, Configuration configuration) {
        String elementName = configuration.getConfigElementName();
        if (elementName != null) {
            Element childElement = new Element(elementName);
            element.addContent(childElement);
            configuration.writeConfiguration(childElement);
        }
    }


    default void readConfiguration(Element element, Configuration configuration) {
        String elementName = configuration.getConfigElementName();
        if (elementName != null) {
            Element childElement = element.getChild(elementName);
            if (childElement != null) {
                configuration.readConfiguration(childElement);
            }
        }
    }

    default Project resolveProject() {
        if (this instanceof ProjectSupplier) {
            ProjectSupplier projectSupplier = (ProjectSupplier) this;
            Project project = projectSupplier.getProject();
            if (project != null) {
                return project;
            }
        }

        Configuration parent = this.getParent();
        if (parent != null) {
            Project project = parent.resolveProject();
            if (project != null) {
                return project;
            }
        }

        ConfigurationEditorForm settingsEditor = this.getSettingsEditor();
        if (Failsafe.check(settingsEditor)) {
            JComponent component = settingsEditor.getComponent();
            DataContext dataContext = DataManager.getInstance().getDataContext(component);
            return PlatformDataKeys.PROJECT.getData(dataContext);
        }
        return null;
    }
}
