package com.dci.intellij.dbn.data.type.ui;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.psql.style.options.PSQLCodeStyleSettings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.editor.ui.BasicListPopupValuesProvider;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithPopup;
import com.dci.intellij.dbn.data.type.DataTypeDefinition;

import javax.swing.JLabel;
import java.util.ArrayList;
import java.util.List;

public class DataTypeEditor extends TextFieldWithPopup {
    public DataTypeEditor(ConnectionHandler connection) {
        super(connection.getProject());
        PSQLCodeStyleSettings codeStyleSettings =
                PSQLCodeStyleSettings.getInstance(connection.getProject());
        CodeStyleCaseOption caseOption = codeStyleSettings.getCaseSettings().getDatatypeCaseOption();

        List<DataTypeDefinition> nativeDataTypes = connection.getInterfaceProvider().getNativeDataTypes().list();
        List<String> nativeDataTypeNames = new ArrayList<String>();
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
