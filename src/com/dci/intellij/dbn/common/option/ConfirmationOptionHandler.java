package com.dci.intellij.dbn.common.option;


import java.text.MessageFormat;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

public class ConfirmationOptionHandler implements DialogWrapper.DoNotAskOption, PersistentConfiguration{
    private String configName;
    private String title;
    private String message;
    private boolean confirm;

    public ConfirmationOptionHandler(String configName, String title, String message, boolean defaultKeepAsking) {
        this.configName = configName;
        this.title = title;
        this.message = message;
        this.confirm = defaultKeepAsking;
    }

    @Override
    public boolean isToBeShown() {
        return true;
    }

    @Override
    public void setToBeShown(boolean keepAsking, int selectedIndex) {
        this.confirm = keepAsking;
    }

    public void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }

    public boolean isConfirm() {
        return confirm;
    }

    @Override
    public boolean canBeHidden() {
        return true;
    }

    @Override
    public boolean shouldSaveOptionsOnCancel() {
        return false;
    }

    @NotNull
    @Override
    public String getDoNotShowMessage() {
        return "Do not ask again";
    }

    public boolean resolve(String ... messageArgs) {
        if (confirm) {
            int optionIndex = Messages.showDialog(
                    MessageFormat.format(message, messageArgs),
                    Constants.DBN_TITLE_PREFIX + title,
                    new String[]{"Yes", "No"}, 0, Icons.DIALOG_QUESTION, this);
            return optionIndex == 0;
        }
        return true;
    }

    /*******************************************************
     *              PersistentConfiguration                *
     *******************************************************/
    @Override
    public void readConfiguration(Element element) {
        confirm = SettingsUtil.getBoolean(element, configName, confirm);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsUtil.setBoolean(element, configName, confirm);
    }
}
