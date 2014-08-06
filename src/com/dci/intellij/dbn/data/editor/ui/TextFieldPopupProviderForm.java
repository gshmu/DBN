package com.dci.intellij.dbn.data.editor.ui;

import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Disposer;

public abstract class TextFieldPopupProviderForm extends KeyAdapter implements DBNForm {
    protected TextFieldWithPopup editorComponent;
    private boolean isAutoPopup;
    private boolean isEnabled = true;
    private JBPopup popup;
    private Set<AnAction> actions = new HashSet<AnAction>();

    protected TextFieldPopupProviderForm(TextFieldWithPopup editorComponent, boolean isAutoPopup) {
        this.editorComponent = editorComponent;
        this.isAutoPopup = isAutoPopup;
        EventManager.subscribe(editorComponent.getProject(), FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    }

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerAdapter() {
        @Override
        public void selectionChanged(FileEditorManagerEvent event) {
            hidePopup();
        }
    };

    public TextFieldWithPopup getEditorComponent() {
        return editorComponent;
    }

    public JTextField getTextField() {
        return editorComponent.getTextField();
    }

    public Project getProject() {
        return editorComponent.getProject();
    }

    public JBPopup getPopup() {
        return popup;
    }

    /**
     * Create the popup and return it.
     * If the popup shouldn't show-up for some reason (e.g. empty completion list),
     * than this method should return null
     */
    @Nullable
    public abstract JBPopup createPopup();
    public abstract void handleKeyPressedEvent(KeyEvent e);
    public abstract void handleKeyReleasedEvent(KeyEvent e);
    public abstract void handleFocusLostEvent(FocusEvent e);
    public abstract String getKeyShortcutName();
    public abstract String getDescription();
    public abstract TextFieldPopupType getPopupType();
    public String getKeyShortcutDescription() {
        Shortcut[] shortcuts = KeyUtil.getShortcuts(getKeyShortcutName());
        return KeymapUtil.getShortcutsText(shortcuts);
    }

    public boolean isAutoPopup() {
        return isAutoPopup;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void registerAction(AnAction action) {
        actions.add(action);
    }

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

    public boolean matchesKeyEvent(KeyEvent keyEvent) {
        Shortcut[] shortcuts = KeyUtil.getShortcuts(getKeyShortcutName());
        return KeyUtil.match(shortcuts, keyEvent);
    }

    public void showPopup() {
        new ConditionalLaterInvocator(){
            public void execute() {
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
            }
        }.start();
    }

    protected void hidePopup() {
        if (isShowingPopup()) {
            new ConditionalLaterInvocator() {
                @Override
                public void execute() {
                    popup.cancel();
                    popup = null;
                }
            }.start();
        }
    }

    protected boolean isShowingPopup() {
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

    public void dispose() {
        if (!disposed) {
            disposed = true;
            EventManager.unsubscribe(fileEditorManagerListener);
            fileEditorManagerListener = null;
            editorComponent = null;
            popup = null;
        }
    }
}
