package pg.gipter.producer.command;

public final class CommandUtils {

    private CommandUtils() { }

    static boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
