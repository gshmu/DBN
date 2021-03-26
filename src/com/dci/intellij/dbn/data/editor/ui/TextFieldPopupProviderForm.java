package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.KeyAdapter;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.util.Context;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Disposer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public abstract class TextFieldPopupProviderForm extends DBNFormImpl implements KeyAdapter, TextFieldPopupProvider {
    protected TextFieldWithPopup<?> editorComponent;
    @Getter private final boolean autoPopup;
    @Getter private final boolean buttonVisible;
    @Getter @Setter private boolean enabled = true;
    @Getter @Setter private JLabel button;
    @Getter private JBPopup popup;
    @Getter private final Set<AnAction> actions = new HashSet<>();

    TextFieldPopupProviderForm(TextFieldWithPopup<?> editorComponent, boolean autoPopup, boolean buttonVisible) {
        super(editorComponent, editorComponent.getProject());
        this.editorComponent = editorComponent;
        this.autoPopup = autoPopup;
        this.buttonVisible = buttonVisible;
        ProjectEvents.subscribe(ensureProject(), this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    }

    private final FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            hidePopup();
        }
    };

    public TextFieldWithPopup<?> getEditorComponent() {
        return editorComponent;
    }

    public JTextField getTextField() {
        return editorComponent.getTextField();
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

    void registerAction(AnAction action) {
        actions.add(action);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!e.isConsumed()) {
            for (AnAction action : actions) {
                if (KeyUtil.match(action.getShortcutSet().getShortcuts(), e)) {
                    DataContext dataContext = Context.getDataContext(this);
                    ActionManager actionManager = ActionManager.getInstance();
                    AnActionEvent actionEvent = new AnActionEvent(null, dataContext, "", action.getTemplatePresentation(), actionManager, 2);
                    action.actionPerformed(actionEvent);
                    e.consume();
                    return;
                }
            }
        }
    }

    public void preparePopup() {}

    boolean isPreparingPopup = false;
    @Override
    public void showPopup() {
        if (isPreparingPopup) return;

        isPreparingPopup = true;
        Progress.prompt(getProject(), "Loading " + getDescription(), true, (progress) -> {
            preparePopup();
            if (progress.isCanceled()) {
                isPreparingPopup = false;
                return;
            }

            Dispatch.run(() -> {
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
            Dispatch.run(() -> {
                if (isShowingPopup()) {
                    popup.cancel();
                    popup = null;
                }
            });
        }
    }

    @Override
    public boolean isShowingPopup() {
        return popup != null && popup.isVisible();
    }


    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }
}
