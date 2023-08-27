package pg.gipter.statistics.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.core.producers.vcs.VCSVersionProducer;
import pg.gipter.core.producers.vcs.VCSVersionProducerFactory;
import pg.gipter.statistics.Statistic;
import pg.gipter.statistics.dao.StatisticDao;
import pg.gipter.statistics.dto.RunDetails;
import pg.gipter.ui.UploadStatus;
import pg.gipter.utils.SystemUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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
        statistic.setJavaVersion(SystemUtils.javaVersion());
        statistic.setSystemUsers(new HashSet<>(Collections.singletonList(SystemUtils.userName())));
        statistic.setLastUpdateStatus(runDetails.getStatus());
        statistic.setLastRunType(runDetails.getRunType());
        if (UploadStatus.isFailed(runDetails.getStatus())) {
            statistic.setLastFailedDate(now);
        } else if (UploadStatus.isSuccess(runDetails.getStatus())) {
            statistic.setLastSuccessDate(now);
        }
        statistic.setControlSystemMap(createControlSystemMap(runDetails.getApplicationPropertiesCollection()));
        statistic.setApplicationVersion(appProperties.version().getVersion());
        if (runDetails.getExceptionDetails() != null && !runDetails.getExceptionDetails().isEmpty()) {
            statistic.setExceptions(runDetails.getExceptionDetails());
        } else {
            statistic.setExceptions(Collections.emptyList());
        }

        return statistic;
    }

    Map<VersionControlSystem, Set<String>> createControlSystemMap(Collection<ApplicationProperties> appPropertiesCollection) {
        Map<VersionControlSystem, Set<String>> controlSystemMap = new LinkedHashMap<>();
        for (ApplicationProperties ap : appPropertiesCollection) {
            for (String projectPath : ap.projectPaths()) {
                try {
                    VersionControlSystem vcs = VersionControlSystem.valueFrom(Paths.get(projectPath));
                    VCSVersionProducer vcsVersionProducer = VCSVersionProducerFactory.getInstance(vcs, projectPath);
                    if (controlSystemMap.containsKey(vcs)) {
                        controlSystemMap.get(vcs).add(vcsVersionProducer.getVersion());
                    } else {
                        controlSystemMap.put(vcs, Stream.of(vcsVersionProducer.getVersion()).collect(toSet()));
                    }
                } catch (IOException | IllegalArgumentException e) {
                    logger.warn("VCS problem. Project [{}]. Details: [{}]", projectPath, e.getMessage());
                }
            }
        }
        return controlSystemMap;
    }
}
