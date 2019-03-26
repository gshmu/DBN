package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.dispose.Failsafe;
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
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

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

    @Nullable
    public static VirtualFile getVirtualFile(@NotNull AnActionEvent e) {
        return e.getData(PlatformDataKeys.VIRTUAL_FILE);
    }

    @Nullable
    public static VirtualFile getVirtualFile(@NotNull Component component) {
        DataContext dataContext = getDataContext(component);
        return PlatformDataKeys.VIRTUAL_FILE.getData(dataContext);
    }

    @Nullable
    public static Editor getEditor(@NotNull AnActionEvent e) {
        return e.getData(PlatformDataKeys.EDITOR);
    }

    @Nullable
    public static FileEditor getFileEditor(@NotNull AnActionEvent e) {
        return e.getData(PlatformDataKeys.FILE_EDITOR);
    }


    @NotNull
    public static Project ensureProject(AnActionEvent e) {
        return Failsafe.nn(e.getData(PlatformDataKeys.PROJECT));
    }

    public static <T extends ProjectComponent> T getComponent(AnActionEvent e, Class<T> componentClass) {
        Project project = ensureProject(e);
        return Failsafe.getComponent(project, componentClass);
    }

    /**
     * @deprecated use getProject(Component)
     */
    public static Project getProject(){
        DataContext dataContext = DataManager.getInstance().getDataContextFromFocus().getResult();
        return PlatformDataKeys.PROJECT.getData(dataContext);
    }

    public static Project getProject(Component component){
        DataContext dataContext = getDataContext(component);
        return PlatformDataKeys.PROJECT.getData(dataContext);
    }

    private static DataContext getDataContext(Component component) {
        return DataManager.getInstance().getDataContext(component);
    }

    public static void registerDataProvider(JComponent component, DataProviderSupplier dataProviderSupplier) {
        DataProvider dataProvider = dataProviderSupplier.getDataProvider();
        if (dataProvider != null) {
            DataManager.registerDataProvider(component, dataProvider);
        }
    }
}
