package com.dci.intellij.dbn.data.grid.color;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.util.TextAttributesUtil;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;

import java.awt.Color;

import static com.dci.intellij.dbn.common.util.CommonUtil.nvln;

public class BasicTableTextAttributes implements DataGridTextAttributes {
    private final SimpleTextAttributes plainData;
    private final SimpleTextAttributes plainDataModified;
    private final SimpleTextAttributes plainDataAtCaretRow;
    private final SimpleTextAttributes plainDataAtCaretRowModified;
    private final SimpleTextAttributes trackingData;
    private final SimpleTextAttributes trackingDataModified;
    private final SimpleTextAttributes trackingDataAtCaretRow;
    private final SimpleTextAttributes trackingDataAtCaretRowModified;
    private final SimpleTextAttributes modifiedData;
    private final SimpleTextAttributes modifiedDataAtCaretRow;
    private final SimpleTextAttributes deletedData;
    private final SimpleTextAttributes errorData;
    private final SimpleTextAttributes readonlyData;
    private final SimpleTextAttributes readonlyDataModified;
    private final SimpleTextAttributes readonlyDataAtCaretRow;
    private final SimpleTextAttributes readonlyDataAtCaretRowModified;
    private final SimpleTextAttributes loadingData;
    private final SimpleTextAttributes loadingDataAtCaretRow;
    private final SimpleTextAttributes primaryKey;
    private final SimpleTextAttributes primaryKeyModified;
    private final SimpleTextAttributes primaryKeyAtCaretRow;
    private final SimpleTextAttributes primaryKeyAtCaretRowModified;
    private final SimpleTextAttributes foreignKey;
    private final SimpleTextAttributes foreignKeyModified;
    private final SimpleTextAttributes foreignKeyAtCaretRow;
    private final SimpleTextAttributes foreignKeyAtCaretRowModified;
    private final SimpleTextAttributes selection;
    private final SimpleTextAttributes searchResult;

    private final Color caretRowBgColor;

    private static final Latent<BasicTableTextAttributes> INSTANCE = Latent.laf(() -> new BasicTableTextAttributes());

