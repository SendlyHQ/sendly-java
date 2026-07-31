package com.sendly.models;

import java.util.List;
import java.util.Map;

/**
 * The approved WhatsApp template to send, with its variable values.
 */
public class WhatsAppTemplateSendParams {
    private final String name;
    private final String language;
    private final Map<String, String> variables;
    private final List<WhatsAppTemplateButtonVariables> buttons;

    /**
     * Create template send params.
     *
     * @param name     Template name as approved (e.g. "order_shipped")
     * @param language Template language code (e.g. "en_US") — must match the
     *                 approved template's language exactly
     */
    public WhatsAppTemplateSendParams(String name, String language) {
        this(name, language, null, null);
    }

    /**
     * Create template send params with variable values.
     *
     * @param name      Template name as approved (e.g. "order_shipped")
     * @param language  Template language code (e.g. "en_US") — must match the
     *                  approved template's language exactly
     * @param variables Body variable values keyed by placeholder number:
     *                  {@code Map.of("1", "Acme Inc", "2", "#4821")}
     */
    public WhatsAppTemplateSendParams(String name, String language, Map<String, String> variables) {
        this(name, language, variables, null);
    }

    /**
     * Create template send params with all options.
     *
     * @param name      Template name as approved (e.g. "order_shipped")
     * @param language  Template language code (e.g. "en_US") — must match the
     *                  approved template's language exactly
     * @param variables Body variable values keyed by placeholder number
     * @param buttons   Variable values for dynamic-URL buttons
     */
    public WhatsAppTemplateSendParams(String name, String language, Map<String, String> variables,
                                      List<WhatsAppTemplateButtonVariables> buttons) {
        this.name = name;
        this.language = language;
        this.variables = variables;
        this.buttons = buttons;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public List<WhatsAppTemplateButtonVariables> getButtons() {
        return buttons;
    }
}
