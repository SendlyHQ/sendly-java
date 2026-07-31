package com.sendly.models;

import java.util.Map;

/**
 * Variable values for one dynamic-URL button on an approved WhatsApp
 * template.
 */
public class WhatsAppTemplateButtonVariables {
    private final int index;
    private final Map<String, String> variables;

    /**
     * Create button variable values.
     *
     * @param index     Zero-based index of the button on the approved template
     * @param variables Values for the button's URL placeholders, keyed by
     *                  placeholder number: {@code Map.of("1", "4821")}
     */
    public WhatsAppTemplateButtonVariables(int index, Map<String, String> variables) {
        this.index = index;
        this.variables = variables;
    }

    public int getIndex() {
        return index;
    }

    public Map<String, String> getVariables() {
        return variables;
    }
}
