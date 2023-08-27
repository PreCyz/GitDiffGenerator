package pg.gipter.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.model.NamePatternValue;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;

class TextFieldIntelliSenseTest {

    private TextFieldIntelliSense<NamePatternValue> service;

    @BeforeEach
    void setUp() {
        service = new TextFieldIntelliSense<>(null, NamePatternValue.class);
    }

    @Test
    void givenFullPhrase_whenGetCompleted_thenReturnCompleted() {
        final Set<String> completed = service.getCompleted("s-{CURRENT_MONTH_NAME}-{START_DATE_YEAR}-{CUR");
        assertThat(completed).containsExactly("{CURRENT_MONTH_NAME}", "{START_DATE_YEAR}");
    }

    @Test
    void givenFullPhraseWithNoCompleted_whenGetCompleted_thenReturnEmptySet() {
        final Set<String> completed = service.getCompleted("s-{CUR");
        assertThat(completed).isEmpty();
    }

    @Test
    void givenFullPhraseWithoutInteliSense_whenGetCompleted_thenReturnEmptySet() {
        final Set<String> completed = service.getCompleted("s-");
        assertThat(completed).isEmpty();
    }


    @Test
    void givenFullPhrase_whenGetUncompleted_thenReturnOneElement() {
        final String uncompleted = service.getUncompleted("s-{CURRENT_MONTH_NAME}-{START_DATE_YEAR}-{CUR");
        assertThat(uncompleted).isEqualTo("CUR");
    }

    @Test
    void givenFullPhraseWithoutCompleted_whenGetUncompleted_thenReturnOneElement() {
        final String uncompleted = service.getUncompleted("s-{CUR");
        assertThat(uncompleted).isEqualTo("CUR");
    }

    @Test
    void givenFullPhraseWithoutInteliSense_whenGetUncompleted_thenReturnEmptySet() {
        final String uncompleted = service.getUncompleted("s-");
        assertThat(uncompleted).isEmpty();
    }

    @Test
    void givenFullPhraseWithoutUncompleted_whenGetUncompleted_thenReturnEmptySet() {
        final String uncompleted = service.getUncompleted("s-{CURRENT_MONTH_NAME}-{START_DATE_YEAR}");
        assertThat(uncompleted).isEmpty();
    }

    @Test
    void givenFullPhraseWithOnlyUncompleted_whenGetUncompleted_thenReturnOneElement() {
        final String uncompleted = service.getUncompleted("{START");
        assertThat(uncompleted).isEqualTo("START");
    }

    @Test
    void givenNoPhrase_whenGetFilteredValues_thenReturnNothing() {
        service.caretPosition = 0;
        final Set<String> filteredValues = service.getFilteredValues("");
        assertThat(filteredValues).isEmpty();
    }

    @Test
    void givenPhraseWithoutInteliSense_whenGetFilteredValues_thenReturnNothing() {
        final Set<String> filteredValues = service.getFilteredValues("some-text");
        assertThat(filteredValues).isEmpty();
    }

    @Test
    void givenPhrase_whenGetFilteredValues_thenReturnFiltered() {
        String text = "{cur";
        service.caretPosition = text.length() - 1;
        final Set<String> filteredValues = service.getFilteredValues(text);
        assertThat(filteredValues).containsExactly(
                "{CURRENT_DATE}",
                "{CURRENT_YEAR}",
                "{CURRENT_WEEK_NUMBER}",
                "{CURRENT_MONTH_NAME}",
                "{current_month_name}",
                "{CURRENT_MONTH_NUMBER}"
        );
    }

    @Test
    void givenPhraseWithStart_whenGetFilteredValues_thenReturnFiltered() {
        final Set<String> filteredValues = service.getFilteredValues("some-text-{CURRENT_DATE}-{sta");
        assertThat(filteredValues).containsExactly(
                "{START_DATE}",
                "{START_DATE_YEAR}",
                "{START_DATE_MONTH_NAME}",
                "{start_date_month_name}",
                "{START_DATE_MONTH_NUMBER}",
                "{START_DATE_WEEK_NUMBER}"
        );
    }

