package com.dci.intellij.dbn.common.ui.tree;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;

public class DBNTree extends Tree implements Disposable {
    public DBNTree() {
        setTransferHandler(new DBNTreeTransferHandler());
    }

    public DBNTree(TreeModel treemodel) {
        super(treemodel);
        setTransferHandler(new DBNTreeTransferHandler());
        setFont(UIUtil.getLabelFont());
    }

    public DBNTree(TreeNode root) {
        super(root);
        setTransferHandler(new DBNTreeTransferHandler());
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    public void dispose() {
        if (!disposed) {
            disposed = true;
            setModel(null);
            setSelectionModel(null);
            GUIUtil.removeListeners(this);
            getUI().uninstallUI(this);

        }
    }


    @Override
    public boolean isDisposed() {
        return disposed;
    }}
