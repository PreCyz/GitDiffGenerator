package pg.gipter.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.data.DataDaoFactory;
import pg.gipter.core.dao.data.ProgramData;
import pg.gipter.jobs.JobParam;
import pg.gipter.jobs.JobProperty;
import pg.gipter.jobs.JobType;
import pg.gipter.ui.UploadStatus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static pg.gipter.core.dao.DaoConstants.DATA_PROPERTIES;

public class ProgramDataConverter implements Converter {

    private static final Logger logger = LoggerFactory.getLogger(ProgramDataConverter.class);
    private Properties dataProperties;
    private final ProgramData programData;
    private final JobParam jobParam;

    ProgramDataConverter() {
        programData = new ProgramData();
        jobParam = new JobParam();
    }

    Properties loadDataProperties() {
        Properties properties = new Properties();
        try (InputStream fis = new FileInputStream(DATA_PROPERTIES);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            properties.load(reader);
        } catch (IOException | NullPointerException e) {
            logger.warn("Error when loading {}. Exception message is: {}", DATA_PROPERTIES, e.getMessage());
        }
        return properties;
    }

    @Override
    public boolean convert() {
        boolean result = false;
        dataProperties = loadDataProperties();
        if (!dataProperties.isEmpty()) {
            programData.setLastUploadDateTime(convertLocalDateTime(DaoConstants.UPLOAD_DATE_TIME_KEY));
            programData.setUploadStatus(convertEnum(DaoConstants.UPLOAD_STATUS_KEY, UploadStatus.class));

            jobParam.setConfigsStr(convertString(JobProperty.CONFIGS.key(), ""));
            jobParam.setConfigs(Stream.of(convertString(JobProperty.CONFIGS.key(), "").split(",")).collect(toSet()));
            jobParam.setNextFireDate(convertLocalDateTime(JobProperty.NEXT_FIRE_DATE.key()));
            jobParam.setCronExpression(convertString(JobProperty.CRON.key(), null));
            jobParam.setJobType(convertEnum(JobProperty.TYPE.key(), JobType.class));
            jobParam.setScheduleStart(convertLocalDate(JobProperty.SCHEDULE_START.key()));
            jobParam.setDayOfWeek(convertEnum(JobProperty.DAY_OF_WEEK.key(), DayOfWeek.class));
            convertInteger(JobProperty.DAY_OF_MONTH.key()).ifPresent(jobParam::setDayOfMonth);
            convertHourOfDay().ifPresent(jobParam::setHourOfDay);
            convertMinuteOfHour().ifPresent(jobParam::setMinuteOfHour);

            programData.setJobParam(jobParam);
            DataDaoFactory.getDataDao().saveProgramData(programData);

            try {
                Files.deleteIfExists(Paths.get(DATA_PROPERTIES));
            } catch (IOException e) {
                logger.error("There is something weird going on.");
            }
            result = true;
        } else {
            logger.info("No data.properties to convert");
        }
        return result;
    }

    private LocalDateTime convertLocalDateTime(String key) {
        String defaultValue = null;
        final String value = dataProperties.getProperty(key, defaultValue);
        if (value != null) {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        }
        return null;
    }

    private LocalDate convertLocalDate(String key) {
        String defaultValue = null;
        final String value = dataProperties.getProperty(key, defaultValue);
        if (value != null) {
            return LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
        }
        return null;
    }

    private String convertString(String key, String defaultValue) {
        final String value = dataProperties.getProperty(key, defaultValue);
        if (value != null && !value.equals(defaultValue)) {
            return value;
        }
        return defaultValue;
    }

    private <T extends Enum<T>> T convertEnum(String key, Class<T> clazz) {
        String defaultValue = null;
        final String value = dataProperties.getProperty(key, defaultValue);
        if (value != null) {
            return Enum.valueOf(clazz, value);
        }
        return null;
    }

    private Optional<Integer> convertInteger(String key) {
        final String value = dataProperties.getProperty(key, null);
        if (value != null) {
            return Optional.of(Integer.parseInt(value));
        }
        return Optional.empty();
    }

    private Optional<Integer> convertHourOfDay() {
        final String hourOfTheDay = dataProperties.getProperty(JobProperty.HOUR_OF_THE_DAY.key(), null);
        if (hourOfTheDay != null) {
            final String[] split = hourOfTheDay.split(":");
            return Optional.of(Integer.parseInt(split[0]));
        }
        return Optional.empty();
    }

    private Optional<Integer> convertMinuteOfHour() {
        final String hourOfTheDay = dataProperties.getProperty(JobProperty.HOUR_OF_THE_DAY.key(), null);
        if (hourOfTheDay != null) {
            final String[] split = hourOfTheDay.split(":");
            return Optional.of(Integer.parseInt(split[1]));
        }
        return Optional.empty();
    }
}
