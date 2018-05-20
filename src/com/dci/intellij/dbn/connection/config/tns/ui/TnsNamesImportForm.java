package com.dci.intellij.dbn.connection.config.tns.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.config.tns.TnsName;
import com.dci.intellij.dbn.connection.config.tns.TnsNamesParser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TnsNamesImportForm extends DBNFormImpl<TnsNamesImportDialog>{
    private TextFieldWithBrowseButton tnsNamesFileTextField;
    private JBScrollPane tnsNamesScrollPanel;
    private JPanel mainPanel;
    private JLabel errorLabel;

    private TnsNamesTable tnsNamesTable;

    public TnsNamesImportForm(@NotNull final TnsNamesImportDialog parentComponent, @Nullable File file) {
        super(parentComponent);
        final Project project = parentComponent.getProject();
        tnsNamesTable = new TnsNamesTable(project, new TnsName[0]);
        tnsNamesScrollPanel.setViewportView(tnsNamesTable);
        tnsNamesScrollPanel.getViewport().setBackground(tnsNamesTable.getBackground());
        errorLabel.setIcon(Icons.COMMON_ERROR);
        errorLabel.setVisible(false);

        if (file != null) {
            tnsNamesFileTextField.setText(file.getPath());
            updateTnsNamesTable();
        }
        updateButtons();

        tnsNamesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateButtons();
            }
        });

        tnsNamesFileTextField.addBrowseFolderListener(
                null,
                null,
                project,
                TnsNamesParser.FILE_CHOOSER_DESCRIPTOR);

        tnsNamesFileTextField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                updateTnsNamesTable();
            }
        });
    }

    private void updateButtons() {
        TnsNamesImportDialog parentComponent = getParentComponent();
        parentComponent.getImportSelectedAction().setEnabled(tnsNamesTable.getSelectedRowCount() > 0);
        parentComponent.getImportAllAction().setEnabled(tnsNamesTable.getRowCount() > 0);
    }

    private void updateTnsNamesTable() {
        try {
            String fileName = tnsNamesFileTextField.getTextField().getText();
            if (StringUtil.isNotEmpty(fileName)) {
                TnsName[] tnsNames = TnsNamesParser.parse(new File(fileName));
                tnsNamesTable.setModel(new TnsNamesTableModel(tnsNames));
                tnsNamesTable.accommodateColumnsSize();
            }
            errorLabel.setVisible(false);
        } catch (Exception ex) {
            tnsNamesTable.setModel(new TnsNamesTableModel(new TnsName[0]));
            tnsNamesTable.accommodateColumnsSize();

            errorLabel.setVisible(true);
            String message = ex.getMessage();
            message = StringUtil.isEmpty(message) ? "File may be corrupt or not a valid tnsnames.ora file." : message;
            errorLabel.setText("Error reading file: " + message);
        }
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public List<TnsName> getAllTnsNames() {
        return Arrays.asList(tnsNamesTable.getModel().getTnsNames());
    }

    public List<TnsName> getSelectedTnsNames() {
        List<TnsName> selectedTnsNames = new ArrayList<TnsName>();
        TnsName[] tnsNames = tnsNamesTable.getModel().getTnsNames();
        int[] selectedRows = tnsNamesTable.getSelectedRows();
        for (int selectedRow : selectedRows) {
            selectedTnsNames.add(tnsNames[selectedRow]);
        }
        return selectedTnsNames;
    }
}
