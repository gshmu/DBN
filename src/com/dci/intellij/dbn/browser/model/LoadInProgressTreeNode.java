package com.dci.intellij.dbn.browser.model;

import javax.swing.Icon;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.load.LoadIcon;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;

public class LoadInProgressTreeNode extends DisposableBase implements BrowserTreeNode {
    private BrowserTreeNode parent;
    private List list;

    public LoadInProgressTreeNode(@NotNull BrowserTreeNode parent) {
        this.parent = parent;
    }

    public List asList() {
        if (list == null) {
            synchronized (this) {
                if (list == null) {
                    list = new List();
                    list.add(this);
                }
            }
        }

        return list;
    }

    public boolean isTreeStructureLoaded() {
        return true;
    }

    public void initTreeElement() {}

    public boolean canExpand() {
        return false;
    }

    public int getTreeDepth() {
        return getTreeParent().getTreeDepth() + 1;
    }

    public BrowserTreeNode getTreeChild(int index) {
        return null;
    }

    @NotNull
    public BrowserTreeNode getTreeParent() {
        return FailsafeUtil.get(parent);
    }

    public List getTreeChildren() {
        return null;
    }

    @Override
    public void refreshTreeChildren(@NotNull DBObjectType... objectTypes) {}

    public void rebuildTreeChildren() {}

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
        return LoadIcon.INSTANCE;
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
        return FailsafeUtil.get(getTreeParent().getConnectionHandler());
    }

    @NotNull
    public Project getProject() {
        return getTreeParent().getProject();
    }

    @Nullable
    @Override
    public GenericDatabaseElement getParentElement() {
        return null;
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    @Nullable
    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
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
