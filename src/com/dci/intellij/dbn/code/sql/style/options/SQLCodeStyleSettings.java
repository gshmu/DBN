package com.dci.intellij.dbn.code.sql.style.options;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCustomSettings;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleFormattingSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.code.sql.style.options.ui.SQLCodeStyleSettingsEditorForm;
import com.dci.intellij.dbn.common.Icons;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SQLCodeStyleSettings extends CodeStyleCustomSettings<CodeStyleCustomSettings, SQLCodeStyleSettingsEditorForm> {

    SQLCodeStyleSettings(CodeStyleCustomSettings parent) {
        super(parent);
    }

    public static SQLCodeStyleSettings getInstance(@NotNull Project project) {
        return ProjectCodeStyleSettings.getInstance(project).getSQLCodeStyleSettings();
    }

    @Override
    @Nls
    public String getDisplayName() {
        return "SQL";
    }

    @Override
    @Nullable
    public Icon getIcon() {
        return Icons.FILE_SQL;
    }

    @Override
    protected CodeStyleCaseSettings createCaseSettings(CodeStyleCustomSettings parent) {
        return new SQLCodeStyleCaseSettings(parent);
    }

    @Override
    protected CodeStyleFormattingSettings createAttributeSettings(CodeStyleCustomSettings parent) {
        return new SQLCodeStyleFormattingSettings(parent);
    }

    @Override
    protected String getElementName() {
        return "DBN-SQL";
    }

    /*********************************************************
    *                     Configuration                     *
    *********************************************************/
    @Override
    @NotNull
    public SQLCodeStyleSettingsEditorForm createConfigurationEditor() {
        return new SQLCodeStyleSettingsEditorForm(this);
    }
}
