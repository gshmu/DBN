package com.dci.intellij.dbn.execution.script.ui;

import javax.swing.Action;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ScriptExecutionInputDialog extends DBNDialog<ScriptExecutionInputForm> {
    private ConnectionHandlerRef connectionRef;
    private DBObjectRef<DBSchema> schemaRef;

    public ScriptExecutionInputDialog(Project project, VirtualFile scriptFile, @Nullable ConnectionHandler connectionHandler, @Nullable DBSchema schema) {
        super(project, "Execute Database Script", true);
        setModal(true);
        component = new ScriptExecutionInputForm(this, scriptFile, connectionHandler, schema);
        Action okAction = getOKAction();
        okAction.putValue(Action.NAME, "Execute");
        init();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component == null ? null : component.getPreferredFocusedComponent();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
        };
    }

    public ConnectionHandler getConnection() {
        return connectionRef.get();
    }

    public void setConnection(ConnectionHandler connection) {
        this.connectionRef = connection.getRef();
    }


    @Nullable
    public DBSchema getSchema() {
        return DBObjectRef.get(schemaRef);
    }

    public void setSchema(DBSchema schema) {
        this.schemaRef = DBObjectRef.from(schema);
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
