package com.dci.intellij.dbn.editor.session.color;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.TextAttributesUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributes;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.SimpleTextAttributes;

import java.awt.*;

public class SessionBrowserTextAttributes extends CommonUtil implements DataGridTextAttributes {
    private final SimpleTextAttributes activeSession;
    private final SimpleTextAttributes inactiveSession;
    private final SimpleTextAttributes cachedSession;
    private final SimpleTextAttributes snipedSession;
    private final SimpleTextAttributes killedSession;
    private final SimpleTextAttributes selection;
    private final SimpleTextAttributes searchResult;
    private final SimpleTextAttributes activeSessionAtCaretRow;
    private final SimpleTextAttributes inactiveSessionAtCaretRow;
    private final SimpleTextAttributes cachedSessionAtCaretRow;
    private final SimpleTextAttributes snipedSessionAtCaretRow;
    private final SimpleTextAttributes killedSessionAtCaretRow;

    private final SimpleTextAttributes loadingData;
    private final SimpleTextAttributes loadingDataAtCaretRow;


    private final Color caretRowBgColor;

    private static final Latent<SessionBrowserTextAttributes> INSTANCE = Latent.laf(() -> new SessionBrowserTextAttributes());

    private SessionBrowserTextAttributes() {
        EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        caretRowBgColor = globalScheme.getAttributes(DataGridTextAttributesKeys.CARET_ROW).getBackgroundColor();

        activeSession = TextAttributesUtil.getSimpleTextAttributes(SessionBrowserTextAttributesKeys.ACTIVE_SESSION);
        inactiveSession = TextAttributesUtil.getSimpleTextAttributes(SessionBrowserTextAttributesKeys.INACTIVE_SESSION);
        cachedSession = TextAttributesUtil.getSimpleTextAttributes(SessionBrowserTextAttributesKeys.CACHED_SESSION);
        snipedSession = TextAttributesUtil.getSimpleTextAttributes(SessionBrowserTextAttributesKeys.SNIPED_SESSION);
        killedSession = TextAttributesUtil.getSimpleTextAttributes(SessionBrowserTextAttributesKeys.KILLED_SESSION);
        loadingData = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.LOADING_DATA);

        activeSessionAtCaretRow = new SimpleTextAttributes(caretRowBgColor, activeSession.getFgColor(), null, activeSession.getStyle());
        inactiveSessionAtCaretRow = new SimpleTextAttributes(caretRowBgColor, inactiveSession.getFgColor(), null, inactiveSession.getStyle());
        cachedSessionAtCaretRow = new SimpleTextAttributes(caretRowBgColor, cachedSession.getFgColor(), null, cachedSession.getStyle());
        snipedSessionAtCaretRow = new SimpleTextAttributes(caretRowBgColor, snipedSession.getFgColor(), null, snipedSession.getStyle());
        killedSessionAtCaretRow = new SimpleTextAttributes(caretRowBgColor, killedSession.getFgColor(), null, killedSession.getStyle());
        loadingDataAtCaretRow = new SimpleTextAttributes(caretRowBgColor, loadingData.getFgColor(), null, loadingData.getFontStyle());

        selection = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.SELECTION);
        searchResult = TextAttributesUtil.getSimpleTextAttributes(EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES);
    }

    public static SessionBrowserTextAttributes get() {
        return INSTANCE.get();
    }

    public SimpleTextAttributes getActiveSession(boolean atCaretRow) {
        return atCaretRow ? activeSessionAtCaretRow : activeSession;
    }

    public SimpleTextAttributes getInactiveSession(boolean atCaretRow) {
        return atCaretRow ? inactiveSessionAtCaretRow : inactiveSession;
    }

    public SimpleTextAttributes getCachedSession(boolean atCaretRow) {
        return atCaretRow ? cachedSessionAtCaretRow : cachedSession;
    }

    public SimpleTextAttributes getSnipedSession(boolean atCaretRow) {
        return atCaretRow ? snipedSessionAtCaretRow : snipedSession;
    }

    public SimpleTextAttributes getKilledSession(boolean atCaretRow) {
        return atCaretRow ? killedSessionAtCaretRow : killedSession;
    }

    @Override
    public SimpleTextAttributes getLoadingData(boolean atCaretRow) {
        return atCaretRow ? loadingDataAtCaretRow : loadingData;
    }

    @Override
    public SimpleTextAttributes getSelection() {
        return selection;
    }

    @Override
    public SimpleTextAttributes getSearchResult() {
        return searchResult;
    }

    @Override
    public Color getCaretRowBgColor() {
        return caretRowBgColor;
    }

    @Override
    public SimpleTextAttributes getPlainData(boolean modified, boolean atCaretRow) {
        return getActiveSession(atCaretRow);
    }
}
