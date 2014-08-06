package com.dci.intellij.dbn.error;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.NonNls;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.diagnostic.LogMessage;
import com.intellij.ide.DataManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.FAILED;
import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.NEW_ISSUE;

public class DBNErrorReportSubmitter extends ErrorReportSubmitter {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private static final String URL = "http://dci.myjetbrains.com/youtrack/";
    private static final String ISSUE_URL = URL + "rest/issue";
    private static final String LOGIN_URL = URL + "rest/user/login";
    private static final String ENC = "UTF-8";

    public DBNErrorReportSubmitter() {
        System.out.println();
    }

    @Override
    public IdeaPluginDescriptor getPluginDescriptor() {
        IdeaPluginDescriptor pluginDescriptor = (IdeaPluginDescriptor) super.getPluginDescriptor();
        if (pluginDescriptor == null) {
            pluginDescriptor = PluginManager.getPlugin(PluginId.getId(DatabaseNavigator.DBN_PLUGIN_ID));
            setPluginDescriptor(pluginDescriptor);
        }
        return pluginDescriptor;
    }

    @Override
    public String getReportActionText() {
        return "Submit Issue Report";
    }

    @Override
    public SubmittedReportInfo submit(IdeaLoggingEvent[] events, Component parentComponent) {
        DataContext dataContext = DataManager.getInstance().getDataContext(parentComponent);
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);

        String localPluginVersion = getPluginDescriptor().getVersion();
        String repositoryPluginVersion = DatabaseNavigator.getInstance().getRepositoryPluginVersion();

        if (repositoryPluginVersion != null && repositoryPluginVersion.compareTo(localPluginVersion) > 0) {
            NotificationUtil.sendWarningNotification(project, Constants.DBN_TITLE_PREFIX + "New Plugin Version Available", "A newer version of Database Navigator plugin is available in repository" + ". Error report not sent.");
            return new SubmittedReportInfo(ISSUE_URL, "", FAILED);
        }

        IdeaLoggingEvent firstEvent = events[0];
        String firstEventText = firstEvent.getThrowableText();
        String summary = firstEventText.substring(0, Math.min(Math.max(80, firstEventText.length()), 80));

/*
        Throwable t = firstEvent.getThrowable();
        if (t != null) {
            PluginId pluginId = IdeErrorsDialog.findPluginId(t);
            if (pluginId != null) {
                IdeaPluginDescriptor ideaPluginDescriptor = PluginManager.getPlugin(pluginId);
                if (ideaPluginDescriptor != null && !ideaPluginDescriptor.isBundled()) {
                    localPluginVersion = ideaPluginDescriptor.getVersion();
                }
            }
        }
*/
        String platformBuild = ApplicationInfo.getInstance().getBuild().asString();

        @NonNls StringBuilder description = new StringBuilder();
        description.append("Java Version: ").append(System.getProperty("java.version")).append('\n');
        description.append("Operating System: ").append(System.getProperty("os.name")).append('\n');
        description.append("IDE Version: ").append(platformBuild).append('\n');
        description.append("DBN Version: ").append(localPluginVersion).append("\n");

        for (IdeaLoggingEvent event : events) {
            LogMessage logMessage = (LogMessage) event.getData();
            String additionalInfo = logMessage == null ? null : logMessage.getAdditionalInfo();
            if (StringUtil.isNotEmpty(additionalInfo)) {
                description.append("\n\nUser Message:");
                description.append("\n__________________________________________________________________\n");
                description.append(additionalInfo);
                description.append("\n__________________________________________________________________");
            }
            description.append("\n\n").append(event.toString());
        }


        String result = submit(localPluginVersion, summary, description.toString());

        LOGGER.info("Error report submitted, response: " + result);

        if (result == null) {
            NotificationUtil.sendErrorNotification(project, Constants.DBN_TITLE_PREFIX + "Error Reporting", "Failed to send error report");
            return new SubmittedReportInfo(ISSUE_URL, "", FAILED);
        }

        String ticketId = null;
        try {
            Pattern regex = Pattern.compile("id=\"([^\"]+)\"", Pattern.DOTALL | Pattern.MULTILINE);
            Matcher regexMatcher = regex.matcher(result);
            if (regexMatcher.find()) {
                ticketId = regexMatcher.group(1);
            }
        } catch (PatternSyntaxException ex) {
        }

        if (ticketId == null) {
            NotificationUtil.sendErrorNotification(project, Constants.DBN_TITLE_PREFIX + "Error Reporting", "Failed to send error report");
            return new SubmittedReportInfo(ISSUE_URL, "", FAILED);
        }

        String ticketUrl = URL + "issue/" + ticketId;
        NotificationUtil.sendInfoNotification(project, Constants.DBN_TITLE_PREFIX + "Error Reporting",
                "<html>Error report successfully sent. Ticket <a href='" + ticketUrl + "'>" + ticketId + "</a> created.</html>");

        return new SubmittedReportInfo(ticketUrl, ticketId, NEW_ISSUE);
    }

    public String submit(String pluginVersion, String summary, String description) {
        StringBuilder response = new StringBuilder("");

        StringBuilder data = new StringBuilder();
        try {
            data.append(URLEncoder.encode("login", ENC)).append("=").append(URLEncoder.encode("autosubmit", ENC));
            data.append("&").append(URLEncoder.encode("password", ENC)).append("=").append(URLEncoder.encode("autosubmit", ENC));

            URL url = new URL(LOGIN_URL);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data.toString());
            wr.flush();

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }

            data = new StringBuilder();
            data.append(URLEncoder.encode("project", ENC)).append("=").append(URLEncoder.encode("DBN", ENC));
            data.append("&").append(URLEncoder.encode("assignee", ENC)).append("=").append(URLEncoder.encode("Unassigned", ENC));
            data.append("&").append(URLEncoder.encode("summary", ENC)).append("=").append(URLEncoder.encode(summary, ENC));
            data.append("&").append(URLEncoder.encode("description", ENC)).append("=").append(URLEncoder.encode(description, ENC));
            data.append("&").append(URLEncoder.encode("priority", ENC)).append("=").append(URLEncoder.encode("4", ENC));
            data.append("&").append(URLEncoder.encode("type", ENC)).append("=").append(URLEncoder.encode("Exception", ENC));

            if (pluginVersion != null)
                data.append("&").append(URLEncoder.encode("affectsVersion", ENC)).append("=").append(URLEncoder.encode(pluginVersion, ENC));

            url = new URL(ISSUE_URL);
            conn = url.openConnection();
            conn.setDoOutput(true);

            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data.toString());
            wr.flush();

            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = rd.readLine()) != null) {
                response.append(line);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }
}