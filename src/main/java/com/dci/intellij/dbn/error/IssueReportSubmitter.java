package com.dci.intellij.dbn.error;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.Context;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;
import com.intellij.diagnostic.AbstractMessage;
import com.intellij.diagnostic.LogMessage;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.idea.IdeaLogger;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

import static com.dci.intellij.dbn.common.util.Commons.nvl;
import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;
import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.FAILED;
import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.NEW_ISSUE;

@Slf4j
abstract class IssueReportSubmitter extends ErrorReportSubmitter {

    private static final String ENCODING = "UTF-8";
    private static final String LINE_DELIMITER = "\n__________________________________________________________________\n";

    @Override
    public IdeaPluginDescriptor getPluginDescriptor() {
        IdeaPluginDescriptor pluginDescriptor = (IdeaPluginDescriptor) super.getPluginDescriptor();
        if (pluginDescriptor == null) {
            pluginDescriptor = DatabaseNavigator.getPluginDescriptor();
            setPluginDescriptor(pluginDescriptor);
        }
        return pluginDescriptor;
    }

    @NotNull
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
                          @NotNull Consumer<SubmittedReportInfo> consumer){

        DataContext dataContext = Context.getDataContext(parentComponent);
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);

        String localPluginVersion = getPluginDescriptor().getVersion();

        IdeaLoggingEvent event = events[0];
        String eventSummary = event.getThrowableText();
        final String summary = eventSummary.substring(0, Math.min(eventSummary.length(), 100));

        String platformBuild = ApplicationInfo.getInstance().getBuild().asString();
        ConnectionHandler connection = loadConnection(project);

        String connectionString = null;
        String driverString = null;
        if (connection != null) {
            ConnectionInfo connectionInfo = connection.getConnectionInfo();
            if (connectionInfo != null) {
                String databaseType = connectionInfo.getDatabaseType().getName();
                String productVersion = connectionInfo.getProductVersion();
                connectionString = databaseType + " " + productVersion;
                driverString = connectionInfo.getDriverVersion();

            } else {
                ConnectionDatabaseSettings databaseSettings = connection.getSettings().getDatabaseSettings();
                DatabaseType databaseType = DatabaseType.resolve(databaseSettings.getDriver());
                if (databaseType == DatabaseType.GENERIC ) {
                    databaseType = databaseSettings.getDatabaseType();
                }

                connectionString = databaseType == DatabaseType.GENERIC ? null : databaseType.getName();

                String driverLibrary = databaseSettings.getDriverLibrary();
                driverString = Strings.isEmpty(driverLibrary) ? null : new File(driverLibrary).getName();
            }
        }

        StringBuilder description = new StringBuilder();
        addEnvInfo(description, "Java Version", System.getProperty("java.version"));
        addEnvInfo(description, "Operating System", System.getProperty("os.name"));
        addEnvInfo(description, "IDE Version", platformBuild);
        addEnvInfo(description, "DBN Version", localPluginVersion);
        addEnvInfo(description, "Database Version", connectionString == null ? "NA" : connectionString);
        addEnvInfo(description, "Driver Version", driverString == null ? "NA" : driverString);
        addEnvInfo(description, "Last Action Id", nvl(IdeaLogger.ourLastActionId, "NA"));

        if (Strings.isNotEmpty(additionalInfo)) {
            description.append(getMarkupElement(MarkupElement.PANEL, "User Message"));
            description.append(additionalInfo);
            description.append(getMarkupElement(MarkupElement.PANEL));
        }

        String exceptionMessage = event.getMessage();
        if (Strings.isNotEmpty(exceptionMessage) && !"null".equals(exceptionMessage)) {
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
            if (!attachments.isEmpty()) {
                Set<String> attachmentTexts = new HashSet<>();
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


        Progress.prompt(project, null, true, "Submitting issue report", null, progress -> {
            TicketResponse result;
            try {
                result = submit(events, localPluginVersion, summary, description.toString());
            } catch (Exception e) {
                conditionallyLog(e);

                NotificationSupport.sendErrorNotification(
                        project,
                        NotificationGroup.REPORTING,
                        "<html>Failed to send error report: {0}</html>", e);

                consumer.accept(new SubmittedReportInfo(null, null, FAILED));
                return;
            }

            String errorMessage = result.getErrorMessage();
            if (Strings.isEmpty(errorMessage)) {
                log.info("Error report submitted, response: " + result);

                String ticketId = result.getTicketId();
                String ticketUrl = getTicketUrl(ticketId);
                NotificationSupport.sendInfoNotification(
                        project,
                        NotificationGroup.REPORTING,
                        "<html>Error report successfully sent. Ticket <a href='" + ticketUrl + "'>" + ticketId + "</a> created.</html>");

                consumer.accept(new SubmittedReportInfo(ticketUrl, ticketId, NEW_ISSUE));
            } else {
                NotificationSupport.sendErrorNotification(
                        project,
                        NotificationGroup.REPORTING,
                        "<html>Failed to send error report: " + errorMessage + "</html>");
                consumer.accept(new SubmittedReportInfo(null, null, FAILED));
            }
        });

        return true;
    }

    @Nullable
    private static ConnectionHandler loadConnection(Project project) {
        ConnectionHandler connection = ConnectionManager.getLastUsedConnection();
        if (connection != null) return connection;
        if (project == null) return null;

        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        List<ConnectionHandler> connections = connectionBundle.getConnections();
        if (!connections.isEmpty()) return connections.get(0);

        connections = connectionBundle.getAllConnections();
        if (!connections.isEmpty()) return connections.get(0);

        return null;
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
    public String getMarkupElement(MarkupElement element, String title) {return nvl(title, "");}

    @NotNull
    public abstract TicketResponse submit(@NotNull IdeaLoggingEvent[] events, String pluginVersion, String summary, String description) throws Exception;

    private static String format(Calendar calendar) {
        return calendar == null ?  null : Long.toString(calendar.getTime().getTime());
    }

    static byte[] join(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (val param : params.entrySet()) {
            if (Strings.isEmpty(param.getKey())) {
                throw new IllegalArgumentException(param.toString());
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            if (Strings.isNotEmpty(param.getValue())) {
                builder.append(param.getKey()).append('=').append(URLEncoder.encode(param.getValue(), ENCODING));
            }
        }
        return builder.toString().getBytes(ENCODING);
    }
}