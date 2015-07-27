package com.dci.intellij.dbn.execution.script.options.ui;

import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.thread.SimpleCallback;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.execution.script.CmdLineInterfaceBundle;
import com.dci.intellij.dbn.execution.script.ScriptExecutionManager;
import com.dci.intellij.dbn.execution.script.options.ScriptExecutionSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.awt.RelativePoint;

public class ScriptExecutionSettingsForm extends ConfigurationEditorForm<ScriptExecutionSettings> {
    private JPanel mainPanel;
    private JPanel cmdLineInterfacesTablePanel;
    private JTextField executionTimeoutTextField;
    private CmdLineInterfacesTable cmdLineInterfacesTable;

    public ScriptExecutionSettingsForm(ScriptExecutionSettings settings) {
        super(settings);
        Project project = settings.getParent().getProject();
        cmdLineInterfacesTable = new CmdLineInterfacesTable(project, settings.getCommandLineInterfaces());

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(cmdLineInterfacesTable);
        decorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                showNewInterfacePopup(
                        anActionButton.getDataContext(),
                        anActionButton.getPreferredPopupPoint());
            }
        });
        decorator.setRemoveAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                cmdLineInterfacesTable.removeRow();
            }
        });
        decorator.setMoveUpAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                cmdLineInterfacesTable.moveRowUp();
            }
        });
        decorator.setMoveDownAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                cmdLineInterfacesTable.moveRowDown();
            }
        });
        decorator.setPreferredSize(new Dimension(-1, 300));
        JPanel panel = decorator.createPanel();
        cmdLineInterfacesTablePanel.add(panel, BorderLayout.CENTER);
        cmdLineInterfacesTable.getParent().setBackground(cmdLineInterfacesTable.getBackground());
        registerComponents(mainPanel);
        updateBorderTitleForeground(mainPanel);
    }

    private void showNewInterfacePopup(DataContext dataContext, RelativePoint point) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (DatabaseType databaseType : DatabaseType.values()) {
            if (databaseType != DatabaseType.UNKNOWN){
                actionGroup.add(new CreateInterfaceAction(databaseType));
            }
        }

        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                null,
                actionGroup,
                dataContext,
                null,
                false);

        popup.show(point);
    }

    public class CreateInterfaceAction extends DumbAwareAction {
        private DatabaseType databaseType;
        public CreateInterfaceAction(DatabaseType databaseType) {
            super();
            getTemplatePresentation().setText(databaseType.getName(), false);
            getTemplatePresentation().setIcon(databaseType.getIcon());
            this.databaseType = databaseType;
        }

        public void actionPerformed(AnActionEvent e) {
            Project project = e.getProject();
            if (project != null) {
                ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(project);
                scriptExecutionManager.createCmdLineInterface(databaseType, cmdLineInterfacesTable.getNames(), new SimpleCallback<CmdLineInterface>() {
                    @Override
                    public void start(@Nullable CmdLineInterface inputValue) {
                        cmdLineInterfacesTable.addInterface(inputValue);
                    }
                });
            }
        }
    }
    
    public JPanel getComponent() {
        return mainPanel;
    }
    
    public void applyFormChanges() throws ConfigurationException {
        ScriptExecutionSettings settings = getConfiguration();
        settings.setExecutionTimeout(ConfigurationEditorUtil.validateIntegerInputValue(executionTimeoutTextField, "Execution timeout", true, 0, 6000, "\nUse value 0 for no timeout"));
        CmdLineInterfacesTableModel model = cmdLineInterfacesTable.getModel();
        model.validate();
        CmdLineInterfaceBundle executorBundle = model.getBundle();
        settings.setCommandLineInterfaces(executorBundle);
    }

    public void resetFormChanges() {
        ScriptExecutionSettings settings = getConfiguration();
        executionTimeoutTextField.setText(Integer.toString(settings.getExecutionTimeout()));
        cmdLineInterfacesTable.getModel().setBundle(settings.getCommandLineInterfaces());
    }
}
