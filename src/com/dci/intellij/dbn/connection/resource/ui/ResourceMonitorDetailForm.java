package com.dci.intellij.dbn.connection.resource.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import com.dci.intellij.dbn.connection.session.SessionManagerListener;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionListener;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.DumbAwareActionButton;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.dci.intellij.dbn.common.message.MessageCallback.conditional;
import static com.dci.intellij.dbn.connection.transaction.TransactionAction.actions;


public class ResourceMonitorDetailForm extends DBNFormImpl {
    private final DBNTable<ResourceMonitorSessionsTableModel> sessionsTable;
    private final DBNTable<ResourceMonitorTransactionsTableModel> transactionsTable;
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel sessionsPanel;
    private JBScrollPane transactionsTableScrollPane;
    private JButton commitButton;
    private JButton rollbackButton;
    private JLabel openTransactionsLabel;

    private final ConnectionHandlerRef connectionHandler;

    ResourceMonitorDetailForm(@NotNull DBNComponent parent, ConnectionHandler connectionHandler) {
        super(parent);
        this.connectionHandler = connectionHandler.getRef();

        DBNHeaderForm headerForm = new DBNHeaderForm(this, connectionHandler);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        ResourceMonitorSessionsTableModel sessionsTableModel = new ResourceMonitorSessionsTableModel(connectionHandler);
        sessionsTable = new ResourceMonitorSessionsTable(this, sessionsTableModel);
        sessionsTable.getSelectionModel().addListSelectionListener(e -> {
            DBNConnection connection = getSelectedConnection();
            refreshTransactionsData(connection);
        });

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(sessionsTable);
        decorator.addExtraAction(commitAction);
        decorator.addExtraAction(rollbackAction);
        decorator.addExtraAction(disconnectAction);
        decorator.addExtraAction(deleteSessionAction);
        decorator.setPreferredSize(new Dimension(-1, 400));
        sessionsPanel.add(decorator.createPanel(), BorderLayout.CENTER);
        sessionsTable.getParent().setBackground(sessionsTable.getBackground());

        // transactions table
        ResourceMonitorTransactionsTableModel transactionsTableModel = new ResourceMonitorTransactionsTableModel(connectionHandler, null);
        transactionsTable = new ResourceMonitorTransactionsTable(this, transactionsTableModel);
        transactionsTableScrollPane.setViewportView(transactionsTable);
        transactionsTableScrollPane.getViewport().setBackground(transactionsTable.getBackground());

        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);

        commitButton.addActionListener(transactionActionListener);
        rollbackButton.addActionListener(transactionActionListener);

