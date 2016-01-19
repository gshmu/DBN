package com.dci.intellij.dbn.error;

import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;

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
    public String getMarkupElement(MarkupElement element) {
        switch (element) {
            case CODE: return "{code}";
            case BOLD: return "*";
            case ITALIC: return "_";
            case TABLE: return "|";
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

            HttpPost httpPost = new HttpPost(URL + "rest/api/2/issue");
            StringEntity params = new StringEntity(requestString);
            httpPost.addHeader("content-type", "application/json");
            httpPost.setEntity(params);
            HttpClient httpClient = HTTP_CLIENT_BUILDER.build();
            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse == null) {
                return new JiraTicketResponse(null, "Received empty response from server");
            } else {
                InputStream in = httpResponse.getEntity().getContent();
                String responseString = CommonUtil.readInputStream(in);
                return new JiraTicketResponse(responseString, null);
            }
        } catch (Exception e) {
            return new JiraTicketResponse(null, e.getMessage());
        }
    }
}
