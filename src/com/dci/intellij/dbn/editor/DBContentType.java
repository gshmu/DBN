package com.dci.intellij.dbn.editor;

import com.dci.intellij.dbn.common.util.EnumerationUtil;
import com.dci.intellij.dbn.object.type.DBObjectType;

public enum DBContentType {
    NONE("No Content"),
    DATA("Data", EditorProviderId.DATA),

    CODE("Code", EditorProviderId.CODE),
    CODE_SPEC("Code Spec", EditorProviderId.CODE_SPEC),
    CODE_BODY("Code Body", "BODY", EditorProviderId.CODE_BODY),
    CODE_SPEC_AND_BODY("Code Spec and Body", new DBContentType[]{CODE_SPEC, CODE_BODY}),
    CODE_AND_DATA("Code and Data", new DBContentType[]{CODE, DATA});

    private DBContentType[] subContentTypes = new DBContentType[0];
    private String description;
    private String objectTypeSubname;
    private EditorProviderId editorProviderId;

    private DBContentType(String description, DBContentType[] subContentTypes) {
        this.description = description;
        this.subContentTypes = subContentTypes;
    }

    private DBContentType(String description) {
        this.description = description;
    }

    private DBContentType(String description, EditorProviderId editorProviderId) {
        this.description = description;
        this.editorProviderId = editorProviderId;
    }

    DBContentType(String description, String objectTypeSubname, EditorProviderId editorProviderId) {
        this.description = description;
        this.objectTypeSubname = objectTypeSubname;
        this.editorProviderId = editorProviderId;
    }

    public DBContentType[] getSubContentTypes() {
        return subContentTypes;
    }

    public EditorProviderId getEditorProviderId() {
        return editorProviderId;
    }

    public boolean isBundle() {
        return subContentTypes.length > 0;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public boolean isCode() {
        return this == CODE || this == CODE_SPEC || this == CODE_BODY || this == CODE_SPEC_AND_BODY;
    }

    public boolean isData() {
        return this == DATA; 
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return description;
    }

    public String getObjectTypeSubname() {
        return objectTypeSubname;
    }
    
    public boolean isOneOf(DBContentType ... contentTypes){
        return EnumerationUtil.isOneOf(this, contentTypes);
    }

    public static DBContentType get(DBObjectType objectType) {
        switch (objectType) {
            case FUNCTION:
            case PROCEDURE:
            case TRIGGER:
            case DATASET_TRIGGER:
            case DATABASE_TRIGGER: return CODE;
            case PACKAGE:
            case TYPE: return CODE_SPEC_AND_BODY;
            case VIEW:
            case MATERIALIZED_VIEW: return CODE_AND_DATA;
            case TABLE: return DATA;
            default: return NONE;
        }
    }

    public boolean has(DBContentType contentType) {
        switch (contentType) {
            case DATA: return this == DATA || this == CODE_AND_DATA;
            case CODE: return this == CODE || this == CODE_AND_DATA || this == CODE_SPEC_AND_BODY;
            default:   return false;
        }
    }
}
