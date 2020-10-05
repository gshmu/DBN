package com.dci.intellij.dbn.error;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.intellij.diagnostic.AbstractMessage;
import com.intellij.diagnostic.LogMessage;
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
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            pluginDescriptor = PluginManager.getPlugin(DatabaseNavigator.DBN_PLUGIN_ID);
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
        SubmittedReportInfo[] reportInfo = new SubmittedReportInfo[1];
        Consumer<SubmittedReportInfo> consumer = submittedReportInfo -> reportInfo[0] = submittedReportInfo;
        LogMessage data = (LogMessage) events[0].getData();
        String additionalInfo = data == null ? null : data.getAdditionalInfo();
        submit(events, additionalInfo, parentComponent, consumer);
        return reportInfo[0];
    }

    public boolean submit(@NotNull IdeaLoggingEvent[] events,
                          @Nullable String additionalInfo,
                          @NotNull Component parentComponent,
                          @NotNull Consumer<? super SubmittedReportInfo> consumer){

        DataContext dataContext = DataManager.getInstance().getDataContext(parentComponent);
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);

        String localPluginVersion = getPluginDescriptor().getVersion();
        String repositoryPluginVersion = DatabaseNavigator.getInstance().getRepositoryPluginVersion();

        if (repositoryPluginVersion != null && repositoryPluginVersion.compareTo(localPluginVersion) > 0) {
            NotificationSupport.sendWarningNotification(
                    project,
                    NotificationGroup.REPORTING,
                    "A newer version of Database Navigator plugin is available in repository. Error report not sent.");

            consumer.consume(new SubmittedReportInfo(getTicketUrlStub(), "", FAILED));
            return false;
        }

        IdeaLoggingEvent event = events[0];
        String eventSummary = event.getThrowableText();
        final String summary = eventSummary.substring(0, Math.min(eventSummary.length(), 100));

        String platformBuild = ApplicationInfo.getInstance().getBuild().asString();
        ConnectionInfo connectionInfo = ConnectionManager.getLastUsedConnectionInfo();
        String connectionString = null;
        String driverString = null;
        if (connectionInfo != null) {
            connectionString = connectionInfo.getDatabaseType().getName() + " " + connectionInfo.getProductVersion();
            driverString = connectionInfo.getDriverVersion();
        }
        @NonNls final StringBuilder description = new StringBuilder();
        addEnvInfo(description, "Java Version", System.getProperty("java.version"));
        addEnvInfo(description, "Operating System", System.getProperty("os.name"));
        addEnvInfo(description, "IDE Version", platformBuild);
        addEnvInfo(description, "DBN Version", localPluginVersion);
        addEnvInfo(description, "Database Version", connectionString == null ? "NA" : connectionString);
        addEnvInfo(description, "Driver Version", driverString == null ? "NA" : driverString);
        addEnvInfo(description, "Last Action Id", IdeaLogger.ourLastActionId);

        if (StringUtil.isNotEmpty(additionalInfo)) {
            description.append(getMarkupElement(MarkupElement.PANEL, "User Message"));
            description.append(additionalInfo);
            description.append(getMarkupElement(MarkupElement.PANEL));
        }

        String exceptionMessage = event.getMessage();
        if (StringUtil.isNotEmpty(exceptionMessage) && !"null".equals(exceptionMessage)) {
            description.append("\n\n");
            exceptionMessage = exceptionMessage.replace("{", "\\{").replace("}", "\\}").replace("[", "\\[").replace("]", "\\]");
            description.append(exceptionMessage);
            description.append("\n\n");
        }
        description.append(getMarkupElement(MarkupElement.CODE, event.getThrowable().getClass().getName()));
        String eventDetails = event.getThrowableText();
        if (eventDetails.length() > 30000) {
            eventDetails = eventDetails.substring(0, 30000);
        }
        description.append(eventDetails);
        description.append(getMarkupElement(MarkupElement.CODE));

        Object eventData = event.getData();
        if (eventData instanceof AbstractMessage) {
            List<Attachment> attachments = ((AbstractMessage) eventData).getIncludedAttachments();
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


        Progress.prompt(project, "Submitting issue report", true,
                (progress) -> {
                    TicketResponse result;
                    try {
                        result = submit(events, localPluginVersion, summary, description.toString());
                    } catch (Exception e) {

                        NotificationSupport.sendErrorNotification(
                                project,
                                NotificationGroup.REPORTING,
                                "<html>Failed to send error report: {0}</html>", e);

                        consumer.consume(new SubmittedReportInfo(null, null, FAILED));
                        return;
                    }

                    String errorMessage = result.getErrorMessage();
                    if (StringUtil.isEmpty(errorMessage)) {
                        LOGGER.info("Error report submitted, response: " + result);

                        String ticketId = result.getTicketId();
                        String ticketUrl = getTicketUrl(ticketId);
                        NotificationSupport.sendInfoNotification(
                                project,
                                NotificationGroup.REPORTING,
                                "<html>Error report successfully sent. Ticket <a href='" + ticketUrl + "'>" + ticketId + "</a> created.</html>");

                        consumer.consume(new SubmittedReportInfo(ticketUrl, ticketId, NEW_ISSUE));
                    } else {
                        NotificationSupport.sendErrorNotification(
                                project,
                                NotificationGroup.REPORTING,
                                "<html>Failed to send error report: " + errorMessage + "</html>");
                        consumer.consume(new SubmittedReportInfo(null, null, FAILED));
                    }
                });

        return true;
    }

    void addEnvInfo(StringBuilder description, String label, String value) {
        String boldME = getMarkupElement(MarkupElement.BOLD);
        String tableME = getMarkupElement(MarkupElement.TABLE);
        description.append(tableME);
        description.append(boldME);
        description.append(label);
        description.append(boldME);
        description.append(": ");
        description.append(tableME);
        description.append(value);
        description.append(tableME);
        description.append('\n');
    }

    public abstract String getTicketUrlStub();
    public abstract String getTicketUrl(String ticketId);
    public String getMarkupElement(MarkupElement element) {return getMarkupElement(element, null);}
    public String getMarkupElement(MarkupElement element, String title) {return CommonUtil.nvl(title, "");}

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