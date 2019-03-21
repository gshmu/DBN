package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.find.action.CloseOnESCAction;
import com.dci.intellij.dbn.data.find.action.NextOccurrenceAction;
import com.dci.intellij.dbn.data.find.action.PrevOccurrenceAction;
import com.dci.intellij.dbn.data.find.action.ShowHistoryAction;
import com.dci.intellij.dbn.data.find.action.ToggleMatchCase;
import com.dci.intellij.dbn.data.find.action.ToggleRegex;
import com.dci.intellij.dbn.data.find.action.ToggleWholeWordsOnlyAction;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.model.DataModel;
import com.dci.intellij.dbn.data.model.DataModelListener;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
import com.intellij.find.FindSettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.LightColors;
import com.intellij.ui.components.JBList;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

public class DataSearchComponent extends DBNFormImpl implements Disposable, SelectionListener, DataSearchResultListener, DataModelListener {
    private static final int MATCHES_LIMIT = 10000;

    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JTextField searchField;
    private JLabel matchesLabel;
    private JLabel closeLabel;

    private DataFindModel findModel;
    private DataSearchResultController searchResultController;
    private boolean myListeningSelection = false;
    private ActionToolbar actionsToolbar;
    private SearchableDataComponent searchableComponent;

    public JTextField getSearchField() {
        return searchField;
    }

