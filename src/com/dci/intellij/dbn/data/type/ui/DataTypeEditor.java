package com.dci.intellij.dbn.data.type.ui;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.psql.style.options.PSQLCodeStyleSettings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithPopup;
import com.dci.intellij.dbn.data.type.DataTypeDefinition;

import java.util.ArrayList;
import java.util.List;

public class DataTypeEditor extends TextFieldWithPopup {
    public DataTypeEditor(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject());
        PSQLCodeStyleSettings codeStyleSettings =
                PSQLCodeStyleSettings.getInstance(connectionHandler.getProject());
        CodeStyleCaseOption caseOption = codeStyleSettings.getCaseSettings().getDatatypeCaseOption();

        List<DataTypeDefinition> nativeDataTypes = connectionHandler.getInterfaceProvider().getNativeDataTypes().list();
        List<String> nativeDataTypeNames = new ArrayList<String>();
        for (DataTypeDefinition nativeDataType : nativeDataTypes) {
            String typeName = nativeDataType.getName();
            typeName = caseOption.changeCase(typeName);
            nativeDataTypeNames.add(typeName);
        }
        createValuesListPopup(nativeDataTypeNames, false);
    }



    public String getDataTypeRepresentation() {
        return getTextField().getText();
    }
}
