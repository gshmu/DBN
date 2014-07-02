package com.dci.intellij.dbn.browser.ui;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ModuleConnectionBundle;
import com.dci.intellij.dbn.connection.ProjectConnectionBundle;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.JList;

public class ModuleListCellRenderer extends ColoredListCellRenderer {

    protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        BrowserTreeNode treeNode = (BrowserTreeNode) value;
        setIcon(treeNode.getIcon(0));

        String displayName;
        if (treeNode instanceof ModuleConnectionBundle) {
            ModuleConnectionBundle connectionManager = (ModuleConnectionBundle) treeNode;
            displayName = connectionManager.getModule().getName();
        } else if (treeNode instanceof ProjectConnectionBundle) {
            displayName = "PROJECT";
        } else {
            displayName = treeNode.getPresentableText();
        }

        append(displayName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
}
