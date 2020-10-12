package com.dci.intellij.dbn.debugger.common.process.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.dci.intellij.dbn.debugger.common.config.ui.CompileDebugDependenciesDialog;
import com.dci.intellij.dbn.debugger.common.config.ui.ObjectListCellRenderer;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompileDebugDependenciesForm extends DBNFormImpl {
    private JList<DBSchemaObject> objectList;
    private JPanel mainPanel;
    private JCheckBox rememberSelectionCheckBox;
    private JPanel headerPanel;
    private JPanel hintPanel;

    public CompileDebugDependenciesForm(CompileDebugDependenciesDialog parent, DBRunConfig<?> runConfiguration, List<DBSchemaObject> compileList) {
        super(parent);
        String hintText = "The program you are trying to debug or some of its dependencies are not compiled with debug information." +
                "This may result in breakpoints being ignored during the debug execution, as well as missing information about execution stacks and variables.\n" +
                "In order to achieve full debugging support you are advised to compile the respective programs in debug mode.";

        DBNHintForm hintForm = new DBNHintForm(this, hintText, null, true);
        hintPanel.add(hintForm.getComponent());

        objectList.setCellRenderer(new ObjectListCellRenderer());
        DefaultListModel<DBSchemaObject> model = new DefaultListModel<>();

        Collections.sort(compileList);
        for (DBSchemaObject schemaObject : compileList) {
            model.addElement(schemaObject);
        }
        objectList.setModel(model);

        List<DBMethod> methods = runConfiguration.getMethods();

        List<DBSchemaObject> selectedObjects = new ArrayList<>();
        for (DBMethod method : methods) {
            DBProgram<?, ?> program = method.getProgram();
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
                new DBNHeaderForm(this, (DBObject) source) :
                new DBNHeaderForm(this, CommonUtil.nvl(source, Presentable.UNKNOWN));
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        parent.registerRememberSelectionCheckBox(rememberSelectionCheckBox);
    }

    private int[] computeSelection(List<DBSchemaObject> compileList, List<DBSchemaObject> selectedObjects) {
        List<Integer> selectedIndices = new ArrayList<>();
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

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public List<DBObjectRef<?>> getSelection() {
        List<DBSchemaObject> selectedValuesList = objectList.getSelectedValuesList();
        return CollectionUtil.map(selectedValuesList, o -> o.getRef());
    }

    public void selectAll() {
        objectList.setSelectionInterval(0, objectList.getModel().getSize() -1);
    }

    public void selectNone() {
        objectList.clearSelection();
    }

}
