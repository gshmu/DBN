package com.dbn.oracleAI.config.providers.ui;


import com.dbn.common.action.BasicActionButton;
import com.dbn.common.icon.Icons;
import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.oracleAI.config.providers.AIProviderCredentialBundle;
import com.dbn.oracleAI.config.providers.AIProviderCredentialSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class AIProviderCredentialsSettingsForm extends ConfigurationEditorForm<AIProviderCredentialSettings> {
  private JPanel mainPanel;
  private JPanel credentialsTablePanel;

  private final AIProviderCredentialsEditorTable credentialsTable;

  public AIProviderCredentialsSettingsForm(AIProviderCredentialSettings settings) {
    super(settings);

    credentialsTable = new AIProviderCredentialsEditorTable(this, settings.getCredentials());


    ToolbarDecorator decorator = UserInterface.createToolbarDecorator(credentialsTable);
    decorator.setAddAction(anActionButton -> credentialsTable.insertRow());
    decorator.setRemoveAction(anActionButton -> credentialsTable.removeRow());
    decorator.setMoveUpAction(anActionButton -> credentialsTable.moveRowUp());
    decorator.setMoveDownAction(anActionButton -> credentialsTable.moveRowDown());
    decorator.addExtraAction(new BasicActionButton("Revert Changes", null, Icons.ACTION_REVERT) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        TableCellEditor cellEditor = credentialsTable.getCellEditor();
        if (cellEditor != null) {
          cellEditor.cancelCellEditing();
        }

        credentialsTable.setCredentials(getConfiguration().getCredentials());
      }

    });
    decorator.setPreferredSize(new Dimension(-1, 200));
    JPanel panel = decorator.createPanel();
    credentialsTablePanel.add(panel, BorderLayout.CENTER);
    credentialsTable.getParent().setBackground(credentialsTable.getBackground());
    registerComponents(mainPanel);
  }

  @NotNull
  @Override
  public JPanel getMainComponent() {
    return mainPanel;
  }

  @Override
  public void applyFormChanges() throws ConfigurationException {
    AIProviderCredentialSettings configuration = getConfiguration();
    AIProviderCredentialTableModel model = credentialsTable.getModel();
    model.validate();
    AIProviderCredentialBundle credentials = model.getCredentials();
    configuration.setCredentials(credentials);
  }

  @Override
  public void resetFormChanges() {
    AIProviderCredentialSettings settings = getConfiguration();
    credentialsTable.getModel().setCredentials(settings.getCredentials());
  }
}
