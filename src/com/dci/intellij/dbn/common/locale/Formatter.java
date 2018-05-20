package com.dci.intellij.dbn.common.locale;

import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.value.ValueAdapter;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Formatter implements Cloneable{
    private DateFormat dateFormat;
    private DateFormat timeFormat;
    private DateFormat dateTimeFormat;
    private NumberFormat numberFormat;
    private NumberFormat integerFormat;

    private String dateFormatPattern;
    private String timeFormatPattern;
    private String datetimeFormatPattern;
    private String numberFormatPattern;
    private String integerFormatPattern;

    private Formatter() {
    }

    public Formatter(@NotNull Locale locale, DBDateFormat dateFormatOption, DBNumberFormat numberFormatOption) {
        int dFormat = dateFormatOption.getDateFormat();
        dateFormat = SimpleDateFormat.getDateInstance(dFormat, locale);
        timeFormat = SimpleDateFormat.getTimeInstance(dFormat, locale);
        dateTimeFormat = SimpleDateFormat.getDateTimeInstance(dFormat, dFormat, locale);


        boolean groupingUsed = numberFormatOption == DBNumberFormat.GROUPED;

        integerFormat = NumberFormat.getIntegerInstance(locale);
        integerFormat.setGroupingUsed(groupingUsed);

        numberFormat = DecimalFormat.getInstance(locale);
        numberFormat.setGroupingUsed(groupingUsed);
        numberFormat.setMaximumFractionDigits(10);

        dateFormatPattern = ((SimpleDateFormat) dateFormat).toPattern();
        timeFormatPattern = ((SimpleDateFormat) timeFormat).toPattern();
        datetimeFormatPattern = ((SimpleDateFormat) dateTimeFormat).toPattern();
        numberFormatPattern = ((DecimalFormat) numberFormat).toPattern();
        integerFormatPattern = ((DecimalFormat) integerFormat).toPattern();
    }

    public Formatter(@NotNull Locale locale, String dateFormatPattern, String timeFormatPattern, String numberFormatPattern) {
        if (StringUtil.isEmptyOrSpaces(dateFormatPattern)) throw new IllegalArgumentException("Date format pattern empty.");
        if (StringUtil.isEmptyOrSpaces(timeFormatPattern)) throw new IllegalArgumentException("Time format pattern empty.");
        if (StringUtil.isEmptyOrSpaces(numberFormatPattern)) throw new IllegalArgumentException("Number format pattern empty.");
        this.dateFormatPattern = dateFormatPattern;
        this.timeFormatPattern = timeFormatPattern;
        this.datetimeFormatPattern = dateFormatPattern + ' ' + timeFormatPattern;
        this.numberFormatPattern = numberFormatPattern;

        int fractionIndex = numberFormatPattern.lastIndexOf('.');
        if (fractionIndex > -1) {
            this.integerFormatPattern = numberFormatPattern.substring(0, fractionIndex);
        } else {
            this.integerFormatPattern = numberFormatPattern;
        }

        DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
        dateFormat = new SimpleDateFormat(this.dateFormatPattern, dateFormatSymbols);
        timeFormat = new SimpleDateFormat(this.timeFormatPattern, dateFormatSymbols);
        dateTimeFormat = new SimpleDateFormat(this.datetimeFormatPattern, dateFormatSymbols);

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
        numberFormat = new DecimalFormat(this.numberFormatPattern, decimalFormatSymbols);
        integerFormat = new DecimalFormat(this.integerFormatPattern, decimalFormatSymbols);
        integerFormat.setMaximumFractionDigits(0);
    }

    public static Formatter getInstance(@NotNull Project project) {
        return RegionalSettings.getInstance(project).getFormatter();
    }

    public static ThreadLocal<Formatter> init(@NotNull final Project project) {
        return new ThreadLocal<Formatter>(){
            @Override
            public Formatter get() {
                Formatter formatter = super.get();
                if (formatter == null) {
                    formatter = getInstance(project);
                    set(formatter);
                }
                return formatter;
            }
        };
    }

    public String getDateFormatPattern() {
        return dateFormatPattern;
    }

    public String getTimeFormatPattern() {
        return timeFormatPattern;
    }

    public String getDatetimeFormatPattern() {
        return datetimeFormatPattern;
    }

    public String getNumberFormatPattern() {
        return numberFormatPattern;
    }

    public String getIntegerFormatPattern() {
        return integerFormatPattern;
    }

    public String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public Date parseDate(String string) throws ParseException {
        return dateFormat.parse(string);
    }

    public String formatTime(Date date) {
        return timeFormat.format(date);
    }

    public Date parseTime(String string) throws ParseException {
        return timeFormat.parse(string);
    }

    public String formatDateTime(Date date) {
        return dateTimeFormat.format(date);
    }

    public Date parseDateTime(String string) throws ParseException {
        return dateTimeFormat.parse(string);
    }


    public String formatNumber(Number number) {
        return numberFormat.format(number);
    }

    public Number parseNumber(String string) throws ParseException {
        return numberFormat.parse(string);
    }

    public String formatInteger(Number number) {
        return integerFormat.format(number);
    }

    public Number parseInteger(String string) throws ParseException {
        return integerFormat.parse(string);
    }

    public String formatObject(Object object) {
        if (object != null) {
            return
                object instanceof Number ? formatNumber((Number) object) :
                object instanceof Date ? formatDateTime((Date) object) :
                object instanceof String ? (String) object :
                object instanceof ValueAdapter ? ((ValueAdapter) object).getDisplayValue() :
                object.toString();
        } else {
            return null;
        }
    }

    public Object parseObject(Class clazz, String string) throws ParseException {
        if (Date.class.isAssignableFrom(clazz)) {
            return parseDateTime(string);
        }
        if (Number.class.isAssignableFrom(clazz)) {
            return parseNumber(string);
        }
        return string;
    }

    @Override
    public Formatter clone() {
        Formatter clone = new Formatter();
        clone.dateFormat = (DateFormat) dateFormat.clone();
        clone.timeFormat = (DateFormat) timeFormat.clone();
        clone.dateTimeFormat = (DateFormat) dateTimeFormat.clone();
        clone.numberFormat = (NumberFormat) numberFormat.clone();
        clone.integerFormat = (NumberFormat) integerFormat.clone();

        clone.dateFormatPattern = dateFormatPattern;
        clone.timeFormatPattern = timeFormatPattern;
        clone.datetimeFormatPattern = datetimeFormatPattern;
        clone.numberFormatPattern = numberFormatPattern;
        clone.integerFormatPattern = integerFormatPattern;
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Formatter formatter = (Formatter) o;

        if (dateFormat != null ? !dateFormat.equals(formatter.dateFormat) : formatter.dateFormat != null) return false;
        if (timeFormat != null ? !timeFormat.equals(formatter.timeFormat) : formatter.timeFormat != null) return false;
        if (dateTimeFormat != null ? !dateTimeFormat.equals(formatter.dateTimeFormat) : formatter.dateTimeFormat != null) return false;
        if (numberFormat != null ? !numberFormat.equals(formatter.numberFormat) : formatter.numberFormat != null) return false;
        if (integerFormat != null ? !integerFormat.equals(formatter.integerFormat) : formatter.integerFormat != null) return false;
        if (dateFormatPattern != null ? !dateFormatPattern.equals(formatter.dateFormatPattern) : formatter.dateFormatPattern != null) return false;
        if (timeFormatPattern != null ? !timeFormatPattern.equals(formatter.timeFormatPattern) : formatter.timeFormatPattern != null) return false;
        if (datetimeFormatPattern != null ? !datetimeFormatPattern.equals(formatter.datetimeFormatPattern) : formatter.datetimeFormatPattern != null) return false;
        if (numberFormatPattern != null ? !numberFormatPattern.equals(formatter.numberFormatPattern) : formatter.numberFormatPattern != null) return false;
        return !(integerFormatPattern != null ? !integerFormatPattern.equals(formatter.integerFormatPattern) : formatter.integerFormatPattern != null);

    }

    @Override
    public int hashCode() {
        int result = dateFormat != null ? dateFormat.hashCode() : 0;
        result = 31 * result + (timeFormat != null ? timeFormat.hashCode() : 0);
        result = 31 * result + (dateTimeFormat != null ? dateTimeFormat.hashCode() : 0);
        result = 31 * result + (numberFormat != null ? numberFormat.hashCode() : 0);
        result = 31 * result + (integerFormat != null ? integerFormat.hashCode() : 0);
        result = 31 * result + (dateFormatPattern != null ? dateFormatPattern.hashCode() : 0);
        result = 31 * result + (timeFormatPattern != null ? timeFormatPattern.hashCode() : 0);
        result = 31 * result + (datetimeFormatPattern != null ? datetimeFormatPattern.hashCode() : 0);
        result = 31 * result + (numberFormatPattern != null ? numberFormatPattern.hashCode() : 0);
        result = 31 * result + (integerFormatPattern != null ? integerFormatPattern.hashCode() : 0);
        return result;
    }
}
