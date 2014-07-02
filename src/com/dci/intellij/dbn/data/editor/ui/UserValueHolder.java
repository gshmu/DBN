package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.intellij.openapi.project.Project;

public interface UserValueHolder {
    void setUserValue(Object userValue);
    void updateUserValue(Object userValue, boolean bulk);
    TextContentType getContentType();
    void setContentType(TextContentType contentType);
    Object getUserValue();
    String getFormattedUserValue();
    String getName();
    Project getProject();
}
