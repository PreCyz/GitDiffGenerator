package pg.gipter.dao;

import java.util.Optional;
import java.util.Properties;

public interface DataDao {

    Optional<Properties> loadDataProperties();
    void saveUploadStatus(String status);
    void saveNextUpload(String nextUploadDateTime);
    void saveDataProperties(Properties properties);
}