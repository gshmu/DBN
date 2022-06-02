package com.dci.intellij.dbn.data.record.ui;

import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.data.record.DatasetRecord;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class RecordViewerColumnForm extends DBNFormBase {
    private JLabel columnLabel;
    private JPanel valueFieldPanel;
    private JLabel dataTypeLabel;
    private JPanel mainPanel;

    private final JTextField valueTextField;

    private final DBObjectRef<DBColumn> columnRef;
    private final DatasetRecord record;

    RecordViewerColumnForm(RecordViewerForm parentForm, DatasetRecord record, DBColumn column) {
        super(parentForm);
        this.record = record;
        this.columnRef = DBObjectRef.of(column);
        Project project = record.getDataset().getProject();

        DBDataType dataType = column.getDataType();

        columnLabel.setIcon(column.getIcon());
        columnLabel.setText(column.getName());
        dataTypeLabel.setText(dataType.getQualifiedName());
        dataTypeLabel.setForeground(UIUtil.getInactiveTextColor());

        valueTextField = new ColumnValueTextField(record, column);
        valueTextField.setPreferredSize(new Dimension(200, -1));
        valueTextField.addKeyListener(keyAdapter);

        valueFieldPanel.add(valueTextField, BorderLayout.CENTER);
        valueTextField.setEditable(false);
        valueTextField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        valueTextField.setBackground(Colors.getTextFieldBackground());
        updateColumnValue(column);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    private void updateColumnValue(DBColumn column) {
        Object value = record.getColumnValue(column);
        Formatter formatter = Formatter.getInstance(ensureProject());
        if (value instanceof String) {
            String userValue = (String) value;
            if (userValue.indexOf('\n') > -1) {
                userValue = userValue.replace('\n', ' ');
            } else {
            }
            valueTextField.setText(userValue);
        } else {
            String presentableValue = formatter.formatObject(value);
            valueTextField.setText(presentableValue);
        }
    }

    @NotNull
    public DBColumn getColumn() {
        return columnRef.ensure();
    }

    protected int[] getMetrics(@NotNull int[] metrics) {
        return new int[] {
            (int) Math.max(metrics[0], columnLabel.getPreferredSize().getWidth()),
            (int) Math.max(metrics[1], valueFieldPanel.getPreferredSize().getWidth())};
    }

    protected void adjustMetrics(@NotNull int[] metrics) {
        columnLabel.setPreferredSize(new Dimension(metrics[0], columnLabel.getHeight()));
        valueFieldPanel.setPreferredSize(new Dimension(metrics[1], valueFieldPanel.getHeight()));
    }

/*    public Object getEditorValue() throws ParseException {
        DBDataType dataType = cell.getColumnInfo().getDataType();
        Class clazz = dataType.getTypeClass();
        String textValue = valueTextField.getText().trim();
        if (textValue.length() > 0) {
            Object value = getFormatter().parseObject(clazz, textValue);
            return dataType.getNativeDataType().getDataTypeDefinition().convert(value);
        } else {
            return null;
        }
    }*/

    public JTextField getViewComponent() {
        return valueTextField;
    }

    /*********************************************************
     *                     Listeners                         *
     *********************************************************/
    private final KeyListener keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!e.isConsumed()) {
                RecordViewerForm parentForm = (RecordViewerForm) ensureParent();
                if (e.getKeyCode() == 38) {//UP
                    parentForm.focusPreviousColumnPanel(RecordViewerColumnForm.this);
                    e.consume();
                } else if (e.getKeyCode() == 40) { // DOWN
                    parentForm.focusNextColumnPanel(RecordViewerColumnForm.this);
                    e.consume();
                }
            }
        }
    };

}
