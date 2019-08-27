package pg.gipter.statistic.dao;

public final class UserDaoFactory {

    private UserDaoFactory() { }

    public static UserDao getUserDao() {
        return new UserDaoImpl();
    }
}