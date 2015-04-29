package com.dci.intellij.dbn.common.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;

public class DBNCollapsiblePanel<P extends DisposableProjectComponent> extends DBNFormImpl<P>{
    private JLabel collapseExpandLabel;
    private JPanel contentPanel;
    private JPanel mainPanel;
    private boolean expanded = false;

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public DBNCollapsiblePanel(@NotNull P parentComponent, JComponent contentComponent, String title) {
        super(parentComponent);
        contentPanel.add(contentComponent, BorderLayout.CENTER);
        collapseExpandLabel.setText(title);
        collapseExpandLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        collapseExpandLabel.setIcon(Icons.COMMON_RIGHT);

        collapseExpandLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
                    expanded = !expanded;
                    contentPanel.setVisible(expanded);
                    collapseExpandLabel.setIcon(expanded ? Icons.COMMON_DOWN : Icons.COMMON_RIGHT);
                }
            }
        });
    }
}
