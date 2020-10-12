package com.dci.intellij.dbn.connection.config.tns.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.config.tns.TnsName;
import com.dci.intellij.dbn.connection.config.tns.TnsNamesParser;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TnsNamesImportForm extends DBNFormImpl{
    private TextFieldWithBrowseButton tnsNamesFileTextField;
    private JBScrollPane tnsNamesScrollPanel;
    private JPanel mainPanel;
    private JLabel errorLabel;

    private final TnsNamesTable tnsNamesTable;

    TnsNamesImportForm(@NotNull TnsNamesImportDialog parent, @Nullable File file) {
        super(parent);
        tnsNamesTable = new TnsNamesTable(this, Collections.emptyList());
        tnsNamesScrollPanel.setViewportView(tnsNamesTable);
        tnsNamesScrollPanel.getViewport().setBackground(tnsNamesTable.getBackground());
        errorLabel.setIcon(Icons.COMMON_ERROR);
        errorLabel.setVisible(false);

        if (file != null) {
            tnsNamesFileTextField.setText(file.getPath());
            updateTnsNamesTable();
        }
        updateButtons();

        tnsNamesTable.getSelectionModel().addListSelectionListener(e -> updateButtons());

        tnsNamesFileTextField.addBrowseFolderListener(
                null,
                null,
                getProject(),
                TnsNamesParser.FILE_CHOOSER_DESCRIPTOR);

        tnsNamesFileTextField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                updateTnsNamesTable();
            }
        });
    }

    public TnsNamesImportDialog getParentDialog() {
        return ensureParentComponent();
    }

    private void updateButtons() {
        TnsNamesImportDialog parentComponent = ensureParentComponent();
        parentComponent.getImportSelectedAction().setEnabled(tnsNamesTable.getSelectedRowCount() > 0);
        parentComponent.getImportAllAction().setEnabled(tnsNamesTable.getRowCount() > 0);
    }

    private void updateTnsNamesTable() {
        try {
            String fileName = tnsNamesFileTextField.getTextField().getText();
            if (StringUtil.isNotEmpty(fileName)) {
                List<TnsName> tnsNames = TnsNamesParser.parse(new File(fileName));
                tnsNamesTable.setModel(new TnsNamesTableModel(tnsNames));
                tnsNamesTable.accommodateColumnsSize();
            }
            errorLabel.setVisible(false);
        } catch (Exception ex) {
            tnsNamesTable.setModel(new TnsNamesTableModel(Collections.emptyList()));
            tnsNamesTable.accommodateColumnsSize();

            errorLabel.setVisible(true);
            String message = ex.getMessage();
            message = StringUtil.isEmpty(message) ? "File may be corrupt or not a valid tnsnames.ora file." : message;
            errorLabel.setText("Error reading file: " + message);
        }
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    List<TnsName> getAllTnsNames() {
        return tnsNamesTable.getModel().getTnsNames();
    }

    List<TnsName> getSelectedTnsNames() {
        List<TnsName> selectedTnsNames = new ArrayList<>();
        List<TnsName> tnsNames = tnsNamesTable.getModel().getTnsNames();
        int[] selectedRows = tnsNamesTable.getSelectedRows();
        for (int selectedRow : selectedRows) {
            selectedTnsNames.add(tnsNames.get(selectedRow));
        }
        return selectedTnsNames;
    }
}
