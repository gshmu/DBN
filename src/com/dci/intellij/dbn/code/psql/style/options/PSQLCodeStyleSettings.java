package com.dci.intellij.dbn.code.psql.style.options;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCustomSettings;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleFormattingSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.code.psql.style.options.ui.PSQLCodeStyleSettingsEditorForm;
import com.dci.intellij.dbn.common.Icons;
import com.intellij.openapi.project.Project;

public class PSQLCodeStyleSettings extends CodeStyleCustomSettings<PSQLCodeStyleSettingsEditorForm>{

    public static PSQLCodeStyleSettings getInstance(@NotNull Project project) {
        return ProjectCodeStyleSettings.getInstance(project).getPSQLCodeStyleSettings();    
    }

    @Nullable
    public Icon getIcon() {
        return Icons.FILE_PLSQL;
    }

    public String getDisplayName() {
        return "PL/SQL";
    }

    protected CodeStyleCaseSettings createCaseSettings() {
        return new PSQLCodeStyleCaseSettings();
    }

    protected CodeStyleFormattingSettings createAttributeSettings() {
        return new PSQLCodeStyleFormattingSettings();
    }

    @Override
    protected String getElementName() {
        return "DBN-PSQL";
    }

    /*********************************************************
    *                     Configuration                     *
    *********************************************************/
    @NotNull
    public PSQLCodeStyleSettingsEditorForm createConfigurationEditor() {
        return new PSQLCodeStyleSettingsEditorForm(this);
    }


}