package com.dci.intellij.dbn.execution.method.ui;

import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithPopup;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.data.type.DataTypeDefinition;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.intellij.util.ui.UIUtil;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class MethodExecutionTypeAttributeForm extends DBNFormImpl implements DBNForm {
    private JLabel attributeTypeLabel;
    private JLabel attributeLabel;
    private JPanel mainPanel;
    private JPanel attributePanel;
    private JPanel inputFieldPanel;

    private JComponent inputComponent;
    private JTextField inputTextField;

    private DBArgument argument;
    private DBTypeAttribute typeAttribute;
    private MethodExecutionForm executionComponent;

    public MethodExecutionTypeAttributeForm(DBArgument argument, DBTypeAttribute typeAttribute, MethodExecutionForm executionComponent) {
        this.argument = argument;
        this.typeAttribute = typeAttribute;
        this.executionComponent = executionComponent;
        attributeLabel.setText(typeAttribute.getName());
        attributeLabel.setIcon(typeAttribute.getIcon());
        attributeTypeLabel.setForeground(UIUtil.getInactiveTextColor());
        attributeTypeLabel.setText(typeAttribute.getDataType().getQualifiedName());

        DBDataType dataType = typeAttribute.getDataType();
        DBNativeDataType nativeDataType = dataType.getNativeDataType();
        DataTypeDefinition dataTypeDefinition = nativeDataType.getDataTypeDefinition();
        GenericDataType genericDataType = dataTypeDefinition.getGenericDataType();


        if (genericDataType == GenericDataType.DATE_TIME) {
            TextFieldWithPopup inputField = new TextFieldWithPopup(argument.getProject());
            inputField.setPreferredSize(new Dimension(200, -1));
            inputField.createCalendarPopup(false);
            inputComponent  = inputField;
            inputTextField = inputField.getTextField();

        } else {
            inputTextField = new JTextField();
            inputTextField.setPreferredSize(new Dimension(200, -1));
            inputComponent = inputTextField;
        }
        String value = executionComponent.getExecutionInput().getInputValue(argument, typeAttribute);
        inputTextField.setText(value);
        inputTextField.setDisabledTextColor(inputTextField.getForeground());
        inputFieldPanel.add(inputComponent, BorderLayout.CENTER);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void updateExecutionInput() {
        String value = CommonUtil.nullIfEmpty(inputTextField.getText());
        executionComponent.getExecutionInput().setInputValue(argument, typeAttribute, value);
    }

    protected int[] getMetrics(int[] metrics) {
        if (metrics == null) metrics = new int[]{0, 0};
        return new int[] {
                (int) Math.max(metrics[0], attributePanel.getPreferredSize().getWidth()),
                (int) Math.max(metrics[1], inputComponent.getPreferredSize().getWidth())};
    }

    protected void adjustMetrics(int[] metrics) {
        attributePanel.setPreferredSize(new Dimension(metrics[0], attributePanel.getHeight()));
        inputComponent.setPreferredSize(new Dimension(metrics[1], inputComponent.getHeight()));
    }

    public void addDocumentListener(DocumentListener documentListener){
        inputTextField.getDocument().addDocumentListener(documentListener);
    }

    public void dispose() {
        super.dispose();
        argument = null;
        typeAttribute = null;
        executionComponent = null;
    }
}
