package com.dci.intellij.dbn.debugger.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.options.ui.DebuggerSettingsForm;

public class DebuggerSettings extends Configuration<DebuggerSettingsForm>{
    private DBDebuggerType debuggerType = DBDebuggerType.JWDP;

    public String getDisplayName() {
        return "Data editor general settings";
    }

    public String getHelpTopic() {
        return "debugger";
    }

    /*********************************************************
    *                       Settings                        *
    *********************************************************/

    public DBDebuggerType getDebuggerType() {
        return debuggerType;
    }

    public void setDebuggerType(DBDebuggerType debuggerType) {
        this.debuggerType = debuggerType;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @NotNull
    public DebuggerSettingsForm createConfigurationEditor() {
        return new DebuggerSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "debugger";
    }

    public void readConfiguration(Element element) {
        debuggerType = SettingsUtil.getEnum(element, "debugger-type", debuggerType);
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setEnum(element, "debugger-type", debuggerType);
    }
}
