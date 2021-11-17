package com.dci.intellij.dbn.diagnostics.data;


import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
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

import static com.dci.intellij.dbn.common.util.CommonUtil.nvl;


@Getter
public class ParserDiagnosticsResult implements PersistentStateElement<ParserDiagnosticsResult>, Comparable<ParserDiagnosticsResult> {

    private String id = UUID.randomUUID().toString();
    private boolean saved;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private final Map<String, Integer> entries = new TreeMap<>();

    public ParserDiagnosticsResult() {}

    public ParserDiagnosticsResult(Element element) {
        readState(element);
    }

    public ParserDiagnosticsDeltaResult delta(@Nullable ParserDiagnosticsResult previous) {
        ParserDiagnosticsDeltaResult deltaResult = new ParserDiagnosticsDeltaResult();

        this.entries.keySet().forEach(file ->
                deltaResult.addEntry(file,
                        this.getErrorCount(file),
                        previous == null ?
                                this.getErrorCount(file) :
                                previous.getErrorCount(file)));

        if (previous != null) {
            previous.getFiles().stream().filter(file -> !this.isPresent(file)).forEach(file ->
                    deltaResult.addEntry(file, 0,previous.getErrorCount(file)));
        }
        return deltaResult;
    }

    public void addEntry(String file, int errorCount) {
        entries.put(file, errorCount);
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
        this.saved = true;
    }

    @Override
    public void readState(Element element) {
        if (element != null) {
            id = element.getAttributeValue("id");
            saved = true;
            timestamp = Timestamp.valueOf(element.getAttributeValue("timestamp"));
            List<Element> children = element.getChildren();
            for (Element child : children) {
                String file = child.getAttributeValue("file");
                int count = SettingsSupport.integerAttribute(child, "error-count", 0);
                entries.put(file, count);
            }
        }
    }

    @Override
    public void writeState(Element element) {
        element.setAttribute("id", id);
        element.setAttribute("timestamp", timestamp.toString());
        for (String file : entries.keySet()) {
            Integer count = entries.get(file);

            Element child = new Element("files");
            child.setAttribute("file", file);
            SettingsSupport.setIntegerAttribute(child, "error-count", count);
            element.addContent(child);

        }
    }

    @Override
    public int compareTo(@NotNull ParserDiagnosticsResult o) {
        return o.getTimestamp().compareTo(this.getTimestamp());
    }
}
