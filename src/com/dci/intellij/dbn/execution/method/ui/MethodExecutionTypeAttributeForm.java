package com.dci.intellij.dbn.execution.method.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.data.editor.ui.ListPopupValuesProvider;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithPopup;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithTextEditor;
import com.dci.intellij.dbn.data.editor.ui.UserValueHolderImpl;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

public class MethodExecutionTypeAttributeForm extends DBNFormImpl implements DBNForm {
    private JLabel attributeTypeLabel;
    private JLabel attributeLabel;
    private JPanel mainPanel;
    private JPanel attributePanel;
    private JPanel inputFieldPanel;

    private JComponent inputComponent;
    private JTextField inputTextField;
    private UserValueHolderImpl<String> userValueHolder;

    private DBObjectRef<DBArgument> argumentRef; // TODO replace with argument ref
    private DBObjectRef<DBTypeAttribute> typeAttributeRef;
    private MethodExecutionForm executionComponent;

    public MethodExecutionTypeAttributeForm(DBArgument argument, DBTypeAttribute typeAttribute, MethodExecutionForm executionComponent) {
        this.argumentRef = DBObjectRef.from(argument);
        this.typeAttributeRef = DBObjectRef.from(typeAttribute);
        this.executionComponent = executionComponent;
        attributeLabel.setText(typeAttribute.getName());
        attributeLabel.setIcon(typeAttribute.getIcon());
        attributeTypeLabel.setForeground(UIUtil.getInactiveTextColor());
        attributeTypeLabel.setText(typeAttribute.getDataType().getQualifiedName());

        DBDataType dataType = typeAttribute.getDataType();
        GenericDataType genericDataType = dataType.getGenericDataType();

        Project project = argument.getProject();

        String value = executionComponent.getExecutionInput().getInputValue(argument, typeAttribute);
        if (genericDataType.is(GenericDataType.XMLTYPE, GenericDataType.CLOB)) {
            TextFieldWithTextEditor inputField = new TextFieldWithTextEditor(project, "[" + genericDataType.name() + "]");

            TextContentType contentType =
                    genericDataType == GenericDataType.XMLTYPE ?
                            TextContentType.get(project, "XML") :
                            TextContentType.getPlainText(project);
            if (contentType == null) {
                contentType = TextContentType.getPlainText(project);
            }

            String typeAttributeName = argument.getName() + "." + typeAttribute.getName();
            userValueHolder = new UserValueHolderImpl<String>(typeAttributeName, DBObjectType.TYPE_ATTRIBUTE, dataType, project);
            userValueHolder.setUserValue(value);
            userValueHolder.setContentType(contentType);
            inputField.setUserValueHolder(userValueHolder);

            inputField.setPreferredSize(new Dimension(240, -1));
            inputTextField = inputField.getTextField();
            inputFieldPanel.add(inputField, BorderLayout.CENTER);
        } else {
            TextFieldWithPopup inputField = new TextFieldWithPopup(project);
            inputField.setPreferredSize(new Dimension(240, -1));
            if (genericDataType == GenericDataType.DATE_TIME) {
                inputField.createCalendarPopup(false);
            }

            inputField.createValuesListPopup(createValuesProvider(), true);
            inputTextField = inputField.getTextField();
            inputFieldPanel.add(inputField, BorderLayout.CENTER);
            inputTextField.setText(value);
        }

        inputTextField.setDisabledTextColor(inputTextField.getForeground());
    }

    @NotNull
    private ListPopupValuesProvider createValuesProvider() {
        return new ListPopupValuesProvider() {
            @Override
            public String getDescription() {
                return "History Values List";
            }

            @Override
            public List<String> getValues() {
                DBArgument argument = getArgument();
                DBTypeAttribute typeAttribute = getTypeAttribute();
                if (argument != null && typeAttribute != null) {
                    return executionComponent.getExecutionInput().getInputValueHistory(argument, typeAttribute);
                }
                return Collections.emptyList();
            }

            @Override
            public boolean isLongLoading() {
                return false;
            }
        };
    }

    public DBArgument getArgument() {
        return DBObjectRef.get(argumentRef);
    }

    public DBTypeAttribute getTypeAttribute() {
        return DBObjectRef.get(typeAttributeRef);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void updateExecutionInput() {
        DBArgument argument = getArgument();
        DBTypeAttribute typeAttribute = getTypeAttribute();
        if (argument != null && typeAttribute != null) {
            if (userValueHolder != null ) {
                String value = CommonUtil.nullIfEmpty(userValueHolder.getUserValue());
                executionComponent.getExecutionInput().setInputValue(argument, typeAttribute, value);
            } else {
                String value = CommonUtil.nullIfEmpty(inputTextField == null ? null : inputTextField.getText());
                executionComponent.getExecutionInput().setInputValue(argument, typeAttribute, value);
            }
        }
    }

    protected int[] getMetrics(int[] metrics) {
        if (metrics == null) metrics = new int[]{0, 0};
        return new int[] {
                (int) Math.max(metrics[0], attributePanel.getPreferredSize().getWidth()),
                (int) Math.max(metrics[1], inputFieldPanel.getPreferredSize().getWidth())};
    }

    protected void adjustMetrics(int[] metrics) {
        attributePanel.setPreferredSize(new Dimension(metrics[0], attributePanel.getHeight()));
        inputFieldPanel.setPreferredSize(new Dimension(metrics[1], inputFieldPanel.getHeight()));
    }

    public void addDocumentListener(DocumentListener documentListener){
        inputTextField.getDocument().addDocumentListener(documentListener);
    }

    public void dispose() {
        super.dispose();
        executionComponent = null;
    }
}
