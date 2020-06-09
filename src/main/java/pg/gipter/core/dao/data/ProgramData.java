package pg.gipter.core.dao.data;

import pg.gipter.jobs.upload.JobParam;
import pg.gipter.ui.UploadStatus;

import java.time.LocalDateTime;

public class ProgramData {
    private JobParam jobParam;
    private UploadStatus uploadStatus;
    private LocalDateTime lastUploadDateTime;

    public ProgramData() {
    }

    public ProgramData(JobParam jobParam, UploadStatus uploadStatus, LocalDateTime lastUploadDateTime) {
        this.jobParam = jobParam;
        this.uploadStatus = uploadStatus;
        this.lastUploadDateTime = lastUploadDateTime;
    }

    public JobParam getJobParam() {
        return jobParam;
    }

    public void setJobParam(JobParam jobParam) {
        this.jobParam = jobParam;
    }

    public UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public LocalDateTime getLastUploadDateTime() {
        return lastUploadDateTime;
    }

    public void setLastUploadDateTime(LocalDateTime lastUploadDateTime) {
        this.lastUploadDateTime = lastUploadDateTime;
    }
}
