package com.dci.intellij.dbn.execution.method.history.ui;

import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.lookup.DBMethodRef;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

public class MethodExecutionHistoryGroupedTreeModel extends MethodExecutionHistoryTreeModel {
    private List<MethodExecutionInput> executionInputs;
    public MethodExecutionHistoryGroupedTreeModel(List<MethodExecutionInput> executionInputs) {
        super(executionInputs);
        this.executionInputs = executionInputs;
        for (MethodExecutionInput executionInput : executionInputs) {
            RootTreeNode rootNode = getRoot();

            ConnectionTreeNode connectionNode = rootNode.getConnectionNode(executionInput);
            SchemaTreeNode schemaNode = connectionNode.getSchemaNode(executionInput);

            String programName = executionInput.getMethodRef().getProgramName();
            if (programName != null) {
                ProgramTreeNode programNode = schemaNode.getProgramNode(executionInput);
                programNode.getMethodNode(executionInput);
            } else {
                schemaNode.getMethodNode(executionInput);
            }
        }
    }

    @Override
    protected String getMethodName(MethodExecutionInput executionInput) {
        return executionInput.getMethodRef().getMethodName();
    }

    @Override
    public TreePath getTreePath(MethodExecutionInput executionInput) {
        List<MethodExecutionHistoryTreeNode> path = new ArrayList<MethodExecutionHistoryTreeNode>();
        MethodExecutionHistoryTreeModel.RootTreeNode rootTreeNode = getRoot();
        path.add(rootTreeNode);
        ConnectionTreeNode connectionTreeNode = rootTreeNode.getConnectionNode(executionInput);
        path.add(connectionTreeNode);
        SchemaTreeNode schemaTreeNode = connectionTreeNode.getSchemaNode(executionInput);
        path.add(schemaTreeNode);
        if (executionInput.getMethodRef().getProgramName() != null) {
            ProgramTreeNode programTreeNode = schemaTreeNode.getProgramNode(executionInput);
            path.add(programTreeNode);
            MethodTreeNode methodTreeNode = programTreeNode.getMethodNode(executionInput);
            path.add(methodTreeNode);
        } else {
            MethodTreeNode methodTreeNode = schemaTreeNode.getMethodNode(executionInput);
            path.add(methodTreeNode);
        }

        return new TreePath(path.toArray());
    }

    public List<MethodExecutionInput> getExecutionInputs() {
        List<MethodExecutionInput> executionInputs = new ArrayList<MethodExecutionInput>();
        for (TreeNode connectionTreeNode : getRoot().getChildren()) {
            ConnectionTreeNode connectionNode = (ConnectionTreeNode) connectionTreeNode;
            for (TreeNode schemaTreeNode : connectionNode.getChildren()) {
                SchemaTreeNode schemaNode = (SchemaTreeNode) schemaTreeNode;
                for (TreeNode node : schemaNode.getChildren()) {
                    if (node instanceof ProgramTreeNode) {
                        ProgramTreeNode programNode = (ProgramTreeNode) node;
                        for (TreeNode methodTreeNode : programNode.getChildren()) {
                            MethodTreeNode methodNode = (MethodTreeNode) methodTreeNode;
                            MethodExecutionInput executionInput =
                                    getExecutionInput(connectionNode, schemaNode, programNode, methodNode);

                            if (executionInput != null) {
                                executionInputs.add(executionInput);
                            }
                        }

                    } else {
                        MethodTreeNode methodNode = (MethodTreeNode) node;
                        MethodExecutionInput executionInput =
                                getExecutionInput(connectionNode, schemaNode, null, methodNode);

                        if (executionInput != null) {
                            executionInputs.add(executionInput);
                        }
                    }
                }
            }
        }
        return executionInputs;
    }

    private MethodExecutionInput getExecutionInput(
            ConnectionTreeNode connectionNode,
            SchemaTreeNode schemaNode,
            ProgramTreeNode programNode,
            MethodTreeNode methodNode) {
        for (MethodExecutionInput executionInput : executionInputs) {
            DBMethodRef methodIdentifier = executionInput.getMethodRef();
            if (executionInput.getConnectionHandler().getId().equals(connectionNode.getConnectionHandler().getId()) &&
                methodIdentifier.getSchemaName().equalsIgnoreCase(schemaNode.getName()) &&
                methodIdentifier.getMethodName().equalsIgnoreCase(methodNode.getName()) &&
                methodIdentifier.getOverload() == methodNode.getOverload() ) {
                String inputProgramName = methodIdentifier.getProgramName();
                if (programNode == null) {
                    if (inputProgramName == null) {
                        return executionInput;
                    }
                } else {
                    if (programNode.getName().equalsIgnoreCase(inputProgramName)) {
                        return executionInput;
                    }
                }
            }
        }
        return null;
    }



}
