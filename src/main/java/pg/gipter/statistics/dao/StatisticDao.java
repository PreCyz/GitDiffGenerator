package pg.gipter.statistics.dao;

import pg.gipter.statistics.Statistic;

public interface StatisticDao {

    void updateStatistics(Statistic user);
    boolean isStatisticsAvailable();
}
