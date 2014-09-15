package com.dci.intellij.dbn.browser.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.TreeCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.LoadInProgressTreeNode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ModuleConnectionBundle;
import com.dci.intellij.dbn.connection.ProjectConnectionBundle;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;

public class DatabaseBrowserTreeCellRenderer implements TreeCellRenderer {
    private DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer();
    //private static final LoaderCellRendererComponent LOADER_COMPONENT = new LoaderCellRendererComponent();
    private DatabaseBrowserSettings browserSettings;

    public DatabaseBrowserTreeCellRenderer(Project project) {
        browserSettings = DatabaseBrowserSettings.getInstance(project);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof LoadInProgressTreeNode) {
            return new LoaderCellRendererComponent(LoadInProgressTreeNode.LOOSE_INSTANCE);
        } else {
            return cellRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
    }

    private static class LoaderCellRendererComponent extends JPanel {
        LoadInProgressTreeNode treeElement;
        private LoaderCellRendererComponent(LoadInProgressTreeNode treeElement) {
            setLayout(new BorderLayout());
            add(new JLabel("Loading...", treeElement.getIcon(0), SwingConstants.LEFT), BorderLayout.WEST);
            setBackground(UIUtil.getTreeTextBackground());
        }
    }

    private class DefaultTreeCellRenderer extends ColoredTreeCellRenderer {
        public Font getFont() {
            Font font = super.getFont();
            return font == null ? UIUtil.getTreeFont() : font;
        }

        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            BrowserTreeNode treeNode = (BrowserTreeNode) value;
            setIcon(treeNode.getIcon(0));

            boolean isDirty = false;
            String displayName;
            if (treeNode instanceof ModuleConnectionBundle) {
                ModuleConnectionBundle connectionManager = (ModuleConnectionBundle) treeNode;
                displayName = connectionManager.getModule().getName();
            } else if (treeNode instanceof ProjectConnectionBundle) {
                displayName = "PROJECT";
            } else {
                displayName = treeNode.getPresentableText();
            }

            if (treeNode instanceof DBObjectList) {
                DBObjectList objectsList = (DBObjectList) treeNode;
                boolean isEmpty = objectsList.getTreeChildCount() == 0;
                isDirty = objectsList.isLoading() || (!objectsList.isLoaded() && !hasConnectivity(objectsList));
                SimpleTextAttributes textAttributes =
                        isDirty ? SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES :
                        isEmpty ? SimpleTextAttributes.REGULAR_ATTRIBUTES :
                        SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;

                append(displayName, textAttributes);

                // todo display load error
                /*
                            SimpleTextAttributes descriptionTA = objectsList.getErrorMessage() == null ?
                                    SimpleTextAttributes.GRAY_ATTRIBUTES : SimpleTextAttributes.ERROR_ATTRIBUTES;
                            append(" " + displayDetails, descriptionTA);

                            if (objectsList.getErrorMessage() != null) {
                                String msg = "Could not load " + displayName + ". Cause: " + objectsList.getErrorMessage();
                                setToolTipText(msg);
                            }  else {
                                setToolTipText(null);
                            }
                */
            } else {
                boolean showBold = false;
                boolean showGrey = false;
                boolean isError = false;
                if (treeNode instanceof DBObject) {
                    DBObject object = (DBObject) treeNode;
                    if (object.isOfType(DBObjectType.SCHEMA)) {
                        DBSchema schema = (DBSchema) object;
                        showBold = schema.isUserSchema();
                        showGrey = schema.isEmptySchema();
                    }

                    isError = !object.isValid();
                }

                SimpleTextAttributes textAttributes =
                        isError ? SimpleTextAttributes.ERROR_ATTRIBUTES :
                        showBold ? (showGrey ? SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES : SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES) :
                                (showGrey ? SimpleTextAttributes.GRAYED_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES);

                if (displayName == null) displayName = "displayName null!!";

                append(displayName, textAttributes);
            }
            String displayDetails = treeNode.getPresentableTextDetails();
            if (!StringUtil.isEmptyOrSpaces(displayDetails)) {
                append(" " + displayDetails, isDirty ? SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES : SimpleTextAttributes.GRAY_ATTRIBUTES);
            }


            if (browserSettings.getGeneralSettings().getShowObjectDetails().value()) {
                String conditionalDetails = treeNode.getPresentableTextConditionalDetails();
                if (!StringUtil.isEmptyOrSpaces(conditionalDetails)) {
                    append(" - " + conditionalDetails, SimpleTextAttributes.GRAY_ATTRIBUTES);
                }

            }
        }

        private boolean hasConnectivity(DBObjectList objectsList) {
            ConnectionHandler connectionHandler = objectsList.getConnectionHandler();
            return connectionHandler != null && objectsList.getConnectionHandler().canConnect() && connectionHandler.isValid();
        }
    }
}
