package com.dci.intellij.dbn.editor.code.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.option.ConfirmationOptionHandler;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.editor.code.options.ui.CodeEditorConfirmationSettingsForm;

public class CodeEditorConfirmationSettings extends Configuration<CodeEditorConfirmationSettingsForm> {
    public static final String REMEMBER_OPTION_HINT = ""; //"\n\n(you can remember your option and change it at any time in Settings > Operations > Session Manager)";

    private ConfirmationOptionHandler saveChangesOptionHandler =
            new ConfirmationOptionHandler(
                    "save-changes",
                    "Save changes",
                    "Are you sure you want to save the changes for {0}?" + REMEMBER_OPTION_HINT, false);

    private ConfirmationOptionHandler revertChangesOptionHandler =
            new ConfirmationOptionHandler(
                    "revert-changes",
                    "Revert Changes",
                    "Are you sure you want to revert the changes for {0}?" + REMEMBER_OPTION_HINT, true);

    private InteractiveOptionHandler<CodeEditorChangesOption> exitOnChangesOptionHandler =
            new InteractiveOptionHandler<CodeEditorChangesOption>(
                    "exit-on-changes",
                    "Unsaved Changes",
                    "You are about to close the editor for {0} and you have unsaved changes.\nPlease select whether to save or discard the changes." + REMEMBER_OPTION_HINT,
                    CodeEditorChangesOption.ASK,
                    CodeEditorChangesOption.SAVE,
                    CodeEditorChangesOption.DISCARD,
                    CodeEditorChangesOption.CANCEL);


    public String getDisplayName() {
        return "Code Editor Confirmation Settings";
    }

    public String getHelpTopic() {
        return "codeEditorConfirmationSettings";
    }


    /*********************************************************
     *                       Settings                        *
     *********************************************************/

    public ConfirmationOptionHandler getSaveChangesOptionHandler() {
        return saveChangesOptionHandler;
    }

    public ConfirmationOptionHandler getRevertChangesOptionHandler() {
        return revertChangesOptionHandler;
    }

    public InteractiveOptionHandler<CodeEditorChangesOption> getExitOnChangesOptionHandler() {
        return exitOnChangesOptionHandler;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @NotNull
    public CodeEditorConfirmationSettingsForm createConfigurationEditor() {
        return new CodeEditorConfirmationSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "confirmations";
    }

    public void readConfiguration(Element element) {
        saveChangesOptionHandler.readConfiguration(element);
        revertChangesOptionHandler.readConfiguration(element);
    }

    public void writeConfiguration(Element element) {
        saveChangesOptionHandler.writeConfiguration(element);
        revertChangesOptionHandler.writeConfiguration(element);
    }
}
