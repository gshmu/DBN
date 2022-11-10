package com.dci.intellij.dbn.data.type.ui;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.code.psql.style.PSQLCodeStyle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.editor.ui.BasicListPopupValuesProvider;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithPopup;
import com.dci.intellij.dbn.data.type.DataTypeDefinition;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class DataTypeEditor extends TextFieldWithPopup {
    public DataTypeEditor(ConnectionHandler connection) {
        super(connection.getProject());
        CodeStyleCaseSettings caseSettings = PSQLCodeStyle.caseSettings(getProject());
        CodeStyleCaseOption caseOption = caseSettings.getObjectCaseOption();

        List<DataTypeDefinition> nativeDataTypes = connection.getInterfaceProvider().getNativeDataTypes().list();
        List<String> nativeDataTypeNames = new ArrayList<>();
        for (DataTypeDefinition nativeDataType : nativeDataTypes) {
            String typeName = nativeDataType.getName();
            typeName = caseOption.format(typeName);
            nativeDataTypeNames.add(typeName);
        }
        BasicListPopupValuesProvider valuesProvider = new BasicListPopupValuesProvider("Native Data Types", nativeDataTypeNames);
        createValuesListPopup(valuesProvider, true);
    }



    public String getDataTypeRepresentation() {
        return getTextField().getText();
    }

    @Override
    public void customizeButton(JLabel button) {
        super.customizeButton(button);
    }
}
