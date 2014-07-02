package com.dci.intellij.dbn.connection.ui;

import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.DBLanguageFile;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.util.List;

public class SelectCurrentSchemaForm extends DBNFormImpl implements DBNForm {
    private JPanel mainPanel;
    private JLabel fileLabel;
    private JTextArea hintTextArea;
    private JList schemasList;
    private JLabel connectionLabel;

    public SelectCurrentSchemaForm(DBLanguageFile file, ConnectionHandler connectionHandler) {
        VirtualFile virtualFile = file.getVirtualFile();
        fileLabel.setText(virtualFile.getPath());
        fileLabel.setIcon(VirtualFileUtil.getIcon(virtualFile));
        connectionLabel.setText(connectionHandler.getName());
        connectionLabel.setIcon(connectionHandler.getIcon());
        schemasList.setCellRenderer(new ObjectListCellRenderer());

        Project project = connectionHandler.getProject();
        FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
        DBSchema currentSchema = connectionMappingManager.getCurrentSchema(virtualFile);

        List<DBSchema> schemas = connectionHandler.getObjectBundle().getSchemas();
        DefaultListModel model = new DefaultListModel();

        for (DBSchema schema : schemas) {
            model.addElement(schema);
        }
        schemasList.setModel(model);
        schemasList.setSelectedValue(currentSchema, true);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void setHintMessage(String hint) {
        if (hint == null) {
            hintTextArea.setText(null);
            hintTextArea.setVisible(false);
        } else {
            hintTextArea.setText(hint);
        }
    }

    public DBSchema getSelectedSchema() {
        return (DBSchema) schemasList.getSelectedValue();
    }

    public JComponent getSchemasList() {
        return schemasList;
    }

    public void dispose() {
        super.dispose();
    }
}