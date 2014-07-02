package com.dci.intellij.dbn.common.locale.options.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.locale.DBDateFormat;
import com.dci.intellij.dbn.common.locale.DBNumberFormat;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;
import java.util.Locale;

public class RegionalSettingsEditorForm extends ConfigurationEditorForm<RegionalSettings> {
    private JPanel mainPanel;
    private JComboBox localeComboBox;
    private JLabel numberPreviewLabel;
    private JLabel integerPreviewLabel;
    private JLabel datePreviewLabel;
    private JLabel timePreviewLabel;
    private JPanel previewPanel;
    private JRadioButton shortRadioButton;
    private JRadioButton mediumRadioButton;
    private JRadioButton longRadioButton;
    private JRadioButton groupedRadioButton;
    private JRadioButton ungroupedRadioButton;
    private JTextField customNumberFormatTextField;
    private JTextField customDateFormatTextField;
    private JTextField customTimeFormatTextField;
    private JLabel errorLabel;
    private JRadioButton presetPatternsRadioButton;
    private JRadioButton customPatternsRadioButton;

    boolean isUpdating = false;


    private Date previewDate = new Date();
    private double previewNumber = ((double)(System.currentTimeMillis()/1000))/1000;

    public RegionalSettingsEditorForm(RegionalSettings regionalSettings) {
        super(regionalSettings);
        previewPanel.setBorder(UIUtil.getTextFieldBorder());
        previewPanel.setBackground(UIUtil.getToolTipBackground());
        errorLabel.setVisible(false);
        updateBorderTitleForeground(mainPanel);

        resetChanges();
        updatePreview();

        registerComponent(localeComboBox);
        registerComponent(shortRadioButton);
        registerComponent(mediumRadioButton);
        registerComponent(longRadioButton);

        registerComponent(groupedRadioButton);
        registerComponent(ungroupedRadioButton);

        registerComponent(presetPatternsRadioButton);
        registerComponent(customPatternsRadioButton);
        registerComponent(customNumberFormatTextField);
        registerComponent(customDateFormatTextField);
        registerComponent(customTimeFormatTextField);
    }

