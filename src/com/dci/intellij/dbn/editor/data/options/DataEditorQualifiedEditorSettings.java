package com.dci.intellij.dbn.editor.data.options;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.editor.data.options.ui.DataEditorQualifiedEditorSettingsForm;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DataEditorQualifiedEditorSettings extends BasicConfiguration<DataEditorSettings, DataEditorQualifiedEditorSettingsForm> {
    private final Latent<List<TextContentType>> contentTypes = Latent.basic(() -> {
        List<TextContentType> contentTypes = new ArrayList<>();
        createContentType("Text", "PLAIN_TEXT", contentTypes);
        createContentType("Properties", "Properties", contentTypes);
        createContentType("XML", "XML", contentTypes);
        createContentType("DTD", "DTD", contentTypes);
        createContentType("HTML", "HTML", contentTypes);
        createContentType("XHTML", "XHTML", contentTypes);
        createContentType("CSS", "CSS", contentTypes);
        createContentType("Java", "JAVA", contentTypes);
        createContentType("SQL", "DBN-SQL", contentTypes);
        createContentType("PL/SQL", "DBN-PSQL", contentTypes);
        createContentType("JPA QL", "JPA QL", contentTypes);
        createContentType("JavaScript", "JavaScript", contentTypes);
        createContentType("JSON", "JSON", contentTypes);
        createContentType("JSON5", "JSON5", contentTypes);
        createContentType("PHP", "PHP", contentTypes);
        createContentType("JSP", "JSP", contentTypes);
        createContentType("JSPx", "JSPX", contentTypes);
        createContentType("Perl", "Perl", contentTypes);
        createContentType("Groovy", "Groovy", contentTypes);
        createContentType("FTL", "FTL", contentTypes);
        createContentType("TML", "TML", contentTypes);
        createContentType("GSP", "GSP", contentTypes);
        createContentType("ASP", "ASP", contentTypes);
        createContentType("VTL", "VTL", contentTypes);
        createContentType("AIDL", "AIDL", contentTypes);
        createContentType("YAML", "YAML", contentTypes);
        createContentType("Flex", "SWF", contentTypes);
        createContentType("C#", "C#", contentTypes);
        createContentType("C++", "C++", contentTypes);
        createContentType("Bash", "Bash", contentTypes);
        createContentType("Manifest", "Manifest", contentTypes);
        return contentTypes;
    });

    private int textLengthThreshold = 300;

    DataEditorQualifiedEditorSettings(DataEditorSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return "Data editor text content type settings";
    }

    @Override
    public String getHelpTopic() {
        return "dataEditor";
    }

    public List<TextContentType> getContentTypes() {
        return contentTypes.get();
    }

    private void createContentType(String name, String fileTypeName, List<TextContentType> contentTypes) {
        TextContentType contentType = TextContentType.create(name, fileTypeName);
        if (contentType != null) {
            contentTypes.add(contentType);
        }
    }

    @Nullable
    public TextContentType getContentType(String name) {
        if (StringUtil.isNotEmpty(name)) {
            for (TextContentType contentType : getContentTypes()) {
                if (contentType.getName().equals(name)) {
                    return contentType;
                }
            }
        }
        return null;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public DataEditorQualifiedEditorSettingsForm createConfigurationEditor() {
        return new DataEditorQualifiedEditorSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "qualified-text-editor";
    }

    @Override
    public void readConfiguration(Element element) {
        textLengthThreshold = SettingsSupport.getIntegerAttribute(element, "text-length-threshold", textLengthThreshold);
        Element contentTypes = element.getChild("content-types");
        for (Object o : contentTypes.getChildren()) {
            Element child = (Element) o;
            String name = child.getAttributeValue("name");
            TextContentType contentType = getContentType(name);
            if (contentType != null) {
                boolean enabled = Boolean.parseBoolean(child.getAttributeValue("enabled"));
                contentType.setSelected(enabled);
            }
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsSupport.setIntegerAttribute(element, "text-length-threshold", textLengthThreshold);
        Element contentTypes = new Element("content-types");
        element.addContent(contentTypes);
        for (TextContentType contentType : getContentTypes()) {
            Element child = new Element("content-type");
            child.setAttribute("name", contentType.getName());
            child.setAttribute("enabled", Boolean.toString(contentType.isSelected()));
            contentTypes.addContent(child);
        }
    }
}
