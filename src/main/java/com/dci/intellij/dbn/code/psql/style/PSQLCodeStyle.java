package com.dci.intellij.dbn.code.psql.style;

import com.dci.intellij.dbn.code.common.style.DBLCodeStyle;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleFormattingSettings;
import com.dci.intellij.dbn.code.psql.style.options.PSQLCodeStyleSettings;
import com.dci.intellij.dbn.code.psql.style.options.PSQLCodeStyleSettingsWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.Nullable;

public class PSQLCodeStyle extends DBLCodeStyle {


    public static PSQLCodeStyleSettings settings(@Nullable Project project) {
        CodeStyleSettings rootSettings = rootSettings(project);
        PSQLCodeStyleSettingsWrapper wrappedSettings = rootSettings.getCustomSettings(PSQLCodeStyleSettingsWrapper.class);
        return wrappedSettings.getSettings();
    }

    public static CodeStyleCaseSettings caseSettings(Project project) {
        return settings(project).getCaseSettings();
    }

    public static CodeStyleFormattingSettings formattingSettings(Project project) {
        return settings(project).getFormattingSettings();
    }
}
