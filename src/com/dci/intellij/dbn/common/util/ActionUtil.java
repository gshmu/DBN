package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.action.Lookup;
import com.intellij.openapi.actionSystem.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.UUID;

public class ActionUtil implements Lookup {
    public static final AnAction SEPARATOR = Separator.getInstance();

    public static ActionToolbar createActionToolbar(@NotNull JComponent component, String place, boolean horizontal, String actionGroupName){
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup actionGroup = (ActionGroup) actionManager.getAction(actionGroupName);
        ActionToolbar toolbar = actionManager.createActionToolbar(adjustPlace(place), actionGroup, horizontal);
        toolbar.setTargetComponent(component);
        return toolbar;
    }

    public static ActionToolbar createActionToolbar(@NotNull JComponent component, String place, boolean horizontal, ActionGroup actionGroup){
        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar toolbar = actionManager.createActionToolbar(adjustPlace(place), actionGroup, horizontal);
        toolbar.setTargetComponent(component);
        return toolbar;
    }

    public static ActionToolbar createActionToolbar(@NotNull JComponent component, String place, boolean horizontal, AnAction... actions){
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (AnAction action : actions) {
            if (action == SEPARATOR)
                actionGroup.addSeparator(); else
                actionGroup.add(action);
        }

        ActionToolbar toolbar = actionManager.createActionToolbar(adjustPlace(place), actionGroup, horizontal);
        toolbar.setTargetComponent(component);
        return toolbar;
    }

    private static String adjustPlace(String place) {
        if (StringUtil.isEmpty(place)) {
            return UUID.randomUUID().toString();
        }
        return place;
    }

}
