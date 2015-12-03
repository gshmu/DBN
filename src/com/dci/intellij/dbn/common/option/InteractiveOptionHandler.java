package com.dci.intellij.dbn.common.option;


import java.text.MessageFormat;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

public class InteractiveOptionHandler<T extends InteractiveOption> implements DialogWrapper.DoNotAskOption, PersistentConfiguration{
    private String configName;
    private String title;
    private String message;
    private T defaultOption;
    private T selectedOption;
    private T[] options;

    public InteractiveOptionHandler(String configName, String title, String message, @NotNull T defaultOption, T... options) {
        this.configName = configName;
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
        T selectedOption = options[selectedIndex];
        if (keepAsking || selectedOption == null || selectedOption.isAsk() || selectedOption.isCancel()) {
            this.selectedOption = null;
        } else {
            this.selectedOption = selectedOption;
        }
    }

    public void set(T selectedOption) {
        assert !selectedOption.isCancel();
        this.selectedOption = selectedOption;
    }

    @NotNull
    public T get() {
        return CommonUtil.nvl(selectedOption, defaultOption);
    }

    @NotNull
    public T getDefaultOption() {
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

    public T resolve(String ... messageArgs) {
        if (selectedOption != null && !selectedOption.isAsk()) {
            return selectedOption;
        } else {
            int optionIndex = Messages.showDialog(
                    MessageFormat.format(message, messageArgs),
                    Constants.DBN_TITLE_PREFIX + title,
                    toStringOptions(options), 0, Icons.DIALOG_QUESTION, this);
            return options[optionIndex];
        }
    }

    public static String[] toStringOptions(InteractiveOption[] options) {
        String[] stringOptions = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            stringOptions[i] = options[i].getName();
        }
        return stringOptions;
    }


    /*******************************************************
     *              PersistentConfiguration                *
     *******************************************************/
    @Override
    public void readConfiguration(Element element) {
        T option = (T) SettingsUtil.getEnum(element, configName, (Enum)defaultOption);
        set(option);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsUtil.setEnum(element, configName, (Enum) get());
    }
}
