package com.dci.intellij.dbn.object.dependency.ui;

import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;

public class ObjectDependencyTree extends JTree{

    public ObjectDependencyTree(Project project, DBSchemaObject schemaObject) {
        ObjectDependencyTreeModel model = new ObjectDependencyTreeModel(project, schemaObject, ObjectDependencyType.OUTGOING);
        setModel(model);
        setCellRenderer(new CellRenderer());
        addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                revalidate();
                repaint();
            }
        });
    }

    public void setDependencyType(ObjectDependencyType dependencyType) {
        ObjectDependencyTreeModel oldModel = (ObjectDependencyTreeModel) getModel();
        DBSchemaObject object = oldModel.getObject();
        Project project = oldModel.getProject();
        if (object != null && project != null && !project.isDisposed()) {
            ObjectDependencyTreeModel model = new ObjectDependencyTreeModel(project, object, dependencyType);
            setModel(model);
            Disposer.dispose(oldModel);
        }
    }

    public void setRootObject(DBSchemaObject object) {
        ObjectDependencyTreeModel oldModel = (ObjectDependencyTreeModel) getModel();
        ObjectDependencyType dependencyType = oldModel.getDependencyType();
        Project project = oldModel.getProject();
        if (project != null && !project.isDisposed()) {
            ObjectDependencyTreeModel model = new ObjectDependencyTreeModel(project, object, dependencyType);
            setModel(model);
            Disposer.dispose(oldModel);
        }
    }

    private class CellRenderer extends ColoredTreeCellRenderer {

        @Override
        public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            ObjectDependencyTreeNode node = (ObjectDependencyTreeNode) value;
            DBObject object = node.getObject();

            if (object != null) {
                ObjectDependencyTreeNode selectedNode = (ObjectDependencyTreeNode) tree.getLastSelectedPathComponent();
                boolean highlight = selectedNode != null && selectedNode != node && CommonUtil.safeEqual(object, selectedNode.getObject());

                SimpleTextAttributes regularAttributes = highlight ?
                        SimpleTextAttributes.REGULAR_ATTRIBUTES.derive(SimpleTextAttributes.STYLE_PLAIN, null, new JBColor(0xCCCCFF, 0x155221), null) :
                        SimpleTextAttributes.REGULAR_ATTRIBUTES;
                SimpleTextAttributes grayAttributes = highlight ?
                        SimpleTextAttributes.GRAY_ATTRIBUTES.derive(SimpleTextAttributes.STYLE_PLAIN, null, new JBColor(0xCCCCFF, 0x155221), null) :
                        SimpleTextAttributes.GRAY_ATTRIBUTES;

                setOpaque(true);
                setIcon(object.getIcon());
                setBackground(regularAttributes.getBgColor());
                if (highlight) setBorder(new LineBorder(JBColor.red)); else setBorder(null);
                ObjectDependencyTreeNode rootNode = (ObjectDependencyTreeNode) node.getModel().getRoot();
                DBObject rootObject = rootNode.getObject();
                if (rootObject == null || !CommonUtil.safeEqual(rootObject.getSchema(), object.getSchema())) {
                    append(object.getSchema().getName() + ".", grayAttributes);
                }

                append(object.getName(), regularAttributes);
            } else {
                append("Loading...", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
            }
        }
    }
}
