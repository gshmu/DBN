package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.load.LoadInProgressIcon;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
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

@Nullifiable
public class LoadInProgressTreeNode extends BrowserTreeNodeBase implements BrowserTreeNode {
    private BrowserTreeNode parent;

    public LoadInProgressTreeNode(@NotNull BrowserTreeNode parent) {
        this.parent = parent;
    }

    @Override
    public boolean isTreeStructureLoaded() {
        return true;
    }

    @Override
    public void initTreeElement() {}

    @Override
    public boolean canExpand() {
        return false;
    }

    @Override
    public int getTreeDepth() {
        return getParent().getTreeDepth() + 1;
    }

    @Override
    public BrowserTreeNode getChildAt(int index) {
        return null;
    }

    @Override
    @NotNull
    public BrowserTreeNode getParent() {
        return Failsafe.nn(parent);
    }

    @Override
    public List getChildren() {
        return null;
    }

    @Override
    public void refreshTreeChildren(@NotNull DBObjectType... objectTypes) {}

    @Override
    public void rebuildTreeChildren() {}

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int getIndex(BrowserTreeNode child) {
        return -1;
    }

    @Override
    public Icon getIcon(int flags) {
        return LoadInProgressIcon.INSTANCE;
    }
    @Override
    public String getPresentableText() {
        return "Loading...";
    }

    @Override
    public String getPresentableTextDetails() {
        return null;
    }

    @Override
    public String getPresentableTextConditionalDetails() {
        return null;
    }

    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return getParent().getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return Failsafe.nn(getParent().getConnectionHandler());
    }

    @Override
    @NotNull
    public Project getProject() {
        return getParent().getProject();
    }

    @Nullable
    @Override
    public GenericDatabaseElement getParentElement() {
        return null;
    }

    @Override
    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    /*********************************************************
    *                    ItemPresentation                    *
    *********************************************************/
    @Override
    public String getLocationString() {
        return null;
    }

    @Override
    public Icon getIcon(boolean open) {
        return null;
    }

    public TextAttributesKey getTextAttributesKey() {
        return SQLTextAttributesKeys.IDENTIFIER;
    }

    /*********************************************************
    *                    NavigationItem                      *
    *********************************************************/
    @Override
    public void navigate(boolean requestFocus) {}
    @Override
    public boolean canNavigate() { return false;}
    @Override
    public boolean canNavigateToSource() {return false;}

    @NotNull
    @Override
    public String getName() {
        return "";
    }

    @Override
    public ItemPresentation getPresentation() {
        return this;
    }

    public FileStatus getFileStatus() {
        return FileStatus.NOT_CHANGED;
    }

    /*********************************************************
    *                    ToolTipProvider                    *
    *********************************************************/
    @Override
    public String getToolTip() {
        return null;
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
