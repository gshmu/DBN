package com.dci.intellij.dbn.error;

import org.jetbrains.annotations.Nullable;

public interface TicketResponse {
    @Nullable
    String getTicketId();

    String getErrorMessage();
}
