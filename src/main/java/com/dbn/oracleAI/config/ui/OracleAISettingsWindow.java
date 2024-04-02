package com.dbn.oracleAI.config.ui;

import com.dbn.connection.ConnectionHandler;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;

public class OracleAISettingsWindow extends JDialog {
  private JPanel contentPane;
  private JTabbedPane tabbedPane;
  private ConnectionHandler currConnection;
  public OracleAISettingsWindow(ConnectionHandler connection) {
    super(WindowManager.getInstance().getFrame(connection.getProject()), "Oracle AI Chat Box", true);
    currConnection = connection;
    setContentPane(contentPane);
    setTitle("Oracle AI Settings");
    setSize(1000, 700);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(WindowManager.getInstance().getFrame(connection.getProject()));
    setTabs();
  }

  public void display() {
    setVisible(true);
  }


  private void setTabs() {
    tabbedPane.addTab("Profiles",new ProfileManagementPanel(currConnection));
    tabbedPane.addTab("Credentials", new CredentialManagementPanel());
  }
}