    public DataSearchComponent(final SearchableDataComponent searchableComponent) {
        this.searchableComponent = searchableComponent;
        BasicTable<? extends BasicDataModel> table = searchableComponent.getTable();
        DataModel dataModel = table.getModel();
        dataModel.addDataModelListener(this);
        initializeFindModel();

        findModel = new DataFindModel();
        DataSearchResult searchResult = dataModel.getSearchResult();
        searchResult.setMatchesLimit(MATCHES_LIMIT);
        searchResultController = new DataSearchResultController(searchableComponent);
        searchResult.addListener(this);
        searchResultController.updateResult(findModel);

        Disposer.register(this, searchResultController);
        configureLeadPanel();

        findModel.addObserver(findModel -> {
            String stringToFind = findModel.getStringToFind();
            if (!wholeWordsApplicable(stringToFind)) {
                findModel.setWholeWordsOnly(false);
            }
            updateUIWithFindModel();
            updateResults(true);
            FindManager findManager = getFindManager();
            syncFindModels(findManager.getFindInFileModel(), DataSearchComponent.this.findModel);
        });

        closeLabel.setText(" ");
        closeLabel.setIcon(IconLoader.getIcon("/actions/cross.png"));
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                close();
            }
        });

        updateUIWithFindModel();
        //new CloseOnESCAction(this, table);
        new PrevOccurrenceAction(this, table, false);
        new NextOccurrenceAction(this, table, false);
        searchableComponent.getTable().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!e.isConsumed()) {
                    int keyChar = e.getKeyChar();
                    if (keyChar == 27) { // ESCAPE
                        searchableComponent.hideSearchHeader();
                    }
                }
            }
        });
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return searchField;
    }

    @Override
    public void modelChanged() {
        searchResultController.updateResult(findModel);
    }

    @Override
    public void searchResultUpdated(DataSearchResult searchResult) {
        int count = searchResult.size();
        if (searchField.getText().isEmpty()) {
            updateUIWithEmptyResults();
        } else {
            if (count <= searchResult.getMatchesLimit()) {
                if (count > 0) {
                    setRegularBackground();
                    if (count > 1) {
                        matchesLabel.setText(count + " matches");
                    } else {
                        matchesLabel.setText("1 match");
                    }
                } else {
                    setNotFoundBackground();
                    matchesLabel.setText("No matches");
                }
            } else {
                setRegularBackground();
                matchesLabel.setText("More than " + searchResult.getMatchesLimit() + " matches");
                boldMatchInfo();
            }
        }
    }

    public void initializeFindModel() {
        if (findModel == null) {
            findModel = new DataFindModel();
            FindManager findManager = getFindManager();
            findModel.copyFrom(findManager.getFindInFileModel());
            findModel.setPromptOnReplace(false);
        }
/*
        String stringToFind = searchableComponent.getSelectedText();
        findModel.setStringToFind(StringUtil.isEmpty(stringToFind) ? "" : stringToFind);
*/
    }
    
    public void resetFindModel() {
        if (findModel != null) {
            findModel.setStringToFind("");
        }
    }

    private void configureLeadPanel() {
        initTextField();
        setupSearchFieldListener();

        DefaultActionGroup myActionsGroup = new DefaultActionGroup("search bar", false);
        myActionsGroup.add(new ShowHistoryAction(searchField, this));
        myActionsGroup.add(new PrevOccurrenceAction(this, searchField, true));
        myActionsGroup.add(new NextOccurrenceAction(this, searchField, true));
        //myActionsGroup.add(new FindAllAction(this));
        myActionsGroup.add(new ToggleMatchCase(this));
        myActionsGroup.add(new ToggleRegex(this));

        actionsToolbar = ActionManager.getInstance().createActionToolbar("SearchBar", myActionsGroup, true);

        myActionsGroup.addAction(new ToggleWholeWordsOnlyAction(this));

        actionsToolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        actionsPanel.add(actionsToolbar.getComponent(), BorderLayout.CENTER);

        setSmallerFontAndOpaque(matchesLabel);


        JLabel closeLabel = new JLabel(" ", IconLoader.getIcon("/actions/cross.png"), SwingConstants.RIGHT);
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                close();
            }
        });

        closeLabel.setToolTipText("Close search bar (Escape)");
        CompatibilityUtil.setSmallerFont(searchField);

        searchField.registerKeyboardAction(e -> {
            if (StringUtil.isEmptyOrSpaces(searchField.getText())) {
                close();
            } else {
                // TODO
                //requestFocus(myEditor.getContentComponent());
                addTextToRecent(DataSearchComponent.this.searchField);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, SystemInfo.isMac ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_FOCUSED);

        final String initialText = findModel.getStringToFind();

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                setInitialText(initialText);
            }
        });
    }

    private void setupSearchFieldListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent documentEvent) {
                searchFieldDocumentChanged();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent documentEvent) {
                searchFieldDocumentChanged();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent documentEvent) {
                searchFieldDocumentChanged();
            }
        });
    }

    private void searchFieldDocumentChanged() {
        String text = searchField.getText();
        findModel.setStringToFind(text);
        if (!StringUtil.isEmpty(text)) {
            updateResults(true);
        } else {
            nothingToSearchFor();
        }
    }

    public boolean isRegexp() {
        return findModel.isRegularExpressions();
    }

    public void setRegexp(boolean val) {
        findModel.setRegularExpressions(val);
    }

    public FindModel getFindModel() {
        return findModel;
    }

    private static void syncFindModels(FindModel to, FindModel from) {
        to.setCaseSensitive(from.isCaseSensitive());
        to.setWholeWordsOnly(from.isWholeWordsOnly());
        to.setRegularExpressions(from.isRegularExpressions());
        to.setInCommentsOnly(from.isInCommentsOnly());
        to.setInStringLiteralsOnly(from.isInStringLiteralsOnly());
    }

    private void updateUIWithFindModel() {

        actionsToolbar.updateActionsImmediately();

        String stringToFind = findModel.getStringToFind();

        if (!StringUtil.equals(stringToFind, searchField.getText())) {
            searchField.setText(stringToFind);
        }

        setTrackingSelection(!findModel.isGlobal());
    }

    private static boolean wholeWordsApplicable(String stringToFind) {
        return !stringToFind.startsWith(" ") &&
                !stringToFind.startsWith("\t") &&
                !stringToFind.endsWith(" ") &&
                !stringToFind.endsWith("\t");
    }

    private void setTrackingSelection(boolean b) {
        if (b) {
            if (!myListeningSelection) {
                // TODO
                //myEditor.getSelectionModel().addSelectionListener(this);
            }
        } else {
            if (myListeningSelection) {
                // TODO
                //myEditor.getSelectionModel().removeSelectionListener(this);
            }
        }
        myListeningSelection = b;
    }

    public void showHistory(boolean byClickingToolbarButton, JTextField textField) {
        FeatureUsageTracker.getInstance().triggerFeatureUsed("find.recent.search");
        FindSettings settings = FindSettings.getInstance();
        String[] recent = textField == searchField ? settings.getRecentFindStrings() : settings.getRecentReplaceStrings();
        JBList<String> list = new JBList<>((String[]) ArrayUtil.reverseArray(recent));
        CompatibilityUtil.showSearchCompletionPopup(byClickingToolbarButton, actionsPanel, list, "Recent Searches", textField);
    }

    private void initTextField() {
        //searchField.setColumns(25);
        if (CompatibilityUtil.isUnderGTKLookAndFeel()) {
            searchField.setOpaque(false);
        }
        searchField.putClientProperty("AuxEditorComponent", Boolean.TRUE);
        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent e) {
                GUIUtil.repaint(searchField);
            }

            @Override
            public void focusLost(final FocusEvent e) {
                GUIUtil.repaint(searchField);
            }
        });
        new CloseOnESCAction(this, searchField);
    }


    public void setInitialText(final String initialText) {
        String text = initialText != null ? initialText : "";
        setTextInField(text);
        searchField.selectAll();
    }

    private void requestFocus(Component component) {
        Project project = searchableComponent.getTable().getProject();
        IdeFocusManager.getInstance(project).requestFocus(component, true);
    }

    public void searchBackward() {
        moveCursor(DataSearchDirection.UP);
        addTextToRecent(searchField);
    }

    public void searchForward() {
        moveCursor(DataSearchDirection.DOWN);
        addTextToRecent(searchField);
    }

    private void addTextToRecent(JTextComponent textField) {
        final String text = textField.getText();
        if (text.length() > 0) {
            if (textField == searchField) {
                FindSettings.getInstance().addStringToFind(text);
            } else {
                FindSettings.getInstance().addStringToReplace(text);
            }
        }
    }

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        updateResults(true);
    }

    private void moveCursor(DataSearchDirection direction) {
        searchResultController.moveCursor(direction);
    }

    private static void setSmallerFontAndOpaque(final JComponent component) {
        CompatibilityUtil.setSmallerFont(component);
        component.setOpaque(false);
    }

    public void requestFocus() {
        searchField.setSelectionStart(0);
        searchField.setSelectionEnd(searchField.getText().length());
        requestFocus(searchField);
    }

    public void close() {
        getSearchResult().clear();
        searchableComponent.hideSearchHeader();
    }

    public void removeNotify() {
/*
        // TODO
        myLivePreview.cleanUp();
        myLivePreview.dispose();
*/
        setTrackingSelection(false);
        addTextToRecent(searchField);
    }

    private void updateResults(final boolean allowedToChangedEditorSelection) {
        matchesLabel.setFont(matchesLabel.getFont().deriveFont(Font.PLAIN));
        String text = searchField.getText();
        if (text.length() == 0) {
            nothingToSearchFor();
            searchableComponent.cancelEditActions();
            BasicTable table = searchableComponent.getTable();
            table.clearSelection();
            GUIUtil.repaint(table);
        } else {

            if (findModel.isRegularExpressions()) {
                try {
                    Pattern.compile(text);
                } catch (Exception e) {
                    setNotFoundBackground();
                    matchesLabel.setText("Incorrect regular expression");
                    boldMatchInfo();
                    getSearchResult().clear();
                    return;
                }
            }

            FindManager findManager = getFindManager();
            if (allowedToChangedEditorSelection) {
                findManager.setFindWasPerformed();
                FindModel copy = new FindModel();
                copy.copyFrom(findModel);
                copy.setReplaceState(false);
                findManager.setFindNextModel(copy);
            }

            searchResultController.updateResult(findModel);
        }
    }

    private FindManager getFindManager() {
        Project project = searchableComponent.getTable().getProject();
        return FindManager.getInstance(project);
    }

    private void nothingToSearchFor() {
        updateUIWithEmptyResults();
        getSearchResult().clear();
    }

    private void updateUIWithEmptyResults() {
        setRegularBackground();
        matchesLabel.setText("");
    }

    private void boldMatchInfo() {
        matchesLabel.setFont(matchesLabel.getFont().deriveFont(Font.BOLD));
    }


    private void setNotFoundBackground() {
        searchField.setBackground(LightColors.RED);
    }

    private void setRegularBackground() {
        searchField.setBackground(UIUtil.getTextFieldBackground());
    }

    public String getTextInField() {
        return searchField.getText();
    }

    public void setTextInField(final String text) {
        searchField.setText(text);
        findModel.setStringToFind(text);
    }

    public boolean hasMatches() {
        return !getSearchResult().isEmpty();
    }

    private DataSearchResult getSearchResult() {
        return searchableComponent.getTable().getModel().getSearchResult();
    }

}
