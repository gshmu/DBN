package com.dci.intellij.dbn.common.option;


import java.text.MessageFormat;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

public class InteractiveOptionHandler implements DialogWrapper.DoNotAskOption{
    private String title;
    private String message;
    private InteractiveOption defaultOption;
    private InteractiveOption selectedOption;
    private InteractiveOption[] options;

    public InteractiveOptionHandler(String title, String message, @NotNull InteractiveOption defaultOption, InteractiveOption... options) {
        this.title = title;
        this.message = message;
        this.options = options;
        this.defaultOption = defaultOption;
    }

    @Override
    public boolean isToBeShown() {
        return true;
    }

    @Override
    public void setToBeShown(boolean keepAsking, int selectedIndex) {
        InteractiveOption selectedOption = options[selectedIndex];
        if (keepAsking || selectedOption == null || selectedOption.isAsk() || selectedOption.isCancel()) {
            this.selectedOption = null;
        } else {
            this.selectedOption = selectedOption;
        }
    }

    public void setSelectedOption(InteractiveOption selectedOption) {
        if (selectedOption.isAsk() || selectedOption.isCancel()) {
            this.selectedOption = null;
        } else {
            this.selectedOption = selectedOption;
        }
    }

    @NotNull
    public InteractiveOption getSelectedOption() {
        return CommonUtil.nvl(selectedOption, defaultOption);
    }

    @NotNull
    public InteractiveOption getDefaultOption() {
        return defaultOption;
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
        return "Remember option";
    }

    public int resolve(String ... messageArgs) {
        if (selectedOption != null) {
            return CommonUtil.indexOf(options, selectedOption);
        } else {
            return Messages.showDialog(
                    MessageFormat.format(message, messageArgs),
                    Constants.DBN_TITLE_PREFIX + title,
                    toStringOptions(options), 0, Icons.DIALOG_WARNING, this);
        }
    }

    public static String[] toStringOptions(InteractiveOption[] options) {
        String[] stringOptions = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            stringOptions[i] = options[i].getName();
        }
        return stringOptions;
    }

}
