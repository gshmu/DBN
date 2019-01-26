package com.dci.intellij.dbn.execution.compiler.options;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.execution.compiler.CompileDependenciesOption;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.options.ui.CompilerSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class CompilerSettings extends Configuration<CompilerSettingsForm>{
    private CompileType compileType = CompileType.KEEP;
    private CompileDependenciesOption compileDependenciesOption = CompileDependenciesOption.ASK;
    private boolean alwaysShowCompilerControls = false;

    @Override
    public String getDisplayName() {
        return "Data editor general settings";
    }

    @Override
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

    public CompileDependenciesOption getCompileDependenciesOption() {
        return compileDependenciesOption;
    }

    public void setCompileDependenciesOption(CompileDependenciesOption compileDependenciesOption) {
        this.compileDependenciesOption = compileDependenciesOption;
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
    @Override
    @NotNull
    public CompilerSettingsForm createConfigurationEditor() {
        return new CompilerSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "compiler";
    }

    @Override
    public void readConfiguration(Element element) {
        compileType = CompileType.get(SettingsUtil.getString(element, "compile-type", compileType.name()));
        compileDependenciesOption = CompileDependenciesOption.get(SettingsUtil.getString(element, "compile-dependencies", compileDependenciesOption.name()));
        alwaysShowCompilerControls = SettingsUtil.getBoolean(element, "always-show-controls", alwaysShowCompilerControls);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsUtil.setString(element, "compile-type", compileType.name());
        SettingsUtil.setString(element, "compile-dependencies", compileDependenciesOption.name());
        SettingsUtil.setBoolean(element, "always-show-controls", alwaysShowCompilerControls);
    }
}
