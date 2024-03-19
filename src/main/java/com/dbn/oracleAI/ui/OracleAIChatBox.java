package com.dbn.oracleAI.ui;

import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.enums.ActionAIType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Hashtable;

public class OracleAIChatBox extends JPanel {
  private static final Dimension SCROLL_PANE_DIMENSION = new Dimension(400, 200);
  private static final Insets TEXT_AREA_INSETS = JBUI.insets(10);
  private static final Insets PANEL_INSETS = JBUI.insets(10, 20);

  private final JComboBox<String> optionsComboBox;
  private final JTextArea inputTextArea;
  private final JTextPane displayTextPane;
  private JRadioButton showRequestButton, executeRequestButton;
  private JCheckBox explainSQLCheckbox, narrateCheckbox;
  public DatabaseOracleAIManager currManager;
  private JPanel actionPanel, secondaryOptionsPanel;

  public OracleAIChatBox() {
    setLayout(new BorderLayout());
    optionsComboBox = new ComboBox<>(new String[]{"profile_1", "profile_2"});
    inputTextArea = createTextArea();
    displayTextPane = createTextPane();
    initializeUI();
  }

  private void initializeUI() {
    initializeTextStyles();
    JPanel topControlsPanel = new JPanel(new BorderLayout());
    topControlsPanel.add(createComboBoxPanel(), BorderLayout.NORTH);
    initializeActionSelectionComponents();
    topControlsPanel.add(actionPanel, BorderLayout.CENTER);
    topControlsPanel.add(secondaryOptionsPanel, BorderLayout.SOUTH);
    add(topControlsPanel, BorderLayout.NORTH);
    add(createTextFieldsPanel(), BorderLayout.CENTER);
  }

  private JPanel createComboBoxPanel() {
    JPanel comboBoxPanel = new JPanel(new BorderLayout());
    comboBoxPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    comboBoxPanel.add(createCenterPanel(), BorderLayout.WEST);
    comboBoxPanel.add(createSliderPanel(), BorderLayout.EAST);
    return comboBoxPanel;
  }

  private JPanel createCenterPanel() {
    JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    centerPanel.add(optionsComboBox);
    return centerPanel;
  }

  private JPanel createSliderPanel() {
    JSlider temperatureSlider = createTemperatureSlider();
    JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    sliderPanel.add(new JLabel("Temperature: "));
    sliderPanel.add(temperatureSlider);
    return sliderPanel;
  }

  private JSlider createTemperatureSlider() {
    JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);

    slider.setMajorTickSpacing(2);

    slider.setMinorTickSpacing(1);

    slider.setPaintTicks(true);
    slider.setPaintLabels(true);

    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
    labelTable.put(0, new JLabel("0"));
    labelTable.put(2, new JLabel(""));
    labelTable.put(4, new JLabel(""));
    labelTable.put(6, new JLabel(""));
    labelTable.put(8, new JLabel(""));
    labelTable.put(10, new JLabel("1"));
    slider.setLabelTable(labelTable);

