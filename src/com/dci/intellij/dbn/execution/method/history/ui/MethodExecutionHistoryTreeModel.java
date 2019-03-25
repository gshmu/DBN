package com.dci.intellij.dbn.execution.method.history.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.List;

import static com.dci.intellij.dbn.connection.ConnectionId.UNKNOWN_CONNECTION;

public abstract class MethodExecutionHistoryTreeModel extends DefaultTreeModel implements Disposable {
    List<MethodExecutionInput> executionInputs;

    MethodExecutionHistoryTreeModel(List<MethodExecutionInput> executionInputs) {
        super(new DefaultMutableTreeNode());
        this.executionInputs = executionInputs;
        setRoot(new RootTreeNode());
    }

    @Override
    public RootTreeNode getRoot() {
        return (RootTreeNode) super.getRoot();
    }

    public abstract List<MethodExecutionInput> getExecutionInputs();

    protected abstract String getMethodName(MethodExecutionInput executionInput);

    public abstract TreePath getTreePath(MethodExecutionInput executionInput);

    /**********************************************************
     *                        TreeNodes                       *
     **********************************************************/
    protected class RootTreeNode extends MethodExecutionHistoryTreeNode {
        RootTreeNode() {
            super(null, MethodExecutionHistoryTreeNode.Type.ROOT, "ROOT");
        }

        ConnectionTreeNode getConnectionNode(MethodExecutionInput executionInput) {
            if (!isLeaf())
                for (TreeNode node : getChildren()) {
                    ConnectionTreeNode connectionNode = (ConnectionTreeNode) node;
                    if (connectionNode.getConnectionHandlerId().equals(executionInput.getMethodRef().getConnectionId())) {
                        return connectionNode;
                    }
                }

            return new ConnectionTreeNode(this, executionInput);
        }
    }

    protected class ConnectionTreeNode extends MethodExecutionHistoryTreeNode {
        ConnectionHandlerRef connectionHandler;
        ConnectionTreeNode(MethodExecutionHistoryTreeNode parent, MethodExecutionInput executionInput) {
            super(parent, MethodExecutionHistoryTreeNode.Type.CONNECTION, null);
            this.connectionHandler = ConnectionHandlerRef.from(executionInput.getConnectionHandler());
        }

        ConnectionHandler getConnectionHandler() {
            return ConnectionHandlerRef.get(connectionHandler);
        }

        public ConnectionId getConnectionHandlerId() {
            ConnectionHandler connectionHandler = getConnectionHandler();
            return connectionHandler == null ? UNKNOWN_CONNECTION : connectionHandler.getConnectionId();
        }

        @Override
        public String getName() {
            ConnectionHandler connectionHandler = getConnectionHandler();
            return connectionHandler == null ? "[unknown]" : connectionHandler.getName();
        }

        @Override
        public Icon getIcon() {
            ConnectionHandler connectionHandler = getConnectionHandler();
            return connectionHandler == null ? Icons.CONNECTION_INVALID : connectionHandler.getIcon();
        }

        SchemaTreeNode getSchemaNode(MethodExecutionInput executionInput) {
            if (!isLeaf())
                for (TreeNode node : getChildren()) {
                    SchemaTreeNode schemaNode = (SchemaTreeNode) node;
                    if (schemaNode.getName().equalsIgnoreCase(executionInput.getMethodRef().getSchemaName())) {
                        return schemaNode;
                    }
                }
            return new SchemaTreeNode(this, executionInput);
        }
    }

    protected class SchemaTreeNode extends MethodExecutionHistoryTreeNode {
        SchemaTreeNode(MethodExecutionHistoryTreeNode parent, MethodExecutionInput executionInput) {
            super(parent, MethodExecutionHistoryTreeNode.Type.SCHEMA, executionInput.getMethodRef().getSchemaName());
        }

        ProgramTreeNode getProgramNode(MethodExecutionInput executionInput) {
            DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();
            DBObjectRef programRef = methodRef.getParentRef(DBObjectType.PROGRAM);
            String programName = programRef.objectName;
            if (!isLeaf())
                for (TreeNode node : getChildren()) {
                    if (node instanceof ProgramTreeNode) {
                        ProgramTreeNode programNode = (ProgramTreeNode) node;
                        if (programNode.getName().equalsIgnoreCase(programName)) {
                            return programNode;
                        }
                    }
                }
            return new ProgramTreeNode(this, executionInput);
        }

        MethodTreeNode getMethodNode(MethodExecutionInput executionInput) {
            if (!isLeaf())
                for (TreeNode node : getChildren()) {
                    if (node instanceof MethodTreeNode) {
                        MethodTreeNode methodNode = (MethodTreeNode) node;
                        if (methodNode.getExecutionInput() == executionInput) {
                            return methodNode;
                        }
                    }
                }
            return new MethodTreeNode(this, executionInput);
        }

    }

    class ProgramTreeNode extends MethodExecutionHistoryTreeNode {
        ProgramTreeNode(MethodExecutionHistoryTreeNode parent, MethodExecutionInput executionInput) {
            super(parent,
                    getNodeType(MethodRefUtil.getProgramObjectType(executionInput.getMethodRef())),
                    MethodRefUtil.getProgramName(executionInput.getMethodRef()));
        }

        MethodTreeNode getMethodNode(MethodExecutionInput executionInput) {
            DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();
            String methodName = methodRef.objectName;
            int overload = methodRef.overload;
            if (!isLeaf())
                for (TreeNode node : getChildren()) {
                    MethodTreeNode methodNode = (MethodTreeNode) node;
                    if (methodNode.getName().equalsIgnoreCase(methodName) && methodNode.getOverload() == overload) {
                        return methodNode;
                    }
                }
            return new MethodTreeNode(this, executionInput);
        }
    }

    protected class MethodTreeNode extends MethodExecutionHistoryTreeNode {
        private MethodExecutionInput executionInput;

        MethodTreeNode(MethodExecutionHistoryTreeNode parent, MethodExecutionInput executionInput) {
            super(parent,
                    getNodeType(executionInput.getMethodRef().objectType),
                    getMethodName(executionInput));
            this.executionInput = executionInput;
        }

        int getOverload() {
            return executionInput.getMethodRef().overload;
        }

        MethodExecutionInput getExecutionInput() {
            return executionInput;
        }

        @Override
        public boolean isValid() {
            return !executionInput.isObsolete() && !executionInput.isInactive();
        }
    }


    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void markDisposed() {
        disposed = true;
    }
}
