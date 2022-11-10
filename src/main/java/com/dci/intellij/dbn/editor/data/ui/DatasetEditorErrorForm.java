package com.dci.intellij.dbn.editor.data.ui;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.util.Fonts;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.editor.data.DatasetEditorError;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelRow;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.ide.IdeTooltip;
import com.intellij.ide.IdeTooltipManager;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class DatasetEditorErrorForm extends DBNFormBase implements ChangeListener {
    public static final Color BACKGROUND_COLOR = new JBColor(
            new Color(0xFFCCCC),
            new Color(0x743A3A));
    private JPanel mainPanel;
    private JLabel errorIconLabel;
    private JTextArea errorMessageTextArea;

    private final WeakRef<DatasetEditorModelCell> cell;

    public DatasetEditorErrorForm(@NotNull DatasetEditorModelCell cell) {
        super(null, cell.getProject());
        this.cell = WeakRef.of(cell);
        DatasetEditorError error = Failsafe.nd(cell.getError());
        error.addChangeListener(this);
        //errorIconLabel.setIcon(Icons.EXEC_MESSAGES_ERROR);
        errorIconLabel.setText("");
        errorMessageTextArea.setText(Strings.textWrap(error.getMessage(), 60, ": ,."));
        Color backgroundColor = BACKGROUND_COLOR;
        errorMessageTextArea.setBackground(backgroundColor);
        errorMessageTextArea.setFont(mainPanel.getFont());
        errorMessageTextArea.setFont(Fonts.deriveFont(Fonts.REGULAR, (float) 14));
        mainPanel.setBackground(backgroundColor);
    }

    @NotNull
    public DatasetEditorModelCell getCell() {
        return cell.ensure();
    }

    public void show() {
        DatasetEditorModelCell cell = getCell();
        DatasetEditorModelRow row = cell.getRow();
        DatasetEditorTable table = row.getModel().getEditorTable();
        Rectangle rectangle = table.getCellRect(row.getIndex(), cell.getIndex(), false);

        if (table.isShowing()) {
            Point location = rectangle.getLocation();
            int x = (int) (location.getX() + rectangle.getWidth() / 4);
            int y = (int) (location.getY() - 2);
            Point cellLocation = new Point(x, y);

            JPanel component = this.getMainComponent();
            IdeTooltip tooltip = new IdeTooltip(table, cellLocation, component);
            tooltip.setTextBackground(BACKGROUND_COLOR);
            IdeTooltipManager.getInstance().show(tooltip, true);
        }
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
    }
}
