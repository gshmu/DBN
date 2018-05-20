package com.dci.intellij.dbn.editor.session.options;

import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.editor.session.options.ui.SessionBrowserSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class SessionBrowserSettings extends Configuration<SessionBrowserSettingsForm> {
    public static final String REMEMBER_OPTION_HINT = ""; //"\n\n(you can remember your option and change it at any time in Settings > Operations > Session Manager)";

    private boolean reloadOnFilterChange = false;
    private InteractiveOptionHandler<SessionInterruptionOption> disconnectSession =
            new InteractiveOptionHandler<SessionInterruptionOption>(
                    "disconnect-session",
                    "Disconnect Sessions",
                    "Are you sure you want to disconnect the {0} from connection {1}?\nPlease select your disconnect option." +
                            REMEMBER_OPTION_HINT,
                    SessionInterruptionOption.ASK,
                    SessionInterruptionOption.IMMEDIATE,
                    SessionInterruptionOption.POST_TRANSACTION,
                    SessionInterruptionOption.CANCEL);

    private InteractiveOptionHandler<SessionInterruptionOption> killSession =
            new InteractiveOptionHandler<SessionInterruptionOption>(
                    "kill-session",
                    "Kill Sessions",
                    "Are you sure you want to kill the {0} from connection {1}?\nPlease select your kill option." +
                            REMEMBER_OPTION_HINT,
                    SessionInterruptionOption.ASK,
                    SessionInterruptionOption.NORMAL,
                    SessionInterruptionOption.IMMEDIATE,
                    SessionInterruptionOption.CANCEL);

    public String getDisplayName() {
        return "Session Browser Settings";
    }

    public String getHelpTopic() {
        return "sessionBrowser";
    }


    /*********************************************************
     *                       Settings                        *
     *********************************************************/

    public InteractiveOptionHandler<SessionInterruptionOption> getDisconnectSession() {
        return disconnectSession;
    }

    public InteractiveOptionHandler<SessionInterruptionOption> getKillSession() {
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
    @NotNull
    public SessionBrowserSettingsForm createConfigurationEditor() {
        return new SessionBrowserSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "session-browser";
    }

    public void readConfiguration(Element element) {
        disconnectSession.readConfiguration(element);
        killSession.readConfiguration(element);
        reloadOnFilterChange = SettingsUtil.getBoolean(element, "reload-on-filter-change", reloadOnFilterChange);
    }

    public void writeConfiguration(Element element) {
        disconnectSession.writeConfiguration(element);
        killSession.writeConfiguration(element);
        SettingsUtil.setBoolean(element, "reload-on-filter-change", reloadOnFilterChange);
    }
}
