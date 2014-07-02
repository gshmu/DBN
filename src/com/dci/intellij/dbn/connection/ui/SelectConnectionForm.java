package com.dci.intellij.dbn.connection.ui;

import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ModuleConnectionBundle;
import com.dci.intellij.dbn.connection.ProjectConnectionBundle;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.DBLanguageFile;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.List;

public class SelectConnectionForm extends DBNFormImpl implements DBNForm {
    private JPanel mainPanel;
    private JList connectionsList;
    private JList schemasList;
    private JLabel fileLabel;

    private ConnectionHandler activeConnection;
    private DBSchema currentSchema;

    public SelectConnectionForm(DBLanguageFile file) {
        connectionsList.setCellRenderer(new ConnectionListCellRenderer());
        connectionsList.addListSelectionListener(connectionListSelectionListener);
        schemasList.setCellRenderer(new ObjectListCellRenderer());
        VirtualFile virtualFile = file.getVirtualFile();
        fileLabel.setText(virtualFile.getPath());
        fileLabel.setIcon(VirtualFileUtil.getIcon(virtualFile));

        Project project = file.getProject();
        FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
        activeConnection = connectionMappingManager.getActiveConnection(virtualFile);
        currentSchema = connectionMappingManager.getCurrentSchema(virtualFile);

        DefaultListModel connectionListModel = new DefaultListModel();
        List<ConnectionHandler> connectionHandlers = ProjectConnectionBundle.getInstance(project).getConnectionHandlers();
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            connectionListModel.addElement(connectionHandler);
        }

        Module currentModule = ModuleUtil.findModuleForFile(virtualFile, project);
        if (currentModule != null) {
            connectionHandlers = ModuleConnectionBundle.getInstance(currentModule).getConnectionHandlers();
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                connectionListModel.addElement(connectionHandler);
            }
        }

        connectionsList.setModel(connectionListModel);
        if (activeConnection == null) {
            if (connectionsList.getModel().getSize() > 0 ) {
                connectionsList.setSelectedIndex(0);
            }
        } else {
            connectionsList.setSelectedValue(activeConnection, true);
        }
        populateSchemaList();
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public JList getConnectionsList() {
        return connectionsList;
    }

    private ListSelectionListener connectionListSelectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            populateSchemaList();
        }
    };

    protected void addListSelectionListener(ListSelectionListener listener) {
        connectionsList.addListSelectionListener(listener);
        schemasList.addListSelectionListener(listener);
    }

    private void populateSchemaList() {
        ConnectionHandler selectedConnection = getSelectedConnection();
        if (selectedConnection == null) {
            schemasList.setModel(new DefaultListModel());
        } else {
            List<DBSchema> schemas = selectedConnection.getObjectBundle().getSchemas();
            DefaultListModel model = new DefaultListModel();

            for (DBSchema schema : schemas) {
                model.addElement(schema);
            }
            schemasList.setModel(model);
            DBSchema selectedSchema =
                    activeConnection == selectedConnection ? currentSchema :
                    selectedConnection.getObjectBundle().getUserSchema();
            schemasList.setSelectedValue(selectedSchema, true);
        }
    }

    public ConnectionHandler getSelectedConnection() {
        return (ConnectionHandler) connectionsList.getSelectedValue();
    }

    public DBSchema getSelectedSchema() {
        return (DBSchema) schemasList.getSelectedValue();
    }
    
    public boolean isValidSelection() {
        return connectionsList.getSelectedIndices().length == 1 && schemasList.getSelectedIndices().length == 1;
    }

    public void dispose() {
        super.dispose();
        activeConnection = null;
        currentSchema = null;        
    }
}
