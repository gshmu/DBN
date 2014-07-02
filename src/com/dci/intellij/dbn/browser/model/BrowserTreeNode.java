package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.browser.ui.ToolTipProvider;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;

public interface BrowserTreeNode extends NavigationItem, ItemPresentation, ToolTipProvider, Disposable, GenericDatabaseElement {

    List<BrowserTreeNode> EMPTY_LIST = new ArrayList<BrowserTreeNode>();

    void initTreeElement();

    boolean canExpand();

    int getTreeDepth();

    boolean isTreeStructureLoaded();

    BrowserTreeNode getTreeChild(int index);

    BrowserTreeNode getTreeParent();

    List<? extends BrowserTreeNode> getTreeChildren();

    void rebuildTreeChildren();

    int getTreeChildCount();

    boolean isLeafTreeElement();

    int getIndexOfTreeChild(BrowserTreeNode child);

    Icon getIcon(int flags);

    String getPresentableText();

    String getPresentableTextDetails();

    String getPresentableTextConditionalDetails();
}
