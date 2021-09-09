package com.dci.intellij.dbn.common.environment;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.ColorIcon;
import com.intellij.util.ui.UIUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.Color;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getBooleanAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setBooleanAttribute;

@Getter
@Setter
@EqualsAndHashCode
public class EnvironmentType implements Cloneable<EnvironmentType>, PersistentConfiguration, Presentable {

    private static final Color DEFAULT_REGULAR_COLOR = Color.LIGHT_GRAY;
    private static final Color DEFAULT_DARK_COLOR = Color.DARK_GRAY;

    public interface EnvironmentColor {
/*        JBColor DEVELOPMENT = new JBColor(new Color(-2430209), new Color(0x445F80));
        JBColor INTEGRATION = new JBColor(new Color(-2621494), new Color(0x466646));
        JBColor PRODUCTION = new JBColor(new Color(-11574), new Color(0x634544));
        JBColor OTHER = new JBColor(new Color(-1576), new Color(0x5C5B41));*/
        JBColor NONE = new JBColor(new Color(0xffffff), Color.DARK_GRAY);
    }

    public static final EnvironmentType DEFAULT     = new EnvironmentType(EnvironmentTypeId.DEFAULT, "", "", null, null, false, false);
    public static final EnvironmentType DEVELOPMENT = new EnvironmentType(EnvironmentTypeId.DEVELOPMENT, "Development", "Development environment", new Color(-2430209), new Color(0x445F80), false, false);
    public static final EnvironmentType INTEGRATION = new EnvironmentType(EnvironmentTypeId.INTEGRATION, "Integration", "Integration environment", new Color(-2621494), new Color(0x466646), true, false);
    public static final EnvironmentType PRODUCTION  = new EnvironmentType(EnvironmentTypeId.PRODUCTION, "Production", "Productive environment", new Color(-11574), new Color(0x634544), true, true);
    public static final EnvironmentType OTHER       = new EnvironmentType(EnvironmentTypeId.OTHER, "Other", "", new Color(-1576), new Color(0x5C5B41), false, false);
    private static final EnvironmentType[] DEFAULT_ENVIRONMENT_TYPES = new EnvironmentType[] {
            DEVELOPMENT,
            INTEGRATION,
            PRODUCTION,
            OTHER};

    private EnvironmentTypeId id;
    private String name;
    private String description;
    private Color regularColor;
    private Color darkColor;
    private JBColor color;
    private boolean readonlyCode = false;
    private boolean readonlyData = false;
    private boolean isDarkScheme = UIUtil.isUnderDarcula();

    public static EnvironmentType forName(String name) {
        for (EnvironmentType environmentType : DEFAULT_ENVIRONMENT_TYPES){
            if (environmentType.name.equals(name)) {
                return environmentType;
            }
        }
        return null;
    }

    public EnvironmentType() {
        this(EnvironmentTypeId.create());
    }

    public EnvironmentType(EnvironmentTypeId id) {
        this.id = id;
    }

    public EnvironmentType(EnvironmentTypeId id, String name, String description, Color regularColor, Color darkColor, boolean readonlyCode, boolean dataEditable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.regularColor = regularColor;
        this.darkColor = darkColor;
        this.readonlyCode = readonlyCode;
        this.readonlyData = dataEditable;
    }

    @Override
    @NotNull
    public String getName() {
        return CommonUtil.nvl(name, "");
    }

    @Nullable
    @Override
    public Icon getIcon() {
        JBColor color = getColor();
        return color == null ? null : new ColorIcon(12, color);
    }

    @Nullable
    public JBColor getColor() {
        if (isDarkScheme != UIUtil.isUnderDarcula()) {
            isDarkScheme = UIUtil.isUnderDarcula();
            color = null;
        }

        if (color == null) {
            if (isDarkScheme && darkColor != null) {
                Color regularColor = CommonUtil.nvl(this.regularColor, DEFAULT_REGULAR_COLOR);
                color = new JBColor(regularColor, darkColor);
            } else if (!isDarkScheme && regularColor != null) {
                Color darkColor = CommonUtil.nvl(this.darkColor, DEFAULT_DARK_COLOR);
                this.color = new JBColor(regularColor, darkColor);
            }
        }

        return color;
    }

    public void setColor(Color color) {
        if (UIUtil.isUnderDarcula())
            darkColor = color; else
            regularColor = color;
        this.color = null;
    }

    @Override
    public EnvironmentType clone() {
        return new EnvironmentType(id, name, description, regularColor, darkColor, readonlyCode, readonlyData);
    }
    
    @Override
    public String toString() {
        return name;
    }


    @Override
    public void readConfiguration(Element element) {
        id = EnvironmentTypeId.get(element.getAttributeValue("id"));
        name = element.getAttributeValue("name");
        description = element.getAttributeValue("description");

        String value = element.getAttributeValue("color");
        if (StringUtil.isNotEmpty(value)) {
            int index = value.indexOf('/');
            if (index > -1) {
                String regularRgb = value.substring(0, index);
                String darkRgb = value.substring(index + 1);
                regularColor = StringUtil.isEmpty(regularRgb) ? null : new Color(Integer.parseInt(regularRgb));
                darkColor = StringUtil.isEmpty(darkRgb) ? null : new Color(Integer.parseInt(darkRgb));
            }
        }

        EnvironmentType defaultEnvironmentType = forName(name);
        if (id == null && defaultEnvironmentType != null) {
            id = defaultEnvironmentType.id;
        }
        if (id == null) id = EnvironmentTypeId.get(name.toLowerCase());
        readonlyCode = getBooleanAttribute(element, "readonly-code", readonlyCode);
        readonlyData = getBooleanAttribute(element, "readonly-data", readonlyData);

    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("id", id.id());
        element.setAttribute("name", name);
        element.setAttribute("description", CommonUtil.nvl(description, ""));
        element.setAttribute("color",
                (regularColor != null ? regularColor.getRGB() : "") + "/" +
                (darkColor != null ? darkColor.getRGB() : ""));
        setBooleanAttribute(element, "readonly-code", readonlyCode);
        setBooleanAttribute(element, "readonly-data", readonlyData);
    }
}
