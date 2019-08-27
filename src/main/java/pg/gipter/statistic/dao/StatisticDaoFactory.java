package pg.gipter.statistic.dao;

public final class StatisticDaoFactory {

    private StatisticDaoFactory() { }

    public static StatisticDao getUserDao() {
        return new StatisticDaoImpl();
    }
}