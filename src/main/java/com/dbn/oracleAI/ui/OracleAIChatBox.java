package com.dbn.oracleAI.ui;

import com.dbn.common.ui.listener.KeyAdapter;
import com.dbn.execution.ExecutionManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;

import static com.dbn.oracleAI.DatabaseOracleAIManager.currConnection;
import static com.dbn.oracleAI.DatabaseOracleAIManager.queryOracleAI;

public class OracleAIChatBox extends JPanel {
  private final JComboBox<String> optionsComboBox;
  private final JTextArea inputTextArea;
  private final JTextArea displayTextArea;

  public OracleAIChatBox(Project project) {

//    ProjectEvents.subscribe(project, this, EnvironmentManagerListener.TOPIC, environmentManagerListener());

    setLayout(new BorderLayout());

    optionsComboBox = new ComboBox<>(new String[]{"narrate", "showsql"});
    JPanel comboBoxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    comboBoxPanel.add(optionsComboBox);
    add(comboBoxPanel, BorderLayout.NORTH);

    JPanel textFieldsPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 0.5;
    gbc.insets = JBUI.insets(10, 20);

    inputTextArea = new JTextArea();
    inputTextArea.setLineWrap(true);
    inputTextArea.setWrapStyleWord(true);
    inputTextArea.setMargin(JBUI.insets(10));
    JScrollPane inputTextScrollPane = new JBScrollPane(inputTextArea);
    inputTextScrollPane.setPreferredSize(new Dimension(400, 200));
    gbc.gridx = 0;
    gbc.gridy = 0;
    textFieldsPanel.add(inputTextScrollPane, gbc);

    inputTextArea.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
          e.consume();
          ApplicationManager.getApplication().executeOnPooledThread(() -> {
            String output = queryOracleAI(inputTextArea.getText(), Objects.requireNonNull(optionsComboBox.getSelectedItem()).toString());
            ApplicationManager.getApplication().invokeLater(() -> {
              setDisplayTextArea(output);
            });
          });
        }
      }
    });

    displayTextArea = new JTextArea();
    displayTextArea.setLineWrap(true);
    displayTextArea.setWrapStyleWord(true);
    displayTextArea.setEditable(false);
    displayTextArea.setMargin(JBUI.insets(10));
    JScrollPane displayTextScrollPane = new JBScrollPane(displayTextArea);
    displayTextScrollPane.setPreferredSize(new Dimension(400, 200));
    gbc.gridx = 0;
    gbc.gridy = 1;
    textFieldsPanel.add(displayTextScrollPane, gbc);

    add(textFieldsPanel, BorderLayout.CENTER);
  }

  public void setDisplayTextArea(String s) {
    displayTextArea.setText(s);
  }

//  @NotNull
//  private EnvironmentManagerListener environmentManagerListener() {
//    return new EnvironmentManagerListener() {
//      @Override
//      public void configurationChanged(Project project) {
//        EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(getProject()).getVisibilitySettings();
//        TabbedPane resultTabs = getResultTabs();
//        for (TabInfo tabInfo : resultTabs.getTabs()) {
//          updateTab(visibilitySettings, tabInfo);
//        }
//      }
//
//      private void updateTab(EnvironmentVisibilitySettings visibilitySettings, TabInfo tabInfo) {
//        ExecutionResult<?> executionResult = getExecutionResult(tabInfo);
//        if (executionResult != null) {
//          ConnectionHandler connection = executionResult.getConnection();
//          EnvironmentType environmentType = connection.getEnvironmentType();
//          if (visibilitySettings.getExecutionResultTabs().value()) {
//            tabInfo.setTabColor(environmentType.getColor());
//          } else {
//            tabInfo.setTabColor(null);
//          }
//        }
//      }
//    };
//  }

  @NotNull
  public ExecutionManager getExecutionManager() {
    Project project = currConnection.getProject();
    return ExecutionManager.getInstance(project);
  }
}
