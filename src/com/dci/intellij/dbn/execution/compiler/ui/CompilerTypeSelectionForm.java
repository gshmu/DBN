package com.dci.intellij.dbn.execution.compiler.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CompilerTypeSelectionForm extends DBNFormImpl<CompilerTypeSelectionDialog> {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JCheckBox rememberSelectionCheckBox;
    private JPanel hintPanel;

    public CompilerTypeSelectionForm(final CompilerTypeSelectionDialog parentComponent, @Nullable DBSchemaObject object) {
        super(parentComponent);
        if (object == null) {
            headerPanel.setVisible(false);
        } else {
            DBNHeaderForm headerForm = new DBNHeaderForm(object, this);
            headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        }
        String hintText =
                "The compile option type \"Debug\" enables you to use the selected object(s) in debugging activities (i.e. pause/trace execution). " +
                        "For runtime performance reasons, it is recommended to use normal compile option, unless you plan to debug the selected element(s)." +
                        "\n\"Keep current\" will carry over the existing compile type.\n\n" +
                        "Please select your compile option.";
        DBNHintForm hintForm = new DBNHintForm(hintText, null, true);
        hintPanel.add(hintForm.getComponent(), BorderLayout.CENTER);

        parentComponent.registerRememberSelectionCheckBox(rememberSelectionCheckBox);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
