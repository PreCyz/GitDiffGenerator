package pg.gipter.converters;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import pg.gipter.MockitoExtension;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.dao.data.DataDaoFactory;
import pg.gipter.core.dao.data.ProgramData;
import pg.gipter.jobs.upload.JobProperty;
import pg.gipter.jobs.upload.JobType;
import pg.gipter.ui.UploadStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith({MockitoExtension.class})
class ProgramDataConverterTest {

    @Spy
    private ProgramDataConverter converter;
    private final LocalDateTime uploadDateTime = LocalDateTime.now();
    private final LocalDateTime nextFireDate = LocalDateTime.now();
    private final LocalDate scheduleStart = LocalDate.now();

    private Properties properties;

    @BeforeEach
    void setup() {
        properties = new Properties();
        properties.put(JobProperty.HOUR_OF_THE_DAY.key(), "1:02");
        properties.put(JobProperty.DAY_OF_MONTH.key(), "1");
        properties.put(JobProperty.DAY_OF_WEEK.key(), DayOfWeek.MONDAY.name());
        properties.put(JobProperty.CRON.key(), "0 0 12 */3 * ?");
        properties.put(JobProperty.TYPE.key(), JobType.EVERY_WEEK.name());
        properties.put(JobProperty.SCHEDULE_START.key(), scheduleStart.format(DateTimeFormatter.ISO_DATE));
        properties.put(JobProperty.NEXT_FIRE_DATE.key(), nextFireDate.format(DateTimeFormatter.ISO_DATE_TIME));
        properties.put(JobProperty.CONFIGS.key(), "config1,config2");
        properties.put(DaoConstants.UPLOAD_STATUS_KEY, UploadStatus.SUCCESS.name());
        properties.put(DaoConstants.UPLOAD_DATE_TIME_KEY, uploadDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
    }

    @AfterEach
    private void teardown() {
        ConfigurationDaoFactory.getCachedConfigurationDao().resetCache();
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.DATA_JSON));
        } catch (IOException e) {
            System.err.println("There is something weird going on.");
        }
    }

    @Test
    void givenFullProperties_whenConvert_thenSaveProgramData() {
        doReturn(properties).when(converter).loadDataProperties();

        final boolean actual = converter.convert();

        assertThat(actual).isTrue();
        final ProgramData programData = DataDaoFactory.getDataDao().readProgramData();
        assertThat(programData).isNotNull();
        assertThat(programData.getUploadStatus()).isEqualTo(UploadStatus.SUCCESS);
        assertThat(programData.getLastUploadDateTime()).isEqualTo(uploadDateTime);
        assertThat(programData.getJobParam().getMinuteOfHour()).isEqualTo(2);
        assertThat(programData.getJobParam().getHourOfDay()).isEqualTo(1);
        assertThat(programData.getJobParam().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(programData.getJobParam().getDayOfMonth()).isEqualTo(1);
        assertThat(programData.getJobParam().getCronExpression()).isEqualTo("0 0 12 */3 * ?");
        assertThat(programData.getJobParam().getJobType()).isEqualTo(JobType.EVERY_WEEK);
        assertThat(programData.getJobParam().getScheduleStart()).isEqualTo(scheduleStart);
        assertThat(programData.getJobParam().getNextFireDate()).isEqualTo(nextFireDate);
        assertThat(programData.getJobParam().getConfigsStr()).contains(Arrays.asList("config1","config2"));
        assertThat(programData.getJobParam().getConfigs()).contains("config1","config2");
    }

    @Test
    void givenPropertiesWithoutHourOfTheDay_whenConvert_thenSaveProgramData() {
        properties.remove(JobProperty.HOUR_OF_THE_DAY.key());
        doReturn(properties).when(converter).loadDataProperties();

        final boolean actual = converter.convert();

        assertThat(actual).isTrue();
        final ProgramData programData = DataDaoFactory.getDataDao().readProgramData();
        assertThat(programData).isNotNull();
        assertThat(programData.getUploadStatus()).isEqualTo(UploadStatus.SUCCESS);
        assertThat(programData.getLastUploadDateTime()).isEqualTo(uploadDateTime);
        assertThat(programData.getJobParam().getMinuteOfHour()).isNull();
        assertThat(programData.getJobParam().getHourOfDay()).isNull();
        assertThat(programData.getJobParam().getDayOfMonth()).isEqualTo(1);
        assertThat(programData.getJobParam().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(programData.getJobParam().getCronExpression()).isEqualTo("0 0 12 */3 * ?");
        assertThat(programData.getJobParam().getJobType()).isEqualTo(JobType.EVERY_WEEK);
        assertThat(programData.getJobParam().getScheduleStart()).isEqualTo(scheduleStart);
        assertThat(programData.getJobParam().getNextFireDate()).isEqualTo(nextFireDate);
        assertThat(programData.getJobParam().getConfigsStr()).contains(Arrays.asList("config1","config2"));
        assertThat(programData.getJobParam().getConfigs()).contains("config1","config2");
    }

    @Test
    void givenPropertiesWithoutUploadStatus_whenConvert_thenSaveProgramData() {
        properties.remove(DaoConstants.UPLOAD_STATUS_KEY);
        doReturn(properties).when(converter).loadDataProperties();

        final boolean actual = converter.convert();

        assertThat(actual).isTrue();
        final ProgramData programData = DataDaoFactory.getDataDao().readProgramData();
        assertThat(programData).isNotNull();
        assertThat(programData.getUploadStatus()).isNull();
        assertThat(programData.getLastUploadDateTime()).isEqualTo(uploadDateTime);
        assertThat(programData.getJobParam().getMinuteOfHour()).isEqualTo(2);
        assertThat(programData.getJobParam().getHourOfDay()).isEqualTo(1);
        assertThat(programData.getJobParam().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(programData.getJobParam().getCronExpression()).isEqualTo("0 0 12 */3 * ?");
        assertThat(programData.getJobParam().getJobType()).isEqualTo(JobType.EVERY_WEEK);
        assertThat(programData.getJobParam().getScheduleStart()).isEqualTo(scheduleStart);
        assertThat(programData.getJobParam().getNextFireDate()).isEqualTo(nextFireDate);
        assertThat(programData.getJobParam().getConfigsStr()).contains(Arrays.asList("config1","config2"));
        assertThat(programData.getJobParam().getConfigs()).contains("config1","config2");
    }

    @Test
    void givenPropertiesWithoutCronExpression_whenConvert_thenSaveProgramData() {
        properties.remove(JobProperty.CRON.key());
        doReturn(properties).when(converter).loadDataProperties();

        final boolean actual = converter.convert();

        assertThat(actual).isTrue();
        final ProgramData programData = DataDaoFactory.getDataDao().readProgramData();
        assertThat(programData).isNotNull();
        assertThat(programData.getUploadStatus()).isEqualTo(UploadStatus.SUCCESS);
        assertThat(programData.getLastUploadDateTime()).isEqualTo(uploadDateTime);
        assertThat(programData.getJobParam().getMinuteOfHour()).isEqualTo(2);
        assertThat(programData.getJobParam().getHourOfDay()).isEqualTo(1);
        assertThat(programData.getJobParam().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(programData.getJobParam().getCronExpression()).isNull();
        assertThat(programData.getJobParam().getJobType()).isEqualTo(JobType.EVERY_WEEK);
        assertThat(programData.getJobParam().getScheduleStart()).isEqualTo(scheduleStart);
        assertThat(programData.getJobParam().getNextFireDate()).isEqualTo(nextFireDate);
        assertThat(programData.getJobParam().getConfigsStr()).contains(Arrays.asList("config1","config2"));
        assertThat(programData.getJobParam().getConfigs()).contains("config1","config2");
    }

    @Test
    void givenPropertiesWithoutScheduleStart_whenConvert_thenSaveProgramData() {
        properties.remove(JobProperty.SCHEDULE_START.key());
        doReturn(properties).when(converter).loadDataProperties();

        final boolean actual = converter.convert();

        assertThat(actual).isTrue();
        final ProgramData programData = DataDaoFactory.getDataDao().readProgramData();
        assertThat(programData).isNotNull();
        assertThat(programData.getUploadStatus()).isEqualTo(UploadStatus.SUCCESS);
        assertThat(programData.getLastUploadDateTime()).isEqualTo(uploadDateTime);
        assertThat(programData.getJobParam().getMinuteOfHour()).isEqualTo(2);
        assertThat(programData.getJobParam().getHourOfDay()).isEqualTo(1);
        assertThat(programData.getJobParam().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(programData.getJobParam().getCronExpression()).isEqualTo("0 0 12 */3 * ?");
        assertThat(programData.getJobParam().getJobType()).isEqualTo(JobType.EVERY_WEEK);
        assertThat(programData.getJobParam().getScheduleStart()).isNull();
        assertThat(programData.getJobParam().getNextFireDate()).isEqualTo(nextFireDate);
        assertThat(programData.getJobParam().getConfigsStr()).contains(Arrays.asList("config1","config2"));
        assertThat(programData.getJobParam().getConfigs()).contains("config1","config2");
    }

    @Test
    void givenPropertiesWithoutNextFireDate_whenConvert_thenSaveProgramData() {
        properties.remove(JobProperty.NEXT_FIRE_DATE.key());
        doReturn(properties).when(converter).loadDataProperties();

        final boolean actual = converter.convert();

        assertThat(actual).isTrue();
        final ProgramData programData = DataDaoFactory.getDataDao().readProgramData();
        assertThat(programData).isNotNull();
        assertThat(programData.getUploadStatus()).isEqualTo(UploadStatus.SUCCESS);
        assertThat(programData.getLastUploadDateTime()).isEqualTo(uploadDateTime);
        assertThat(programData.getJobParam().getMinuteOfHour()).isEqualTo(2);
        assertThat(programData.getJobParam().getHourOfDay()).isEqualTo(1);
        assertThat(programData.getJobParam().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(programData.getJobParam().getCronExpression()).isEqualTo("0 0 12 */3 * ?");
        assertThat(programData.getJobParam().getJobType()).isEqualTo(JobType.EVERY_WEEK);
        assertThat(programData.getJobParam().getScheduleStart()).isEqualTo(scheduleStart);
        assertThat(programData.getJobParam().getNextFireDate()).isNull();
        assertThat(programData.getJobParam().getConfigsStr()).contains(Arrays.asList("config1","config2"));
        assertThat(programData.getJobParam().getConfigs()).contains("config1","config2");
    }

    @Test
    void givenPropertiesWithoutConfigs_whenConvert_thenSaveProgramData() {
        properties.remove(JobProperty.CONFIGS.key());
        doReturn(properties).when(converter).loadDataProperties();

        final boolean actual = converter.convert();

        assertThat(actual).isTrue();
        final ProgramData programData = DataDaoFactory.getDataDao().readProgramData();
        assertThat(programData).isNotNull();
        assertThat(programData.getUploadStatus()).isEqualTo(UploadStatus.SUCCESS);
        assertThat(programData.getLastUploadDateTime()).isEqualTo(uploadDateTime);
        assertThat(programData.getJobParam().getMinuteOfHour()).isEqualTo(2);
        assertThat(programData.getJobParam().getHourOfDay()).isEqualTo(1);
        assertThat(programData.getJobParam().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(programData.getJobParam().getCronExpression()).isEqualTo("0 0 12 */3 * ?");
        assertThat(programData.getJobParam().getJobType()).isEqualTo(JobType.EVERY_WEEK);
        assertThat(programData.getJobParam().getScheduleStart()).isEqualTo(scheduleStart);
        assertThat(programData.getJobParam().getNextFireDate()).isEqualTo(nextFireDate);
        assertThat(programData.getJobParam().getConfigsStr()).isEmpty();
        assertThat(programData.getJobParam().getConfigs()).containsExactly("");
    }
}