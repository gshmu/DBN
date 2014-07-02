package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.Icon;
import javax.swing.JTree;

public class MessagesTreeCellRenderer extends ColoredTreeCellRenderer {
    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof StatementExecutionMessagesNode) {
            BundleTreeNode node = (BundleTreeNode) value;
            append("Statement Execution Messages", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            append(" (" + node.getChildCount() + " files)", SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
        else if (value instanceof CompilerMessagesNode) {
            BundleTreeNode node = (BundleTreeNode) value;
            append("Compiler Messages", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            append(" (" + node.getChildCount() + " objects)", SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
        else if (value instanceof StatementExecutionMessagesFileNode){
            StatementExecutionMessagesFileNode node = (StatementExecutionMessagesFileNode) value;
            VirtualFile virtualFile = node.getVirtualFile();

            setIcon(VirtualFileUtil.getIcon(virtualFile));
            append(virtualFile.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            append(" (" + virtualFile.getPath() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
        else if (value instanceof CompilerMessagesObjectNode){
            CompilerMessagesObjectNode compilerMessagesObjectNode = (CompilerMessagesObjectNode) value;
            DBSchemaObject object = compilerMessagesObjectNode.getObject();

            if (object != null) {
                setIcon(object.getOriginalIcon());
                append(object.getQualifiedName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                append(" - " + object.getConnectionHandler().getPresentableText(), SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        }
        else if (value instanceof CompilerMessageNode) {
            CompilerMessageNode node = (CompilerMessageNode) value;
            CompilerMessage message = node.getCompilerMessage();
            append(message.getText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

            MessageType messageType = message.getType();
            Icon icon =
                    messageType == MessageType.ERROR ? Icons.EXEC_MESSAGES_ERROR :
                    messageType == MessageType.WARNING ? Icons.EXEC_MESSAGES_WARNING :
                    messageType == MessageType.INFO ? Icons.EXEC_MESSAGES_INFO : null;
            setIcon(icon);
        }
        else if (value instanceof StatementExecutionMessageNode) {
            StatementExecutionMessageNode execMessageNode = (StatementExecutionMessageNode) value;
            StatementExecutionMessage message = execMessageNode.getExecutionMessage();
            boolean isOrphan = message.isOrphan();

            MessageType messageType = message.getType();
            Icon icon =
                    messageType == MessageType.ERROR ? (isOrphan ? Icons.EXEC_MESSAGES_WARNING : Icons.EXEC_MESSAGES_ERROR) :
                    messageType == MessageType.WARNING ? Icons.EXEC_MESSAGES_WARNING :
                    messageType == MessageType.INFO ? Icons.EXEC_MESSAGES_INFO : null;

            setIcon(icon);

            append(message.getText(), isOrphan ?
                    SimpleTextAttributes.GRAY_ATTRIBUTES :
                    SimpleTextAttributes.REGULAR_ATTRIBUTES);

            if (message.getCauseMessage() != null) {
                append(" " + message.getCauseMessage(), isOrphan ?
                        SimpleTextAttributes.GRAY_ATTRIBUTES :
                        SimpleTextAttributes.ERROR_ATTRIBUTES);
            }

            append(" - Connection: " + message.getExecutionResult().getConnectionHandler().getName() + ": " + message.getExecutionResult().getExecutionDuration() + "ms", isOrphan ?
                    SimpleTextAttributes.GRAY_ATTRIBUTES :
                    SimpleTextAttributes.GRAY_ATTRIBUTES);

        }
    }

}