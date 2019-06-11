package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.latent.Latent;
import com.intellij.ui.border.CustomLineBorder;

import javax.swing.table.TableCellRenderer;
import java.awt.*;

public interface DBNTableHeaderRenderer extends TableCellRenderer {
     Latent<CustomLineBorder> BORDER_BR = Latent.laf(
            () -> new CustomLineBorder(Colors.tableHeaderBorderColor(), 0, 0, 1, 1));

     Latent<CustomLineBorder> BORDER_TBR = Latent.laf(
            () -> new CustomLineBorder(Colors.tableHeaderBorderColor(), 1, 0, 1, 1));

     Latent<CustomLineBorder> BORDER_LBR = Latent.laf(
            () -> new CustomLineBorder(Colors.tableHeaderBorderColor(), 0, 1, 1, 1));

     Latent<CustomLineBorder> BORDER_TLBR = Latent.laf(
            () -> new CustomLineBorder(Colors.tableHeaderBorderColor(), 1, 1, 1, 1));

    void setFont(Font font);

}
