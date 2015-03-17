package com.dci.intellij.dbn.common.util;

import javax.swing.JComponent;
import java.awt.Component;
import org.jetbrains.annotations.Nullable;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;

public class ActionUtil {
    public static final AnAction SEPARATOR = Separator.getInstance();


    public static ActionToolbar createActionToolbar(String place, boolean horizontal, String actionGroupName){
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup actionGroup = (ActionGroup) actionManager.getAction(actionGroupName);
        return actionManager.createActionToolbar(place, actionGroup, horizontal);
    }

    public static ActionToolbar createActionToolbar(String place, boolean horizontal, ActionGroup actionGroup){
        ActionManager actionManager = ActionManager.getInstance();
        return actionManager.createActionToolbar(place, actionGroup, horizontal);
    }

    public static ActionToolbar createActionToolbar(String place, boolean horizontal, AnAction... actions){
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (AnAction action : actions) {
            if (action == SEPARATOR)
                actionGroup.addSeparator(); else
                actionGroup.add(action);
        }

        return actionManager.createActionToolbar(place, actionGroup, horizontal);
    }

    @Nullable
    public static Project getProject(AnActionEvent e) {
        return e.getData(PlatformDataKeys.PROJECT);
    }

    /**
     * @deprecated use getProject(Component)
     */
    public static Project getProject(){
        DataContext dataContext = DataManager.getInstance().getDataContextFromFocus().getResult();
        return PlatformDataKeys.PROJECT.getData(dataContext);
    }

    public static Project getProject(Component component){
        return PlatformDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(component));
    }
    
    public static void registerDataProvider(JComponent component, DataProviderSupplier dataProviderSupplier) {
        DataProvider dataProvider = dataProviderSupplier.getDataProvider();
        if (dataProvider != null) {
            DataManager.registerDataProvider(component, dataProvider);
        }
    }
}
