package com.dci.intellij.dbn.execution.compiler.options;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.options.ui.CompilerSettingsForm;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

public class CompilerSettings extends Configuration<CompilerSettingsForm>{
    private CompileType compileType = CompileType.KEEP;
    private boolean alwaysShowCompilerControls = false;

    public String getDisplayName() {
        return "Data editor general settings";
    }

    public String getHelpTopic() {
        return "executionEngine";
    }

    /*********************************************************
    *                       Settings                        *
    *********************************************************/

    public CompileType getCompileType() {
        return compileType;
    }

    public void setCompileType(CompileType compileType) {
        this.compileType = compileType;
    }

    public boolean alwaysShowCompilerControls() {
        return alwaysShowCompilerControls;
    }

    public void setAlwaysShowCompilerControls(boolean alwaysShowCompilerControls) {
        this.alwaysShowCompilerControls = alwaysShowCompilerControls;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    public CompilerSettingsForm createConfigurationEditor() {
        return new CompilerSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "compiler";
    }

    public void readConfiguration(Element element) throws InvalidDataException {
        compileType = CompileType.get(SettingsUtil.getString(element, "compile-type", compileType.name()));
        alwaysShowCompilerControls = SettingsUtil.getBoolean(element, "always-show-controls", alwaysShowCompilerControls);
    }

    public void writeConfiguration(Element element) throws WriteExternalException {
        SettingsUtil.setString(element, "compile-type", compileType.name());
        SettingsUtil.setBoolean(element, "always-show-controls", alwaysShowCompilerControls);
    }
}
