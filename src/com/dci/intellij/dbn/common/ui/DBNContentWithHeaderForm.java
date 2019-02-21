package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public abstract class DBNContentWithHeaderForm<T extends DBNDialog> extends DBNFormImpl<T>{
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel contentPanel;

    private DBNHeaderForm headerForm;
    private DBNForm contentForm;

    public DBNContentWithHeaderForm(@NotNull T parentComponent) {
        super(parentComponent);
        headerForm = createHeaderForm();
        contentForm = createContentForm();

        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        contentPanel.add(contentForm.getComponent(), BorderLayout.CENTER);
        Disposer.register(this, contentForm);
    }

    public abstract DBNHeaderForm createHeaderForm();

    public abstract DBNForm createContentForm();

    public DBNHeaderForm getHeaderForm() {
        return headerForm;
    }

    public DBNForm getContentForm() {
        return contentForm;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
        headerForm = null;
        contentForm = null;
    }
}
