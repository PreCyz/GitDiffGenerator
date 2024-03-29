package pg.gipter.services;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

public class TextFieldIntelliSense<E extends Enum<E>> {

    private final Set<String> definedPatterns;
    private final TextField textField;

    private boolean ignoreListener = false;
    private String currentValue = "";

    int caretPosition;
    private Integer caretAfterChange;
    private String previousValue = "";

    TextFieldIntelliSense(TextField textField, Class<E> enumClass) {
        definedPatterns = EnumSet.allOf(enumClass)
                .stream()
                .map(e -> String.format("{%s}", e.name()))
                .collect(toCollection(LinkedHashSet::new));
        this.textField = textField;
    }

    public static <E extends Enum<E>> TextFieldIntelliSense<E> init(TextField textField, Class<E> enumClass) {
        TextFieldIntelliSense<E> service = new TextFieldIntelliSense<>(textField, enumClass);
        TextFields.bindAutoCompletion(service.textField, service.suggestionsCallback());
        service.textField.setOnKeyReleased(service.keyReleasedEventHandler());
        service.textField.textProperty().addListener(service.textFieldChangeListener());
        return service;
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

    Set<String> getFilteredValues(String text) {
        Set<String> result = Collections.emptySet();
        final String uncompleted = getUncompleted(text).toLowerCase();
        if (text.endsWith("{") || (!text.isEmpty() && text.charAt(caretPosition) == '{')) {
            result = new LinkedHashSet<>(definedPatterns);
        } else if (!uncompleted.isEmpty()) {
            result = definedPatterns.stream()
                    .filter(pattern -> pattern.contains(uncompleted) || pattern.contains(uncompleted.toUpperCase()))
                    .collect(toCollection(LinkedHashSet::new));
        }
        return result;
    }

    Optional<Integer> getSelectedStartPosition(String text) {
        Optional<Integer> result = Optional.empty();
        boolean isPatternStarted = text.lastIndexOf("{") > text.lastIndexOf("}");
        if (isPatternStarted) {
            result = Optional.of(text.lastIndexOf("{") + 1);
        }
        return result;
    }

    String getValue(String oldValue, String newValue) {
        caretAfterChange = null;
        if (!previousValue.isEmpty()) {
            String firstPart = previousValue.substring(0, caretPosition);
            String secondPart = previousValue.substring(caretPosition);
            String result = firstPart + newValue + secondPart;
            caretAfterChange = (firstPart + newValue).length();
            return result;
        } else if (oldValue != null) {
            return oldValue.substring(0, oldValue.lastIndexOf("{")) + newValue;
        }
        return newValue;
    }

    private Callback<AutoCompletionBinding.ISuggestionRequest, Collection<String>> suggestionsCallback() {
        return param -> getFilteredValues(param.getUserText());
    }

    private EventHandler<KeyEvent> keyReleasedEventHandler() {
        return event -> {
            if (EnumSet.of(KeyCode.ENTER, KeyCode.TAB).contains(event.getCode())) {
                ignoreListener = true;
                textField.setText(currentValue);
                Optional.ofNullable(caretAfterChange).ifPresentOrElse(
                        textField::positionCaret,
                        () -> textField.positionCaret(currentValue.length())
                );
                ignoreListener = false;
            } else if (KeyCode.BACK_SPACE == event.getCode()) {
                final Optional<Integer> startPosition = getSelectedStartPosition(currentValue);
                if (startPosition.isPresent()) {
                    final int endSelectionPosition = textField.getCaretPosition();
                    textField.selectRange(startPosition.get(), endSelectionPosition);
                }
                caretPosition--;
            }
        };
    }

    private ChangeListener<String> textFieldChangeListener() {
        return (observable, oldValue, newValue) -> {
            if (ignoreListener) return;

            currentValue = newValue;
            if (definedPatterns.contains(newValue)) {
                currentValue = getValue(oldValue, newValue);
                ignoreListener = true;
                textField.setText(currentValue);
                ignoreListener = false;
            }
            int idx = 0;
            boolean goNext = true;
            while (goNext) {
                caretPosition = idx;
                if (!oldValue.isEmpty() && idx < oldValue.length()) {
                    goNext = idx < newValue.length() && newValue.charAt(idx) == oldValue.charAt(idx);
                } else {
                    goNext = false;
                }
                idx++;
            }
            if (idx < newValue.length() - 1) {
                previousValue = oldValue;
            } else {
                previousValue = "";
            }
            textField.positionCaret(caretPosition);
        };
    }

}
