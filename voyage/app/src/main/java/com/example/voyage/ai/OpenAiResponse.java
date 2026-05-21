package com.example.voyage.ai;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OpenAiResponse {

    // Convenience field — top-level text from the first output message
    @SerializedName("output_text")
    public String outputText;

    // Full structured output (fallback if output_text is absent)
    public List<OutputItem> output;

    public static class OutputItem {
        public List<ContentItem> content;
    }

    public static class ContentItem {
        public String type;
        public String text;
    }

    /** Returns the response text, checking output_text first then the nested structure. */
    public String getText() {
        if (outputText != null && !outputText.isEmpty()) return outputText;
        if (output != null) {
            for (OutputItem item : output) {
                if (item.content != null) {
                    for (ContentItem c : item.content) {
                        if ("output_text".equals(c.type) && c.text != null && !c.text.isEmpty()) {
                            return c.text;
                        }
                    }
                }
            }
        }
        return null;
    }
}
