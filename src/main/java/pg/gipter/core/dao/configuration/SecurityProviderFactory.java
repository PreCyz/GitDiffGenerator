package pg.gipter.core.dao.configuration;

public final class SecurityProviderFactory {

    private SecurityProviderFactory() { }

    public static SecurityProvider getSecurityProvider() {
        return CipherDetailsReader.getInstance();
    }
}
