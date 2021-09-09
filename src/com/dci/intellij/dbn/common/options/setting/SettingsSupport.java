package com.dci.intellij.dbn.common.options.setting;

import com.dci.intellij.dbn.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.jetbrains.annotations.NotNull;

@Slf4j
public final class SettingsSupport {
    private SettingsSupport() {}

    public static String getString(Element parent, String childName, String defaultValue) {
        Element element = parent.getChild(childName);
        String stringValue = getStringValue(element);
        return stringValue == null ? defaultValue : stringValue;
    }

    public static int getInteger(Element parent, String childName, int defaultValue) {
        try {
            Element element = parent.getChild(childName);
            String stringValue = getStringValue(element);
            return stringValue == null ? defaultValue : Integer.parseInt(stringValue);
        } catch (Exception e) {
            log.warn("Failed to read INTEGER config (" + childName + "): " + e.getMessage());
            return defaultValue;
        }
    }

    public static double getDouble(Element parent, String childName, double defaultValue) {
        try {
            Element element = parent.getChild(childName);
            String stringValue = getStringValue(element);
            return stringValue == null ? defaultValue : Double.parseDouble(stringValue);
        } catch (Exception e){
            log.warn("Failed to read DOUBLE config (" + childName + "): " + e.getMessage());
            return defaultValue;
        }
    }

    public static boolean getBoolean(Element parent, String childName, boolean defaultValue) {
        Element element = parent.getChild(childName);
        String stringValue = getStringValue(element);
        return stringValue == null ? defaultValue : Boolean.parseBoolean(stringValue);
    }

    public static <T extends Enum<T>> T getEnum(Element parent, String childName, @NotNull T defaultValue) {
        try {
            Element element = parent.getChild(childName);
            String stringValue = getStringValue(element);
            return stringValue == null ? defaultValue : (T) T.valueOf(defaultValue.getClass(), stringValue);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to read ENUM config (" + childName + "): " + e.getMessage());
            return defaultValue;
        }
    }

    public static String getStringValue(Element element) {
        if (element != null) {
            String value = element.getAttributeValue("value");
            if (StringUtil.isNotEmptyOrSpaces(value)) {
                return value;
            }
        }
        return null;
    }

    public static boolean getBooleanAttribute(Element element, String attributeName, boolean value) {
        String attributeValue = element.getAttributeValue(attributeName);
        return StringUtil.isEmptyOrSpaces(attributeValue) ? value : Boolean.parseBoolean(attributeValue);
    }

    public static short getShortAttribute(Element element, String attributeName, short defaultValue) {
        try {
            String attributeValue = element.getAttributeValue(attributeName);
            if (StringUtil.isEmpty(attributeValue)) {
                return defaultValue;
            }
            return Short.parseShort(attributeValue);
        } catch (Exception e) {
            log.warn("Failed to read SHORT config (" + attributeName + "): " + e.getMessage());
            return defaultValue;
        }
    }

    public static int getIntegerAttribute(Element element, String attributeName, int defaultValue) {
        try {
            String attributeValue = element.getAttributeValue(attributeName);
            if (StringUtil.isEmpty(attributeValue)) {
                return defaultValue;
            }
            return Integer.parseInt(attributeValue);
        } catch (NumberFormatException e) {
            log.warn("Failed to read INTEGER config (" + attributeName + "): " + e.getMessage());
            return defaultValue;
        }
    }

    /*
        public static <T extends Enum<T>> T getEnumAttribute(Element element, String attributeName, T value) {
            String attributeValue = element.getAttributeValue(attributeName);
            Class<T> enumClass = (Class<T>) value.getClass();
            return StringUtil.isEmpty(attributeValue) ? value : T.valueOf(enumClass, attributeValue);
        }
    */

    public static  <T extends Enum<T>> T getEnumAttribute(Element element, String attributeName, Class<T> enumClass) {
        try {
            String attributeValue = element.getAttributeValue(attributeName);
            return StringUtil.isEmpty(attributeValue) ? null : T.valueOf(enumClass, attributeValue);
        } catch (Exception e) {
            log.warn("Failed to read ENUM config (" + attributeName + "): " + e.getMessage());
            return null;
        }
    }

    public static  <T extends Enum<T>> T getEnumAttribute(Element element, String attributeName, @NotNull T defaultValue) {
        try {
            String attributeValue = element.getAttributeValue(attributeName);
            return StringUtil.isEmpty(attributeValue) ? defaultValue : T.valueOf((Class<T>) defaultValue.getClass(), attributeValue);
        } catch (Exception e) {
            log.warn("Failed to read ENUM config (" + attributeName + "): " + e.getMessage());
            return defaultValue;
        }
    }

    public static String readCdata(Element parent) {
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


    public static void setInteger(Element parent, String childName, int value) {
        Element element = new Element(childName);
        element.setAttribute("value", Integer.toString(value));
        parent.addContent(element);
    }

    public static void setString(Element parent, String childName, String value) {
        Element element = new Element(childName);
        element.setAttribute("value", value == null ? "" : value);
        parent.addContent(element);
    }

    public static void setDouble(Element parent, String childName, double value) {
        Element element = new Element(childName);
        element.setAttribute("value", Double.toString(value));
        parent.addContent(element);
    }

    public static void setBoolean(Element parent, String childName, boolean value) {
        Element element = new Element(childName);
        element.setAttribute("value", Boolean.toString(value));
        parent.addContent(element);
    }

    public static  <T extends Enum<T>> void setEnum(Element parent, String childName, T value) {
        Element element = new Element(childName);
        element.setAttribute("value",value.name());
        parent.addContent(element);
    }

    public static void setBooleanAttribute(Element element, String attributeName, boolean value) {
        element.setAttribute(attributeName, Boolean.toString(value));
    }

    public static void setIntegerAttribute(Element element, String attributeName, int value) {
        element.setAttribute(attributeName, Integer.toString(value));
    }

    public static void setStringAttribute(Element element, String attributeName, String value) {
        element.setAttribute(attributeName, value == null ? "" : value);
    }

    public static  <T extends Enum<T>> void setEnumAttribute(Element element, String attributeName, T value) {
        element.setAttribute(attributeName, value.name());
    }
}
