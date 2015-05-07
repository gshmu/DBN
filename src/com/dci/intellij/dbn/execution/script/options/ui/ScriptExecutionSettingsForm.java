package com.dci.intellij.dbn.execution.script.options.ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.execution.script.CmdLineInterfaceBundle;
import com.dci.intellij.dbn.execution.script.options.ScriptExecutionSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;

public class ScriptExecutionSettingsForm extends ConfigurationEditorForm<ScriptExecutionSettings> {
    private JPanel mainPanel;
    private JPanel cmdLineInterfacesTablePanel;
    private CmdLineInterfacesTable cmdLineInterfacesTable;

    public ScriptExecutionSettingsForm(ScriptExecutionSettings settings) {
        super(settings);
        Project project = settings.getParent().getProject();
        cmdLineInterfacesTable = new CmdLineInterfacesTable(project, settings.getCommandLineInterfaces());

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(cmdLineInterfacesTable);
        decorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                cmdLineInterfacesTable.insertRow();
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
    
    public JPanel getComponent() {
        return mainPanel;
    }
    
    public void applyFormChanges() throws ConfigurationException {
        ScriptExecutionSettings settings = getConfiguration();
        CmdLineInterfacesTableModel model = cmdLineInterfacesTable.getModel();
        model.validate();
        CmdLineInterfaceBundle executorBundle = model.getCmdLineInterfaces();
        settings.setCommandLineInterfaces(executorBundle);
    }

    public void resetFormChanges() {
        ScriptExecutionSettings settings = getConfiguration();
        cmdLineInterfacesTable.getModel().setCmdLineInterfaces(settings.getCommandLineInterfaces());
    }
}
