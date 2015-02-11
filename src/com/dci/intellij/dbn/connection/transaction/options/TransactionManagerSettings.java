package com.dci.intellij.dbn.connection.transaction.options;

import org.jdom.Element;

import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.transaction.TransactionOption;
import com.dci.intellij.dbn.connection.transaction.options.ui.TransactionManagerSettingsForm;

public class TransactionManagerSettings extends Configuration<TransactionManagerSettingsForm> {
    public static final String REMEMBER_OPTION_HINT = "\n\n(you can remember your option and change it at any time in Settings > Operations > Transaction Manager)";

    private InteractiveOptionHandler closeProjectOptionHandler =
            new InteractiveOptionHandler(
                    "Uncommitted changes",
                    "You have uncommitted changes on one or more connections for project \"{0}\". \n" +
                            "Please specify whether to commit or rollback these changes before closing the project" +
                            REMEMBER_OPTION_HINT,
                    TransactionOption.ASK,
                    TransactionOption.COMMIT,
                    TransactionOption.ROLLBACK,
                    TransactionOption.REVIEW_CHANGES,
                    TransactionOption.CANCEL);

    private InteractiveOptionHandler toggleAutoCommitOptionHandler = new InteractiveOptionHandler(
            "Uncommitted changes",
            "You have uncommitted changes on the connection \"{0}\". \n" +
                    "Please specify whether to commit or rollback these changes before switching Auto-Commit ON." +
                    REMEMBER_OPTION_HINT,
            TransactionOption.ASK,
            TransactionOption.COMMIT,
            TransactionOption.ROLLBACK,
            TransactionOption.REVIEW_CHANGES,
            TransactionOption.CANCEL);

    private InteractiveOptionHandler disconnectOptionHandler = new InteractiveOptionHandler(
            "Uncommitted changes",
            "You have uncommitted changes on the connection \"{0}\". \n" +
                    "Please specify whether to commit or rollback these changes before disconnecting" +
                    REMEMBER_OPTION_HINT,
            TransactionOption.ASK,
            TransactionOption.COMMIT,
            TransactionOption.ROLLBACK,
            TransactionOption.REVIEW_CHANGES,
            TransactionOption.CANCEL);

    private InteractiveOptionHandler commitMultipleChangesOptionHandler = new InteractiveOptionHandler(
            "Commit multiple changes",
            "This commit action will affect several other changes on the connection \"{0}\", " +
                    "\nnot only the ones done in \"{1}\"" +
                    REMEMBER_OPTION_HINT,
            TransactionOption.ASK,
            TransactionOption.COMMIT,
            TransactionOption.REVIEW_CHANGES,
            TransactionOption.CANCEL);

    private InteractiveOptionHandler rollbackMultipleChangesOptionHandler = new InteractiveOptionHandler(
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

    public InteractiveOptionHandler getCloseProjectOptionHandler() {
        return closeProjectOptionHandler;
    }

    public InteractiveOptionHandler getToggleAutoCommitOptionHandler() {
        return toggleAutoCommitOptionHandler;
    }

    public InteractiveOptionHandler getDisconnectOptionHandler() {
        return disconnectOptionHandler;
    }

    public InteractiveOptionHandler getCommitMultipleChangesOptionHandler() {
        return commitMultipleChangesOptionHandler;
    }

    public InteractiveOptionHandler getRollbackMultipleChangesOptionHandler() {
        return rollbackMultipleChangesOptionHandler;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    public TransactionManagerSettingsForm createConfigurationEditor() {
        return new TransactionManagerSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "transactions";
    }

    public void readConfiguration(Element element) {
    }

    public void writeConfiguration(Element element) {
    }
}
