package com.dci.intellij.dbn.execution.compiler.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.execution.compiler.CompileDependenciesOption;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.*;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class CompilerSettingsForm extends ConfigurationEditorForm<CompilerSettings> {
    private JPanel mainPanel;
    private JComboBox<CompileType> compileTypeComboBox;
    private JComboBox<CompileDependenciesOption> compileDependenciesComboBox;
    private JComboBox<ShowControlOption> showControlsComboBox;


    public CompilerSettingsForm(CompilerSettings settings) {
        super(settings);

        initComboBox(showControlsComboBox,
                ShowControlOption.ALWAYS,
                ShowControlOption.WHEN_INVALID);

        initComboBox(compileTypeComboBox,
                CompileType.NORMAL,
                CompileType.DEBUG,
                CompileType.KEEP,
                CompileType.ASK);

        initComboBox(compileDependenciesComboBox,
                CompileDependenciesOption.YES,
                CompileDependenciesOption.NO,
                CompileDependenciesOption.ASK);


        updateBorderTitleForeground(mainPanel);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        CompilerSettings settings = getConfiguration();
        settings.setCompileType(getSelection(compileTypeComboBox));
        settings.setCompileDependenciesOption(getSelection(compileDependenciesComboBox));
        ShowControlOption showControlOption = getSelection(showControlsComboBox);
        settings.setAlwaysShowCompilerControls(showControlOption != null && showControlOption.getValue());
    }

    @Override
    public void resetFormChanges() {
        CompilerSettings settings = getConfiguration();
        setSelection(compileTypeComboBox, settings.getCompileType());
        setSelection(compileDependenciesComboBox, settings.getCompileDependenciesOption());
        setSelection(showControlsComboBox,
                settings.alwaysShowCompilerControls() ?
                        ShowControlOption.ALWAYS:
                        ShowControlOption.WHEN_INVALID);
    }

    private enum ShowControlOption implements Presentable {
        ALWAYS("Always", true),
        WHEN_INVALID("When object invalid", false);

        private String name;
        private boolean value;

        ShowControlOption(String name, boolean value) {
            this.name = name;
            this.value = value;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        public boolean getValue() {
            return value;
        }
    }
}
