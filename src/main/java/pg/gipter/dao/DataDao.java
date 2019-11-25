package pg.gipter.dao;

import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public interface DataDao {

    Optional<Properties> loadDataProperties();
    void saveUploadStatus(String status);
    void saveNextUpload(String nextUploadDateTime);
    void saveDataProperties(Properties properties);

    void convertExistingJob(Set<String> configNames);
}
