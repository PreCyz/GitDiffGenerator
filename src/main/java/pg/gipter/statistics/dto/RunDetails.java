package pg.gipter.statistics.dto;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.statistics.ExceptionDetails;
import pg.gipter.ui.RunType;
import pg.gipter.ui.UploadStatus;

import java.util.Collection;
import java.util.List;

/** Created by Pawel Gawedzki on 29-Aug-2019. */
public class RunDetails {

    private final Collection<ApplicationProperties> applicationPropertiesCollection;
    private final UploadStatus status;
    private final RunType runType;
    private final List<ExceptionDetails> exceptionDetails;

    public RunDetails(Collection<ApplicationProperties> applicationPropertiesCollection,
                      UploadStatus status,
                      RunType runType,
                      List<ExceptionDetails> exceptionDetails
    ) {
        this.applicationPropertiesCollection = applicationPropertiesCollection;
        this.status = status;
        this.runType = runType;
        this.exceptionDetails = exceptionDetails;
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

    public List<ExceptionDetails> getExceptionDetails() {
        return exceptionDetails;
    }
}
