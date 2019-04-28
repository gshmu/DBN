package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.browser.ui.ToolTipProvider;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.List;

public interface BrowserTreeNode extends TreeNode, NavigationItem, ItemPresentation, ToolTipProvider, GenericDatabaseElement {

    enum LoadStatus {
        NEW,
        LOADING,
        LOADED
    }

    void initTreeElement();

    boolean canExpand();

    int getTreeDepth();

    boolean isTreeStructureLoaded();

    List<? extends BrowserTreeNode> getChildren();

    void refreshTreeChildren(@NotNull DBObjectType... objectTypes);

    void rebuildTreeChildren();

    Icon getIcon(int flags);

    @Override
    String getPresentableText();

    String getPresentableTextDetails();

    String getPresentableTextConditionalDetails();

    @Override
    BrowserTreeNode getChildAt(int index);

    @Override
    @Nullable
    BrowserTreeNode getParent();

    int getIndex(BrowserTreeNode child);
}
