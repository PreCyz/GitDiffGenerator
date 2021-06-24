package pg.gipter.services;

import java.util.*;

import static java.util.stream.Collectors.toCollection;

public class IntelliSenseService<E extends Enum<E>> {

    private final Set<String> definedPatterns;

    public IntelliSenseService(Class<E> enumClass) {
        definedPatterns = EnumSet.allOf(enumClass)
                .stream()
                .map(e -> String.format("{%s}", e.name()))
                .collect(toCollection(LinkedHashSet::new));
    }

    public Set<String> getDefinedPatterns() {
        return new LinkedHashSet<>(definedPatterns);
    }

    Set<String> getCompleted(String text) {
        return definedPatterns.stream()
                .filter(text::contains)
                .collect(toCollection(LinkedHashSet::new));
    }

    String getUncompleted(String text) {
        final Set<String> completed = getCompleted(text);
        String result = "";
        String textCut = text;
        for (String s : completed) {
            textCut = textCut.replace(s, "");
        }
        if (textCut.contains("{") && textCut.indexOf("{") + 1 <= textCut.length()) {
            result = textCut.substring(textCut.indexOf("{") + 1);
        }
        return result;
    }

    public Set<String> getFilteredValues(String text) {
        Set<String> result = Collections.emptySet();
        final String uncompleted = getUncompleted(text).toLowerCase();
        if (text.endsWith("{")) {
            result = new LinkedHashSet<>(definedPatterns);
        } else if (!uncompleted.isEmpty() ) {
            result = definedPatterns.stream()
                    .filter(pattern -> pattern.contains(uncompleted) || pattern.contains(uncompleted.toUpperCase()))
                    .collect(toCollection(LinkedHashSet::new));
        }
        return result;
    }

    public Optional<Integer> getSelectedStartPosition(String text) {
        Optional<Integer> result = Optional.empty();
        boolean isPatternStarted = text.lastIndexOf("{") > text.lastIndexOf("}");
        if (isPatternStarted) {
            result = Optional.of(text.lastIndexOf("{") + 1);
        }
        return result;
    }

    public String getValue(String oldValue, String newValue) {
        if (oldValue != null) {
            return oldValue.substring(0, oldValue.lastIndexOf("{")) + newValue;
        }
        return newValue;
    }

}
