package com.dci.intellij.dbn.diagnostics.data;


import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import static com.dci.intellij.dbn.common.util.Commons.nvl;


@Getter
public class ParserDiagnosticsResult implements PersistentStateElement, Comparable<ParserDiagnosticsResult> {

    private final Map<String, Integer> entries = new TreeMap<>();
    private final ProjectRef project;

    private String id = UUID.randomUUID().toString();
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private int index;
    private boolean draft = true;
    private int errorCount;

    public ParserDiagnosticsResult(@NotNull Project project) {
        this.project = ProjectRef.of(project);
    }

    public ParserDiagnosticsResult(@NotNull Project project, Element element) {
        this(project);
        readState(element);
    }

    public ParserDiagnosticsDeltaResult delta(@Nullable ParserDiagnosticsResult previous) {
        return new ParserDiagnosticsDeltaResult(previous, this);
    }

    public void addEntry(String file, int errorCount) {
        entries.put(file, errorCount);
        this.errorCount += errorCount;
    }

    public Set<String> getFiles() {
        return entries.keySet();
    }

    public int getErrorCount(String file) {
        return nvl(entries.get(file), 0);
    }

    public boolean isPresent(String file) {
        return entries.containsKey(file);
    }

    public void markSaved() {
        this.draft = false;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    public String getName() {
        Formatter formatter = RegionalSettings.getInstance(getProject()).getBaseFormatter();
        return "Result " + index + " (" + formatter.formatDateTime(timestamp) + ")" + (draft ? " - draft" : "") + "";
    }

    @Override
    public void readState(Element element) {
        if (element != null) {
            id = element.getAttributeValue("id");
            draft = false;
            timestamp = Timestamp.valueOf(element.getAttributeValue("timestamp"));
            List<Element> children = element.getChildren();
            for (Element child : children) {
                String filePath = child.getAttributeValue("path");
                int errorCount = SettingsSupport.integerAttribute(child, "error-count", 0);
                addEntry(filePath, errorCount);
            }
        }
    }

    @Override
    public void writeState(Element element) {
        element.setAttribute("id", id);
        element.setAttribute("timestamp", timestamp.toString());
        for (String filePath : entries.keySet()) {
            Integer errorCount = entries.get(filePath);

            Element child = new Element("file");
            child.setAttribute("path", filePath);
            SettingsSupport.setIntegerAttribute(child, "error-count", errorCount);
            element.addContent(child);

        }
    }

    @Override
    public int compareTo(@NotNull ParserDiagnosticsResult o) {
        return o.getTimestamp().compareTo(this.getTimestamp());
    }
}
