package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.load.LoadInProgressIcon;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;

public class LoadInProgressTreeNode extends BrowserTreeNodeBase implements BrowserTreeNode {
    private BrowserTreeNode parent;

    public LoadInProgressTreeNode(@NotNull BrowserTreeNode parent) {
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
        return getParent().getTreeDepth() + 1;
    }

    public BrowserTreeNode getChildAt(int index) {
        return null;
    }

    @NotNull
    public BrowserTreeNode getParent() {
        return FailsafeUtil.get(parent);
    }

    public List getChildren() {
        return null;
    }

    @Override
    public void refreshTreeChildren(@NotNull DBObjectType... objectTypes) {}

    public void rebuildTreeChildren() {}

    public int getChildCount() {
        return 0;
    }

    public boolean isLeaf() {
        return true;
    }

    public int getIndex(BrowserTreeNode child) {
        return -1;
    }

    public Icon getIcon(int flags) {
        return LoadInProgressIcon.INSTANCE;
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

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return FailsafeUtil.get(getParent().getConnectionHandler());
    }

    @NotNull
    public Project getProject() {
        return getParent().getProject();
    }

    @Nullable
    @Override
    public GenericDatabaseElement getParentElement() {
        return null;
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
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
        super.dispose();
        parent = null;
    }

    public class List extends ArrayList<BrowserTreeNode> implements Disposable {
        @Override
        public void dispose() {
            if (size() > 0) {
                BrowserTreeNode browserTreeNode = get(0);
                browserTreeNode.dispose();
                clear();
            }
        }
    }
}
