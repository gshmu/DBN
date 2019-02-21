package com.dci.intellij.dbn.code.common.completion.options.filter.ui;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class CodeCompletionFilterSettingsForm extends ConfigurationEditorForm<CodeCompletionFilterSettings> {
    private JPanel mainPanel;
    private CodeCompletionFilterTree tree;
    private CodeCompletionFilterTreeModel treeModel;

    public CodeCompletionFilterSettingsForm(CodeCompletionFilterSettings codeCompletionFilterSettings) {
        super(codeCompletionFilterSettings);
        treeModel = new CodeCompletionFilterTreeModel(codeCompletionFilterSettings);
        tree = new CodeCompletionFilterTree(treeModel);
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tree, BorderLayout.CENTER);
    }

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        treeModel.applyChanges();
    }

    @Override
    public void resetFormChanges() {
        treeModel.resetChanges();
    }
}
