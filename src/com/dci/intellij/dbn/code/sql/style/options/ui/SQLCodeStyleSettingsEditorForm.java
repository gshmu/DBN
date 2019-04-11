package com.dci.intellij.dbn.code.sql.style.options.ui;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCustomSettings;
import com.dci.intellij.dbn.code.sql.style.options.SQLCodeStyleSettings;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class SQLCodeStyleSettingsEditorForm extends CompositeConfigurationEditorForm<CodeStyleCustomSettings> {
    private JPanel mainPanel;
    private JPanel casePanel;
    private JPanel previewPanel;
    private JPanel attributesPanel;

    public SQLCodeStyleSettingsEditorForm(SQLCodeStyleSettings settings) {
        super(settings);
        casePanel.add(settings.getCaseSettings().createComponent(), BorderLayout.CENTER);
        attributesPanel.add(settings.getFormattingSettings().createComponent(), BorderLayout.CENTER);
        updateBorderTitleForeground(previewPanel);
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }
}
