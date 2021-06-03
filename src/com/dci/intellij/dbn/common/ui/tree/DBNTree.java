package com.dci.intellij.dbn.common.ui.tree;

import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public class DBNTree extends Tree implements DBNComponent {
    public static final DefaultTreeCellRenderer DEFAULT_CELL_RENDERER = new DefaultTreeCellRenderer();

    private final WeakRef<DBNComponent> parent;

    public DBNTree(@NotNull DBNComponent parent) {
        this.parent = WeakRef.of(parent);
        setTransferHandler(DBNTreeTransferHandler.INSTANCE);

        Disposer.register(parent, this);
    }

    public DBNTree(@NotNull DBNComponent parent, TreeModel treeModel) {
        super(treeModel);
        this.parent = WeakRef.of(parent);
        setTransferHandler(DBNTreeTransferHandler.INSTANCE);
        setFont(UIUtil.getLabelFont());

        Disposer.register(parent, this);
        SafeDisposer.register(this, treeModel);
    }

    public DBNTree(@NotNull DBNComponent parent, TreeNode root) {
        super(root);
        this.parent = WeakRef.of(parent);
        setTransferHandler(DBNTreeTransferHandler.INSTANCE);

        Disposer.register(parent, this);
        SafeDisposer.register(this, root);
    }

    @Override
    public void setModel(TreeModel treeModel) {
        TreeModel oldTreeModel = getModel();
        super.setModel(treeModel);

        SafeDisposer.register(this, treeModel);
        SafeDisposer.dispose(oldTreeModel);
    }

    @Nullable
    public final Project getProject() {
        return parent.ensure().getProject();
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return this;
    }

    @NotNull
    @Override
    public <T extends Disposable> T parent() {
        return (T) parent.ensure();
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    @Getter
    private boolean disposed;

    @Override
    public final void dispose() {
        if (!disposed) {
            disposed = true;
            getUI().uninstallUI(this);
            setSelectionModel(null);
            disposeInner();
            nullify();
        }
    }

    public void disposeInner(){};
}
