package com.dci.intellij.dbn.object.common.ui;

import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeModel;

public class ObjectTree extends DBNTree {

    public ObjectTree(@NotNull Project project) {
        super(project, new ObjectTreeModel(null, null, null));
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
