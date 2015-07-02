package com.dci.intellij.dbn.options.action;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.options.ConfigId;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class SettingsGroupAction extends ActionGroup {
    private ConfigId[] configIds;
    public SettingsGroupAction(ConfigId ... configIds) {
        super("Settings", true);
        this.configIds = configIds;
    }

    @NotNull
    @Override
    public AnAction[] getChildren(AnActionEvent e) {
        List<AnAction> actions = new ArrayList<AnAction>();
        for (ConfigId configId : configIds) {
            actions.add(new SettingsAction(configId));
        }
        return actions.toArray(new AnAction[actions.size()]);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        //e.getPresentation().setIcon(Icons.ACTION_SETTINGS);
    }
}
