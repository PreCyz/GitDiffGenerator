package pg.gipter.converters;

import pg.gipter.core.ApplicationProperties;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConverterFactory {

    private ConverterFactory() { }

    public static List<Converter> getConverters(ApplicationProperties applicationProperties) {
        return Stream.of(
                new ItemTypeConverter(),
                new SecurityConverter(),
                new ProgramDataConverter(),
                new CustomCommandConverter(applicationProperties)
        ).collect(Collectors.toList());
    }
}
