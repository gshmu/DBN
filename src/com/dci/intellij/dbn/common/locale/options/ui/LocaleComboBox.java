package com.dci.intellij.dbn.common.locale.options.ui;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public class LocaleComboBox extends ComboBox {
    private Locale defaultLocale = Locale.getDefault();
    private LocaleWrapper[] localeWrappers;
    public LocaleComboBox() {
        Locale[] locales = Locale.getAvailableLocales();

        // use locale wrappers in the dropdown because the speed search
        // relies on locale toString which is not the same with the display name
        localeWrappers = new LocaleWrapper[locales.length];
        for (int i=0; i<locales.length; i++) {
            localeWrappers[i] = new LocaleWrapper(locales[i]);
        }
        Arrays.sort(localeWrappers, new LocaleWrapperComparator());
        setModel(new DefaultComboBoxModel(localeWrappers));
        setRenderer(new LocaleCellRenderer());
    }

    class LocaleCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                LocaleWrapper localeWrapper = (LocaleWrapper) value;
                String text = localeWrapper.isSystemDefault() ?
                        localeWrapper.getDisplayName() + " - System default" :
                        localeWrapper.getDisplayName();
                label.setText(text);
            }
            return label;
        }
    }

    public Locale getSelectedLocale() {
        LocaleWrapper localeWrapper = (LocaleWrapper) getSelectedItem();
        return localeWrapper.getLocale();
    }

    public void setSelectedLocale(Locale locale) {
        for (LocaleWrapper localeWrapper: localeWrappers) {
            if (localeWrapper.getLocale().equals(locale)) {
                setSelectedItem(localeWrapper);
                break;
            }
        }
    }

    private class LocaleWrapperComparator implements Comparator<LocaleWrapper> {

        public int compare(LocaleWrapper locale1, LocaleWrapper locale2) {
            //if (locale1.isSystemDefault()) return -1;
            //if (locale2.isSystemDefault()) return 1;
            return locale1.getDisplayName().compareTo(locale2.getDisplayName());
        }
    }

    private class LocaleWrapper {
        private Locale locale;
        private String displayName;

        private LocaleWrapper(Locale locale) {
            this.locale = locale;
            this.displayName = locale.getDisplayName();
        }

        public String getDisplayName() {
            return displayName;
        }

        public Locale getLocale() {
            return locale;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public boolean isSystemDefault() {
            return locale.equals(defaultLocale);
        }
    }

}
