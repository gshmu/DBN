package com.dbn.connection.config.ui;

import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.config.AICloudSettings;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

public class AICloudSettingsForm extends ConfigurationEditorForm<AICloudSettings> {

  private JPanel mainPanel;
  private JLabel intro;
  private JLabel networkAllow;
  private JComboBox providerComboBox;
  private JTextArea aclTextArea;
  private JTextArea grantTextArea;
  private JLabel intro2;
  private JButton copyButton;
  private JButton applyButton;
  private JLabel grantTextField;

  // Variable to hold the user name, replacing the hard-coded "SCOTT"
  private String userName;

  public AICloudSettingsForm(final AICloudSettings aiCloudSettings) {
    super(aiCloudSettings);
    if(ConnectionHandler.get(getConfiguration().getConnectionId())!=null) {
      this.userName = ConnectionHandler.get(getConfiguration().getConnectionId()).getUserName();
    }
    providerComboBox.addItem("OpenAI");
    providerComboBox.addItem("Cohere");

    intro.setText("This section displays some mandatory actions that need to be made on the server side for the use of this connection.");
    intro2.setText("Please perform the following logged as admin to this remote database.");

    grantTextField.setText("Grant execution to user " + userName + " for the AI Cloud Package.");

    grantTextArea.setText("grant execute on DBMS_CLOUD to " + userName + "; \ngrant execute on DBM$_CLOUD_AI to " + userName + ";");

    networkAllow.setText("Allow Network Access to AI Provider");

    aclTextArea.setText("BEGIN\n    DBMS_NETWORK_ACL_ADMIN.APPEND_HOST_ACE(\n       HOST => 'api.cohere.ai',\n       ACE => XS$ACE_TYPE(PRIVILEGE_LIST => XS$NAME_LIST('http'),\n         PRINCIPAL_NAME =>'" + userName + "',\n\tPRINCIPAL_TYPE => XS_ACL.PTYPE_DB));\nEND;\n/");
  }

  @Override
  public void applyFormChanges() throws ConfigurationException {

  }

  @Override
  public void resetFormChanges() {

  }

  @Override
  protected JComponent getMainComponent() {
    return mainPanel;
  }
}