    @Override
    protected ItemListener createItemListener() {
        return new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                getConfiguration().setModified(true);
                updatePreview();
            }
        };
    }

    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getConfiguration().setModified(true);
                updatePreview();
            }
        };
    }

    @Override
    protected DocumentListener createDocumentListener() {
        return new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                getConfiguration().setModified(true);
                updatePreview();
            }
        };
    }

    public Locale getSelectedLocale() {
        LocaleComboBox localeComboBox = (LocaleComboBox) this.localeComboBox;
        return localeComboBox.getSelectedLocale();
    }

    public void setSelectedLocale(Locale locale) {
        LocaleComboBox localeComboBox = (LocaleComboBox) this.localeComboBox;
        localeComboBox.setSelectedLocale(locale);
    }

    private void updatePreview() {
        if (isUpdating) return;
        isUpdating = true;
        try {
            Locale locale = getSelectedLocale();
            DBDateFormat dateFormat = getSelectedDateFormat();
            DBNumberFormat numberFormat = getSelectedNumberFormat();
            boolean customSettings = customPatternsRadioButton.isSelected();
            Formatter formatter = null;
            if (customSettings) {
                try {
                    formatter = new Formatter(
                            locale,
                            customDateFormatTextField.getText(),
                            customTimeFormatTextField.getText(),
                            customNumberFormatTextField.getText());
                    errorLabel.setVisible(false);
                } catch (Exception e) {
                    errorLabel.setText("Invalid pattern: " + e.getMessage());
                    errorLabel.setIcon(Icons.STMT_EXECUTION_ERROR);
                    errorLabel.setVisible(true);
                }
            } else {
                formatter = new Formatter(locale, dateFormat, numberFormat);
                customNumberFormatTextField.setText(formatter.getNumberFormatPattern());
                customDateFormatTextField.setText(formatter.getDateFormatPattern());
                customTimeFormatTextField.setText(formatter.getTimeFormatPattern());
            }

            if (formatter != null) {
                datePreviewLabel.setText(formatter.formatDate(previewDate));
                timePreviewLabel.setText(formatter.formatTime(previewDate));
                numberPreviewLabel.setText(formatter.formatNumber(previewNumber));
                integerPreviewLabel.setText(formatter.formatInteger(previewNumber));
            }

            shortRadioButton.setEnabled(!customSettings);
            mediumRadioButton.setEnabled(!customSettings);
            longRadioButton.setEnabled(!customSettings);
            groupedRadioButton.setEnabled(!customSettings);
            ungroupedRadioButton.setEnabled(!customSettings);

            customNumberFormatTextField.setEnabled(customSettings);
            customDateFormatTextField.setEnabled(customSettings);
            customTimeFormatTextField.setEnabled(customSettings);
        } finally {
            isUpdating = false;
        }
    }

    private DBDateFormat getSelectedDateFormat() {
        return
            shortRadioButton.isSelected() ? DBDateFormat.SHORT :
            mediumRadioButton.isSelected() ? DBDateFormat.MEDIUM :
            longRadioButton.isSelected() ? DBDateFormat.LONG : DBDateFormat.MEDIUM;
    }

    private DBNumberFormat getSelectedNumberFormat(){
        return
            groupedRadioButton.isSelected() ? DBNumberFormat.GROUPED :
            ungroupedRadioButton.isSelected() ? DBNumberFormat.UNGROUPED : DBNumberFormat.UNGROUPED;
    }


    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyChanges() throws ConfigurationException {
        RegionalSettings regionalSettings = getConfiguration();

        Locale locale = getSelectedLocale();
        regionalSettings.setLocale(locale);

        DBDateFormat dateFormat = getSelectedDateFormat();
        regionalSettings.setDateFormatOption(dateFormat);

        DBNumberFormat numberFormat = getSelectedNumberFormat();
        regionalSettings.setNumberFormatOption(numberFormat);
        
        regionalSettings.getUseCustomFormats().applyChanges(customPatternsRadioButton);
        regionalSettings.getCustomDateFormat().applyChanges(customDateFormatTextField);
        regionalSettings.getCustomTimeFormat().applyChanges(customTimeFormatTextField);
        regionalSettings.getCustomNumberFormat().applyChanges(customNumberFormatTextField);
    }

    public void resetChanges() {
        RegionalSettings regionalSettings = getConfiguration();
        setSelectedLocale(regionalSettings.getLocale());

        Boolean useCustomFormats = regionalSettings.getUseCustomFormats().value();
        customPatternsRadioButton.setSelected(useCustomFormats);
        presetPatternsRadioButton.setSelected(!useCustomFormats);
        if (customPatternsRadioButton.isSelected()) {
            regionalSettings.getCustomDateFormat().resetChanges(customDateFormatTextField);
            regionalSettings.getCustomTimeFormat().resetChanges(customTimeFormatTextField);
            regionalSettings.getCustomNumberFormat().resetChanges(customNumberFormatTextField);
        }

        DBDateFormat dateFormat = regionalSettings.getDateFormatOption();
        if (dateFormat == DBDateFormat.SHORT) shortRadioButton.setSelected(true); else
        if (dateFormat == DBDateFormat.MEDIUM) mediumRadioButton.setSelected(true); else
        if (dateFormat == DBDateFormat.LONG) longRadioButton.setSelected(true);

        DBNumberFormat numberFormat = regionalSettings.getNumberFormatOption();
        if (numberFormat == DBNumberFormat.GROUPED) groupedRadioButton.setSelected(true); else
        if (numberFormat == DBNumberFormat.UNGROUPED) ungroupedRadioButton.setSelected(true);
        
        
    }

    private void createUIComponents() {
        localeComboBox = new LocaleComboBox();
    }
}
