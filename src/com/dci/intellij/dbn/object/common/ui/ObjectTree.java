package com.dci.intellij.dbn.object.common.ui;

import javax.swing.tree.TreeModel;

import com.dci.intellij.dbn.common.ui.tree.DBNTree;

public class ObjectTree extends DBNTree {

    public ObjectTree() {
        super(new ObjectTreeModel(null, null, null));
        setCellRenderer(new ObjectTreeCellRenderer());
        new ObjectTreeSpeedSearch(this);
    }

    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
    }

    public ObjectTreeModel getModel() {
        return (ObjectTreeModel) super.getModel();
    }

}
