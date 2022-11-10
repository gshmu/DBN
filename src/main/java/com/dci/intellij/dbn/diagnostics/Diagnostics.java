package com.dci.intellij.dbn.diagnostics;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.Unsafe;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

public final class Diagnostics {
    private static boolean developerMode = false;
    private static final DebugLogging debugLogging = new DebugLogging();
    private static final DatabaseLag databaseLag = new DatabaseLag();
    private static final Miscellaneous miscellaneous = new Miscellaneous();

    public static boolean isDialogSizingReset() {
        return developerMode && miscellaneous.dialogSizingReset;
    }

    public static boolean isBulkActionsEnabled() {
        return developerMode && miscellaneous.bulkActionsEnabled;
    }

    public static boolean isAlternativeParserEnabled() {
        return developerMode && miscellaneous.alternativeParserEnabled;
    }

    public static boolean isDatabaseAccessDebug() {
        return developerMode && debugLogging.databaseAccess;
    }

    public static boolean isDatabaseResourceDebug() {
        return developerMode && debugLogging.databaseResource;
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
        return developerMode;
    }

    public static void setDeveloperMode(boolean developerMode) {
        Diagnostics.developerMode = developerMode;
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
        private boolean databaseAccess = false;
        private boolean databaseResource = false;

        @Override
        public void readState(Element element) {
            Element debugMode = element.getChild("debug-logging");
            if (debugMode != null) {
                databaseAccess = booleanAttribute(debugMode, "database-access", databaseAccess);
                databaseResource = booleanAttribute(debugMode, "database-resource", databaseResource);
            }
        }

        @Override
        public void writeState(Element element) {
            Element debugMode = new Element("debug-logging");
            element.addContent(debugMode);
            setBooleanAttribute(debugMode, "database-access", databaseAccess);
            setBooleanAttribute(debugMode, "database-resource", databaseResource);
        }
    }

    @Getter
    @Setter
    public static final class Miscellaneous implements PersistentStateElement{
        private boolean dialogSizingReset = false;
        private boolean bulkActionsEnabled = false;
        private boolean alternativeParserEnabled = false;

        @Override
        public void readState(Element element) {
            Element miscellaneous = element.getChild("miscellaneous");
            if (miscellaneous != null) {
                dialogSizingReset = booleanAttribute(miscellaneous, "dialog-sizing-reset", dialogSizingReset);
                bulkActionsEnabled = booleanAttribute(miscellaneous, "bulk-actions-enabled", bulkActionsEnabled);
                // TODO too early to activate alternative parser
                //alternativeParserEnabled = booleanAttribute(miscellaneous, "alternative-parser-enabled", alternativeParserEnabled);
            }
        }

        @Override
        public void writeState(Element element) {
            Element miscellaneous = new Element("miscellaneous");
            element.addContent(miscellaneous);
            setBooleanAttribute(miscellaneous, "dialog-sizing-reset", dialogSizingReset);
            setBooleanAttribute(miscellaneous, "bulk-actions-enabled", bulkActionsEnabled);
            setBooleanAttribute(miscellaneous, "alternative-parser-enabled", alternativeParserEnabled);
        }
    }


    public static void introduceDatabaseLag(int millis) {
        if (Diagnostics.developerMode && Diagnostics.databaseLag.enabled) {
            Unsafe.silent(() -> Thread.sleep(millis));
        }
    }
}
