package com.dci.intellij.dbn.execution.method.ui;

import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.common.ui.ExecutionOptionsForm;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodExecutionInputForm extends DBNFormImpl<DisposableProjectComponent> {
    private JPanel mainPanel;
    private JPanel argumentsPanel;
    private JPanel headerPanel;
    private JLabel noArgumentsLabel;
    private JScrollPane argumentsScrollPane;
    private JLabel debuggerVersionLabel;
    private JPanel versionPanel;
    private JLabel debuggerTypeLabel;
    private JPanel executionOptionsPanel;
    private JPanel argumentsContainerPanel;


    private List<MethodExecutionInputArgumentForm> argumentForms = new ArrayList<>();
    private ExecutionOptionsForm executionOptionsForm;
    private MethodExecutionInput executionInput;
    private Set<ChangeListener> changeListeners = new HashSet<>();

    public MethodExecutionInputForm(DisposableProjectComponent parentComponent, final MethodExecutionInput executionInput, boolean showHeader, @NotNull DBDebuggerType debuggerType) {
        super(parentComponent);
        this.executionInput = executionInput;
        DBObjectRef methodRef = executionInput.getMethodRef();

        final ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
        if (debuggerType.isDebug()) {
            versionPanel.setVisible(true);
            versionPanel.setBorder(Borders.BOTTOM_LINE_BORDER);
            DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(getProject());
            String debuggerVersion = debuggerManager.getDebuggerVersion(connectionHandler);
            debuggerVersionLabel.setText(debuggerVersion);
            debuggerTypeLabel.setText(debuggerType.name());
        } else {
            versionPanel.setVisible(false);
        }

        executionOptionsForm = new ExecutionOptionsForm(this, executionInput, debuggerType);
        executionOptionsPanel.add(executionOptionsForm.getComponent());

        //objectPanel.add(new ObjectDetailsPanel(method).getComponent(), BorderLayout.NORTH);

        if (showHeader) {
            DBNHeaderForm headerForm = new DBNHeaderForm(methodRef, this);
            headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        }
        headerPanel.setVisible(showHeader);

        argumentsPanel.setLayout(new BoxLayout(argumentsPanel, BoxLayout.Y_AXIS));
        int[] metrics = new int[]{0, 0};

        //topSeparator.setVisible(false);
        DBMethod method = executionInput.getMethod();
        List<DBArgument> arguments = method == null ? Collections.emptyList() : new ArrayList<>(method.getArguments());
        noArgumentsLabel.setVisible(arguments.size() == 0);
        for (DBArgument argument: arguments) {
            if (argument.isInput()) {
                metrics = addArgumentPanel(argument, metrics);
                //topSeparator.setVisible(true);
            }
        }
        argumentsContainerPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        for (MethodExecutionInputArgumentForm component : argumentForms) {
            component.adjustMetrics(metrics);
        }

        if (argumentForms.size() > 0) {
            MethodExecutionInputArgumentForm firstArgumentForm = argumentForms.get(0);
            int scrollUnitIncrement = firstArgumentForm.getScrollUnitIncrement();
            Dimension minSize = new Dimension(-1, Math.min(argumentForms.size(), 10) * scrollUnitIncrement + 2);
            argumentsScrollPane.setMinimumSize(minSize);
            argumentsScrollPane.getVerticalScrollBar().setUnitIncrement(scrollUnitIncrement);
        } else {
            argumentsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            Dimension preferredSize = argumentsScrollPane.getViewport().getComponent(0).getPreferredSize();
            preferredSize.setSize(preferredSize.getWidth(), preferredSize.getHeight() + 2);
            argumentsScrollPane.setMinimumSize(preferredSize);
        }


        Dimension preferredSize = mainPanel.getPreferredSize();
        int width = (int) preferredSize.getWidth() + 24;
        int height = (int) Math.min(preferredSize.getHeight(), 380);
        mainPanel.setPreferredSize(new Dimension(width, height));

        for (MethodExecutionInputArgumentForm argumentComponent : argumentForms){
            argumentComponent.addDocumentListener(documentListener);
        }
    }

    public void setExecutionInput(MethodExecutionInput executionInput) {
        this.executionInput = executionInput;
    }

    public MethodExecutionInput getExecutionInput() {
        return executionInput;
    }

    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    private int[] addArgumentPanel(DBArgument argument, int[] gridMetrics) {
        MethodExecutionInputArgumentForm argumentComponent = new MethodExecutionInputArgumentForm(this, argument);
        argumentsPanel.add(argumentComponent.getComponent());
        argumentForms.add(argumentComponent);
        return argumentComponent.getMetrics(gridMetrics);
   }

    public void updateExecutionInput() {
        for (MethodExecutionInputArgumentForm argumentComponent : argumentForms) {
            argumentComponent.updateExecutionInput();
        }
        executionOptionsForm.updateExecutionInput();
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
        executionOptionsForm.addChangeListener(changeListener);
    }

    private DocumentListener documentListener = new DocumentAdapter() {
        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
            notifyChangeListeners();
        }
    };

    private void notifyChangeListeners() {
        if (changeListeners != null) {
            for (ChangeListener changeListener : changeListeners) {
                changeListener.stateChanged(new ChangeEvent(this));
            }
        }
    }

    @Deprecated
    public void touch() {
        executionOptionsForm.touch();
    }


    @Override
    public void dispose() {
        super.dispose();
        DisposerUtil.dispose(argumentForms);
        CollectionUtil.clear(argumentForms);
        CollectionUtil.clear(changeListeners);
        executionInput = null;
    }
}
