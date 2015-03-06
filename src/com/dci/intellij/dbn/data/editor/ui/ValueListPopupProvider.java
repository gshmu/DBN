package com.dci.intellij.dbn.data.editor.ui;

import javax.swing.Icon;
import javax.swing.JLabel;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Disposer;

public class ValueListPopupProvider implements TextFieldPopupProvider{
    private TextFieldWithPopup editorComponent;
    private ListPopupValuesProvider valuesProvider;
    private List<String> values = new ArrayList<String>();


    private boolean autoPopup;
    private boolean enabled = true;
    private boolean buttonVisible = true;
    private JLabel button;

    private JBPopup popup;

    public ValueListPopupProvider(TextFieldWithPopup editorComponent, ListPopupValuesProvider valuesProvider, boolean autoPopup, boolean buttonVisible) {
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
    public void showPopup() {
        if (valuesProvider.isLazyLoading()) {
            if (isPreparingPopup) return;

            isPreparingPopup = true;
            new BackgroundTask(editorComponent.getProject(), "Loading " + getDescription(), false, true) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                    if (values.isEmpty()) values = valuesProvider.getValues();
                    if (progressIndicator.isCanceled()) {
                        isPreparingPopup = false;
                        return;
                    }

                    new SimpleLaterInvocator(){
                        public void execute() {
                            try {
                                if (!isShowingPopup()) {
                                    doShowPopup();
                                }
                            } finally {
                                isPreparingPopup = false;
                            }
                        }
                    }.start();

                }
            }.start();
        } else {
            doShowPopup();
        }
    }

    private void doShowPopup() {
        if (values.size() < 20)  {
            String[] valuesArray = values.toArray(new String[values.size()]);
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

            for (int i = 0; i<values.size(); i++) {
                actionGroup.add(new ValueSelectAction(i));
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
        private int index;

        public ValueSelectAction(int index) {
            this.index = index;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            editorComponent.setText(values.get(index));
        }

        @Override
        public void update(AnActionEvent e) {
            String value = values.get(index);
            String text = NamingUtil.enhanceUnderscoresForDisplay(value);
            Presentation presentation = e.getPresentation();
            presentation.setText(text);
        }
    }
}
