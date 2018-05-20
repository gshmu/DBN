package com.dci.intellij.dbn.debugger.options;

import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.options.ui.DebuggerSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class DebuggerSettings extends Configuration<DebuggerSettingsForm>{
    private boolean useGenericRunners = true;
    private InteractiveOptionHandler<DebuggerTypeOption> debuggerType =
            new InteractiveOptionHandler<DebuggerTypeOption>(
                    "debugger-type",
                    "Debugger Type",
                    "Please select debugger type to use.",
                    DBDebuggerType.JDWP.isSupported() ? DebuggerTypeOption.ASK : DebuggerTypeOption.JDBC,
                    DebuggerTypeOption.JDWP,
                    DebuggerTypeOption.JDBC,
                    DebuggerTypeOption.CANCEL);

    public String getDisplayName() {
        return "Data editor general settings";
    }

    public String getHelpTopic() {
        return "debugger";
    }

    public InteractiveOptionHandler<DebuggerTypeOption> getDebuggerType() {
        return debuggerType;
    }

    /*********************************************************
    *                       Settings                        *
    *********************************************************/



    public boolean isUseGenericRunners() {
        return useGenericRunners;
    }

    public void setUseGenericRunners(boolean useGenericRunners) {
        this.useGenericRunners = useGenericRunners;
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
        debuggerType.readConfiguration(element);
        useGenericRunners = SettingsUtil.getBoolean(element, "use-generic-runners", useGenericRunners);
    }

    public void writeConfiguration(Element element) {
        debuggerType.writeConfiguration(element);
        SettingsUtil.setBoolean(element, "use-generic-runners", useGenericRunners);
    }
}
