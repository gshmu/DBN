package com.dci.intellij.dbn.diagnostics;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.Unsafe;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

public final class Diagnostics {
    private static final DeveloperMode developerMode = new DeveloperMode();
    private static final DebugLogging debugLogging = new DebugLogging();
    private static final DatabaseLag databaseLag = new DatabaseLag();
    private static final Miscellaneous miscellaneous = new Miscellaneous();

    public static boolean isDialogSizingReset() {
        return isDeveloperMode() && miscellaneous.dialogSizingReset;
    }

    public static boolean isBulkActionsEnabled() {
        return isDeveloperMode() && miscellaneous.bulkActionsEnabled;
    }

    public static boolean isBackgroundDisposerDisabled() {
        return isDeveloperMode() && miscellaneous.backgroundDisposerDisabled;
    }

    public static boolean isFailsafeLoggingEnabled() {
        return isDeveloperMode() && debugLogging.failsafeErrors;
    }

    public static boolean isDatabaseAccessDebug() {
        return isDeveloperMode() && debugLogging.databaseAccess;
    }

    public static boolean isDatabaseResourceDebug() {
        return isDeveloperMode() && debugLogging.databaseResource;
    }

    public static int getConnectivityLag() {
        return databaseLag.connectivity;
    }

    public static int getQueryingLag() {
        return databaseLag.querying;
    }

    public static int getFetchingLag() {
        return databaseLag.fetching;
    }

    public static boolean isDeveloperMode() {
        return developerMode.isEnabled();
    }

    public static DeveloperMode getDeveloperMode() {
        return developerMode;
    }

    public static DebugLogging getDebugLogging() {
        return debugLogging;
    }

    public static DatabaseLag getDatabaseLag() {
        return databaseLag;
    }

    public static Miscellaneous getMiscellaneous() {
        return miscellaneous;
    }

    public static boolean hasEnabledFeatures() {
        return miscellaneous.hasEnabledFeatures() ||
                debugLogging.hasEnabledFeatures() ||
                databaseLag.enabled;
    }

    public static void readState(Element element) {
        if (element == null) return;

        developerMode.setTimeout(getInteger(element, "developer-mode-timeout", developerMode.getTimeout()));
        debugLogging.readState(element);
        databaseLag.readState(element);
        miscellaneous.readState(element);
    }

    public static void writeState(Element element) {
        setInteger(element, "developer-mode-timeout", developerMode.getTimeout());
        debugLogging.writeState(element);
        databaseLag.writeState(element);
        miscellaneous.writeState(element);
    }

    @Getter
    @Setter
    public static final class DatabaseLag implements PersistentStateElement {
        private boolean enabled = false;
        private int connectivity = 2000;
        private int querying = 2000;
        private int fetching = 500;

        public void readState(Element element) {
            Element databaseLag = element.getChild("database-lag");
            if (databaseLag != null) {
                enabled = booleanAttribute(databaseLag, "enabled", enabled);
                connectivity = integerAttribute(databaseLag, "connectivity", connectivity);
                querying = integerAttribute(databaseLag, "querying", querying);
                fetching = integerAttribute(databaseLag, "fetching", fetching);
            }

        }

        public void writeState(Element element) {
            Element databaseLag = new Element("database-lag");
            element.addContent(databaseLag);
            setBooleanAttribute(databaseLag, "enabled", enabled);
            setIntegerAttribute(databaseLag, "connectivity", connectivity);
            setIntegerAttribute(databaseLag, "querying", querying);
            setIntegerAttribute(databaseLag, "fetching", fetching);
        }
    }

    @Getter
    @Setter
    public static final class DebugLogging implements PersistentStateElement{
        private boolean failsafeErrors = false;
        private boolean databaseAccess = false;
        private boolean databaseResource = false;

        public boolean hasEnabledFeatures() {
            return failsafeErrors || databaseAccess || databaseResource;
        }

        @Override
        public void readState(Element element) {
            Element debugMode = element.getChild("debug-logging");
            if (debugMode != null) {
                failsafeErrors = booleanAttribute(debugMode, "failsafe-errors", failsafeErrors);
                databaseAccess = booleanAttribute(debugMode, "database-access", databaseAccess);
                databaseResource = booleanAttribute(debugMode, "database-resource", databaseResource);
            }
        }

        @Override
        public void writeState(Element element) {
            Element debugMode = new Element("debug-logging");
            element.addContent(debugMode);
            setBooleanAttribute(debugMode, "failsafe-errors", failsafeErrors);
            setBooleanAttribute(debugMode, "database-access", databaseAccess);
            setBooleanAttribute(debugMode, "database-resource", databaseResource);
        }
    }

    @Getter
    @Setter
    public static final class Miscellaneous implements PersistentStateElement{
        private boolean dialogSizingReset = false;
        private boolean bulkActionsEnabled = false;
        private boolean backgroundDisposerDisabled = false;

        public boolean hasEnabledFeatures() {
            return dialogSizingReset ||
                    bulkActionsEnabled ||
                    backgroundDisposerDisabled;
        }

        @Override
        public void readState(Element element) {
            Element miscellaneous = element.getChild("miscellaneous");
            if (miscellaneous != null) {
                dialogSizingReset = booleanAttribute(miscellaneous, "dialog-sizing-reset", dialogSizingReset);
                bulkActionsEnabled = booleanAttribute(miscellaneous, "bulk-actions-enabled", bulkActionsEnabled);
                backgroundDisposerDisabled = booleanAttribute(miscellaneous, "background-disposer-disabled", backgroundDisposerDisabled);
            }
        }

        @Override
        public void writeState(Element element) {
            Element miscellaneous = new Element("miscellaneous");
            element.addContent(miscellaneous);
            setBooleanAttribute(miscellaneous, "dialog-sizing-reset", dialogSizingReset);
            setBooleanAttribute(miscellaneous, "bulk-actions-enabled", bulkActionsEnabled);
            setBooleanAttribute(miscellaneous, "background-disposer-disabled", backgroundDisposerDisabled);
        }
    }


    public static void introduceDatabaseLag(int millis) {
        if (Diagnostics.isDeveloperMode() && Diagnostics.databaseLag.enabled) {
            Unsafe.silent(() -> Thread.sleep(millis));
        }
    }
}
