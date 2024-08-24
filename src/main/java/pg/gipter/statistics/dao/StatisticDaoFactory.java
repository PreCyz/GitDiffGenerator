package pg.gipter.statistics.dao;

public final class StatisticDaoFactory {
    private StatisticDaoFactory() { }

    public static StatisticDao getStatisticDao() {
        StatisticRepository repository = new StatisticRepository();
        repository.init();
        return repository;
    }
}
