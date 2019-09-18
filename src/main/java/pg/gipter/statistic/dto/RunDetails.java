package pg.gipter.statistic.dto;

import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.RunType;
import pg.gipter.ui.UploadStatus;

import java.util.Collection;

/** Created by Pawel Gawedzki on 29-Aug-2019. */
public class RunDetails {

    private Collection<ApplicationProperties> applicationPropertiesCollection;
    private UploadStatus status;
    private RunType runType;

    public RunDetails(Collection<ApplicationProperties> applicationPropertiesCollection, UploadStatus status, RunType runType) {
        this.applicationPropertiesCollection = applicationPropertiesCollection;
        this.status = status;
        this.runType = runType;
    }

    public Collection<ApplicationProperties> getApplicationPropertiesCollection() {
        return applicationPropertiesCollection;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public RunType getRunType() {
        return runType;
    }
}
