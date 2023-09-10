package pg.gipter.env;

import pg.gipter.Environment;

import java.util.Objects;

public final class EnvSettingsFactory {
    public static EnvSettings getInstance(Environment environment) {
        if (Objects.requireNonNull(environment) == Environment.PROD) {
            return new ProdSettings(environment);
        }
        return new DevSettings(environment);
    }
}
