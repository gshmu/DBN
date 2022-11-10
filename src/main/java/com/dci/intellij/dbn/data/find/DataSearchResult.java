package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.list.ReversedList;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.intellij.openapi.Disposable;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

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
            throw AlreadyDisposedException.INSTANCE;
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
        return getNext(fromRowIndex, fromColumnIndex, scrollPolicy);
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
        selectedMatch = getNext(fromRowIndex, fromColumnIndex, scrollPolicy);
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
        selectedMatch = getPrevious(fromRowIndex, fromColumnIndex, scrollPolicy);
        return selectedMatch;
    }

    private DataSearchResultMatch getNext(int fromRowIndex, int fromColumnIndex, DataSearchResultScrollPolicy scrollPolicy) {
        if (matches.size() > 0) {
            for (DataSearchResultMatch match : matches) {
                int rowIndex = match.getRowIndex();
                int columnIndex = match.getColumnIndex();

                switch (scrollPolicy) {
                    case HORIZONTAL: {
                        if (rowIndex > fromRowIndex || (rowIndex == fromRowIndex && columnIndex >= fromColumnIndex)) {
                            return match;
                        }
                        break;
                    }

                    case VERTICAL: {
                        if (columnIndex > fromColumnIndex || (columnIndex == fromColumnIndex && rowIndex >= fromRowIndex)) {
                            return match;
                        }
                        break;
                    }
                }

            }
            //reached end of the matches without resolving selection
            // scroll to the beginning
            return Lists.firstElement(matches);
        }
        
        return null;
    }
    
    private DataSearchResultMatch getPrevious(int fromRowIndex, int fromColumnIndex, DataSearchResultScrollPolicy scrollPolicy) {
        if (matches.size() > 0) {
            for (DataSearchResultMatch match : ReversedList.get(matches)) {
                int rowIndex = match.getRowIndex();
                int columnIndex = match.getColumnIndex();
                switch (scrollPolicy) {
                    case HORIZONTAL: {
                        if (rowIndex < fromRowIndex || (rowIndex == fromRowIndex && columnIndex <= fromColumnIndex)) {
                            return match;
                        }
                        break;
                    }

                    case VERTICAL: {
                        if (columnIndex < fromColumnIndex || (columnIndex == fromColumnIndex && rowIndex <= fromRowIndex)) {
                            return match;
                        }
                        break;
                    }
                }
            }
            //reached beginning of the matches actions without resolving selection
            // scroll to the end
            return Lists.lastElement(matches);
        }
        
        return null;
    }    


    @Override
    public void dispose() {
        CollectionUtil.clear(matches);
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
