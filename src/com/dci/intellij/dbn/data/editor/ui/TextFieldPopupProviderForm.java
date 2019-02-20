package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public abstract class TextFieldPopupProviderForm extends KeyAdapter implements DBNForm, TextFieldPopupProvider {
    protected TextFieldWithPopup editorComponent;
    private boolean autoPopup;
    private boolean enabled = true;
    private boolean buttonVisible;
    private JBPopup popup;
    private JLabel button;
    private Set<AnAction> actions = new HashSet<>();

    TextFieldPopupProviderForm(TextFieldWithPopup editorComponent, boolean autoPopup, boolean buttonVisible) {
        this.editorComponent = editorComponent;
        this.autoPopup = autoPopup;
        this.buttonVisible = buttonVisible;
        Project project = editorComponent.getProject();
        EventUtil.subscribe(project, this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    }

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            hidePopup();
        }
    };

    public TextFieldWithPopup getEditorComponent() {
        return editorComponent;
    }

    public JTextField getTextField() {
        return editorComponent.getTextField();
    }

    @Override
    @NotNull
    public Project getProject() {
        return editorComponent.getProject();
    }

    public JBPopup getPopup() {
        return popup;
    }

    /**
     * Create the popup and return it.
     * If the popup shouldn't show-up for some reason (e.g. empty completion actions),
     * than this method should return null
     */
    @Nullable
    public abstract JBPopup createPopup();
    @Override
    public final String getKeyShortcutDescription() {
        return KeymapUtil.getShortcutsText(getShortcuts());
    }

    @Override
    public final Shortcut[] getShortcuts() {
        return KeyUtil.getShortcuts(getKeyShortcutName());
    }

    protected abstract String getKeyShortcutName();

    @Override
    public boolean isAutoPopup() {
        return autoPopup;
    }

    @Override
    public boolean isButtonVisible() {
        return buttonVisible;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setButton(@Nullable JLabel button) {
        this.button = button;
    }

    @Nullable
    @Override
    public JLabel getButton() {
        return button;
    }

    void registerAction(AnAction action) {
        actions.add(action);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!e.isConsumed()) {
            for (AnAction action : actions) {
                if (KeyUtil.match(action.getShortcutSet().getShortcuts(), e)) {
                    DataContext dataContext = DataManager.getInstance().getDataContext(getComponent());
                    ActionManager actionManager = ActionManager.getInstance();
                    AnActionEvent actionEvent = new AnActionEvent(null, dataContext, "", action.getTemplatePresentation(), actionManager, 2);
                    action.actionPerformed(actionEvent);
                    e.consume();
                    return;
                }
            }
        }
    }

    public Set<AnAction> getActions() {
        return actions;
    }

    public void preparePopup() {}

    boolean isPreparingPopup = false;
    @Override
    public void showPopup() {
        if (isPreparingPopup) return;

        isPreparingPopup = true;
        BackgroundTask.invoke(getProject(),
                instructions("Loading " + getDescription(), TaskInstruction.CANCELLABLE),
                (data, progress) -> {
                    preparePopup();
                    if (progress.isCanceled()) {
                        isPreparingPopup = false;
                        return;
                    }

                    SimpleLaterInvocator.invoke(this, () -> {
                        try {
                            if (!isShowingPopup()) {
                                popup = createPopup();
                                if (popup != null) {
                                    Disposer.register(TextFieldPopupProviderForm.this, popup);

                                    JPanel panel = (JPanel) popup.getContent();
                                    panel.setBorder(Borders.COMPONENT_LINE_BORDER);

                                    editorComponent.clearSelection();

                                    if (editorComponent.isShowing()) {
                                        Point location = editorComponent.getLocationOnScreen();
                                        location.setLocation(location.getX() + 4, location.getY() + editorComponent.getHeight() + 4);
                                        popup.showInScreenCoordinates(editorComponent, location);
                                        //cellEditor.highlight(TextCellEditor.HIGHLIGHT_TYPE_POPUP);
                                    }
                                }
                            }
                        } finally {
                            isPreparingPopup = false;
                        }
                    });
                });
    }

    @Override
    public void hidePopup() {
        if (isShowingPopup()) {
            SimpleLaterInvocator.invoke(this, () -> {
                popup.cancel();
                popup = null;
            });
        }
    }

    @Override
    public boolean isShowingPopup() {
        return popup != null && popup.isVisible();
    }


    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            fileEditorManagerListener = null;
            editorComponent = null;
            popup = null;
        }
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }
}