    @Test
    void givenPhraseWithEnd_whenGetFilteredValues_thenReturnFiltered() {
        String text = "{CURRENT_DATE}-some-text{end";
        service.caretPosition = text.length() - 1;
        final Set<String> filteredValues = service.getFilteredValues(text);
        assertThat(filteredValues).containsExactly(
                "{END_DATE}",
                "{END_DATE_YEAR}",
                "{END_DATE_MONTH_NAME}",
                "{end_date_month_name}",
                "{END_DATE_MONTH_NUMBER}",
                "{END_DATE_WEEK_NUMBER}"
        );
    }

    @Test
    void givenPhraseWithMonth_whenGetFilteredValues_thenReturnFiltered() {
        String text = "{CURRENT_DATE}{MONTH";
        service.caretPosition = text.length() - 1;
        final Set<String> filteredValues = service.getFilteredValues(text);
        assertThat(filteredValues).containsExactly(
                "{CURRENT_MONTH_NAME}",
                "{current_month_name}",
                "{CURRENT_MONTH_NUMBER}",
                "{START_DATE_MONTH_NAME}",
                "{start_date_month_name}",
                "{START_DATE_MONTH_NUMBER}",
                "{END_DATE_MONTH_NAME}",
                "{end_date_month_name}",
                "{END_DATE_MONTH_NUMBER}"
        );
    }

    @Test
    void givenPhraseWithMonthName_whenGetFilteredValues_thenReturnFiltered() {
        String text = "{CURRENT_DATE}{MONTH_NAME";
        service.caretPosition = text.length() - 1;
        final Set<String> filteredValues = service.getFilteredValues(text);
        assertThat(filteredValues).containsExactly(
                "{CURRENT_MONTH_NAME}",
                "{current_month_name}",
                "{START_DATE_MONTH_NAME}",
                "{start_date_month_name}",
                "{END_DATE_MONTH_NAME}",
                "{end_date_month_name}"
        );
    }

    @Test
    void givenPhraseAndOnlyOpenSign_whenGetFilteredValues_thenReturnAll() {
        final Set<String> filteredValues = service.getFilteredValues("some-{");
        assertThat(filteredValues).containsExactlyElementsOf(EnumSet.allOf(NamePatternValue.class)
                .stream()
                .map(e -> String.format("{%s}", e.name()))
                .collect(toCollection(LinkedHashSet::new))
        );
    }

    @Test
    void givenOnlyOpenSign_whenGetFilteredValues_thenReturnAll() {
        final Set<String> filteredValues = service.getFilteredValues("{");
        assertThat(filteredValues).containsExactlyElementsOf(EnumSet.allOf(NamePatternValue.class)
                .stream()
                .map(e -> String.format("{%s}", e.name()))
                .collect(toCollection(LinkedHashSet::new))
        );
    }

    @Test
    void givenFatText_whenGetSelectedStartPosition_thenReturn6() {
        final Optional<Integer> actual = service.getSelectedStartPosition("some-{some");
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(6);
    }

    @Test
    void givenOnlyOpenBracket_whenGetSelectedStartPosition_thenReturn1() {
        final Optional<Integer> actual = service.getSelectedStartPosition("{");
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(1);
    }

    @Test
    void givenOpenBracketAndSomeText_whenGetSelectedStartPosition_thenReturn1() {
        final Optional<Integer> actual = service.getSelectedStartPosition("{some");
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(1);
    }

    @Test
    void givenEmptyText_whenGetSelectedStartPosition_thenReturnEmpty() {
        final Optional<Integer> actual = service.getSelectedStartPosition("");
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    void givenPlainText_whenGetSelectedStartPosition_thenReturnEmpty() {
        final Optional<Integer> actual = service.getSelectedStartPosition("plain text");
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    void givenTextFullOfBrackets_whenGetSelectedStartPosition_thenReturnStartPositionForTheLastBracket() {
        final Optional<Integer> actual = service.getSelectedStartPosition("s-{a}-{some}-{some");
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(14);
    }

    @Test
    void givenOldNullAndNewText_whenGetValue_thenReturnNewValue() {
        final String actual = service.getValue(null, "newValue");
        assertThat(actual).isEqualTo("newValue");
    }

    @Test
    void givenOldAndNewText_whenGetValue_thenReturnNewValue() {
        final String actual = service.getValue("some={new", "newValue");
        assertThat(actual).isEqualTo("some=newValue");
    }

    @Test
    void givenRealLiveExample_whenGetValue_thenReturnProperValue() {
        final String actual = service.getValue("{CURRENT_YEAR", "CURRENT_YEAR");
        assertThat(actual).isEqualTo("CURRENT_YEAR");
    }
}