package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.ThreadLocalFlag;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

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
                } catch (Exception e){
                    if (!(e instanceof ProcessCanceledException)) {
                        LOGGER.error("Error notifying configuration changes", e);
                    }
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
}
