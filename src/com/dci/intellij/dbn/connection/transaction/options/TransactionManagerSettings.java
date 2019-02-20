package com.dci.intellij.dbn.connection.transaction.options;

import com.dci.intellij.dbn.common.option.InteractiveOptionBroker;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.connection.transaction.TransactionOption;
import com.dci.intellij.dbn.connection.transaction.options.ui.TransactionManagerSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class TransactionManagerSettings extends BasicConfiguration<OperationSettings, TransactionManagerSettingsForm> {
    public static final String REMEMBER_OPTION_HINT = ""/*"\n\n(you can remember your option and change it at any time in Settings > Operations > Transaction Manager)"*/;

    private InteractiveOptionBroker<TransactionOption> closeProject =
            new InteractiveOptionBroker<TransactionOption>(
                    "on-project-close",
                    "Uncommitted changes",
                    "You have uncommitted changes on one or more connections for project \"{0}\". \n" +
                            "Please specify whether to commit or rollback these changes before closing the project" +
                            REMEMBER_OPTION_HINT,
                    TransactionOption.ASK,
                    TransactionOption.COMMIT,
                    TransactionOption.ROLLBACK,
                    TransactionOption.REVIEW_CHANGES,
                    TransactionOption.CANCEL);

    private InteractiveOptionBroker<TransactionOption> toggleAutoCommit =
            new InteractiveOptionBroker<TransactionOption>(
                    "on-autocommit-toggle",
                    "Uncommitted changes",
                    "You have uncommitted changes on the connection \"{0}\". \n" +
                            "Please specify whether to commit or rollback these changes before switching Auto-Commit ON." +
                            REMEMBER_OPTION_HINT,
                    TransactionOption.ASK,
                    TransactionOption.COMMIT,
                    TransactionOption.ROLLBACK,
                    TransactionOption.REVIEW_CHANGES,
                    TransactionOption.CANCEL);

    private InteractiveOptionBroker<TransactionOption> disconnect =
            new InteractiveOptionBroker<TransactionOption>(
                    "on-disconnect",
                    "Uncommitted changes",
                    "You have uncommitted changes on the connection \"{0}\". \n" +
                            "Please specify whether to commit or rollback these changes before disconnecting" +
                            REMEMBER_OPTION_HINT,
                    TransactionOption.ASK,
                    TransactionOption.COMMIT,
                    TransactionOption.ROLLBACK,
                    TransactionOption.REVIEW_CHANGES,
                    TransactionOption.CANCEL);

    private InteractiveOptionBroker<TransactionOption> commitMultipleChanges =
            new InteractiveOptionBroker<TransactionOption>(
                    "on-commit",
                    "Commit multiple changes",
                    "This commit action will affect several other changes on the connection \"{0}\", " +
                            "\nnot only the ones done in \"{1}\"" +
                            REMEMBER_OPTION_HINT,
                    TransactionOption.ASK,
                    TransactionOption.COMMIT,
                    TransactionOption.REVIEW_CHANGES,
                    TransactionOption.CANCEL);

    private InteractiveOptionBroker<TransactionOption> rollbackMultipleChanges =
            new InteractiveOptionBroker<TransactionOption>(
                    "on-rollback",
                    "Rollback multiple changes",
                    "This rollback action will affect several other changes on the connection \"{0}\", " +
                            "\nnot only the ones done in \"{1}\"." +
                            REMEMBER_OPTION_HINT,
                    TransactionOption.ASK,
                    TransactionOption.ROLLBACK,
                    TransactionOption.REVIEW_CHANGES,
                    TransactionOption.CANCEL);

    public TransactionManagerSettings(OperationSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return "Transaction manager settings";
    }

    @Override
    public String getHelpTopic() {
        return "transactionManager";
    }


    /*********************************************************
     *                       Settings                        *
     *********************************************************/

    public InteractiveOptionBroker<TransactionOption> getCloseProject() {
        return closeProject;
    }

    public InteractiveOptionBroker<TransactionOption> getToggleAutoCommit() {
        return toggleAutoCommit;
    }

    public InteractiveOptionBroker<TransactionOption> getDisconnect() {
        return disconnect;
    }

    public InteractiveOptionBroker<TransactionOption> getCommitMultipleChanges() {
        return commitMultipleChanges;
    }

    public InteractiveOptionBroker<TransactionOption> getRollbackMultipleChanges() {
        return rollbackMultipleChanges;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public TransactionManagerSettingsForm createConfigurationEditor() {
        return new TransactionManagerSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "transactions";
    }

    @Override
    public void readConfiguration(Element element) {
        Element uncommittedChangesElement = element.getChild("uncommitted-changes");
        if (uncommittedChangesElement != null) {
            closeProject.readConfiguration(uncommittedChangesElement);
            disconnect.readConfiguration(uncommittedChangesElement);
            toggleAutoCommit.readConfiguration(uncommittedChangesElement);
        }
        Element multipleUncommittedChangesElement = element.getChild("multiple-uncommitted-changes");
        if (multipleUncommittedChangesElement != null) {
            commitMultipleChanges.readConfiguration(uncommittedChangesElement);
            rollbackMultipleChanges.readConfiguration(uncommittedChangesElement);
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        Element uncommittedChangesElement = new Element("uncommitted-changes");
        element.addContent(uncommittedChangesElement);
        closeProject.writeConfiguration(uncommittedChangesElement);
        disconnect.writeConfiguration(uncommittedChangesElement);
        toggleAutoCommit.writeConfiguration(uncommittedChangesElement);

        Element multipleUncommittedChangesElement = new Element("multiple-uncommitted-changes");
        element.addContent(multipleUncommittedChangesElement);
        commitMultipleChanges.writeConfiguration(multipleUncommittedChangesElement);
        rollbackMultipleChanges.writeConfiguration(multipleUncommittedChangesElement);

    }
}
