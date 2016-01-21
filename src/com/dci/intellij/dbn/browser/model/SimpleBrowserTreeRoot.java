package com.dci.intellij.dbn.browser.model;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;

public class SimpleBrowserTreeRoot implements BrowserTreeNode {
    private List<ConnectionBundle> rootChildren;
    private ProjectRef projectRef;

    public SimpleBrowserTreeRoot(@NotNull Project project, ConnectionBundle connectionBundle) {
        this.projectRef = new ProjectRef(project);
        this.rootChildren = new ArrayList<ConnectionBundle>();
        if (connectionBundle != null) {
            this.rootChildren.add(connectionBundle);
        }
    }

    @NotNull
    public Project getProject() {
        return FailsafeUtil.get(projectRef.get());
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
    public BrowserTreeNode getTreeParent() {
        return null;
    }

    public List<ConnectionBundle> getTreeChildren() {
        return FailsafeUtil.get(rootChildren);
    }

    @Override
    public void refreshTreeChildren(@NotNull DBObjectType... objectTypes) {
        for (ConnectionBundle connectionBundle : getTreeChildren()) {
            connectionBundle.refreshTreeChildren(objectTypes);
        }
    }

    public void rebuildTreeChildren() {
        for (ConnectionBundle connectionBundle : getTreeChildren()) {
            connectionBundle.rebuildTreeChildren();
        }
    }

    public BrowserTreeNode getTreeChild(int index) {
        return getTreeChildren().get(index);
    }

    public int getTreeChildCount() {
        return getTreeChildren().size();
    }

    public boolean isLeafTreeElement() {
        return false;
    }

    public int getIndexOfTreeChild(BrowserTreeNode child) {
        return getTreeChildren().indexOf(child);
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
    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return null;
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    @Nullable
    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    public boolean isDisposed() {
        return false;
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
        if (rootChildren != null) {
            rootChildren.clear();
            rootChildren = null;
        }
    }
}
