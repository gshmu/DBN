package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.FileStatus;

import javax.swing.Icon;
import java.util.List;

public class LoadInProgressTreeNode implements BrowserTreeNode {
    private BrowserTreeNode parent;
    private int iconIndex;
    private boolean disposed;

    private static Icon[] icons = new Icon[12];
    static {
        for (int i = 0; i <= 12 - 1; i++) {
            icons[i] = IconLoader.getIcon("/process/step_" + (i + 1) + ".png");
        }
    }

    public LoadInProgressTreeNode(BrowserTreeNode parent) {
        this.parent = parent;
    }

    public boolean isTreeStructureLoaded() {
        return true;
    }

    public void initTreeElement() {}

    public boolean canExpand() {
        return false;
    }

    public int getTreeDepth() {
        return parent.getTreeDepth() + 1;
    }

    public BrowserTreeNode getTreeChild(int index) {
        return null;
    }

    public BrowserTreeNode getTreeParent() {
        return parent;
    }

    public List<? extends BrowserTreeNode> getTreeChildren() {
        return null;
    }

    public void rebuildTreeChildren() {
    }

    public int getTreeChildCount() {
        return 0;
    }

    public boolean isLeafTreeElement() {
        return true;
    }

    public int getIndexOfTreeChild(BrowserTreeNode child) {
        return -1;
    }

    public Icon getIcon(int flags) {
        iconIndex++;
        if (iconIndex == icons.length) iconIndex = 0;
        return icons[iconIndex];
    }

    public String getPresentableText() {
        return "Loading...";
    }

    public String getPresentableTextDetails() {
        return null;
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    public ConnectionHandler getConnectionHandler() {
        return parent.getConnectionHandler();
    }

    public Project getProject() {
        return parent.getProject();
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    public boolean isDisposed() {
        return disposed || parent.isDisposed();
    }

    /*********************************************************
    *                    ItemPresentation                    *
    *********************************************************/
    public String getLocationString() {
        return null;
    }

    public Icon getIcon(boolean open) {
        return null;
    }

    public TextAttributesKey getTextAttributesKey() {
        return SQLTextAttributesKeys.IDENTIFIER;
    }

    /*********************************************************
    *                    NavigationItem                      *
    *********************************************************/
    public void navigate(boolean requestFocus) {}
    public boolean canNavigate() { return false;}
    public boolean canNavigateToSource() {return false;}

    public String getName() {
        return null;
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public FileStatus getFileStatus() {
        return FileStatus.NOT_CHANGED;
    }

    /*********************************************************
    *                    ToolTipProvider                    *
    *********************************************************/
    public String getToolTip() {
        return null;
    }

    public void dispose() {
        disposed = true;
    }
}
