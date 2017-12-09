package ru.innopolis.university.innometrics.jb_plugin.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CodeLocation {
    public enum CodeElementLabel {
        PROJ, LANG, NS, CLASS, FUNC, LINE
    }

    private static class CodeElement {
        private CodeElementLabel label;
        private String value;

        private CodeElement(CodeElementLabel label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    private static final String DELIMITER = "|";
    private static final String LABEL_DELIMITER = ":";

    private List<CodeElement> elements;

    public CodeLocation() {
        this.elements = new ArrayList<>();
    }

    public void addElement(CodeElementLabel label, String value) {
        elements.add(new CodeElement(label, value));
    }

    public String buildPath() {
        return elements.stream()
                .map(e -> e.label + LABEL_DELIMITER + e.value)
                .collect(Collectors.joining(DELIMITER));
    }
}
