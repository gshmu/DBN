package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.model.DataModel;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.find.FindManager;
import com.intellij.find.FindResult;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DataSearchResultController {
    private final WeakRef<SearchableDataComponent> searchableComponent;

    DataSearchResultController(SearchableDataComponent searchableComponent) {
        this.searchableComponent = WeakRef.of(searchableComponent);
    }

    @NotNull
    public SearchableDataComponent getSearchableComponent() {
        return searchableComponent.ensure();
    }

    void moveCursor(DataSearchDirection direction) {
        BasicTable<? extends BasicDataModel> table = getSearchableComponent().getTable();
        DataModel dataModel = table.getModel();
        DataSearchResult searchResult = dataModel.getSearchResult();
        DataSearchResultScrollPolicy scrollPolicy = DataSearchResultScrollPolicy.HORIZONTAL;
        DataSearchResultMatch oldSelection = searchResult.getSelectedMatch();
        DataSearchResultMatch selection =
                direction == DataSearchDirection.DOWN ? searchResult.selectNext(scrollPolicy) :
                        direction == DataSearchDirection.UP ? searchResult.selectPrevious(scrollPolicy) : null;

        updateSelection(table, oldSelection, selection);
    }

    private void selectFirst(int selectedRowIndex, int selectedColumnIndex) {
        BasicTable<? extends BasicDataModel> table = getSearchableComponent().getTable();
        DataModel dataModel = table.getModel();
        DataSearchResult searchResult = dataModel.getSearchResult();
        DataSearchResultScrollPolicy scrollPolicy = DataSearchResultScrollPolicy.HORIZONTAL;

        DataSearchResultMatch oldSelection = searchResult.getSelectedMatch();
        DataSearchResultMatch selection = searchResult.selectFirst(selectedRowIndex, selectedColumnIndex, scrollPolicy);

        updateSelection(table, oldSelection, selection);
    }

    private static void updateSelection(BasicTable table, DataSearchResultMatch oldSelection, DataSearchResultMatch selection) {
        if (oldSelection != null) {
            DataModelCell cell = oldSelection.getCell();
            Rectangle cellRectangle = table.getCellRect(cell);
            table.repaint(cellRectangle);
        }

        if (selection != null) {
            DataModelCell cell = selection.getCell();
            Rectangle cellRectangle = table.getCellRect(cell);
            table.repaint(cellRectangle);
            cellRectangle.grow(100, 100);
            table.scrollRectToVisible(cellRectangle);
        }
    }

    void updateResult(DataFindModel findModel) {
        Background.run(() -> {
            BasicTable table = getSearchableComponent().getTable();
            DataModel dataModel = table.getModel();
            DataSearchResult searchResult = dataModel.getSearchResult();

            try {
                long updateTimestamp = System.currentTimeMillis();
                searchResult.startUpdating(updateTimestamp);

                FindManager findManager = FindManager.getInstance(table.getProject());

                List<DataSearchResultMatch> matches = new ArrayList<>();
                for (Object r : dataModel.getRows()) {
                    DataModelRow row = (DataModelRow) r;
                    for (Object c : row.getCells()) {
                        DataModelCell cell = (DataModelCell) c;
                        String userValue = cell.getFormattedUserValue();
                        if (userValue != null) {
                            int findOffset = 0;
                            while (true) {
                                FindResult findResult = findManager.findString(userValue, findOffset, findModel);
                                if (findResult.isStringFound()) {
                                    int startOffset = findResult.getStartOffset();
                                    int endOffset = findResult.getEndOffset();

                                    searchResult.checkTimestamp(updateTimestamp);
                                    DataSearchResultMatch match = new DataSearchResultMatch(cell, startOffset, endOffset);
                                    matches.add(match);

                                    findOffset = endOffset;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
                searchResult.setMatches(matches);
            } finally {
                searchResult.stopUpdating();
            }

            Dispatch.run(() -> {
                int selectedRowIndex = table.getSelectedRow();
                int selectedColumnIndex = table.getSelectedRow();
                if (selectedRowIndex < 0) selectedRowIndex = 0;
                if (selectedColumnIndex < 0) selectedColumnIndex = 0;
                getSearchableComponent().cancelEditActions();

                table.clearSelection();
                GUIUtil.repaint(table);

                selectFirst(selectedRowIndex, selectedColumnIndex);
                searchResult.notifyListeners();
            });
        });
    }
}
