package pg.gipter.converter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConverterFactory {

    private ConverterFactory() { }

    public static List<Converter> getConverters() {
        return Stream.of(
                new PropertiesConverter(),
                new FileNameConverter(),
                new SecurityConverter()
        ).collect(Collectors.toList());
    }
}
