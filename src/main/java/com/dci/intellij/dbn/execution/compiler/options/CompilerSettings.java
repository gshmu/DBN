package com.dci.intellij.dbn.execution.compiler.options;

import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.execution.compiler.CompileDependenciesOption;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.options.ui.CompilerSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class CompilerSettings extends BasicConfiguration<OperationSettings, CompilerSettingsForm> {
    private CompileType compileType = CompileType.KEEP;
    private CompileDependenciesOption compileDependenciesOption = CompileDependenciesOption.ASK;
    private boolean alwaysShowCompilerControls = false;

    public CompilerSettings(OperationSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return "Data editor general settings";
    }

    @Override
    public String getHelpTopic() {
        return "executionEngine";
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
        compileType = CompileType.get(SettingsSupport.getString(element, "compile-type", compileType.name()));
        compileDependenciesOption = CompileDependenciesOption.get(SettingsSupport.getString(element, "compile-dependencies", compileDependenciesOption.name()));
        alwaysShowCompilerControls = SettingsSupport.getBoolean(element, "always-show-controls", alwaysShowCompilerControls);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsSupport.setString(element, "compile-type", compileType.name());
        SettingsSupport.setString(element, "compile-dependencies", compileDependenciesOption.name());
        SettingsSupport.setBoolean(element, "always-show-controls", alwaysShowCompilerControls);
    }
}