        Project project = ensureProject();
        ProjectEvents.subscribe(project, this, TransactionListener.TOPIC, transactionListener);
        ProjectEvents.subscribe(project, this, SessionManagerListener.TOPIC, sessionManagerListener);
    }

    private final AnActionButton commitAction = new DumbAwareActionButton("Commit", null, Icons.CONNECTION_COMMIT) {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DatabaseSession session = getSelectedSession();
            if (session != null) {
                ConnectionHandler connectionHandler = getConnectionHandler();
                MessageUtil.showQuestionDialog(getProject(),
                        "Commit Session",
                        "Are you sure you want to commit the session \"" + session.getName() + "\" for connection\"" + connectionHandler.getName() + "\"" ,
                        MessageUtil.OPTIONS_YES_NO, 0,
                        (option) -> conditional(option == 0,
                                () -> {
                                    DBNConnection connection = getSelectedConnection();
                                    if (connection != null) {
                                        DatabaseTransactionManager transactionManager = getTransactionManager();
                                        transactionManager.execute(
                                                connectionHandler,
                                                connection,
                                                actions(TransactionAction.COMMIT),
                                                false,
                                                null);
                                    }
                                }));
            }
        }

        @Override
        public void updateButton(AnActionEvent e) {
            DBNConnection connection = getSelectedConnection();
            e.getPresentation().setEnabled(connection != null && connection.hasDataChanges());
        }
    };

    private final AnActionButton rollbackAction = new DumbAwareActionButton("Rollback", null, Icons.CONNECTION_ROLLBACK) {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DatabaseSession session = getSelectedSession();
            if (session != null) {
                ConnectionHandler connectionHandler = getConnectionHandler();
                MessageUtil.showQuestionDialog(getProject(),
                        "Rollback Session",
                        "Are you sure you want to rollback the session \"" + session.getName() + "\" for connection\"" + connectionHandler.getName() + "\"" ,
                        MessageUtil.OPTIONS_YES_NO, 0,
                        (option) -> conditional(option == 0,
                                () -> {
                                    DBNConnection connection = getSelectedConnection();
                                    if (connection != null) {
                                        DatabaseTransactionManager transactionManager = getTransactionManager();
                                        transactionManager.execute(
                                                connectionHandler,
                                                connection,
                                                actions(TransactionAction.ROLLBACK),
                                                false,
                                                null);
                                    }
                                }));
            }
        }

        @Override
        public void updateButton(AnActionEvent e) {
            DBNConnection connection = getSelectedConnection();
            e.getPresentation().setEnabled(connection != null && connection.hasDataChanges());
        }

    };

    private final AnActionButton disconnectAction = new DumbAwareActionButton("Disconnect", null, Icons.ACTION_DISCONNECT_SESSION) {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DatabaseSession session = getSelectedSession();
            if (session != null) {
                ConnectionHandler connectionHandler = getConnectionHandler();
                MessageUtil.showQuestionDialog(getProject(),
                        "Disconnect Session",
                        "Are you sure you want to disconnect the session \"" + session.getName() + "\" for connection\"" + connectionHandler.getName() + "\"" ,
                        MessageUtil.OPTIONS_YES_NO, 0,
                        (option) -> conditional(option == 0,
                                () -> {
                                    DBNConnection connection = getSelectedConnection();
                                    if (connection != null) {
                                        DatabaseTransactionManager transactionManager = getTransactionManager();
                                        transactionManager.execute(
                                                connectionHandler,
                                                connection,
                                                actions(TransactionAction.DISCONNECT),
                                                false,
                                                null);
                                    }
                                }));
            }
        }

        @Override
        public void updateButton(AnActionEvent e) {
            DBNConnection connection = getSelectedConnection();
            e.getPresentation().setEnabled(connection != null && !connection.isClosed());
        }

    };

    private final AnActionButton deleteSessionAction = new DumbAwareActionButton("Delete Session", null, Icons.ACTION_DELETE) {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DatabaseSession session = getSelectedSession();
            if (session != null) {
                MessageUtil.showQuestionDialog(getProject(),
                        "Delete Session",
                        "Are you sure you want to delete the session \"" + session.getName() + "\" for connection\"" + getConnectionHandler().getName() + "\"" ,
                        MessageUtil.OPTIONS_YES_NO, 0,
                        (option) -> conditional(option == 0,
                                () -> {
                                    DatabaseSessionManager sessionManager = DatabaseSessionManager.getInstance(getProject());
                                    sessionManager.deleteSession(session);
                                }));
            }
        }

        @Override
        public void updateButton(AnActionEvent e) {
            DatabaseSession session = getSelectedSession();
            DBNConnection connection = getSelectedConnection();
            e.getPresentation().setEnabled(
                    session != null &&
                    session.isCustom() &&
                    (connection == null || !connection.hasDataChanges()));
        }
    };

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    /********************************************************
     *                Transaction Listener                  *
     ********************************************************/
    private final TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void afterAction(@NotNull ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action, boolean succeeded) {
            if (connectionHandler == getConnectionHandler() && transactionsTable.getModel().getConnection() == connection && succeeded) {
                refreshTransactionsData(connection);
            }
        }
    };

    private final SessionManagerListener sessionManagerListener = new SessionManagerListener() {
        @Override
        public void sessionCreated(DatabaseSession session) {
            if (session.getConnectionHandler() == getConnectionHandler()) {
                refreshSessionData(session);
            }
        }

        @Override
        public void sessionDeleted(DatabaseSession session) {
            if (session.getConnectionHandler() == getConnectionHandler()) {
                refreshSessionData(session);
            }
        }

        @Override
        public void sessionChanged(DatabaseSession session) {
            if (session.getConnectionHandler() == getConnectionHandler()) {
                refreshSessionData(session);
            }
        }
    };

    private final ActionListener transactionActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == commitButton || source == rollbackButton) {
                ConnectionHandler connectionHandler = getConnectionHandler();
                DatabaseTransactionManager transactionManager = getTransactionManager();
                DBNConnection connection = getSelectedConnection();
                if (connection != null) {
                    if (source == commitButton) {
                        transactionManager.execute(
                                connectionHandler,
                                connection,
                                actions(TransactionAction.COMMIT),
                                false,
                                null);

                    } else if (source == rollbackButton) {
                        transactionManager.execute(
                                connectionHandler,
                                connection,
                                actions(TransactionAction.ROLLBACK), false,
                                null);
                    }
                }
            }
        }
    };

    private void refreshSessionData(DatabaseSession session) {
        Dispatch.run(() -> {
            checkDisposed();
            ConnectionHandler connectionHandler = getConnectionHandler();

            ResourceMonitorSessionsTableModel sessionsTableModel = new ResourceMonitorSessionsTableModel(connectionHandler);
            sessionsTable.setModel(sessionsTableModel);
        });
    }

    private void refreshTransactionsData(DBNConnection connection) {
        Dispatch.run(() -> {
            checkDisposed();
            ConnectionHandler connectionHandler = getConnectionHandler();

            ResourceMonitorTransactionsTableModel transactionsTableModel = new ResourceMonitorTransactionsTableModel(connectionHandler, connection);
            transactionsTable.setModel(transactionsTableModel);
            boolean transactional = connection != null && connection.hasDataChanges();
            commitButton.setEnabled(transactional);
            rollbackButton.setEnabled(transactional);
            DatabaseSession session = getSelectedSession();
            openTransactionsLabel.setText(session == null ? "Open Transactions" : "Open Transactions (" + session.getName() + ")");
        });
    }

    @Nullable
    private DatabaseSession getSelectedSession() {
        return sessionsTable.getModel().getSession(sessionsTable.getSelectedRow());
    }

    @Nullable
    private DBNConnection getSelectedConnection() {
        DatabaseSession session = getSelectedSession();
        if (session != null) {
            return getConnectionHandler().getConnectionPool().getSessionConnection(session.getId());
        }
        return null;
    }

    @NotNull
    private DatabaseTransactionManager getTransactionManager() {
        return DatabaseTransactionManager.getInstance(getProject());
    }

}
