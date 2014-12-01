package com.dci.intellij.dbn.common.environment;

import java.awt.Color;
import java.util.UUID;
import org.jdom.Element;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

public class EnvironmentType implements Cloneable, PersistentConfiguration {

    public interface EnvironmentColor {
/*        JBColor DEVELOPMENT = new JBColor(new Color(-2430209), new Color(0x445F80));
        JBColor INTEGRATION = new JBColor(new Color(-2621494), new Color(0x466646));
        JBColor PRODUCTION = new JBColor(new Color(-11574), new Color(0x634544));
        JBColor OTHER = new JBColor(new Color(-1576), new Color(0x5C5B41));*/
        JBColor NONE = new JBColor(new Color(0xffffff), Color.DARK_GRAY);
    }

    public static final EnvironmentType DEFAULT     = new EnvironmentType("default", "", "", null, null);
    public static final EnvironmentType DEVELOPMENT = new EnvironmentType("development", "Development", "Development environment", new Color(-2430209), new Color(0x445F80));
    public static final EnvironmentType INTEGRATION = new EnvironmentType("integration", "Integration", "Integration environment", new Color(-2621494), new Color(0x466646));
    public static final EnvironmentType PRODUCTION  = new EnvironmentType("production", "Production", "Productive environment", new Color(-11574), new Color(0x634544));
    public static final EnvironmentType OTHER       = new EnvironmentType("other", "Other", "", new Color(-1576), new Color(0x5C5B41));
    public static final EnvironmentType[] DEFAULT_ENVIRONMENT_TYPES = new EnvironmentType[] {
            DEVELOPMENT,
            INTEGRATION,
            PRODUCTION,
            OTHER};

    private String id;
    private String name;
    private String description;
    private Color regularColor;
    private Color darkColor;
    private JBColor color;

    public static EnvironmentType forName(String name) {
        for (EnvironmentType environmentType : DEFAULT_ENVIRONMENT_TYPES){
            if (environmentType.getName().equals(name)) {
                return environmentType;
            }
        }
        return null;
    }

    public EnvironmentType() {
        id = UUID.randomUUID().toString();
    }

    public EnvironmentType(String id, String name, String description, Color regularColor, Color darkColor) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.regularColor = regularColor;
        this.darkColor = darkColor;
        if (regularColor != null && darkColor != null) {
            this.color = new JBColor(regularColor, darkColor);
        }

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JBColor getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (UIUtil.isUnderDarcula()) {
            darkColor = color;
        } else {
            regularColor = color;
        }
        Color regularColor = CommonUtil.nvl(this.regularColor, Color.lightGray);
        Color darkColor = CommonUtil.nvl(this.darkColor, Color.darkGray);
        this.color = new JBColor(regularColor, darkColor);
    }

    public void setColor(JBColor color) {
        this.color = color;
    }

    public EnvironmentType clone() {
        return new EnvironmentType(id, name, description, regularColor, darkColor);
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnvironmentType that = (EnvironmentType) o;

        if (color != null ? !color.equals(that.color) : that.color != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (!id.equals(that.id)) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }

    @Override
    public void readConfiguration(Element element) {
        id = element.getAttributeValue("id");
        name = element.getAttributeValue("name");
        description = element.getAttributeValue("description");

        String value = element.getAttributeValue("color");
        if (StringUtil.isEmptyOrSpaces(value)) {
            if (regularColor != null && darkColor != null) {
                color = new JBColor(regularColor, darkColor);
            }
        } else {
            int index = value.indexOf("/");
            if (index > -1) {
                regularColor = new Color(Integer.parseInt(value.substring(0, index)));
                darkColor = new Color(Integer.parseInt(value.substring(index + 1)));
                color = new JBColor(regularColor, darkColor);
            }
        }

        EnvironmentType defaultEnvironmentType = forName(name);
        if (defaultEnvironmentType != null) {
            if (id == null) id = defaultEnvironmentType.getId();
        }
        if (id == null) id = name.toLowerCase();
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("id", id);
        element.setAttribute("name", name);
        element.setAttribute("description", description);
        if (regularColor != null && darkColor != null){
            String attributeValue = Integer.toString(regularColor.getRGB()) + "/" + Integer.toString(darkColor.getRGB());
            element.setAttribute("color", attributeValue);
        }
    }
}
