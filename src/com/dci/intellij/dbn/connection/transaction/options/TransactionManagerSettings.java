package com.dci.intellij.dbn.connection.transaction.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.transaction.TransactionOption;
import com.dci.intellij.dbn.connection.transaction.options.ui.TransactionManagerSettingsForm;

public class TransactionManagerSettings extends Configuration<TransactionManagerSettingsForm> {
    public static final String REMEMBER_OPTION_HINT = "\n\n(you can remember your option and change it at any time in Settings > Operations > Transaction Manager)";

    private InteractiveOptionHandler<TransactionOption> closeProject =
            new InteractiveOptionHandler<TransactionOption>(
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

    private InteractiveOptionHandler<TransactionOption> toggleAutoCommit =
            new InteractiveOptionHandler<TransactionOption>(
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

    private InteractiveOptionHandler<TransactionOption> disconnect =
            new InteractiveOptionHandler<TransactionOption>(
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

    private InteractiveOptionHandler<TransactionOption> commitMultipleChanges =
            new InteractiveOptionHandler<TransactionOption>(
                    "on-commit",
                    "Commit multiple changes",
                    "This commit action will affect several other changes on the connection \"{0}\", " +
                            "\nnot only the ones done in \"{1}\"" +
                            REMEMBER_OPTION_HINT,
                    TransactionOption.ASK,
                    TransactionOption.COMMIT,
                    TransactionOption.REVIEW_CHANGES,
                    TransactionOption.CANCEL);

    private InteractiveOptionHandler<TransactionOption> rollbackMultipleChanges =
            new InteractiveOptionHandler<TransactionOption>(
                    "on-rollback",
                    "Rollback multiple changes",
                    "This rollback action will affect several other changes on the connection \"{0}\", " +
                            "\nnot only the ones done in \"{1}\"." +
                            REMEMBER_OPTION_HINT,
                    TransactionOption.ASK,
                    TransactionOption.ROLLBACK,
                    TransactionOption.REVIEW_CHANGES,
                    TransactionOption.CANCEL);

    public String getDisplayName() {
        return "Transaction manager settings";
    }

    public String getHelpTopic() {
        return "transactionManager";
    }


    /*********************************************************
     *                       Settings                        *
     *********************************************************/

    public InteractiveOptionHandler<TransactionOption> getCloseProject() {
        return closeProject;
    }

    public InteractiveOptionHandler<TransactionOption> getToggleAutoCommit() {
        return toggleAutoCommit;
    }

    public InteractiveOptionHandler<TransactionOption> getDisconnect() {
        return disconnect;
    }

    public InteractiveOptionHandler<TransactionOption> getCommitMultipleChanges() {
        return commitMultipleChanges;
    }

    public InteractiveOptionHandler<TransactionOption> getRollbackMultipleChanges() {
        return rollbackMultipleChanges;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @NotNull
    public TransactionManagerSettingsForm createConfigurationEditor() {
        return new TransactionManagerSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "transactions";
    }

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
