package com.dci.intellij.dbn.editor.session.options;

import com.dci.intellij.dbn.common.option.InteractiveOptionBroker;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.editor.session.options.ui.SessionBrowserSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class SessionBrowserSettings extends BasicConfiguration<OperationSettings, SessionBrowserSettingsForm> {
    public static final String REMEMBER_OPTION_HINT = ""; //"\n\n(you can remember your option and change it at any time in Settings > Operations > Session Manager)";

    private boolean reloadOnFilterChange = false;
    private InteractiveOptionBroker<SessionInterruptionOption> disconnectSession =
            new InteractiveOptionBroker<SessionInterruptionOption>(
                    "disconnect-session",
                    "Disconnect Sessions",
                    "Are you sure you want to disconnect the {0} from connection {1}?\nPlease select your disconnect option." +
                            REMEMBER_OPTION_HINT,
                    SessionInterruptionOption.ASK,
                    SessionInterruptionOption.IMMEDIATE,
                    SessionInterruptionOption.POST_TRANSACTION,
                    SessionInterruptionOption.CANCEL);

    private InteractiveOptionBroker<SessionInterruptionOption> killSession =
            new InteractiveOptionBroker<SessionInterruptionOption>(
                    "kill-session",
                    "Kill Sessions",
                    "Are you sure you want to kill the {0} from connection {1}?\nPlease select your kill option." +
                            REMEMBER_OPTION_HINT,
                    SessionInterruptionOption.ASK,
                    SessionInterruptionOption.NORMAL,
                    SessionInterruptionOption.IMMEDIATE,
                    SessionInterruptionOption.CANCEL);

    public SessionBrowserSettings(OperationSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return "Session Browser Settings";
    }

    @Override
    public String getHelpTopic() {
        return "sessionBrowser";
    }


    /*********************************************************
     *                       Settings                        *
     *********************************************************/

    public InteractiveOptionBroker<SessionInterruptionOption> getDisconnectSession() {
        return disconnectSession;
    }

    public InteractiveOptionBroker<SessionInterruptionOption> getKillSession() {
        return killSession;
    }

    public boolean isReloadOnFilterChange() {
        return reloadOnFilterChange;
    }

    public void setReloadOnFilterChange(boolean reloadOnFilterChange) {
        this.reloadOnFilterChange = reloadOnFilterChange;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public SessionBrowserSettingsForm createConfigurationEditor() {
        return new SessionBrowserSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "session-browser";
    }

    @Override
    public void readConfiguration(Element element) {
        disconnectSession.readConfiguration(element);
        killSession.readConfiguration(element);
        reloadOnFilterChange = SettingsSupport.getBoolean(element, "reload-on-filter-change", reloadOnFilterChange);
    }

    @Override
    public void writeConfiguration(Element element) {
        disconnectSession.writeConfiguration(element);
        killSession.writeConfiguration(element);
        SettingsSupport.setBoolean(element, "reload-on-filter-change", reloadOnFilterChange);
    }
}
