package pg.gipter.utils;

import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.jobs.JobParam;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

public class JobHelper {

    private final DataDao dataDao;

    public JobHelper() {
        dataDao = DaoFactory.getDataDao();
    }

    public void updateJobConfigs(String oldConfigName, String newConfigName) {
        Optional<JobParam> jobParamOpt = dataDao.loadJobParam();
        if (jobParamOpt.isPresent() && !jobParamOpt.get().getConfigs().isEmpty()) {
            JobParam jobParam = jobParamOpt.get();
            if (jobParam.getConfigs().contains(oldConfigName)) {
                Set<String> configs = jobParam.getConfigs()
                        .stream()
                        .filter(configName -> !configName.equals(oldConfigName))
                        .collect(toCollection(LinkedHashSet::new));
                configs.add(newConfigName);
                jobParam.setConfigs(configs);
                dataDao.saveJobParam(jobParam);
            }
        }
    }
}
