package com.dci.intellij.dbn.common.locale;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.LazyThreadLocal;
import com.intellij.openapi.project.Project;

public class FormatterProvider extends LazyThreadLocal<Formatter> {
    private Project project;

    public FormatterProvider(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    protected Formatter load() {
        return Formatter.getInstance(project).clone();
    }
}
