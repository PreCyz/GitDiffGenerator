package pg.gipter.dao;

import pg.gipter.statistic.dto.Statistics;

public interface StatisticDao {

    void updateStatistics(Statistics user);
    boolean isStatisticsAvailable();
}
