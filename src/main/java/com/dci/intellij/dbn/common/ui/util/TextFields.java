package com.dci.intellij.dbn.common.ui.util;

import com.dci.intellij.dbn.common.routine.Consumer;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class TextFields {

    public static void onTextChange(TextFieldWithBrowseButton textField, Consumer<DocumentEvent> consumer) {
        onTextChange(textField.getTextField(), consumer);
    }

    public static void onTextChange(JTextComponent textField, Consumer<DocumentEvent> consumer) {
        addDocumentListener(textField, new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                consumer.accept(e);
            }
        });
    }

    public static void addDocumentListener(JTextComponent textField, DocumentListener documentListener) {
        if (textField == null) return;
        textField.getDocument().addDocumentListener(documentListener);
    }
}
