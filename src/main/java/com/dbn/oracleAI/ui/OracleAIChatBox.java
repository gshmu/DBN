package com.dbn.oracleAI.ui;

import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.enums.ActionAIType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.text.DefaultEditorKit;

public class OracleAIChatBox extends JPanel {
  private static final Dimension SCROLL_PANE_DIMENSION = new Dimension(400, 200);
  private static final Insets TEXT_AREA_INSETS = JBUI.insets(10);
  private static final Insets PANEL_INSETS = JBUI.insets(10, 20);

  private final JComboBox<String> optionsComboBox;
  private final JTextArea inputTextArea;
  private final JTextArea displayTextArea;
  private final JLabel titleLabel;
  public DatabaseOracleAIManager currManager;

  public OracleAIChatBox() {
    setLayout(new BorderLayout());
    titleLabel = new JLabel();
    optionsComboBox = new ComboBox<>(new String[]{"narrate", "showsql"});
    inputTextArea = createTextArea(true);
    displayTextArea = createTextArea(false);
    initializeUI();
  }

  private void initializeUI() {
    add(createComboBoxPanel(), BorderLayout.NORTH);
    add(createTextFieldsPanel(), BorderLayout.CENTER);
  }

  private JPanel createComboBoxPanel() {
    JPanel comboBoxPanel = new JPanel(new BorderLayout());
    comboBoxPanel.add(titleLabel, BorderLayout.WEST);

    JPanel comboHolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    comboHolderPanel.add(optionsComboBox);
    comboBoxPanel.add(comboHolderPanel, BorderLayout.EAST);
    comboBoxPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

    return comboBoxPanel;
  }

  private JPanel createTextFieldsPanel() {
    JPanel textFieldsPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 0.5;
    gbc.insets = PANEL_INSETS;

    JScrollPane inputTextScrollPane = new JBScrollPane(inputTextArea);
    inputTextScrollPane.setPreferredSize(SCROLL_PANE_DIMENSION);
    gbc.gridx = 0;
    gbc.gridy = 0;
    textFieldsPanel.add(inputTextScrollPane, gbc);

    JScrollPane displayTextScrollPane = new JBScrollPane(displayTextArea);
    displayTextScrollPane.setPreferredSize(SCROLL_PANE_DIMENSION);
    gbc.gridy = 1;
    textFieldsPanel.add(displayTextScrollPane, gbc);

    setupEnterAction();

    return textFieldsPanel;
  }

  private JTextArea createTextArea(boolean editable) {
    JTextArea textArea = new JTextArea();
    textArea.setEditable(editable);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setMargin(TEXT_AREA_INSETS);
    return textArea;
  }

  private void setupEnterAction() {
    InputMap inputMap = inputTextArea.getInputMap(JComponent.WHEN_FOCUSED);
    ActionMap actionMap = inputTextArea.getActionMap();

    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "submit");
    inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");
    actionMap.put("submit", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        submitText();
      }
    });
    actionMap.put("insert-break", new DefaultEditorKit.InsertBreakAction());
  }

  private void submitText() {
    String selectedAction = (String) optionsComboBox.getSelectedItem();
    ActionAIType actionType;
    try {
      actionType = ActionAIType.getByAction(Objects.requireNonNull(selectedAction));
    } catch (IllegalArgumentException ex) {
      JOptionPane.showMessageDialog(this, "Invalid action selected.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    ApplicationManager.getApplication().executeOnPooledThread(() ->
        processQuery(actionType));
  }

  private void processQuery(ActionAIType actionType) {
    String output = currManager.queryOracleAI(inputTextArea.getText(), actionType);
    ApplicationManager.getApplication().invokeLater(() -> setDisplayTextArea(output));
  }


  public void setDisplayTextArea(String s) {
    displayTextArea.setText(s);
  }

  public void updateForConnection(String connection) {
    titleLabel.setText(connection);
    inputTextArea.setText("");
    displayTextArea.setText("");

  }
  public OracleAIChatBoxState captureState(String connection) {
    OracleAIChatBoxState state = new OracleAIChatBoxState(connection);
    state.setSelectedOption((String) optionsComboBox.getSelectedItem());
    state.setInputText(inputTextArea.getText());
    state.setDisplayText(displayTextArea.getText());
    return state;
  }

  public void restoreState(OracleAIChatBoxState state) {
    if (state == null) return;
    titleLabel.setText(state.getCurrConnection());
    optionsComboBox.setSelectedItem(state.getSelectedOption());
    inputTextArea.setText(state.getInputText());
    displayTextArea.setText(state.getDisplayText());
  }
}
