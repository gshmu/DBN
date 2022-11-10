package com.dci.intellij.dbn.error;

import com.google.gson.JsonObject;

class JiraTicketRequest {
    private JsonObject jsonObject = new JsonObject();

    JiraTicketRequest(String summary, String description) {
        summary = summary.replace("\r\n", " ").replace("\t", " ");

        // project
        JsonObject project = new JsonObject();
        project.addProperty("key", "DBNE");

        // issue type
        JsonObject issueType = new JsonObject();
        issueType.addProperty("name", "Exception");


        // fields
        JsonObject fields = new JsonObject();
        fields.add("project", project);
        fields.addProperty("summary", summary);
        fields.addProperty("description", description);
        fields.add("issuetype", issueType);
        jsonObject.add("fields", fields);
    }

    JsonObject getJsonObject() {
        return jsonObject;
    }
}
