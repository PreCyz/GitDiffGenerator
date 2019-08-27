package pg.gipter.statistic.dao;

import pg.gipter.statistic.dto.GipterUser;

public interface UserDao {

    void updateUserStatistics(GipterUser user);
    boolean isStatisticsAvailable();
}
