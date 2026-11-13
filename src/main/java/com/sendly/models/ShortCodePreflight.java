package com.sendly.models;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What a dry run of a short code application says, without changing anything.
 */
public class ShortCodePreflight {
    private final boolean ok;
    private final List<ShortCodeIssue> issues;
    private final List<String> requiredDocuments;
    private final List<String> missingDocuments;
    private final List<ShortCodeDocument> documents;
    private final List<String> ignoredFields;

    public ShortCodePreflight(JsonObject json) {
        this.ok = ShortCodeJson.bool(json, "ok");
        List<ShortCodeIssue> issueList = new ArrayList<>();
        for (JsonObject issue : ShortCodeJson.objects(json, "issues")) {
            issueList.add(new ShortCodeIssue(issue));
        }
        this.issues = Collections.unmodifiableList(issueList);
        this.requiredDocuments = ShortCodeJson.strings(json, "requiredDocuments");
        this.missingDocuments = ShortCodeJson.strings(json, "missingDocuments");
        this.documents = ShortCodeApplicationView.documentsOf(json);
        this.ignoredFields = ShortCodeJson.strings(json, "ignoredFields");
    }

    /** Whether the application can be submitted as it is. */
    public boolean isOk() { return ok; }

    /** Every issue found, all at once. */
    public List<ShortCodeIssue> getIssues() { return issues; }

    /** The forms this order type needs. */
    public List<String> getRequiredDocuments() { return requiredDocuments; }

    /** The required forms not signed yet. */
    public List<String> getMissingDocuments() { return missingDocuments; }

    /** The forms Sendly has prepared for this application. */
    public List<ShortCodeDocument> getDocuments() { return documents; }

    /** Fields Sendly or the carriers own, which were ignored; empty when none. */
    public List<String> getIgnoredFields() { return ignoredFields; }
}
