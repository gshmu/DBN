package com.dbn.oracleAI.ui;

import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.types.ActionAIType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class OracleAIChatBox extends JPanel {
  private static final Insets TEXT_AREA_INSETS = JBUI.insets(10);
  private static final int MIN_TEMPERATURE = 0;
  private static final int MAX_TEMPERATURE = 10;
  private static final int DEFAULT_TEMPERATURE = 5;

  private static OracleAIChatBox instance;
  private JComboBox<String> optionsComboBox;
  private JPanel panel1;
  private JSlider temperatureSlider;
  private JComboBox<String> comboBox;
  private JTextPane displayTextPane;
  private JRadioButton showRequestButton, executeRequestButton;
  private JCheckBox explainSQLCheckbox, narrateCheckbox;
  public DatabaseOracleAIManager currManager;

  private OracleAIChatBox(Project project) {
    currManager = project.getService(DatabaseOracleAIManager.class);
    initializeUI();
    this.setLayout(new BorderLayout(0, 0));
    this.add(panel1);
  }
  public static OracleAIChatBox getInstance(Project project) {
    if (instance == null) {
      instance = new OracleAIChatBox(project);
    }
    return instance;
  }

  private void initializeUI() {
    initializeTextStyles();
    initializeActionSelectionComponents();
    configureTemperatureSlider();
    createTextFieldsPanel();
    configureTextArea(displayTextPane);

    ResourceBundle actions = ResourceBundle.getBundle("Messages", Locale.getDefault());

    showRequestButton.setText(actions.getString("showRequest.action"));
    executeRequestButton.setText(actions.getString("executeRequest.action"));
    narrateCheckbox.setText(actions.getString("narrate.action"));
    explainSQLCheckbox.setText(actions.getString("explainSql.action"));

    optionsComboBox.addActionListener(e -> {
      if(optionsComboBox.getSelectedItem()=="New Profile...") currManager.openSettings();
    });

  }

  private void configureTemperatureSlider() {
    temperatureSlider.setMinimum(MIN_TEMPERATURE);
    temperatureSlider.setMaximum(MAX_TEMPERATURE);
    temperatureSlider.setValue(DEFAULT_TEMPERATURE);
    temperatureSlider.setMajorTickSpacing(2);
    temperatureSlider.setMinorTickSpacing(1);
    temperatureSlider.setPaintTicks(true);
    temperatureSlider.setPaintLabels(true);

    updateSliderLabels(temperatureSlider, temperatureSlider.getValue());

    temperatureSlider.addChangeListener(e -> {
      JSlider source = (JSlider)e.getSource();
      if (!source.getValueIsAdjusting()) {
        updateSliderLabels(source, source.getValue());
      }
    });

  }

  private void updateSliderLabels(JSlider slider, int currentValue) {
    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
    labelTable.put(0, new JLabel("0"));
    labelTable.put(currentValue, new JLabel(String.valueOf((float) currentValue/10)));
    labelTable.put(10, new JLabel("1"));
    slider.setLabelTable(labelTable);
  }


  private void initializeActionSelectionComponents() {
    attachActionListeners();
    createSecondaryOptions();
  }


  private void attachActionListeners() {
    showRequestButton.addActionListener(e -> updateSecondaryOptionsEnabled());
    executeRequestButton.addActionListener(e -> updateSecondaryOptionsEnabled());
  }

  private void createSecondaryOptions() {
    explainSQLCheckbox.setEnabled(showRequestButton.isSelected());
    narrateCheckbox.setEnabled(!showRequestButton.isSelected());
  }

  private void updateSecondaryOptionsEnabled() {
    explainSQLCheckbox.setEnabled(showRequestButton.isSelected());
    narrateCheckbox.setEnabled(executeRequestButton.isSelected());
  }

  private void createTextFieldsPanel() {

    comboBox.addItem("");
    comboBox.addItem("What are the names of all the customers");
    comboBox.addItem("Who joined after February 2022");
    comboBox.addItem("Can you list all customers by their join date in ascending order");
    comboBox.addItem("I need the email addresses and join dates of customers whose last name is Doe");
    comboBox.setPreferredSize(new Dimension(50, 50));
    setupEnterAction();
  }


  private void configureTextArea(JTextComponent textComponent) {
    textComponent.setEditable(false);
    textComponent.setMargin(TEXT_AREA_INSETS);
    if (textComponent instanceof JTextArea) {
      JTextArea textArea = (JTextArea) textComponent;
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
    }
  }
  private void setupEnterAction() {

    Component editorComponent = comboBox.getEditor().getEditorComponent();

    if (editorComponent instanceof JComponent) {
      JComponent editor = (JComponent) editorComponent;

      InputMap inputMap = editor.getInputMap(JComponent.WHEN_FOCUSED);
      ActionMap actionMap = editor.getActionMap();

      inputMap.put(KeyStroke.getKeyStroke("ENTER"), "submit");
      inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");

      actionMap.put("submit", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          submitText();
        }
      });
    }


  }


  private void submitText() {
    ApplicationManager.getApplication().executeOnPooledThread(() ->
        processQuery(selectedAction()));
  }

  private ActionAIType selectedAction(){
    if(showRequestButton.isSelected()){
      if(explainSQLCheckbox.isSelected()){
        return ActionAIType.EXPLAINSQL;
      } else {
        return ActionAIType.SHOWSQL;
      }
    } else{
      if(narrateCheckbox.isSelected()){
        return ActionAIType.NARRATE;
      } else {
        return ActionAIType.EXECUTESQL;
      }
    }
  }
  private void processQuery(ActionAIType actionType) {
    String output = currManager.queryOracleAI(Objects.requireNonNull(comboBox.getSelectedItem()).toString(), actionType, Objects.requireNonNull(optionsComboBox.getSelectedItem()).toString());
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

  private void initializeTextStyles() {
    StyledDocument doc = displayTextPane.getStyledDocument();
    Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

    Style regular = doc.addStyle("regular", def);
    StyleConstants.setFontFamily(def, "SansSerif");

    Style s = doc.addStyle("code", regular);
    StyleConstants.setForeground(s, Color.getHSBColor(210, 50, 100));
  }

  public void updateProfiles(String selectdProfile) {


    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      List<Profile> fetchedProfiles = currManager.fetchProfiles();
      ApplicationManager.getApplication().invokeLater(() -> {
        optionsComboBox.removeAllItems();
        populateComboBoxWithProfiles(fetchedProfiles);
        optionsComboBox.setSelectedItem(selectdProfile);
      });
    });

  }

  private void populateComboBoxWithProfiles(List<Profile> profiles) {
    for (Profile profile : profiles) {
      optionsComboBox.addItem(profile.getProfileName());
    }
    optionsComboBox.addItem("New Profile...");
  }
  public OracleAIChatBoxState captureState(String connection) {
    OracleAIChatBoxState state = new OracleAIChatBoxState(connection);
    state.setSelectedOption((String) optionsComboBox.getSelectedItem());
    state.setInputText(comboBox.getSelectedItem().toString());
    state.setDisplayText(displayTextPane.getText());
    return state;
  }

  public void restoreState(OracleAIChatBoxState state) {
    if (state == null) return;
    optionsComboBox.addItem(state.getSelectedOption());
    optionsComboBox.setSelectedItem(state.getSelectedOption());
    this.updateProfiles(state.getSelectedOption());

    comboBox.setSelectedItem(state.getInputText());
    displayTextPane.setText(state.getDisplayText());
  }
}
