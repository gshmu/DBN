package com.dci.intellij.dbn.debugger.common.process.ui;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.dci.intellij.dbn.debugger.common.config.ui.CompileDebugDependenciesDialog;
import com.dci.intellij.dbn.debugger.common.config.ui.ObjectListCellRenderer;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;

public class CompileDebugDependenciesForm extends DBNFormImpl<CompileDebugDependenciesDialog> {
    private JList objectList;
    private JPanel mainPanel;
    private JCheckBox rememberSelectionCheckBox;
    private JPanel headerPanel;
    private JPanel hintPanel;

    public CompileDebugDependenciesForm(CompileDebugDependenciesDialog parentComponent, DBRunConfig runConfiguration, List<DBSchemaObject> compileList) {
        super(parentComponent);
        String hintText = "The program you are trying to debug or some of its dependencies are not compiled with debug information." +
                "This may result in breakpoints being ignored during the debug execution, as well as missing information about execution stacks and variables.\n" +
                "In order to achieve full debugging support you are advised to compile the respective programs in debug mode.";

        DBNHintForm hintForm = new DBNHintForm(hintText, null, true);
        hintPanel.add(hintForm.getComponent());

        objectList.setCellRenderer(new ObjectListCellRenderer());
        DefaultListModel model = new DefaultListModel();

        Collections.sort(compileList);
        for (DBSchemaObject schemaObject : compileList) {
            model.addElement(schemaObject);
        }
        objectList.setModel(model);

        List<DBMethod> methods = runConfiguration.getMethods();

        List<DBSchemaObject> selectedObjects = new ArrayList<DBSchemaObject>();
        for (DBMethod method : methods) {
            DBProgram program = method.getProgram();
            DBSchemaObject selectedObject = program == null ? method : program;
            if (!selectedObjects.contains(selectedObject)) {
                selectedObjects.add(selectedObject);
            }
        }

        int[] selectedIndicesArray = computeSelection(compileList, selectedObjects);

        objectList.setSelectedIndices(selectedIndicesArray);
        if (selectedIndicesArray.length > 0) {
            objectList.ensureIndexIsVisible(selectedIndicesArray.length - 1);
        }

        Presentable source = runConfiguration.getSource();
        DBNHeaderForm headerForm = source instanceof DBObject ?
                new DBNHeaderForm((DBObject) source, this) :
                new DBNHeaderForm(CommonUtil.nvl(source, Presentable.UNKNOWN), this);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        parentComponent.registerRememberSelectionCheckBox(rememberSelectionCheckBox);
    }

    private int[] computeSelection(List<DBSchemaObject> compileList, List<DBSchemaObject> selectedObjects) {
        List<Integer> selectedIndices = new ArrayList<Integer>();
        for (DBSchemaObject selectedObject : selectedObjects) {
            int index = compileList.indexOf(selectedObject);
            if (index > -1) {
                selectedIndices.add(index);
            }
        }


        int[] selectedIndicesArray = new int[selectedIndices.size()];
        for (int i = 0; i < selectedIndices.size(); i++) {
            Integer selectedIndex = selectedIndices.get(i);
            selectedIndicesArray[i] = selectedIndex;
        }
        return selectedIndicesArray;
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public List<DBSchemaObject> getSelection() {
        List<DBSchemaObject> objects = new ArrayList<DBSchemaObject>();
        for (Object o : objectList.getSelectedValues()) {
            objects.add((DBSchemaObject) o);
        }
        return objects;
    }

    public void selectAll() {
        objectList.setSelectionInterval(0, objectList.getModel().getSize() -1);
    }

    public void selectNone() {
        objectList.clearSelection();
    }

    public void dispose() {
        super.dispose();
    }
}
