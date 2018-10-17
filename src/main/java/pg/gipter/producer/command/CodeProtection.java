package pg.gipter.producer.command;

import java.util.EnumSet;
import java.util.stream.Collectors;

public enum CodeProtection {
    NONE, SIMPLE, STATEMENT;

    public static CodeProtection valueFor(String value) {
        String errMsg;
        try {
            return CodeProtection.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            String supportedValues = EnumSet.allOf(VersionControlSystem.class)
                    .stream()
                    .map(VersionControlSystem::name)
                    .collect(Collectors.joining(", "));
            errMsg = String.format("Given value [%s] is not supported. Supported values are: [%s]%n",
                    value, String.join(", ", supportedValues));
        }
        throw new IllegalArgumentException(errMsg);
    }
}
