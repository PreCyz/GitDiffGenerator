package pg.gipter.statistic.dao;

import pg.gipter.statistic.Statistic;

public interface StatisticDao {

    void updateStatistics(Statistic user);
    boolean isStatisticsAvailable();
}
