package pg.gipter.core.dao.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.jobs.JobParam;
import pg.gipter.jobs.json.LocalDateAdapter;
import pg.gipter.jobs.json.LocalDateTimeAdapter;
import pg.gipter.ui.UploadStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

class DataAccess implements DataDao {

    private final Logger logger = LoggerFactory.getLogger(DataAccess.class);

    private ProgramData cachedProgramData;

    private DataAccess() { }

    private static class DataAccessHolder {
        private static final DataAccess INSTANCE = new DataAccess();
    }

    static DataAccess getInstance() {
        return DataAccessHolder.INSTANCE;
    }

    @Override
    public Optional<JobParam> loadJobParam() {
        ProgramData programData = readProgramData();
        return Optional.ofNullable(programData.getJobParam());
    }

    @Override
    public ProgramData readProgramData() {
        if (cachedProgramData != null) {
            logger.info("Program data taken from cached value");
            return cachedProgramData;
        }
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe())
                .create();
        ProgramData programData = new ProgramData();
        try (InputStream fis = new FileInputStream(DaoConstants.DATA_JSON);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            programData = gson.fromJson(reader, ProgramData.class);
        } catch (IOException | NullPointerException e) {
            logger.warn("Warning when loading {}. Exception message is: {}",
                    DaoConstants.DATA_JSON, e.getMessage());
        }
        cachedProgramData = programData;
        return cachedProgramData;
    }

    @Override
    public void saveJobParam(JobParam jobParam) {
        final ProgramData programData = readProgramData();
        programData.setJobParam(jobParam);
        writeToJson(programData);
    }

    private void writeToJson(ProgramData programData) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe())
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(programData, ProgramData.class);
        try (OutputStream os = new FileOutputStream(DaoConstants.DATA_JSON);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            writer.write(json);
            cachedProgramData = programData;
            logger.info("File {} updated with {}.", DaoConstants.DATA_JSON, ProgramData.class.getSimpleName());
        } catch (IOException e) {
            logger.error("Error when writing {}. Exception message is: {}",
                    DaoConstants.DATA_JSON, e.getMessage());
            throw new IllegalArgumentException("Error when writing program data into json.");
        }
    }

    @Override
    public void saveUploadStatus(UploadStatus uploadStatus) {
        final ProgramData programData = readProgramData();
        programData.setLastUploadDateTime(LocalDateTime.now());
        programData.setUploadStatus(uploadStatus);
        writeToJson(programData);
    }

    @Override
    public void saveNextUploadDateTime(LocalDateTime nextUploadDateTime) {
        final ProgramData programData = readProgramData();
        final JobParam jobParam = Optional.ofNullable(programData.getJobParam()).orElseGet(JobParam::new);
        jobParam.setNextFireDate(nextUploadDateTime);
        programData.setJobParam(jobParam);
        writeToJson(programData);
    }

    @Override
    public void removeJobParam() {
        final ProgramData programData = readProgramData();
        programData.setJobParam(null);
        writeToJson(programData);
    }

    @Override
    public void saveProgramData(ProgramData programData) {
        writeToJson(programData);
    }

}
