package pg.gipter.utils;

import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.DataDao;
import pg.gipter.job.upload.JobProperty;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class JobHelper {

    private DataDao dataDao;

    public JobHelper() {
        dataDao = DaoFactory.getDataDao();
    }

    public void updateJobConfigs(String oldConfigName, String newConfigName) {
        Optional<Properties> dataProperties = dataDao.loadDataProperties();
        if (dataProperties.isPresent() && dataProperties.get().containsKey(JobProperty.CONFIGS.value())) {
            Properties data = dataProperties.get();
            LinkedHashSet<String> configs = Stream.of(data.getProperty(JobProperty.CONFIGS.value()).split(","))
                    .collect(toCollection(LinkedHashSet::new));
            if (configs.contains(oldConfigName)) {
                configs = configs.stream()
                        .filter(configName -> !configName.equals(oldConfigName))
                        .collect(toCollection(LinkedHashSet::new));
                configs.add(newConfigName);
                data.put(JobProperty.CONFIGS.value(), String.join(",", configs));
                dataDao.saveDataProperties(data);
            }
        }
    }
}
