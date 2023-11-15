package com.dbn.data.editor.ui.calendar;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Safe;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/******************************************************
 *                       Actions                      *
 ******************************************************/
public class CalendarNextMonthAction extends CalendarPopupAction {
    CalendarNextMonthAction() {
        super("Next Month", null, Icons.CALENDAR_NEXT_MONTH);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CalendarTableModel model = getCalendarTableModel(e);
        Safe.run(model, m -> m.rollMonth(1));
    }
}
