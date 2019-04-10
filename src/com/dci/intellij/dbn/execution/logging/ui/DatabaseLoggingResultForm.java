package com.dci.intellij.dbn.execution.logging.ui;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultFormBase;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.intellij.ide.actions.NextOccurenceToolbarAction;
import com.intellij.ide.actions.PreviousOccurenceToolbarAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DatabaseLoggingResultForm extends ExecutionResultFormBase<DatabaseLoggingResult> {
    private JPanel mainPanel;
    private JPanel consolePanel;
    private JPanel actionsPanel;

    private DatabaseLoggingResultConsole console;

    public DatabaseLoggingResultForm(@NotNull DatabaseLoggingResult loggingResult) {
        super(loggingResult);
        ConnectionHandler connectionHandler = loggingResult.getConnectionHandler();
        console = new DatabaseLoggingResultConsole(connectionHandler, loggingResult.getName(), false);
        consolePanel.add(console.getComponent(), BorderLayout.CENTER);

        ActionManager actionManager = ActionManager.getInstance();
        //ActionGroup actionGroup = (ActionGroup) actionManager.getAction("DBNavigator.ActionGroup.DatabaseLogOutput");
        DefaultActionGroup toolbarActions = (DefaultActionGroup) console.getToolbarActions();
        if (toolbarActions != null) {
            for (AnAction action : toolbarActions.getChildActionsOrStubs()) {
                if (action instanceof PreviousOccurenceToolbarAction || action instanceof NextOccurenceToolbarAction) {
                    toolbarActions.remove(action);
                }
            }

            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.KillProcess"), Constraints.FIRST);
            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.RerunProcess"), Constraints.FIRST);
            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.Close"), Constraints.FIRST);
            toolbarActions.add(actionManager.getAction("DBNavigator.Actions.DatabaseLogOutput.Settings"), Constraints.LAST);
            ActionToolbar actionToolbar = actionManager.createActionToolbar(ActionPlaces.UNKNOWN, toolbarActions, false);
            actionsPanel.add(actionToolbar.getComponent());
            actionToolbar.setTargetComponent(console.getToolbarContextComponent());
        }


        ActionUtil.registerDataProvider(mainPanel, loggingResult);
    }

    public DatabaseLoggingResultConsole getConsole() {
        return console;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void disposeInner() {
        Disposer.dispose(console);
        super.disposeInner();
    }
}
