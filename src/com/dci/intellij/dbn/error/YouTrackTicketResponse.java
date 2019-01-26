package com.dci.intellij.dbn.error;

import org.jetbrains.annotations.Nullable;

public class YouTrackTicketResponse implements TicketResponse{
    private String ticketId;
    private String errorMessage;

    public YouTrackTicketResponse(String ticketId, String errorMessage) {
        this.ticketId = ticketId;
        this.errorMessage = errorMessage;
    }

    @Override
    @Nullable
    public String getTicketId() {
        return ticketId;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
