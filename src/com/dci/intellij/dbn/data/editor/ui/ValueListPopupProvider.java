package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class ValueListPopupProvider implements TextFieldPopupProvider{
    private TextFieldWithPopup editorComponent;
    private ListPopupValuesProvider valuesProvider;

    private boolean autoPopup;
    private boolean enabled = true;
    private boolean buttonVisible;
    private JLabel button;

    private JBPopup popup;

    ValueListPopupProvider(TextFieldWithPopup editorComponent, ListPopupValuesProvider valuesProvider, boolean autoPopup, boolean buttonVisible) {
        this.editorComponent = editorComponent;
        this.valuesProvider = valuesProvider;
        this.autoPopup = autoPopup;
        this.buttonVisible = buttonVisible;
    }

    @Override
    public TextFieldPopupType getPopupType() {
        return null;
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

    @Override
    public boolean isButtonVisible() {
        return buttonVisible;
    }

    @Override
    public boolean isAutoPopup() {
        return autoPopup;
    }

    @Override
    public boolean isShowingPopup() {
        return popup != null && popup.isVisible();
    }

    boolean isPreparingPopup = false;
    @Override
    public void showPopup() {
        if (valuesProvider.isLongLoading()) {
            if (isPreparingPopup) return;

            isPreparingPopup = true;
            BackgroundTask.invoke(editorComponent.getProject(),
                    instructions("Loading " + getDescription(), TaskInstruction.CANCELLABLE),
                    (data, progress) -> {
                        // load the values
                        getValues();
                        getSecondaryValues();
                        if (progress.isCanceled()) {
                            isPreparingPopup = false;
                            return;
                        }

                        SimpleLaterInvocator.invoke(() -> {
                            try {
                                if (!isShowingPopup()) {
                                    doShowPopup();
                                }
                            } finally {
                                isPreparingPopup = false;
                            }
                        });
                    });
        } else {
            doShowPopup();
        }
    }

    private void doShowPopup() {
        List<String> values = getValues();
        List<String> secondaryValues = getSecondaryValues();
        if (false && values.size() < 20)  {
            String[] valuesArray = values.toArray(new String[0]);
            BaseListPopupStep<String> listPopupStep = new BaseListPopupStep<String>(null, valuesArray){
                @Override
                public PopupStep onChosen(String selectedValue, boolean finalChoice) {
                    editorComponent.setText(selectedValue);
                    return FINAL_CHOICE;
                }
            };
            popup = JBPopupFactory.getInstance().createListPopup(listPopupStep);
        } else {
            DefaultActionGroup actionGroup = new DefaultActionGroup();

            for (String value : values) {
                if (StringUtil.isNotEmpty(value)) {
                    actionGroup.add(new ValueSelectAction(value));
                }
            }
            if (secondaryValues.size() > 0) {
                if (values.size() > 0) {
                    actionGroup.add(ActionUtil.SEPARATOR);
                }
                for (String secondaryValue : secondaryValues) {
                    if (StringUtil.isNotEmpty(secondaryValue)) {
                        actionGroup.add(new ValueSelectAction(secondaryValue));
                    }
                }
            }

            popup = JBPopupFactory.getInstance().createActionGroupPopup(
                    null,
                    actionGroup,
                    DataManager.getInstance().getDataContext(editorComponent),
                    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                    true, null, 10);
        }

        GUIUtil.showUnderneathOf(popup, editorComponent, 4, 200);
    }

    private List<String> getValues() {
        return valuesProvider.getValues();
    }

    private List<String> getSecondaryValues() {
        return valuesProvider.getSecondaryValues();
    }

    @Override
    public void hidePopup() {
        if (popup != null) {
            if (popup.isVisible()) popup.cancel();
            Disposer.dispose(popup);
        }
    }

    @Override public void handleFocusLostEvent(FocusEvent focusEvent) {}
    @Override public void handleKeyPressedEvent(KeyEvent keyEvent) {}
    @Override public void handleKeyReleasedEvent(KeyEvent keyEvent) {}

    @Override
    public String getDescription() {
        return valuesProvider.getDescription();
    }

    @Override
    public String getKeyShortcutDescription() {
        return KeymapUtil.getShortcutsText(getShortcuts());
    }

    @Override
    public Shortcut[] getShortcuts() {
        return KeyUtil.getShortcuts(IdeActions.ACTION_CODE_COMPLETION);
    }

    @Nullable
    @Override
    public Icon getButtonIcon() {
        return Icons.DATA_EDITOR_LIST;
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(popup);
    }

    private class ValueSelectAction extends AnAction {
        private String value;

        ValueSelectAction(String value) {
            this.value = value;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            editorComponent.setText(value);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setText(value, false);
        }
    }
}
