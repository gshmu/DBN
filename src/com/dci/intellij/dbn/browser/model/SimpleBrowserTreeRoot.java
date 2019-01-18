package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleBrowserTreeRoot extends BrowserTreeNodeBase implements BrowserTreeNode {
    private List<ConnectionBundle> rootChildren;
    private ProjectRef projectRef;

    public SimpleBrowserTreeRoot(@NotNull Project project, ConnectionBundle connectionBundle) {
        this.projectRef = ProjectRef.from(project);
        this.rootChildren = new ArrayList<ConnectionBundle>();
        if (connectionBundle != null) {
            this.rootChildren.add(connectionBundle);
        }
    }

    @NotNull
    public Project getProject() {
        return projectRef.getnn();
    }

    @Nullable
    @Override
    public GenericDatabaseElement getParentElement() {
        return null;
    }

    /**************************************************
     *              BrowserTreeNode            *
     **************************************************/
    public boolean isTreeStructureLoaded() {
        return true;
    }

    public void initTreeElement() {}

    public boolean canExpand() {
        return true;
    }

    public int getTreeDepth() {
        return 0;
    }

    @Nullable
    public BrowserTreeNode getParent() {
        return null;
    }

    public List<ConnectionBundle> getChildren() {
        return FailsafeUtil.get(rootChildren);
    }

    @Override
    public void refreshTreeChildren(@NotNull DBObjectType... objectTypes) {
        for (ConnectionBundle connectionBundle : getChildren()) {
            connectionBundle.refreshTreeChildren(objectTypes);
        }
    }

    public void rebuildTreeChildren() {
        for (ConnectionBundle connectionBundle : getChildren()) {
            connectionBundle.rebuildTreeChildren();
        }
    }

    public BrowserTreeNode getChildAt(int index) {
        return getChildren().get(index);
    }

    public int getChildCount() {
        return getChildren().size();
    }

    public boolean isLeaf() {
        return false;
    }

    public int getIndex(BrowserTreeNode child) {
        return getChildren().indexOf(child);
    }

    public Icon getIcon(int flags) {
        return Icons.WINDOW_DATABASE_BROWSER;
    }

    public String getPresentableText() {
        return "Connection Managers";
    }

    public String getPresentableTextDetails() {
        return null;
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    /**************************************************
     *              GenericDatabaseElement            *
     **************************************************/
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return null;
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

   /*********************************************************
    *                    ToolTipProvider                    *
    *********************************************************/
    public String getToolTip() {
        return null;
    }

    /*********************************************************
     *                  NavigationItem                       *
     *********************************************************/
    public String getName() {
        return getPresentableText();
    }
    
    public void navigate(boolean b) {}

    public boolean canNavigate() {
        return false;
    }

    public boolean canNavigateToSource() {
        return false;
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public FileStatus getFileStatus() {
        return FileStatus.NOT_CHANGED;
    }

    /*********************************************************
     *                 ItemPresentation                      *
     *********************************************************/
    public String getLocationString() {
        return null;
    }

    public Icon getIcon(boolean open) {
        return getIcon(0);
    }

    public TextAttributesKey getTextAttributesKey() {
        return SQLTextAttributesKeys.IDENTIFIER;
    }

    /*********************************************************
     *                       Disposable                      *
     *********************************************************/
    public void dispose() {
        super.dispose();
        if (rootChildren != null) {
            rootChildren.clear();
            rootChildren = null;
        }
    }
}
