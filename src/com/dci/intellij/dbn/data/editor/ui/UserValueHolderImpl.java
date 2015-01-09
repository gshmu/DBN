package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.intellij.openapi.project.Project;

public class UserValueHolderImpl<T> implements UserValueHolder<T>{
    private String name;
    private Project project;
    private T userValue;
    private TextContentType contentType;

    public UserValueHolderImpl(String name, Project project) {
        this.name = name;
        this.project = project;
    }

    public T getUserValue() {
        return userValue;
    }

    @Override
    public String getFormattedUserValue() {
        throw new UnsupportedOperationException();
    }

    public void setUserValue(T userValue) {
        this.userValue = userValue;
    }

    public void updateUserValue(T userValue, boolean bulk) {
        this.userValue = userValue;
    }

    public TextContentType getContentType() {
        return contentType;
    }

    public void setContentType(TextContentType contentType) {
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public Project getProject() {
        return project;
    }
}
