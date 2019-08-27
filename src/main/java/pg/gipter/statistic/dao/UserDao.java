package pg.gipter.statistic.dao;

import pg.gipter.statistic.dto.Statistics;

public interface UserDao {

    void updateUserStatistics(Statistics user);
    boolean isStatisticsAvailable();
}
