package com.dci.intellij.dbn.editor.data.options;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.editor.data.options.ui.DataEditorQualifiedEditorSettingsForm;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public class DataEditorQualifiedEditorSettings extends Configuration<DataEditorQualifiedEditorSettingsForm> {
    private List<TextContentType> contentTypes;
    private int textLengthThreshold = 300;

    public String getDisplayName() {
        return "Data editor text content type settings";
    }

    public String getHelpTopic() {
        return "dataEditor";
    }

    public synchronized List<TextContentType> getContentTypes() {
        if (contentTypes == null) {
            contentTypes = new ArrayList<TextContentType>();
            createContentType("Text", "PLAIN_TEXT");
            createContentType("Properties", "Properties");
            createContentType("XML", "XML");
            createContentType("DTD", "DTD");
            createContentType("HTML", "HTML");
            createContentType("XHTML", "XHTML");
            createContentType("CSS", "CSS");
            createContentType("Java", "JAVA");
            createContentType("SQL", "DBN-SQL");
            createContentType("PL/SQL", "DBN-PSQL");
            createContentType("JPA QL", "JPA QL");
            createContentType("JavaScript", "JavaScript");
            createContentType("PHP", "PHP");
            createContentType("JSP", "JSP");
            createContentType("JSPx", "JSPX");
            createContentType("Perl", "Perl");
            createContentType("Groovy", "Groovy");
            createContentType("FTL", "FTL");
            createContentType("TML", "TML");
            createContentType("GSP", "GSP");
            createContentType("ASP", "ASP");
            createContentType("VTL", "VTL");
            createContentType("AIDL", "AIDL");
            createContentType("YAML", "YAML");
            createContentType("Flex", "SWF");
            createContentType("C#", "C#");
            createContentType("C++", "C++");
            createContentType("Bash", "Bash");
            createContentType("Manifest", "Manifest");
        }
        return contentTypes;
    }

    public TextContentType getPlainTextContentType() {
        return getContentType("Text");
    }

    private void createContentType(String name, String fileTypeName) {
        TextContentType contentType = TextContentType.create(name, fileTypeName);
        if (contentType != null) {
            contentTypes.add(contentType);
        }
    }

    public TextContentType getContentType(String name) {
        for (TextContentType contentType : getContentTypes()) {
            if (contentType.getName().equals(name)) {
                return contentType;
            }
        }
        return null;
    }

    public int getTextLengthThreshold() {
        return textLengthThreshold;
    }

    public void setTextLengthThreshold(int textLengthThreshold) {
        this.textLengthThreshold = textLengthThreshold;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    public DataEditorQualifiedEditorSettingsForm createConfigurationEditor() {
        return new DataEditorQualifiedEditorSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "qualified-text-editor";
    }

    public void readConfiguration(Element element) {
        textLengthThreshold = SettingsUtil.getIntegerAttribute(element, "text-length-threshold", textLengthThreshold);
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

    public void writeConfiguration(Element element) {
        SettingsUtil.setIntegerAttribute(element, "text-length-threshold", textLengthThreshold);
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
