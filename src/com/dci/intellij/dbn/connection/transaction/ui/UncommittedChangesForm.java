package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionListener;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UncommittedChangesForm extends DBNFormImpl implements TransactionListener {
    private JTable changesTable;
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JBScrollPane changesTableScrollPane;
    private JButton commitButton;
    private JButton rollbackButton;
    private JPanel transactionActionsPanel;

    private ConnectionHandler connectionHandler;

    public UncommittedChangesForm(final ConnectionHandler connectionHandler, final TransactionAction additionalOperation, boolean showActions) {
        this.connectionHandler = connectionHandler;
        Project project = connectionHandler.getProject();

        // HEADER
        String headerTitle = connectionHandler.getName();
        Icon headerIcon = connectionHandler.getIcon();
        Color headerBackground = UIUtil.getPanelBackground();
        if (getEnvironmentSettings(connectionHandler.getProject()).getVisibilitySettings().getDialogHeaders().value()) {
            headerBackground = connectionHandler.getEnvironmentType().getColor();
        }
        DBNHeaderForm headerForm = new DBNHeaderForm(
                headerTitle,
                headerIcon,
                headerBackground);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        changesTableScrollPane.getViewport().setBackground(changesTable.getBackground());

        transactionActionsPanel.setVisible(showActions);
        if (showActions) {
            final ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(connectionHandler.getProject());
                    Object source = e.getSource();
                    if (source == commitButton) {
                        transactionManager.execute(connectionHandler, false, TransactionAction.COMMIT, additionalOperation);
                    } else if (source == rollbackButton) {
                        transactionManager.execute(connectionHandler, false, TransactionAction.ROLLBACK, additionalOperation);
                    }
                }
            };

            commitButton.addActionListener(actionListener);
            rollbackButton.addActionListener(actionListener);

        }
        EventManager.subscribe(project, TransactionListener.TOPIC, this);
    }

    private void createUIComponents() {
        UncommittedChangesTableModel model = new UncommittedChangesTableModel(connectionHandler);
        changesTable = new UncommittedChangesTable(model);
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
        EventManager.unsubscribe(this);
        connectionHandler = null;
    }

    /********************************************************
     *                Transaction Listener                  *
     ********************************************************/
    @Override
    public void beforeAction(ConnectionHandler connectionHandler, TransactionAction action) {
    }

    @Override
    public void afterAction(ConnectionHandler connectionHandler, TransactionAction action, boolean succeeded) {
        if (connectionHandler == this.connectionHandler && succeeded) {
            refreshForm(connectionHandler);
        }
    }

    private void refreshForm(final ConnectionHandler connectionHandler) {
        new SimpleLaterInvocator() {
            @Override
            public void execute() {
                if (!isDisposed()) {
                    UncommittedChangesTableModel model = new UncommittedChangesTableModel(connectionHandler);
                    changesTable.setModel(model);
                    commitButton.setEnabled(false);
                    rollbackButton.setEnabled(false);
                }
            }
        }.start();
    }
}
