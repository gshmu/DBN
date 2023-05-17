package com.dci.intellij.dbn.connection.config.tns.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.config.tns.TnsName;
import com.dci.intellij.dbn.connection.config.tns.TnsNamesBundle;
import com.dci.intellij.dbn.connection.config.tns.TnsNamesParser;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBScrollPane;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.ui.util.TextFields.onTextChange;

public class TnsNamesImportForm extends DBNFormBase {
    private TextFieldWithBrowseButton tnsNamesFileTextField;
    private JBScrollPane tnsNamesScrollPanel;
    private JPanel mainPanel;
    private JLabel errorLabel;

    private final TnsNamesTable tnsNamesTable;

    @Getter
    private TnsNamesBundle tnsNames;

    TnsNamesImportForm(@NotNull TnsNamesImportDialog parent, @Nullable File file) {
        super(parent);
        tnsNamesTable = new TnsNamesTable(this, Collections.emptyList());
        tnsNamesScrollPanel.setViewportView(tnsNamesTable);
        tnsNamesScrollPanel.getViewport().setBackground(Colors.getTableBackground());
        errorLabel.setIcon(Icons.COMMON_ERROR);
        errorLabel.setVisible(false);

        if (file != null) {
            tnsNamesFileTextField.setText(file.getPath());
            updateTnsNamesTable();
        }
        updateSelections();

        tnsNamesTable.getSelectionModel().addListSelectionListener(e -> updateSelections());

        tnsNamesFileTextField.addBrowseFolderListener(
                null,
                null,
                getProject(),
                TnsNamesParser.FILE_CHOOSER_DESCRIPTOR);

        onTextChange(tnsNamesFileTextField, e -> updateTnsNamesTable());
    }

    public TnsNamesImportDialog getParentDialog() {
        return ensureParentComponent();
    }

    private void updateSelections() {
        int rowCount = tnsNamesTable.getRowCount();
        int selectedRowCount = tnsNamesTable.getSelectedRowCount();

        TnsNamesImportDialog parentComponent = ensureParentComponent();
        parentComponent.getImportSelectedAction().setEnabled(selectedRowCount > 0);
        parentComponent.getImportAllAction().setEnabled(rowCount > 0);

        List<TnsName> profiles = tnsNamesTable.getModel().getTnsNames();
        for (int i = 0; i < rowCount; i++) {
            boolean selected = tnsNamesTable.isRowSelected(i);
            TnsName profile = profiles.get(tnsNamesTable.convertRowIndexToModel(i));
            profile.setSelected(selected);
        }
    }

    private void updateTnsNamesTable() {
        try {
            String fileName = tnsNamesFileTextField.getTextField().getText();
            if (Strings.isNotEmpty(fileName)) {
                tnsNames = TnsNamesParser.get(new File(fileName));
                tnsNamesTable.setModel(new TnsNamesTableModel(new ArrayList<>(tnsNames.getProfiles())));
                tnsNamesTable.accommodateColumnsSize();
            }
            errorLabel.setVisible(false);
        } catch (Exception ex) {
            tnsNamesTable.setModel(new TnsNamesTableModel(Collections.emptyList()));
            tnsNamesTable.accommodateColumnsSize();

            errorLabel.setVisible(true);
            String message = ex.getMessage();
            message = Strings.isEmpty(message) ? "File may be corrupt or not a valid tnsnames.ora file." : message;
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
            int rowIndex = tnsNamesTable.convertRowIndexToModel(selectedRow);
            selectedTnsNames.add(tnsNames.get(rowIndex));
        }
        return selectedTnsNames;
    }
}
