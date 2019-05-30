package com.dci.intellij.dbn.error;

import com.intellij.diagnostic.LogMessageEx;
import com.intellij.errorreport.bean.ErrorBean;
import com.intellij.idea.IdeaLogger;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class YouTrackIssueReportSubmitter extends IssueReportSubmitter {

    private static final String URL = "http://dci.myjetbrains.com/youtrack/";
    private static final String ISSUE_URL = URL + "rest/issue";

    @Override
    public String getTicketUrlStub() {
        return ISSUE_URL;
    }

    @Override
    public String getTicketUrl(String ticketId) {
        return URL + "issue/" + ticketId;
    }

    @Override
    @NotNull
    public TicketResponse submit(@NotNull IdeaLoggingEvent[] events, String pluginVersion, String summary, String description) throws Exception{
        StringBuilder response = new StringBuilder("");

        ErrorBean errorBean = new ErrorBean(events[0].getThrowable(), IdeaLogger.ourLastActionId);
        Object eventData = events[0].getData();
        if (eventData instanceof LogMessageEx) {
            errorBean.setAttachments(((LogMessageEx)eventData).getAttachments());
        }

        Map<String, String> parameters = createParameters(summary, description, pluginVersion, errorBean);
        byte[] output = join(parameters);
        java.net.URL issueUrl = new URL(ISSUE_URL);
        URLConnection issueConnection = issueUrl.openConnection();
        issueConnection.setDoOutput(true);

        try (OutputStream outputStream = issueConnection.getOutputStream()) {
            outputStream.write(output);
        }

        BufferedReader responseReader = new BufferedReader(new InputStreamReader(issueConnection.getInputStream()));

        String line;
        while ((line = responseReader.readLine()) != null) {
            response.append(line);
        }

        String ticketId = null;
        String errorMessage = null;
        try {
            Pattern regex = Pattern.compile("id=\"([^\"]+)\"", Pattern.DOTALL | Pattern.MULTILINE);
            Matcher regexMatcher = regex.matcher(response);
            if (regexMatcher.find()) {
                ticketId = regexMatcher.group(1);
            }
        } catch (PatternSyntaxException e) {
            errorMessage = "Failed to receive error report confirmation";
        }
        return new YouTrackTicketResponse(ticketId, errorMessage);
    }



    private static Map<String, String> createParameters(String summary, String description, String pluginVersion, ErrorBean error) {
        Map<String, String> params = ContainerUtil.newLinkedHashMap();

        params.put("login", "autosubmit");
        params.put("password", "autosubmit");

        params.put("project", "DBNE");
        params.put("assignee", "Unassigned");
        params.put("summary", summary);
        params.put("description", description);
        params.put("priority", "4");
        params.put("type", "Exception");

        if (pluginVersion != null)                     {
            params.put("affectsVersion", pluginVersion);
        }
        return params;
    }



}
