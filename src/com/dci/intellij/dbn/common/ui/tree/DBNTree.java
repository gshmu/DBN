package com.dci.intellij.dbn.common.ui.tree;

import com.dci.intellij.dbn.common.dispose.DisposeUtil;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public class DBNTree extends Tree implements DBNComponent {
    public static final DefaultTreeCellRenderer DEFAULT_CELL_RENDERER = new DefaultTreeCellRenderer();

    private final WeakRef<DBNComponent> parentComponent;

    public DBNTree(@NotNull DBNComponent parent) {
        parentComponent = WeakRef.of(parent);
        setTransferHandler(new DBNTreeTransferHandler());

        Disposer.register(parent, this);
    }

    public DBNTree(@NotNull DBNComponent parent, TreeModel treemodel) {
        super(treemodel);
        parentComponent = WeakRef.of(parent);
        setTransferHandler(new DBNTreeTransferHandler());
        setFont(UIUtil.getLabelFont());

        Disposer.register(parent, this);
    }

    public DBNTree(@NotNull DBNComponent parent, TreeNode root) {
        super(root);
        parentComponent = WeakRef.of(parent);
        setTransferHandler(new DBNTreeTransferHandler());

        Disposer.register(parent, this);
    }

    @NotNull
    public final Project getProject() {
        return parentComponent.ensure().getProject();
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return this;
    }

    @NotNull
    @Override
    public <T extends DBNComponent> T getParentComponent() {
        return (T) parentComponent.ensure();
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
            DisposeUtil.dispose(getModel());
            setSelectionModel(null);
            disposeInner();

            nullify();
        }
    }

    public void disposeInner(){};
}
