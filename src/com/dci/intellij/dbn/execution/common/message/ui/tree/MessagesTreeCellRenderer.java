package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MessagesTreeCellRenderer extends ColoredTreeCellRenderer {
    public static final JBColor HIGHLIGHT_BACKGROUND = new JBColor(0xE0EFFF, 0x364135);
    public static final SimpleTextAttributes HIGHLIGHT_REGULAR_ATTRIBUTES = SimpleTextAttributes.REGULAR_ATTRIBUTES.derive(SimpleTextAttributes.STYLE_PLAIN, null, HIGHLIGHT_BACKGROUND, null);
    public static final SimpleTextAttributes HIGHLIGHT_GRAY_ATTRIBUTES = SimpleTextAttributes.GRAY_ATTRIBUTES.derive(SimpleTextAttributes.STYLE_PLAIN, null, HIGHLIGHT_BACKGROUND, null);
    public static final SimpleTextAttributes HIGHLIGHT_ERROR_ATTRIBUTES = SimpleTextAttributes.ERROR_ATTRIBUTES.derive(SimpleTextAttributes.STYLE_PLAIN, null, HIGHLIGHT_BACKGROUND, null);

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Failsafe.lenient(() -> {
            if (value instanceof Disposable) {
                Disposable disposable = (Disposable) value;
                if (disposable.isDisposed()) return;;
            }
            Icon icon = null;
            Color background = null;
            if (value instanceof StatementExecutionMessagesNode) {
                BundleTreeNode node = (BundleTreeNode) value;
                append("Statement Execution Messages", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                append(" (" + node.getChildCount() + " files)", SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
            else if (value instanceof ExplainPlanMessagesNode) {
                BundleTreeNode node = (BundleTreeNode) value;
                append("Explain Plan Messages", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
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

                icon = VirtualFileUtil.getIcon(virtualFile);
                append(virtualFile.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                append(" (" + virtualFile.getPath() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
            else if (value instanceof ExplainPlanMessagesFileNode) {
                ExplainPlanMessagesFileNode node = (ExplainPlanMessagesFileNode) value;
                VirtualFile virtualFile = node.getVirtualFile();

                icon = VirtualFileUtil.getIcon(virtualFile);
                append(virtualFile.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                append(" (" + virtualFile.getPath() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);

            }
            else if (value instanceof CompilerMessagesObjectNode){
                CompilerMessagesObjectNode compilerMessagesObjectNode = (CompilerMessagesObjectNode) value;
                DBSchemaObject object = compilerMessagesObjectNode.getObject();

                ConnectionHandler connectionHandler;
                if (object == null) {
                    DBObjectRef<DBSchemaObject> objectRef = compilerMessagesObjectNode.getObjectRef();
                    icon = objectRef.getObjectType().getIcon();
                    append(objectRef.getPath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    connectionHandler = objectRef.lookupConnectionHandler();
                } else {
                    icon = object.getIcon();
                    append(object.getQualifiedName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    connectionHandler = object.getConnectionHandler();
                }

                if (connectionHandler != null) {
                    append(" - " + connectionHandler.getPresentableText(), SimpleTextAttributes.GRAY_ATTRIBUTES);
                }
            }
            else if (value instanceof CompilerMessageNode) {
                CompilerMessageNode node = (CompilerMessageNode) value;
                CompilerMessage message = node.getCompilerMessage();
                boolean highlight = message.isNew() && !selected;
                SimpleTextAttributes regularAttributes = getRegularAttributes(highlight);
                SimpleTextAttributes secondaryTextAttributes = getGrayAttributes(highlight);

                append(message.getText(), regularAttributes);

                MessageType messageType = message.getType();
                icon =
                        messageType == MessageType.ERROR ? Icons.EXEC_MESSAGES_ERROR :
                                messageType == MessageType.WARNING ? Icons.EXEC_MESSAGES_WARNING_INACTIVE :
                                        messageType == MessageType.INFO ? Icons.EXEC_MESSAGES_INFO : null;

                int line = message.getLine();
                int position = message.getPosition();
                if (line > 0 && position > 0) {
                    append(" (line " + line + " / position " + position + ")", secondaryTextAttributes);
                }
                background = regularAttributes.getBgColor();
            }
            else if (value instanceof StatementExecutionMessageNode) {
                StatementExecutionMessageNode execMessageNode = (StatementExecutionMessageNode) value;
                StatementExecutionMessage message = execMessageNode.getExecutionMessage();
                boolean isOrphan = message.isOrphan();
                boolean highlight = message.isNew() && !selected;
                SimpleTextAttributes regularAttributes = getRegularAttributes(highlight);
                SimpleTextAttributes greyAttributes = getGrayAttributes(highlight);
                SimpleTextAttributes errorAttributes = getErrorAttributes(highlight);


                MessageType messageType = message.getType();
                icon =
                        messageType == MessageType.ERROR ? (isOrphan ? Icons.EXEC_MESSAGES_ERROR_INACTIVE : Icons.EXEC_MESSAGES_ERROR) :
                                messageType == MessageType.WARNING ? (isOrphan ? Icons.EXEC_MESSAGES_WARNING_INACTIVE : Icons.EXEC_MESSAGES_WARNING) :
                                        messageType == MessageType.INFO ? (isOrphan ? Icons.EXEC_MESSAGES_INFO_INACTIVE : Icons.EXEC_MESSAGES_INFO) : null;

                append(message.getText(), isOrphan ?
                        greyAttributes :
                        regularAttributes);

                if (message.getCauseMessage() != null) {
                    append(" " + message.getCauseMessage(), isOrphan ?
                            greyAttributes :
                            errorAttributes);
                }

                ConnectionHandler connectionHandler = message.getExecutionResult().getConnectionHandler();
                append(" - Connection: " + connectionHandler.getName() + ": " + message.getExecutionResult().getExecutionDuration() + "ms", greyAttributes);
                background = regularAttributes.getBgColor();
            }
            else if (value instanceof ExplainPlanMessageNode) {
                ExplainPlanMessageNode explainPlanMessageNode = (ExplainPlanMessageNode) value;
                ExplainPlanMessage message = explainPlanMessageNode.getExplainPlanMessage();

                boolean highlight = message.isNew() && !selected;
                SimpleTextAttributes regularAttributes = getRegularAttributes(highlight);
                SimpleTextAttributes greyAttributes = getGrayAttributes(highlight);


                MessageType messageType = message.getType();
                icon =
                        messageType == MessageType.ERROR ? Icons.EXEC_MESSAGES_ERROR :
                                messageType == MessageType.WARNING ? Icons.EXEC_MESSAGES_WARNING_INACTIVE :
                                        messageType == MessageType.INFO ? Icons.EXEC_MESSAGES_INFO : null;

                append(message.getText(), regularAttributes);
                ConnectionHandler connectionHandler = message.getConnectionHandler();
                if (connectionHandler != null) {
                    append(" - Connection: " + connectionHandler.getName(), greyAttributes);
                }
                background = regularAttributes.getBgColor();
            }

            setIcon(icon);
            setBackground(selected ? (isFocused() ?
                    UIUtil.getTreeSelectionBackground() :
                    UIUtil.getTreeUnfocusedSelectionBackground()) :
                    CommonUtil.nvl(background, tree.getBackground()));
        });
    }

    private static SimpleTextAttributes getErrorAttributes(boolean highlight) {
        return highlight ? HIGHLIGHT_ERROR_ATTRIBUTES : SimpleTextAttributes.ERROR_ATTRIBUTES;
    }

    private static SimpleTextAttributes getGrayAttributes(boolean highlight) {
        return highlight ? HIGHLIGHT_GRAY_ATTRIBUTES : SimpleTextAttributes.GRAY_ATTRIBUTES;
    }

    private static SimpleTextAttributes getRegularAttributes(boolean highlight) {
        return highlight ? HIGHLIGHT_REGULAR_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES;
    }

    @Override
    protected boolean shouldDrawBackground() {
        return true;
    }
}