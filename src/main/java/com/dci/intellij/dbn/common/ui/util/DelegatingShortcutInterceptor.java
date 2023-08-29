package com.dci.intellij.dbn.common.ui.util;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.Actions;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.intellij.openapi.actionSystem.AnAction.getEventProject;

public abstract class DelegatingShortcutInterceptor implements AnActionListener {
    private final String delegateActionId;
    private final Class<? extends AnAction> delegateActionClass;

    public DelegatingShortcutInterceptor(String delegateActionId) {
        this.delegateActionId = delegateActionId;
        this.delegateActionClass = getAction().getClass();
    }

    private AnAction getAction() {
        return ActionManager.getInstance().getAction(delegateActionId);
    }

    @Override
    @Compatibility
    public void afterActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext, @NotNull AnActionEvent event) {
        attemptDelegation(action, event);
    }

    @Override
    @Compatibility
    public void afterActionPerformed(@NotNull AnAction action, @NotNull AnActionEvent event, @NotNull AnActionResult result) {
        attemptDelegation(action, event);
    }

    private void attemptDelegation(@NotNull AnAction action, @NotNull AnActionEvent event) {
        if (Objects.equals(delegateActionClass, action.getClass())) return; // action presumingly invoked already
        if (!Actions.isConsumed(event)) return; // event not consumed, there is still hope

        Shortcut[] shortcuts = Keyboard.getShortcuts(delegateActionId);
        if (!Keyboard.match(shortcuts, event)) return; // not matching shortcut

        Project project = getEventProject(event);
        if (isNotValid(project)) return;

        invokeDelegate(event);
    }

    private void invokeDelegate(@NotNull AnActionEvent event) {
        AnAction delegateAction = getAction();
        AnActionEvent delegateEvent = new AnActionEvent(
                event.getInputEvent(),
                event.getDataContext(),
                event.getPlace(),
                new Presentation(),
                ActionManager.getInstance(), 0);

        Dispatch.run(() -> delegateAction.actionPerformed(delegateEvent));
    }
}
