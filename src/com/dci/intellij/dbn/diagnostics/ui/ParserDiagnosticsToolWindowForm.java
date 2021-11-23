package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class ParserDiagnosticsToolWindowForm extends DBNFormImpl implements DataProvider {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel contentPanel;

    private final ParserDiagnosticsForm mainForm;

    public ParserDiagnosticsToolWindowForm(@Nullable Disposable parent, @Nullable Project project) {
        super(parent, project);

        mainForm = new ParserDiagnosticsForm(this);
        contentPanel.add(mainForm.getMainComponent(), BorderLayout.CENTER);

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(actionsPanel,"", false, "DBNavigator.ActionGroup.ParserDiagnostics");
        actionsPanel.add(actionToolbar.getComponent());
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        return mainForm.getData(dataId);
    }
}
