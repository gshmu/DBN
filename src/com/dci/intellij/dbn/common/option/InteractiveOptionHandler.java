package com.dci.intellij.dbn.common.option;


import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
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
    private T lastUsedOption;
    private List<T> options;

    public InteractiveOptionHandler(String configName, String title, String message, @NotNull T defaultOption, T... options) {
        this.configName = configName;
        this.title = title;
        this.message = message;
        this.options = Arrays.asList(options);
        this.defaultOption = defaultOption;
    }

    @Override
    public boolean isToBeShown() {
        return true;
    }

    @Override
    public void setToBeShown(boolean keepAsking, int selectedIndex) {
        T selectedOption = options.get(selectedIndex);
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
            int lastUsedOptionIndex = 0;
            if (lastUsedOption != null) {
                lastUsedOptionIndex = options.indexOf(lastUsedOption);
            }

            int optionIndex = Messages.showDialog(
                    MessageFormat.format(message, messageArgs),
                    Constants.DBN_TITLE_PREFIX + title,
                    toStringOptions(options), lastUsedOptionIndex, Icons.DIALOG_QUESTION, this);

            T option = options.get(optionIndex);
            if (!option.isCancel() && !option.isAsk()) {
                lastUsedOption = option;
            }
            return option;
        }
    }

    public static String[] toStringOptions(List<? extends InteractiveOption> options) {
        String[] stringOptions = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            stringOptions[i] = options.get(i).getName();
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
