package com.dci.intellij.dbn.error;

import com.dci.intellij.dbn.common.util.Commons;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import static com.dci.intellij.dbn.common.dispose.Failsafe.conditionallyLog;

public class JiraIssueReportSubmitter extends IssueReportSubmitter {
    private static final HttpClientBuilder HTTP_CLIENT_BUILDER = HttpClientBuilder.create();
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder();
    private static final String URL = "https://database-navigator.atlassian.net/";

    @Override
    public String getTicketUrlStub() {
        return URL + "browse/";
    }

    @Override
    public String getTicketUrl(String ticketId) {
        return URL + "browse/" + ticketId;
    }

    @Override
    public String getMarkupElement(MarkupElement element, String title) {
        switch (element) {
            case BOLD: return "*";
            case ITALIC: return "_";
            case TABLE: return "|";
            case CODE: return title == null ? "{code}" : "{code:title=" + title + "}";
            case PANEL: return title == null ? "{panel}" : "{panel:title=" + title + "}";
        }
        return "";
    }

    @NotNull
    @Override
    public TicketResponse submit(@NotNull IdeaLoggingEvent[] events, String pluginVersion, String summary, String description) throws Exception {
        JiraTicketRequest ticketRequest = new JiraTicketRequest(summary, description);
        try {
            Gson gson = GSON_BUILDER.create();
            String requestString = gson.toJson(ticketRequest.getJsonObject());
            StringEntity params = new StringEntity(requestString);

            // https://developer.atlassian.com/cloud/jira/platform/jira-rest-api-basic-authentication/
            // dancioca@bluewin.ch:YeRDb1bt6dA7DJH75D8j3465 => BASE64
            // or plugin@database-navigator.com:YeRDb1bt6dA7DJH75D8j3465 => BASE64

            HttpPost httpPost = new HttpPost(URL + "rest/api/latest/issue");
            httpPost.addHeader("Authorization", "Basic cGx1Z2luQGRhdGFiYXNlLW5hdmlnYXRvci5jb206WWVSRGIxYnQ2ZEE3REpINzVEOGozNDY1");
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(params);
            HttpClient httpClient = HTTP_CLIENT_BUILDER.build();
            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse == null) {
                return new JiraTicketResponse(null, "Received empty response from server");
            } else {
                InputStream in = httpResponse.getEntity().getContent();
                String responseString = Commons.readInputStream(in);
                return new JiraTicketResponse(responseString, null);
            }
        } catch (Exception e) {
            conditionallyLog(e);
            return new JiraTicketResponse(null, e.getMessage());
        }
    }
}
