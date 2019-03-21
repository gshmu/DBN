package com.dci.intellij.dbn.common.options.setting;

import com.dci.intellij.dbn.common.util.StringUtil;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.jetbrains.annotations.NotNull;

public interface SettingsSupport {
    static int getInteger(Element parent, String childName, int originalValue) {
        Element element = parent.getChild(childName);
        String stringValue = getStringValue(element);
        return stringValue == null ? originalValue : Integer.parseInt(stringValue);
    }

    static String getString(Element parent, String childName, String originalValue) {
        Element element = parent.getChild(childName);
        String stringValue = getStringValue(element);
        return stringValue == null ? originalValue : stringValue;
    }

    static double getDouble(Element parent, String childName, double originalValue) {
        Element element = parent.getChild(childName);
        String stringValue = getStringValue(element);
        return stringValue == null ? originalValue : Double.parseDouble(stringValue);
    }

    static boolean getBoolean(Element parent, String childName, boolean originalValue) {
        Element element = parent.getChild(childName);
        String stringValue = getStringValue(element);
        return stringValue == null ? originalValue : Boolean.parseBoolean(stringValue);
    }

    static <T extends Enum> T getEnum(Element parent, String childName, T originalValue) {
        Element element = parent.getChild(childName);
        String stringValue = getStringValue(element);
        return stringValue == null ? originalValue : (T) T.valueOf(originalValue.getClass(), stringValue);
    }

    static String readCdata(Element parent) {
        StringBuilder builder = new StringBuilder();
        int contentSize = parent.getContentSize();
        for (int i=0; i<contentSize; i++) {
            Content content = parent.getContent(i);
            if (content instanceof Text) {
                Text cdata = (Text) content;
                builder.append(cdata.getText());
            }
        }
        return builder.toString();
    }

    static String getStringValue(Element element) {
        if (element != null) {
            String value = element.getAttributeValue("value");
            if (StringUtil.isNotEmptyOrSpaces(value)) {
                return value;
            }
        }
        return null;
    }


    static void setInteger(Element parent, String childName, int value) {
        Element element = new Element(childName);
        element.setAttribute("value", Integer.toString(value));
        parent.addContent(element);
    }

    static void setString(Element parent, String childName, String value) {
        Element element = new Element(childName);
        element.setAttribute("value", value == null ? "" : value);
        parent.addContent(element);
    }

    static void setDouble(Element parent, String childName, double value) {
        Element element = new Element(childName);
        element.setAttribute("value", Double.toString(value));
        parent.addContent(element);
    }

    static void setBoolean(Element parent, String childName, boolean value) {
        Element element = new Element(childName);
        element.setAttribute("value", Boolean.toString(value));
        parent.addContent(element);
    }

    static  <T extends Enum> void setEnum(Element parent, String childName, T value) {
        Element element = new Element(childName);
        element.setAttribute("value",value.name());
        parent.addContent(element);
    }

    static boolean getBooleanAttribute(Element element, String attributeName, boolean staticValue) {
        String attributeValue = element.getAttributeValue(attributeName);
        return StringUtil.isEmptyOrSpaces(attributeValue) ? staticValue : Boolean.parseBoolean(attributeValue);
    }

    static void setBooleanAttribute(Element element, String attributeName, boolean value) {
        element.setAttribute(attributeName, Boolean.toString(value));
    }

    static int getIntegerAttribute(Element element, String attributeName, int staticValue) {
        String attributeValue = element.getAttributeValue(attributeName);
        if (attributeValue == null || attributeValue.trim().length() == 0) {
            return staticValue;
        }
        return Integer.parseInt(attributeValue);
    }

    static void setIntegerAttribute(Element element, String attributeName, int value) {
        element.setAttribute(attributeName, Integer.toString(value));
    }

    static void setStringAttribute(Element element, String attributeName, String value) {
        element.setAttribute(attributeName, value == null ? "" : value);
    }


/*
    public static <T extends Enum<T>> T getEnumAttribute(Element element, String attributeName, T staticValue) {
        String attributeValue = element.getAttributeValue(attributeName);
        Class<T> enumClass = (Class<T>) staticValue.getClass();
        return StringUtil.isEmpty(attributeValue) ? staticValue : T.valueOf(enumClass, attributeValue);
    }

*/
    static  <T extends Enum<T>> T getEnumAttribute(Element element, String attributeName, Class<T> enumClass) {
        String attributeValue = element.getAttributeValue(attributeName);
        return StringUtil.isEmpty(attributeValue) ? null : T.valueOf(enumClass, attributeValue);
    }

    static  <T extends Enum<T>> T getEnumAttribute(Element element, String attributeName, @NotNull T staticValue) {
        String attributeValue = element.getAttributeValue(attributeName);
        return StringUtil.isEmpty(attributeValue) ? staticValue : T.valueOf((Class<T>) staticValue.getClass(), attributeValue);
    }

    static  <T extends Enum<T>> void setEnumAttribute(Element element, String attributeName, T value) {
        element.setAttribute(attributeName, value.name());
    }
}
