package com.dci.intellij.dbn.common.ui.tree;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public class DBNTree extends Tree implements Disposable {

    public static final DefaultTreeCellRenderer DEFAULT_CELL_RENDERER = new DefaultTreeCellRenderer();

    public DBNTree() {
        setTransferHandler(new DBNTreeTransferHandler());
    }

    public DBNTree(TreeModel treemodel) {
        super(treemodel);
        setTransferHandler(new DBNTreeTransferHandler());
        setFont(UIUtil.getLabelFont());

        DisposerUtil.register(this, treemodel);
    }

    public DBNTree(TreeNode root) {
        super(root);
        setTransferHandler(new DBNTreeTransferHandler());
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            GUIUtil.removeListeners(this);
            //setModel(null);
            getUI().uninstallUI(this);
            setSelectionModel(null);

        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }}
