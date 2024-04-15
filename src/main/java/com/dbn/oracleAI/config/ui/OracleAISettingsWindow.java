package com.dbn.oracleAI.config.ui;

import com.dbn.connection.ConnectionHandler;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

public class OracleAISettingsWindow extends JDialog {

  static private final ResourceBundle messages =
    ResourceBundle.getBundle("Messages", Locale.getDefault());

  private JPanel settingsWindowContentPane;
  private JTabbedPane settingsTabbedPane;
  private JPanel settingsWindowBottomPanel;
  private JButton settingsCloseButton;
  private ConnectionHandler currConnection;

  public OracleAISettingsWindow(ConnectionHandler connection) {
    super(WindowManager.getInstance().getFrame(connection.getProject()), messages.getString(
      "ai.settings.window.title"), true);
    currConnection = connection;
    setContentPane(settingsWindowContentPane);
    setTitle(messages.getString("ai.settings.window.title"));
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    pack();
    setLocationRelativeTo(WindowManager.getInstance().getFrame(connection.getProject()));
    setTabs();

    settingsCloseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
  }

  public void display() {
    setVisible(true);
  }



  private void setTabs() {
    settingsTabbedPane.addTab(messages.getString("ai.settings.window.tab.profiles.title"), new ProfileManagementPanel(currConnection));
    settingsTabbedPane.addTab(messages.getString("ai.settings.window.tab.credentials.title"), new CredentialManagementPanel(currConnection));
  }
}
