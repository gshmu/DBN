package com.dci.intellij.dbn.error;

import java.awt.Component;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.intellij.diagnostic.LogMessage;
import com.intellij.diagnostic.LogMessageEx;
import com.intellij.ide.DataManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.idea.IdeaLogger;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.FAILED;
import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.NEW_ISSUE;

abstract class IssueReportSubmitter extends ErrorReportSubmitter {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private static final String ENCODING = "UTF-8";
    private static final String LINE_DELIMITER = "\n__________________________________________________________________\n";

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

    public SubmittedReportInfo submit(IdeaLoggingEvent[] events, Component parentComponent) {
        final SubmittedReportInfo[] reportInfo = new SubmittedReportInfo[1];
        Consumer<SubmittedReportInfo> consumer = new Consumer<SubmittedReportInfo>() {
            @Override
            public void consume(SubmittedReportInfo submittedReportInfo) {
                reportInfo[0] = submittedReportInfo;
            }
        };
        String additionalInfo = ((LogMessage)events[0].getData()).getAdditionalInfo();
        submit(events, additionalInfo, parentComponent, consumer);
        return reportInfo[0];
    }

    public boolean submit(@NotNull final IdeaLoggingEvent[] events, String additionalInfo, @NotNull Component parentComponent, @NotNull final Consumer<SubmittedReportInfo> consumer) {
        DataContext dataContext = DataManager.getInstance().getDataContext(parentComponent);
        final Project project = PlatformDataKeys.PROJECT.getData(dataContext);

        final String localPluginVersion = getPluginDescriptor().getVersion();
        String repositoryPluginVersion = DatabaseNavigator.getInstance().getRepositoryPluginVersion();

        if (repositoryPluginVersion != null && repositoryPluginVersion.compareTo(localPluginVersion) > 0) {
            NotificationUtil.sendWarningNotification(project, Constants.DBN_TITLE_PREFIX + "New Plugin Version Available", "A newer version of Database Navigator plugin is available in repository" + ". Error report not sent.");
            consumer.consume(new SubmittedReportInfo(getTicketUrlStub(), "", FAILED));
            return false;
        }

        IdeaLoggingEvent event = events[0];
        String eventText = event.getThrowableText();
        final String summary = eventText.substring(0, Math.min(Math.max(80, eventText.length()), 80));

        String platformBuild = ApplicationInfo.getInstance().getBuild().asString();
        ConnectionInfo connectionInfo = ConnectionManager.getLastUsedConnectionInfo();
        String connectionString = null;
        String driverString = null;
        if (connectionInfo != null) {
            connectionString = connectionInfo.getDatabaseType().getDisplayName() + " " + connectionInfo.getProductVersion();
            driverString = connectionInfo.getDriverVersion();
        }

        @NonNls final StringBuilder description = new StringBuilder();
        description.append("Java Version: ").append(System.getProperty("java.version")).append('\n');
        description.append("Operating System: ").append(System.getProperty("os.name")).append('\n');
        description.append("IDE Version: ").append(platformBuild).append('\n');
        description.append("DBN Version: ").append(localPluginVersion).append("\n");
        description.append("Database Version: ").append(connectionString == null ? "NA" : connectionString).append("\n");
        description.append("Driver Version: ").append(driverString == null ? "NA" : driverString).append("\n");
        description.append("Last Action Id: ").append(IdeaLogger.ourLastActionId).append("\n");

        if (StringUtil.isNotEmpty(additionalInfo)) {
            description.append("\n\nUser Message:");
            description.append(LINE_DELIMITER);
            description.append(additionalInfo);
            description.append(LINE_DELIMITER);
        }

        description.append("\n\n").append(event.toString());

        Object eventData = event.getData();
        if (eventData instanceof LogMessageEx) {
            List<Attachment> attachments = ((LogMessageEx) eventData).getAttachments();
            if (attachments.size() > 0) {
                Set<String> attachmentTexts = new HashSet<String>();
                for (Attachment attachment : attachments) {
                    attachmentTexts.add(attachment.getDisplayText().trim());
                }

                description.append("\n\nAttachments:");
                description.append(LINE_DELIMITER);
                int index = 0;
                for (String attachmentText : attachmentTexts) {
                    if (index > 0) description.append(LINE_DELIMITER);
                    description.append("\n");
                    description.append(attachmentText);
                    index++;
                }

                description.append(LINE_DELIMITER);
            }
        }


        new BackgroundTask(project, "Submitting issue report", false, false) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                TicketResponse result = null;
                try {
                    result = submit(events, localPluginVersion, summary, description.toString());
                } catch (Exception e) {

                    NotificationUtil.sendErrorNotification(project, Constants.DBN_TITLE_PREFIX + "Error Reporting",
                            "<html>Failed to send error report: "+ e.getMessage() + "</html>");

                    consumer.consume(new SubmittedReportInfo(getTicketUrlStub(), "", FAILED));
                    return;
                }

                String errorMessage = result.getErrorMessage();
                if (StringUtil.isEmpty(errorMessage)) {
                    LOGGER.info("Error report submitted, response: " + result);

                    String ticketId = result.getTicketId();
                    String ticketUrl = getTicketUrl(ticketId);
                    NotificationUtil.sendInfoNotification(project, Constants.DBN_TITLE_PREFIX + "Error Reporting",
                            "<html>Error report successfully sent. Ticket <a href='" + ticketUrl + "'>" + ticketId + "</a> created.</html>");

                    consumer.consume(new SubmittedReportInfo(ticketUrl, ticketId, NEW_ISSUE));
                } else {
                    NotificationUtil.sendErrorNotification(project, Constants.DBN_TITLE_PREFIX + "Error Reporting", errorMessage);
                    consumer.consume(new SubmittedReportInfo(getTicketUrlStub(), "", FAILED));
                }
            }
        }.start();

        return true;
    }

    public abstract String getTicketUrlStub();
    public abstract String getTicketUrl(String ticketId);

    @NotNull
    public abstract TicketResponse submit(@NotNull IdeaLoggingEvent[] events, String pluginVersion, String summary, String description) throws Exception;

    private static String format(Calendar calendar) {
        return calendar == null ?  null : Long.toString(calendar.getTime().getTime());
    }

    static byte[] join(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (com.intellij.openapi.util.text.StringUtil.isEmpty(param.getKey())) {
                throw new IllegalArgumentException(param.toString());
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            if (com.intellij.openapi.util.text.StringUtil.isNotEmpty(param.getValue())) {
                builder.append(param.getKey()).append('=').append(URLEncoder.encode(param.getValue(), ENCODING));
            }
        }
        return builder.toString().getBytes(ENCODING);
    }
}