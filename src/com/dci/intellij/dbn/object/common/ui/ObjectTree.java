package com.dci.intellij.dbn.object.common.ui;

import com.dci.intellij.dbn.common.ui.tree.DBNTree;

import javax.swing.tree.TreeModel;

public class ObjectTree extends DBNTree {

    public ObjectTree() {
        super(new ObjectTreeModel(null, null, null));
        setCellRenderer(new ObjectTreeCellRenderer());
        new ObjectTreeSpeedSearch(this);
    }

    @Override
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
    }

    @Override
    public ObjectTreeModel getModel() {
        return (ObjectTreeModel) super.getModel();
    }

}
