package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.common.dispose.Checks;
import com.dci.intellij.dbn.common.ui.table.DBNColoredTableCellRenderer;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.ui.util.Borders;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.data.find.DataSearchResult;
import com.dci.intellij.dbn.data.find.DataSearchResultMatch;
import com.dci.intellij.dbn.data.grid.color.BasicTableTextAttributes;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributes;
import com.dci.intellij.dbn.data.grid.options.DataGridAuditColumnSettings;
import com.dci.intellij.dbn.data.grid.ui.table.sortable.SortableTable;
import com.dci.intellij.dbn.data.model.DataModel;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.border.Border;
import java.awt.*;
import java.util.Iterator;


public class BasicTableCellRenderer extends DBNColoredTableCellRenderer {

    public DataGridTextAttributes getAttributes() {
        return BasicTableTextAttributes.get();
    }

    @Override
    protected void customizeCellRenderer(DBNTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
        DataModelCell cell = (DataModelCell) value;
        if (Checks.isValid(cell)) {
            SortableTable sortableTable = (SortableTable) table;
            boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == rowIndex && table.getSelectedRowCount() == 1;

            BasicTableTextAttributes attributes = (BasicTableTextAttributes) getAttributes();
            SimpleTextAttributes textAttributes = null;


            if (isSelected) {
                textAttributes = attributes.getSelection();

            } else if (sortableTable.isLoading()) {
                textAttributes = attributes.getLoadingData(isCaretRow);

            } else if (cell.isLargeValue()) {
                textAttributes = attributes.getReadonlyData(false, isCaretRow);

            } else {
                DataGridAuditColumnSettings auditColumnSettings = sortableTable.getDataGridSettings().getAuditColumnSettings();
                boolean auditColumn = auditColumnSettings.isAuditColumn(cell.getColumnInfo().getName());
                if (auditColumn) {
                    textAttributes = attributes.getAuditData(false, isCaretRow);
                }
            }
            if (textAttributes == null) {
                textAttributes = attributes.getPlainData(false, isCaretRow);
            }

            Color background = Commons.nvl(textAttributes.getBgColor(), table.getBackground());
            Color foreground = Commons.nvl(textAttributes.getFgColor(), table.getForeground());
            Border border = Borders.lineBorder(background);

            setBorder(border);
            setBackground(background);
            setForeground(foreground);
            writeUserValue(cell, textAttributes, attributes);
        }
    }

    protected void writeUserValue(DataModelCell cell, SimpleTextAttributes textAttributes, DataGridTextAttributes configTextAttributes) {
        String presentableValue;
        String temporaryValue = cell.getTemporaryUserValue();
        if (temporaryValue != null) {
            presentableValue = temporaryValue;

        } else if (cell.getUserValue() instanceof String) {
            presentableValue = (String) cell.getUserValue();
            if (presentableValue.indexOf('\n') > -1) {
                presentableValue = presentableValue.replace('\n', ' ');
            }

        } else {
            presentableValue = Commons.nvl(cell.getPresentableValue(), "");
        }

        DataModel model = cell.getModel();
        if (model.hasSearchResult()) {
            DataSearchResult searchResult = model.getSearchResult();

            Iterator<DataSearchResultMatch> matches = searchResult.getMatches(cell);
            if (matches != null) {
                int lastEndOffset = 0;
                SimpleTextAttributes searchResultAttributes = configTextAttributes.getSearchResult();
                DataSearchResultMatch selectedMatch = searchResult.getSelectedMatch();
                if (selectedMatch != null && selectedMatch.getCell() == cell) {
                    searchResultAttributes = configTextAttributes.getSelection();
                }

                int valueLength = presentableValue.length();
                while (matches.hasNext()) {
                    DataSearchResultMatch match = matches.next();
                    if (match.getStartOffset() > lastEndOffset) {
                        int startOffset = Math.min(valueLength, lastEndOffset);
                        int endOffset = Math.min(valueLength, match.getStartOffset());
                        append(presentableValue.substring(startOffset, endOffset), textAttributes);
                    }
                    int startOffset = Math.min(valueLength, match.getStartOffset());
                    int endOffset = Math.min(valueLength, match.getEndOffset());
                    append(presentableValue.substring(startOffset, endOffset), searchResultAttributes);
                    lastEndOffset = match.getEndOffset();

                }
                if (lastEndOffset < valueLength) {
                    append(presentableValue.substring(lastEndOffset), textAttributes);
                }
            } else {
                append(presentableValue, textAttributes);
            }

        } else {
            append(presentableValue, textAttributes);
        }
    }

    protected static boolean match(int[] indexes, int index) {
        for (int idx : indexes) {
            if (idx == index) return true;
        }
        return false;
    }

    @Override
    protected SimpleTextAttributes modifyAttributes(SimpleTextAttributes attributes) {
        return attributes;
    }
}
