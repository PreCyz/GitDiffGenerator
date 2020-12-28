package pg.gipter.statistics.dao;

public final class StatisticDaoFactory {
    private StatisticDaoFactory() { }

    public static StatisticDao getStatisticDao() {
        return new StatisticDaoImpl();
    }
}
