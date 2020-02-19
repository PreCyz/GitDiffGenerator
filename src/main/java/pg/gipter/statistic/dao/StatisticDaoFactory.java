package pg.gipter.statistic.dao;

public final class StatisticDaoFactory {
    private StatisticDaoFactory() { }

    public static StatisticDao getStatisticDao() {
        return new StatisticDaoImpl();
    }
}
