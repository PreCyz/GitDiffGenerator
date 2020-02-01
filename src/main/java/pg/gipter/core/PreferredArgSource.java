package pg.gipter.core;

import java.util.EnumSet;
import java.util.stream.Collectors;

public enum PreferredArgSource {
    CLI, FILE, UI;

    public static PreferredArgSource valueFor(String value) {
        String errMsg;
        try {
            return PreferredArgSource.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            String supportedValues = EnumSet.allOf(PreferredArgSource.class)
                    .stream()
                    .map(PreferredArgSource::name)
                    .collect(Collectors.joining(", "));
            errMsg = String.format("Given value [%s] is not supported. Supported values are: [%s]%n",
                    value, String.join(", ", supportedValues));
        }
        throw new IllegalArgumentException(errMsg);
    }
}
