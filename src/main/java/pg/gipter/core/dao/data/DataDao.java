package pg.gipter.core.dao.data;

import pg.gipter.jobs.upload.JobParam;
import pg.gipter.ui.UploadStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DataDao {

    Optional<JobParam> loadJobParam();
    ProgramData readProgramData();
    void saveJobParam(JobParam jobParam);
    void saveUploadStatus(UploadStatus uploadStatus);
    void saveNextUploadDateTime(LocalDateTime nextUploadDateTime);
    void removeJobParam();
    void saveProgramData(ProgramData programData);
}
