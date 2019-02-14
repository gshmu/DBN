package com.dci.intellij.dbn.common.locale.options;

import com.dci.intellij.dbn.common.locale.DBDateFormat;
import com.dci.intellij.dbn.common.locale.DBNumberFormat;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.ui.RegionalSettingsEditorForm;
import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.options.setting.StringSetting;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class RegionalSettings extends BasicProjectConfiguration<GeneralProjectSettings, RegionalSettingsEditorForm> {
    private Locale locale = Locale.getDefault();
    private DBDateFormat dateFormatOption = DBDateFormat.MEDIUM;
    private DBNumberFormat numberFormatOption = DBNumberFormat.UNGROUPED;

    private BooleanSetting useCustomFormats = new BooleanSetting("use-custom-formats", false);
    private StringSetting customNumberFormat = new StringSetting("custom-number-format", null);
    private StringSetting customDateFormat = new StringSetting("custom-date-format", null);
    private StringSetting customTimeFormat = new StringSetting("custom-time-format", null);

    private ThreadLocal<Formatter> formatter = new ThreadLocal<Formatter>();

    public RegionalSettings(GeneralProjectSettings parent) {
        super(parent);
    }

    public static RegionalSettings getInstance(@NotNull Project project) {
        return GeneralProjectSettings.getInstance(project).getRegionalSettings();
    }



    @Override
    public void apply() throws ConfigurationException {
        formatter.set(null);
        super.apply();
    }

    public Formatter getFormatter(){
        Formatter formatter = this.formatter.get();
        if (formatter == null) {
            formatter = useCustomFormats.value() ?
                    new Formatter(locale, customDateFormat.value(), customTimeFormat.value(), customNumberFormat.value()) :
                    new Formatter(locale, dateFormatOption, numberFormatOption);
            this.formatter.set(formatter);
        }
        return formatter;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public DBDateFormat getDateFormatOption() {
        return dateFormatOption;
    }

    public void setDateFormatOption(DBDateFormat dateFormatOption) {
        this.dateFormatOption = dateFormatOption;
    }

    public DBNumberFormat getNumberFormatOption() {
        return numberFormatOption;
    }

    public void setNumberFormatOption(DBNumberFormat numberFormatOption) {
        this.numberFormatOption = numberFormatOption;
    }

    public BooleanSetting getUseCustomFormats() {
        return useCustomFormats;
    }

    public StringSetting getCustomDateFormat() {
        return customDateFormat;
    }

    public StringSetting getCustomTimeFormat() {
        return customTimeFormat;
    }

    public StringSetting getCustomNumberFormat() {
        return customNumberFormat;
    }

    /*********************************************************
     *                      Configuration                    *
     *********************************************************/
    @Override
    @NotNull
    public RegionalSettingsEditorForm createConfigurationEditor() {
        return new RegionalSettingsEditorForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "regional-settings";
    }

    @Override
    public void readConfiguration(Element element) {
        formatter.set(null);
        String localeString = SettingsSupport.getString(element, "locale", Locale.getDefault().toString());
        boolean useSystemLocale = localeString.equals("SYSTEM_DEFAULT");
        if (useSystemLocale) {
             this.locale = Locale.getDefault();
        } else {
            for (Locale locale : Locale.getAvailableLocales()) {
                if (locale.toString().equals(localeString)) {
                    this.locale = locale;
                    break;
                }
            }
        }

        dateFormatOption = SettingsSupport.getEnum(element, "date-format", DBDateFormat.MEDIUM);
        numberFormatOption = SettingsSupport.getEnum(element, "number-format", DBNumberFormat.UNGROUPED);
        useCustomFormats.readConfiguration(element);

        if (useCustomFormats.value()) {
            customNumberFormat.readConfiguration(element);
            customDateFormat.readConfiguration(element);
            customTimeFormat.readConfiguration(element);
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsSupport.setEnum(element, "date-format", dateFormatOption);
        SettingsSupport.setEnum(element, "number-format", numberFormatOption);

        String localeString = this.locale.equals(Locale.getDefault()) ? "SYSTEM_DEFAULT" : locale.toString();
        SettingsSupport.setString(element, "locale", localeString);

        useCustomFormats.writeConfiguration(element);
        if (useCustomFormats.value()) {
            customNumberFormat.writeConfiguration(element);
            customDateFormat.writeConfiguration(element);
            customTimeFormat.writeConfiguration(element);
        }

    }


}
