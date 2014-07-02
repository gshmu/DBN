package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.intellij.openapi.project.Project;

public class UserValueHolderImpl implements UserValueHolder{
    private String name;
    private Project project;
    private Object userValue;

    public UserValueHolderImpl(String name, Project project) {
        this.name = name;
        this.project = project;
    }

    public Object getUserValue() {
        return userValue;
    }

    @Override
    public String getFormattedUserValue() {
        throw new UnsupportedOperationException();
    }

    public void setUserValue(Object userValue) {
        this.userValue = userValue;
    }

    public void updateUserValue(Object userValue, boolean bulk) {
        this.userValue = userValue;
    }

    public TextContentType getContentType() {
        return null;
    }

    public void setContentType(TextContentType contentType) {
    }

    public String getName() {
        return name;
    }

    public Project getProject() {
        return project;
    }
}
