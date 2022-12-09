package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.exception.OutdatedContentException;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.intellij.openapi.Disposable;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static com.dci.intellij.dbn.data.find.DataSearchResultScrollPolicy.HORIZONTAL;
import static com.dci.intellij.dbn.data.find.DataSearchResultScrollPolicy.VERTICAL;

@Getter
@Setter
public class DataSearchResult implements Disposable {
    private final Set<DataSearchResultListener> listeners = new HashSet<>();
    private List<DataSearchResultMatch> matches = Collections.emptyList();
    private DataSearchResultMatch selectedMatch;
    private int matchesLimit;
    private long updateTimestamp = 0;
    private boolean updating;

    public void clear() {
        selectedMatch = null;
        matches = Collections.emptyList();
    }

    public int size() {
        return matches.size();
    }

    public boolean isEmpty() {
        return matches.isEmpty();
    }

    public void addListener(DataSearchResultListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners() {
        for (DataSearchResultListener listener : listeners) {
            listener.searchResultUpdated(this);
        }
    }

    public void checkTimestamp(Long updateTimestamp) {
        if (this.updateTimestamp != updateTimestamp) {
            throw new OutdatedContentException(this);
        }
    }

    public Iterator<DataSearchResultMatch> getMatches(DataModelCell cell) {
        DataSearchResultMatch first = matches.isEmpty() ? null : findMatch(null, cell);
        if (first != null) {
            return new Iterator<DataSearchResultMatch>() {
                private DataSearchResultMatch next = first;

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public DataSearchResultMatch next() {
                    DataSearchResultMatch current = next;
                    next = findMatch(next, cell);
                    return current;
                }

                @Override
                public void remove() {}

            };
        } else {
            return null;
        }
    }

    private DataSearchResultMatch findMatch(DataSearchResultMatch previous, DataModelCell cell) {
        int index = previous == null ? 0 : matches.indexOf(previous) + 1;
        for (int i = index; i< matches.size(); i++) {
            DataSearchResultMatch match = matches.get(i);
            if (match != null && match.getCell() == cell) {
                return match;

            } else if (previous != null) {
                return null;
            }
        }
        return null;
    }

    public DataSearchResultMatch selectFirst(int fromRowIndex, int fromColumnIndex, DataSearchResultScrollPolicy scrollPolicy) {
        if (updating) return null;
        return next(fromRowIndex, fromColumnIndex, scrollPolicy);
    }
    
    public DataSearchResultMatch selectNext(DataSearchResultScrollPolicy scrollPolicy) {
        if (updating) return null;
        int fromRowIndex = 0;
        int fromColumnIndex = 0;
        
        if (selectedMatch != null) {
            fromRowIndex = selectedMatch.getRowIndex();
            fromColumnIndex = selectedMatch.getColumnIndex();
            switch (scrollPolicy) {
                case VERTICAL: fromRowIndex++; break;
                case HORIZONTAL: fromColumnIndex++; break;
            }
        }
        selectedMatch = next(fromRowIndex, fromColumnIndex, scrollPolicy);
        return selectedMatch;
    }

    public DataSearchResultMatch selectPrevious(DataSearchResultScrollPolicy scrollPolicy) {
        if (updating) return null;
        int fromRowIndex = 999999;
        int fromColumnIndex = 999999;

        if (selectedMatch != null) {
            fromRowIndex = selectedMatch.getRowIndex();
            fromColumnIndex = selectedMatch.getColumnIndex();
            switch (scrollPolicy) {
                case VERTICAL: fromRowIndex--; break;
                case HORIZONTAL: fromColumnIndex--; break;
            }
        }
        selectedMatch = previous(fromRowIndex, fromColumnIndex, scrollPolicy);
        return selectedMatch;
    }

    private DataSearchResultMatch next(int fromRow, int fromCol, DataSearchResultScrollPolicy scrollPolicy) {
        if (matches.isEmpty()) return null;

        for (DataSearchResultMatch match : matches) {
            int row = match.getRowIndex();
            int col = match.getColumnIndex();

            if (scrollPolicy == HORIZONTAL) {
                if (row > fromRow || (row == fromRow && col >= fromCol)) return match;
            } else if (scrollPolicy == VERTICAL) {
                if (col > fromCol || (col == fromCol && row >= fromRow)) return match;
            }
        }
        //reached end of the matches without resolving selection scroll to the beginning
        return Lists.firstElement(matches);
    }
    
    private DataSearchResultMatch previous(int fromRow, int fromCol, DataSearchResultScrollPolicy scrollPolicy) {
        if (matches.isEmpty()) return null;

        for (DataSearchResultMatch match : Lists.reversed(matches)) {
            int row = match.getRowIndex();
            int col = match.getColumnIndex();

            if (scrollPolicy == HORIZONTAL) {
                if (row < fromRow || (row == fromRow && col <= fromCol)) return match;
            } else if (scrollPolicy == VERTICAL) {
                if (col < fromCol || (col == fromCol && row <= fromRow)) return match;
            }
        }
        //reached beginning of the matches actions without resolving selection scroll to the end
        return Lists.lastElement(matches);
    }


    @Override
    public void dispose() {
        matches = Disposer.replace(matches, Collections.emptyList());
        CollectionUtil.clear(listeners);
        selectedMatch = null;
    }

    public void startUpdating(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
        this.updating = true;
        clear();
    }

    public void stopUpdating() {
        this.updating = false;
    }
}
