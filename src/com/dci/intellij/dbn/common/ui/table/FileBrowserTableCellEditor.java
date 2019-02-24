package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.Borders;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import static com.dci.intellij.dbn.data.editor.ui.DataEditorComponent.BUTTON_BORDER;

public class FileBrowserTableCellEditor extends AbstractCellEditor implements TableCellEditor{
    private JPanel mainPanel = new JPanel();
    private JTextField textField = new JTextField();
    private FileChooserDescriptor fileChooserDescriptor;

    public FileBrowserTableCellEditor(FileChooserDescriptor fileChooserDescriptor) {
        this.fileChooserDescriptor = fileChooserDescriptor;
        textField.setBorder(Borders.TEXT_FIELD_BORDER);

        JLabel button = new JLabel(Icons.DATA_EDITOR_BROWSE);
        button.setBorder(BUTTON_BORDER);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(10, -1));
        mainPanel.setBackground(UIUtil.getTableBackground());
        button.addMouseListener(mouseListener);

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(textField, BorderLayout.CENTER);
        mainPanel.add(button, BorderLayout.EAST);

        FileChooserFactory.getInstance().installFileCompletion(textField, fileChooserDescriptor, true, null);
    }



    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(fileChooserDescriptor, null, null);
                VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(new File(textField.getText()));
                VirtualFile[] virtualFiles = fileChooser.choose(null, file);
                if (virtualFiles.length > 0) {
                    textField.setText(new File(virtualFiles[0].getPath()).getPath());
                }
            }
        }
    };

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textField.setText((String) value);
        Dispatch.invoke(() -> {
            textField.selectAll();
            textField.requestFocus();
        });
        return mainPanel;
    }

    @Override
    public Object getCellEditorValue() {
        return textField.getText();
    }
}
