package com.dci.intellij.dbn.browser.model;

import javax.swing.Icon;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.ui.ToolTipProvider;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;

public interface BrowserTreeNode extends NavigationItem, ItemPresentation, ToolTipProvider, GenericDatabaseElement {

    void initTreeElement();

    boolean canExpand();

    int getTreeDepth();

    boolean isTreeStructureLoaded();

    BrowserTreeNode getTreeChild(int index);

    @Nullable
    BrowserTreeNode getTreeParent();

    List<? extends BrowserTreeNode> getTreeChildren();

    void refreshTreeChildren(@Nullable DBObjectType objectType);

    void rebuildTreeChildren();

    int getTreeChildCount();

    boolean isLeafTreeElement();

    int getIndexOfTreeChild(BrowserTreeNode child);

    Icon getIcon(int flags);

    String getPresentableText();

    String getPresentableTextDetails();

    String getPresentableTextConditionalDetails();
}
