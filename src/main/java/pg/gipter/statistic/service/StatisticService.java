package pg.gipter.statistic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.producer.command.VersionControlSystem;
import pg.gipter.core.producer.version.VCSVersionProducer;
import pg.gipter.core.producer.version.VCSVersionProducerFactory;
import pg.gipter.statistic.Statistic;
import pg.gipter.statistic.dao.StatisticDao;
import pg.gipter.statistic.dto.RunDetails;
import pg.gipter.ui.UploadStatus;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/** Created by Pawel Gawedzki on 29-Aug-2019. */
public class StatisticService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticService.class);

    public void updateStatistics(RunDetails runDetails) {
        StatisticDao statisticDao = DaoFactory.getStatisticDao();
        if (statisticDao.isStatisticsAvailable()) {
            Statistic statistic = createStatistics(runDetails);
            statisticDao.updateStatistics(statistic);
        } else {
            logger.info("Statistics are not available and have not been updated.");
        }
    }

    Statistic createStatistics(RunDetails runDetails) {
        ApplicationProperties appProperties = new ArrayList<>(runDetails.getApplicationPropertiesCollection()).get(0);

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        Statistic statistic = new Statistic();
        statistic.setUsername(appProperties.toolkitUsername());
        statistic.setFirstExecutionDate(now);
        statistic.setLastExecutionDate(now);
        statistic.setJavaVersion(System.getProperty("java.version"));
        statistic.setSystemUsers(new HashSet<>(Collections.singletonList(System.getProperty("user.name"))));
        statistic.setLastUpdateStatus(runDetails.getStatus());
        statistic.setLastRunType(runDetails.getRunType());
        if (EnumSet.of(UploadStatus.FAIL, UploadStatus.N_A).contains(runDetails.getStatus())) {
            statistic.setLastFailedDate(now);
        } else if (EnumSet.of(UploadStatus.SUCCESS, UploadStatus.PARTIAL_SUCCESS).contains(runDetails.getStatus())) {
            statistic.setLastSuccessDate(now);
        }
        statistic.setControlSystemMap(createControlSystemMap(runDetails.getApplicationPropertiesCollection()));
        statistic.setApplicationVersion(appProperties.version());

        return statistic;
    }

    Map<VersionControlSystem, Set<String>> createControlSystemMap(Collection<ApplicationProperties> appPropertiesCollection) {
        Map<VersionControlSystem, Set<String>> controlSystemMap = new LinkedHashMap<>();
        for (ApplicationProperties ap : appPropertiesCollection) {
            for (String projectPath : ap.projectPaths()) {
                VersionControlSystem vcs = VersionControlSystem.valueFrom(Paths.get(projectPath).toFile());
                VCSVersionProducer vcsVersionProducer = VCSVersionProducerFactory.getInstance(vcs, projectPath);
                try {
                    if (controlSystemMap.containsKey(vcs)) {
                        controlSystemMap.get(vcs).add(vcsVersionProducer.getVersion());
                    } else {
                        controlSystemMap.put(vcs, Stream.of(vcsVersionProducer.getVersion()).collect(toSet()));
                    }
                } catch (IOException e) {
                    logger.warn("Can not get vcs version for project [{}]", projectPath);
                }
            }
        }
        return controlSystemMap;
    }
}
