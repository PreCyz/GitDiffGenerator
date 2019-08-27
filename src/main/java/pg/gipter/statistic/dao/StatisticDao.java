package pg.gipter.statistic.dao;

import pg.gipter.statistic.dto.Statistics;

public interface StatisticDao {

    void updateStatistics(Statistics user);
    boolean isStatisticsAvailable();
}
