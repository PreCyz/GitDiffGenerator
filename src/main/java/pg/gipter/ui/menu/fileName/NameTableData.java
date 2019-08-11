package pg.gipter.ui.menu.fileName;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

public class NameTableData {
    private StringProperty wordToReplace;
    private StringProperty replacement;

    NameTableData(String wordToReplace, String replacement) {
        this.wordToReplace = new SimpleStringProperty(wordToReplace);
        this.replacement = new SimpleStringProperty(replacement);
    }

    public String getWordToReplace() {
        return wordToReplace.get();
    }

    public StringProperty wordToReplaceProperty() {
        return wordToReplace;
    }

    public void setWordToReplace(String wordToReplace) {
        this.wordToReplace.set(wordToReplace);
    }

    public String getReplacement() {
        return replacement.get();
    }

    public StringProperty replacementProperty() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement.set(replacement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NameTableData that = (NameTableData) o;
        return Objects.equals(wordToReplace, that.wordToReplace) &&
                Objects.equals(replacement, that.replacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordToReplace, replacement);
    }
}
