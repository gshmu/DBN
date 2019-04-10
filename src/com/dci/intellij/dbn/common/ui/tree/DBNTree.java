package com.dci.intellij.dbn.common.ui.tree;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.dispose.RegisteredDisposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

@Nullifiable
public class DBNTree extends Tree implements RegisteredDisposable {
    private ProjectRef projectRef;
    public static final DefaultTreeCellRenderer DEFAULT_CELL_RENDERER = new DefaultTreeCellRenderer();

    public DBNTree(@NotNull Project project) {
        projectRef = ProjectRef.from(project);
        setTransferHandler(new DBNTreeTransferHandler());
    }

    public DBNTree(@NotNull Project project, TreeModel treemodel) {
        super(treemodel);
        projectRef = ProjectRef.from(project);
        setTransferHandler(new DBNTreeTransferHandler());
        setFont(UIUtil.getLabelFont());
    }

    public DBNTree(TreeNode root) {
        super(root);
        setTransferHandler(new DBNTreeTransferHandler());
    }

    @NotNull
    public final Project getProject() {
        return projectRef.nn();
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void markDisposed() {
        disposed = true;
    }

    public void disposeInner(){
        getUI().uninstallUI(this);
        Disposer.dispose(getModel());
        setSelectionModel(null);
        RegisteredDisposable.super.disposeInner();
    }
}