    return slider;
  }

  private void initializeActionSelectionComponents() {
    createMainActionSelection();
    createSecondaryOptions();
  }

  private void createMainActionSelection() {
    showRequestButton = new JRadioButton("Show Request", true);
    executeRequestButton = new JRadioButton("Execute Request");
    ButtonGroup mainActionGroup = new ButtonGroup();
    mainActionGroup.add(showRequestButton);
    mainActionGroup.add(executeRequestButton);

    actionPanel = new JPanel(new FlowLayout());
    actionPanel.add(showRequestButton);
    actionPanel.add(executeRequestButton);

    attachActionListeners();
  }

  private void attachActionListeners() {
    showRequestButton.addActionListener(e -> updateSecondaryOptionsEnabled());
    executeRequestButton.addActionListener(e -> updateSecondaryOptionsEnabled());
  }

  private void createSecondaryOptions() {
    secondaryOptionsPanel = new JPanel(new FlowLayout());

    explainSQLCheckbox = new JCheckBox("Explain SQL");
    narrateCheckbox = new JCheckBox("Narrate");

    // Initial states based on the default selected radio button (Show Request)
    explainSQLCheckbox.setEnabled(showRequestButton.isSelected());
    narrateCheckbox.setEnabled(!showRequestButton.isSelected());

    secondaryOptionsPanel.add(explainSQLCheckbox);
    secondaryOptionsPanel.add(narrateCheckbox);
  }

  private void updateSecondaryOptionsEnabled() {
    explainSQLCheckbox.setEnabled(showRequestButton.isSelected());
    narrateCheckbox.setEnabled(executeRequestButton.isSelected());
  }

  private JPanel createTextFieldsPanel() {
    JPanel textFieldsPanel = new JPanel(new GridBagLayout());
    textFieldsPanel.add(createScrollPane(inputTextArea, SCROLL_PANE_DIMENSION), createGbc(0, 0.5));
    textFieldsPanel.add(createScrollPane(displayTextPane, SCROLL_PANE_DIMENSION), createGbc(1, 0.5));
    setupEnterAction();
    return textFieldsPanel;
  }

  private JScrollPane createScrollPane(Component view, Dimension dimension) {
    JScrollPane scrollPane = new JBScrollPane(view);
    scrollPane.setPreferredSize(dimension);
    return scrollPane;
  }

  private GridBagConstraints createGbc(int grid_y, double weight_y) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = weight_y;
    gbc.insets = PANEL_INSETS;
    gbc.gridx = 0;
    gbc.gridy = grid_y;
    return gbc;
  }

  private JTextArea createTextArea() {
    JTextArea textArea = new JTextArea();
    configureTextArea(textArea, true);
    return textArea;
  }

  private JTextPane createTextPane() {
    JTextPane textPane = new JTextPane();
    configureTextArea(textPane, false);
    return textPane;
  }

  private void configureTextArea(JTextComponent textComponent, boolean editable) {
    textComponent.setEditable(editable);
    textComponent.setMargin(TEXT_AREA_INSETS);
    if (textComponent instanceof JTextArea) {
      JTextArea textArea = (JTextArea) textComponent;
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
    }
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
//    String selectedAction = (String) secondaryOptionsComboBox.getSelectedItem();
    ActionAIType actionType = ActionAIType.NARRATE;
    try {
//      actionType = ActionAIType.getByAction(selectedAction);
    } catch (IllegalArgumentException ex) {
      JOptionPane.showMessageDialog(this, "Invalid action selected.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    ApplicationManager.getApplication().executeOnPooledThread(() ->
        processQuery(actionType));
  }

  private void processQuery(ActionAIType actionType) {
    String output = currManager.queryOracleAI(inputTextArea.getText(), actionType);
    ApplicationManager.getApplication().invokeLater(() -> setDisplayTextPane(output));
  }


  public void setDisplayTextPane(String s) {
    try {
      StyledDocument doc = displayTextPane.getStyledDocument();
      doc.remove(0, doc.getLength());

      String[] tokens = s.split("```");
      boolean isCode = false;
      for (String token : tokens) {
        if (isCode) {
          doc.insertString(doc.getLength(), token, doc.getStyle("code"));
        } else {
          doc.insertString(doc.getLength(), token, doc.getStyle("regular"));
        }
        isCode = !isCode;
      }
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  public void updateForConnection(String connection) {
//    titleLabel.setText(connection);
    inputTextArea.setText("");
    displayTextPane.setText("");

  }

  private void initializeTextStyles() {
    StyledDocument doc = displayTextPane.getStyledDocument();
    Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

    Style regular = doc.addStyle("regular", def);
    StyleConstants.setFontFamily(def, "SansSerif");

    Style s = doc.addStyle("code", regular);
    StyleConstants.setForeground(s, Color.getHSBColor(210, 50, 100));
  }

  public OracleAIChatBoxState captureState(String connection) {
    OracleAIChatBoxState state = new OracleAIChatBoxState(connection);
    state.setSelectedOption((String) optionsComboBox.getSelectedItem());
    state.setInputText(inputTextArea.getText());
    state.setDisplayText(displayTextPane.getText());
    return state;
  }

  public void restoreState(OracleAIChatBoxState state) {
    if (state == null) return;
//    titleLabel.setText(state.getCurrConnection());
    optionsComboBox.setSelectedItem(state.getSelectedOption());
    inputTextArea.setText(state.getInputText());
    displayTextPane.setText(state.getDisplayText());
  }
}
