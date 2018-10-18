package pg.gipter.settings;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPropertiesTest {

    private ApplicationProperties appProps;

    @Test
    void given_authorFromCommandLine_when_author_then_returnThatAuthor() {
        appProps = new ApplicationProperties(new String[]{"author=testAuthor"});

        String actual = appProps.author();

        assertThat(actual).isEqualTo("testAuthor");
    }

    @Test
    void given_itemPathFromCommandLine_when_itemPath_then_returnThatItemPath() {
        appProps = new ApplicationProperties(new String[]{"itemPath=testItemPath"});

        String actual = appProps.itemPath();

        assertThat(actual).startsWith("testItemPath" + File.separator);
    }

    @Test
    void given_defaultParams_when_fileName_then_returnThatFileName() {
        appProps = new ApplicationProperties(new String[]{});
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
        String fileName = String.format("%d-%s-week-%d.txt", now.getYear(), now.getMonth().name(), weekNumber).toLowerCase();

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo(fileName);
    }

    @Test
    void given_statement_when_fileName_then_returnFileNameForStatement() {
        appProps = new ApplicationProperties(new String[]{
                "codeProtection=statement"
        });
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
        String fileName = String.format("%d-%s-week-%d.txt", now.getYear(), now.getMonth().name(), weekNumber).toLowerCase();

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo(fileName);
    }

    @Test
    void given_itemFileNameFromCommandLine_when_fileName_then_returnThatFileName() {
        appProps = new ApplicationProperties(new String[]{"itemFileName=fileName"});

        String actual = appProps.fileName();

        assertThat(actual).startsWith("fileName");
    }

    @Test
    void given_itemFileNameAndStartDateAndEndDateFromCommandLine_when_fileName_then_returnBuildFileName() {
        appProps = new ApplicationProperties(new String[]{"itemFileName=fileName", "startDate=2018-10-07", "endDate=2018-10-14"});

        String actual = appProps.fileName();

        assertThat(actual).isEqualTo("fileName-20181007-20181014.txt");
    }
}