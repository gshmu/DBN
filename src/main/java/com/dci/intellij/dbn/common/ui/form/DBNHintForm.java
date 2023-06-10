package com.dci.intellij.dbn.common.ui.form;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.text.MimeType;
import com.dci.intellij.dbn.common.text.TextContent;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.util.Fonts;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;

public class DBNHintForm extends DBNFormBase {
    private JPanel mainPanel;
    private JLabel hintLabel;
    private JTextPane hintTextPane;
    private HyperlinkLabel actionLink;

    private final boolean boxed;

    public DBNHintForm(DBNForm parent, @Nullable TextContent hintContent, MessageType messageType, boolean boxed) {
        this(parent, hintContent, messageType, boxed, null, null);
    }

    public DBNHintForm(DBNForm parent, @Nullable TextContent hintContent, MessageType messageType, boolean boxed, String actionText, Runnable action) {
        super(parent);
        this.boxed = boxed;
        hintLabel.setText("");
        setMessageType(messageType);
        setHintContent(hintContent);

        Color background = getBackground();

        mainPanel.setBackground(background);
        hintTextPane.setBackground(background);
        hintTextPane.setFont(Fonts.getLabelFont());
        hintTextPane.setForeground(boxed ? Colors.lafBrighter(Colors.getLabelForeground(), 1) : Colors.HINT_COLOR);
        hintTextPane.getDocument().addDocumentListener(createDocumentListener());
        if (boxed) {
            mainPanel.setBorder(new RoundedLineBorder(UIUtil.getBoundsColor(), 3));
        } else {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) mainPanel.getLayout();
            gridLayoutManager.setMargin(JBUI.emptyInsets());
        }

        if (actionText != null) {
            actionLink.setVisible(true);
            actionLink.setHyperlinkText(actionText);
            actionLink.addHyperlinkListener(e -> action.run());
        } else {
            actionLink.setVisible(false);
        }


    }

    @NotNull
    private DocumentListener createDocumentListener() {
        return new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                Dispatch.run(() -> resizeTextPane());
            }
        };
    }

    @SneakyThrows
    private void resizeTextPane() {
        Dimension preferredSize = hintTextPane.getPreferredSize();

        if (hintTextPane.getDocument() instanceof HTMLDocument) {
            HTMLDocument htmlDocument = (HTMLDocument) hintTextPane.getDocument();
            int width = hintTextPane.getWidth();
            htmlDocument.remove(0, htmlDocument.getLength());
            htmlDocument.insertAfterEnd(htmlDocument.getDefaultRootElement(), "<html><body>" + hintTextPane.getText() + "</body></html>");
            hintTextPane.setSize(width, 1); // Reset the size temporarily to calculate the preferred size
            Dimension contentSize = hintTextPane.getPreferredSize();
            if (!preferredSize.equals(contentSize)) {
                hintTextPane.setPreferredSize(contentSize);
                hintTextPane.revalidate();
            }
        } else {
            Dimension contentSize = hintTextPane.getUI().getPreferredSize(hintTextPane);
            if (!preferredSize.equals(contentSize)) {
                hintTextPane.setPreferredSize(contentSize);
                hintTextPane.revalidate();
            }
        }
    }

    @NotNull
    private Color getBackground() {
        return boxed ? Colors.lafBrighter(Colors.getPanelBackground(), 1) : Colors.getPanelBackground();
    }

    public void setHighlighted(boolean highlighted) {
        Color background = highlighted ? Colors.getTextFieldBackground() : getBackground();
        mainPanel.setBackground(background);
        hintTextPane.setBackground(background);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public void setHintContent(@Nullable TextContent content) {
        if (content == null) {
            hintTextPane.setText("");
            hintTextPane.setContentType(MimeType.TEXT_PLAIN.id());
        } else {
            hintTextPane.setText(content.getText());
            hintTextPane.setContentType(content.getTypeId());
        }
    }

    public void setMessageType(MessageType messageType) {
        if (messageType != null) {
            Icon icon = Icons.COMMON_INFO;
            switch (messageType) {
                case INFO: icon = Icons.COMMON_INFO; break;
                case WARNING: icon = Icons.COMMON_WARNING; break;
                case ERROR: icon = Icons.COMMON_ERROR; break;

            }
            hintLabel.setIcon(icon);
        } else {
            hintLabel.setVisible(false);
        }
    }
}