    private BasicTableTextAttributes() {
        EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        caretRowBgColor = globalScheme.getAttributes(DataGridTextAttributesKeys.CARET_ROW).getBackgroundColor();

        deletedData = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.DELETED_DATA);
        errorData = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.ERROR_DATA);
        modifiedData = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.MODIFIED_DATA);
        modifiedDataAtCaretRow = new SimpleTextAttributes(caretRowBgColor, modifiedData.getFgColor(), null, modifiedData.getFontStyle());

        plainData = createPlainData();

        plainDataModified = new SimpleTextAttributes(
                nvln(modifiedData.getBgColor(), plainData.getBgColor()),
                nvln(modifiedData.getFgColor(), plainData.getFgColor()), null,
                modifiedData.getFontStyle());
        plainDataAtCaretRow = new SimpleTextAttributes(caretRowBgColor, plainData.getFgColor(), null, plainData.getFontStyle());
        plainDataAtCaretRowModified = new SimpleTextAttributes(
                caretRowBgColor,
                nvln(modifiedData.getFgColor(), plainData.getFgColor()), null,
                modifiedData.getFontStyle());


        trackingData = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.TRACKING_DATA);
        trackingDataModified = new SimpleTextAttributes(
                nvln(modifiedData.getBgColor(), trackingData.getBgColor()),
                nvln(modifiedData.getFgColor(), plainData.getFgColor()), null,
                modifiedData.getFontStyle());
        trackingDataAtCaretRow = new SimpleTextAttributes(caretRowBgColor, trackingData.getFgColor(), null, trackingData.getFontStyle());
        trackingDataAtCaretRowModified = new SimpleTextAttributes(
                caretRowBgColor,
                nvln(modifiedData.getFgColor(), plainData.getFgColor()), null,
                modifiedData.getFontStyle());


        readonlyData = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.READONLY_DATA);
        readonlyDataModified = new SimpleTextAttributes(
                nvln(modifiedData.getBgColor(), readonlyData.getBgColor()),
                nvln(modifiedData.getFgColor(), readonlyData.getFgColor()), null,
                modifiedData.getFontStyle());
        readonlyDataAtCaretRow = new SimpleTextAttributes(caretRowBgColor, readonlyData.getFgColor(), null, readonlyData.getFontStyle());
        readonlyDataAtCaretRowModified = new SimpleTextAttributes(
                caretRowBgColor,
                nvln(modifiedData.getFgColor(), readonlyData.getFgColor()), null,
                modifiedData.getFontStyle());

        loadingData = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.LOADING_DATA);
        loadingDataAtCaretRow = new SimpleTextAttributes(caretRowBgColor, loadingData.getFgColor(), null, loadingData.getFontStyle());

        primaryKey= TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.PRIMARY_KEY);
        primaryKeyModified = new SimpleTextAttributes(
                nvln(modifiedData.getBgColor(), primaryKey.getBgColor()),
                nvln(modifiedData.getFgColor(), primaryKey.getFgColor()), null,
                modifiedData.getStyle());
        primaryKeyAtCaretRow = new SimpleTextAttributes(caretRowBgColor, primaryKey.getFgColor(), null, primaryKey.getStyle());
        primaryKeyAtCaretRowModified = new SimpleTextAttributes(
                caretRowBgColor,
                nvln(modifiedData.getFgColor(), primaryKey.getFgColor()), null,
                modifiedData.getStyle());

        foreignKey = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.FOREIGN_KEY);
        foreignKeyModified = new SimpleTextAttributes(
                nvln(modifiedData.getBgColor(), foreignKey.getBgColor()),
                nvln(modifiedData.getFgColor(), foreignKey.getFgColor()), null,
                modifiedData.getStyle());
        foreignKeyAtCaretRow = new SimpleTextAttributes(caretRowBgColor, foreignKey.getFgColor(), null, foreignKey.getStyle());
        foreignKeyAtCaretRowModified = new SimpleTextAttributes(
                caretRowBgColor,
                nvln(modifiedData.getFgColor(), foreignKey.getFgColor()), null,
                modifiedData.getStyle());

        selection = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.SELECTION);
        searchResult = TextAttributesUtil.getSimpleTextAttributes(EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES);
    }

    private SimpleTextAttributes createPlainData() {
        SimpleTextAttributes plainData = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.PLAIN_DATA);
        if (plainData.getFgColor() == null) plainData = plainData.derive(plainData.getStyle(), UIUtil.getTextFieldForeground(), plainData.getBgColor(), null);
        if (plainData.getBgColor() == null) plainData = plainData.derive(plainData.getStyle(), plainData.getFgColor(), UIUtil.getTextFieldBackground(), null);
        return plainData;
    }

    public static BasicTableTextAttributes get() {
        return INSTANCE.get();
    }

    @Override
    public SimpleTextAttributes getPlainData(boolean modified, boolean atCaretRow) {
        return modified && atCaretRow ? plainDataAtCaretRowModified :
                atCaretRow ? plainDataAtCaretRow :
                modified ? plainDataModified : plainData;
    }

    public SimpleTextAttributes getModifiedData(boolean atCaretRow) {
        return atCaretRow ? modifiedDataAtCaretRow : modifiedData;
    }

    public SimpleTextAttributes getDeletedData() {
        return deletedData;
    }

    public SimpleTextAttributes getErrorData() {
        return errorData;
    }

    public SimpleTextAttributes getReadonlyData(boolean modified, boolean atCaretRow) {
        return
            modified && atCaretRow ? readonlyDataAtCaretRowModified :
            atCaretRow ? readonlyDataAtCaretRow :
            modified ? readonlyDataModified : readonlyData;
    }

    @Override
    public SimpleTextAttributes getLoadingData(boolean atCaretRow) {
        return atCaretRow ? loadingDataAtCaretRow : loadingData;
    }

    public SimpleTextAttributes getPrimaryKey(boolean modified, boolean atCaretRow) {
        return
            modified && atCaretRow ? primaryKeyAtCaretRowModified :
            atCaretRow ? primaryKeyAtCaretRow :
            modified ? primaryKeyModified : primaryKey;
    }

    public SimpleTextAttributes getForeignKey(boolean modified, boolean atCaretRow) {
        return
            modified && atCaretRow ? foreignKeyAtCaretRowModified :
            atCaretRow ? foreignKeyAtCaretRow :
            modified ? foreignKeyModified : foreignKey;
    }

    public SimpleTextAttributes getTrackingData(boolean modified, boolean atCaretRow) {
        return
            modified && atCaretRow ? trackingDataAtCaretRowModified :
            atCaretRow ? trackingDataAtCaretRow :
            modified ? trackingDataModified : trackingData;
    }

    public SimpleTextAttributes getPrimaryKeyAtCaretRow() {
        return primaryKeyAtCaretRow;
    }

    public SimpleTextAttributes getPrimaryKeyModified() {
        return primaryKeyModified;
    }

    public SimpleTextAttributes getPrimaryKeyAtCaretRowModified() {
        return primaryKeyAtCaretRowModified;
    }

//    }

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
}
